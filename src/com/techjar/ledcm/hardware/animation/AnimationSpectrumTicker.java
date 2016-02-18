
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
public class AnimationSpectrumTicker extends AnimationSpectrumAnalyzer {
    private float[] amplitudes = new float[64];
    private int bandIncrement = 4;
    private float hue = 0;
    private int speed = 1;
    private int rainbow = 0;
    private volatile int mode = BeatDetect.SOUND_ENERGY;
    private volatile int rangeMin = 0;
    private volatile int rangeMax = 26;
    private volatile int threshold = 27;
    private volatile float onset;

    @Override
    public String getName() {
        return "Spectrum Ticker";
    }

    @Override
    public synchronized void refresh() {
        if (ticks % speed == 0) {
            for (int x = dimension.x - 2; x >= 0; x--) {
                Color color = new Color();
                if (rainbow == 1) {
                    float[] hsb = ledManager.getLEDColor(x, 0, 0).toHSB(null);
                    color.fromHSB((x + 1) / (float)(dimension.x - 1), 1, hsb[2]);
                } else color = ledManager.getLEDColor(x, 0, 0);
                ledManager.setLEDColor(x + 1, 0, 0, color);
                ledManager.setLEDColor(x, 0, 0, new Color());
            }
            if (onset > 0) {
                onset -= 0.05F * speed;
                Color color = new Color();
                if (rainbow == 2) color.fromHSB(hue % 360F, 1, onset);
                else if (rainbow == 1) color.fromHSB(0, 1, onset);
                else color = Util.multiplyColor(LEDCubeManager.getPaintColor(), onset);
                ledManager.setLEDColor(0, 0, 0, color);
                hue += 1F / 360F;
            }
        }
        /*for (int x = 0; x < 8; x++) {
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
        }*/
    }

    @Override
    public synchronized void reset() {
        amplitudes = new float[64];
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(15 - (speed - 1)) / 15F, 1F / 15F}),
            //new AnimationOption("density", "Density", AnimationOption.OptionType.SLIDER, new Object[]{(30 - (density - 1)) / 30F, 1F / 30F}),
            new AnimationOption("rainbow", "Rainbow", AnimationOption.OptionType.COMBOBUTTON, new Object[]{rainbow, 0, "Off", 1, "Static", 2, "Cycle"}),
            new AnimationOption("mode", "Mode", AnimationOption.OptionType.COMBOBUTTON, new Object[]{getBeatDetectMode(), BeatDetect.FREQ_ENERGY, "Frequency Energy", BeatDetect.SOUND_ENERGY, "Sound Energy"}),
            new AnimationOption("rangeMin", "Range Min", AnimationOption.OptionType.SLIDER, new Object[]{rangeMin / 26F, 1F / 27F}),
            new AnimationOption("rangeMax", "Range Max", AnimationOption.OptionType.SLIDER, new Object[]{rangeMax / 26F, 1F / 27F}),
            new AnimationOption("threshold", "Threshold", AnimationOption.OptionType.SLIDER, new Object[]{(threshold - 1) / 26F, 1F / 27F}),
            //new AnimationOption("mode", "Trigger", AnimationOption.OptionType.RADIOGROUP, new Object[]{mode, 0, "Kick", 1, "Snare", 2, "Hat"}),
        };
    }

    @Override
    public synchronized void optionChanged(String name, String value) {
        switch (name) {
            case "speed":
                speed = 1 + Math.round(15 * (1 - Float.parseFloat(value)));
                break;
            /*case "density":
                density = 2 + Math.round(30 * (1 - Float.parseFloat(value)));
                break;*/
            case "rainbow":
                rainbow = Integer.parseInt(value);
                break;
            case "mode":
                mode = Integer.parseInt(value);
                break;
            case "rangeMin":
                rangeMin = Math.round(26 * Float.parseFloat(value));
                break;
            case "rangeMax":
                rangeMax = Math.round(26 * Float.parseFloat(value));
                break;
            case "threshold":
                threshold = 1 + Math.round(26 * Float.parseFloat(value));
                break;
        }
    }

    @Override
    public boolean isFFT() {
        return false;
    }

    @Override
    public boolean isBeatDetect() {
        return true;
    }

    @Override
    public int getBeatDetectMode() {
        return mode;
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
    public synchronized void processBeatDetect(BeatDetect bt) {
        switch (mode) {
            case BeatDetect.FREQ_ENERGY:
                if (bt.isRange(Math.min(rangeMin, rangeMax), Math.max(rangeMin, rangeMax), threshold)) {
                    onset = 1;
                }
                break;
            case BeatDetect.SOUND_ENERGY:
                if (bt.isOnset()) {
                    onset = 1;
                }
                break;
        }
    }
}
