
package com.techjar.ledcm.util;

import static org.lwjgl.opengl.GL15.*;

import com.obj.WavefrontObject;
import com.techjar.ledcm.render.InstancedRenderer;
import com.techjar.ledcm.util.logging.LogHelper;
import com.techjar.ledcm.LEDCubeManager;

import com.techjar.ledcm.util.math.Quaternion;
import com.techjar.ledcm.util.math.Vector3;
import lombok.Getter;

import org.lwjgl.util.Color;
import org.lwjgl.util.vector.Matrix4f;
import org.newdawn.slick.opengl.Texture;

/**
 *
 * @author Techjar
 */
public class Model {
	private boolean mutable = true;
	private ModelMesh[] meshes;
	@Getter private final Texture texture;
	//@Getter private final Texture normalMap;
	@Getter private final Texture specularMap;
	@Getter private final Material material;
	@Getter private final boolean translucent;
	@Getter private WavefrontObject collisionMesh;
	@Getter private float mass;
	private AxisAlignedBB aabb;

	public Model(int meshCount, Texture texture, /*Texture normalMap,*/ Texture specularMap, Material material, boolean translucent) {
		this.meshes = new ModelMesh[meshCount];
		this.texture = texture;
		//this.normalMap = normalMap;
		this.specularMap = specularMap;
		this.material = material;
		this.translucent = translucent;
	}

	/**
	 * Renders an instance of this model with the specified parameters through the {@link InstancedRenderer}.
	 *
	 * @return the InstanceItem if instanced is true
	 */
	public InstancedRenderer.InstanceItem render(Matrix4f transform, Color color, Vector3 scale, boolean lod, boolean instanced, int textureID) {
		if (instanced && textureID != 0) throw new IllegalArgumentException("textureID cannot be set for instanced render, use non-instanced render instead");
		float distance = LEDCubeManager.getCamera().getPosition().distanceSquared(new Vector3(transform.m30, transform.m31, transform.m32));
		ModelMesh mesh = lod ? getMeshByDistanceSquared(distance - meshes[0].getRadius()) : meshes[0];
		if (instanced) return InstancedRenderer.addItem(mesh, transform, color, scale);
		else InstancedRenderer.draw(mesh, transform, color, scale, textureID);
		return null;
	}

	public InstancedRenderer.InstanceItem render(Matrix4f transform, Color color, Vector3 scale, boolean lod, boolean instanced) {
		return render(transform, color, scale, lod, instanced, 0);
	}

	public InstancedRenderer.InstanceItem render(Matrix4f transform, Color color, Vector3 scale) {
		return render(transform, color, scale, true, true, 0);
	}

	public InstancedRenderer.InstanceItem render(Matrix4f transform, Color color) {
		return render(transform, color, new Vector3(1, 1, 1), true, true, 0);
	}

	public InstancedRenderer.InstanceItem render(Vector3 position, Quaternion rotation, Color color, Vector3 scale, boolean lod, boolean instanced, int textureID) {
		Matrix4f matrix = new Matrix4f();
		matrix.translate(Util.convertVector(position));
		Matrix4f.mul(matrix, rotation.getMatrix(), matrix);
		return render(matrix, color, scale, lod, instanced, textureID);
	}

	public InstancedRenderer.InstanceItem render(Vector3 position, Quaternion rotation, Color color, Vector3 scale, boolean lod, boolean instanced) {
		return render(position, rotation, color, scale, lod, instanced, 0);
	}

	public InstancedRenderer.InstanceItem render(Vector3 position, Quaternion rotation, Color color, Vector3 scale) {
		return render(position, rotation, color, scale, true, true, 0);
	}

	public InstancedRenderer.InstanceItem render(Vector3 position, Quaternion rotation, Color color) {
		return render(position, rotation, color, new Vector3(1, 1, 1), true, true, 0);
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

	public ModelMesh getMeshByDistanceSquared(float distance) {
		for (int i = 0; i < meshes.length - 1; i++) {
			if (distance < meshes[i].getLODDistance() * meshes[i].getLODDistance()) {
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
			LogHelper.fine("Deleted model VBO: %d", mesh.getVBO());
		}
		meshes = new ModelMesh[meshes.length];
	}
}
