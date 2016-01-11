
package com.techjar.ledcm.hardware.animation.sequence;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Vector3;

/**
 * Args: degrees (axis [x|y|z])
 *
 * @author Techjar
 */
public class SequenceCommandRotateTransform extends SequenceCommand {
    public SequenceCommandRotateTransform(AnimationSequence sequence) {
        super(sequence);
    }

    @Override
    public boolean execute(String[] args) {
        Vector3 axis;
        switch (args[1]) {
            case "X":
            case "x":
                axis = new Vector3(1, 0, 0);
                break;
            case "Y":
            case "y":
                axis = new Vector3(0, 1, 0);
                break;
            case "Z":
            case "z":
                axis = new Vector3(0, 0, 1);
                break;
            default:
                throw new IllegalArgumentException("Invalid axis: " + args[1]);
        }
        LEDCubeManager.getLEDCube().rotateTransform((float)Math.toRadians(Float.parseFloat(args[0])), axis);
        return true;
    }
}
