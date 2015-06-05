
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
    public void refresh() {
        for (int x = 0; x < dimension.x; x++) {
            double value = ((timer.getSeconds() / 7 + ((7 - x) / (float)dimension.x) / 5)) % 1;
            //double value = ((timer.getSeconds() / (dimension.y - 1) + ((dimension.y - 1 - y) / (dimension.y - 1F)) / 5)) % 1;
            Color color = new Color();
            color.fromHSB((float)value, 1, 1);
            ledManager.setLEDColor(x, 0, 0, color);
        }
    }

    @Override
    public void reset() {
        timer.restart();
    }
}
