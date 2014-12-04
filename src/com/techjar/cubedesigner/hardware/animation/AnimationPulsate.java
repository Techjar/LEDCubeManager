
package com.techjar.cubedesigner.hardware.animation;

import com.techjar.cubedesigner.CubeDesigner;
import com.techjar.cubedesigner.util.Timer;
import com.techjar.cubedesigner.util.Util;
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
        for (int y = 0; y < 8; y++) {
            double value = Math.sin(timer.getSeconds() * 2 + ((7 - y) / 7F) * 3) * 0.5 + 0.5;
            Color color = Util.multiplyColor(CubeDesigner.getPaintColor(), value);
            for (int x = 0; x < 8; x++) {
                for (int z = 0; z < 8; z++) {
                    ledManager.setLEDColorNormalized(x, y, z, color);
                }
            }
        }
    }

    @Override
    public void reset() {
        timer.restart();
    }
}
