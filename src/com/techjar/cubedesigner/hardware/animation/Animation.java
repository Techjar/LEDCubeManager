
package com.techjar.cubedesigner.hardware.animation;

import com.techjar.cubedesigner.CubeDesigner;
import com.techjar.cubedesigner.hardware.LEDManager;

/**
 *
 * @author Techjar
 */
public abstract class Animation {
    protected final LEDManager ledManager;

    public Animation() {
        this.ledManager = CubeDesigner.getLEDManager();
    }

    public abstract String getName();
    public abstract void refresh();
    public abstract void reset();
}
