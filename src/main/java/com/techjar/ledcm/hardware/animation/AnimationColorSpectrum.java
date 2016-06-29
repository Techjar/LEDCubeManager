
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
                    ledManager.setLEDColor(x, y, z, new Color(Math.round((x / (dimension.x - 1F)) * 255), Math.round((y / (dimension.y - 1F)) * 255), Math.round((z / (dimension.z - 1F)) * 255)));
                }
            }
        }
    }

    @Override
    public synchronized void reset() {
    }
}
