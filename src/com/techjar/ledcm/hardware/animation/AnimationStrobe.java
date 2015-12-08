
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Timer;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationStrobe extends Animation {
    private boolean state;
    private int speed = 2;

    public AnimationStrobe() {
        super();
    }

    @Override
    public String getName() {
        return "Strobe";
    }

    @Override
    public synchronized void refresh() {
        if (ticks % speed == 0) {
            state = !state;
            for (int x = 0; x < dimension.x; x++) {
                for (int y = 0; y < dimension.y; y++) {
                    for (int z = 0; z < dimension.z; z++) {
                        ledManager.setLEDColor(x, y, z, state ? LEDCubeManager.getPaintColor() : new Color());
                    }
                }
            }
        }
    }

    @Override
    public synchronized void reset() {
        state = false;
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(59 - (speed - 1)) / 59F, 1F / 59F, false}),
        };
    }

    @Override
    public void optionChanged(String name, String value) {
        switch (name) {
            case "speed":
                speed = 1 + Math.round(59 * (1 - Float.parseFloat(value)));
                break;
        }
    }
}
