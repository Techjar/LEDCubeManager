
package com.techjar.ledcm.hardware.manager;

import com.techjar.ledcm.hardware.LEDArray;
import com.techjar.ledcm.util.Dimension3D;
import com.techjar.ledcm.util.Vector3;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 * Manages the LED cube data. Must have a constructor that accept a String array.
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
     * Returns the number of LEDs in the cube.
     */
    public int getLEDCount();

    /**
     * Returns whether this LEDManager is monochrome.
     */
    public boolean isMonochrome();

    /**
     * Returns the monochrome color.
     */
    public Color getMonochromeColor();

    /**
     * Get gamma correction
     */
    public boolean getGammaCorrection();

    /**
     * Set gamma correction
     */
    public void setGammaCorrection(boolean gammaCorrection);

    /**
     * Returns desired serial port baud rate for this LEDManager
     */
    public int getBaudRate();

    /**
     * Returns byte array to be sent across the controller connection.
     */
    public byte[] getCommData();

    /**
     * Returns immutable LEDArray instance.
     */
    public LEDArray getLEDArray();

    /**
     * Updates immutable LEDArray instance.
     */
    public void updateLEDArray();

    /**
     * Gets the color of an LED as the raw value sent to the cube, not normalized into the 24-bit RGB color space. May not exceed 8-bit resolution.
     */
    public Color getLEDColorReal(int x, int y, int z);

    /**
     * Gets the color of an LED normalized into the 24-bit RGB color space.
     */
    public Color getLEDColor(int x, int y, int z);

    /**
     * Sets the color of an LED as the real value sent to the cube, not normalized into the 24-bit RGB color space. May not exceed 8-bit resolution.
     */
    public void setLEDColorReal(int x, int y, int z, ReadableColor color);

    /**
     * Sets the color of an LED normalized into the 24-bit RGB color space.
     */
    public void setLEDColor(int x, int y, int z, ReadableColor color);

    /**
     * Encodes a vector into an integer for array indexing in this LEDManager.
     */
    public int encodeVector(Vector3 vector);

    /**
     * Encodes a vector into an integer for array indexing in this LEDManager.
     */
    public int encodeVector(int x, int y, int z);

    /**
     * Decodes an integers into a vector for array indexing in this LEDManager.
     */
    public Vector3 decodeVector(int value);
}
