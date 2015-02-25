
package com.techjar.ledcm;

import com.obj.Face;
import com.obj.TextureCoordinate;
import com.obj.Vertex;
import com.obj.WavefrontObject;
import com.techjar.ledcm.util.AxisAlignedBB;
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
        File objectFile = null;
        File collisionFile = null;
        Texture texture = textureManager.getTexture("white.png");
        Vector3 scale = new Vector3(1, 1, 1);
        File modelFile = new File(modelPath, file);
        @Cleanup BufferedReader br = new BufferedReader(new FileReader(modelFile));
        String line;
        while ((line = br.readLine()) != null) {
            String[] split = line.split(" ", 2);
            String[] subsplit = split[1].split(" ");
            switch (split[0].toLowerCase()) {
                case "render":
                    if (objectFile != null) throw new IOException("Duplicate \"render\" entry in model file");
                    //object = new WavefrontObject(new File(modelFile.getParent(), split[1]).getAbsolutePath());
                    objectFile = new File(modelFile.getParent(), split[1]);
                    break;
                case "texture":
                    texture = textureManager.getTexture(split[1]);
                    break;
                case "scale":
                    if (subsplit.length == 1) {
                        scale = new Vector3(Float.parseFloat(subsplit[0]), Float.parseFloat(subsplit[0]), Float.parseFloat(subsplit[0]));
                    } else if (subsplit.length == 1) {
                        scale = new Vector3(Float.parseFloat(subsplit[0]), Float.parseFloat(subsplit[1]), Float.parseFloat(subsplit[2]));
                    } else {
                        throw new IOException("Illegal arguments to scale: " + split[1]);
                    }
                    break;
                case "collision":
                    switch (subsplit[1].toLowerCase()) {
                        case "mesh":
                            //collision = new WavefrontObject(new File(modelPath, subsplit[2]).getAbsolutePath());
                            collisionFile = new File(modelPath, subsplit[2]);
                            break;
                    }
                    break;
            }
        }
        if (objectFile == null) throw new IOException("Missing \"render\" entry in model file");
        WavefrontObject object = new WavefrontObject(objectFile.getAbsolutePath(), scale.getX(), scale.getY(), scale.getZ());
        WavefrontObject collision = null;
        if (collisionFile != null) collision = new WavefrontObject(collisionFile.getAbsolutePath(), scale.getX(), scale.getY(), scale.getZ());
        List<Float> vertices = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();
        System.out.println(object.getCurrentGroup().getFaces().size() + " faces.");
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
        Model model = new Model(vertices.size() / 3, Util.floatListToArray(vertices), Util.floatListToArray(normals), Util.floatListToArray(texCoords), center, (float)object.radius, object.getCurrentGroup().getFaces().size());
        model.setTexture(texture);
        model.setCollisionMesh(collision);
        model.setAABB(new AxisAlignedBB(minVertex, maxVertex));
        model.makeImmutable();
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
