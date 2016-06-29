
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.LEDUtil;

/**
 *
 * @author Techjar
 */
public class AnimationStaticFill extends Animation {
    public AnimationStaticFill() {
        super();
    }

    @Override
    public String getName() {
        return "Static Fill";
    }

    @Override
    public synchronized void refresh() {
        LEDUtil.fill(ledManager, LEDCubeManager.getPaintColor());
    }

    @Override
    public synchronized void reset() {
    }
}
