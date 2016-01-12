
package com.techjar.ledcm.hardware.animation.sequence;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.gui.GUI;
import com.techjar.ledcm.gui.GUIButton;
import com.techjar.ledcm.hardware.animation.Animation;
import com.techjar.ledcm.hardware.animation.AnimationOption;
import com.techjar.ledcm.util.Util;
import java.util.List;

/**
 * Args: (animation class name) [(option value) sets]<br><br>
 * The animation's options will be reset to default when set.
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
        List<GUI> components = LEDCubeManager.getInstance().getScreenMainControl().animOptionsScrollBox.findComponentsByName("resetdefaults_button");
        if (components.size() > 0) {
            GUI component = components.get(0);
            if (component instanceof GUIButton) {
                ((GUIButton)component).click();
            }
        }
        for (int i = 1; i < args.length; i += 2) {
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
