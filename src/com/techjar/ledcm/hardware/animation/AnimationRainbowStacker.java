
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Timer;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class AnimationRainbowStacker extends Animation {
    private long layers;
    private long curLayer;
    private int curLayerNum;
    private long topLayer;
    private long allLayers;

    public AnimationRainbowStacker() {
        super();
        topLayer = 1L << (dimension.y - 1);
        allLayers = (long)Math.pow(2, dimension.y) - 1;
    }

    @Override
    public String getName() {
        return "Rainbow Stacker";
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
                    curLayerNum++;
                } else {
                    curLayer >>>= 1;
                }
            }
            for (int y = 0; y < dimension.y; y++) {
                Color color = checkBit(layers, y) ? getColorAtLayer(y) : checkBit(curLayer, y) ? getColorAtLayer(curLayerNum) : new Color();
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
        curLayerNum = 0;
    }

    private boolean checkBit(long number, int bit) {
        return (number & (1L << bit)) != 0;
    }

    private Color getColorAtLayer(int layer) {
        Color color = new Color();
        color.fromHSB((layer / (float)(dimension.y - 1)) * (300F / 360F), 1, 1);
        return color;
    }
}
