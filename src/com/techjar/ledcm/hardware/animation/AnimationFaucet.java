
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Vector2;
import java.util.Random;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationFaucet extends Animation {
    private Random random = new Random();
    private Vector2 pos = new Vector2();

    public AnimationFaucet() {
        super();
    }

    @Override
    public String getName() {
        return "Faucet";
    }

    @Override
    public synchronized void refresh() {
        if (ticks % 3 == 0) {
            for (int x = 0; x < dimension.x; x++) {
                for (int z = 0; z < dimension.z; z++) {
                    for (int y = 1; y < dimension.y; y++) {
                        ledManager.setLEDColor(x, y - 1, z, ledManager.getLEDColor(x, y, z));
                        ledManager.setLEDColor(x, y, z, new Color());
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
}
