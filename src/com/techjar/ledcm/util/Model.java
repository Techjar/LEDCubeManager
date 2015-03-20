
package com.techjar.ledcm.util;

import static org.lwjgl.opengl.GL15.*;

import com.obj.WavefrontObject;
import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.logging.LogHelper;
import java.nio.ByteBuffer;
import lombok.Getter;
import org.lwjgl.BufferUtils;
import org.newdawn.slick.opengl.Texture;

/**
 *
 * @author Techjar
 */
public class Model {
    private static ByteBuffer buffer;
    private boolean mutable = true;
    private int vbo;
    @Getter private int indices;
    private Vector3 center;
    @Getter private float radius;
    @Getter private int faceCount;
    @Getter private Texture texture;
    @Getter private WavefrontObject collisionMesh;
    private AxisAlignedBB aabb;
    //private float[] vertices;
    //private float[] normals;
    //private float[] texCoords;

    public Model(int indices, float[] vertices, float[] normals, float[] texCoords, Vector3 center, float radius, int faceCount) {
        this.indices = indices;
        //this.vertices = vertices;
        //this.normals = normals;
        //this.texCoords = texCoords;
        boolean hasTexCoords = texCoords.length > 0;
        this.center = center;
        this.radius = radius;
        this.faceCount = faceCount;
        int dataSize = vertices.length * 4 + normals.length * 2 + (hasTexCoords ? texCoords.length * 2 : (vertices.length / 3) * 2 * 2);
        if (buffer == null || buffer.capacity() < dataSize) {
            buffer = BufferUtils.createByteBuffer(dataSize);
        } else {
            buffer.rewind();
            buffer.limit(dataSize);
        }
        LogHelper.info("Model uses %d bytes in VBO.", dataSize);
        for (int i = 0; i < indices; i++) {
            buffer.putFloat(vertices[i * 3]);
            buffer.putFloat(vertices[i * 3 + 1]);
            buffer.putFloat(vertices[i * 3 + 2]);
            buffer.putShort(Util.floatToShortBits(normals[i * 3]));
            buffer.putShort(Util.floatToShortBits(normals[i * 3 + 1]));
            buffer.putShort(Util.floatToShortBits(normals[i * 3 + 2]));
            if (hasTexCoords) {
                buffer.putShort(Util.floatToShortBits(texCoords[i * 2]));
                buffer.putShort(Util.floatToShortBits(texCoords[i * 2 + 1]));
            } else {
                buffer.putShort((short)0);
                buffer.putShort((short)0);
            }
        }
        buffer.rewind();
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public boolean isInFrustum(Vector3 position) {
        Vector3 pos = center.add(position);
        return LEDCubeManager.getFrustum().sphereInFrustum(pos.getX(), pos.getY(), pos.getZ(), radius) > 0;
    }

    public int getVBO() {
        return vbo;
    }

    public Vector3 getCenter() {
        return center.copy();
    }

    public void setTexture(Texture texture) {
        checkMutable();
        this.texture = texture;
    }

    public void setCollisionMesh(WavefrontObject collisionMesh) {
        checkMutable();
        this.collisionMesh = collisionMesh;
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
        glDeleteBuffers(vbo);
        vbo = 0;
    }
}
