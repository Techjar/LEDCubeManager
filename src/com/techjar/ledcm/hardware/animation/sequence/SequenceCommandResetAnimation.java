
package com.techjar.ledcm.hardware.animation.sequence;

import com.techjar.ledcm.LEDCubeManager;

/**
 * Resets the current animation.
 *
 * @author Techjar
 */
public class SequenceCommandResetAnimation extends SequenceCommand {
    public SequenceCommandResetAnimation(AnimationSequence sequence) {
        super(sequence);
    }

    @Override
    public boolean execute(String[] args) {
        LEDCubeManager.getLEDCube().getCommThread().getCurrentAnimation().reset();
        return true;
    }
}
