
package com.techjar.ledcm.hardware.animation;

import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class AnimationColorSpectrum extends Animation {
    public AnimationColorSpectrum() {
        super();
    }

    @Override
    public String getName() {
        return "Color Spectrum";
    }

    @Override
    public synchronized void refresh() {
        for (int x = 0; x < dimension.x; x++) {
            for (int y = 0; y < dimension.y; y++) {
                for (int z = 0; z < dimension.z; z++) {
                    ledManager.setLEDColor(x, y, z, new Color((int)((x / (float)dimension.x) * 255), (int)((y / (float)dimension.y) * 255), (int)((z / (float)dimension.z) * 255)));
                }
            }
        }
    }

    @Override
    public synchronized void reset() {
    }
}
