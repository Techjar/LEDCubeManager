
package com.techjar.ledcm.hardware.animation.sequence;

import com.techjar.ledcm.LEDCubeManager;

/**
 * Args: [(option value) sets]
 *
 * @author Techjar
 */
public class SequenceCommandSetOptions extends SequenceCommand {
    public SequenceCommandSetOptions(AnimationSequence sequence) {
        super(sequence);
    }

    @Override
    public boolean execute(String[] args) {
        for (int i = 0; i < args.length; i += 2) {
            LEDCubeManager.getLEDCube().getCommThread().getCurrentAnimation().setOption(args[i], args[i + 1]);
        }
        return true;
    }
}
