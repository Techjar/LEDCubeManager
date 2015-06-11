
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import java.util.Random;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class AnimationTicker extends Animation {
    private Random random = new Random();
    private float hue = 0;
    private int speed = 2;
    private int density = 10;
    private int rainbow = 0;

    public AnimationTicker() {
        super();
    }

    @Override
    public String getName() {
        return "Ticker";
    }

    @Override
    public void refresh() {
        if (ticks % speed == 0) {
            for (int x = dimension.x - 2; x >= 0; x--) {
                Color color = new Color();
                if (rainbow == 1) {
                    float[] hsb = ledManager.getLEDColor(x, 0, 0).toHSB(null);
                    color.fromHSB((x + 1) / (float)(dimension.x - 1), 1, hsb[2]);
                } else color = ledManager.getLEDColor(x, 0, 0);
                ledManager.setLEDColor(x + 1, 0, 0, color);
                ledManager.setLEDColor(x, 0, 0, new Color());
            }
            if (random.nextInt(density) == 0) {
                Color color = new Color();
                //if (rainbow) color.fromHSB(hue % 360F, 1, 1);
                //else color = LEDCubeManager.getPaintColor();
                if (rainbow == 2) color.fromHSB(hue % 360F, 1, 1);
                else if (rainbow == 1) color.fromHSB(0, 1, 1);
                else color = LEDCubeManager.getPaintColor();
                ledManager.setLEDColor(0, 0, 0, color);
                hue += 1F / 360F;
            }
        }
    }

    @Override
    public void reset() {
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(15 - (speed - 1)) / 15F, 1F / 15F}),
            new AnimationOption("density", "Density", AnimationOption.OptionType.SLIDER, new Object[]{(30 - (density - 1)) / 30F, 1F / 30F}),
            new AnimationOption("rainbow", "Rainbow", AnimationOption.OptionType.COMBOBUTTON, new Object[]{rainbow, 0, "Off", 1, "Static", 2, "Cycle"}),
        };
    }

    @Override
    public void optionChanged(String name, String value) {
        switch (name) {
            case "speed":
                speed = 1 + Math.round(15 * (1 - Float.parseFloat(value)));
                break;
            case "density":
                density = 2 + Math.round(30 * (1 - Float.parseFloat(value)));
                break;
            case "rainbow":
                rainbow = Integer.parseInt(value);
                break;
        }
    }
}
