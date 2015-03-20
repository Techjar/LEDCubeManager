
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Timer;
import java.util.Random;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class AnimationScrollers extends Animation {
    private Random random = new Random();
    private int[] dots = new int[64];
    private boolean[] directions = new boolean[64];

    public AnimationScrollers() {
        super();
    }

    @Override
    public String getName() {
        return "Scrollers";
    }

    @Override
    public void refresh() {
        if (ticks % 3 == 0) {
            for (int x = 0; x < 8; x++) {
                for (int z = 0; z < 8; z++) {
                    int index = z | (x << 3);
                    int dot = dots[index];
                    if ((dot != 0b1 && dot != 0b10000000) || random.nextInt(500) == 0) {
                        if (directions[index]) dots[index] <<= 1;
                        else dots[index] >>= 1;
                        if (dots[index] == 0b1 || dots[index] == 0b10000000) directions[index] = !directions[index];
                    }
                }
            }
            updateCube();
        }
    }

    @Override
    public void reset() {
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                int index = z | (x << 3);
                directions[index] = random.nextBoolean();
                dots[index] = directions[index] ? 0b1 : 0b10000000;
            }
        }
        updateCube();
    }

    private void updateCube() {
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                int dot = dots[z | (x << 3)];
                for (int y = 0; y < 8; y++) {
                    ledManager.setLEDColor(x, y, z, checkBit(dot, y) ? LEDCubeManager.getPaintColor() : new Color());
                }
            }
        }
    }

    private boolean checkBit(int number, int bit) {
        return (number & (1 << bit)) != 0;
    }
}
