
package com.techjar.ledcm.hardware.manager;

import com.techjar.ledcm.hardware.LEDArray;
import com.techjar.ledcm.util.Dimension3D;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Vector3;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class TestLEDManager implements LEDManager {
    private final byte[] red;
    private final byte[] green;
    private final byte[] blue;
    private final int xSize;
    private final int ySize;
    private final int zSize;
    private boolean gammaCorrection;
    private LEDArray ledArray;

    public TestLEDManager(boolean gammaCorrection, int xSize, int ySize, int zSize) {
        this.gammaCorrection = gammaCorrection;
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
        red = new byte[xSize * ySize * zSize];
        green = new byte[xSize * ySize * zSize];
        blue = new byte[xSize * ySize * zSize];
        updateLEDArray();
    }

    public TestLEDManager(String[] args) {
        this(Boolean.parseBoolean(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
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
        return new Dimension3D(xSize, ySize, zSize);
    }

    @Override
    public int getLEDCount() {
        return xSize * ySize * zSize;
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
            return new byte[100];
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
        if (x < 0 || x >= xSize) throw new IllegalArgumentException("Invalid X coordinate: " + x);
        if (y < 0 || y >= ySize) throw new IllegalArgumentException("Invalid Y coordinate: " + y);
        if (z < 0 || z >= zSize) throw new IllegalArgumentException("Invalid Z coordinate: " + z);

        int index = x + xSize * (y + ySize * z);
        return new Color(red[index], green[index], blue[index]);
    }

    @Override
    public void setLEDColorReal(int x, int y, int z, ReadableColor color) {
        setLEDColor(x, y, z, color);
    }

    @Override
    public void setLEDColor(int x, int y, int z, ReadableColor color) {
        if (x < 0 || x >= xSize) throw new IllegalArgumentException("Invalid X coordinate: " + x);
        if (y < 0 || y >= ySize) throw new IllegalArgumentException("Invalid Y coordinate: " + y);
        if (z < 0 || z >= zSize) throw new IllegalArgumentException("Invalid Z coordinate: " + z);

        int index = x + xSize * (y + ySize * z);
        red[index] = color.getRedByte();
        green[index] = color.getGreenByte();
        blue[index] = color.getBlueByte();
    }

    @Override
    public int encodeVector(Vector3 vector) {
        return encodeVector((int)vector.getX(), (int)vector.getY(), (int)vector.getZ());
    }

    @Override
    public int encodeVector(int x, int y, int z) {
        return x + xSize * (y + ySize * z);
    }

    @Override
    public Vector3 decodeVector(int value) {
        return new Vector3(value % xSize, (value / xSize) % ySize, value / (xSize * ySize));
    }
}
