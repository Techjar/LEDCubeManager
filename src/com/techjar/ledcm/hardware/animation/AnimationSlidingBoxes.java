
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.hardware.LEDUtil;
import com.techjar.ledcm.util.Direction;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector3;
import java.util.Arrays;
import java.util.Random;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationSlidingBoxes extends Animation {
    private Random random = new Random();
    private int boxCount = 3;
    private int[] moving = new int[boxCount];
    private Vector3[] offsets = new Vector3[boxCount];
    private Vector3[] directions = new Vector3[boxCount];
    private int lastMoved = -1;

    public AnimationSlidingBoxes() {
        super();
        for (int i = 0; i < boxCount; i++) {
            offsets[i] = new Vector3();
            directions[i] = new Vector3();
        }
    }

    @Override
    public String getName() {
        return "Sliding Boxes";
    }

    @Override
    public void refresh() {
        if (ticks % 3 == 0) {
            LEDUtil.clear(ledManager);
            boolean boxMoving = false;
            for (int i = 0; i < boxCount; i++) {
                if (moving[i] > 0) {
                    boxMoving = true;
                    moving[i]--;
                    offsets[i] = offsets[i].add(directions[i]);
                }
                drawBox(i, offsets[i]);
            }
            if (!boxMoving) {
                int[] boxes = new int[boxCount];
                for (int i = 0; i < boxCount; i++) {
                    boxes[i] = i;
                }
                Util.shuffleArray(boxes, random);
                Direction[] dirs = Arrays.copyOf(Direction.VALID_DIRECTIONS, Direction.VALID_DIRECTIONS.length);
                Util.shuffleArray(dirs, random);
                boxloop: for (int i = 0; i < boxCount; i++) {
                    int box = boxes[i];
                    if (boxCount > 1 && box == lastMoved) continue;
                    for (Direction dir : dirs) {
                        Vector3 pos = offsets[box].add(dir.getVector().multiply(4));
                        if (isFreeSpace(pos)) {
                            directions[box] = dir.getVector();
                            moving[box] = 4;
                            lastMoved = box;
                            break boxloop;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void reset() {
        LEDUtil.clear(ledManager);
        for (int i = 0; i < boxCount; i++) {
            moving[i] = 0;
            directions[i] = new Vector3();
            do {
                offsets[i] = new Vector3(random.nextInt(2) * 4, random.nextInt(2) * 4, random.nextInt(2) * 4);
            } while (!isFreeSpace(offsets[i]));
            drawBox(i, offsets[i]);
        }
    }

    private Color getBoxColor(int index) {
        switch (index) {
            case 0: return new Color(255, 0, 0);
            case 1: return new Color(0, 255, 0);
            case 2: return new Color(0, 0, 255);
            case 3: return new Color(255, 255, 0);
            case 4: return new Color(255, 0, 255);
            case 5: return new Color(0, 255, 255);
        }
        return new Color(255, 255, 255);
    }

    private void drawBox(int index, Vector3 offset) {
        Color color = getBoxColor(index);
        for (int x = (int)offset.getX(); x < (int)offset.getX() + 4; x++) {
            for (int y = (int)offset.getY(); y < (int)offset.getY() + 4; y++) {
                for (int z = (int)offset.getZ(); z < (int)offset.getZ() + 4; z++) {
                    ledManager.setLEDColor(x, y, z, color);
                }
            }
        }
    }

    private boolean isFreeSpace(Vector3 pos) {
        if (pos.getX() < 0 || pos.getX() >= dimension.x || pos.getY() < 0 || pos.getY() >= dimension.y || pos.getZ() < 0 || pos.getZ() >= dimension.z) return false;
        return ledManager.getLEDColor((int)pos.getX(), (int)pos.getY(), (int)pos.getZ()).equals(new Color());
    }
}