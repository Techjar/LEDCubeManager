
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
    public synchronized void refresh() {
        for (int y = 0; y < dimension.y; y++) {
            double value = Math.sin(timer.getSeconds() * 2 + ((7 - y) / 7F) * 3) * 0.5 + 0.5;
            Color color = Util.multiplyColor(LEDCubeManager.getPaintColor(), value);
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
