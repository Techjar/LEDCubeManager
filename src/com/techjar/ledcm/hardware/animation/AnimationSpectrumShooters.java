
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Direction;
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
public class AnimationSpectrumShooters extends AnimationSpectrumAnalyzer {
    private Random random = new Random();
    private float[] amplitudes;
    private Color[] randomColors;
    private final int size;
    private final int bandIncrement;
    private final int bandRepeat;
    // Used to make the sensitivity inversely proportional to the index.
    // Should be slightly higher than the highest possible index.
    private final float indexDivisor;
    private float sensitivity = 20.0F;
    private int colorMode = 1;

    public AnimationSpectrumShooters() {
        size = dimension.z * dimension.y;
        bandIncrement = Math.max(Math.round(256F / size), 1);
        bandRepeat = Math.max(Math.round(size / 512F), 1);
        indexDivisor = size + 6;
    }

    @Override
    public String getName() {
        return "Spectrum Shooters";
    }

    @Override
    public synchronized void refresh() {
        for (int z = 0; z < dimension.z; z++) {
            for (int y = 0; y < dimension.y; y++) {
                for (int x = dimension.x - 2; x >= 0; x--) {
                    ledManager.setLEDColor(x + 1, y, z, ledManager.getLEDColor(x, y, z));
                    ledManager.setLEDColor(x, y, z, new Color());
                }
            }
        }
        for (int i = 0; i < size; i++) {
            float amplitude = amplitudes[i] - 2;
            if (randomColors[i].equals(new Color())) randomColors[i] = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            if (amplitude > sensitivity * (1 - (i / indexDivisor))) {
                int z = i % dimension.z;
                int y = i / dimension.y;
                ledManager.setLEDColor(0, y, z, getColor(i));
            } else if (colorMode == 2) randomColors[i] = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
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
            new AnimationOption("colormode", "Color", AnimationOption.OptionType.COMBOBOX, new Object[]{colorMode, 0, "Picker", 1, "Rainbow", 2, "Random"}),
            new AnimationOption("sensitivity", "Sensitivity", AnimationOption.OptionType.SLIDER, new Object[]{(50 - sensitivity) / 50F}),
        };
    }

    @Override
    public synchronized void optionChanged(String name, String value) {
        switch (name) {
            case "colormode":
                colorMode = Integer.parseInt(value);
                break;
            case "sensitivity":
                sensitivity = 50 * (1 - Float.parseFloat(value));
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
                float band = fft.getBand(i * bandIncrement + j);
                if (band > amplitude) amplitude = band;
            }
            if (amplitude > amplitudes[i]) amplitudes[i] = amplitude;
            else if (amplitudes[i] > 0) amplitudes[i] -= Math.max(amplitudes[i] / 7, 1F);
        }
    }

    @Override
    public synchronized void processBeatDetect(BeatDetect bt) {
    }

    private Color getColor(int index) {
        if (colorMode == 1) {
            Color color = new Color();
            color.fromHSB(index / (float)size, 1, 1);
            return color;
        }
        if (colorMode == 2) return randomColors[index];
        return LEDCubeManager.getLEDCube().getPaintColor();
    }
}
