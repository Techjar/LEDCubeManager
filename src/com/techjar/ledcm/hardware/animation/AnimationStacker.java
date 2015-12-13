
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Timer;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class AnimationStacker extends Animation {
    private long layers;
    private long curLayer;
    private long topLayer;
    private long allLayers;

    public AnimationStacker() {
        super();
        topLayer = 1L << (dimension.y - 1);
        allLayers = (long)Math.pow(2, dimension.y) - 1;
    }

    @Override
    public String getName() {
        return "Stacker";
    }

    @Override
    public synchronized void refresh() {
        if (ticks % 3 == 0) {
            if (layers != allLayers) {
                if (curLayer == 0) {
                    curLayer = topLayer;
                } else if ((layers | (curLayer >>> 1)) == layers) {
                    layers |= curLayer;
                    curLayer = 0;
                } else {
                    curLayer >>>= 1;
                }
            }
            for (int y = 0; y < dimension.y; y++) {
                Color color = checkBit(layers | curLayer, y) ? LEDCubeManager.getPaintColor() : new Color();
                for (int x = 0; x < dimension.x; x++) {
                    for (int z = 0; z < dimension.z; z++) {
                        ledManager.setLEDColor(x, y, z, color);
                    }
                }
            }
        }
    }

    @Override
    public synchronized void reset() {
        layers = 0;
    }

    private boolean checkBit(long number, int bit) {
        return (number & (1L << bit)) != 0;
    }
}
