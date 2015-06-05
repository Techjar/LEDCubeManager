
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationPulsate extends Animation {
    private Timer timer = new Timer();

    public AnimationPulsate() {
        super();
    }

    @Override
    public String getName() {
        return "Pulsate";
    }

    @Override
    public void refresh() {
        for (int x = 0; x < dimension.x; x++) {
            double value = Math.sin(timer.getSeconds() * 2 + ((7 - x) / (float)dimension.x) * 3) * 0.5 + 0.5;
            Color color = Util.multiplyColor(LEDCubeManager.getPaintColor(), value);
            ledManager.setLEDColor(x, 0, 0, color);
        }
    }

    @Override
    public void reset() {
        timer.restart();
    }
}
