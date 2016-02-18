
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.LEDUtil;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector3;
import java.util.Random;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class AnimationWireframe  extends Animation {
    private Random random = new Random();
    private int animMode = 0;
    private int colorMode = 0;
    private int speed = 3;
    private Vector3[] traceOffsets = new Vector3[6];
    private int traceDir;
    private boolean traceState;
    private Vector3 resizeMin;
    private Vector3 resizeMax;
    private Vector3 resizeDir;
    private Vector3 oldResizeDir;
    private boolean resizeState;

    public AnimationWireframe() {
        super();
    }

    @Override
    public String getName() {
        return "Wireframe";
    }

    @Override
    public synchronized void refresh() {
        if (ticks % speed == 0) {
            if (animMode == 0) {
                LEDUtil.clear(ledManager);
                for (int x = 0; x < dimension.x; x++) {
                    for (int y = 0; y < dimension.y; y++) {
                        for (int z = 0; z < dimension.z; z++) {
                            if (((x == 0 || x == dimension.x - 1) && (y == 0 || y == dimension.y - 1)) || ((x == 0 || x == dimension.x - 1) && (z == 0 || z == dimension.z - 1)) || ((y == 0 || y == dimension.y - 1) && (z == 0 || z == dimension.z - 1))) {
                                ledManager.setLEDColor(x, y, z, getColor(x, y, z));
                            }
                        }
                    }
                }
            } else if (animMode == 1) {
                for (int i = 0; i < traceOffsets.length; i++) {
                    Vector3 offset = traceOffsets[i];
                    setLED((int)offset.getX(), (int)offset.getY(), (int)offset.getZ(), !traceState);
                    switch (i) {
                        case 0:
                            if (offset.getX() == (traceDir > 0 ? dimension.x - 1 : 0)) {
                                if (offset.getY() == (traceDir > 0 ? dimension.y - 1 : 0)) {
                                    if (offset.getZ() != (traceDir > 0 ? dimension.z - 1 : 0)) {
                                        traceOffsets[i] = offset.add(new Vector3(0, 0, traceDir));
                                    }
                                } else {
                                    Vector3 offset2 = new Vector3(offset.getX(), offset.getZ(), offset.getY());
                                    setLED((int)offset2.getX(), (int)offset2.getY(), (int)offset2.getZ(), !traceState);
                                    traceOffsets[i] = offset.add(new Vector3(0, traceDir, 0));
                                }
                            } else {
                                traceOffsets[i] = offset.add(new Vector3(traceDir, 0, 0));
                            }
                            break;
                        case 1:
                            if (offset.getY() == (traceDir > 0 ? dimension.y - 1 : 0)) {
                                if (offset.getZ() == (traceDir > 0 ? dimension.z - 1 : 0)) {
                                    if (offset.getX() != (traceDir > 0 ? dimension.x - 1 : 0)) {
                                        traceOffsets[i] = offset.add(new Vector3(traceDir, 0, 0));
                                    }
                                } else {
                                    Vector3 offset2 = new Vector3(offset.getZ(), offset.getY(), offset.getX());
                                    setLED((int)offset2.getX(), (int)offset2.getY(), (int)offset2.getZ(), !traceState);
                                    traceOffsets[i] = offset.add(new Vector3(0, 0, traceDir));
                                }
                            } else {
                                traceOffsets[i] = offset.add(new Vector3(0, traceDir, 0));
                            }
                            break;
                        case 2:
                            if (offset.getZ() == (traceDir > 0 ? dimension.z - 1 : 0)) {
                                if (offset.getX() == (traceDir > 0 ? dimension.x - 1 : 0)) {
                                    if (offset.getY() != (traceDir > 0 ? dimension.y - 1 : 0)) {
                                        traceOffsets[i] = offset.add(new Vector3(0, traceDir, 0));
                                    } else {
                                        traceState = !traceState;
                                        for (int j = 0; j < traceOffsets.length; j++) {
                                            traceOffsets[j] = new Vector3();
                                        }
                                    }
                                } else {
                                    Vector3 offset2 = new Vector3(offset.getY(), offset.getX(), offset.getZ());
                                    setLED((int)offset2.getX(), (int)offset2.getY(), (int)offset2.getZ(), !traceState);
                                    traceOffsets[i] = offset.add(new Vector3(traceDir, 0, 0));
                                }
                            } else {
                                traceOffsets[i] = offset.add(new Vector3(0, 0, traceDir));
                            }
                            break;
                    }
                }
            } else if (animMode == 2) {
                drawResizeBox();
                if (resizeDir == null) {
                    do {
                        resizeDir = new Vector3(random.nextBoolean() ? 1 : -1, random.nextBoolean() ? 1 : -1, random.nextBoolean() ? 1 : -1);
                    } while (resizeDir.equals(oldResizeDir));
                }
                if (resizeDir.getX() > 0) {
                    resizeMin = resizeState ? resizeMin.add(new Vector3(resizeDir.getX(), 0, 0)) : resizeMin.subtract(new Vector3(resizeDir.getX(), 0, 0));
                } else {
                    resizeMax = resizeState ? resizeMax.add(new Vector3(resizeDir.getX(), 0, 0)) : resizeMax.subtract(new Vector3(resizeDir.getX(), 0, 0));
                }
                if (resizeDir.getY() > 0) {
                    resizeMin = resizeState ? resizeMin.add(new Vector3(0, resizeDir.getY(), 0)) : resizeMin.subtract(new Vector3(0, resizeDir.getY(), 0));
                } else {
                    resizeMax = resizeState ? resizeMax.add(new Vector3(0, resizeDir.getY(), 0)) : resizeMax.subtract(new Vector3(0, resizeDir.getY(), 0));
                }
                if (resizeDir.getZ() > 0) {
                    resizeMin = resizeState ? resizeMin.add(new Vector3(0, 0, resizeDir.getZ())) : resizeMin.subtract(new Vector3(0, 0, resizeDir.getZ()));
                    if (resizeState && resizeMin.getZ() == dimension.z - 1) {
                        resizeState = false;
                    } else if (!resizeState && resizeMin.getZ() == 0) {
                        oldResizeDir = resizeDir;
                        resizeDir = null;
                        resizeState = true;
                    }
                } else {
                    resizeMax = resizeState ? resizeMax.add(new Vector3(0, 0, resizeDir.getZ())) : resizeMax.subtract(new Vector3(0, 0, resizeDir.getZ()));
                    if (resizeState && resizeMax.getZ() == 0) {
                        resizeState = false;
                    } else if (!resizeState && resizeMax.getZ() == dimension.z - 1) {
                        oldResizeDir = resizeDir;
                        resizeDir = null;
                        resizeState = true;
                    }
                }
            } else if (animMode == 3) {
                drawResizeBox();
                resizeMin = resizeState ? resizeMin.add(new Vector3(1, 1, 1)) : resizeMin.subtract(new Vector3(1, 1, 1));
                resizeMax = resizeState ? resizeMax.subtract(new Vector3(1, 1, 1)) : resizeMax.add(new Vector3(1, 1, 1));
                if ((int)resizeMin.getX() == 0 || (int)resizeMin.getX() == (Math.min(dimension.x, Math.min(dimension.y, dimension.z)) - 1) / 2) {
                    resizeState = !resizeState;
                }
            }
        }
    }

    @Override
    public synchronized void reset() {
        if (animMode == 1) LEDUtil.clear(ledManager);
        traceDir = 1;
        traceState = true;
        for (int i = 0; i < traceOffsets.length; i++) {
            traceOffsets[i] = new Vector3();
        }
        resizeMin = new Vector3();
        resizeMax = new Vector3(dimension.x - 1, dimension.y - 1, dimension.z - 1);
        resizeState = true;
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("animmode", "Animation", AnimationOption.OptionType.COMBOBOX, new Object[]{animMode, 0, "Static", 1, "Trace", 2, "Resize", 3, "Resize Center"}),
            new AnimationOption("colormode", "Color", AnimationOption.OptionType.COMBOBOX, new Object[]{colorMode, 0, "Picker", 1, "Spectrum"}),
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(19 - (speed - 1)) / 19F, 1F / 19F}),
        };
    }

    @Override
    public synchronized void optionChanged(String name, String value) {
        switch (name) {
            case "animmode":
                animMode = Integer.parseInt(value);
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

    @Override
    public boolean isFinished() {
        return !traceState;
    }

    private Color getColor(int x, int y, int z) {
        if (colorMode == 1) {
            return new Color(Math.round((x / (dimension.x - 1F)) * 255), Math.round((y / (dimension.y - 1F)) * 255), Math.round((z / (dimension.z - 1F)) * 255));
        }
        return LEDCubeManager.getPaintColor();
    }

    private void setLED(int x, int y, int z, boolean black)  {
        ledManager.setLEDColor(x, y, z, black ? ReadableColor.BLACK : getColor(x, y, z));
    }

    private void drawResizeBox() {
        LEDUtil.clear(ledManager);
        for (int x = (int)resizeMin.getX(); x < (int)resizeMax.getX() + 1; x++) {
            for (int y = (int)resizeMin.getY(); y < (int)resizeMax.getY() + 1; y++) {
                for (int z = (int)resizeMin.getZ(); z < (int)resizeMax.getZ() + 1; z++) {
                    if (((x == (int)resizeMin.getX() || x == (int)resizeMax.getX()) && (y == (int)resizeMin.getY() || y == (int)resizeMax.getY())) || ((x == (int)resizeMin.getX() || x == (int)resizeMax.getX()) && (z == (int)resizeMin.getZ() || z == (int)resizeMax.getZ())) || ((y == (int)resizeMin.getY() || y == (int)resizeMax.getY()) && (z == (int)resizeMin.getZ() || z == (int)resizeMax.getZ()))) {
                        ledManager.setLEDColor(x, y, z, getColor(x, y, z));
                    }
                }
            }
        }
    }
}
