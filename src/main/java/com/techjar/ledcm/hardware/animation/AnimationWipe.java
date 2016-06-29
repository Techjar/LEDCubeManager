
package com.techjar.ledcm.hardware.animation;

import com.flowpowered.noise.NoiseQuality;
import com.flowpowered.noise.module.source.Perlin;
import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector2;
import com.techjar.ledcm.util.Vector3;
import java.util.Random;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;
import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author Techjar
 */
public class AnimationWipe extends Animation {
    private int speed = 3;
    private int type = 0;
    private int wipeLevel = 0;
    private boolean wipeState;

    public AnimationWipe() {
        super();
    }

    @Override
    public String getName() {
        return "Wipe";
    }

    @Override
    public synchronized void refresh() {
        if (wipeLevel > -1 && ticks % speed == 0) {
            for (int x = 0; x < dimension.x; x++) {
                for (int z = 0; z < dimension.z; z++) {
                    if (type == 0 || x == 0 || x == dimension.x - 1 || z == 0 || z == dimension.z - 1) {
                        ledManager.setLEDColor(x, wipeLevel, z, wipeState ? LEDCubeManager.getPaintColor() : ReadableColor.BLACK);
                    } else if (!wipeState) {
                        ledManager.setLEDColor(x, wipeLevel, z, ReadableColor.BLACK);
                    }
                }
            }
            if (++wipeLevel >= dimension.y) {
                if (wipeState) {
                    wipeLevel = 0;
                    wipeState = false;
                } else {
                    wipeLevel = -1;
                }
            }
        }
    }

    @Override
    public synchronized void reset() {
        wipeLevel = 0;
        wipeState = true;
    }

    @Override
    public AnimationOption[] getOptions() {
        return new AnimationOption[]{
            new AnimationOption("type", "Type", AnimationOption.OptionType.COMBOBOX, new Object[]{type, 0, "Solid", 1, "Hollow"}),
            new AnimationOption("speed", "Speed", AnimationOption.OptionType.SLIDER, new Object[]{(19 - (speed - 1)) / 19F, 1F / 19F}),
        };
    }

    @Override
    public synchronized void optionChanged(String name, String value) {
        switch (name) {
            case "type":
                type = Integer.parseInt(value);
                reset();
                break;
            case "speed":
                speed = 1 + (19 - Math.round(19 * Float.parseFloat(value)));
                break;
        }
    }

    @Override
    public boolean isFinished() {
        return wipeLevel < 0;
    }
}
