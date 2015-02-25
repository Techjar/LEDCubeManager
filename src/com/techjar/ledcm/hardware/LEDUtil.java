
package com.techjar.ledcm.hardware;

import com.techjar.ledcm.util.Dimension3D;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class LEDUtil {
    private LEDUtil() {
    }

    public static void fill(LEDManager ledManager, Color color) {
        Dimension3D dim = ledManager.getDimensions();
        for (int x = 0; x < dim.x; x++) {
            for (int y = 0; y < dim.y; y++) {
                for (int z = 0; z < dim.z; z++) {
                    ledManager.setLEDColor(x, y, z, color);
                }
            }
        }
    }

    public static void clear(LEDManager ledManager) {
        fill(ledManager, new Color());
    }
}
