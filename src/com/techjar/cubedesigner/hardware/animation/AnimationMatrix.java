
package com.techjar.cubedesigner.hardware.animation;

import com.techjar.cubedesigner.util.MathHelper;
import com.techjar.cubedesigner.util.Timer;
import java.util.Random;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationMatrix extends Animation {
    private Timer timer = new Timer();
    private int[] lines = new int[64];
    private Color[] colors = new Color[64];
    private boolean[] states = new boolean[512];
    private Random random = new Random();
    private float hue = 120F / 360F;

    public AnimationMatrix() {
        super();
    }

    @Override
    public String getName() {
        return "The Matrix";
    }

    @Override
    public void refresh() {
        if (timer.getMilliseconds() >= 50) {
            timer.restart();
            for (int x = 0; x < 8; x++) {
                for (int z = 0; z < 8; z++) {
                    int index = x | (z << 3);
                    lines[index] >>= 1;
                    for (int y = 7; y >= 0; y--) {
                        int stateIndex = index | (y << 6);
                        if (y == 7) {
                            if (lines[index] == 0 && random.nextInt(20) == 0) {
                                int count = random.nextInt(3) + 3;
                                for (int i = 0; i < count; i++) lines[index] |= (0b10000000 << i);
                                float value = random.nextFloat() * 1.333F;
                                Color color = new Color();
                                color.fromHSB(hue, Math.min(0.333F + value, 1), Math.min(0.333F + (1 - value), 1));
                                colors[index] = color;
                            }
                        }
                        boolean bit = checkBit(lines[index], y);
                        if (bit != states[stateIndex]) {
                            states[stateIndex] = bit;
                            ledManager.setLEDColor(x, y, z, states[stateIndex] ? colors[index] : new Color());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void reset() {
        lines = new int[64];
        states = new boolean[512];
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                for (int z = 0; z < 8; z++) {
                    ledManager.setLEDColor(x, y, z, new Color());
                }
            }
        }
    }

    private boolean checkBit(int number, int bit) {
        return (number & (1 << bit)) != 0;
    }
}
