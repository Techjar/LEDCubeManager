
package com.techjar.ledcm.render.pipeline;

import com.hackoeur.jglm.Matrices;
import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.render.InstancedRenderer;
import com.techjar.ledcm.render.camera.RenderCamera;
import com.techjar.ledcm.render.camera.RenderCameraStandard;
import com.techjar.ledcm.util.ShaderProgram;
import com.techjar.ledcm.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Techjar
 */
public class RenderPipelineStandard implements RenderPipeline {
	private ShaderProgram mainShader;

	@Override
	public void init() {
		LEDCubeManager.getInstance().addRenderCamera(new RenderCameraStandard());
	}

	@Override
	public void update(float delta) {
	}

	@Override
	public void preRender3D() {
		LEDCubeManager ledcm = LEDCubeManager.getInstance();
		mainShader.use();
		ledcm.getLightingHandler().sendToShader();
		ledcm.faceCount = 0;
	}

	@Override
	public void render3D() {
		LEDCubeManager ledcm = LEDCubeManager.getInstance();

		mainShader.use();
		ledcm.sendMatrixToProgram();

		InstancedRenderer.prepareItems();
		ledcm.faceCount += InstancedRenderer.renderAll()[1];
		ShaderProgram.useNone();
	}

	@Override
	public void postRender3D() {
	}

	@Override
	public void render2D() {
	}

	@Override
	public void loadShaders() {
		mainShader = new ShaderProgram().loadShader("main").link();
	}
}
