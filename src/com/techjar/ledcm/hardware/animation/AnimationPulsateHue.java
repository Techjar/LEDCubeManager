
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationPulsateHue extends Animation {
    private Timer timer = new Timer();
    private float speed = 5;
    private float scale = 1;

    public AnimationPulsateHue() {
        super();
    }

    @Override
    public String getName() {
        return "Hue Pulsate";
    }

    @Override
    public synchronized void refresh() {
        for (int x = 0; x < dimension.x; x++) {
            double value = (timer.getSeconds() * (speed / 50) + ((dimension.x - x) / (float)dimension.x) * (scale / 10)) % 1;
            //double value = ((timer.getSeconds() / (dimension.y - 1) + ((dimension.y - 1 - y) / (dimension.y - 1F)) / 5)) % 1;
            Color color = new Color();
            color.fromHSB((float)value, 1, 1);
            ledManager.setLEDColor(x, 0, 0, color);
        }
    }

    @Override
    public synchronized void reset() {
        timer.restart();
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(speed - 1) / 149F}),
            new AnimationOption("scale", "Scale", AnimationOption.OptionType.SLIDER, new Object[]{(19 - (scale - 1)) / 19F}),
        };
    }

    @Override
    public synchronized void optionChanged(String name, String value) {
        switch (name) {
            case "speed":
                speed = 1 + (149 * Float.parseFloat(value));
                break;
            case "scale":
                scale = 1 + (19 * (1 - Float.parseFloat(value)));
                break;
        }
    }
}
