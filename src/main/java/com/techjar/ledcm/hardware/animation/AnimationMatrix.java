
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.hardware.LEDUtil;
import com.techjar.ledcm.util.BitSetUtil;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import java.util.BitSet;
import java.util.Random;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationMatrix extends Animation {
    private BitSet[] lines;
    private Color[] colors;
    private boolean[] states;
    private Random random = new Random();
    private float hue = 120F / 360F;

    public AnimationMatrix() {
        super();
        lines = new BitSet[dimension.x * dimension.z];
    }

    @Override
    public String getName() {
        return "The Matrix";
    }

    @Override
    public synchronized void refresh() {
        if (ticks % 3 == 0) {
            for (int x = 0; x < dimension.x; x++) {
                for (int z = 0; z < dimension.z; z++) {
                    int index = x + (z * dimension.x);
                    BitSetUtil.shiftRight(lines[index], 1);
                    for (int y = dimension.y - 1; y >= 0; y--) {
                        int stateIndex = index + y * dimension.x * dimension.z;
                        if (y == dimension.y - 1) {
                            if (lines[index].isEmpty() && random.nextInt(20) == 0) {
                                int count = random.nextInt(3) + 3;
                                for (int i = 0; i < count; i++) lines[index].set((dimension.y - 1) + i);
                                float value = random.nextFloat() * 1.333F;
                                Color color = new Color();
                                color.fromHSB(hue, Math.min(0.333F + value, 1), Math.min(0.333F + (1 - value), 1));
                                colors[index] = color;
                            }
                        }
                        boolean bit = lines[index].get(y);
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
    public synchronized void reset() {
        for (int i = 0; i < lines.length; i++) {
            lines[i] = new BitSet();
        }
        colors = new Color[dimension.x * dimension.z];
        states = new boolean[dimension.x * dimension.y * dimension.z];
        LEDUtil.clear(ledManager);
    }

    private boolean checkBit(long number, int bit) {
        return (number & (1L << bit)) != 0;
    }
}
