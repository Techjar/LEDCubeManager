
package com.techjar.ledcm.util;

import static org.lwjgl.opengl.GL15.*;

import com.obj.WavefrontObject;
import com.techjar.ledcm.render.InstancedRenderer;
import com.techjar.ledcm.LEDCubeManager;

import lombok.Getter;

import org.lwjgl.opengl.Display;
import org.lwjgl.util.Color;
import org.newdawn.slick.opengl.Texture;

/**
 *
 * @author Techjar
 */
public class Model {
    private boolean mutable = true;
    private ModelMesh[] meshes;
    @Getter private final Texture texture;
    @Getter private final Material material;
    @Getter private final boolean translucent;
    @Getter private WavefrontObject collisionMesh;
    @Getter private float mass;
    private AxisAlignedBB aabb;

    public Model(int meshCount, Texture texture, Material material, boolean translucent) {
        this.meshes = new ModelMesh[meshCount];
        this.texture = texture;
        this.material = material;
        this.translucent = translucent;
    }

    /**
     * Renders an instance of this model with the specified parameters through the {@link InstancedRenderer}.
     *
     * @return Number of faces in the chosen mesh.
     */
    public void render(Vector3 position, Quaternion rotation, Color color, Vector3 scale, boolean lod, boolean instanced) {
        float distance = LEDCubeManager.getCamera().getPosition().distance(position);
        ModelMesh mesh = lod ? getMeshByDistance(distance - meshes[0].getRadius()) : meshes[0];
        if (instanced) InstancedRenderer.addItem(mesh, position, rotation, color, scale);
        else InstancedRenderer.draw(mesh, position, rotation, color, scale);
    }

    public void render(Vector3 position, Quaternion rotation, Color color, Vector3 scale) {
        render(position, rotation, color, scale, true, true);
    }

    public void render(Vector3 position, Quaternion rotation, Color color) {
        render(position, rotation, color, new Vector3(1, 1, 1), true, true);
    }

    public void loadMesh(int lod, float lodDistance, int indices, float[] vertices, float[] normals, float[] texCoords, Vector3 center, float radius, int faceCount) {
        checkMutable();
        meshes[lod] = new ModelMesh(lodDistance, this, indices, vertices, normals, texCoords, center, radius, faceCount);
    }

    public ModelMesh getMesh(int lod) {
        return meshes[lod];
    }

    public ModelMesh getMeshByDistance(float distance) {
        for (int i = 0; i < meshes.length - 1; i++) {
            if (distance < meshes[i].getLODDistance()) {
                return meshes[i];
            }
        }
        return meshes[meshes.length - 1];
    }

    public void setPhysicsInfo(WavefrontObject collisionMesh, float mass) {
        checkMutable();
        this.collisionMesh = collisionMesh;
        this.mass = mass;
    }

    public AxisAlignedBB getAABB() {
        return aabb;
    }

    public void setAABB(AxisAlignedBB aabb) {
        checkMutable();
        this.aabb = aabb;
    }

    public void makeImmutable() {
        mutable = false;
    }

    private void checkMutable() {
        if (!mutable) throw new IllegalStateException("model is immutable");
    }

    public void release() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (ModelMesh mesh : meshes) {
            glDeleteBuffers(mesh.getVBO());
        }
        meshes = new ModelMesh[meshes.length];
    }
}
