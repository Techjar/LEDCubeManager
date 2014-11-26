
package com.techjar.cubedesigner.util;

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

    public Quaternion(Vector3 vector, float rotation) {
        rotation = (float)Math.toRadians(rotation);
        float sinRot = (float)Math.sin(rotation / 2);
        w = (float)Math.cos(rotation / 2);
        x = vector.x * sinRot;
        y = vector.y * sinRot;
        z = vector.z * sinRot;
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
        this(pitch, yaw, roll, Angle.Order.ZYX);
    }

    public Quaternion(Angle angle) {
        this(angle.getPitch(), angle.getYaw(), angle.getRoll(), angle.getOrder());
    }

    public Quaternion copy() {
        return new Quaternion(this);
    }

    public float getW() {
        return w;
    }

    public void setW(float w) {
        this.w = w;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public void normalize() {
        float norm = (float)Math.sqrt(w * w + x * x + y * y + z * z);
        if (norm > 0) {
            w /= norm;
            x /= norm;
            y /= norm;
            z /= norm;
        } else {
            w = 1;
            x = 0;
            y = 0;
            z = 0;
        }
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
        return this.multiply(new Quaternion(axis.getVector(), degrees));
    }

    public Quaternion multiply(Quaternion other) {
        float newW = w * other.w - x * other.x - y * other.y - z * other.z;
        float newX = w * other.x + other.w * x + y * other.z - z * other.y;
        float newY = w * other.y + other.w * y - x * other.z + z * other.x;
        float newZ = w * other.z + other.w * z + x * other.y - y * other.x;
        return new Quaternion(newW, newX, newY, newZ);
    }

    public Matrix4f getMatrix() {
        Matrix4f matrix = new Matrix4f();
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
