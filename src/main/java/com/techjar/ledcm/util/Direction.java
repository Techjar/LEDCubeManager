
package com.techjar.ledcm.util;

import lombok.Getter;

/**
 *
 * @author Techjar
 */
public enum Direction {
    UP(0, 1, 0),
    DOWN(0, -1, 0),
    NORTH(0, 0, -1),
    SOUTH(0, 0, 1),
    EAST(1, 0, 0),
    WEST(-1, 0, 0),
    UNKNOWN(0, 0, 0);

    private final int x;
    private final int y;
    private final int z;
    public static final Direction[] VALID_DIRECTIONS = {UP, DOWN, NORTH, SOUTH, EAST, WEST};

    private Direction(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3 getVector() {
        return new Vector3(x, y, z);
    }

    public Direction getOpposite() {
        switch (this) {
            case UP: return DOWN;
            case DOWN: return UP;
            case NORTH: return SOUTH;
            case SOUTH: return NORTH;
            case EAST: return WEST;
            case WEST: return EAST;
        }
        return UNKNOWN;
    }
}
