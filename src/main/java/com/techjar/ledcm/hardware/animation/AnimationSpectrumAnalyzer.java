
package com.techjar.ledcm.hardware.animation;

import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;

/**
 *
 * @author Techjar
 */
public abstract class AnimationSpectrumAnalyzer extends Animation {
    public abstract boolean isFFT();
    public abstract boolean isBeatDetect();
    public abstract int getBeatDetectMode();
    public abstract void processFFT(FFT fft);
    public abstract void processBeatDetect(BeatDetect bt);
}
