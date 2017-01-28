
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationPingPong extends Animation {
    private int position1;
    private int position2;
    private float hue = 0;
    private boolean direction;
    private boolean twoballs;
    private int dotSize = 5;
    private final int maxDotSize;
    private int speed = 1;
    private int rainbow = 0;

    public AnimationPingPong() {
        super();
        maxDotSize = (dimension.x / 4) - 1;
    }

    @Override
    public String getName() {
        return "Ping Pong";
    }

    @Override
    public synchronized void refresh() {
        if (ticks % speed == 0) {
            if (twoballs) {
                int half = dimension.x / 2 - 1;
                Color color1 = new Color(LEDCubeManager.getPaintColor()), color2 = new Color(LEDCubeManager.getPaintColor());
                if (rainbow == 2) {
                    color1.fromHSB(hue, 1, 1);
                    color2.fromHSB(hue, 1, 1);
                }
                if (direction) {
                    if (rainbow == 1) {
                        color1.fromHSB(position1 / (float)half, 1, 1);
                        color2.fromHSB(1 - (((position2 - (dotSize - 1)) - (half + 1)) / (float)half), 1, 1);
                    }
                    position1++;
                    position2--;
                    ledManager.setLEDColor(position1, 0, 0, color1);
                    ledManager.setLEDColor(position1 - dotSize, 0, 0, new Color());
                    ledManager.setLEDColor(position2 + 1, 0, 0, new Color());
                    ledManager.setLEDColor(position2 - (dotSize - 1), 0, 0, color2);
                } else {
                    if (rainbow == 1) {
                        color1.fromHSB((position1 - (dotSize - 1)) / (float)half, 1, 1);
                        color2.fromHSB(1 - ((position2 - (half + 1)) / (float)half), 1, 1);
                    }
                    position1--;
                    position2++;
                    ledManager.setLEDColor(position1 + 1, 0, 0, new Color());
                    ledManager.setLEDColor(position1 - (dotSize - 1), 0, 0, color1);
                    ledManager.setLEDColor(position2, 0, 0, color2);
                    ledManager.setLEDColor(position2 - dotSize, 0, 0, new Color());
                }
                if (position1 == dotSize - 1 || position1 == half) direction = !direction;
            }
            else {
                Color color = new Color(LEDCubeManager.getPaintColor());
                if (rainbow == 2) color.fromHSB(hue, 1, 1);
                if (direction) {
                    if (rainbow == 1) color.fromHSB(position1 / (float)(dimension.x - 1), 1, 1);
                    position1++;
                    ledManager.setLEDColor(position1, 0, 0, color);
                    ledManager.setLEDColor(position1 - dotSize, 0, 0, new Color());
                }
                else {
                    if (rainbow == 1) color.fromHSB((position1 - (dotSize - 1)) / (float)(dimension.x - 1), 1, 1);
                    position1--;
                    ledManager.setLEDColor(position1 + 1, 0, 0, new Color());
                    ledManager.setLEDColor(position1 - (dotSize - 1), 0, 0, color);
                }
                if (position1 == dotSize - 1 || position1 == dimension.x - 1) direction = !direction;
            }
            hue += 1F / 360F;
        }
    }

    @Override
    public synchronized void reset() {
        position1 = dotSize - 1;
        position2 = dimension.x - 1;
        direction = true;
        //ledManager.setLEDColor(position1, 0, 0, LEDCubeManager.getPaintColor());
        //if (twoballs) ledManager.setLEDColor(position2, 0, 0, LEDCubeManager.getPaintColor());
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(15 - (speed - 1)) / 15F, 1F / 15F}),
            new AnimationOption("rainbow", "Rainbow", AnimationOption.OptionType.COMBOBUTTON, new Object[]{rainbow, 0, "Off", 1, "Static", 2, "Cycle"}),
            new AnimationOption("twoballs", "Two Dots", AnimationOption.OptionType.CHECKBOX, new Object[]{twoballs}),
            new AnimationOption("dotSize", "Dot Size", AnimationOption.OptionType.SLIDER, new Object[]{(dotSize - 1F) / maxDotSize, 1F / maxDotSize}),
        };
    }

    @Override
    public synchronized void optionChanged(String name, String value) {
        switch (name) {
            case "speed":
                speed = 1 + Math.round(15 * (1 - Float.parseFloat(value)));
                break;
            case "rainbow":
                rainbow = Integer.parseInt(value);
                break;
            case "twoballs":
                twoballs = Boolean.parseBoolean(value);
                reset();
                break;
            case "dotSize":
                dotSize = 1 + Math.round(maxDotSize * Float.parseFloat(value));
                reset();
                break;
        }
    }
}
