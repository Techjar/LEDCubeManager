
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import java.util.Random;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationRain extends Animation {
    private int[] drops = new int[64];
    private boolean[] floor = new boolean[64];
    private int[] lightning = new int[64];
    private boolean[] states = new boolean[512];
    private Random random = new Random();
    private Color topColor = new Color(0, 0, 255);
    private Color bottomColor = new Color(255, 0, 255);
    private Color lightningColor = new Color(225, 255, 255);

    public AnimationRain() {
        super();
    }

    @Override
    public String getName() {
        return "Rain";
    }

    @Override
    public void refresh() {
        if (ticks % 3 == 0) {
            for (int x = 0; x < 8; x++) {
                for (int z = 0; z < 8; z++) {
                    int index = x | (z << 3);
                    if (lightning[index] > 0) {
                        lightning[index]--;
                        if (lightning[index] == 0) {
                            for (int y = 0; y < 8; y++) {
                                Color color = MathHelper.lerp(bottomColor, topColor, y / 7F);
                                if (y == 0) ledManager.setLEDColor(x, y, z, floor[index] ? color: new Color());
                                else ledManager.setLEDColor(x, y, z, checkBit(drops[index], y) ? color: new Color());
                            }
                        }
                    } else if (random.nextInt(3000) == 0) {
                        lightning[index] = random.nextInt(30) + 1;
                    }
                    boolean lightningCheck = checkLightning(lightning[index]);
                    if (lightning[index] > 0) {
                        for (int y = 0; y < 8; y++) {
                            ledManager.setLEDColor(x, y, z, lightningCheck ? lightningColor : new Color());
                        }
                    }
                    drops[index] >>= 1;
                    for (int y = 7; y >= 0; y--) {
                        int stateIndex = index | (y << 6);
                        Color color = MathHelper.lerp(bottomColor, topColor, y / 7F);
                        if (y == 7) {
                            if (random.nextInt(20) == 0) drops[index] |= 0b10000000;
                        } else if (y == 0) {
                            if (floor[index] && random.nextInt(30) == 0) {
                                floor[index] = false;
                            }
                            if (checkBit(drops[index], y)) {
                                drops[index] &= 0b11111110;
                                floor[index] = true;
                            }
                        }
                        if (y == 0) {
                            if (floor[index] != states[stateIndex]) {
                                states[stateIndex] = floor[index];
                                if (!lightningCheck) ledManager.setLEDColor(x, y, z, states[stateIndex] ? color : new Color());
                            }
                        } else {
                            boolean bit = checkBit(drops[index], y);
                            if (bit != states[stateIndex]) {
                                states[stateIndex] = bit;
                                if (!lightningCheck) ledManager.setLEDColor(x, y, z, states[stateIndex] ? color : new Color());
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void reset() {
        drops = new int[64];
        floor = new boolean[64];
        lightning = new int[64];
        states = new boolean[512];
    }

    private boolean checkBit(int number, int bit) {
        return (number & (1 << bit)) != 0;
    }

    private boolean checkLightning(int value) {
        return value > 0 ? random.nextInt(value) == 0 : false;
    }
}
