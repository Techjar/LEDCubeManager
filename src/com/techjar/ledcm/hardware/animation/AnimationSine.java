
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationSine extends Animation {
    private Timer timer = new Timer();

    public AnimationSine() {
        super();
    }

    @Override
    public String getName() {
        return "Sine Wave";
    }

    @Override
    public void refresh() {
        for (int x = 0; x < 8; x++) {
            double value = Math.sin(timer.getSeconds() * 5 + ((7 - x) / 7F) * 3) * 0.5 + 0.5;
            int onY = (int)Math.round(value * 7);
            for (int z = 0; z < 8; z++) {
                for (int y = 0; y < 8; y++) {
                    ledManager.setLEDColor(x, y, z, y == onY ? LEDCubeManager.getPaintColor() : new Color());
                }
            }
        }
    }

    @Override
    public void reset() {
        timer.restart();
    }
}
