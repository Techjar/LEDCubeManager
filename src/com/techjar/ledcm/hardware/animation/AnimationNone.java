
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.hardware.LEDUtil;

/**
 *
 * @author Techjar
 */
public class AnimationNone extends Animation {
    public AnimationNone() {
        super();
    }

    @Override
    public String getName() {
        return "None";
    }

    @Override
    public synchronized void refresh() {
    }

    @Override
    public synchronized void reset() {
        LEDUtil.clear(ledManager);
    }
}
