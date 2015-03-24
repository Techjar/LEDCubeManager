
package com.techjar.ledcm;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL33.*;

import com.techjar.ledcm.util.ModelMesh;
import com.techjar.ledcm.util.Quaternion;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector3;
import java.nio.ByteBuffer;
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
    private static final Map<ModelMesh, LinkedList<InstanceItem>> itemsAlpha = new HashMap<>();
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

    public static void addItem(ModelMesh model, Vector3 position, Quaternion rotation, Color color) {
        Map<ModelMesh, LinkedList<InstanceItem>> items;
        if (model.getTexture().hasAlpha() || color.getAlpha() < 255) items = itemsAlpha;
        else items = itemsNormal;
        if (!items.containsKey(model)) items.put(model, new LinkedList<InstanceItem>());
        items.get(model).add(new InstanceItem(model, position, rotation, color));
    }

    public static int renderAll() {
        int total = 0;
        /*for (LinkedList<InstanceItem> items : itemsAlpha.values()) {
            Collections.sort(items, new AlphaSorter());
        }*/
        total += renderItems(itemsNormal);
        total += renderItems(itemsAlpha);
        return total;
    }

    private static int renderItems(Map<ModelMesh, LinkedList<InstanceItem>> items) {
        int total = 0;
        for (Map.Entry<ModelMesh, LinkedList<InstanceItem>> entry : items.entrySet()) {
            ModelMesh model = entry.getKey();
            LinkedList<InstanceItem> queue = entry.getValue();
            int count = queue.size();
            total += count;
            int dataSize = count * 80;
            if (buffer == null || buffer.capacity() < dataSize) {
                buffer = BufferUtils.createByteBuffer(dataSize);
            } else {
                buffer.rewind();
                buffer.limit(dataSize);
            }
            for (InstanceItem item = queue.poll(); item != null; item = queue.poll()) {
                Util.storeColorInBuffer(item.getColor(), buffer);
                Matrix4f matrix = new Matrix4f();
                matrix.translate(Util.convertVector(item.getPosition()));
                Matrix4f.mul(matrix, item.getRotation().getMatrix(), matrix);
                Util.storeMatrixInBuffer(matrix, buffer);
            }
            model.getTexture().bind();
            buffer.rewind();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, buffer, GL_STREAM_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER, model.getVBO());
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 22, 0);
            glVertexAttribPointer(1, 3, GL_HALF_FLOAT, false, 22, 12);
            glVertexAttribPointer(2, 2, GL_HALF_FLOAT, false, 22, 18);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            for (int i = 0; i < 8; i++) glEnableVertexAttribArray(i);
            glDrawArraysInstanced(GL_TRIANGLES, 0, model.getIndices(), count);
            for (int i = 0; i < 8; i++) glDisableVertexAttribArray(i);
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
        private final ModelMesh model;
        private final Vector3 position;
        private final Quaternion rotation;
        private final Color color;
    }
}
