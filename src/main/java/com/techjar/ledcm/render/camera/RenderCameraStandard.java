package com.techjar.ledcm.render.camera;

import com.hackoeur.jglm.Matrices;
import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Util;
import org.lwjgl.opengl.Display;

import java.awt.*;

public class RenderCameraStandard implements RenderCamera {
	@Override
	public boolean shouldRender() {
		return Display.isActive() || (LEDCubeManager.getFrame().isVisible() && LEDCubeManager.getFrame().getState() != Frame.ICONIFIED) || LEDCubeManager.getFrameServer().numClients > 0;
	}

	@Override
	public boolean usesMainWindow() {
		return true;
	}

	@Override
	public void setup() {
		LEDCubeManager ledcm = LEDCubeManager.getInstance();

		// Setup projection matrix
		ledcm.setupView(Util.convertMatrix(Matrices.perspective(ledcm.getFieldOfView(), (float)LEDCubeManager.getDisplayMode().getWidth() / (float)LEDCubeManager.getDisplayMode().getHeight(), ledcm.getNearClip(), ledcm.getViewDistance())), LEDCubeManager.getCamera().getPosition(), LEDCubeManager.getCamera().getAngle());
	}
}
