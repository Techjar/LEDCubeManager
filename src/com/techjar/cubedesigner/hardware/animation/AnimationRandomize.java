
package com.techjar.cubedesigner.hardware.animation;

import com.techjar.cubedesigner.util.Timer;
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
    public void refresh() {
        for (int i = 0; i < 30; i++) {
            if (random.nextInt(4) == 0) ledManager.setLEDColor(random.nextInt(8), random.nextInt(8), random.nextInt(8), new Color(random.nextInt(16), random.nextInt(16), random.nextInt(16)));
            else ledManager.setLEDColor(random.nextInt(8), random.nextInt(8), random.nextInt(8), new Color());
        }
    }

    @Override
    public void reset() {
    }
}
