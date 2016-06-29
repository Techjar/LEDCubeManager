
package com.techjar.ledcm.hardware.animation.sequence;

import com.techjar.ledcm.LEDCubeManager;
import java.io.File;

/**
 * Args: (file path)
 *
 * @author Techjar
 */
public class SequenceCommandLoadMusic extends SequenceCommand {
    private boolean loaded;

    public SequenceCommandLoadMusic(AnimationSequence sequence) {
        super(sequence);
    }

    @Override
    public boolean execute(String[] args) {
        if (!loaded) {
            LEDCubeManager.getLEDCube().getSpectrumAnalyzer().stop();
            LEDCubeManager.getLEDCube().getSpectrumAnalyzer().loadFile(new File(args[0]));
            loaded = true;
        }
        return LEDCubeManager.getLEDCube().getSpectrumAnalyzer().isPlaying();
    }

    @Override
    public boolean onSequenceLoad(String[] args) {
        try {
            LEDCubeManager.getLEDCube().getSpectrumAnalyzer().convertFile(new File(args[0]), false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }
}
