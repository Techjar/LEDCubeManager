
package com.techjar.ledcm.hardware.animation.sequence;

import com.techjar.ledcm.LEDCubeManager;
import org.lwjgl.util.Color;

/**
 * Args: red green blue
 *
 * @author Techjar
 */
public class SequenceCommandSetColorPicker extends SequenceCommand {
    public SequenceCommandSetColorPicker(AnimationSequence sequence) {
        super(sequence);
    }

    @Override
    public boolean execute(String[] args) {
        LEDCubeManager.setPaintColor(new Color(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2])));
        return true;
    }
}
