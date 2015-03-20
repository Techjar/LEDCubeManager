
package com.techjar.ledcm;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL33.*;

import com.techjar.ledcm.util.Model;
import com.techjar.ledcm.util.Quaternion;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector3;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import lombok.Value;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.Color;
import org.lwjgl.util.vector.Matrix4f;

/**
 *
 * @author Techjar
 */
public final class InstancedRenderer {
    private static final Map<Model, Queue<InstanceItem>> items = new HashMap<>();
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

    public static void addItem(Model model, Vector3 position, Quaternion rotation, Color color) {
        if (!items.containsKey(model)) items.put(model, new LinkedList<InstanceItem>());
        items.get(model).add(new InstanceItem(model, position, rotation, color));
    }

    public static int renderAll() {
        int total = 0;
        for (Map.Entry<Model, Queue<InstanceItem>> entry : items.entrySet()) {
            Model model = entry.getKey();
            Queue<InstanceItem> queue = entry.getValue();
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

    @Value private static class InstanceItem {
        private final Model model;
        private final Vector3 position;
        private final Quaternion rotation;
        private final Color color;
    }
}
