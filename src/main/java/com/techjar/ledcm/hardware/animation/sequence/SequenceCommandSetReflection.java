
package com.techjar.ledcm.hardware.animation.sequence;

import com.techjar.ledcm.LEDCubeManager;

/**
 * Args: x y z<br><br>
 * Arguments are booleans.
 *
 * @author Techjar
 */
public class SequenceCommandSetReflection extends SequenceCommand {
    public SequenceCommandSetReflection(AnimationSequence sequence) {
        super(sequence);
    }

    @Override
    public boolean execute(String[] args) {
        LEDCubeManager.getLEDCube().setReflection(Boolean.parseBoolean(args[0]), Boolean.parseBoolean(args[1]), Boolean.parseBoolean(args[2]));
        return true;
    }
}
