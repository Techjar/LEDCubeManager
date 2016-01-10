
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.LEDCharacter;
import com.techjar.ledcm.hardware.LEDUtil;
import com.techjar.ledcm.util.MathHelper;
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
    private String text = "LED Cube";
    private LEDCharacter[] characters;
    private int speed = 4;
    private int colorMode = 0;
    private int animMode = 0;
    private int hSpace = 1;
    private int scrollOffset = 0;
    private int currentChar = 0;
    private float rotation;
    private Color topColor = new Color(0, 255, 255);
    private Color bottomColor = new Color(255, 255, 0);
    private boolean finished = false;

    public AnimationText() {
        super();
    }

    @Override
    public String getName() {
        return "Text";
    }

    @Override
    public synchronized void refresh() {
        if (ticks % speed == 0) {
            LEDUtil.clear(ledManager);
            if (animMode == 0 || animMode == 1 || animMode == 2) {
                scrollOffset--;
                for (int i = 0; i < characters.length; i++) {
                    final int index = i;
                    final LEDCharacter ch = characters[i];
                    ch.clearTransform();
                    switch (animMode) {
                        case 0:
                        case 1:
                            ch.applyTransform(new LEDCharacter.Transformer() {
                                @Override
                                public Vector3 transform(Vector3 vector) {
                                    return vector.add(new Vector3(0, 0, (ch.getFontSize() + hSpace) * index));
                                }
                            });
                            switch (animMode) {
                                case 0:
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
                                                        finished = true;
                                                    }
                                                }
                                            }
                                            return vector;
                                        }
                                    });
                                    break;
                                case 1:
                                    ch.applyTransform(new LEDCharacter.Transformer() {
                                        @Override
                                        public Vector3 transform(Vector3 vector) {
                                            vector = vector.add(new Vector3(0, 0, dimension.z)).add(new Vector3(0, 0, scrollOffset));
                                            if (index == characters.length - 1) {
                                                if (vector.getZ() < -dimension.z) {
                                                    scrollOffset = 0;
                                                    finished = true;
                                                }
                                            }
                                            return vector;
                                        }
                                    });
                                    break;
                            }
                            break;
                        case 2:
                            ch.applyTransform(new LEDCharacter.Transformer() {
                                @Override
                                public Vector3 transform(Vector3 vector) {
                                    vector = vector.add(new Vector3((dimension.x + (ch.getThickness() - 1)) * index + dimension.x + ch.getThickness() + scrollOffset, 0, 0));
                                    if (index == characters.length - 1) {
                                        if (vector.getX() < -ch.getThickness()) {
                                            scrollOffset = 0;
                                            finished = true;
                                        }
                                    }
                                    return vector;
                                }
                            });
                            break;
                    }
                    drawCharacter(ch);
                }
            } else if (characters.length > 0) {
                final LEDCharacter ch = characters[currentChar];
                ch.clearTransform();
                if (scrollOffset < -dimension.x - ch.getThickness()) {
                    scrollOffset = 0;
                    rotation = 0;
                    currentChar++;
                    if (currentChar >= characters.length) {
                        currentChar = 0;
                        finished = true;
                    }
                } else if (scrollOffset < -(dimension.x / 2) && rotation < 1) {
                    final Matrix4f matrix = new Matrix4f();
                    matrix.translate(new Vector3f((ch.getThickness() - 1) / 2F, 0, (ch.getFontSize() - 1) / 2F));
                    matrix.rotate((float)Math.PI * 2 * rotation, new Vector3f(0, -1, 0));
                    matrix.translate(new Vector3f(-(ch.getThickness() - 1) / 2F, 0, -(ch.getFontSize() - 1) / 2F));
                    ch.applyTransform(new LEDCharacter.Transformer() {
                        @Override
                        public Vector3 transform(Vector3 vector) {
                            return Util.transformVector(vector, matrix, true);
                        }
                    });
                    rotation += 0.05F;
                } else {
                    scrollOffset--;
                }
                ch.applyTransform(new LEDCharacter.Transformer() {
                    @Override
                    public Vector3 transform(Vector3 vector) {
                        return vector.add(new Vector3(dimension.x + scrollOffset, 0, 0));
                    }
                });
                drawCharacter(ch);
            }
        }
    }

    @Override
    public synchronized void reset() {
        finished = false;
        scrollOffset = 0;
        currentChar = 0;
        rotation = 0;
        characters = new LEDCharacter[text.length()];
        for (int i = 0; i < characters.length; i++) {
            characters[i] = LEDCharacter.getChar(text.charAt(i));
            characters[i].setThickness(getThickness());
        }
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("topcolor", "Grad. Top", AnimationOption.OptionType.COLORPICKER, new Object[]{topColor}),
            new AnimationOption("bottomcolor", "Grad. Btm.", AnimationOption.OptionType.COLORPICKER, new Object[]{bottomColor}),
            new AnimationOption("colormode", "Color", AnimationOption.OptionType.COMBOBOX, new Object[]{colorMode, 0, "Picker", 1, "Rainbow", 2, "Gradient"}),
            new AnimationOption("animmode", "Animation", AnimationOption.OptionType.COMBOBOX, new Object[]{animMode, 0, "Depth Scroll", 1, "Scroll", 2, "Fly Through", 3, "Fly In, Spin, Fly Out"}),
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(19 - (speed - 1)) / 19F, 1F / 19F}),
            new AnimationOption("text", "Text", AnimationOption.OptionType.TEXT, new Object[]{text, "^[\u0020-\u007E]*$", 100}),
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
        if (animMode == 2 || animMode == 3) return 2;
        return 1;
    }

    private void drawCharacter(final LEDCharacter ch) {
        switch (colorMode) {
            case 0:
                ch.draw(ledManager, LEDCubeManager.getPaintColor());
                break;
            case 1:
                ch.draw(ledManager, new LEDCharacter.Colorizer() {
                    @Override
                    public ReadableColor getColorAt(Vector3 vector) {
                        Color color = new Color();
                        color.fromHSB((vector.getY() / (ch.getFontSize() - 1)) * (300F / 360F), 1, 1);
                        return color;
                    }
                });
                break;
            case 2:
                ch.draw(ledManager, new LEDCharacter.Colorizer() {
                    @Override
                    public ReadableColor getColorAt(Vector3 vector) {
                        return MathHelper.lerpLab(bottomColor, topColor, vector.getY() / (ch.getFontSize() - 1));
                    }
                });
                break;
        }
    }
}
