
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
    private boolean[] posStates;
    private int posMode = 0;
    private int moveMode = 0;
    private int directionMode = 0;
    private int speed = 3;
    private int delay = 60;
    private boolean globalDirection;

    public AnimationScrollers() {
        super();
        dots = new int[dimension.x * dimension.z];
        directions = new boolean[dimension.x * dimension.z];
        moving = new boolean[dimension.x * dimension.z];
        posStates = new boolean[dimension.x * dimension.z];
    }

    @Override
    public String getName() {
        return "Scrollers";
    }

    @Override
    public synchronized void refresh() {
        for (int x = 0; x < dimension.x; x++) {
            for (int z = 0; z < dimension.z; z++) {
                int index = z + (x * dimension.z);
                long dot = dots[index];
                boolean doMove = false;
                if (directionMode == 0 || directions[index] == globalDirection) {
                    if (moveMode == 0) {
                        if (ticks % speed == 0) {
                            doMove = random.nextInt(500 * (dots.length / 64)) == 0;
                        }
                    } else if (moveMode == 1) {
                        doMove = true;
                        for (int i = 0; i < moving.length; i++) {
                            if (moving[i]) {
                                doMove = false;
                                break;
                            }
                        }
                    } else if (moveMode == 2) {
                        doMove = ticks % delay == 0;
                    }
                }
                if (moving[index] || doMove) {
                    moving[index] = true;
                    if (ticks % speed == 0) {
                        if (directions[index]) dots[index]++;
                        else dots[index]--;
                        if (dots[index] < 0) dots[index] = 0;
                        if (dots[index] >= dimension.y) dots[index] = dimension.y - 1;
                        if (posMode == 0 || (posMode == 2 && posStates[index])) {
                            if (dots[index] == 0 || dots[index] == dimension.y - 1) {
                                moving[index] = false;
                                directions[index] = !directions[index];
                                posStates[index] = false;
                            }
                        } else if (posMode == 1 || (posMode == 2 && !posStates[index])) {
                            if (dots[index] == 0 || dots[index] == dimension.y - 1 || random.nextInt(dimension.y / 2) == 0) {
                                moving[index] = false;
                                if (posMode != 2) directions[index] = !directions[index];
                                posStates[index] = true;
                            }
                        }
                    }
                }
                if (directionMode == 1) {
                    boolean allDone = true;
                    for (int i = 0; i < directions.length; i++) {
                        if (directions[i] == globalDirection) {
                            allDone = false;
                            break;
                        }
                    }
                    if (allDone) globalDirection = !globalDirection;
                }
            }
        }
        updateCube();
    }

    @Override
    public synchronized void reset() {
        for (int x = 0; x < dimension.x; x++) {
            for (int z = 0; z < dimension.z; z++) {
                int index = z + (x * dimension.z);
                if (posMode == 2) {
                    directions[index] = true;
                    dots[index] = 0;
                } else {
                    directions[index] = random.nextBoolean();
                    dots[index] = directions[index] ? 0 : dimension.y - 1;
                }
                moving[index] = false;
                posStates[index] = false;
            }
        }
        globalDirection = true;
        updateCube();
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("posmode", "Position", AnimationOption.OptionType.COMBOBOX, new Object[]{posMode, 0, "Opposite", 1, "Random", 2, "O/R Mix"}),
            new AnimationOption("movemode", "Movement", AnimationOption.OptionType.COMBOBOX, new Object[]{moveMode, 0, "Random", 1, "Sequential", 2, "Simultaneous"}),
            new AnimationOption("directionmode", "Direction", AnimationOption.OptionType.COMBOBOX, new Object[]{directionMode, 0, "Individual", 1, "Global"}),
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(19 - (speed - 1)) / 19F, 1F / 19F}),
            new AnimationOption("delay", "Sim. Delay", AnimationOption.OptionType.SLIDER, new Object[]{(delay - 1) / 299F, 1F / 299F, false}),
        };
    }

    @Override
    public synchronized void optionChanged(String name, String value) {
        switch (name) {
            case "posmode":
                posMode = Integer.parseInt(value);
                reset();
                break;
            case "movemode":
                moveMode = Integer.parseInt(value);
                reset();
                break;
            case "directionmode":
                directionMode = Integer.parseInt(value);
                reset();
                break;
            case "speed":
                speed = 1 + (19 - Math.round(19 * Float.parseFloat(value)));
                break;
            case "delay":
                delay = 1 + Math.round(299 * Float.parseFloat(value));
                break;
        }
    }

    private void updateCube() {
        for (int x = 0; x < dimension.x; x++) {
            for (int z = 0; z < dimension.z; z++) {
                long dot = dots[z + (x * dimension.z)];
                for (int y = 0; y < dimension.y; y++) {
                    ledManager.setLEDColor(x, y, z, y == dot ? LEDCubeManager.getPaintColor() : new Color());
                }
            }
        }
    }
}
