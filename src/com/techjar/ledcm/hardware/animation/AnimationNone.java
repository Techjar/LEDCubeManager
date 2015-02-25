
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import org.lwjgl.util.Color;

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
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                for (int z = 0; z < 8; z++) {
                    ledManager.setLEDColor(x, y, z, new Color());
                }
            }
        }
    }
}
