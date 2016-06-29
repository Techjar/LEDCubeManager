
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector2;
import org.lwjgl.util.Color;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Techjar
 */
public class AnimationSine extends Animation {
    private Timer timer = new Timer();
    private int colorMode = 0;
    private float speed = 5;
    private int type = 0;

    public AnimationSine() {
        super();
    }

    @Override
    public String getName() {
        return "Sine Wave";
    }

    @Override
    public synchronized void refresh() {
        if (type == 0) {
            for (int z = 0; z < dimension.z; z++) {
                double value = Math.sin(timer.getSeconds() * speed + ((dimension.z - 1 - z) / (dimension.z - 1F)) * 3) * 0.5 + 0.5;
                int onY = (int)Math.round(value * (dimension.y - 1));
                for (int x = 0; x < dimension.x; x++) {
                    for (int y = 0; y < dimension.y; y++) {
                        ledManager.setLEDColor(x, y, z, y == onY ? getColor(y) : new Color());
                    }
                }
            }
        } else if (type == 1 || type == 2) {
            for (int z = 0; z < dimension.z; z++) {
                double input = timer.getSeconds() * speed + ((dimension.z - 1 - z) / (dimension.z - 1F)) * 3;
                double value = Math.sin(input) * 0.5 + 0.5;
                double value2 = (type == 1 ? Math.cos(input) : Math.sin(input + Math.PI)) * 0.5 + 0.5;
                int onY = (int)Math.round(value * (dimension.y - 1));
                int onY2 = (int)Math.round(value2 * (dimension.y - 1));
                for (int x = 0; x < dimension.x; x++) {
                    for (int y = 0; y < dimension.y; y++) {
                        ledManager.setLEDColor(x, y, z, y == (x > (dimension.x / 2 - 1) ? onY2 : onY) ? getColor(y) : new Color());
                    }
                }
            }
        } else if (type == 3) {
            Vector3f center = LEDCubeManager.getLEDCube().getCenterPoint();
            float maxDist = new Vector2(center.x, center.z).distance(new Vector2(0, 0));
            for (int z = 0; z < dimension.z; z++) {
                for (int x = 0; x < dimension.x; x++) {
                    float dist = new Vector2(x, z).distance(new Vector2(center.x, center.z));
                    double value = Math.sin(timer.getSeconds() * speed + (dist / maxDist) * 3) * 0.5 + 0.5;
                    int onY = (int)Math.round(value * (dimension.y - 1));
                    for (int y = 0; y < dimension.y; y++) {
                        ledManager.setLEDColor(x, y, z, y == onY ? getColor(y) : new Color());
                    }
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
            new AnimationOption("type", "Type", AnimationOption.OptionType.COMBOBOX, new Object[]{colorMode, 0, "Standard", 1, "Dual (Cosine)", 2, "Dual (Inverted)", 3, "Circle"}),
            new AnimationOption("colormode", "Color", AnimationOption.OptionType.COMBOBOX, new Object[]{colorMode, 0, "Picker", 1, "Rainbow"}),
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(speed - 1) / 19F}),
        };
    }

    @Override
    public synchronized void optionChanged(String name, String value) {
        switch (name) {
            case "type":
                type = Integer.parseInt(value);
                break;
            case "colormode":
                colorMode = Integer.parseInt(value);
                break;
            case "speed":
                speed = 1 + (19 * Float.parseFloat(value));
                break;
        }
    }

    private Color getColor(int y) {
        if (colorMode == 1) {
            Color color = new Color();
            color.fromHSB((y / (dimension.y - 1F)) * (300F / 360F), 1, 1);
            return color;
        }
        return LEDCubeManager.getPaintColor();
    }
}
