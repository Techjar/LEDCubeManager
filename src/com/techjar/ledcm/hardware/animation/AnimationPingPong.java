
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
    private boolean direction;
    private boolean twoballs;
    private int speed = 1;

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
            ledManager.setLEDColor(direction ? position1++ : position1--, 0, 0, new Color());
            ledManager.setLEDColor(position1, 0, 0, LEDCubeManager.getPaintColor());
            if (twoballs) {
                ledManager.setLEDColor(direction ? position2-- : position2++, 0, 0, new Color());
                ledManager.setLEDColor(position2, 0, 0, LEDCubeManager.getPaintColor());
                if (position1 == 0 || position1 == dimension.x / 2 - 1) direction = !direction;
            }
            else {
                if (position1 == 0 || position1 == dimension.x - 1) direction = !direction;
            }
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
            new AnimationOption("twoballs", "Two Balls", AnimationOption.OptionType.CHECKBOX, new Object[]{twoballs}),
        };
    }

    @Override
    public void optionChanged(String name, String value) {
        switch (name) {
            case "speed":
                speed = 1 + Math.round(15 * (1 - Float.parseFloat(value)));
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
