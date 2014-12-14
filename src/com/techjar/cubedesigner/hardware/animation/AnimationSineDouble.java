
package com.techjar.cubedesigner.hardware.animation;

import com.techjar.cubedesigner.CubeDesigner;
import com.techjar.cubedesigner.util.Timer;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationSineDouble extends Animation {
    private Timer timer = new Timer();

    public AnimationSineDouble() {
        super();
    }

    @Override
    public String getName() {
        return "Sine/Cosine Wave";
    }

    @Override
    public void refresh() {
        for (int x = 0; x < 8; x++) {
            double input = timer.getSeconds() * 5 + ((7 - x) / 7F) * 3;
            double value = Math.sin(input) * 0.5 + 0.5;
            double value2 = Math.cos(input) * 0.5 + 0.5;
            int onY = (int)Math.round(value * 7);
            int onY2 = (int)Math.round(value2 * 7);
            for (int z = 0; z < 8; z++) {
                for (int y = 0; y < 8; y++) {
                    ledManager.setLEDColor(x, y, z, y == (z > 3 ? onY2 : onY) ? CubeDesigner.getPaintColor() : new Color());
                }
            }
        }
    }

    @Override
    public void reset() {
        timer.restart();
    }
}
