
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.LEDUtil;
import com.techjar.ledcm.util.Direction;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector3;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationSlidingBoxes extends Animation {
    private Random random = new Random();
    private int colorSeed;
    private int boxCount = 3;
    private int boxCountReal;
    private int boxSize;
    private int[] moving;
    private Vector3[] offsets;
    private Vector3[] directions;
    private int lastMoved = -1;
    private int colorMode = 1;
    private int speed = 3;
    private Timer timer = new Timer();

    public AnimationSlidingBoxes() {
        super();
        boxSize = Math.min(Math.min(dimension.x, dimension.y), dimension.z) / 2;
    }

    @Override
    public String getName() {
        return "Sliding Boxes";
    }

    @Override
    public synchronized void refresh() {
        if (ticks % speed == 0) {
            LEDUtil.clear(ledManager);
            boolean boxMoving = false;
            for (int i = 0; i < boxCountReal; i++) {
                if (moving[i] > 0) {
                    boxMoving = true;
                    moving[i]--;
                    offsets[i] = offsets[i].add(directions[i]);
                }
                drawBox(i, offsets[i]);
            }
            if (!boxMoving) {
                int[] boxes = new int[boxCountReal];
                for (int i = 0; i < boxCountReal; i++) {
                    boxes[i] = i;
                }
                Util.shuffleArray(boxes, random);
                Direction[] dirs = Arrays.copyOf(Direction.VALID_DIRECTIONS, Direction.VALID_DIRECTIONS.length);
                Util.shuffleArray(dirs, random);
                boxloop: for (int i = 0; i < boxCountReal; i++) {
                    int box = boxes[i];
                    if (boxCountReal > 1 && box == lastMoved) continue;
                    for (Direction dir : dirs) {
                        Vector3 pos = offsets[box].add(dir.getVector().multiply(boxSize));
                        if (isFreeSpace(pos)) {
                            directions[box] = dir.getVector();
                            moving[box] = boxSize;
                            lastMoved = box;
                            break boxloop;
                        }
                    }
                }
            }
        }
    }

    @Override
    public synchronized void reset() {
        LEDUtil.clear(ledManager);
        colorSeed = random.nextInt();
        timer.restart();
        moving = new int[boxCount];
        offsets = new Vector3[boxCount];
        directions = new Vector3[boxCount];
        for (int i = 0; i < boxCount; i++) {
            offsets[i] = new Vector3();
            directions[i] = new Vector3();
        }
        List<Vector3> offsetList = getOffsets();
        Collections.shuffle(offsetList);
        offsetList.remove(0);
        boxCountReal = 0;
        for (int i = 0; i < boxCount; i++) {
            moving[i] = 0;
            directions[i] = new Vector3();
            if (offsetList.size() > 0) {
                offsets[i] = offsetList.remove(0);
                drawBox(i, offsets[i]);
                boxCountReal++;
            } else {
                offsets[i] = null;
            }
        }
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("boxcount", "Box Count", AnimationOption.OptionType.SPINNER, new Object[]{boxCount, 1, ledManager.getLEDCount() - 1, 1, 0}),
            new AnimationOption("boxsize", "Box Size", AnimationOption.OptionType.SPINNER, new Object[]{boxSize, 1, Math.min(Math.min(dimension.x, dimension.y), dimension.z) / 2, 1, 0}),
            new AnimationOption("colormode", "Color", AnimationOption.OptionType.COMBOBOX, new Object[]{colorMode, 0, "Random", 1, "Random After 7", 2, "Hue Cycle"}),
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(19 - (speed - 1)) / 19F, 1F / 19F}),
        };
    }

    @Override
    public synchronized void optionChanged(String name, String value) {
        switch (name) {
            case "boxcount":
                boxCount = (int)Float.parseFloat(value);
                reset();
                break;
            case "boxsize":
                boxSize = (int)Float.parseFloat(value);
                reset();
                break;
            case "colormode":
                colorMode = Integer.parseInt(value);
                break;
            case "speed":
                speed = 1 + (19 - Math.round(19 * Float.parseFloat(value)));
                break;
        }
    }

    private List<Vector3> getOffsets() {
        List<Vector3> list = new ArrayList<>();
        for (int x = 0; x < dimension.x / boxSize; x++) {
            for (int y = 0; y < dimension.y / boxSize; y++) {
                for (int z = 0; z < dimension.z / boxSize; z++) {
                    list.add(new Vector3(x * boxSize, y * boxSize, z * boxSize));
                }
            }
        }
        return list;
    }

    private Color getBoxColor(int index) {
        Random rand = new Random(colorSeed + index);
        rand.nextInt(); // Throw away crappy first result
        if (colorMode == 2) {
            Color color = new Color();
            color.fromHSB((rand.nextFloat() + ((float)timer.getSeconds() / 20)) % 1, 1, 1);
            return color;
        }
        if (colorMode == 1) {
            switch (index) {
                case 0: return new Color(255, 0, 0);
                case 1: return new Color(0, 255, 0);
                case 2: return new Color(0, 0, 255);
                case 3: return new Color(255, 255, 0);
                case 4: return new Color(255, 0, 255);
                case 5: return new Color(0, 255, 255);
                case 6: return new Color(255, 255, 255);
            }
        }
        return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }

    private void drawBox(int index, Vector3 offset) {
        Color color = getBoxColor(index);
        for (int x = (int)offset.getX(); x < (int)offset.getX() + boxSize; x++) {
            for (int y = (int)offset.getY(); y < (int)offset.getY() + boxSize; y++) {
                for (int z = (int)offset.getZ(); z < (int)offset.getZ() + boxSize; z++) {
                    if (Util.isInsideCube(x, y, z)) {
                        ledManager.setLEDColor(x, y, z, color);
                    }
                }
            }
        }
    }

    private boolean isFreeSpace(Vector3 pos) {
        if (pos.getX() < 0 || pos.getX() >= dimension.x || pos.getY() < 0 || pos.getY() >= dimension.y || pos.getZ() < 0 || pos.getZ() >= dimension.z) return false;
        return ledManager.getLEDColor((int)pos.getX(), (int)pos.getY(), (int)pos.getZ()).equals(new Color());
    }
}
