
package com.techjar.cubedesigner.hardware.animation;

import com.techjar.cubedesigner.CubeDesigner;
import com.techjar.cubedesigner.hardware.LEDUtil;

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
    public void refresh() {
        LEDUtil.fill(ledManager, CubeDesigner.getPaintColor());
    }

    @Override
    public void reset() {
    }
}
