
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.LEDManager;
import com.techjar.ledcm.util.Dimension3D;
import com.techjar.ledcm.util.Vector3;

/**
 *
 * @author Techjar
 */
public abstract class Animation {
    protected final LEDManager ledManager;
    protected final Dimension3D dimension;
    protected long ticks;

    public Animation() {
        this.ledManager = LEDCubeManager.getLEDManager();
        this.dimension = this.ledManager.getDimensions();
    }

    public abstract String getName();
    public abstract void refresh();
    public abstract void reset();
    
    public AnimationOption[] getOptions() {
        return new AnimationOption[0];
    }

    public void optionChanged(String name, Object value) {
    }

    public void setParameters(String[] args) {
    }

    public void incTicks() {
        ticks++;
    }
}
