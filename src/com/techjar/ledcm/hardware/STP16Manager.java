package com.techjar.ledcm.hardware;

import com.techjar.ledcm.util.Dimension3D;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Vector3;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 * @author Leonelf
 */
public class STP16Manager implements LEDManager {

    private boolean gammaCorrection;
    private byte[] red = new byte[512];
    private byte[] green = new byte[512];
    private byte[] blue = new byte[512];

    public STP16Manager(boolean gammaCorrection) {
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
    public int getLEDCount() {
        return 512;
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
            byte[] array = new byte[512 * 3];
            byte[] colors = new byte[512 * 3];
            if (gammaCorrection) {
                for (int i = 0; i < 512; i++) {
                    colors[i] = (byte) Math.round(MathHelper.cie1931(blue[i] / 255D) * 255D);            //Red
                    colors[i + 512] = (byte) Math.round(MathHelper.cie1931(green[i] / 255D) * 255D);    //Green
                    colors[i + 1024] = (byte) Math.round(MathHelper.cie1931(red[i] / 255D) * 255D);    //Blue
                }
            } else {
                for (int i = 0; i < 512; i++) {
                    colors[i] = (byte) Math.round(blue[i]);          //Red
                    colors[i + 512] = (byte) Math.round(green[i]);  //Green
                    colors[i + 1024] = (byte) Math.round(red[i]);  //Blue
                }
            }

            for (int layer = 0; layer < 8; layer++) {
                for (int bit = 0; bit < 8; bit++) {
                    for (int color = 0; color < 3; color++) {
                        for (int ledX = 0; ledX < 8; ledX++) {
                            for (int ledY = 0; ledY < 8; ledY++) {
                                if ((colors[ledX + 8 * ledY + 64 * layer + 512 * color] & (1 << bit)) > 0)
                                    array[(ledX + 8 * color + 24 * bit + 192 * layer)] |= (1 << ledY);
                            }
                        }
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
        if (x < 0 || x > 7)
            throw new IllegalArgumentException("Invalid X coordinate: " + x);
        if (y < 0 || y > 7)
            throw new IllegalArgumentException("Invalid Y coordinate: " + y);
        if (z < 0 || z > 7)
            throw new IllegalArgumentException("Invalid Z coordinate: " + z);

        int index = (y << 6) | (x << 3) | z;
        return new Color(red[index], green[index], blue[index]);
    }

    @Override
    public void setLEDColorReal(int x, int y, int z, ReadableColor color) {
        setLEDColor(x, y, z, color);
    }

    @Override
    public void setLEDColor(int x, int y, int z, ReadableColor color) {
        if (x < 0 || x > 7)
            throw new IllegalArgumentException("Invalid X coordinate: " + x);
        if (y < 0 || y > 7)
            throw new IllegalArgumentException("Invalid Y coordinate: " + y);
        if (z < 0 || z > 7)
            throw new IllegalArgumentException("Invalid Z coordinate: " + z);

        int index = (y << 6) | (x << 3) | z;
        red[index] = color.getRedByte();
        green[index] = color.getGreenByte();
        blue[index] = color.getBlueByte();
    }

    @Override
    public int encodeVector(Vector3 vector) {
        return encodeVector((int) vector.getY(), (int) vector.getX(), (int) vector.getZ());
    }

    @Override
    public int encodeVector(int x, int y, int z) {
        return (y << 6) | (x << 3) | z;
    }

    @Override
    public Vector3 decodeVector(int value) {
        return new Vector3((value >> 3) & 7, (value >> 6) & 7, value & 7);
    }

    @Override
    public int getBaudrate() {
        return 921600;
    }
}
