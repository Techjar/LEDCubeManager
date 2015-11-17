
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationPulsateHue extends Animation {
    private Timer timer = new Timer();

    public AnimationPulsateHue() {
        super();
    }

    @Override
    public String getName() {
        return "Hue Pulsate";
    }

    @Override
    public synchronized void refresh() {
        for (int y = 0; y < dimension.y; y++) {
            double value = ((timer.getSeconds() / 7 + ((7 - y) / 7F) / 5)) % 1;
            //double value = ((timer.getSeconds() / (dimension.y - 1) + ((dimension.y - 1 - y) / (dimension.y - 1F)) / 5)) % 1;
            Color color = new Color();
            color.fromHSB((float)value, 1, 1);
            for (int x = 0; x < dimension.x; x++) {
                for (int z = 0; z < dimension.z; z++) {
                    ledManager.setLEDColor(x, y, z, color);
                }
            }
        }
    }

    @Override
    public synchronized void reset() {
        timer.restart();
    }
}
