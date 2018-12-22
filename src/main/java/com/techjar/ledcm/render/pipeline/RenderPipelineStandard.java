
package com.techjar.ledcm.render.pipeline;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import com.hackoeur.jglm.Matrices;
import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.render.BloomProcessor;
import com.techjar.ledcm.render.InstancedRenderer;
import com.techjar.ledcm.render.camera.RenderCamera;
import com.techjar.ledcm.render.camera.RenderCameraStandard;
import com.techjar.ledcm.util.ShaderProgram;
import com.techjar.ledcm.util.Util;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.DisplayMode;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Techjar
 */
public class RenderPipelineStandard implements RenderPipeline {
	private ShaderProgram mainShader;
	private BloomProcessor bloomProcessor;

	@Override
	public void init() {
		LEDCubeManager ledcm = LEDCubeManager.getInstance();
		DisplayMode displayMode = LEDCubeManager.getDisplayMode();

		if (ledcm.isEnableBloom())
			bloomProcessor = new BloomProcessor(ledcm.getMultisampleFBO(), ledcm.getBloomTexture(), displayMode.getWidth(), displayMode.getHeight());
		LEDCubeManager.getInstance().addRenderCamera(new RenderCameraStandard());
	}

	@Override
	public void changeDisplayMode() {
		LEDCubeManager ledcm = LEDCubeManager.getInstance();
		DisplayMode displayMode = LEDCubeManager.getDisplayMode();

		if (bloomProcessor != null) {
			bloomProcessor.cleanup();
			bloomProcessor = null;
		}
		if (ledcm.isEnableBloom())
			bloomProcessor = new BloomProcessor(ledcm.getMultisampleFBO(), ledcm.getBloomTexture(), displayMode.getWidth(), displayMode.getHeight());
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

		if (bloomProcessor != null)
			bloomProcessor.apply(ledcm.getBloomAmount());
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
		if (bloomProcessor != null) bloomProcessor.loadShaders();
	}
}
