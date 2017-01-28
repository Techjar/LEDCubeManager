package com.techjar.ledcm.util.math;

import java.util.LinkedList;
import java.util.Queue;

public class PooledMutableVector3 extends MutableVector3 {
	private static Queue<PooledMutableVector3> pool = new LinkedList<>();
	private boolean released;

	private PooledMutableVector3(float x, float y, float z) {
		super(x, y, z);
	}

	@Override
	public MutableVector3 set(float x, float y, float z) {
		if (released) throw new IllegalStateException("Pooled object is released");
		return super.set(x, y, z);
	}

	public static PooledMutableVector3 get(float x, float y, float z) {
		synchronized (pool) {
			if (!pool.isEmpty()) {
				PooledMutableVector3 vec = pool.poll();
				vec.released = false;
				vec.set(x, y, z);
				return vec;
			}
		}
		return new PooledMutableVector3(x, y, z);
	}

	public static PooledMutableVector3 get() {
		return get(0, 0, 0);
	}

	public static PooledMutableVector3 get(Vector3 other) {
		return get(other.x, other.y, other.z);
	}

	public static PooledMutableVector3 get(org.lwjgl.util.vector.Vector3f other) {
		return get(other.x, other.y, other.z);
	}

	public void release() {
		synchronized (pool) {
			if (pool.size() < 100) {
				pool.add(this);
			}
			this.released = true;
		}
	}

	public Vector3 toImmutableRelease() {
		Vector3 vec = this.toImmutable();
		this.release();
		return vec;
	}
}
