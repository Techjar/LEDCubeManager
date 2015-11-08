
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
public class ArduinoLEDManager implements LEDManager {
    private final int bits;
    private final int outBits;
    private final int resolution;
    private final int outResolution;
    private final float factor;
    private final byte[] red = new byte[512];
    private final byte[] green = new byte[512];
    private final byte[] blue = new byte[512];
    private boolean gammaCorrection;
    private LEDArray ledArray;

    public ArduinoLEDManager(int bits, boolean gammaCorrection, int outBits) {
        if (bits < 1 || bits > 8) throw new IllegalArgumentException("Invalid bits: " + bits);
        if (outBits < bits || outBits < 1 || outBits > 31) throw new IllegalArgumentException("Invalid outBits: " + outBits);
        this.gammaCorrection = gammaCorrection;
        this.bits = bits;
        this.outBits = outBits;
        this.resolution = (int)Math.pow(2, bits) - 1;
        this.outResolution = (int)Math.pow(2, outBits) - 1;
        this.factor = 255F / resolution;
        updateLEDArray();
    }

    public ArduinoLEDManager(int bits, boolean gammaCorrection) {
        this(bits, gammaCorrection, bits);
    }

    public ArduinoLEDManager(int bits) {
        this(bits, false, bits);
    }

    @Override
    public int getResolution() {
        return resolution;
    }

    @Override
    public float getFactor() {
        return factor;
    }

    @Override
    public Dimension3D getDimensions() {
        return new Dimension3D(8, 8, 8);
    }

    @Override
    public int getLEDCount() {
        return 8 * 8 * 8;
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
            byte[] redT = ledArray.getTransformed().getRed();
            byte[] greenT = ledArray.getTransformed().getGreen();
            byte[] blueT = ledArray.getTransformed().getBlue();
            byte[] array = new byte[192 * outBits];
            int[] red2 = new int[512];
            int[] green2 = new int[512];
            int[] blue2 = new int[512];
            for (int i = 0; i < 512; i++) {
                red2[i] = Math.round((redT[i] & 0xFF) / factor);
                green2[i] = Math.round((greenT[i] & 0xFF) / factor);
                blue2[i] = Math.round((blueT[i] & 0xFF) / factor);
            }
            if (gammaCorrection) {
                for (int i = 0; i < 512; i++) {
                    red2[i] = (int)Math.round(MathHelper.cie1931((double)red2[i] / resolution) * outResolution);
                    green2[i] = (int)Math.round(MathHelper.cie1931((double)green2[i] / resolution) * outResolution);
                    blue2[i] = (int)Math.round(MathHelper.cie1931((double)blue2[i] / resolution) * outResolution);
                }
            } else if (outBits != bits) {
                for (int i = 0; i < 512; i++) {
                    red2[i] = (int)Math.round(((double)red2[i] / resolution) * outResolution);
                    green2[i] = (int)Math.round(((double)green2[i] / resolution) * outResolution);
                    blue2[i] = (int)Math.round(((double)blue2[i] / resolution) * outResolution);
                }
            }
            for (int i = 0; i < outBits; i++) {
                int mask = 1 << i;
                int index = 192 * i;
                for (int j = 0; j < 512; j++) {
                    int bit = j % 8;
                    array[index + (j / 8)] |= ((red2[j] & mask) >> i) << bit;
                    array[index + (j / 8) + 64] |= ((green2[j] & mask) >> i) << bit;
                    array[index + (j / 8) + 128] |= ((blue2[j] & mask) >> i) << bit;
                }
            }
            return array;
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
        ReadableColor color = getLEDColor(x, y, z);
        return new Color(Math.round(color.getRed() / factor), Math.round(color.getGreen() / factor), Math.round(color.getBlue() / factor));
    }

    @Override
    public Color getLEDColor(int x, int y, int z) {
        if (x < 0 || x > 7) throw new IllegalArgumentException("Invalid X coordinate: " + x);
        if (y < 0 || y > 7) throw new IllegalArgumentException("Invalid Y coordinate: " + y);
        if (z < 0 || z > 7) throw new IllegalArgumentException("Invalid Z coordinate: " + z);

        int index = (y << 6) | (z << 3) | x;
        return new Color(red[index], green[index], blue[index]);
    }

    @Override
    public void setLEDColorReal(int x, int y, int z, ReadableColor color) {
        setLEDColor(x, y, z, new Color(Math.round(color.getRed() * factor), Math.round(color.getGreen() * factor), Math.round(color.getBlue() * factor)));
    }

    @Override
    public void setLEDColor(int x, int y, int z, ReadableColor color) {
        if (x < 0 || x > 7) throw new IllegalArgumentException("Invalid X coordinate: " + x);
        if (y < 0 || y > 7) throw new IllegalArgumentException("Invalid Y coordinate: " + y);
        if (z < 0 || z > 7) throw new IllegalArgumentException("Invalid Z coordinate: " + z);

        int index = (y << 6) | (z << 3) | x;
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
        return (y << 6) | (z << 3) | x;
    }

    @Override
    public Vector3 decodeVector(int value) {
        return new Vector3(value & 7, (value >> 6) & 7, (value >> 3) & 7);
    }
}
