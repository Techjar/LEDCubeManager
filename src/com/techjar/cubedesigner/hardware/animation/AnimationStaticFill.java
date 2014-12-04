
package com.techjar.cubedesigner.hardware.animation;

import com.techjar.cubedesigner.CubeDesigner;

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
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                for (int z = 0; z < 8; z++) {
                    ledManager.setLEDColorNormalized(x, y, z, CubeDesigner.getPaintColor());
                }
            }
        }
    }

    @Override
    public void reset() {
    }
}
