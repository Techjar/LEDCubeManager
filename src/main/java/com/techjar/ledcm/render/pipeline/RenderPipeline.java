
package com.techjar.ledcm.render.pipeline;

/**
 *
 * @author Techjar
 */
public interface RenderPipeline {
    public int get3DPasses();
    public int get2DPasses();
    public void render3D(int pass);
    public void render2D(int pass);
    public void loadShaders();
}
