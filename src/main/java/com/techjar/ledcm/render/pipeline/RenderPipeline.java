
package com.techjar.ledcm.render.pipeline;

/**
 *
 * @author Techjar
 */
public interface RenderPipeline {
	public void update();
	public void render3D();
	public void render2D();
	public void loadShaders();
}
