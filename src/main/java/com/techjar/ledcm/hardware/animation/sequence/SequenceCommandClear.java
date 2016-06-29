
package com.techjar.ledcm.hardware.animation.sequence;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.LEDUtil;

/**
 * Clears the cube.
 *
 * @author Techjar
 */
public class SequenceCommandClear extends SequenceCommand {
    public SequenceCommandClear(AnimationSequence sequence) {
        super(sequence);
    }

    @Override
    public boolean execute(String[] args) {
        LEDUtil.clear(LEDCubeManager.getLEDCube().getLEDManager());
        return true;
    }
}
