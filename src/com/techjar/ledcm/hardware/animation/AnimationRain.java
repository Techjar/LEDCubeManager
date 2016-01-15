
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.util.BitSetUtil;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import java.util.BitSet;
import java.util.Random;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationRain extends Animation {
    private BitSet[] drops;
    private float[] floor;
    private int[] lightning;
    private boolean[] states;
    private float[] floorStates;
    private Random random = new Random();
    private Color topColor = new Color(0, 0, 255);
    private Color bottomColor = new Color(255, 0, 255);
    private Color lightningColor = new Color(225, 255, 255);
    private int dropFreq = 20;
    private int lightningFreq = 2500;
    private boolean lightningEnable = true;
    private boolean floorCollect = true;
    private int colorMode = 0;
    private int speed = 3;

    public AnimationRain() {
        super();
        drops = new BitSet[dimension.x * dimension.z];
    }

    @Override
    public String getName() {
        return "Rain";
    }

    @Override
    public synchronized void refresh() {
        if (ticks % speed == 0) {
            for (int x = 0; x < dimension.x; x++) {
                for (int z = 0; z < dimension.z; z++) {
                    int index = x + (z * dimension.x);
                    if (lightning[index] > 0) {
                        lightning[index]--;
                        if (lightning[index] == 0) {
                            for (int y = 0; y < dimension.y; y++) {
                                Color color = MathHelper.lerpLab(bottomColor, topColor, y / (dimension.y - 1F));
                                if (y == 0) ledManager.setLEDColor(x, y, z, floor[index] > 0 ? Util.multiplyColor(color, floor[index]) : new Color());
                                else ledManager.setLEDColor(x, y, z, drops[index].get(y) ? color : new Color());
                            }
                        }
                    } else if (lightningEnable && random.nextInt(lightningFreq) == 0) {
                        lightning[index] = random.nextInt(30) + 1;
                    }
                    boolean lightningCheck = checkLightning(lightning[index]);
                    if (lightning[index] > 0) {
                        for (int y = 0; y < dimension.y; y++) {
                            ledManager.setLEDColor(x, y, z, lightningCheck ? lightningColor : new Color());
                        }
                    }
                    BitSetUtil.shiftRight(drops[index], 1);
                    for (int y = dimension.y - 1; y >= 0; y--) {
                        int stateIndex = index + y * dimension.x * dimension.z;
                        Color color = new Color();
                        if (colorMode == 0) color = MathHelper.lerpLab(bottomColor, topColor, y / (dimension.y - 1F));
                        else if (colorMode == 1) color.fromHSB((y / (dimension.y - 1F)) * (300F / 360F), 1, 1);
                        else if (colorMode == 2) color.fromHSB((1 - (y / (dimension.y - 1F))) * (300F / 360F), 1, 1);
                        if (y == dimension.y - 1) {
                            if (random.nextInt(dropFreq) == 0) drops[index].set(dimension.y - 1);
                        } else if (y == 0) {
                            floor[index] = Math.max(floor[index] - (random.nextFloat() * 0.1F), 0);
                            if (floorCollect && drops[index].get(y)) {
                                drops[index].clear(0);
                                floor[index] = 1;
                            }
                        }
                        if ((floorCollect && y == 0) || (!floorCollect && y == 0 && floor[index] != floorStates[stateIndex])) {
                            if (floor[index] != floorStates[stateIndex]) {
                                floorStates[stateIndex] = floor[index];
                                if (!lightningCheck) ledManager.setLEDColor(x, y, z, floorStates[stateIndex] > 0 ? Util.multiplyColor(color, floorStates[stateIndex]) : new Color());
                            }
                        } else {
                            boolean bit = drops[index].get(y);
                            if (bit != states[stateIndex]) {
                                states[stateIndex] = bit;
                                if (!lightningCheck) ledManager.setLEDColor(x, y, z, states[stateIndex] ? color : new Color());
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public synchronized void reset() {
        for (int i = 0; i < drops.length; i++) {
            drops[i] = new BitSet();
        }
        floor = new float[dimension.x * dimension.z];
        lightning = new int[dimension.x * dimension.z];
        states = new boolean[dimension.x * dimension.y * dimension.z];
        floorStates = new float[dimension.x * dimension.z];
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("topcolor", "Top", AnimationOption.OptionType.COLORPICKER, new Object[]{topColor}),
            new AnimationOption("bottomcolor", "Bottom", AnimationOption.OptionType.COLORPICKER, new Object[]{bottomColor}),
            new AnimationOption("lightningcolor", "Lightning", AnimationOption.OptionType.COLORPICKER, new Object[]{lightningColor}),
            new AnimationOption("colormode", "Color", AnimationOption.OptionType.COMBOBOX, new Object[]{colorMode, 0, "Gradient", 1, "Rainbow Up", 2, "Rainbow Down"}),
            new AnimationOption("dropfreq", "Drop Freq", AnimationOption.OptionType.SLIDER, new Object[]{(98 - (dropFreq - 2)) / 98F, 1F / 98F, false}),
            new AnimationOption("lightningfreq", "L. Freq", AnimationOption.OptionType.SLIDER, new Object[]{(29950 - (lightningFreq - 50)) / 29950F}),
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(19 - (speed - 1)) / 19F, 1F / 19F}),
            new AnimationOption("lightningenable", "L. Enable", AnimationOption.OptionType.CHECKBOX, new Object[]{lightningEnable}),
            new AnimationOption("floorcollect", "Floor Coll.", AnimationOption.OptionType.CHECKBOX, new Object[]{floorCollect}),
        };
    }

    @Override
    public synchronized void optionChanged(String name, String value) {
        switch (name) {
            case "topcolor":
                topColor = Util.stringToColor(value);
                break;
            case "bottomcolor":
                bottomColor = Util.stringToColor(value);
                break;
            case "lightningcolor":
                lightningColor = Util.stringToColor(value);
                break;
            case "dropfreq":
                dropFreq = 2 + (98 - Math.round(98 * Float.parseFloat(value)));
                break;
            case "lightningfreq":
                lightningFreq = 50 + (29950 - Math.round(29950 * Float.parseFloat(value)));
                break;
            case "lightningenable":
                lightningEnable = Boolean.parseBoolean(value);
                break;
            case "floorcollect":
                floorCollect = Boolean.parseBoolean(value);
                break;
            case "colormode":
                colorMode = Integer.parseInt(value);
                break;
            case "speed":
                speed = 1 + (19 - Math.round(19 * Float.parseFloat(value)));
                break;
        }
    }

    private boolean checkBit(long number, int bit) {
        return (number & (1L << bit)) != 0;
    }

    private boolean checkLightning(int value) {
        return value > 0 ? random.nextInt(value) == 0 : false;
    }
}
