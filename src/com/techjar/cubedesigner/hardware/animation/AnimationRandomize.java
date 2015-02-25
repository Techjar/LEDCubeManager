
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
            if (random.nextInt(4) == 0) ledManager.setLEDColor(random.nextInt(dimension.x), random.nextInt(dimension.y), random.nextInt(dimension.z), new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            else ledManager.setLEDColor(random.nextInt(dimension.x), random.nextInt(dimension.y), random.nextInt(dimension.z), new Color());
        }
    }

    @Override
    public void reset() {
    }
}
