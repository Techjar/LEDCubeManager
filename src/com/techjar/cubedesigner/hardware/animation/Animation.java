
package com.techjar.cubedesigner.hardware.animation;

import com.techjar.cubedesigner.CubeDesigner;
import com.techjar.cubedesigner.hardware.LEDManager;
import com.techjar.cubedesigner.util.Dimension3D;
import com.techjar.cubedesigner.util.Vector3;

/**
 *
 * @author Techjar
 */
public abstract class Animation {
    protected final LEDManager ledManager;
    protected final Dimension3D dimension;

    public Animation() {
        this.ledManager = CubeDesigner.getLEDManager();
        this.dimension = this.ledManager.getDimensions();
    }

    public abstract String getName();
    public abstract void refresh();
    public abstract void reset();
    public void setParameters(String[] args){}
}
