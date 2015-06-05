
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Util;
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
    private int bandIncrement = 3;

    public AnimationSpectrumBars() {
        super();
        amplitudes = new float[dimension.x];
    }

    @Override
    public String getName() {
        return "Spectrum Dots";
    }

    @Override
    public synchronized void refresh() {
        for (int i = 0; i < amplitudes.length; i++) {
            float amplitude = amplitudes[i] - 2;
            float constant = 10;
            float increment = (5.0F * constant) * (1 - (i / (float)(amplitudes.length + 10)));
            float brightness = MathHelper.clamp(amplitude / increment, 0, 1);
            Color color = LEDCubeManager.getPaintColor();
            color = Util.multiplyColor(color, brightness);
            ledManager.setLEDColor(i, 0, 0, amplitude > 0 ? color : new Color());
        }
    }

    @Override
    public void reset() {
        amplitudes = new float[dimension.x];
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
        for (int i = 0; i < amplitudes.length; i++) {
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

    private Color colorAtBrightness(float brightness) {
        int res = ledManager.getResolution();
        if (brightness > 0.9) return new Color(Math.round(res * brightness), 0, 0);
        if (brightness > 0.5) return new Color(Math.round(res * brightness), Math.round(res * brightness), 0);
        if (brightness > 0.15) return new Color(0, Math.round(res * brightness), 0);
        if (brightness > 0.01) return new Color(0, 0, Math.round(res * brightness));
        return new Color();
    }
}
