
package com.techjar.ledcm.hardware.animation.sequence;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.animation.Animation;
import com.techjar.ledcm.hardware.animation.AnimationOption;
import com.techjar.ledcm.util.Util;

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
        Animation anim = LEDCubeManager.getLEDCube().getCommThread().getCurrentAnimation();
        for (int i = 0; i < args.length; i += 2) {
            for (AnimationOption option : anim.getOptions()) {
                if (option.getId().equals(args[i])) {
                    Util.setOptionInGUI(option, args[i + 1]);
                    break;
                }
            }
        }
        return true;
    }
}
