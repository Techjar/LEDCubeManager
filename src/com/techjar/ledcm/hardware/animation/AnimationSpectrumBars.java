
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector2;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;
import java.util.Random;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationSpectrumBars extends AnimationSpectrumAnalyzer {
    private Random random = new Random();
    private float[] amplitudes;
    private Color[] randomColors;
    private final int size;
    private final int bandIncrement;
    private final int bandRepeat;
    // Used to make the sensitivity inversely proportional to the index.
    // Should be slightly higher than the highest possible index.
    private final float indexDivisor;
    private int colorMode = 0;
    private float holdUp = 7;

    public AnimationSpectrumBars() {
        size = dimension.x * dimension.z;
        bandIncrement = Math.max(Math.round(256F / size), 1);
        bandRepeat = Math.max(Math.round(size / 256F), 1);
        indexDivisor = size + 6;
    }

    @Override
    public String getName() {
        return "Spectrum Bars";
    }

    @Override
    public synchronized void refresh() {
        for (int i = 0; i < size; i++) {
            float amplitude = amplitudes[i] - 2;
            if (colorMode == 3 && (amplitude <= 0 || randomColors[i].equals(new Color()))) randomColors[i] = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            Vector2 pos = spiralPosition(i);
            pos = new Vector2(pos.getX() + ((dimension.x / 2) - 1), pos.getY() + ((dimension.z / 2) - 1));
            for (int j = 0; j < dimension.y; j++) {
                float increment = ((5.0F / (dimension.y / 8)) * (j + 1)) * (1 - (i / indexDivisor));
                ledManager.setLEDColor((int)pos.getX(), j, (int)pos.getY(), amplitude > 0 ? getColor(i, j, MathHelper.clamp(amplitude / increment, 0, 1)) : new Color());
                amplitude -= increment;
            }
        }
    }

    @Override
    public synchronized void reset() {
        amplitudes = new float[size];
        randomColors = new Color[size];
        for (int i = 0; i < size; i++) {
            randomColors[i] = new Color();
        }
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("colormode", "Color", AnimationOption.OptionType.COMBOBOX, new Object[]{colorMode, 0, "Classic", 1, "Rainbow 1", 2, "Rainbow 2", 3, "Random", 4, "Picker"}),
            new AnimationOption("holdUp", "Hold Up", AnimationOption.OptionType.SLIDER, new Object[]{holdUp / 29F}),
        };
    }

    @Override
    public synchronized void optionChanged(String name, String value) {
        switch (name) {
            case "colormode":
                colorMode = Integer.parseInt(value);
                break;
            case "holdUp":
                holdUp = 1 + (29 * Float.parseFloat(value));
                break;
        }
    }

    @Override
    public boolean isFFT() {
        return true;
    }

    @Override
    public boolean isBeatDetect() {
        return false;
    }

    @Override
    public int getBeatDetectMode() {
        return BeatDetect.FREQ_ENERGY;
    }

    @Override
    public synchronized void processFFT(FFT fft) {
        for (int i = 0; i < size; i++) {
            float amplitude = 0;
            for (int j = 0; j < bandIncrement; j++) {
                float band = fft.getBand(Math.min((i / bandRepeat) * bandIncrement + j, fft.specSize() - 1));
                if (band > amplitude) amplitude = band;
            }
            if (amplitude > amplitudes[i]) amplitudes[i] = amplitude;
            else if (amplitudes[i] > 0) amplitudes[i] -= Math.max(amplitudes[i] / holdUp, 1F);
        }
    }

    @Override
    public synchronized void processBeatDetect(BeatDetect bt) {
    }

    private Color getColor(int index, int y, float brightness) {
        if (colorMode == 1) {
            Color color = new Color();
            color.fromHSB((((dimension.y - 1) - y) / (dimension.y - 1F)) * (300F / 360F), 1, brightness);
            return color;
        }
        if (colorMode == 2) {
            Color color = new Color();
            color.fromHSB(index / (float)size, 1, brightness);
            return color;
        }
        if (colorMode == 3) {
            return Util.multiplyColor(randomColors[index], brightness);
        }
        if (colorMode == 4) {
            return Util.multiplyColor(LEDCubeManager.getLEDCube().getPaintColor(), brightness);
        }
        if (y > Math.round(dimension.y / 1.333F)) return new Color(Math.round(255 * brightness), 0, 0);
        if (y > dimension.y / 2) return new Color(Math.round(255 * brightness), Math.round(255 * brightness), 0);
        if (y > dimension.y / 8) return new Color(0, Math.round(255 * brightness), 0);
        return new Color(0, 0, Math.round(255 * brightness));
    }

    private Vector2 spiralPosition(int index) {
        // (di, dj) is a vector - direction in which we move right now
        int di = 1;
        int dj = 0;
        // length of current segment
        int segment_length = 1;

        // current position (i, j) and how much of current segment we passed
        int i = 0;
        int j = 0;
        int segment_passed = 0;
        for (int k = 0; k < index; ++k) {
            // make a step, add 'direction' vector (di, dj) to current position (i, j)
            i += di;
            j += dj;
            ++segment_passed;

            if (segment_passed == segment_length) {
                // done with current segment
                segment_passed = 0;

                // 'rotate' directions
                int buffer = di;
                di = -dj;
                dj = buffer;

                // increase segment length if necessary
                if (dj == 0) {
                    ++segment_length;
                }
            }
        }

        return new Vector2(i, j);
    }
}
