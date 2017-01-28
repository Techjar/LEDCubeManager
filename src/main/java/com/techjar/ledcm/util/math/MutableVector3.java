
package com.techjar.ledcm.util.math;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;

/**
 *
 * @author Techjar
 */
public class MutableVector3 extends Vector3 {
	public MutableVector3() {
	}

	public MutableVector3(float x, float y, float z) {
		super(x, y, z);
	}

	public MutableVector3(Vector3 other) {
		super(other);
	}

	public MutableVector3(org.lwjgl.util.vector.Vector3f other) {
		super(other);
	}

	public MutableVector3 setX(float x) {
		return set(x, y, z);
	}

	public MutableVector3 setY(float y) {
		return set(x, y, z);
	}

	public MutableVector3 setZ(float z) {
		return set(x, y, z);
	}

	public MutableVector3 set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public MutableVector3 set(Vector3 other) {
		return set(other.x, other.y, other.z);
	}

	public Vector3 toImmutable() {
		return new Vector3(this);
	}

	@Override
	public MutableVector3 copy() {
		return new MutableVector3(this);
	}

	@Override
	public MutableVector3 add(Vector3 other) {
		set(this.x + other.x, this.y + other.y, this.z + other.z);
		return this;
	}

	@Override
	public MutableVector3 add(float number) {
		set(this.x + number, this.y + number, this.z + number);
		return this;
	}

	@Override
	public MutableVector3 subtract(Vector3 other) {
		set(this.x - other.x, this.y - other.y, this.z - other.z);
		return this;
	}

	@Override
	public MutableVector3 subtract(float number) {
		set(this.x - number, this.y - number, this.z - number);
		return this;
	}

	@Override
	public MutableVector3 multiply(Vector3 other) {
		set(this.x * other.x, this.y * other.y, this.z * other.z);
		return this;
	}

	@Override
	public MutableVector3 multiply(float number) {
		set(this.x * number, this.y * number, this.z * number);
		return this;
	}

	@Override
	public MutableVector3 multiply(Matrix3f matrix) {
		float newX = matrix.m00 * x + matrix.m01 * y + matrix.m02 * z;
		float newY = matrix.m10 * x + matrix.m11 * y + matrix.m12 * z;
		float newZ = matrix.m20 * x + matrix.m21 * y + matrix.m22 * z;
		set(newX, newY, newZ);
		return this;
	}

	@Override
	public MutableVector3 multiply(Matrix4f matrix) {
		float newX = matrix.m00 * x + matrix.m01 * y + matrix.m02 * z + matrix.m03;
		float newY = matrix.m10 * x + matrix.m11 * y + matrix.m12 * z + matrix.m13;
		float newZ = matrix.m20 * x + matrix.m21 * y + matrix.m22 * z + matrix.m23;
		set(newX, newY, newZ);
		return this;
	}

	@Override
	public MutableVector3 divide(Vector3 other) {
		set(this.x / other.x, this.y / other.y, this.z / other.z);
		return this;
	}

	@Override
	public MutableVector3 divide(float number) {
		set(this.x / number, this.y / number, this.z / number);
		return this;
	}

	@Override
	public MutableVector3 negate() {
		set(-this.x, -this.y, -this.z);
		return this;
	}

	@Override
	public MutableVector3 normalized() {
		set(divide(length()));
		return this;
	}

	@Override
	public MutableVector3 cross(Vector3 other) {
		set(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x);
		return this;
	}

	@Override
	public MutableVector3 project(Vector3 other) {
		set(other.multiply(other.dot(this) / other.dot(other)));
		return this;
	}
}
