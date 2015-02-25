
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.util.Timer;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class AnimationDrain extends Animation {
    private int spread;
    private boolean state;

    public AnimationDrain() {
        super();
    }

    @Override
    public String getName() {
        return "Drain";
    }

    @Override
    public void refresh() {
        if (ticks % 6 == 0) {
            if (state && spread < 4) spread++;
            state = !state;
            for (int x = 3; x > 3 - spread; x--) {
                for (int z = 3; z > 3 - spread; z--) {
                    for (int y = 1; y < 8; y++) {
                        ledManager.setLEDColor(x, y - 1, z, ledManager.getLEDColor(x, y, z));
                        ledManager.setLEDColor(x, y, z, new Color());
                    }
                }
            }
            for (int x = 4; x < 4 + spread; x++) {
                for (int z = 3; z > 3 - spread; z--) {
                    for (int y = 1; y < 8; y++) {
                        ledManager.setLEDColor(x, y - 1, z, ledManager.getLEDColor(x, y, z));
                        ledManager.setLEDColor(x, y, z, new Color());
                    }
                }
            }
            for (int x = 3; x > 3 - spread; x--) {
                for (int z = 4; z < 4 + spread; z++) {
                    for (int y = 1; y < 8; y++) {
                        ledManager.setLEDColor(x, y - 1, z, ledManager.getLEDColor(x, y, z));
                        ledManager.setLEDColor(x, y, z, new Color());
                    }
                }
            }
            for (int x = 4; x < 4 + spread; x++) {
                for (int z = 4; z < 4 + spread; z++) {
                    for (int y = 1; y < 8; y++) {
                        ledManager.setLEDColor(x, y - 1, z, ledManager.getLEDColor(x, y, z));
                        ledManager.setLEDColor(x, y, z, new Color());
                    }
                }
            }
        }
    }

    @Override
    public void reset() {
        spread = 0;
        state = false;
    }
}
