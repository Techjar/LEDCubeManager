
package com.techjar.cubedesigner.hardware.animation;

import com.techjar.cubedesigner.CubeDesigner;
import com.techjar.cubedesigner.util.Direction;
import com.techjar.cubedesigner.util.Timer;
import com.techjar.cubedesigner.util.Util;
import com.techjar.cubedesigner.util.Vector3;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationSnakeBattle extends Animation {
    private Timer timer = new Timer();
    private Random random = new Random();
    private LinkedList<Vector3>[] segmentArray;
    private boolean[] states = new boolean[512];
    private boolean[] dead;
    private Direction[] direction;
    private Color[] colors;
    private final int NUM_SNAKES;

    public AnimationSnakeBattle() {
        super();
        NUM_SNAKES = 5;
        segmentArray = new LinkedList[NUM_SNAKES];
        for (int i = 0; i < NUM_SNAKES; i++) {
            segmentArray[i] = new LinkedList<>();
        }
        dead = new boolean[NUM_SNAKES];
        direction = new Direction[NUM_SNAKES];
        colors = new Color[NUM_SNAKES];
        for (int i = 0; i < NUM_SNAKES; i++) {
            colors[i] = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }
        /*colors[0] = new Color(255, 0, 0);
        colors[1] = new Color(0, 0, 255);
        colors[2] = new Color(0, 255, 0);
        colors[3] = new Color(255, 255, 0);
        colors[4] = new Color(0, 255, 255);*/
    }

    @Override
    public String getName() {
        return "Snake Battle";
    }

    @Override
    public void refresh() {
        if (timer.getMilliseconds() >= 50) {
            timer.restart();
            for (int i = 0; i < NUM_SNAKES; i++) {
                LinkedList<Vector3> segments = segmentArray[i];
                Color color = colors[i];
                if (segments.isEmpty()) {
                    dead[i] = false;
                    Vector3 pos = new Vector3(random.nextInt(dimension.x), random.nextInt(dimension.y), random.nextInt(dimension.z));
                    segments.addFirst(pos);
                    states[Util.encodeCubeVector(pos)] = true;
                    ledManager.setLEDColor((int)pos.getX(), (int)pos.getY(), (int)pos.getZ(), color);
                } else {
                    if (!dead[i]) {
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
                    if (dead[i] || segments.size() > 15) {
                        Vector3 pos = segments.removeLast();
                        states[Util.encodeCubeVector(pos)] = false;
                        ledManager.setLEDColor((int)pos.getX(), (int)pos.getY(), (int)pos.getZ(), new Color());
                    }
                }
            }
        }
    }

    @Override
    public void reset() {
        for (int i = 0; i < NUM_SNAKES; i++) {
            segmentArray[i].clear();
        }
        for (int i = 0; i < NUM_SNAKES; i++) {
            colors[i] = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }
        dead = new boolean[NUM_SNAKES];
        direction = new Direction[NUM_SNAKES];
        states = new boolean[512];
    }

    private boolean isValidPosition(Vector3 position) {
        if (position.getX() < 0 || position.getX() > dimension.x - 1 || position.getY() < 0 || position.getY() > dimension.y - 1 || position.getZ() < 0 || position.getZ() > dimension.z - 1) return false;
        return !states[Util.encodeCubeVector(position)];
    }
}
