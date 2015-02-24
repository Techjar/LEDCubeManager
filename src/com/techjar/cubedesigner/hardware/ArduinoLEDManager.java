
package com.techjar.cubedesigner.hardware;

import com.techjar.cubedesigner.util.Dimension3D;
import com.techjar.cubedesigner.util.MathHelper;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class ArduinoLEDManager implements LEDManager {
    private static final int[] RESOLUTIONS = {0, 1, 3, 7, 15, 31, 63, 127, 255};
    private static final int[] OUT_RESOLUTIONS = {0, 1, 3, 7, 15, 31, 63, 127, 255, 511, 1023, 2047, 4095, 8191, 16383, 32767, 65535};
    private final int bits;
    private final int outBits;
    private final int resolution;
    private final int outResolution;
    private final float factor;
    private boolean gammaCorrection;
    private byte[] red = new byte[512];
    private byte[] green = new byte[512];
    private byte[] blue = new byte[512];

    public ArduinoLEDManager(int bits, boolean gammaCorrection, int outBits) {
        if (bits < 1 || bits >= RESOLUTIONS.length) throw new IllegalArgumentException("Invalid bits: " + bits);
        if (outBits < 1 || outBits >= OUT_RESOLUTIONS.length) throw new IllegalArgumentException("Invalid outBits: " + outBits);
        this.gammaCorrection = gammaCorrection;
        this.bits = bits;
        this.outBits = outBits;
        this.resolution = RESOLUTIONS[bits];
        this.outResolution = OUT_RESOLUTIONS[outBits];
        this.factor = 255F / resolution;
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
    public boolean getGammaCorrection() {
        return gammaCorrection;
    }

    @Override
    public void setGammaCorrection(boolean gammaCorrection) {
        this.gammaCorrection = gammaCorrection;
    }

    @Override
    public byte[] getCommData() {
        synchronized (this) {
            byte[] array = new byte[192 * outBits];
            int[] red = new int[512];
            int[] green = new int[512];
            int[] blue = new int[512];
            for (int i = 0; i < 512; i++) {
                red[i] = Math.round((this.red[i] & 0xFF) / factor);
                green[i] = Math.round((this.green[i] & 0xFF) / factor);
                blue[i] = Math.round((this.blue[i] & 0xFF) / factor);
            }
            if (gammaCorrection) {
                for (int i = 0; i < 512; i++) {
                    red[i] = (byte)Math.round(MathHelper.cie1931((double)red[i] / resolution) * outResolution);
                    green[i] = (byte)Math.round(MathHelper.cie1931((double)green[i] / resolution) * outResolution);
                    blue[i] = (byte)Math.round(MathHelper.cie1931((double)blue[i] / resolution) * outResolution);
                }
            }
            for (int i = 0; i < bits; i++) {
                int mask = 1 << i;
                int index = 192 * i;
                for (int j = 0; j < 512; j++) {
                    int bit = j % 8;
                    array[index + (j / 8)] |= ((red[j] & mask) >> i) << bit;
                    array[index + (j / 8) + 64] |= ((green[j] & mask) >> i) << bit;
                    array[index + (j / 8) + 128] |= ((blue[j] & mask) >> i) << bit;
                }
            }
            return array;
        }
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
}
