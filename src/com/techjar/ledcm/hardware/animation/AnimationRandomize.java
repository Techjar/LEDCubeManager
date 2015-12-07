
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.util.Timer;
import java.util.Random;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationRandomize extends Animation {
    private Random random = new Random();

    public AnimationRandomize() {
        super();
    }

    @Override
    public String getName() {
        return "Randomize";
    }

    @Override
    public synchronized void refresh() {
        for (int i = 0; i < 30 * (ledManager.getLEDCount() / 512F); i++) {
            if (random.nextInt(4) == 0) ledManager.setLEDColorReal(random.nextInt(dimension.x), random.nextInt(dimension.y), random.nextInt(dimension.z), new Color(random.nextInt(ledManager.getResolution() + 1), random.nextInt(ledManager.getResolution() + 1), random.nextInt(ledManager.getResolution() + 1)));
            else ledManager.setLEDColor(random.nextInt(dimension.x), random.nextInt(dimension.y), random.nextInt(dimension.z), new Color());
        }
    }

    @Override
    public synchronized void reset() {
    }
}
