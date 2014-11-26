
package com.techjar.cubedesigner.util;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

import com.techjar.cubedesigner.CubeDesigner;
import com.techjar.cubedesigner.RenderHelper;
import com.techjar.cubedesigner.util.logging.LogHelper;
import java.nio.ByteBuffer;
import lombok.Getter;
import org.lwjgl.BufferUtils;

/**
 *
 * @author Techjar
 */
public class Model {
    private static ByteBuffer buffer;
    private int vbo;
    @Getter private int indices;
    private Vector3 center;
    @Getter private float radius;
    @Getter private int faceCount;
    private boolean hasTexCoords;
    //private float[] vertices;
    //private float[] normals;
    //private float[] texCoords;

    public Model(int indices, float[] vertices, float[] normals, float[] texCoords, Vector3 center, float radius, int faceCount) {
        this.indices = indices;
        //this.vertices = vertices;
        //this.normals = normals;
        //this.texCoords = texCoords;
        this.hasTexCoords = texCoords.length > 0;
        this.center = center;
        this.radius = radius;
        this.faceCount = faceCount;
        int dataSize = vertices.length * 4 + normals.length * 2 + texCoords.length * 2;
        if (buffer == null || buffer.capacity() < dataSize) {
            buffer = BufferUtils.createByteBuffer(dataSize);
        } else {
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
            }
        }
        buffer.rewind();
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void render() {
        if (vbo == 0) throw new IllegalStateException("VBO not initialized");
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_NORMAL_ARRAY);
        if (hasTexCoords) glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glVertexPointer(3, GL_FLOAT, hasTexCoords ? 22 : 18, 0);
        glNormalPointer(GL_HALF_FLOAT, hasTexCoords ? 22 : 18, 12);
        if (hasTexCoords) glTexCoordPointer(2, GL_HALF_FLOAT, 22, 18);
        glDrawArrays(GL_TRIANGLES, 0, indices);
        glDisableClientState(GL_VERTEX_ARRAY);
        glDisableClientState(GL_NORMAL_ARRAY);
        if (hasTexCoords) glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        /*glBegin(GL_TRIANGLES);
        for (int i = 0; i < indices; i++) {
            glNormal3f(normals[i * 3], normals[i * 3 + 1], normals[i * 3 + 2]);
            if (hasTexCoords) glTexCoord2f(texCoords[i * 2], texCoords[i * 2 + 1]);
            glVertex3f(vertices[i * 3], vertices[i * 3 + 1], vertices[i * 3 + 2]);
        }
        glEnd();*/
    }

    public boolean render(Vector3 position, Quaternion angle, boolean frustumCheck) {
        if (frustumCheck) {
            if (!isInFrustum(position)) return false;
        }
        glPushMatrix();
        glTranslatef(position.getX(), position.getY(), position.getZ());
        RenderHelper.matrixMultiply(angle.getMatrix());
        render();
        glPopMatrix();
        return true;
    }

    public boolean render(Vector3 position, Quaternion angle) {
        return render(position, angle, true);
    }

    public boolean isInFrustum(Vector3 position) {
        Vector3 pos = center.add(position);
        return CubeDesigner.getFrustum().sphereInFrustum(pos.getX(), pos.getY(), pos.getZ(), radius) > 0;
    }

    public int getVBO() {
        return vbo;
    }

    public Vector3 getCenter() {
        return center.copy();
    }

    public void release() {
        glDeleteBuffers(vbo);
        vbo = 0;
    }
}
