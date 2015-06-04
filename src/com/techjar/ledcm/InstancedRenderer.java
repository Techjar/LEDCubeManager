
package com.techjar.ledcm;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL33.*;

import com.techjar.ledcm.util.ModelMesh;
import com.techjar.ledcm.util.Quaternion;
import com.techjar.ledcm.util.ShaderProgram;
import com.techjar.ledcm.util.Tuple;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector3;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
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
    private static final LinkedList<InstanceItem> itemsAlpha = new LinkedList<>();
    private static final LinkedList<Tuple<ModelMesh, LinkedList<InstanceItem>>> groupedNormal = new LinkedList<>();
    private static final LinkedList<Tuple<ModelMesh, LinkedList<InstanceItem>>> groupedAlpha = new LinkedList<>();
    private static final int vboId;
    private static ByteBuffer buffer = BufferUtils.createByteBuffer(8000000);

    static {
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glVertexAttribPointer(3, 4, GL_FLOAT, false, 80, 0);
        glVertexAttribDivisor(3, 1);
        for (int i = 0; i < 4; i++) {
            glVertexAttribPointer(4 + i, 4, GL_FLOAT, false, 80, 16 + (16 * i));
            glVertexAttribDivisor(4 + i, 1);
        }
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    private InstancedRenderer() {
    }

    public static void addItem(ModelMesh mesh, Vector3 position, Quaternion rotation, Color color, Vector3 scale) {
        if (mesh.getModel().getTexture().hasAlpha() || color.getAlpha() < 255) {
            itemsAlpha.add(new InstanceItem(mesh, position, rotation, color, scale));
        } else {
            if (!itemsNormal.containsKey(mesh)) itemsNormal.put(mesh, new LinkedList<InstanceItem>());
            itemsNormal.get(mesh).add(new InstanceItem(mesh, position, rotation, color, scale));
        }
    }

    public static void prepareItems() {
        for (ModelMesh key : itemsNormal.keySet()) {
            LinkedList<InstanceItem> list = itemsNormal.get(key);
            groupedNormal.add(new Tuple<>(key, list));
        }
        itemsNormal.clear();
        Collections.sort(itemsAlpha, new AlphaSorter());
        LinkedList<InstanceItem> currentList = null;
        ModelMesh currentMesh = null;
        for (InstanceItem item = itemsAlpha.poll(); item != null; item = itemsAlpha.poll()) {
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
        int total = 0;
        for (int i = 0; i < 8; i++) glEnableVertexAttribArray(i);
        total += renderItems(groupedNormal);
        total += renderItems(groupedAlpha);
        for (int i = 0; i < 8; i++) glDisableVertexAttribArray(i);
        return total;
    }

    private static int renderItems(LinkedList<Tuple<ModelMesh, LinkedList<InstanceItem>>> items) {
        int total = 0;
        for (Tuple<ModelMesh, LinkedList<InstanceItem>> entry : items) {
            ModelMesh mesh = entry.getA();
            LinkedList<InstanceItem> queue = entry.getB();
            int count = queue.size();
            total += count;
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
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_STREAM_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER, mesh.getVBO());
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 22, 0);
            glVertexAttribPointer(1, 3, GL_HALF_FLOAT, false, 22, 12);
            glVertexAttribPointer(2, 2, GL_HALF_FLOAT, false, 22, 18);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glDrawArraysInstanced(GL_TRIANGLES, 0, mesh.getIndices(), count);
        }
        return total;
    }

    private static class AlphaSorter implements Comparator<InstanceItem> {
        private final Vector3 cameraPos;

        public AlphaSorter() {
            this.cameraPos = LEDCubeManager.getCamera().getPosition();
        }

        @Override
        public int compare(InstanceItem o1, InstanceItem o2) {
            float dist1 = cameraPos.distanceSquared(o1.getPosition());
            float dist2 = cameraPos.distanceSquared(o2.getPosition());
            if (dist1 < dist2) return 1;
            if (dist1 > dist2) return -1;
            return 0;
        }
    }

    @Value private static class InstanceItem {
        private final ModelMesh mesh;
        private final Vector3 position;
        private final Quaternion rotation;
        private final Color color;
        private final Vector3 scale;
    }
}
