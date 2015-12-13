
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.LEDUtil;
import com.techjar.ledcm.util.Util;
import java.util.Random;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class AnimationSlidingPanels  extends Animation {
    private Random random = new Random();
    private int colorSeed;
    private int panels = 0b111;
    private int[] panelPos;
    private boolean[] panelDirection;
    private int colorMode = 1;
    private int speed = 4;

    public AnimationSlidingPanels() {
        super();
    }

    @Override
    public String getName() {
        return "Sliding Panels";
    }

    @Override
    public void refresh() {
        if (ticks % speed == 0) {
            LEDUtil.clear(ledManager);
            for (int i = 0; i < 3; i++) {
                if (((panels >> i) & 1) != 0) {
                    drawPanel(i, panelPos[i], getPanelColor(i));
                }
                if (panelDirection[i]) panelPos[i]--;
                else panelPos[i]++;
                if (panelPos[i] == 0) panelDirection[i] = false;
                if (panelPos[i] == (i == 0 ? dimension.x : (i == 1 ? dimension.y : dimension.z)) - 1) panelDirection[i] = true;
            }
        }
    }

    @Override
    public void reset() {
        colorSeed = random.nextInt();
        panelPos = new int[3];
        panelDirection = new boolean[3];
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("xpanel", "X Panel", AnimationOption.OptionType.CHECKBOX, new Object[]{(panels & 1) != 0}),
            new AnimationOption("ypanel", "Y Panel", AnimationOption.OptionType.CHECKBOX, new Object[]{((panels >> 1) & 1) != 0}),
            new AnimationOption("zpanel", "Z Panel", AnimationOption.OptionType.CHECKBOX, new Object[]{((panels >> 2) & 1) != 0}),
            new AnimationOption("colormode", "Color", AnimationOption.OptionType.COMBOBOX, new Object[]{colorMode, 0, "Random", 1, "RGB", 2, "Picker"}),
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(19 - (speed - 1)) / 19F, 1F / 19F}),
        };
    }

    @Override
    public synchronized void optionChanged(String name, String value) {
        switch (name) {
            case "xpanel":
                if (Boolean.parseBoolean(value)) panels |= 0b001;
                else panels &= 0b110;
                break;
            case "ypanel":
                if (Boolean.parseBoolean(value)) panels |= 0b010;
                else panels &= 0b101;
                break;
            case "zpanel":
                if (Boolean.parseBoolean(value)) panels |= 0b100;
                else panels &= 0b011;
                break;
            case "colormode":
                colorMode = Integer.parseInt(value);
                break;
            case "speed":
                speed = 1 + (19 - Math.round(19 * Float.parseFloat(value)));
                break;
        }
    }

    private Color getPanelColor(int index) {
        Random rand = new Random(colorSeed + index);
        rand.nextInt(); // Throw away crappy first result
        if (colorMode == 1) {
            switch (index) {
                case 0: return new Color(255, 0, 0);
                case 1: return new Color(0, 255, 0);
                case 2: return new Color(0, 0, 255);
            }
        }
        if (colorMode == 2) {
            return LEDCubeManager.getPaintColor();
        }
        return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }

    private void drawPanel(int index, int pos, Color color) {
        for (int x = index == 0 ? pos : 0; x < (index == 0 ? pos + 1 : dimension.x); x++) {
            for (int y = index == 1 ? pos : 0; y < (index == 1 ? pos + 1 : dimension.y); y++) {
                for (int z = index == 2 ? pos : 0; z < (index == 2 ? pos + 1 : dimension.z); z++) {
                    ledManager.setLEDColor(x, y, z, Util.addColors(ledManager.getLEDColor(x, y, z), color));
                }
            }
        }
    }
}
