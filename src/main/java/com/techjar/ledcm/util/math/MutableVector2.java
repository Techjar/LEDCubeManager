package com.techjar.ledcm.util.math;

public class MutableVector2 extends Vector2 {
	public MutableVector2() {
	}

	public MutableVector2(float x, float y) {
		super(x, y);
	}

	public MutableVector2(Vector2 other) {
		super(other);
	}

	public MutableVector2(org.newdawn.slick.geom.Vector2f other) {
		super(other);
	}

	public MutableVector2(org.lwjgl.util.vector.Vector2f other) {
		super(other);
	}

	public Vector2 copy() {
		return new Vector2(this);
	}

	public void setX(float x) {
		set(x, y);
	}

	public void setY(float y) {
		set(x, y);
	}

	public void set(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public void set(Vector2 other) {
		set(other.x, other.y);
	}

	@Override
	public Vector2 add(Vector2 other) {
		set(this.x + other.x, this.y + other.y);
		return this;
	}

	@Override
	public Vector2 subtract(Vector2 other) {
		set(this.x - other.x, this.y - other.y);
		return this;
	}

	@Override
	public Vector2 multiply(float number) {
		set(this.x * number, this.y * number);
		return this;
	}

	@Override
	public Vector2 divide(float number) {
		set(this.x / number, this.y / number);
		return this;
	}

	@Override
	public Vector2 negate() {
		set(-this.x, -this.y);
		return this;
	}

	@Override
	public Vector2 normalized() {
		set(divide(length()));
		return this;
	}
}
