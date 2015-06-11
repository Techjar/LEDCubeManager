
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.LEDUtil;
import com.techjar.ledcm.util.Direction;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector3;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

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
    private int speed = 1;
    private int rainbow = 0;

    public AnimationPingPong() {
        super();
    }

    @Override
    public String getName() {
        return "Ping Pong";
    }

    @Override
    public void refresh() {
        if (ticks % speed == 0) {
            if (twoballs) {
                int half = dimension.x / 2 - 1;
                Color color1 = new Color(), color2 = new Color();
                if (rainbow == 1) {
                    color1.fromHSB(position1 / (float)half, 1, 1);
                    color2.fromHSB(1 - ((position2 - (half + 1)) / (float)half), 1, 1);
                } else if (rainbow == 2) {
                    color1.fromHSB(hue, 1, 1);
                    color2.fromHSB(hue, 1, 1);
                }
                else color1 = color2 = LEDCubeManager.getPaintColor();
                ledManager.setLEDColor(direction ? position1++ : position1--, 0, 0, new Color());
                ledManager.setLEDColor(position1, 0, 0, color1);
                ledManager.setLEDColor(direction ? position2-- : position2++, 0, 0, new Color());
                ledManager.setLEDColor(position2, 0, 0, color2);
                if (position1 == 0 || position1 == half) direction = !direction;
            }
            else {
                Color color = new Color();
                if (rainbow == 1) color.fromHSB(position1 / (float)(dimension.x - 1), 1, 1);
                else if (rainbow == 2) color.fromHSB(hue, 1, 1);
                else color = LEDCubeManager.getPaintColor();
                ledManager.setLEDColor(direction ? position1++ : position1--, 0, 0, new Color());
                ledManager.setLEDColor(position1, 0, 0, color);
                if (position1 == 0 || position1 == dimension.x - 1) direction = !direction;
            }
            hue += 1F / 360F;
        }
    }

    @Override
    public void reset() {
        position1 = 0;
        position2 = dimension.x - 1;
        direction = true;
        ledManager.setLEDColor(position1, 0, 0, LEDCubeManager.getPaintColor());
        if (twoballs) ledManager.setLEDColor(position2, 0, 0, LEDCubeManager.getPaintColor());
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(15 - (speed - 1)) / 15F, 1F / 15F}),
            new AnimationOption("rainbow", "Rainbow", AnimationOption.OptionType.COMBOBUTTON, new Object[]{rainbow, 0, "Off", 1, "Static", 2, "Cycle"}),
            new AnimationOption("twoballs", "Two Dots", AnimationOption.OptionType.CHECKBOX, new Object[]{twoballs}),
        };
    }

    @Override
    public void optionChanged(String name, String value) {
        switch (name) {
            case "speed":
                speed = 1 + Math.round(15 * (1 - Float.parseFloat(value)));
                break;
            case "rainbow":
                rainbow = Integer.parseInt(value);
                break;
            case "twoballs":
                position1 = 0;
                position2 = dimension.x - 1;
                direction = true;
                twoballs = Boolean.parseBoolean(value);
                LEDUtil.clear(ledManager);
                break;
        }
    }
}
