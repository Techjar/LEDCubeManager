
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Direction;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector2;
import com.techjar.ledcm.util.Vector3;
import java.util.Random;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class AnimationFaucetFill extends Animation {
    private Random random = new Random();
    private Vector2 pos = new Vector2();

    public AnimationFaucetFill() {
        super();
    }

    @Override
    public String getName() {
        return "Faucet Fill";
    }

    @Override
    public synchronized void refresh() {
        if (ticks % 3 == 0) {
            for (int y = 0; y < dimension.y; y++) {
                for (int x = 0; x < dimension.x; x++) {
                    for (int z = 0; z < dimension.z; z++) {
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
                pos.setX(MathHelper.clamp(pos.getX() + (random.nextInt(3) - 1), 0, dimension.x - 1));
                pos.setY(MathHelper.clamp(pos.getY() + (random.nextInt(3) - 1), 0, dimension.z - 1));
            }
            ledManager.setLEDColor((int)pos.getX(), dimension.y - 1, (int)pos.getY(), LEDCubeManager.getPaintColor());
        }
    }

    @Override
    public synchronized void reset() {
        pos = new Vector2(random.nextInt(dimension.x), random.nextInt(dimension.z));
    }

    private boolean isValidPosition(Vector3 position) {
        if (position.getX() < 0 || position.getX() > dimension.x - 1 || position.getY() < 0 || position.getY() > dimension.y - 1 || position.getZ() < 0 || position.getZ() > dimension.z - 1) return false;
        return ledManager.getLEDColor((int)position.getX(), (int)position.getY(), (int)position.getZ()).equals(ReadableColor.BLACK);
    }
}
