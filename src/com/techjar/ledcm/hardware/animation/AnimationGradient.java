
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.LEDUtil;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class AnimationGradient extends Animation {
    private Timer timer = new Timer();
    private Color color1 = new Color(255, 0, 0);
    private Color color2 = new Color(0, 0, 255);
    private float speed = 7;
    private float scale = 2;
    private boolean pulsate;

    public AnimationGradient() {
        super();
    }

    @Override
    public String getName() {
        return "Gradient";
    }

    @Override
    public synchronized void refresh() {
        for (int x = 0; x < dimension.x; x++) {
            float value = x / (dimension.x - 1F);
            if (pulsate) value = (float)Math.sin(timer.getSeconds() * (speed / 5) + ((dimension.x - x) / (float)dimension.x) * scale) * 0.5F + 0.5F;
            Color color = MathHelper.lerpXyz(color1, color2, value);
            for (int y = 0; y < dimension.y; y++) {
                for (int z = 0; z < dimension.z; z++) {
                    ledManager.setLEDColor(x, y, z, color);
                }
            }
        }
    }

    @Override
    public synchronized void reset() {
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("color1", "Start Color", AnimationOption.OptionType.COLORPICKER, new Object[]{color1}),
            new AnimationOption("color2", "End Color", AnimationOption.OptionType.COLORPICKER, new Object[]{color2}),
            new AnimationOption("pulsate", "Pulsate", AnimationOption.OptionType.CHECKBOX, new Object[]{pulsate}),
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(speed - 1) / 149F}),
            new AnimationOption("scale", "Scale", AnimationOption.OptionType.SLIDER, new Object[]{(29 - (scale - 1)) / 29F}),
        };
    }

    @Override
    public synchronized void optionChanged(String name, String value) {
        switch (name) {
            case "color1":
                color1 = Util.stringToColor(value);
                break;
            case "color2":
                color2 = Util.stringToColor(value);
                break;
            case "pulsate":
                pulsate = Boolean.parseBoolean(value);
                break;
            case "speed":
                speed = 1 + (149 * Float.parseFloat(value));
                break;
            case "scale":
                scale = 1 + (29 * (1 - Float.parseFloat(value)));
                break;
        }
    }
}
