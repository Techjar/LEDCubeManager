
package com.techjar.cubedesigner.hardware.animation;

import com.techjar.cubedesigner.CubeDesigner;
import com.techjar.cubedesigner.util.Direction;
import com.techjar.cubedesigner.util.Timer;
import com.techjar.cubedesigner.util.Util;
import com.techjar.cubedesigner.util.Vector3;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class AnimationSnake extends Animation {
    private Timer timer = new Timer();
    private Random random = new Random();
    private LinkedList<Vector3> segments = new LinkedList<>();
    private boolean[] states = new boolean[512];
    private boolean dead;
    private Direction direction;

    public AnimationSnake() {
        super();
    }

    @Override
    public String getName() {
        return "Snake";
    }

    @Override
    public void refresh() {
        if (timer.getMilliseconds() >= 50) {
            timer.restart();
            Color color = CubeDesigner.getPaintColor();
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
                if (dead || segments.size() > 15) {
                    Vector3 pos = segments.removeLast();
                    states[Util.encodeCubeVector(pos)] = false;
                    ledManager.setLEDColor((int)pos.getX(), (int)pos.getY(), (int)pos.getZ(), new Color());
                }
            }
        }
    }

    @Override
    public void reset() {
        segments.clear();
        dead = false;
        direction = null;
        states = new boolean[512];
    }

    private boolean isValidPosition(Vector3 position) {
        if (position.getX() < 0 || position.getX() > dimension.x - 1 || position.getY() < 0 || position.getY() > dimension.y - 1 || position.getZ() < 0 || position.getZ() > dimension.z - 1) return false;
        return !states[Util.encodeCubeVector(position)];
    }
}
