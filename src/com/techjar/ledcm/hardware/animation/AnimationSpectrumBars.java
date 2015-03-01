
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Vector2;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationSpectrumBars extends AnimationSpectrumAnalyzer {
    private float[] amplitudes = new float[64];

    @Override
    public String getName() {
        return "Spectrum Bars";
    }

    @Override
    public synchronized void refresh() {
        for (int i = 0; i < 64; i++) {
            float amplitude = amplitudes[i] - 2;
            Vector2 pos = spiralPosition(i);
            for (int j = 0; j < 8; j++) {
                float increment = (4.0F * (j + 1)) * (1 - (i / 70F));
                ledManager.setLEDColorReal((int)pos.getX() + 3, j, (int)pos.getY() + 3, amplitude > 0 ? colorAtY(j, MathHelper.clamp(amplitude / increment, 0, 1)) : new Color());
                amplitude -= increment;
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
            float amplitude = fft.getBand(i * 4);
            if (amplitude > amplitudes[i]) amplitudes[i] = amplitude;
            else if (amplitudes[i] > 0) amplitudes[i] -= Math.max(amplitudes[i] / 7, 1F);
        }
    }

    @Override
    public void processBeatDetect(BeatDetect bt) {
    }

    private Color colorAtY(int y, float brightness) {
        int res = ledManager.getResolution();
        if (y > 6) return new Color(Math.round(res * brightness), 0, 0);
        if (y > 5) return new Color(Math.round(res * brightness), Math.round(res * brightness), 0);
        if (y > 1) return new Color(0, Math.round(res * brightness), 0);
        return new Color(0, 0, Math.round(res * brightness));
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
