
package com.techjar.ledcm.hardware;

import com.techjar.ledcm.util.Dimension3D;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class StripLEDManager implements LEDManager {
    private boolean gammaCorrection;
    private int count;
    private byte[] red;
    private byte[] green;
    private byte[] blue;

    public StripLEDManager(int count, boolean gammaCorrection) {
        this.count = count;
        this.gammaCorrection = gammaCorrection;
        red = new byte[count];
        green = new byte[count];
        blue = new byte[count];
    }

    @Override
    public int getResolution() {
        return 127;
    }

    @Override
    public float getFactor() {
        return 255F / 127F;
    }

    @Override
    public Dimension3D getDimensions() {
        return new Dimension3D(count, 1, 1);
    }

    @Override
    public int getLEDCount() {
        return count;
    }

    @Override
    public boolean getGammaCorrection() {
        return gammaCorrection;
    }

    @Override
    public void setGammaCorrection(boolean gammaCorrection) {
        this.gammaCorrection = gammaCorrection;
    }

    @Override
    public byte[] getCommData() {
        return new byte[0];
    }

    @Override
    public Color getLEDColorReal(int x, int y, int z) {
        return getLEDColor(x, y, z);
    }

    @Override
    public Color getLEDColor(int x, int y, int z) {
        if (x < 0 || x > count) throw new IllegalArgumentException("Invalid X coordinate: " + x);
        if (y < 0 || y > 1) throw new IllegalArgumentException("Invalid Y coordinate: " + y);
        if (z < 0 || z > 1) throw new IllegalArgumentException("Invalid Z coordinate: " + z);

        int index = x;
        return new Color(red[index], green[index], blue[index]);
    }

    @Override
    public void setLEDColorReal(int x, int y, int z, ReadableColor color) {
        setLEDColor(x, y, z, color);
    }

    @Override
    public void setLEDColor(int x, int y, int z, ReadableColor color) {
        if (x < 0 || x > count) throw new IllegalArgumentException("Invalid X coordinate: " + x);
        if (y < 0 || y > 1) throw new IllegalArgumentException("Invalid Y coordinate: " + y);
        if (z < 0 || z > 1) throw new IllegalArgumentException("Invalid Z coordinate: " + z);

        int index = x;
        red[index] = color.getRedByte();
        green[index] = color.getGreenByte();
        blue[index] = color.getBlueByte();
    }
}
