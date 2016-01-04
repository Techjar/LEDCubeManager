
package com.techjar.ledcm.hardware.animation.sequence;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.animation.Animation;

/**
 * Args: (animation class name) [(option value) sets]
 *
 * @author Techjar
 */
public class SequenceCommandSetAnimation extends SequenceCommand {
    public SequenceCommandSetAnimation(AnimationSequence sequence) {
        super(sequence);
    }

    @Override
    public boolean execute(String[] args) {
        Animation anim = LEDCubeManager.getLEDCube().getAnimationByClassName("Animation" + args[0]);
        LEDCubeManager.getLEDCube().getCommThread().setCurrentAnimation(anim);
        LEDCubeManager.getInstance().getScreenMainControl().animComboBox.setSelectedItem(anim.getName());
        for (int i = 1; i < args.length; i += 2) {
            LEDCubeManager.getLEDCube().getCommThread().getCurrentAnimation().setOption(args[i], args[i + 1]);
        }
        return true;
    }
}
