
package com.techjar.ledcm.util;

/**
 *
 * @author Techjar
 */
public class Dimension3D {
    public final int x;
    public final int y;
    public final int z;

    public Dimension3D() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public Dimension3D(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Dimension3D(Dimension3D other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

    public Dimension3D copy() {
        return new Dimension3D(this);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
}
