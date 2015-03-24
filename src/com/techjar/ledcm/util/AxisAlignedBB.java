
package com.techjar.ledcm.util;

import lombok.Getter;

/**
 *
 * @author Techjar
 */
public class AxisAlignedBB {
    @Getter private Vector3 minPoint;
    @Getter private Vector3 maxPoint;

    public AxisAlignedBB(Vector3 minPoint, Vector3 maxPoint) {
        this.minPoint = minPoint;
        this.maxPoint = maxPoint;
    }

    public boolean containsPoint(Vector3 offset, Vector3 point) {
        if (point.getX() > minPoint.getX() + offset.getX() && point.getX() < maxPoint.getX() + offset.getX() &&
            point.getY() > minPoint.getY() + offset.getY() && point.getY() < maxPoint.getY() + offset.getY() &&
            point.getZ() > minPoint.getZ() + offset.getZ() && point.getZ() < maxPoint.getZ() + offset.getZ()) {
            return true;
        }
        return false;
    }

    public boolean containsPoint(Vector3 point) {
        return containsPoint(new Vector3(), point);
    }
}
