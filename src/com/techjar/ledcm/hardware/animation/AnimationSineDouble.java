
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Timer;
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
        for (int x = 0; x < dimension.x; x++) {
            double input = timer.getSeconds() * 5 + ((dimension.x - 1 - x) / (dimension.x - 1F)) * 3;
            double value = Math.sin(input) * 0.5 + 0.5;
            double value2 = Math.cos(input) * 0.5 + 0.5;
            int onY = (int)Math.round(value * (dimension.y - 1));
            int onY2 = (int)Math.round(value2 * (dimension.y - 1));
            for (int z = 0; z < dimension.z; z++) {
                for (int y = 0; y < dimension.y; y++) {
                    ledManager.setLEDColor(x, y, z, y == (z > (dimension.z / 2 - 1) ? onY2 : onY) ? LEDCubeManager.getPaintColor() : new Color());
                }
            }
        }
    }

    @Override
    public void reset() {
        timer.restart();
    }
}
