
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.LEDUtil;
import com.techjar.ledcm.util.MathHelper;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

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
        LEDUtil.fill(ledManager, LEDCubeManager.getPaintColor());
    }

    @Override
    public void reset() {
    }
}
