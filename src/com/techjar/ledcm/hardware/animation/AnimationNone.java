
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
    public void refresh() {
    }

    @Override
    public void reset() {
        LEDUtil.clear(ledManager);
    }
}
