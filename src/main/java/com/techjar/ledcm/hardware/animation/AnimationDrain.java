
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.util.Timer;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class AnimationDrain extends Animation {
    private int spreadX;
    private int spreadZ;
    private boolean state;

    public AnimationDrain() {
        super();
    }

    @Override
    public String getName() {
        return "Drain";
    }

    @Override
    public synchronized void refresh() {
        if (ticks % 6 == 0) {
            if (state && spreadX < dimension.x / 2) spreadX++;
            if (state && spreadZ < dimension.z / 2) spreadZ++;
            state = !state;
            for (int x = (dimension.x / 2) - 1; x > (dimension.x / 2) - 1 - spreadX; x--) {
                for (int z = (dimension.z / 2) - 1; z > (dimension.z / 2) - 1 - spreadZ; z--) {
                    for (int y = 1; y < dimension.y; y++) {
                        ledManager.setLEDColor(x, y - 1, z, ledManager.getLEDColor(x, y, z));
                        ledManager.setLEDColor(x, y, z, new Color());
                    }
                }
            }
            for (int x = dimension.x / 2; x < (dimension.x / 2) + spreadX; x++) {
                for (int z = (dimension.z / 2) - 1; z > (dimension.z / 2) - 1 - spreadZ; z--) {
                    for (int y = 1; y < dimension.y; y++) {
                        ledManager.setLEDColor(x, y - 1, z, ledManager.getLEDColor(x, y, z));
                        ledManager.setLEDColor(x, y, z, new Color());
                    }
                }
            }
            for (int x = (dimension.x / 2) - 1; x > (dimension.x / 2) - 1 - spreadX; x--) {
                for (int z = dimension.z / 2; z < (dimension.z / 2) + spreadZ; z++) {
                    for (int y = 1; y < dimension.y; y++) {
                        ledManager.setLEDColor(x, y - 1, z, ledManager.getLEDColor(x, y, z));
                        ledManager.setLEDColor(x, y, z, new Color());
                    }
                }
            }
            for (int x = dimension.x / 2; x < (dimension.x / 2) + spreadX; x++) {
                for (int z = dimension.z / 2; z < (dimension.z / 2) + spreadZ; z++) {
                    for (int y = 1; y < dimension.y; y++) {
                        ledManager.setLEDColor(x, y - 1, z, ledManager.getLEDColor(x, y, z));
                        ledManager.setLEDColor(x, y, z, new Color());
                    }
                }
            }
        }
    }

    @Override
    public synchronized void reset() {
        spreadX = 0;
        spreadZ = 0;
        state = false;
    }

    @Override
    public boolean isFinished() {
        for (int x = 0; x < dimension.x; x++) {
            for (int y = 0; y < dimension.y; y++) {
                for (int z = 0; z < dimension.z; z++) {
                    if (!ledManager.getLEDColor(x, y, z).equals(ReadableColor.BLACK)) return false;
                }
            }
        }
        return true;
    }
}
