
package com.techjar.ledcm.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL43.*;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.techjar.ledcm.util.math.Quaternion;
import com.techjar.ledcm.util.math.Vector3;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.Setter;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.Color;
import org.lwjgl.util.vector.Matrix4f;

/**
 * Handles OpenGL instanced rendering, which offers up to or exceeding an order of magnitude better performance.
 *
 * @author Techjar
 */
public final class InstancedRenderer {
	private static final Map<ModelMesh, Set<InstanceItem>> itemsNormal = new HashMap<>();
	private static final Set<InstanceItem> itemsAlpha = new LinkedHashSet<>();
	private static final List<Tuple<ModelMesh, List<InstanceItem>>> groupedNormal = new LinkedList<>();
	private static final List<Tuple<ModelMesh, List<InstanceItem>>> groupedAlpha = new LinkedList<>();
	private static final List<Tuple<Integer, Integer>> vboIds = new ArrayList<>();
	private static int vaoId;
	private static int alphaTrickVboId;
	private static boolean alphaPolygonFix = false;
	private static int currentVBOIndex = 0;
	private static final int maxVboCount = 64;
	private static ByteBuffer zeroBuffer = BufferUtils.createByteBuffer(500000);
	private static ByteBuffer buffer = BufferUtils.createByteBuffer(8000000);
	private static Matrix4f bufferMatrix = new Matrix4f();

	private InstancedRenderer() {
	}

	public static void init() { // bindingindex 0 = mesh (stride = 22), 1 = instances (stride = 80)
		if (vaoId != 0) throw new IllegalStateException("Already initialized!");
		vaoId = glGenVertexArrays();
		glBindVertexArray(vaoId);
		for (int i = 0; i < 8; i++) glEnableVertexAttribArray(i);
		glVertexAttribFormat(0, 3, GL_FLOAT, false, 0);
		glVertexAttribBinding(0, 0);
		glVertexAttribFormat(1, 3, GL_FLOAT, false, 12);
		glVertexAttribBinding(1, 0);
		glVertexAttribFormat(2, 2, GL_FLOAT, false, 24);
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

	public static InstanceItem addItem(ModelMesh mesh, Matrix4f transform, Color color, Vector3 scale) {
		InstanceItem item;
		if (mesh.getModel().isTranslucent() || color.getAlpha() < 255) {
			itemsAlpha.add(item = new InstanceItem(mesh, new Vector3(transform.m30, transform.m31, transform.m32), transform, color, scale));
		} else {
			if (!itemsNormal.containsKey(mesh)) itemsNormal.put(mesh, new LinkedHashSet<>());
			itemsNormal.get(mesh).add(item = new InstanceItem(mesh, new Vector3(transform.m30, transform.m31, transform.m32), transform, color, scale));
		}
		return item;
	}

	public static void removeItem(InstanceItem item) {
		if (itemsNormal.containsKey(item.getMesh())) {
			itemsNormal.get(item.getMesh()).remove(item);
		}
		itemsAlpha.remove(item);
	}

	public static void prepareItems() {
		groupedNormal.clear();
		groupedAlpha.clear();
		itemsNormal.entrySet().forEach(entry -> {
			groupedNormal.add(new Tuple<>(entry.getKey(), entry.getValue().stream().filter(item -> item.getMesh().isInFrustum(item.getPosition(), item.getScale())).collect(Collectors.toList())));
		});
		List<InstanceItem> itemsAlpha2 = itemsAlpha.stream().filter(item -> item.getMesh().isInFrustum(item.getPosition(), item.getScale())).parallel().sorted(new AlphaSorter()).collect(Collectors.toList());
		LinkedList<InstanceItem> currentList = null;
		ModelMesh currentMesh = null;
		for (InstanceItem item : itemsAlpha2) {
			if (item.getMesh() != currentMesh) {
				currentMesh = item.getMesh();
				currentList = new LinkedList<>();
				groupedAlpha.add(new Tuple<>(item.getMesh(), currentList));
			}
			currentList.add(item);
		}
	}

	public static void clearItems() {
		itemsNormal.clear();
		itemsAlpha.clear();
		groupedNormal.clear();
		groupedAlpha.clear();
	}

	public static int[] renderAll() {
		int[] totals1 = renderItems(groupedNormal, false);
		int[] totals2 = renderItems(groupedAlpha, alphaPolygonFix);
		//for (int i = 0; i < 8; i++) glDisableVertexAttribArray(i);
		return new int[]{totals1[0] + totals2[0], totals1[1] + totals2[1]};
	}

	public static void resetVBOIndex() {
		currentVBOIndex = 0;
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

	private static int[] renderItems(List<Tuple<ModelMesh, List<InstanceItem>>> items, boolean alphaDepthTrick) {
		int total = 0;
		int totalFaces = 0;
		for (Tuple<ModelMesh, List<InstanceItem>> entry : items) {
			ModelMesh mesh = entry.getA();
			List<InstanceItem> queue = entry.getB();
			int count = queue.size();
			total += count;
			totalFaces += mesh.getFaceCount() * count;
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
						bufferMatrix.setIdentity();
						Matrix4f.mul(bufferMatrix, item.getTransform(), bufferMatrix);
						bufferMatrix.scale(Util.convertVector(item.getScale()));
						Util.storeMatrixInBuffer(bufferMatrix, buffer);

						glActiveTexture(GL_TEXTURE0);
						mesh.getModel().getTexture().bind();
						//glActiveTexture(GL_TEXTURE1);
						//mesh.getModel().getNormalMap().bind();
						glActiveTexture(GL_TEXTURE2);
						mesh.getModel().getSpecularMap().bind();
						glActiveTexture(GL_TEXTURE0);
						mesh.getModel().getMaterial().sendToShader(0);
						buffer.rewind();
						Tuple<Integer, Integer> vbo = getVBO(-1);
						glBindBuffer(GL_ARRAY_BUFFER, vbo.getA());
						glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
						glBindBuffer(GL_ARRAY_BUFFER, 0);
						glBindVertexArray(vaoId);
						glBindVertexBuffer(0, mesh.getVBO(), 0, 32);
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
					bufferMatrix.setIdentity();
					Matrix4f.mul(bufferMatrix, item.getTransform(), bufferMatrix);
					bufferMatrix.scale(Util.convertVector(item.getScale()));
					Util.storeMatrixInBuffer(bufferMatrix, buffer);
				}
				glActiveTexture(GL_TEXTURE0);
				mesh.getModel().getTexture().bind();
				//glActiveTexture(GL_TEXTURE1);
				//mesh.getModel().getNormalMap().bind();
				glActiveTexture(GL_TEXTURE2);
				mesh.getModel().getSpecularMap().bind();
				glActiveTexture(GL_TEXTURE0);
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
				glBindVertexBuffer(0, mesh.getVBO(), 0, 32);
				glBindVertexBuffer(1, vbo.getA(), 0, 80);
				glDrawArraysInstanced(GL_TRIANGLES, 0, mesh.getIndices(), count);
				glBindVertexArray(0);
			}
		}
		return new int[]{total, totalFaces};
	}

	/**
	 * Draws a single non-instanced mesh
	 */
	public static int draw(ModelMesh mesh, Matrix4f transform, Color color, Vector3 scale, int textureID) {
		int dataSize = 80;
		if (buffer == null || buffer.capacity() < dataSize) {
			buffer = BufferUtils.createByteBuffer(dataSize);
		} else {
			buffer.rewind();
			buffer.limit(dataSize);
		}
		buffer.rewind();
		Util.storeColorInBuffer(color, buffer);
		Matrix4f matrix = new Matrix4f();
		Matrix4f.mul(matrix, transform, matrix);
		matrix.scale(Util.convertVector(scale));
		Util.storeMatrixInBuffer(matrix, buffer);

		glActiveTexture(GL_TEXTURE0);
		if (textureID != 0) glBindTexture(GL_TEXTURE_2D, textureID);
		else mesh.getModel().getTexture().bind();
		//glActiveTexture(GL_TEXTURE1);
		//mesh.getModel().getNormalMap().bind();
		glActiveTexture(GL_TEXTURE2);
		mesh.getModel().getSpecularMap().bind();
		glActiveTexture(GL_TEXTURE0);
		mesh.getModel().getMaterial().sendToShader(0);
		buffer.rewind();
		Tuple<Integer, Integer> vbo = getVBO(-1);
		glBindBuffer(GL_ARRAY_BUFFER, vbo.getA());
		glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(vaoId);
		glBindVertexBuffer(0, mesh.getVBO(), 0, 32);
		glBindVertexBuffer(1, vbo.getA(), 0, 80);
		if (alphaPolygonFix && (mesh.getModel().isTranslucent() || color.getAlpha() < 255)) {
			glColorMask(false, false, false, false);
			glDrawArrays(GL_TRIANGLES, 0, mesh.getIndices());
			glColorMask(true, true, true, true);
			glDepthFunc(GL_EQUAL);
			glDrawArrays(GL_TRIANGLES, 0, mesh.getIndices());
			glDepthFunc(GL_LEQUAL);
		} else {
			glDrawArrays(GL_TRIANGLES, 0, mesh.getIndices());
		}
		glBindVertexArray(0);
		return mesh.getFaceCount();
	}

	private static class AlphaSorter implements Comparator<InstanceItem> {
		@Override
		public int compare(InstanceItem o1, InstanceItem o2) {
			float dist1 = LEDCubeManager.getCamera().getPosition().distanceSquared(o1.getPosition());
			float dist2 = LEDCubeManager.getCamera().getPosition().distanceSquared(o2.getPosition());
			if (dist1 < dist2) return 1;
			if (dist1 > dist2) return -1;
			return 0;
		}
	}

	@AllArgsConstructor
	public static class InstanceItem {
		@Getter private ModelMesh mesh;
		@Getter private Vector3 position;
		@Getter private Matrix4f transform;
		@Getter @Setter private Color color;
		@Getter @Setter private Vector3 scale;

		public void setTransform(Vector3 position, Quaternion rotation) {
			this.position = position;
			Matrix4f matrix = new Matrix4f();
			matrix.translate(Util.convertVector(position));
			Matrix4f.mul(matrix, rotation.getMatrix(), matrix);
			this.transform = matrix;
		}

		public void setTransform(Matrix4f transform) {
			this.transform = transform;
			this.position = new Vector3(transform.m30, transform.m31, transform.m32);
		}
	}
}
