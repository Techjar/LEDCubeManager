
package com.techjar.ledcm.hardware;

import com.techjar.ledcm.util.Dimension3D;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Vector3;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class TestMediumLEDManager implements LEDManager {
    private final byte[] red = new byte[4096];
    private final byte[] green = new byte[4096];
    private final byte[] blue = new byte[4096];
    private boolean gammaCorrection;
    private LEDArray ledArray;

    public TestMediumLEDManager(boolean gammaCorrection) {
        this.gammaCorrection = gammaCorrection;
        updateLEDArray();
    }

    @Override
    public int getResolution() {
        return 255;
    }

    @Override
    public float getFactor() {
        return 1;
    }

    @Override
    public Dimension3D getDimensions() {
        return new Dimension3D(16, 16, 16);
    }

    @Override
    public int getLEDCount() {
        return 4096;
    }

    @Override
    public boolean isMonochrome() {
        return false;
    }

    @Override
    public Color getMonochromeColor() {
        return null;
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
    public int getBaudRate() {
        return 2000000;
    }

    @Override
    public byte[] getCommData() {
        synchronized (this) {
            return new byte[98304];
        }
    }

    @Override
    public LEDArray getLEDArray() {
        return ledArray;
    }

    @Override
    public void updateLEDArray() {
        ledArray = new LEDArray(this, red, green, blue);
    }

    @Override
    public Color getLEDColorReal(int x, int y, int z) {
        return getLEDColor(x, y, z);
    }

    @Override
    public Color getLEDColor(int x, int y, int z) {
        if (x < 0 || x > 15) throw new IllegalArgumentException("Invalid X coordinate: " + x);
        if (y < 0 || y > 15) throw new IllegalArgumentException("Invalid Y coordinate: " + y);
        if (z < 0 || z > 15) throw new IllegalArgumentException("Invalid Z coordinate: " + z);

        int index = (y << 8) | (x << 4) | z;
        return new Color(red[index], green[index], blue[index]);
    }

    @Override
    public void setLEDColorReal(int x, int y, int z, ReadableColor color) {
        setLEDColor(x, y, z, color);
    }

    @Override
    public void setLEDColor(int x, int y, int z, ReadableColor color) {
        if (x < 0 || x > 15) throw new IllegalArgumentException("Invalid X coordinate: " + x);
        if (y < 0 || y > 15) throw new IllegalArgumentException("Invalid Y coordinate: " + y);
        if (z < 0 || z > 15) throw new IllegalArgumentException("Invalid Z coordinate: " + z);

        int index = (y << 8) | (x << 4) | z;
        red[index] = color.getRedByte();
        green[index] = color.getGreenByte();
        blue[index] = color.getBlueByte();
    }

    @Override
    public int encodeVector(Vector3 vector) {
        return encodeVector((int)vector.getY(), (int)vector.getX(), (int)vector.getZ());
    }

    @Override
    public int encodeVector(int x, int y, int z) {
        return (y << 8) | (x << 4) | z;
    }

    @Override
    public Vector3 decodeVector(int value) {
        return new Vector3((value >> 4) & 15, (value >> 8) & 15, value & 15);
    }
}
