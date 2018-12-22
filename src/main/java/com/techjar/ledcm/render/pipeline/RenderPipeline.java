
package com.techjar.ledcm.render.pipeline;

import com.techjar.ledcm.render.camera.RenderCamera;

import java.util.List;

/**
 *
 * @author Techjar
 */
public interface RenderPipeline {
	public void init();
	public void changeDisplayMode();
	public void update(float delta);
	public void preRender3D();
	public void render3D();
	public void postRender3D();
	public void render2D();
	public void loadShaders();
}
