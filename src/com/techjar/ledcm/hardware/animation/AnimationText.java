
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.hardware.LEDCharacter;
import com.techjar.ledcm.hardware.LEDUtil;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector3;
import java.util.Random;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Techjar
 */
public class AnimationText extends Animation {
    private Random random = new Random();
    private Timer timer = new Timer();
    private String text = "LED Cube";
    private LEDCharacter[] characters;
    private int speed = 5;
    private int colorMode = 0;
    private int animMode = 0;
    private int hSpace = 1;
    private int scrollOffset = 0;

    public AnimationText() {
        super();
    }

    @Override
    public String getName() {
        return "Text";
    }

    @Override
    public synchronized void refresh() {
        if (ticks % speed == 0) scrollOffset--;
        LEDUtil.clear(ledManager);
        for (int i = 0; i < characters.length; i++) {
            final int index = i;
            final LEDCharacter ch = characters[i];
            ch.clearTransform();
            if (animMode == 0) {
                ch.applyTransform(new LEDCharacter.Transformer() {
                    @Override
                    public Vector3 transform(Vector3 vector) {
                        return vector.add(new Vector3(0, 0, (ch.getFontSize() + hSpace) * index));
                    }
                });
            }
            if (animMode == 0) {
                ch.applyTransform(new LEDCharacter.Transformer() {
                    @Override
                    public Vector3 transform(Vector3 vector) {
                        vector = vector.add(new Vector3(0, 0, (dimension.z - 1) + dimension.x)).add(new Vector3(0, 0, scrollOffset));
                        if (vector.getZ() > dimension.z - 1) {
                            int z = (int)vector.getZ() - (dimension.z - 1);
                            vector.setZ(dimension.z - 1);
                            vector.setX(z);
                        } else if (vector.getZ() < 0) {
                            int z = -(int)vector.getZ();
                            vector.setZ(0);
                            vector.setX(z);
                            if (index == characters.length - 1) {
                                if (vector.getX() > dimension.x * 2) {
                                    scrollOffset = 0;
                                }
                            }
                        }
                        return vector;
                    }
                });
                ch.draw(ledManager, ReadableColor.WHITE);
            }

        }
    }

    @Override
    public synchronized void reset() {
        timer.restart();
        scrollOffset = 0;
        characters = new LEDCharacter[text.length()];
        for (int i = 0; i < characters.length; i++) {
            characters[i] = LEDCharacter.getChar(text.charAt(i));
            characters[i].setThickness(getThickness());
        }
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("colormode", "Color", AnimationOption.OptionType.COMBOBOX, new Object[]{colorMode, 0, "Picker"}),
            new AnimationOption("animmode", "Animation", AnimationOption.OptionType.COMBOBOX, new Object[]{animMode, 0, "Depth Scroll", 1, "Scroll"}),
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(19 - (speed - 1)) / 19F, 1F / 19F}),
            new AnimationOption("text", "Text", AnimationOption.OptionType.TEXT, new Object[]{text, "^[\u0020-\u007E]*$", 100}),
        };
    }

    @Override
    public synchronized void optionChanged(String name, String value) {
        switch (name) {
            case "colormode":
                colorMode = Integer.parseInt(value);
                break;
            case "animmode":
                animMode = Integer.parseInt(value);
                reset();
                break;
            case "speed":
                speed = 1 + (19 - Math.round(19 * Float.parseFloat(value)));
                break;
            case "text":
                text = value;
                reset();
                break;
        }
    }

    private int getThickness() {
        return 1;
    }
}
