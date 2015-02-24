
package com.techjar.cubedesigner.hardware.animation;

import com.techjar.cubedesigner.CubeDesigner;
import com.techjar.cubedesigner.util.Direction;
import com.techjar.cubedesigner.util.MathHelper;
import com.techjar.cubedesigner.util.Timer;
import com.techjar.cubedesigner.util.Util;
import com.techjar.cubedesigner.util.Vector2;
import com.techjar.cubedesigner.util.Vector3;
import java.util.Random;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class AnimationFaucetFillRainbow extends Animation {
    private Random random = new Random();
    private Vector2 pos = new Vector2();
    private Timer timer = new Timer();

    public AnimationFaucetFillRainbow() {
        super();
    }

    @Override
    public String getName() {
        return "Rainbow Faucet Fill";
    }

    @Override
    public void refresh() {
        if (ticks % 3 == 0) {
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < 8; x++) {
                    for (int z = 0; z < 8; z++) {
                        Color color = ledManager.getLEDColor(x, y, z);
                        if (color.equals(ReadableColor.BLACK)) continue;
                        if (y > 0 && ledManager.getLEDColor(x, y - 1, z).equals(ReadableColor.BLACK)) {
                            ledManager.setLEDColor(x, y - 1, z, color);
                            ledManager.setLEDColor(x, y, z, ReadableColor.BLACK);
                        } else {
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
            if (random.nextInt(2) == 0) {
                pos.setX(MathHelper.clamp(pos.getX() + (random.nextInt(3) - 1), 0, 7));
                pos.setY(MathHelper.clamp(pos.getY() + (random.nextInt(3) - 1), 0, 7));
            }
            double value = (timer.getSeconds() / 7) % 1;
            Color color = new Color();
            color.fromHSB((float)value, 1, 1);
            ledManager.setLEDColor((int)pos.getX(), 7, (int)pos.getY(), color);
        }
    }

    @Override
    public void reset() {
        pos = new Vector2(random.nextInt(8), random.nextInt(8));
        timer.restart();
    }

    private boolean isValidPosition(Vector3 position) {
        if (position.getX() < 0 || position.getX() > dimension.x - 1 || position.getY() < 0 || position.getY() > dimension.y - 1 || position.getZ() < 0 || position.getZ() > dimension.z - 1) return false;
        return ledManager.getLEDColor((int)position.getX(), (int)position.getY(), (int)position.getZ()).equals(ReadableColor.BLACK);
    }
}
