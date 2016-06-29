
package com.techjar.ledcm.hardware.animation.sequence;

import com.techjar.ledcm.LEDCubeManager;

/**
 * Args: boolean
 *
 * @author Techjar
 */
public class SequenceCommandFreeze extends SequenceCommand {
    public SequenceCommandFreeze(AnimationSequence sequence) {
        super(sequence);
    }

    @Override
    public boolean execute(String[] args) {
        LEDCubeManager.getLEDCube().getCommThread().setFrozen(Boolean.parseBoolean(args[0]));
        return true;
    }
}
