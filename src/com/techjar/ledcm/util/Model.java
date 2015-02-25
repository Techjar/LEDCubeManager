
package com.techjar.ledcm.util;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL33.*;

import com.obj.WavefrontObject;
import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.RenderHelper;
import com.techjar.ledcm.util.logging.LogHelper;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import lombok.Getter;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.Color;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;

/**
 *
 * @author Techjar
 */
public class Model {
    private static ByteBuffer buffer;
    private static final FloatBuffer matrixBuffer;
    private static final FloatBuffer colorBuffer;
    private static final int colorVbo;
    private static final int matrixVbo;
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

    static {
        matrixBuffer = BufferUtils.createFloatBuffer(16);
        colorBuffer = BufferUtils.createFloatBuffer(4);
        colorVbo = glGenBuffers();
        matrixVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, colorVbo);
        glVertexAttribPointer(3, 4, GL_FLOAT, false, 16, 0);
        glVertexAttribDivisor(3, 1);
        glBindBuffer(GL_ARRAY_BUFFER, matrixVbo);
        for (int i = 0; i < 4; i++) {
            glVertexAttribPointer(4 + i, 4, GL_FLOAT, false, 64, 16 * i);
            glVertexAttribDivisor(4 + i, 1);
        }
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

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
        //glVertexAttribPointer(0, 3, GL_FLOAT, false, 22, 0);
        //glVertexAttribPointer(1, 3, GL_HALF_FLOAT, false, 22, 12);
        //glVertexAttribPointer(2, 2, GL_HALF_FLOAT, false, 22, 18);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void render(Color color) {
        if (vbo == 0) throw new IllegalStateException("VBO not initialized");
        texture.bind();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 22, 0);
        glVertexAttribPointer(1, 3, GL_HALF_FLOAT, false, 22, 12);
        glVertexAttribPointer(2, 2, GL_HALF_FLOAT, false, 22, 18);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (int i = 0; i < 8; i++) glEnableVertexAttribArray(i);
        glDrawArrays(GL_TRIANGLES, 0, indices);
        for (int i = 0; i < 8; i++) glDisableVertexAttribArray(i);
        /*glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_NORMAL_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glVertexPointer(3, GL_FLOAT, 22, 0);
        glNormalPointer(GL_HALF_FLOAT, 22, 12);
        glTexCoordPointer(2, GL_HALF_FLOAT, 22, 18);
        glDrawArrays(GL_TRIANGLES, 0, indices);
        glDisableClientState(GL_VERTEX_ARRAY);
        glDisableClientState(GL_NORMAL_ARRAY);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glBindBuffer(GL_ARRAY_BUFFER, 0);*/
        /*glBegin(GL_TRIANGLES);
        for (int i = 0; i < indices; i++) {
            glNormal3f(normals[i * 3], normals[i * 3 + 1], normals[i * 3 + 2]);
            if (hasTexCoords) glTexCoord2f(texCoords[i * 2], texCoords[i * 2 + 1]);
            glVertex3f(vertices[i * 3], vertices[i * 3 + 1], vertices[i * 3 + 2]);
        }
        glEnd();*/
    }

    public void render() {
        render(new Color(255, 255, 255));
    }

    public boolean render(Vector3 position, Quaternion quat, Color color, boolean frustumCheck) {
        if (frustumCheck) {
            if (!isInFrustum(position)) return false;
        }
        //glPushMatrix();
        //glTranslatef(position.getX(), position.getY(), position.getZ());
        //RenderHelper.matrixMultiply(angle.getMatrix());
        glBindBuffer(GL_ARRAY_BUFFER, colorVbo);
        colorBuffer.rewind();
        Util.storeColorInBuffer(color, colorBuffer);
        colorBuffer.rewind();
        glBufferData(GL_ARRAY_BUFFER, colorBuffer, GL_STREAM_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, matrixVbo);
        Matrix4f matrix = new Matrix4f();
        matrix.translate(Util.convertVector(position));
        Matrix4f.mul(matrix, quat.getMatrix(), matrix);
        matrixBuffer.rewind();
        matrix.store(matrixBuffer);
        matrixBuffer.rewind();
        glBufferData(GL_ARRAY_BUFFER, matrixBuffer, GL_STREAM_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        render(color);
        //glPopMatrix();
        return true;
    }

    public boolean render(Vector3 position, Quaternion quat, boolean frustumCheck) {
        return render(position, quat, new Color(255, 255, 255), frustumCheck);
    }

    public boolean render(Vector3 position, Quaternion quat, Color color) {
        return render(position, quat, color, true);
    }

    public boolean render(Vector3 position, Quaternion quat) {
        return render(position, quat, new Color(255, 255, 255), true);
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
