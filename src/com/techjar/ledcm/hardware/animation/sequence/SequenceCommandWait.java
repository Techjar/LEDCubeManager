
package com.techjar.ledcm.hardware.animation.sequence;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Timer;

/**
 * Args: milliseconds<br><br>
 * Will wait until specified time in music position when music sync mode is activated.
 *
 * @author Techjar
 */
public class SequenceCommandWait extends SequenceCommand {
    private Timer timer = new Timer();

    public SequenceCommandWait(AnimationSequence sequence) {
        super(sequence);
    }

    @Override
    public boolean execute(String[] args) {
        if (sequence.isMusicSynced()) {
            return LEDCubeManager.getLEDCube().getSpectrumAnalyzer().getPositionMillis() >= Integer.parseInt(args[0]);
        } else {
            return timer.getMilliseconds() >= Integer.parseInt(args[0]);
        }
    }
}
