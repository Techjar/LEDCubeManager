
package com.techjar.ledcm.render.pipeline;

import com.hackoeur.jglm.Matrices;
import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.render.InstancedRenderer;
import com.techjar.ledcm.util.ShaderProgram;
import com.techjar.ledcm.util.Util;

/**
 *
 * @author Techjar
 */
public class RenderPipelineStandard implements RenderPipeline {
	private ShaderProgram mainShader;

	@Override
	public void init() {
	}

	@Override
	public void update(float delta) {
	}

	@Override
	public void render3D() {
		LEDCubeManager.getInstance().faceCount = 0;
		LEDCubeManager ledcm = LEDCubeManager.getInstance();

		// Setup projection matrix
		ledcm.setupView(Util.convertMatrix(Matrices.perspective(ledcm.getFieldOfView(), (float)LEDCubeManager.getDisplayMode().getWidth() / (float)LEDCubeManager.getDisplayMode().getHeight(), ledcm.getNearClip(), ledcm.getViewDistance())), LEDCubeManager.getCamera().getPosition(), LEDCubeManager.getCamera().getAngle());

		mainShader.use();
		ledcm.sendMatrixToProgram();
		ledcm.getLightingHandler().sendToShader();

		InstancedRenderer.prepareItems();
		LEDCubeManager.getInstance().faceCount += InstancedRenderer.renderAll()[1];
		ShaderProgram.useNone();
	}

	@Override
	public void render2D() {
	}

	@Override
	public void loadShaders() {
		mainShader = new ShaderProgram().loadShader("main").link();
	}
}
