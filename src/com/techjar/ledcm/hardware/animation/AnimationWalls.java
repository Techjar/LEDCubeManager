
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.LEDUtil;
import com.techjar.ledcm.util.Direction;
import com.techjar.ledcm.util.Util;
import java.util.Arrays;
import java.util.Random;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationWalls  extends Animation {
    private Random random = new Random();
    private int colorSeed;
    private Direction direction;
    private int colorMode = 0;
    private int speed = 30;

    public AnimationWalls() {
        super();
    }

    @Override
    public String getName() {
        return "Walls";
    }

    @Override
    public synchronized void refresh() {
        if (ticks % speed == 0) {
            LEDUtil.clear(ledManager);
            int startX = 0, startY = 0, startZ = 0;
            int endX = dimension.x;
            int endY = dimension.y;
            int endZ = dimension.z;
            direction = getRandomDirection(direction);
            switch (direction) {
                case UP:
                    startY = dimension.y - 1;
                    break;
                case DOWN:
                    endY = 1;
                    break;
                case NORTH:
                    endZ = 1;
                    break;
                case SOUTH:
                    startZ = dimension.z - 1;
                    break;
                case EAST:
                    startX = dimension.x - 1;
                    break;
                case WEST:
                    endX = 1;
                    break;
            }
            for (int x = startX; x < endX; x++) {
                for (int y = startY; y < endY; y++) {
                    for (int z = startZ; z < endZ; z++) {
                        ledManager.setLEDColor(x, y, z, getColor(direction));
                    }
                }
            }
        }
    }

    @Override
    public synchronized void reset() {
        colorSeed = random.nextInt();
        direction = Direction.UNKNOWN;
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("colormode", "Color", AnimationOption.OptionType.COMBOBOX, new Object[]{colorMode, 0, "Picker", 1, "Random"}),
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(299 - (speed - 1)) / 299F, 1F / 299F, false}),
        };
    }

    @Override
    public synchronized void optionChanged(String name, String value) {
        switch (name) {
            case "colormode":
                colorMode = Integer.parseInt(value);
                break;
            case "speed":
                speed = 1 + (299 - Math.round(299 * Float.parseFloat(value)));
                break;
        }
    }

    private Color getColor(Direction dir) {
        Random rand = new Random(colorSeed + dir.ordinal());
        rand.nextInt(); // Throw away crappy first result
        if (colorMode == 0) {
            return LEDCubeManager.getPaintColor();
        }
        return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }

    private Direction getRandomDirection(Direction current) {
        Direction[] dirs = Arrays.copyOf(Direction.VALID_DIRECTIONS, Direction.VALID_DIRECTIONS.length);
        Util.shuffleArray(dirs, random);
        for (Direction dir : dirs) {
            if (dir != current && dir != current.getOpposite()) return dir;
        }
        throw new RuntimeException("what"); // This should never happen
    }
}
