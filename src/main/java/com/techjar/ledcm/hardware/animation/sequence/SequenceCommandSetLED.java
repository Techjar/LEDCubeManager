
package com.techjar.ledcm.hardware.animation.sequence;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.manager.LEDManager;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 * Args: x y z red green blue
 *
 * @author Techjar
 */
public class SequenceCommandSetLED extends SequenceCommand {
    public SequenceCommandSetLED(AnimationSequence sequence) {
        super(sequence);
    }

    @Override
    public boolean execute(String[] args) {
        LEDManager ledManager = LEDCubeManager.getLEDCube().getLEDManager();
        ledManager.setLEDColor(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), new Color(Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5])));
        return true;
    }
}
