
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationPulsate extends Animation {
    private Timer timer = new Timer();
    private float speed = 2;
    private float scale = 3;

    public AnimationPulsate() {
        super();
    }

    @Override
    public String getName() {
        return "Pulsate";
    }

    @Override
    public void refresh() {
        for (int x = 0; x < dimension.x; x++) {
            double value = Math.sin(timer.getSeconds() * (speed / 5) + ((dimension.x - x) / (float)dimension.x) * scale) * 0.5 + 0.5;
            Color color = Util.multiplyColor(LEDCubeManager.getPaintColor(), value);
            ledManager.setLEDColor(x, 0, 0, color);
        }
    }

    @Override
    public void reset() {
        timer.restart();
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(speed - 1) / 149F}),
            new AnimationOption("scale", "Scale", AnimationOption.OptionType.SLIDER, new Object[]{(29 - (scale - 1)) / 29F}),
        };
    }

    @Override
    public void optionChanged(String name, String value) {
        switch (name) {
            case "speed":
                speed = 1 + (149 * Float.parseFloat(value));
                break;
            case "scale":
                scale = 1 + (29 * (1 - Float.parseFloat(value)));
                break;
        }
    }
}
