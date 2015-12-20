
package com.techjar.ledcm.hardware;

import com.techjar.ledcm.hardware.manager.LEDManager;
import com.techjar.ledcm.util.Dimension3D;
import com.techjar.ledcm.util.Direction;
import com.techjar.ledcm.util.Vector3;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class LEDUtil {
    private LEDUtil() {
    }

    public static void fill(LEDManager manager, Color color) {
        Dimension3D dim = manager.getDimensions();
        for (int x = 0; x < dim.x; x++) {
            for (int y = 0; y < dim.y; y++) {
                for (int z = 0; z < dim.z; z++) {
                    manager.setLEDColor(x, y, z, color);
                }
            }
        }
    }

    public static void clear(LEDManager manager) {
        fill(manager, new Color());
    }

    public static Direction getSideDirection(LEDManager manager, Vector3 position) {
        Dimension3D dim = manager.getDimensions();
        if (Math.round(position.getX()) <= 0) return Direction.WEST;
        if (Math.round(position.getX()) >= dim.x - 1) return Direction.EAST;
        if (Math.round(position.getY()) <= 0) return Direction.DOWN;
        if (Math.round(position.getY()) >= dim.y - 1) return Direction.UP;
        if (Math.round(position.getZ()) <= 0) return Direction.NORTH;
        if (Math.round(position.getZ()) >= dim.z - 1) return Direction.SOUTH;
        return Direction.UNKNOWN;
    }

    public static Direction getSideDirectionFacing(LEDManager manager, Vector3 position, Vector3 direction) {
        Dimension3D dim = manager.getDimensions();
        if (Math.round(position.getX()) <= 0 && direction.getX() <= 0) return Direction.WEST;
        if (Math.round(position.getX()) >= dim.x - 1 && direction.getX() >= 0) return Direction.EAST;
        if (Math.round(position.getY()) <= 0 && direction.getY() <= 0) return Direction.DOWN;
        if (Math.round(position.getY()) >= dim.y - 1 && direction.getY() >= 0) return Direction.UP;
        if (Math.round(position.getZ()) <= 0 && direction.getZ() <= 0) return Direction.NORTH;
        if (Math.round(position.getZ()) >= dim.z - 1 && direction.getZ() >= 0) return Direction.SOUTH;
        return Direction.UNKNOWN;
    }
}
