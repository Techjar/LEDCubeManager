
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
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
    private float[] amplitudes = new float[64];
    private int bandIncrement = 4;
    private boolean rainbow = true;
    private float sensitivity = 20.0F;

    @Override
    public String getName() {
        return "Spectrum Shooters";
    }

    @Override
    public synchronized void refresh() {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                for (int z = 6; z >= 0; z--) {
                    ledManager.setLEDColor(x, y, z + 1, ledManager.getLEDColor(x, y, z));
                    ledManager.setLEDColor(x, y, z, new Color());
                }
            }
        }
        for (int i = 0; i < 64; i++) {
            float amplitude = amplitudes[i] - 2;
            if (amplitude > sensitivity * (1 - (i / 70F))) {
                int x = i & 7;
                int y = (i >> 3) & 7;
                Color color = new Color();
                if (rainbow) color.fromHSB(i / 63F, 1, 1);
                else color = LEDCubeManager.getPaintColor();
                ledManager.setLEDColor(x, y, 0, color);
            }
        }
    }

    @Override
    public void reset() {
        amplitudes = new float[64];
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
        for (int i = 0; i < 64; i++) {
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
