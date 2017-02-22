package com.techjar.ledcm.render.camera;

import com.hackoeur.jglm.Matrices;
import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Util;

public class RenderCameraStandard implements RenderCamera {
	@Override
	public void setup() {
		LEDCubeManager ledcm = LEDCubeManager.getInstance();

		// Setup projection matrix
		ledcm.setupView(Util.convertMatrix(Matrices.perspective(ledcm.getFieldOfView(), (float)LEDCubeManager.getDisplayMode().getWidth() / (float)LEDCubeManager.getDisplayMode().getHeight(), ledcm.getNearClip(), ledcm.getViewDistance())), LEDCubeManager.getCamera().getPosition(), LEDCubeManager.getCamera().getAngle());
	}
}
