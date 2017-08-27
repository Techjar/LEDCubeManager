package com.techjar.ledcm.render.camera;

public interface RenderCamera {
	boolean shouldRender();
	boolean usesMainWindow();
	void setup();
}
