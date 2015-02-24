
package com.techjar.cubedesigner.hardware.animation;

import com.techjar.cubedesigner.CubeDesigner;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationBlink extends Animation {
    private boolean state;

    public AnimationBlink() {
        super();
    }

    @Override
    public String getName() {
        return "Blink";
    }

    @Override
    public void refresh() {
        if (ticks % 30 == 0) {
            state = !state;
            for (int x = 0; x < dimension.x; x++) {
                for (int y = 0; y < dimension.y; y++) {
                    for (int z = 0; z < dimension.z; z++) {
                        ledManager.setLEDColor(x, y, z, state ? CubeDesigner.getPaintColor() : new Color());
                    }
                }
            }
        }
    }

    @Override
    public void reset() {
        state = false;
    }

}
