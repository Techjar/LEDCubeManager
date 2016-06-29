
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
public class AnimationStacker extends Animation {
    private Random random = new Random();
    private long layers;
    private long curLayer;
    private final long topLayer;
    private final long allLayers;
    private int curLayerNum;
    private int speed = 3;
    private int colorMode = 0;
    private Color color1 = new Color(255, 0, 0);
    private Color color2 = new Color(255, 255, 255);
    private int colorSeed;

    public AnimationStacker() {
        super();
        topLayer = 1L << (dimension.y - 1);
        allLayers = (long)Math.pow(2, dimension.y) - 1;
    }

    @Override
    public String getName() {
        return "Stacker";
    }

    @Override
    public synchronized void refresh() {
        if (ticks % speed == 0) {
            if (layers != allLayers) {
                if (curLayer == 0) {
                    curLayer = topLayer;
                } else if ((layers | (curLayer >>> 1)) == layers) {
                    layers |= curLayer;
                    curLayer = 0;
                    curLayerNum++;
                } else {
                    curLayer >>>= 1;
                }
            }
            for (int y = 0; y < dimension.y; y++) {
                Color color = checkBit(layers, y) ? getColor(y) : checkBit(curLayer, y) ? getColor(curLayerNum) : new Color();
                for (int x = 0; x < dimension.x; x++) {
                    for (int z = 0; z < dimension.z; z++) {
                        ledManager.setLEDColor(x, y, z, color);
                    }
                }
            }
        }
    }

    @Override
    public synchronized void reset() {
        colorSeed = random.nextInt();
        layers = 0;
        curLayerNum = 0;
    }

    @Override
    public boolean isFinished() {
        return layers == allLayers;
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("color1", "Color 1", AnimationOption.OptionType.COLORPICKER, new Object[]{color1}),
            new AnimationOption("color2", "Color 2", AnimationOption.OptionType.COLORPICKER, new Object[]{color2}),
            new AnimationOption("colormode", "Color", AnimationOption.OptionType.COMBOBOX, new Object[]{colorMode, 0, "Picker", 1, "Alternate", 2, "Rainbow", 3, "Random"}),
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(19 - (speed - 1)) / 19F, 1F / 19F}),
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
            case "colormode":
                colorMode = Integer.parseInt(value);
                break;
            case "speed":
                speed = 1 + (19 - Math.round(19 * Float.parseFloat(value)));
                break;
        }
    }

    private Color getColor(int layer) {
        Random rand = new Random(colorSeed + layer);
        rand.nextInt(); // Throw away crappy first result
        if (colorMode == 0) {
            return LEDCubeManager.getPaintColor();
        } else if (colorMode == 1) {
            if (layer % 2 == 0) return color1;
            return color2;
        } else if (colorMode == 2) {
            Color color = new Color();
            color.fromHSB((layer / (dimension.y - 1F)) * (300F / 360F), 1, 1);
            return color;
        }
        return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }

    private boolean checkBit(long number, int bit) {
        return (number & (1L << bit)) != 0;
    }
}
