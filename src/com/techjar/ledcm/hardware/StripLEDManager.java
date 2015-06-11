
package com.techjar.ledcm.hardware;

import com.techjar.ledcm.util.Dimension3D;
import com.techjar.ledcm.util.MathHelper;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class StripLEDManager implements LEDManager {
    private boolean gammaCorrection;
    private final int count;
    private final float factor;
    private byte[] red;
    private byte[] green;
    private byte[] blue;

    public StripLEDManager(int count, boolean gammaCorrection) {
        if (count % 2 != 0) throw new IllegalArgumentException("Strip LED count is in multiples of 2");
        this.count = count;
        this.gammaCorrection = gammaCorrection;
        red = new byte[count];
        green = new byte[count];
        blue = new byte[count];
        factor = 255F / 127F;
    }

    @Override
    public int getResolution() {
        return 127;
    }

    @Override
    public float getFactor() {
        return factor;
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
        synchronized (this) {
            byte[] array = new byte[3 * count];
            int[] red2 = new int[count];
            int[] green2 = new int[count];
            int[] blue2 = new int[count];
            for (int i = 0; i < count; i++) {
                red2[i] = Math.round((red[i] & 0xFF) / factor);
                green2[i] = Math.round((green[i] & 0xFF) / factor);
                blue2[i] = Math.round((blue[i] & 0xFF) / factor);
            }
            if (gammaCorrection) {
                for (int i = 0; i < count; i++) {
                    red2[i] = (int)Math.round(MathHelper.cie1931((double)red2[i] / 127D) * 127D);
                    green2[i] = (int)Math.round(MathHelper.cie1931((double)green2[i] / 127D) * 127D);
                    blue2[i] = (int)Math.round(MathHelper.cie1931((double)blue2[i] / 127D) * 127D);
                }
            }
            for (int i = 0; i < count; i++) {
                array[i * 3] = (byte)(green2[i] | 0b10000000);
                array[(i * 3) + 1] = (byte)(red2[i] | 0b10000000);
                array[(i * 3) + 2] = (byte)(blue2[i] | 0b10000000);
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
        if (x < 0 || x >= count) throw new IllegalArgumentException("Invalid X coordinate: " + x);
        if (y < 0 || y > 0) throw new IllegalArgumentException("Invalid Y coordinate: " + y);
        if (z < 0 || z > 0) throw new IllegalArgumentException("Invalid Z coordinate: " + z);

        int index = x;
        return new Color(red[index], green[index], blue[index]);
    }

    @Override
    public void setLEDColorReal(int x, int y, int z, ReadableColor color) {
        setLEDColor(x, y, z, new Color(Math.round(color.getRed() * factor), Math.round(color.getGreen() * factor), Math.round(color.getBlue() * factor)));
    }

    @Override
    public void setLEDColor(int x, int y, int z, ReadableColor color) {
        if (x < 0 || x >= count) throw new IllegalArgumentException("Invalid X coordinate: " + x);
        if (y < 0 || y > 0) throw new IllegalArgumentException("Invalid Y coordinate: " + y);
        if (z < 0 || z > 0) throw new IllegalArgumentException("Invalid Z coordinate: " + z);

        int index = x;
        red[index] = color.getRedByte();
        green[index] = color.getGreenByte();
        blue[index] = color.getBlueByte();
    }
}
