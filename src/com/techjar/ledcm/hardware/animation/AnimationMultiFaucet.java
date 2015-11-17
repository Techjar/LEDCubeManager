
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Vector2;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationMultiFaucet extends Animation {
    private Random random = new Random();
    private List<Vector2> faucets = new ArrayList<>();
    private List<Color> colors = new ArrayList<>();

    public AnimationMultiFaucet() {
        super();
    }

    @Override
    public String getName() {
        return "Multi Faucet";
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
            for (int i = 0; i < faucets.size(); i++) {
                Vector2 pos = faucets.get(i);
                if (random.nextInt(2) == 0) {
                    do {
                        pos.setX(MathHelper.clamp(pos.getX() + (random.nextInt(3) - 1), 0, dimension.x - 1));
                        pos.setY(MathHelper.clamp(pos.getY() + (random.nextInt(3) - 1), 0, dimension.z - 1));
                    } while (isOtherFaucetAt(pos));
                }
                ledManager.setLEDColor((int)pos.getX(), dimension.y - 1, (int)pos.getY(), colors.get(i));
            }
        }
    }

    @Override
    public synchronized void reset() {
        faucets.clear();
        colors.clear();
        for (int i = 0; i < 5; i++) {
            faucets.add(new Vector2(random.nextInt(dimension.x), random.nextInt(dimension.z)));
            colors.add(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
        }
    }

    private boolean isFaucetAt(Vector2 pos) {
        for (Vector2 faucet : faucets) {
            if ((int)faucet.getX() == (int)pos.getX() && (int)faucet.getY() == (int)pos.getY()) return true;
        }
        return false;
    }

    private boolean isOtherFaucetAt(Vector2 pos) {
        for (Vector2 faucet : faucets) {
            if (faucet != pos && (int)faucet.getX() == (int)pos.getX() && (int)faucet.getY() == (int)pos.getY()) return true;
        }
        return false;
    }
}
