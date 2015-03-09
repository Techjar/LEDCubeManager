
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
public class TLC5940LEDManager implements LEDManager {
    private boolean gammaCorrection;
    private byte[] red = new byte[512];
    private byte[] green = new byte[512];
    private byte[] blue = new byte[512];

    public TLC5940LEDManager(boolean gammaCorrection) {
        this.gammaCorrection = gammaCorrection;
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
            byte[] array = new byte[2304];
            int[] red2 = new int[512];
            int[] green2 = new int[512];
            int[] blue2 = new int[512];
            if (gammaCorrection) {
                for (int i = 0; i < 512; i++) {
                    red2[i] = (int)Math.round(MathHelper.cie1931((double)red[i] / 255) * 4095);
                    green2[i] = (int)Math.round(MathHelper.cie1931((double)green[i] / 255) * 4095);
                    blue2[i] = (int)Math.round(MathHelper.cie1931((double)blue[i] / 255) * 4095);
                }
            } else {
                for (int i = 0; i < 512; i++) {
                    red2[i] = (int)Math.round(((double)red[i] / 255) * 4095);
                    green2[i] = (int)Math.round(((double)green[i] / 255) * 4095);
                    blue2[i] = (int)Math.round(((double)blue[i] / 255) * 4095);
                }
            }
            for (int y = 0, index = 0; y < 8; y++) {
                int[][] arrs = {red2, green2, blue2};
                for (int[] arr : arrs) {
                    for (int i = 0; i < 64; i += 2, index += 3) {
                        System.arraycopy(encode12BitValues(arr[i | (y << 6)], arr[(i + 1) | (y << 6)]), 0, array, index, 3);
                    }
                }
            }
            return array;
        }
    }

    @Override
    public Color getLEDColorReal(int x, int y, int z) {
        return getLEDColor(x, y, z);
    }

    @Override
    public Color getLEDColor(int x, int y, int z) {
        if (x < 0 || x > 7) throw new IllegalArgumentException("Invalid X coordinate: " + x);
        if (y < 0 || y > 7) throw new IllegalArgumentException("Invalid Y coordinate: " + y);
        if (z < 0 || z > 7) throw new IllegalArgumentException("Invalid Z coordinate: " + z);

        int index = (y << 6) | (x << 3) | z;
        return new Color(red[index], green[index], blue[index]);
    }

    @Override
    public void setLEDColorReal(int x, int y, int z, ReadableColor color) {
        setLEDColor(x, y, z, color);
    }

    @Override
    public void setLEDColor(int x, int y, int z, ReadableColor color) {
        if (x < 0 || x > 7) throw new IllegalArgumentException("Invalid X coordinate: " + x);
        if (y < 0 || y > 7) throw new IllegalArgumentException("Invalid Y coordinate: " + y);
        if (z < 0 || z > 7) throw new IllegalArgumentException("Invalid Z coordinate: " + z);

        int index = (y << 6) | (x << 3) | z;
        red[index] = color.getRedByte();
        green[index] = color.getGreenByte();
        blue[index] = color.getBlueByte();
    }

    private byte[] encode12BitValues(int value1, int value2) {
        byte[] bytes = new byte[3];
        bytes[0] = (byte)(value1 >> 4);
        bytes[1] = (byte)((value1 & 0b1111) | ((value2 >> 4) & 0b11110000));
        bytes[2] = (byte)value2;
        return bytes;
    }
}
