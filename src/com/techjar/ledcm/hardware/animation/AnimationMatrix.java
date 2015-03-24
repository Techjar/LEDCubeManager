
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.hardware.LEDUtil;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import java.util.Random;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationMatrix extends Animation {
    private long[] lines;
    private Color[] colors;
    private boolean[] states;
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
        if (ticks % 3 == 0) {
            for (int x = 0; x < dimension.x; x++) {
                for (int z = 0; z < dimension.z; z++) {
                    int index = x | (z << Util.getRequiredBits(dimension.x - 1));
                    lines[index] >>= 1;
                    for (int y = dimension.y - 1; y >= 0; y--) {
                        int stateIndex = index | (y << (Util.getRequiredBits(dimension.x - 1) + Util.getRequiredBits(dimension.z - 1)));
                        if (y == dimension.y - 1) {
                            if (lines[index] == 0 && random.nextInt(20) == 0) {
                                int count = random.nextInt(3) + 3;
                                for (int i = 0; i < count; i++) lines[index] |= ((1L << (dimension.y - 1)) << i);
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
        lines = new long[dimension.x * dimension.z];
        colors = new Color[dimension.x * dimension.z];
        states = new boolean[dimension.x * dimension.y * dimension.z];
        LEDUtil.clear(ledManager);
    }

    private boolean checkBit(long number, int bit) {
        return (number & (1L << bit)) != 0;
    }
}
