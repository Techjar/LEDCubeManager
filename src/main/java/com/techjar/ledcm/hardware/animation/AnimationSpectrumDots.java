
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Util;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;
import java.util.Random;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationSpectrumDots extends AnimationSpectrumAnalyzer {
    private Random random = new Random();
    private float[] amplitudes;
    private Color[] randomColors;
    private int bandIncrement = 3;
    private int colorMode;
    private float holdUp = 7;
    private boolean stereo;

    public AnimationSpectrumDots() {
        super();
        amplitudes = new float[dimension.x];
        randomColors = new Color[dimension.x];
        for (int i = 0; i < randomColors.length; i++) randomColors[i] = new Color();
    }

    @Override
    public String getName() {
        return "Spectrum Dots";
    }

    @Override
    public synchronized void refresh() {
        for (int i = 0; i < amplitudes.length; i++) {
            float amplitude = (float)MathHelper.log(amplitudes[i], 1.45F); // Huh?
            float increment = 10.0F - (i * 0.045F); // What?
            float brightness = MathHelper.clamp(amplitude / increment, 0, 1);
            Color color = new Color();
            if (colorMode == 1) color.fromHSB(i / (float)amplitudes.length, 1, brightness);
            else if (colorMode == 2) color = colorAtBrightness(brightness);
            else if (colorMode == 3) color.fromHSB((1 - brightness) * (300F / 360F), 1, brightness);
            else if (colorMode == 4) color = ledManager.getLEDColor(i, 0, 0).equals(new Color()) || randomColors[i].equals(new Color()) ? Util.multiplyColor(randomColors[i] = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)), brightness) : Util.multiplyColor(randomColors[i], brightness);
            else color = Util.multiplyColor(LEDCubeManager.getPaintColor(), brightness);
            ledManager.setLEDColor(i, 0, 0, amplitude > 0 ? color : new Color());
        }
    }

    @Override
    public synchronized void reset() {
        amplitudes = new float[dimension.x];
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("colorMode", "Color", AnimationOption.OptionType.COMBOBOX, new Object[]{colorMode, 0, "Picker", 1, "Rainbow", 2, "Amplitude", 3, "Amplitude Rainbow", 4, "Random"}),
            new AnimationOption("holdUp", "Hold Up", AnimationOption.OptionType.SLIDER, new Object[]{holdUp / 29F}),
            new AnimationOption("stereo", "Stereo", AnimationOption.OptionType.CHECKBOX, new Object[]{stereo}),
        };
    }

    @Override
    public synchronized void optionChanged(String name, String value) {
        switch (name) {
            case "colorMode":
                colorMode = Integer.parseInt(value);
                break;
            case "holdUp":
                holdUp = 1 + (29 * Float.parseFloat(value));
                break;
            case "stereo":
                stereo = Boolean.parseBoolean(value);
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
    public int getDesiredFFTCount()  {
        return stereo ? 2 : 1;
    }

    @Override
    public synchronized void processFFT(FFT[] fft) {
        if (fft.length == 1) {
            for (int i = 0; i < amplitudes.length; i++) {
                float amplitude = 0;
                for (int j = 0; j < bandIncrement; j++) {
                    float band = fft[0].getBand(i * bandIncrement + j);
                    if (band > amplitude) amplitude = band;
                }
                if (amplitude > amplitudes[i]) amplitudes[i] = amplitude;
                else if (amplitudes[i] > 0)
                    amplitudes[i] -= amplitudes[i] / Math.max(holdUp * MathHelper.log(i, 10), 12F);
            }
        } else {
            for (int i = amplitudes.length / 2 - 1; i >= 0; i--) {
                int k = amplitudes.length / 2 - 1 - i;
                float amplitude = 0;
                for (int j = 0; j < bandIncrement * 2; j++) {
                    float band = fft[0].getBand(k * bandIncrement * 2 + j);
                    if (band > amplitude) amplitude = band;
                }
                if (amplitude > amplitudes[i]) amplitudes[i] = amplitude;
                else if (amplitudes[i] > 0)
                    amplitudes[i] -= amplitudes[i] / Math.max(holdUp * MathHelper.log(i, 10), 12F);
            }
            for (int i = amplitudes.length / 2; i < amplitudes.length; i++) {
                int k = i - amplitudes.length / 2;
                float amplitude = 0;
                for (int j = 0; j < bandIncrement * 2; j++) {
                    float band = fft[1].getBand(k * bandIncrement * 2 + j);
                    if (band > amplitude) amplitude = band;
                }
                if (amplitude > amplitudes[i]) amplitudes[i] = amplitude;
                else if (amplitudes[i] > 0)
                    amplitudes[i] -= amplitudes[i] / Math.max(holdUp * MathHelper.log(i, 10), 12F);
            }
        }
    }

    @Override
    public void processBeatDetect(BeatDetect bt) {
    }

    private Color colorAtBrightness(float brightness) {
        int res = 255;
        Color red = new Color(Math.round(res * brightness), 0, 0);
        Color yellow = new Color(Math.round(res * brightness), Math.round(res * brightness), 0);
        Color green = new Color(0, Math.round(res * brightness), 0);
        Color blue = new Color(0, 0, Math.round(res * brightness));
        if (brightness >= 0.9) return MathHelper.lerpXyz(yellow, red, (brightness - 0.9F) * 10);
        else if (brightness >= 0.6) return MathHelper.lerpXyz(green, yellow, (brightness - 0.6F) * 3.33333F);
        else if (brightness >= 0.2) return MathHelper.lerpXyz(blue, green, (brightness - 0.2F) * 2.5F);
        else if (brightness > 0.001) return blue;
        return new Color();
    }
}
