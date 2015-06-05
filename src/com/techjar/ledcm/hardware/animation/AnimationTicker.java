
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import java.util.Random;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class AnimationTicker extends Animation {
    private Random random = new Random();

    public AnimationTicker() {
        super();
    }

    @Override
    public String getName() {
        return "Ticker";
    }

    @Override
    public void refresh() {
        if (ticks % 2 == 0) {
            for (int x = dimension.x - 2; x >= 0; x--) {
                ledManager.setLEDColor(x + 1, 0, 0, ledManager.getLEDColor(x, 0, 0));
                ledManager.setLEDColor(x, 0, 0, new Color());
            }
            if (random.nextInt(10) == 0) {
                ledManager.setLEDColor(0, 0, 0, LEDCubeManager.getPaintColor());
            }
        }
    }

    @Override
    public void reset() {
    }
}
