
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
public class AnimationScrollers extends Animation {
    private Random random = new Random();
    private long[] dots;
    private boolean[] directions;

    public AnimationScrollers() {
        super();
        dots = new long[dimension.x * dimension.z];
        directions = new boolean[dimension.x * dimension.z];
    }

    @Override
    public String getName() {
        return "Scrollers";
    }

    @Override
    public void refresh() {
        if (ticks % 3 == 0) {
            for (int x = 0; x < dimension.x; x++) {
                for (int z = 0; z < dimension.z; z++) {
                    int index = z + (x * dimension.y);
                    long dot = dots[index];
                    if ((dot != 0b1 && dot != (1L << (dimension.y - 1))) || random.nextInt(500 * (dots.length / 64)) == 0) {
                        if (directions[index]) dots[index] <<= 1;
                        else dots[index] >>>= 1;
                        if (dots[index] == 0b1 || dots[index] == (1L << (dimension.y - 1))) directions[index] = !directions[index];
                    }
                }
            }
            updateCube();
        }
    }

    @Override
    public void reset() {
        for (int x = 0; x < dimension.x; x++) {
            for (int z = 0; z < dimension.z; z++) {
                int index = z + (x * dimension.y);
                directions[index] = random.nextBoolean();
                dots[index] = directions[index] ? 0b1 : (1L << (dimension.y - 1));
            }
        }
        updateCube();
    }

    private void updateCube() {
        for (int x = 0; x < dimension.x; x++) {
            for (int z = 0; z < dimension.z; z++) {
                long dot = dots[z + (x * dimension.y)];
                for (int y = 0; y < dimension.y; y++) {
                    ledManager.setLEDColor(x, y, z, checkBit(dot, y) ? LEDCubeManager.getPaintColor() : new Color());
                }
            }
        }
    }

    private boolean checkBit(long number, int bit) {
        return (number & (1L << bit)) != 0;
    }
}
