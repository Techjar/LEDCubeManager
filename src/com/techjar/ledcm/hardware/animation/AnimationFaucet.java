
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Direction;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector2;
import com.techjar.ledcm.util.Vector3;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class AnimationFaucet extends Animation {
    private Random random = new Random();
    private Vector2[] faucets;
    private Timer timer = new Timer();
    private int colorMode = 0;
    private int speed = 3;
    private boolean fill = false;
    private int faucetCount = 1;
    private int colorSeed;

    public AnimationFaucet() {
        super();
    }

    @Override
    public String getName() {
        return "Faucet";
    }

    @Override
    public synchronized void refresh() {
        if (ticks % speed == 0) {
            for (int y = 0; y < dimension.y; y++) {
                for (int x = 0; x < dimension.x; x++) {
                    for (int z = 0; z < dimension.z; z++) {
                        Color color = ledManager.getLEDColor(x, y, z);
                        if (fill && color.equals(ReadableColor.BLACK)) continue;
                        if (y > 0 && (!fill || ledManager.getLEDColor(x, y - 1, z).equals(ReadableColor.BLACK))) {
                            ledManager.setLEDColor(x, y - 1, z, color);
                            ledManager.setLEDColor(x, y, z, ReadableColor.BLACK);
                        } else if (fill) {
                            Vector3 pos2 = new Vector3(x, y, z);
                            Direction direction = null;
                            Direction[] dirs = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
                            Util.shuffleArray(dirs, random);
                            for (Direction dir : dirs) {
                                Vector3 pos3 = pos2.add(dir.getVector());
                                if (isValidPosition(pos3)) {
                                    direction = dir;
                                    break;
                                }
                            }
                            if (direction != null) {
                                pos2 = pos2.add(direction.getVector());
                                ledManager.setLEDColor((int)pos2.getX(), (int)pos2.getY(), (int)pos2.getZ(), color);
                                ledManager.setLEDColor(x, y, z, ReadableColor.BLACK);
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < faucets.length; i++) {
                Vector2 pos = faucets[i];
                if (random.nextInt(2) == 0) {
                    int j = 0;
                    do {
                        do {
                            pos.setX(MathHelper.clamp(pos.getX() + (random.nextInt(3) - 1), 0, dimension.x - 1));
                            pos.setY(MathHelper.clamp(pos.getY() + (random.nextInt(3) - 1), 0, dimension.z - 1));
                        } while (isFaucetAt(pos, true));
                        j++;
                    } while (j < 20 && !ledManager.getLEDColor((int)pos.getX(), dimension.y - 1, (int)pos.getY()).equals(ReadableColor.BLACK));
                }
                if (ledManager.getLEDColor((int)pos.getX(), dimension.y - 1, (int)pos.getY()).equals(ReadableColor.BLACK)) {
                    ledManager.setLEDColor((int)pos.getX(), dimension.y - 1, (int)pos.getY(), getColor(i));
                }
            }
        }
    }

    @Override
    public synchronized void reset() {
        colorSeed = random.nextInt();
        timer.restart();
        faucets = new Vector2[faucetCount];
        for (int i = 0; i < faucetCount; i++) {
            do {
                faucets[i] = new Vector2(random.nextInt(dimension.x), random.nextInt(dimension.z));
            } while (isFaucetAt(faucets[i], true));
        }
    }

    @Override
    public boolean isFinished() {
        for (int x = 0; x < dimension.x; x++) {
            for (int y = 0; y < dimension.y; y++) {
                for (int z = 0; z < dimension.z; z++) {
                    if (ledManager.getLEDColor(x, y, z).equals(ReadableColor.BLACK)) return false;
                }
            }
        }
        return true;
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("faucetcount", "Count", AnimationOption.OptionType.SPINNER, new Object[]{faucetCount, 1, (dimension.x * dimension.z) / 4, 1, 0}),
            new AnimationOption("colormode", "Color", AnimationOption.OptionType.COMBOBOX, new Object[]{colorMode, 0, "Picker", 1, "Rainbow", 2, "Random", 3, "Static Random"}),
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(19 - (speed - 1)) / 19F, 1F / 19F}),
            new AnimationOption("fill", "Fill", AnimationOption.OptionType.CHECKBOX, new Object[]{fill}),
        };
    }

    @Override
    public synchronized void optionChanged(String name, String value) {
        switch (name) {
            case "faucetcount":
                faucetCount = (int)Float.parseFloat(value);
                reset();
                break;
            case "colormode":
                colorMode = Integer.parseInt(value);
                break;
            case "speed":
                speed = 1 + (19 - Math.round(19 * Float.parseFloat(value)));
                break;
            case "fill":
                fill = Boolean.parseBoolean(value);
                break;
        }
    }

    private Color getColor(int index) {
        Random rand = new Random(colorSeed + index);
        rand.nextInt(); // Throw away crappy first result
        if (colorMode == 0) {
            return LEDCubeManager.getPaintColor();
        } else if (colorMode == 1) {
            Color color = new Color();
            color.fromHSB((rand.nextFloat() + ((float)timer.getSeconds() / 7)) % 1, 1, 1);
            return color;
        } else if (colorMode == 3) {
            return new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
        }
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    private boolean isValidPosition(Vector3 position) {
        return Util.isInsideCube(position) && ledManager.getLEDColor((int)position.getX(), (int)position.getY(), (int)position.getZ()).equals(ReadableColor.BLACK);
    }

    private boolean isFaucetAt(Vector2 pos, boolean otherOnly) {
        for (int i = 0; i < faucetCount; i++) {
            Vector2 faucet = faucets[i];
            if (faucet == null) continue;
            if ((!otherOnly || faucet != pos) && (int)faucet.getX() == (int)pos.getX() && (int)faucet.getY() == (int)pos.getY()) return true;
        }
        return false;
    }
}
