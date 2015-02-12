
package com.techjar.cubedesigner.hardware;

import com.techjar.cubedesigner.util.Dimension3D;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public interface LEDManager {
    /**
     * Returns the brightness resolution as the maximum value that can be used.
     * For example, 4-bit brightness is 0-15, so the return value would be 15.
     */
    public int getResolution();
    /**
     * Returns the factor derived from the resolution. (255 / resolution)
     */
    public float getFactor();

    /**
     * Returns the dimensions of the LED display.
     * Currently, changing this from 8x8x8 is not supported.
     */
    public Dimension3D getDimensions();

    /**
     * Get gamma correction
     */
    public boolean getGammaCorrection();

    /**
     * Set gamma correction
     */
    public void setGammaCorrection(boolean gammaCorrection);

    /**
     * Returns byte array to be sent across the controller connection.
     */
    public byte[] getCommData();

    /**
     * Gets the color of an LED as the raw value sent to the cube, not normalized into the 24-bit RGB color space.
     */
    public Color getLEDColorReal(int x, int y, int z);

    /**
     * Gets the color of an LED normalized into the 24-bit RGB color space.
     */
    public Color getLEDColor(int x, int y, int z);

    /**
     * Sets the color of an LED as the real value sent to the cube, not normalized into the 24-bit RGB color space.
     */
    public void setLEDColorReal(int x, int y, int z, ReadableColor color);

    /**
     * Sets the color of an LED normalized into the 24-bit RGB color space.
     */
    public void setLEDColor(int x, int y, int z, ReadableColor color);
}
