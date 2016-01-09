
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationGradient extends Animation {
    private Timer timer = new Timer();
    private Color color1 = new Color(255, 255, 255);
    private Color color2 = new Color(0, 0, 0);
    private float speed = 10;
    private float scale = 3;
    private boolean pulsate;
    private int colorMode = 0;

    public AnimationGradient() {
        super();
    }

    @Override
    public String getName() {
        return "Gradient";
    }

    @Override
    public synchronized void refresh() {
        for (int y = 0; y < dimension.y; y++) {
            float value = y / (dimension.y - 1F);
            Color color = new Color();
            if (colorMode == 0) {
                if (pulsate) value = (float)Math.sin(timer.getSeconds() * (speed / 5) + (((dimension.y - 1) - y) / (dimension.y - 1F)) * scale) * 0.5F + 0.5F;
                color = MathHelper.lerp(color1, color2, value);
            }
            else if (colorMode == 1) {
                if (pulsate) value = (((float)timer.getSeconds() * (speed * 0.0142857F) + (((dimension.y - 1) - y) / (dimension.y - 1F)) / (scale / 0.6F))) % 1;
                else value *= (300F / 360F);
                color.fromHSB(value, 1, 1);
            }
            for (int x = 0; x < dimension.x; x++) {
                for (int z = 0; z < dimension.z; z++) {
                    ledManager.setLEDColor(x, y, z, color);
                }
            }
        }
    }

    @Override
    public synchronized void reset() {
        timer.restart();
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("color1", "Start Color", AnimationOption.OptionType.COLORPICKER, new Object[]{color1}),
            new AnimationOption("color2", "End Color", AnimationOption.OptionType.COLORPICKER, new Object[]{color2}),
            new AnimationOption("colormode", "Color Mode", AnimationOption.OptionType.COMBOBOX, new Object[]{colorMode, 0, "Bicolor", 1, "Rainbow"}),
            new AnimationOption("pulsate", "Pulsate", AnimationOption.OptionType.CHECKBOX, new Object[]{pulsate}),
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(speed - 1) / 149F}),
            new AnimationOption("scale", "Scale", AnimationOption.OptionType.SLIDER, new Object[]{(29 - (scale - 1)) / 29F}),
        };
    }

    @Override
    public void optionChanged(String name, String value) {
        switch (name) {
            case "color1":
                color1 = Util.stringToColor(value);
                break;
            case "color2":
                color2 = Util.stringToColor(value);
                break;
            case "colormode":
                colorMode = Integer.parseInt(value);
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
