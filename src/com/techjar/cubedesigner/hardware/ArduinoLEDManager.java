
package com.techjar.cubedesigner.hardware;

import com.techjar.cubedesigner.util.Dimension3D;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class ArduinoLEDManager implements LEDManager {
    private byte[] red0 = new byte[64];
    private byte[] green0 = new byte[64];
    private byte[] blue0 = new byte[64];
    private byte[] red1 = new byte[64];
    private byte[] green1 = new byte[64];
    private byte[] blue1 = new byte[64];
    private byte[] red2 = new byte[64];
    private byte[] green2 = new byte[64];
    private byte[] blue2 = new byte[64];
    private byte[] red3 = new byte[64];
    private byte[] green3 = new byte[64];
    private byte[] blue3 = new byte[64];

    public ArduinoLEDManager() {
    }

    @Override
    public int getResolution() {
        return 15;
    }

    @Override
    public Dimension3D getDimensions() {
        return new Dimension3D(8, 8, 8);
    }

    @Override
    public byte[] getSerialData() {
        synchronized (this) {
            byte[] array = new byte[768];
            int index = 0;
            System.arraycopy(red0, 0, array, index++ * 64, 64);
            System.arraycopy(green0, 0, array, index++ * 64, 64);
            System.arraycopy(blue0, 0, array, index++ * 64, 64);
            System.arraycopy(red1, 0, array, index++ * 64, 64);
            System.arraycopy(green1, 0, array, index++ * 64, 64);
            System.arraycopy(blue1, 0, array, index++ * 64, 64);
            System.arraycopy(red2, 0, array, index++ * 64, 64);
            System.arraycopy(green2, 0, array, index++ * 64, 64);
            System.arraycopy(blue2, 0, array, index++ * 64, 64);
            System.arraycopy(red3, 0, array, index++ * 64, 64);
            System.arraycopy(green3, 0, array, index++ * 64, 64);
            System.arraycopy(blue3, 0, array, index++ * 64, 64);
            return array;
        }
    }

    @Override
    public Color getLEDColorRaw(int x, int y, int z) {
        if (x < 0 || x > 7) throw new IllegalArgumentException("Invalid X coordinate: " + x);
        if (y < 0 || y > 7) throw new IllegalArgumentException("Invalid Y coordinate: " + y);
        if (z < 0 || z > 7) throw new IllegalArgumentException("Invalid Z coordinate: " + z);

        // Yes, I know, this stuff is painful...
        int index = (y << 3) | x;
        int bit = 1 << z;
        int red = ((red0[index] & bit) >> z) | (((red1[index] & bit) >> z) << 1) | (((red2[index] & bit) >> z) << 2) | (((red3[index] & bit) >> z) << 3);
        int green = ((green0[index] & bit) >> z) | (((green1[index] & bit) >> z) << 1) | (((green2[index] & bit) >> z) << 2) | (((green3[index] & bit) >> z) << 3);
        int blue = ((blue0[index] & bit) >> z) | (((blue1[index] & bit) >> z) << 1) | (((blue2[index] & bit) >> z) << 2) | (((blue3[index] & bit) >> z) << 3);
        return new Color(red, green, blue);
    }

    @Override
    public Color getLEDColor(int x, int y, int z) {
        ReadableColor color = getLEDColorRaw(x, y, z);
        return new Color(color.getRed() * 17, color.getGreen() * 17, color.getBlue() * 17);
    }

    @Override
    public void setLEDColorRaw(int x, int y, int z, ReadableColor color) {
        if (x < 0 || x > 7) throw new IllegalArgumentException("Invalid X coordinate: " + x);
        if (y < 0 || y > 7) throw new IllegalArgumentException("Invalid Y coordinate: " + y);
        if (z < 0 || z > 7) throw new IllegalArgumentException("Invalid Z coordinate: " + z);

        // You at this point: (╯°□°）╯︵ ┻━┻
        int index = (y << 3) | x;
        int bit = ~(1 << z);
        int value = (color.getRed() & 0b1) << z;
        red0[index] = (byte)(value == 0 ? bit & red0[index] : value | red0[index]);
        value = ((color.getRed() & 0b10) >> 1) << z;
        red1[index] = (byte)(value == 0 ? bit & red1[index] : value | red1[index]);
        value = ((color.getRed() & 0b100) >> 2) << z;
        red2[index] = (byte)(value == 0 ? bit & red2[index] : value | red2[index]);
        value = ((color.getRed() & 0b1000) >> 3) << z;
        red3[index] = (byte)(value == 0 ? bit & red3[index] : value | red3[index]);
        value = (color.getGreen() & 0b1) << z;
        green0[index] = (byte)(value == 0 ? bit & green0[index] : value | green0[index]);
        value = ((color.getGreen() & 0b10) >> 1) << z;
        green1[index] = (byte)(value == 0 ? bit & green1[index] : value | green1[index]);
        value = ((color.getGreen() & 0b100) >> 2) << z;
        green2[index] = (byte)(value == 0 ? bit & green2[index] : value | green2[index]);
        value = ((color.getGreen() & 0b1000) >> 3) << z;
        green3[index] = (byte)(value == 0 ? bit & green3[index] : value | green3[index]);
        value = (color.getBlue() & 0b1) << z;
        blue0[index] = (byte)(value == 0 ? bit & blue0[index] : value | blue0[index]);
        value = ((color.getBlue() & 0b10) >> 1) << z;
        blue1[index] = (byte)(value == 0 ? bit & blue1[index] : value | blue1[index]);
        value = ((color.getBlue() & 0b100) >> 2) << z;
        blue2[index] = (byte)(value == 0 ? bit & blue2[index] : value | blue2[index]);
        value = ((color.getBlue() & 0b1000) >> 3) << z;
        blue3[index] = (byte)(value == 0 ? bit & blue3[index] : value | blue3[index]);
    }

    @Override
    public void setLEDColor(int x, int y, int z, ReadableColor color) {
        setLEDColorRaw(x, y, z, new Color(Math.round(color.getRed() / 17F), Math.round(color.getGreen() / 17F), Math.round(color.getBlue() / 17F)));
    }
}
