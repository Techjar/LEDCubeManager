
package com.techjar.ledcm.util.math;

import com.techjar.ledcm.util.Util;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;

/**
 *
 * @author Techjar
 */
public class Quaternion {
	private float w;
	private float x;
	private float y;
	private float z;

	public Quaternion() {
		this.w = 1;
	}

	public Quaternion(float w, float x, float y, float z) {
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Quaternion(Quaternion other) {
		this.w = other.w;
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
	}

	public Quaternion(org.lwjgl.util.vector.Quaternion other) {
		this.w = other.w;
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
	}

	public Quaternion(Vector3 vector, float rotation) {
		rotation = (float)Math.toRadians(rotation);
		float sinRot = (float)Math.sin(rotation / 2);
		w = (float)Math.cos(rotation / 2);
		x = vector.x * sinRot;
		y = vector.y * sinRot;
		z = vector.z * sinRot;
	}

	public Quaternion(Axis axis, float rotation) {
		this(axis.getVector(), rotation);
	}

	public Quaternion(float pitch, float yaw, float roll, Angle.Order order) {
		// X Pitch Y Yaw Z Roll
		// YXZ Order
		Quaternion quatX = new Quaternion(new Vector3(1, 0, 0), pitch);
		Quaternion quatY = new Quaternion(new Vector3(0, 1, 0), yaw);
		Quaternion quatZ = new Quaternion(new Vector3(0, 0, 1), roll);
		Quaternion quat = null;
		switch (order) {
			case XYZ: quat = quatX.multiply(quatY).multiply(quatZ); break;
			case ZYX: quat = quatZ.multiply(quatY).multiply(quatX); break;
			case YXZ: quat = quatY.multiply(quatX).multiply(quatZ); break;
			case ZXY: quat = quatZ.multiply(quatX).multiply(quatY); break;
			case YZX: quat = quatY.multiply(quatZ).multiply(quatX); break;
			case XZY: quat = quatX.multiply(quatZ).multiply(quatY); break;
		}
		w = quat.w;
		x = quat.x;
		y = quat.y;
		z = quat.z;
	}

	public Quaternion(float pitch, float yaw, float roll) {
		this(pitch, yaw, roll, Angle.Order.YXZ);
	}

	public Quaternion(Angle angle) {
		this(angle.getPitch(), angle.getYaw(), angle.getRoll(), angle.getOrder());
	}

	public Quaternion(Matrix3f matrix) {
		this(matrix.m00, matrix.m01, matrix.m02, matrix.m10, matrix.m11, matrix.m12, matrix.m20, matrix.m21, matrix.m22);
	}

	public Quaternion(Matrix4f matrix) {
		this(matrix.m00, matrix.m01, matrix.m02, matrix.m10, matrix.m11, matrix.m12, matrix.m20, matrix.m21, matrix.m22);
	}

	private Quaternion(float m00, float m01, float m02, float m10, float m11, float m12, float m20, float m21, float m22) {
		float s;
		float tr = m00 + m11 + m22;
		if (tr >= 0.0) {
			s = (float)Math.sqrt(tr + 1.0);
			w = s * 0.5f;
			s = 0.5f / s;
			x = (m21 - m12) * s;
			y = (m02 - m20) * s;
			z = (m10 - m01) * s;
		} else {
			float max = Math.max(Math.max(m00, m11), m22);
			if (max == m00) {
				s = (float)Math.sqrt(m00 - (m11 + m22) + 1.0);
				x = s * 0.5f;
				s = 0.5f / s;
				y = (m01 + m10) * s;
				z = (m20 + m02) * s;
				w = (m21 - m12) * s;
			} else if (max == m11) {
				s = (float)Math.sqrt(m11 - (m22 + m00) + 1.0);
				y = s * 0.5f;
				s = 0.5f / s;
				z = (m12 + m21) * s;
				x = (m01 + m10) * s;
				w = (m02 - m20) * s;
			} else {
				s = (float)Math.sqrt(m22 - (m00 + m11) + 1.0);
				z = s * 0.5f;
				s = 0.5f / s;
				x = (m20 + m02) * s;
				y = (m12 + m21) * s;
				w = (m10 - m01) * s;
			}
		}
	}

	public Quaternion copy() {
		return new Quaternion(this);
	}

	public float getW() {
		return w;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	public Quaternion normalized() {
		float newW, newX, newY, newZ;
		float norm = (float)Math.sqrt(w * w + x * x + y * y + z * z);
		if (norm > 0) {
			newW = w / norm;
			newX = x / norm;
			newY = y / norm;
			newZ = z / norm;
		} else {
			newW = 1;
			newX = 0;
			newY = 0;
			newZ = 0;
		}
		return new Quaternion(newW, newX, newY, newZ);
	}

	public Quaternion rotate(Axis axis, float degrees) {
		//return this.multiply(new Quaternion(axis.getVector(), degrees));
		Matrix4f matrix = getMatrix();
		matrix.rotate((float)Math.toRadians(degrees), Util.convertVector(axis.getVector()));
		return new Quaternion(matrix);
	}

	public Quaternion multiply(Quaternion other) {
		float newW = w * other.w - x * other.x - y * other.y - z * other.z;
		float newX = w * other.x + other.w * x + y * other.z - z * other.y;
		float newY = w * other.y + other.w * y - x * other.z + z * other.x;
		float newZ = w * other.z + other.w * z + x * other.y - y * other.x;
		return new Quaternion(newW, newX, newY, newZ);
	}

	public float norm() {
		return w * w + x * x + y * y + z * z;
	}

	public Quaternion scale(float scale) {
		return new Quaternion(w * scale, x * scale, y * scale, z * scale);
	}

	public Quaternion conjugate() {
		return new Quaternion(w, -x, -y, -z);
	}

	public Quaternion inverse() {
		return conjugate().scale(1 / norm());
	}

	public Matrix4f getMatrix(Matrix4f matrix) {
		float sqw = w * w;
		float sqx = x * x;
		float sqy = y * y;
		float sqz = z * z;

		// invs (inverse square length) is only required if quaternion is not already normalised
		float invs = 1 / (sqx + sqy + sqz + sqw);
		matrix.m00 = (sqx - sqy - sqz + sqw) * invs; // since sqw + sqx + sqy + sqz =1/invs*invs
		matrix.m11 = (-sqx + sqy - sqz + sqw) * invs;
		matrix.m22 = (-sqx - sqy + sqz + sqw) * invs;

		float tmp1 = x * y;
		float tmp2 = z * w;
		matrix.m10 = 2 * (tmp1 + tmp2) * invs;
		matrix.m01 = 2 * (tmp1 - tmp2) * invs;

		tmp1 = x * z;
		tmp2 = y * w;
		matrix.m20 = 2 * (tmp1 - tmp2) * invs;
		matrix.m02 = 2 * (tmp1 + tmp2) * invs;

		tmp1 = y * z;
		tmp2 = x * w;
		matrix.m21 = 2 * (tmp1 + tmp2) * invs;
		matrix.m12 = 2 * (tmp1 - tmp2) * invs;

		return matrix;
	}

	public Matrix4f getMatrix() {
		return getMatrix(new Matrix4f());
	}

	public Vector3 forward() {
		return new Vector3(0, 0, -1).multiply(getMatrix());
	}

	public Vector3 up() {
		return new Vector3(0, 1, 0).multiply(getMatrix());
	}

	public Vector3 right() {
		return new Vector3(1, 0, 0).multiply(getMatrix());
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 23 * hash + Float.floatToIntBits(this.w);
		hash = 23 * hash + Float.floatToIntBits(this.x);
		hash = 23 * hash + Float.floatToIntBits(this.y);
		hash = 23 * hash + Float.floatToIntBits(this.z);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Quaternion other = (Quaternion)obj;
		if (Float.floatToIntBits(this.w) != Float.floatToIntBits(other.w)) {
			return false;
		}
		if (Float.floatToIntBits(this.x) != Float.floatToIntBits(other.x)) {
			return false;
		}
		if (Float.floatToIntBits(this.y) != Float.floatToIntBits(other.y)) {
			return false;
		}
		if (Float.floatToIntBits(this.z) != Float.floatToIntBits(other.z)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Quaternion{" + "w=" + w + ", x=" + x + ", y=" + y + ", z=" + z + '}';
	}
}
