
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Direction;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector3;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationSnake extends Animation {
    private Random random = new Random();
    private int colorSeed;
    private LinkedList<Vector3>[] segmentArray;
    private boolean[] states;
    private boolean[] dead;
    private Direction[] direction;
    private int snakeCount = 5;
    private int snakeLength = 15;
    private boolean infinite = false;
    private int colorMode = 0;
    private int speed = 3;
    private int deathSpeed = 1;
    private boolean respawn = true;
    private Timer timer = new Timer();

    public AnimationSnake() {
        super();
    }

    @Override
    public String getName() {
        return "Snake";
    }

    @Override
    public synchronized void refresh() {
        for (int i = 0; i < snakeCount; i++) {
            LinkedList<Vector3> segments = segmentArray[i];
            Color color = getSnakeColor(i);
            if (segments.isEmpty() && (respawn || !dead[i])) {
                dead[i] = false;
                Vector3 pos = new Vector3(random.nextInt(dimension.x), random.nextInt(dimension.y), random.nextInt(dimension.z));
                segments.addFirst(pos);
                states[Util.encodeCubeVector(pos)] = true;
                ledManager.setLEDColor((int)pos.getX(), (int)pos.getY(), (int)pos.getZ(), color);
            } else {
                if (!dead[i] && ticks % speed == 0) {
                    Vector3 head = segments.getFirst();
                    if (direction[i] == null || !isValidPosition(head.add(direction[i].getVector())) || random.nextInt(5) == 0) {
                        direction[i] = null;
                        Direction[] dirs = Arrays.copyOf(Direction.VALID_DIRECTIONS, Direction.VALID_DIRECTIONS.length);
                        Util.shuffleArray(dirs, random);
                        for (Direction dir : dirs) {
                            Vector3 pos = head.add(dir.getVector());
                            if (isValidPosition(pos)) {
                                direction[i] = dir;
                                break;
                            }
                        }
                    }
                    if (direction[i] == null) {
                        dead[i] = true;
                    } else {
                        Vector3 pos = head.add(direction[i].getVector());
                        segments.addFirst(pos);
                        states[Util.encodeCubeVector(pos)] = true;
                        ledManager.setLEDColor((int)pos.getX(), (int)pos.getY(), (int)pos.getZ(), color);
                    }
                }
                if (!segments.isEmpty() && ((dead[i] && ticks % deathSpeed == 0) || (!infinite && segments.size() > snakeLength))) {
                    Vector3 pos = segments.removeLast();
                    states[Util.encodeCubeVector(pos)] = false;
                    ledManager.setLEDColor((int)pos.getX(), (int)pos.getY(), (int)pos.getZ(), new Color());
                }
            }
        }
    }

    @Override
    public synchronized void reset() {
        colorSeed = random.nextInt();
        segmentArray = new LinkedList[snakeCount];
        for (int i = 0; i < snakeCount; i++) {
            segmentArray[i] = new LinkedList<>();
        }
        dead = new boolean[snakeCount];
        direction = new Direction[snakeCount];
        states = new boolean[ledManager.getLEDCount()];
    }

    @Override
    public synchronized boolean isFinished() {
        if (respawn) return false;
        for (int i = 0; i < dead.length; i++) {
            if (!dead[i] || !segmentArray[i].isEmpty()) return false;
        }
        return true;
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("snakecount", "Snake Count", AnimationOption.OptionType.SPINNER, new Object[]{snakeCount, 1, 1000, 1, 0}),
            new AnimationOption("snakelength", "Snake Length", AnimationOption.OptionType.SPINNER, new Object[]{snakeLength, 1, 1000, 1, 0}),
            new AnimationOption("infinite", "Infinite", AnimationOption.OptionType.CHECKBOX, new Object[]{infinite}),
            new AnimationOption("colormode", "Color", AnimationOption.OptionType.COMBOBOX, new Object[]{colorMode, 0, "Random", 1, "Hue Cycle", 2, "Picker"}),
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(19 - (speed - 1)) / 19F, 1F / 19F}),
            new AnimationOption("deathspeed", "Death Speed", AnimationOption.OptionType.SLIDER, new Object[]{(19 - (deathSpeed - 1)) / 19F, 1F / 19F}),
            new AnimationOption("respawn", "Respawn", AnimationOption.OptionType.CHECKBOX, new Object[]{respawn}),
        };
    }

    @Override
    public synchronized void optionChanged(String name, String value) {
        switch (name) {
            case "snakecount":
                snakeCount = (int)Float.parseFloat(value);
                reset();
                break;
            case "snakelength":
                snakeLength = (int)Float.parseFloat(value);
                reset();
                break;
            case "infinite":
                infinite = Boolean.parseBoolean(value);
                break;
            case "colormode":
                colorMode = Integer.parseInt(value);
                break;
            case "speed":
                speed = 1 + (19 - Math.round(19 * Float.parseFloat(value)));
                break;
            case "deathspeed":
                deathSpeed = 1 + (19 - Math.round(19 * Float.parseFloat(value)));
                break;
            case "respawn":
                respawn = Boolean.parseBoolean(value);
                break;
        }
    }

    private Color getSnakeColor(int index) {
        Random rand = new Random(colorSeed + index);
        rand.nextInt(); // Throw away crappy first result
        if (colorMode == 1) {
            Color color = new Color();
            color.fromHSB((rand.nextFloat() + ((float)timer.getSeconds() / 5)) % 1, 1, 1);
            return color;
        }
        if (colorMode == 2) return LEDCubeManager.getPaintColor();
        return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }

    private boolean isValidPosition(Vector3 position) {
        if (!Util.isInsideCube(position)) return false;
        return !states[Util.encodeCubeVector(position)];
    }
}
