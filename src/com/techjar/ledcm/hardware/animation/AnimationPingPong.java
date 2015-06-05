
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Direction;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector3;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class AnimationPingPong extends Animation {
    private int position;
    private boolean direction;

    public AnimationPingPong() {
        super();
    }

    @Override
    public String getName() {
        return "Ping Pong";
    }

    @Override
    public void refresh() {
        if (ticks % 1 == 0) {
            ledManager.setLEDColor(direction ? position++ : position--, 0, 0, new Color());
            ledManager.setLEDColor(position, 0, 0, LEDCubeManager.getPaintColor());
            if (position == 0 || position == dimension.x - 1) direction = !direction;
        }
    }

    @Override
    public void reset() {
        position = 0;
        direction = true;
        ledManager.setLEDColor(position, 0, 0, LEDCubeManager.getPaintColor());
    }
}
