
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Direction;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Vector2;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationSpectrumShooters extends AnimationSpectrumAnalyzer {
    private float[] amplitudes;
    private final int size;
    private final int bandIncrement;
    private final int bandRepeat;
    // Used to make the sensitivity inversely proportional to the index.
    // Should be slightly higher than the highest possible index.
    private final float indexDivisor;
    private boolean rainbow = true;
    private float sensitivity = 20.0F;

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
            if (amplitude > sensitivity * (1 - (i / indexDivisor))) {
                int z = i % dimension.z;
                int y = i / dimension.y;
                Color color = new Color();
                if (rainbow) color.fromHSB(i / (float)size, 1, 1);
                else color = LEDCubeManager.getPaintColor();
                ledManager.setLEDColor(0, y, z, color);
            }
        }
    }

    @Override
    public void reset() {
        amplitudes = new float[size];
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
    public void processBeatDetect(BeatDetect bt) {
    }
}
