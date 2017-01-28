
package com.techjar.ledcm.util;

import static org.lwjgl.opengl.GL15.*;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.logging.LogHelper;
import java.nio.ByteBuffer;

import com.techjar.ledcm.util.math.Vector3;
import lombok.Getter;
import org.lwjgl.BufferUtils;

/**
 *
 * @author Techjar
 */
public class ModelMesh {
	private static ByteBuffer buffer;
	private final int vbo;
	private final float lodDistance;
	@Getter private final int indices;
	private final Vector3 center;
	@Getter private final float radius;
	@Getter private final int faceCount;
	@Getter private final Model model;
	//private float[] vertices;
	//private float[] normals;
	//private float[] texCoords;

	public ModelMesh(float lodDistance, Model model, int indices, float[] vertices, float[] normals, float[] texCoords, Vector3 center, float radius, int faceCount) {
		this.lodDistance = lodDistance;
		this.model = model;
		this.indices = indices;
		//this.vertices = vertices;
		//this.normals = normals;
		//this.texCoords = texCoords;
		boolean hasTexCoords = texCoords.length > 0;
		this.center = center;
		this.radius = radius;
		this.faceCount = faceCount;
		int dataSize = vertices.length * 4 + normals.length * 4 + (hasTexCoords ? texCoords.length * 4 : indices * 2 * 4);
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
			buffer.putFloat(normals[i * 3]);
			buffer.putFloat(normals[i * 3 + 1]);
			buffer.putFloat(normals[i * 3 + 2]);
			if (hasTexCoords) {
				buffer.putFloat(texCoords[i * 2]);
				buffer.putFloat(texCoords[i * 2 + 1]);
			} else {
				buffer.putFloat(0);
				buffer.putFloat(0);
			}
		}
		buffer.rewind();
		vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	public boolean isInFrustum(Vector3 position, Vector3 scale) {
		Vector3 pos = center.add(position);
		float scaleMax = Math.max(Math.max(scale.getX(), scale.getY()), scale.getZ());
		return LEDCubeManager.getFrustum().sphereInFrustum(pos.getX(), pos.getY(), pos.getZ(), radius * scaleMax) > 0;
	}

	public int getVBO() {
		return vbo;
	}

	public float getLODDistance() {
		return lodDistance;
	}

	public Vector3 getCenter() {
		return center;
	}
}
