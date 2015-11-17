
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.LEDUtil;
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
public class AnimationSnakeInfinite extends Animation {
    private Random random = new Random();
    private LinkedList<Vector3> segments = new LinkedList<>();
    private boolean[] states;
    private boolean dead;
    private Direction direction;
    private float hue;

    public AnimationSnakeInfinite() {
        super();
        states = new boolean[ledManager.getLEDCount()];
    }

    @Override
    public String getName() {
        return "Snake Infinity";
    }

    @Override
    public synchronized void refresh() {
        if (dead || ticks % 3 == 0) {
            Color color = new Color();
            color.fromHSB(hue % 1, 1, 1);
            hue += 0.015F;
            if (segments.isEmpty()) {
                dead = false;
                Vector3 pos = new Vector3(random.nextInt(dimension.x), random.nextInt(dimension.y), random.nextInt(dimension.z));
                segments.addFirst(pos);
                states[Util.encodeCubeVector(pos)] = true;
                ledManager.setLEDColor((int)pos.getX(), (int)pos.getY(), (int)pos.getZ(), color);
            } else {
                if (!dead) {
                    Vector3 head = segments.getFirst();
                    if (direction == null || !isValidPosition(head.add(direction.getVector())) || random.nextInt(5) == 0) {
                        direction = null;
                        Direction[] dirs = Arrays.copyOf(Direction.VALID_DIRECTIONS, Direction.VALID_DIRECTIONS.length);
                        Util.shuffleArray(dirs, random);
                        for (Direction dir : dirs) {
                            Vector3 pos = head.add(dir.getVector());
                            if (isValidPosition(pos)) {
                                direction = dir;
                                break;
                            }
                        }
                    }
                    if (direction == null) {
                        dead = true;
                    } else {
                        Vector3 pos = head.add(direction.getVector());
                        segments.addFirst(pos);
                        states[Util.encodeCubeVector(pos)] = true;
                        ledManager.setLEDColor((int)pos.getX(), (int)pos.getY(), (int)pos.getZ(), color);
                    }
                }
                if (dead) {
                    Vector3 pos = segments.removeLast();
                    states[Util.encodeCubeVector(pos)] = false;
                    ledManager.setLEDColor((int)pos.getX(), (int)pos.getY(), (int)pos.getZ(), new Color());
                    hue = 0;
                }
            }
        }
    }

    @Override
    public synchronized void reset() {
        segments.clear();
        dead = false;
        direction = null;
        states = new boolean[ledManager.getLEDCount()];
        hue = 0;
    }

    private boolean isValidPosition(Vector3 position) {
        if (position.getX() < 0 || position.getX() > dimension.x - 1 || position.getY() < 0 || position.getY() > dimension.y - 1 || position.getZ() < 0 || position.getZ() > dimension.z - 1) return false;
        return !states[Util.encodeCubeVector(position)];
    }
}
