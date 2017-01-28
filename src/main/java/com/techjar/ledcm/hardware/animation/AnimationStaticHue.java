
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.LEDUtil;
import com.techjar.ledcm.util.MathHelper;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class AnimationStaticHue extends Animation {
    private float brightness = 1;
    private float shift = 0;
    private boolean reverse;

    public AnimationStaticHue() {
        super();
    }

    @Override
    public String getName() {
        return "Hue Fill";
    }

    @Override
    public void refresh() {
        for (int x = 0; x < dimension.x; x++) {
            for (int y = 0; y < dimension.y; y++) {
                for (int z = 0; z < dimension.z; z++) {
                    Color color = new Color();
                    color.fromHSB((reverse ? (1 - (x / (dimension.x - 1F))) + shift : (x / (dimension.x - 1F)) + (1 - shift)) % (dimension.x - 1), 1, brightness);
                    ledManager.setLEDColor(x, y, z, color);
                }
            }
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("brightness", "Brightness", AnimationOption.OptionType.SLIDER, new Object[]{brightness}),
            new AnimationOption("shift", "Shift", AnimationOption.OptionType.SLIDER, new Object[]{shift}),
            new AnimationOption("reverse", "Reverse", AnimationOption.OptionType.CHECKBOX, new Object[]{reverse}),
        };
    }

    @Override
    public void optionChanged(String name, String value) {
        switch (name) {
            case "brightness":
                brightness = Float.parseFloat(value);
                break;
            case "shift":
                shift = Float.parseFloat(value);
                break;
            case "reverse":
                reverse = Boolean.parseBoolean(value);
                break;
        }
    }
}
