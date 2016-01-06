
package com.techjar.ledcm.hardware.animation.sequence;

import com.techjar.ledcm.LEDCubeManager;

/**
 * Will wait until the current animation is finished. See {@link com.techjar.ledcm.hardware.animation.Animation#isFinished() Animation.isFinished()}
 *
 * @author Techjar
 */
public class SequenceCommandFinishWait extends SequenceCommand {
    public SequenceCommandFinishWait(AnimationSequence sequence) {
        super(sequence);
    }

    @Override
    public boolean execute(String[] args) {
        return LEDCubeManager.getLEDCube().getCommThread().getCurrentAnimation().isFinished();
    }
}
