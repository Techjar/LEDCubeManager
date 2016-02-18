
package com.techjar.ledcm.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL43.*;

import com.techjar.ledcm.util.ModelMesh;
import com.techjar.ledcm.util.Quaternion;
import com.techjar.ledcm.util.Tuple;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector3;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Value;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.Color;
import org.lwjgl.util.vector.Matrix4f;

/**
 * Handles OpenGL instanced rendering, which offers up to or exceeding an order of magnitude better performance.
 *
 * @author Techjar
 */
public final class InstancedRenderer {
    private static final Map<ModelMesh, LinkedList<InstanceItem>> itemsNormal = new HashMap<>();
    private static final List<InstanceItem> itemsAlpha = new ArrayList<>();
    private static final LinkedList<Tuple<ModelMesh, LinkedList<InstanceItem>>> groupedNormal = new LinkedList<>();
    private static final LinkedList<Tuple<ModelMesh, LinkedList<InstanceItem>>> groupedAlpha = new LinkedList<>();
    private static final List<Tuple<Integer, Integer>> vboIds = new ArrayList<>();
    private static int vaoId;
    private static int alphaTrickVboId;
    private static boolean alphaPolygonFix = false;
    private static int currentVBOIndex = 0;
    private static final int maxVboCount = 64;
    private static ByteBuffer zeroBuffer = BufferUtils.createByteBuffer(500000);
    private static ByteBuffer buffer = BufferUtils.createByteBuffer(8000000);

    private InstancedRenderer() {
    }

    public static void init() { // bindingindex 0 = mesh (stride = 22), 1 = instances (stride = 80)
        if (vaoId != 0) throw new IllegalStateException("Already initialized!");
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);
        for (int i = 0; i < 8; i++) glEnableVertexAttribArray(i);
        glVertexAttribFormat(0, 3, GL_FLOAT, false, 0);
        glVertexAttribBinding(0, 0);
        glVertexAttribFormat(1, 3, GL_HALF_FLOAT, false, 12);
        glVertexAttribBinding(1, 0);
        glVertexAttribFormat(2, 2, GL_HALF_FLOAT, false, 18);
        glVertexAttribBinding(2, 0);
        glVertexAttribFormat(3, 4, GL_FLOAT, false, 0);
        glVertexAttribBinding(3, 1);
        for (int i = 0; i < 4; i++) {
            glVertexAttribFormat(4 + i, 4, GL_FLOAT, false, 16 + (16 * i));
            glVertexAttribBinding(4 + i, 1);
        }
        glVertexBindingDivisor(1, 1);
        glBindVertexArray(0);
    }

    public static boolean getAlphaPolygonFix() {
        return alphaPolygonFix;
    }

    /**
     * Toggles a fix for alpha sorting at the polygon level using a depth buffer trick, with the downside that each translucent object is drawn separately rather than being instanced.
     * @param alphaPolygonFix
     */
    public static void setAlphaPolygonFix(boolean alphaPolygonFix) {
        InstancedRenderer.alphaPolygonFix = alphaPolygonFix;
    }

    public static void addItem(ModelMesh mesh, Vector3 position, Quaternion rotation, Color color, Vector3 scale, float cameraDist) {
        if (mesh.getModel().isTranslucent() || color.getAlpha() < 255) {
            itemsAlpha.add(new InstanceItem(mesh, position, rotation, color, scale, cameraDist));
        } else {
            if (!itemsNormal.containsKey(mesh)) itemsNormal.put(mesh, new LinkedList<InstanceItem>());
            itemsNormal.get(mesh).add(new InstanceItem(mesh, position, rotation, color, scale, cameraDist));
        }
    }

    public static void prepareItems() {
        groupedNormal.clear();
        groupedAlpha.clear();
        for (ModelMesh key : itemsNormal.keySet()) {
            LinkedList<InstanceItem> list = itemsNormal.get(key);
            groupedNormal.add(new Tuple<>(key, list));
        }
        Collections.sort(itemsAlpha, new AlphaSorter());
        LinkedList<InstanceItem> currentList = null;
        ModelMesh currentMesh = null;
        for (InstanceItem item : itemsAlpha) {
            if (item.getMesh() != currentMesh) {
                currentMesh = item.getMesh();
                currentList = new LinkedList<>();
                groupedAlpha.add(new Tuple<>(item.getMesh(), currentList));
            }
            currentList.add(item);
        }
    }

    public static void resetItems() {
        itemsNormal.clear();
        itemsAlpha.clear();
        groupedNormal.clear();
        groupedAlpha.clear();
    }

    public static int renderAll() {
        currentVBOIndex = 0;
        int total = 0;
        total += renderItems(groupedNormal, false);
        total += renderItems(groupedAlpha, alphaPolygonFix);
        for (int i = 0; i < 8; i++) glDisableVertexAttribArray(i);
        return total;
    }

    private static Tuple<Integer, Integer> getNextVBO() {
        Tuple<Integer, Integer> vbo = getVBO(currentVBOIndex++);
        if (currentVBOIndex >= maxVboCount) currentVBOIndex = 0;
        return vbo;
    }

    private static Tuple<Integer, Integer> getVBO(int index) {
        if (index == -1) {
            if (alphaTrickVboId == 0) {
                alphaTrickVboId = glGenBuffers();
                glBindBuffer(GL_ARRAY_BUFFER, alphaTrickVboId);
                zeroBuffer.limit(80);
                glBufferData(GL_ARRAY_BUFFER, zeroBuffer, GL_STREAM_DRAW);
                glBindBuffer(GL_ARRAY_BUFFER, 0);
            }
            return new Tuple<>(alphaTrickVboId, 80);
        }
        if (index >= vboIds.size() || vboIds.get(index) == null) {
            int vboId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            zeroBuffer.limit(500000);
            glBufferData(GL_ARRAY_BUFFER, zeroBuffer, GL_STREAM_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            vboIds.add(index, new Tuple<>(vboId, 500000));
        }
        return vboIds.get(index);
    }

    private static int renderItems(LinkedList<Tuple<ModelMesh, LinkedList<InstanceItem>>> items, boolean alphaDepthTrick) {
        int total = 0;
        for (Tuple<ModelMesh, LinkedList<InstanceItem>> entry : items) {
            ModelMesh mesh = entry.getA();
            LinkedList<InstanceItem> queue = entry.getB();
            int count = queue.size();
            total += count;
            if (alphaDepthTrick) { // Individual draw for alpha polygon trick
                int dataSize = 80;
                if (buffer == null || buffer.capacity() < dataSize) {
                    buffer = BufferUtils.createByteBuffer(dataSize);
                } else {
                    buffer.rewind();
                    buffer.limit(dataSize);
                }
                for (InstanceItem item : queue) {
                    buffer.rewind();
                    Util.storeColorInBuffer(item.getColor(), buffer);
                    Matrix4f matrix = new Matrix4f();
                    matrix.translate(Util.convertVector(item.getPosition()));
                    matrix.scale(Util.convertVector(item.getScale()));
                    Matrix4f.mul(matrix, item.getRotation().getMatrix(), matrix);
                    Util.storeMatrixInBuffer(matrix, buffer);

                    glActiveTexture(GL_TEXTURE0);
                    mesh.getModel().getTexture().bind();
                    //glActiveTexture(GL_TEXTURE1);
                    //mesh.getModel().getNormalMap().bind();
                    mesh.getModel().getMaterial().sendToShader(0);
                    buffer.rewind();
                    Tuple<Integer, Integer> vbo = getVBO(-1);
                    glBindBuffer(GL_ARRAY_BUFFER, vbo.getA());
                    glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
                    glBindBuffer(GL_ARRAY_BUFFER, 0);
                    glBindVertexArray(vaoId);
                    glBindVertexBuffer(0, mesh.getVBO(), 0, 22);
                    glBindVertexBuffer(1, vbo.getA(), 0, 80);
                    glColorMask(false, false, false, false);
                    glDrawArrays(GL_TRIANGLES, 0, mesh.getIndices());
                    glColorMask(true, true, true, true);
                    glDepthFunc(GL_EQUAL);
                    glDrawArrays(GL_TRIANGLES, 0, mesh.getIndices());
                    glDepthFunc(GL_LEQUAL);
                    glBindVertexArray(0);
                }
            } else { // Instanced render
                int dataSize = count * 80;
                if (buffer == null || buffer.capacity() < dataSize) {
                    buffer = BufferUtils.createByteBuffer(dataSize);
                } else {
                    buffer.rewind();
                    buffer.limit(dataSize);
                }
                for (InstanceItem item : queue) {
                    Util.storeColorInBuffer(item.getColor(), buffer);
                    Matrix4f matrix = new Matrix4f();
                    matrix.translate(Util.convertVector(item.getPosition()));
                    matrix.scale(Util.convertVector(item.getScale()));
                    Matrix4f.mul(matrix, item.getRotation().getMatrix(), matrix);
                    Util.storeMatrixInBuffer(matrix, buffer);
                }
                glActiveTexture(GL_TEXTURE0);
                mesh.getModel().getTexture().bind();
                //glActiveTexture(GL_TEXTURE1);
                //mesh.getModel().getNormalMap().bind();
                mesh.getModel().getMaterial().sendToShader(0);
                buffer.rewind();
                Tuple<Integer, Integer> vbo = getNextVBO();
                glBindBuffer(GL_ARRAY_BUFFER, vbo.getA());
                if (vbo.getB() < dataSize) {
                    glBufferData(GL_ARRAY_BUFFER, buffer, GL_STREAM_DRAW);
                    vbo.setB(dataSize);
                }
                else glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
                glBindBuffer(GL_ARRAY_BUFFER, 0);
                glBindVertexArray(vaoId);
                glBindVertexBuffer(0, mesh.getVBO(), 0, 22);
                glBindVertexBuffer(1, vbo.getA(), 0, 80);
                glDrawArraysInstanced(GL_TRIANGLES, 0, mesh.getIndices(), count);
                glBindVertexArray(0);
            }
        }
        return total;
    }

    private static class AlphaSorter implements Comparator<InstanceItem> {
        volatile float crap = 2F;

        @Override
        public int compare(InstanceItem o1, InstanceItem o2) {
            if (o1.cameraDistSqr < o2.cameraDistSqr) return 1;
            if (o1.cameraDistSqr > o2.cameraDistSqr) return -1;
            return 0;
        }
    }

    @Value private static class InstanceItem {
        private final ModelMesh mesh;
        private final Vector3 position;
        private final Quaternion rotation;
        private final Color color;
        private final Vector3 scale;
        private final float cameraDistSqr;
    }
}
