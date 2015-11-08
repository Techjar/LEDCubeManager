
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
        for (int z = 0; z < dimension.z; z++) {
            double value = Math.sin(timer.getSeconds() * 5 + ((dimension.z - 1 - z) / (dimension.z - 1F)) * 3) * 0.5 + 0.5;
            int onY = (int)Math.round(value * (dimension.y - 1));
            for (int x = 0; x < dimension.x; x++) {
                for (int y = 0; y < dimension.y; y++) {
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
