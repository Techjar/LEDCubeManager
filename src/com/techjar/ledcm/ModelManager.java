
package com.techjar.ledcm;

import com.obj.Face;
import com.obj.TextureCoordinate;
import com.obj.Vertex;
import com.obj.WavefrontObject;
import com.techjar.ledcm.util.AxisAlignedBB;
import com.techjar.ledcm.util.Material;
import com.techjar.ledcm.util.Model;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector3;
import com.techjar.ledcm.util.logging.LogHelper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;

/**
 *
 * @author Techjar
 */
public class ModelManager {
    protected final File modelPath;
    protected Map<String, Model> cache;
    protected TextureManager textureManager;

    public ModelManager(TextureManager textureManager) {
        this.textureManager = textureManager;
        modelPath = new File("resources/models/");
        cache = new HashMap<>();
    }

    @SneakyThrows(IOException.class)
    public Model getModel(String file) {
        Model cached = cache.get(file);
        if (cached != null) return cached;
        ArrayList<File> objectFiles = null;
        ArrayList<Float> objectLODDists = new ArrayList<>();
        File collisionFile = null;
        Texture texture = textureManager.getTexture("white.png");
        Material material = new Material();
        boolean translucent = false;
        float mass = 0;
        Vector3 scale = new Vector3(1, 1, 1);
        File modelFile = new File(modelPath, file);
        @Cleanup BufferedReader br = new BufferedReader(new FileReader(modelFile));
        String line;
        while ((line = br.readLine()) != null) {
            String[] split = line.split(" ", 2);
            String[] subsplit = split[1].split(" ");
            switch (split[0].toLowerCase()) {
                case "render":
                    if (objectFiles != null) throw new IOException("Duplicate \"render\" entry in model file");
                    else objectFiles = new ArrayList<>();
                    for (int i = 0; i < subsplit.length; i++) {
                        objectFiles.add(new File(modelFile.getParent(), subsplit[i]));
                        if (i + 1 < subsplit.length) {
                            objectLODDists.add(Float.parseFloat(subsplit[i++ + 1]));
                        } else {
                            objectLODDists.add(Float.MAX_VALUE);
                        }
                    }
                    break;
                case "texture":
                    texture = textureManager.getTexture(split[1]);
                    break;
                case "material":
                    for (int i = 0; i < Math.min(subsplit.length, 4); i++) {
                        if ("default".equalsIgnoreCase(subsplit[i])) continue;
                        String[] subsubsplit = subsplit[i].split(",", 3);
                        switch (i) {
                            case 0:
                                material = new Material(new Vector3f(Float.parseFloat(subsubsplit[0]), Float.parseFloat(subsubsplit[1]), Float.parseFloat(subsubsplit[2])), material.diffuse, material.specular, material.shininess);
                                break;
                            case 1:
                                material = new Material(material.ambient, new Vector3f(Float.parseFloat(subsubsplit[0]), Float.parseFloat(subsubsplit[1]), Float.parseFloat(subsubsplit[2])), material.specular, material.shininess);
                                break;
                            case 2:
                                material = new Material(material.ambient, material.diffuse, new Vector3f(Float.parseFloat(subsubsplit[0]), Float.parseFloat(subsubsplit[1]), Float.parseFloat(subsubsplit[2])), material.shininess);
                                break;
                            case 3:
                                material = new Material(material.ambient, material.diffuse, material.specular, Float.parseFloat(subsplit[i]));
                                break;
                        }
                    }
                    break;
                case "translucent":
                    translucent = true;
                    break;
                case "scale":
                    if (subsplit.length == 1) {
                        scale = new Vector3(Float.parseFloat(subsplit[0]), Float.parseFloat(subsplit[0]), Float.parseFloat(subsplit[0]));
                    } else if (subsplit.length >= 3) {
                        scale = new Vector3(Float.parseFloat(subsplit[0]), Float.parseFloat(subsplit[1]), Float.parseFloat(subsplit[2]));
                    } else {
                        throw new IOException("Illegal arguments to scale: " + split[1]);
                    }
                    break;
                case "collision":
                    switch (subsplit[1].toLowerCase()) {
                        case "mesh":
                            collisionFile = new File(modelPath, subsplit[2]);
                            break;
                    }
                    break;
                case "mass":
                    mass = Float.parseFloat(split[1]);
                    break;
            }
        }
        if (objectFiles == null || objectFiles.size() < 1) throw new IOException("Missing or empty \"render\" entry in model file");
        Model model = new Model(objectFiles.size(), texture, material, translucent);
        for (int i = 0; i < objectFiles.size(); i++) {
            File objectFile = objectFiles.get(i);
            WavefrontObject object = new WavefrontObject(objectFile.getAbsolutePath(), scale.getX(), scale.getY(), scale.getZ());
            List<Float> vertices = new ArrayList<>();
            List<Float> normals = new ArrayList<>();
            List<Float> texCoords = new ArrayList<>();
            LogHelper.info("%d faces.", object.getCurrentGroup().getFaces().size());
            for (Face face : object.getCurrentGroup().getFaces()) {
                if (face.getType() != Face.GL_TRIANGLES) throw new IOException("Quads are deprecated, convert the model to triangles");
                for (Vertex vertex : face.getVertices()) {
                    vertices.add(vertex.getX());
                    vertices.add(vertex.getY());
                    vertices.add(vertex.getZ());
                }
                for (Vertex vertex : face.getNormals()) {
                    normals.add(vertex.getX());
                    normals.add(vertex.getY());
                    normals.add(vertex.getZ());
                }
                for (TextureCoordinate texCoord : face.getTextures()) {
                    if (texCoord != null) {
                        texCoords.add(texCoord.getU());
                        texCoords.add(texCoord.getV());
                    }
                }
            }
            Vector3 minVertex = new Vector3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
            Vector3 maxVertex = new Vector3(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
            for (Vertex vertex : object.getVertices()) {
                if (vertex.getX() < minVertex.getX()) minVertex.setX(vertex.getX());
                if (vertex.getY() < minVertex.getY()) minVertex.setY(vertex.getY());
                if (vertex.getZ() < minVertex.getZ()) minVertex.setZ(vertex.getZ());
                if (vertex.getX() > maxVertex.getX()) maxVertex.setX(vertex.getX());
                if (vertex.getY() > maxVertex.getY()) maxVertex.setY(vertex.getY());
                if (vertex.getZ() > maxVertex.getZ()) maxVertex.setZ(vertex.getZ());
            }
            Vector3 center = minVertex.add(maxVertex).divide(2);
            if (model.getAABB() == null) model.setAABB(new AxisAlignedBB(minVertex, maxVertex));
            model.loadMesh(i, objectLODDists.get(i), vertices.size() / 3, Util.floatListToArray(vertices), Util.floatListToArray(normals), Util.floatListToArray(texCoords), center, (float)object.radius, object.getCurrentGroup().getFaces().size());
        }
        model.setPhysicsInfo(collisionFile != null ? new WavefrontObject(collisionFile.getAbsolutePath(), scale.getX(), scale.getY(), scale.getZ()) : null, mass);
        model.makeImmutable();
        LogHelper.info("Finished loading %s", file);
        cache.put(file, model);
        return model;
    }

    public void unloadModel(String file) {
        if (cache.containsKey(file)) {
            cache.remove(file).release();
        }
    }

    public void cleanup() {
        for (Model model : cache.values())
            model.release();
        cache.clear();
    }
}
