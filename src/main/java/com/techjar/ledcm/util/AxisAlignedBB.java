
package com.techjar.ledcm.util;

/**
 *
 * @author Techjar
 */
public class AxisAlignedBB {
	private final Vector3 minPoint;
	private final Vector3 maxPoint;

	public AxisAlignedBB(Vector3 point1, Vector3 point2) {
		this.minPoint = new Vector3(Math.min(point1.getX(), point2.getX()), Math.min(point1.getY(), point2.getY()), Math.min(point1.getZ(), point2.getZ()));
		this.maxPoint = new Vector3(Math.max(point1.getX(), point2.getX()), Math.max(point1.getY(), point2.getY()), Math.max(point1.getZ(), point2.getZ()));
	}

	public AxisAlignedBB(float x1, float y1, float z1, float x2, float y2, float z2) {
		this(new Vector3(x1, y1, z1), new Vector3(x2, y2, z2));
	}

	public Vector3 getMinPoint() {
		return minPoint.copy();
	}

	public Vector3 getMaxPoint() {
		return maxPoint.copy();
	}

	public boolean containsPoint(Vector3 point) {
		return point.getX() > minPoint.getX() && point.getX() < maxPoint.getX() && point.getY() > minPoint.getY() && point.getY() < maxPoint.getY() && point.getZ() > minPoint.getZ() && point.getZ() < maxPoint.getZ();
	}

	public AxisAlignedBB offset(Vector3 offset) {
		return new AxisAlignedBB(minPoint.add(offset), maxPoint.add(offset));
	}

	public boolean intersects(AxisAlignedBB other) {
		//Vector3 diff2 = minPoint.subtract(other.minPoint).multiply(2);
		//Vector3 totalDim = maxPoint.subtract(minPoint).add(other.maxPoint.subtract(other.minPoint));
		//return Math.abs(diff2.getX()) < totalDim.getX() && Math.abs(diff2.getY()) < totalDim.getY() && Math.abs(diff2.getZ()) < totalDim.getZ();
		return !(minPoint.getX() > other.maxPoint.getX() || maxPoint.getX() < other.minPoint.getX() || minPoint.getY() > other.maxPoint.getY() || maxPoint.getY() < other.minPoint.getY() || minPoint.getZ() > other.maxPoint.getZ() || maxPoint.getZ() < other.minPoint.getZ());
	}

	public boolean contains(AxisAlignedBB other) {
		return minPoint.getX() <= other.minPoint.getX() && maxPoint.getX() >= other.maxPoint.getX() && minPoint.getY() <= other.minPoint.getY() && maxPoint.getY() >= other.maxPoint.getY() && minPoint.getZ() <= other.minPoint.getZ() && maxPoint.getZ() >= other.maxPoint.getZ();
	}

	@Override
	public String toString() {
		return "AxisAlignedBB [minPoint=" + minPoint + ", maxPoint=" + maxPoint + "]";
	}
}
