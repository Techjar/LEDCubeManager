
package com.techjar.ledcm.hardware;

import com.techjar.ledcm.hardware.manager.LEDManager;
import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Dimension3D;
import com.techjar.ledcm.util.Vector3;
import org.lwjgl.util.Color;

/**
 * Immutable LED array for atomic LED color access.
 *
 * @author Techjar
 */
public class LEDArray {
    private final Object lock = new Object();
    private final LEDManager manager;
    private final byte[] red;
    private final byte[] green;
    private final byte[] blue;
    private final LEDArray transformed;

    /**
     * Requires internal arrays to be passed for fast construction.
     */
    public LEDArray(LEDManager manager, byte[] redArray, byte[] greenArray, byte[] blueArray) {
        this(manager, redArray, greenArray, blueArray, true, false);
    }

    private LEDArray(LEDManager manager, byte[] redArray, byte[] greenArray, byte[] blueArray, boolean external, boolean isTransformed) {
        this.manager = manager;
        if (external) {
            red = new byte[redArray.length];
            green = new byte[greenArray.length];
            blue = new byte[blueArray.length];
            System.arraycopy(redArray, 0, red, 0, redArray.length);
            System.arraycopy(greenArray, 0, green, 0, greenArray.length);
            System.arraycopy(blueArray, 0, blue, 0, blueArray.length);
        } else {
            red = redArray;
            green = greenArray;
            blue = blueArray;
        }
        if (!isTransformed && LEDCubeManager.getLEDCube() != null) {
            byte[] redT = new byte[redArray.length];
            byte[] greenT = new byte[greenArray.length];
            byte[] blueT = new byte[blueArray.length];
            Dimension3D dim = manager.getDimensions();
            for (int x = 0; x < dim.x; x++) {
                for (int y = 0; y < dim.y; y++) {
                    for (int z = 0; z < dim.z; z++) {
                        Vector3 vec = new Vector3(x, y, z);
                        Vector3 newVec = LEDCubeManager.getLEDCube().applyTransform(vec);
                        if (newVec.getX() >= 0 && newVec.getX() < dim.x && newVec.getY() >= 0 && newVec.getY() < dim.y && newVec.getZ() >= 0 && newVec.getZ() < dim.z) {
                            int index = manager.encodeVector(vec);
                            int newIndex = manager.encodeVector(newVec);
                            redT[newIndex] = red[index];
                            greenT[newIndex] = green[index];
                            blueT[newIndex] = blue[index];
                        }
                    }
                }
            }
            transformed = new LEDArray(manager, redT, greenT, blueT, false, true);
        } else {
            transformed = this;
        }
    }

    public Color getLEDColor(int x, int y, int z) {
        int index = manager.encodeVector(x, y, z);
        return new Color(red[index], green[index], blue[index]);
    }

    public Color getLEDColorReal(int x, int y, int z) {
        int index = manager.encodeVector(x, y, z);
        Color color = new Color(red[index], green[index], blue[index]);
        if (manager.getResolution() < 255) {
            return new Color(Math.round(color.getRed() / manager.getFactor()), Math.round(color.getGreen() / manager.getFactor()), Math.round(color.getBlue() / manager.getFactor()));
        } else return color;
    }

    public LEDArray getTransformed() {
        return transformed;
    }

    public byte[] getRed() {
        byte[] copy = new byte[red.length];
        System.arraycopy(red, 0, copy, 0, red.length);
        return copy;
    }

    public byte[] getGreen() {
        byte[] copy = new byte[green.length];
        System.arraycopy(green, 0, copy, 0, green.length);
        return copy;
    }

    public byte[] getBlue() {
        byte[] copy = new byte[blue.length];
        System.arraycopy(blue, 0, copy, 0, blue.length);
        return copy;
    }
}
