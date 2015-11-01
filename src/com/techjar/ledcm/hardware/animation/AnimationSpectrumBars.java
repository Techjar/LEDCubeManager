
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
    private float[] amplitudes;
    private final int size;
    private final int bandIncrement;
    private final int bandRepeat;
    // Used to make the sensitivity inversely proportional to the index.
    // Should be slightly higher than the highest possible index.
    private final float indexDivisor;

    public AnimationSpectrumBars() {
        size = dimension.x * dimension.z;
        bandIncrement = Math.max(Math.round(256F / size), 1);
        bandRepeat = Math.max(Math.round(size / 512F), 1);
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
            Vector2 pos = spiralPosition(i);
            for (int j = 0; j < dimension.y; j++) {
                float increment = ((5.0F / (dimension.y / 8)) * (j + 1)) * (1 - (i / indexDivisor));
                ledManager.setLEDColorReal((int)pos.getX() + ((dimension.x / 2) - 1), j, (int)pos.getY() + ((dimension.z / 2) - 1), amplitude > 0 ? colorAtY(j, MathHelper.clamp(amplitude / increment, 0, 1)) : new Color());
                amplitude -= increment;
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
                float band = fft.getBand(Math.min((i / bandRepeat) * bandIncrement + j, fft.specSize() - 1));
                if (band > amplitude) amplitude = band;
            }
            if (amplitude > amplitudes[i]) amplitudes[i] = amplitude;
            else if (amplitudes[i] > 0) amplitudes[i] -= Math.max(amplitudes[i] / 7, 1F);
        }
    }

    @Override
    public void processBeatDetect(BeatDetect bt) {
    }

    private Color colorAtY(int y, float brightness) {
        int res = ledManager.getResolution();
        if (y > Math.round(dimension.y / 1.333F)) return new Color(Math.round(res * brightness), 0, 0);
        if (y > dimension.y / 2) return new Color(Math.round(res * brightness), Math.round(res * brightness), 0);
        if (y > dimension.y / 8) return new Color(0, Math.round(res * brightness), 0);
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
