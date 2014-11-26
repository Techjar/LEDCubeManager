
package com.techjar.cubedesigner;

import com.obj.Face;
import com.obj.TextureCoordinate;
import com.obj.Vertex;
import com.obj.WavefrontObject;
import com.techjar.cubedesigner.util.Model;
import com.techjar.cubedesigner.util.Util;
import com.techjar.cubedesigner.util.Vector3;
import com.techjar.cubedesigner.util.logging.LogHelper;
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

/**
 *
 * @author Techjar
 */
public class ModelManager {
    protected final File modelPath;
    protected Map<String, Model> cache;

    public ModelManager() {
        modelPath = new File("resources/models/");
        cache = new HashMap<>();
    }

    @SneakyThrows(IOException.class)
    public Model getModel(String file) {
        Model cached = cache.get(file);
        if (cached != null) return cached;
        WavefrontObject object = null;
        File modelFile = new File(modelPath, file);
        @Cleanup BufferedReader br = new BufferedReader(new FileReader(modelFile));
        String line;
        while ((line = br.readLine()) != null) {
            String[] split = line.split(" ", 2);
            switch (split[0].toLowerCase()) {
                case "render":
                    if (object != null) throw new IOException("Duplicate \"render\" entry in model file");
                    object = new WavefrontObject(new File(modelFile.getParent(), split[1]).getAbsolutePath());
                    break;
                case "collision":
                    // TODO
                    break;
            }
        }
        if (object == null) throw new IOException("Missing \"render\" entry in model file");
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
        // TODO: collision mesh loading
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
