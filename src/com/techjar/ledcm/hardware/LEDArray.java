
package com.techjar.ledcm.hardware;

import org.lwjgl.util.Color;

/**
 * Immutable LED array for atomic LED color access.
 *
 * @author Techjar
 */
public class LEDArray {
    private final LEDManager manager;
    private final byte[] red;
    private final byte[] green;
    private final byte[] blue;

    /**
     * Requires internal arrays to be passed for fast construction.
     */
    public LEDArray(LEDManager manager, byte[] redArray, byte[] greenArray, byte[] blueArray) {
        this.manager = manager;
        red = new byte[redArray.length];
        green = new byte[greenArray.length];
        blue = new byte[blueArray.length];
        System.arraycopy(redArray, 0, red, 0, redArray.length);
        System.arraycopy(greenArray, 0, green, 0, greenArray.length);
        System.arraycopy(blueArray, 0, blue, 0, blueArray.length);
    }

    public Color getLEDColor(int x, int y, int z) {
        int index = manager.encodeVector(x, y, z);
        return new Color(red[index], green[index], blue[index]);
    }
}
