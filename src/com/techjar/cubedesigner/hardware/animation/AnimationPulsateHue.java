
package com.techjar.cubedesigner.hardware.animation;

import com.techjar.cubedesigner.CubeDesigner;
import com.techjar.cubedesigner.util.Timer;
import com.techjar.cubedesigner.util.Util;
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
        for (int y = 0; y < 8; y++) {
            double value = ((timer.getSeconds() / 7 + ((7 - y) / 7F) / 5)) % 1;
            Color color = new Color();
            color.fromHSB((float)value, 1, 1);
            for (int x = 0; x < 8; x++) {
                for (int z = 0; z < 8; z++) {
                    ledManager.setLEDColor(x, y, z, color);
                }
            }
        }
    }

    @Override
    public void reset() {
        timer.restart();
    }
}
