
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
public class AnimationScrollers extends Animation {
    private Random random = new Random();
    private int[] dots;
    private boolean[] directions;
    private boolean[] moving;
    private int posMode = 0;
    private int moveMode = 0;
    private int speed = 3;

    public AnimationScrollers() {
        super();
        dots = new int[dimension.x * dimension.z];
        directions = new boolean[dimension.x * dimension.z];
        moving = new boolean[dimension.x * dimension.z];
    }

    @Override
    public String getName() {
        return "Scrollers";
    }

    @Override
    public synchronized void refresh() {
        if (ticks % speed == 0) {
            for (int x = 0; x < dimension.x; x++) {
                for (int z = 0; z < dimension.z; z++) {
                    int index = z + (x * dimension.y);
                    long dot = dots[index];
                    boolean doMove = false;
                    if (moveMode == 0) {
                        doMove = random.nextInt(500 * (dots.length / 64)) == 0;
                    } else if (moveMode == 1) {
                        doMove = true;
                        for (int i = 0; i < moving.length; i++) {
                            if (moving[i]) {
                                doMove = false;
                                break;
                            }
                        }
                    } else if (moveMode == 2) {
                        doMove = ticks % 180 == 0;
                    }
                    if (moving[index] || doMove) {
                        moving[index] = true;
                        if (directions[index]) dots[index]++;
                        else dots[index]--;
                        if (posMode == 0) {
                            if (dots[index] == 0 || dots[index] == dimension.y - 1) {
                                moving[index] = false;
                                directions[index] = !directions[index];
                            }
                        } else if (posMode == 1) {
                            if (dots[index] == 0 || dots[index] == dimension.y - 1 || random.nextInt(dimension.y / 2) == 0) {
                                moving[index] = false;
                                directions[index] = !directions[index];
                            }
                        }
                    }
                }
            }
            updateCube();
        }
    }

    @Override
    public synchronized void reset() {
        for (int x = 0; x < dimension.x; x++) {
            for (int z = 0; z < dimension.z; z++) {
                int index = z + (x * dimension.y);
                directions[index] = random.nextBoolean();
                dots[index] = directions[index] ? 0 : dimension.y - 1;
                moving[index] = false;
            }
        }
        updateCube();
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("posmode", "Position", AnimationOption.OptionType.COMBOBOX, new Object[]{posMode, 0, "Top/Bottom", 1, "Random"}),
            new AnimationOption("movemode", "Movement", AnimationOption.OptionType.COMBOBOX, new Object[]{moveMode, 0, "Random", 1, "Sequential", 2, "Simultaneous"}),
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(19 - (speed - 1)) / 19F, 1F / 19F}),
        };
    }

    @Override
    public synchronized void optionChanged(String name, String value) {
        switch (name) {
            case "posmode":
                posMode = Integer.parseInt(value);
                break;
            case "movemode":
                moveMode = Integer.parseInt(value);
                break;
            case "speed":
                speed = 1 + (19 - Math.round(19 * Float.parseFloat(value)));
                break;
        }
    }

    private void updateCube() {
        for (int x = 0; x < dimension.x; x++) {
            for (int z = 0; z < dimension.z; z++) {
                long dot = dots[z + (x * dimension.y)];
                for (int y = 0; y < dimension.y; y++) {
                    ledManager.setLEDColor(x, y, z, y == dot ? LEDCubeManager.getPaintColor() : new Color());
                }
            }
        }
    }
}
