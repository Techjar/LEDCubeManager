
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
public class AnimationDissolve extends Animation {
    private Timer timer = new Timer();
    private Random random = new Random();
    private boolean[] states;
    private int filledCount;

    public AnimationDissolve() {
        super();
    }

    @Override
    public String getName() {
        return "Dissolve";
    }

    @Override
    public synchronized void refresh() {
        if (filledCount < states.length) {
            int count = (int)(timer.getMilliseconds() / 1000);
            for (int i = 0; i < (count + 1) * Math.max(ledManager.getLEDCount() / 512, 1); i++) {
                if (random.nextInt(30000) < timer.getMilliseconds()) {
                    int x = random.nextInt(dimension.x);
                    int y = random.nextInt(dimension.y);
                    int z = random.nextInt(dimension.z);
                    if (!states[Util.encodeCubeVector(x, y, z)]) {
                        states[Util.encodeCubeVector(x, y, z)] = true;
                        ledManager.setLEDColor(x, y, z, LEDCubeManager.getPaintColor());
                        filledCount++;
                    }
                }
            }
        }
    }

    @Override
    public synchronized void reset() {
        timer.restart();
        states = new boolean[ledManager.getLEDCount()];
        filledCount = 0;
    }

    @Override
    public boolean isFinished() {
        return filledCount == states.length;
    }
}
