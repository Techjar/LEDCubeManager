
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
    private final byte[] red = new byte[512];
    private final byte[] green = new byte[512];
    private final byte[] blue = new byte[512];
    private boolean gammaCorrection;
    private LEDArray ledArray;

    public TLC5940LEDManager(boolean gammaCorrection) {
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
        return new Dimension3D(8, 8, 8);
    }

    @Override
    public int getLEDCount() {
        return 512;
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
            byte[] array = new byte[2304];
            int[] red2 = new int[512];
            int[] green2 = new int[512];
            int[] blue2 = new int[512];
            if (gammaCorrection) {
                for (int i = 0; i < 512; i++) {
                    red2[i] = (int)Math.round(MathHelper.cie1931((redT[i] & 0xFF) / 255D) * 4095D);
                    green2[i] = (int)Math.round(MathHelper.cie1931((greenT[i] & 0xFF) / 255D) * 4095D);
                    blue2[i] = (int)Math.round(MathHelper.cie1931((blueT[i] & 0xFF) / 255D) * 4095D);
                }
            } else {
                for (int i = 0; i < 512; i++) {
                    red2[i] = (int)Math.round(((redT[i] & 0xFF) / 255D) * 4095D);
                    green2[i] = (int)Math.round(((greenT[i] & 0xFF) / 255D) * 4095D);
                    blue2[i] = (int)Math.round(((blueT[i] & 0xFF) / 255D) * 4095D);
                }
            }
            for (int y = 0; y < 8; y++) {
                int index = 285 + (288 * y);
                int[][] arrs = {red2, green2, blue2};
                for (int[] arr : arrs) {
                    for (int i = 62; i >= 0; i -= 2, index -= 3) {
                        encode12BitValues(arr[(i + 1) | (y << 6)], arr[i | (y << 6)], array, index);
                    }
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
        return getLEDColor(x, y, z);
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
        setLEDColor(x, y, z, color);
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

    private void encode12BitValues(int value1, int value2, byte[] array, int index) {
        array[index] = (byte)(value1 >> 4);
        array[index + 1] = (byte)((value1 << 4) | ((value2 >> 8) & 0b1111));
        array[index + 2] = (byte)value2;
    }
}
