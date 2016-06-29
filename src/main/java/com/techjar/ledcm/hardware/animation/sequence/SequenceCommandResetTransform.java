
package com.techjar.ledcm.hardware.animation.sequence;

import com.techjar.ledcm.LEDCubeManager;

/**
 * Clears the cube.
 *
 * @author Techjar
 */
public class SequenceCommandResetTransform extends SequenceCommand {
    public SequenceCommandResetTransform(AnimationSequence sequence) {
        super(sequence);
    }

    @Override
    public boolean execute(String[] args) {
        LEDCubeManager.getLEDCube().resetTransform();
        return true;
    }
}
