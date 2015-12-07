
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.LEDUtil;
import com.techjar.ledcm.util.Angle;
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
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class AnimationBalls extends Animation {
    private Random random = new Random();
    private int colorSeed;
    private List<Vector3> positions = new ArrayList<>();
    private List<Vector3> directions = new ArrayList<>();
    private int ballCount = 1;
    private int colorMode = 1;
    private int speed = 3;
    private Timer timer = new Timer();

    public AnimationBalls() {
        super();
    }

    @Override
    public String getName() {
        return "Bouncing Balls";
    }

    @Override
    public synchronized void refresh() {
        if (ticks % speed == 0) {
            for (int j = 0; j < 2; j++) {
                for (int i = 0; i < ballCount; i++) {
                    Vector3 pos = positions.get(i);
                    Vector3 dir = directions.get(i);
                    Vector3 newPos = pos.add(dir);
                    Vector3 newPosR = new Vector3(Math.round(newPos.getX()), Math.round(newPos.getY()), Math.round(newPos.getZ()));
                    if (j == 0 && !Util.isInsideCube(newPosR)) {
                        Vector3 normal = LEDUtil.getSideDirectionFacing(ledManager, pos, dir).getOpposite().getVector();
                        Vector3 newDir = dir.subtract(normal.multiply(dir.dot(normal) * 2));
                        directions.set(i, newDir);
                    }
                    if (j == 1 && Util.isInsideCube(newPosR)) {
                        positions.set(i, newPos);
                    }
                }
            }
            drawBalls();
        }
    }

    @Override
    public synchronized void reset() {
        colorSeed = random.nextInt();
        timer.restart();
        positions.clear();
        directions.clear();
        for (int i = 0; i < ballCount; i++) {
            positions.add(new Vector3(random.nextInt(dimension.x), random.nextInt(dimension.y), random.nextInt(dimension.z)));
            directions.add(new Angle(random.nextInt(181) - 90, random.nextInt(360), 0).forward());
        }
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("ballcount", "Ball Count", AnimationOption.OptionType.SPINNER, new Object[]{ballCount, 1, ledManager.getLEDCount() / 8, 1, 0}),
            new AnimationOption("colormode", "Color", AnimationOption.OptionType.COMBOBOX, new Object[]{colorMode, 0, "Picker", 1, "Random", 2, "Hue Cycle"}),
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(19 - (speed - 1)) / 19F, 1F / 19F}),
        };
    }

    @Override
    public synchronized void optionChanged(String name, String value) {
        switch (name) {
            case "ballcount":
                ballCount = (int)Float.parseFloat(value);
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

    private void drawBalls() {
        LEDUtil.clear(ledManager);
        for (int i = 0; i < ballCount; i++) {
            Vector3 pos = positions.get(i);
            ledManager.setLEDColor(Math.round(pos.getX()), Math.round(pos.getY()), Math.round(pos.getZ()), getBallColor(i));
        }
    }

    private Color getBallColor(int index) {
        Random rand = new Random(colorSeed + index);
        rand.nextInt(); // Throw away crappy first result
        if (colorMode == 0) return LEDCubeManager.getLEDCube().getPaintColor();
        if (colorMode == 2) {
            Color color = new Color();
            color.fromHSB((rand.nextFloat() + ((float)timer.getSeconds() / 20)) % 1, 1, 1);
            return color;
        }
        return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }
}
