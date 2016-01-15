
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.LEDUtil;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector3;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationFireworks extends Animation {
    private Random random = new Random();
    private List<Dot> dots = new ArrayList<>();
    private int speed = 3;
    private int chance = 30;
    private int colorMode = 1;
    private Color rocketColor = new Color(255, 255, 255);

    @Override
    public String getName() {
        return "Fireworks";
    }

    @Override
    public synchronized void refresh() {
        if (ticks % speed == 0) {
            if (random.nextInt(chance) == 0) {
                dots.add(new Dot(new Vector3(random.nextInt(dimension.x), 0, random.nextInt(dimension.z)), getColor(), 1, true));
            }
            List<Dot> addedDots = new ArrayList<>();
            Iterator<Dot> it = dots.iterator();
            while (it.hasNext()) {
                Dot dot = it.next();
                if (dot.rocket) {
                    if ((int)dot.position.getY() >= dimension.y - 3) {
                        it.remove();
                        for (int x = (int)dot.position.getX() - 2; x <= (int)dot.position.getX() + 2; x++) {
                            for (int y = (int)dot.position.getY() - 2; y <= (int)dot.position.getY() + 2; y++) {
                                for (int z = (int)dot.position.getZ() - 2; z <= (int)dot.position.getZ() + 2; z++) {
                                    if (random.nextInt(8) == 0) {
                                        addedDots.add(new Dot(new Vector3(x, y, z), dot.color, 1, false));
                                    }
                                }
                            }
                        }
                    } else {
                        dot.position = dot.position.add(new Vector3(0, 1, 0));
                    }
                } else {
                    if (dot.brightness <= 0) {
                        it.remove();
                    } else if ((int)dot.position.getY() > 0) {
                        if (random.nextInt(5) == 0) {
                            dot.position = dot.position.subtract(new Vector3(0, 1, 0));
                        }
                    }
                    dot.brightness -= random.nextFloat() * 0.05F;
                }
            }
            dots.addAll(addedDots);
            updateCube();
        }
    }

    @Override
    public synchronized void reset() {
        dots.clear();
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("rocketcolor", "R. Color", AnimationOption.OptionType.COLORPICKER, new Object[]{rocketColor}),
            new AnimationOption("colormode", "Color", AnimationOption.OptionType.COMBOBOX, new Object[]{colorMode, 0, "Random", 1, "Random Hue", 2, "Picker"}),
            new AnimationOption("chance", "Chance", AnimationOption.OptionType.SLIDER, new Object[]{(199 - (chance - 1)) / 199F, 1F / 199F, false}),
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(19 - (speed - 1)) / 19F, 1F / 19F}),
        };
    }

    @Override
    public synchronized void optionChanged(String name, String value) {
        switch (name) {
            case "rocketcolor":
                rocketColor = Util.stringToColor(value);
                break;
            case "colormode":
                colorMode = Integer.parseInt(value);
                break;
            case "chance":
                chance = 1 + (199 - Math.round(199 * Float.parseFloat(value)));
                break;
            case "speed":
                speed = 1 + (19 - Math.round(19 * Float.parseFloat(value)));
                break;
        }
    }

    private Color getColor() {
        if (colorMode == 1) {
            Color color = new Color();
            color.fromHSB(random.nextFloat(), 1, 1);
            return color;
        } else if (colorMode == 2) {
            return new Color(LEDCubeManager.getPaintColor());
        }
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    private void updateCube() {
        LEDUtil.clear(ledManager);
        for (Dot dot : dots) {
            if (Util.isInsideCube(dot.position)) {
                ledManager.setLEDColor((int)dot.position.getX(), (int)dot.position.getY(), (int)dot.position.getZ(), dot.rocket ? rocketColor : Util.multiplyColor(dot.color, dot.brightness));
            }
        }
    }

    @Data @AllArgsConstructor private static class Dot {
        public Vector3 position;
        public Color color;
        public float brightness;
        public boolean rocket;
    }
}
