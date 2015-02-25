
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import java.util.Random;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationProgressiveFill extends Animation {
    private Timer timer = new Timer();
    private Random random = new Random();
    private boolean[] states = new boolean[512];
    private int filledCount;

    public AnimationProgressiveFill() {
        super();
    }

    @Override
    public String getName() {
        return "Progressive Fill";
    }

    @Override
    public void refresh() {
        if (filledCount < states.length) {
            int count = (int)(timer.getMilliseconds() / 1000);
            for (int i = 0; i < count + 1; i++) {
                if (random.nextInt(10000) < timer.getMilliseconds()) {
                    int x = random.nextInt(dimension.x);
                    int y = random.nextInt(dimension.y);
                    int z = random.nextInt(dimension.z);
                    if (!states[Util.encodeCubeVector(x, y, z)]) {
                        states[Util.encodeCubeVector(x, y, z)] = true;
                        ledManager.setLEDColor(x, y, z, LEDCubeManager.getPaintColor());
                    }
                }
            }
            filledCount = 0;
            for (int i = 0; i < states.length; i++) {
                if (states[i]) filledCount++;
            }
        }
    }

    @Override
    public void reset() {
        timer.restart();
        states = new boolean[512];
        filledCount = 0;
        for (int x = 0; x < dimension.x; x++) {
            for (int y = 0; y < dimension.y; y++) {
                for (int z = 0; z < dimension.z; z++) {
                    ledManager.setLEDColor(x, y, z, new Color());
                }
            }
        }
    }
}
