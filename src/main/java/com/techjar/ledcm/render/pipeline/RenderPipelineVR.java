package com.techjar.ledcm.render.pipeline;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.techjar.ledcm.hardware.LEDArray;
import com.techjar.ledcm.render.BloomProcessor;
import com.techjar.ledcm.render.LightingHandler;
import com.techjar.ledcm.render.RenderHelper;
import com.techjar.ledcm.render.camera.RenderCamera;
import com.techjar.ledcm.render.camera.RenderCameraVR;
import com.techjar.ledcm.util.*;
import com.techjar.ledcm.util.math.Dimension3D;
import com.techjar.ledcm.util.math.Quaternion;
import com.techjar.ledcm.util.math.Vector2;
import com.techjar.ledcm.util.math.Vector3;
import com.techjar.ledcm.vr.VRRenderModel;
import javafx.scene.effect.Bloom;
import jopenvr.OpenVRUtil;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.Color;
import org.lwjgl.util.Dimension;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.hackoeur.jglm.Matrices;
import com.techjar.ledcm.LEDCube;
import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.render.InstancedRenderer;
import com.techjar.ledcm.util.logging.LogHelper;
import com.techjar.ledcm.vr.VRProvider;
import com.techjar.ledcm.vr.VRProvider.ControllerType;
import com.techjar.ledcm.vr.VRStereoProvider;
import com.techjar.ledcm.vr.VRStereoProvider.EyeType;
import com.techjar.ledcm.vr.VRTrackedController;

public class RenderPipelineVR implements RenderPipeline {
	private ShaderProgram mainShader;
	private ShaderProgram noLightingShader;
	private Model roomModel;
	private Model guiModel;
	private int fboLeftEye;
	private int fboRightEye;
	private int fboGui;
	private int rbLeftEye;
	private int rbRightEye;
	private int rbGui;
	private int texGui;
	private int texBloomLeftEye;
	private int texBloomRightEye;
	private BloomProcessor bloomProcessorEye;
	private BloomProcessor bloomProcessorCenter;
	private RenderPipeline guiPipeline;
	private Vector3 cubePos = new Vector3(0, 1, 0);
	InstancedRenderer.InstanceItem playAreaInstance;
	InstancedRenderer.InstanceItem roomModelInstance;
	private Map<VRRenderModel, InstancedRenderer.InstanceItem> leftModels;
	private Map<VRRenderModel, InstancedRenderer.InstanceItem> rightModels;

	@Override
	public void init() {
		guiPipeline = new RenderPipelineGUI();
		leftModels = new HashMap<>();
		rightModels = new HashMap<>();

		setupFramebuffers();
		//VRProvider.getStereoProvider().setRenderScale(new Vector3(100, 100, 100));
		LEDCubeManager ledcm = LEDCubeManager.getInstance();
		LEDCube ledCube = LEDCubeManager.getLEDCube();
		ledCube.setRenderOffset(Util.convertVector(ledCube.getCenterPoint()).negate().multiply(ledCube.getSpaceMult()).add(cubePos));
		guiModel = LEDCubeManager.getModelManager().getModel("gui.model");

		ledcm.addRenderCamera(new RenderCameraVR(EyeType.LEFT, fboLeftEye));
		ledcm.addRenderCamera(new RenderCameraVR(EyeType.RIGHT, fboRightEye));
		ledcm.addRenderCamera(new RenderCameraVR(EyeType.CENTER, 0));

		ledcm.addResizeHandler(this::setupGUIFramebuffer);

		LightingHandler lightingHandler = ledcm.getLightingHandler();
		LightSource light = lightingHandler.getLight(0);
		light.position = new Vector4f(0, 1, 0, 1);
		light.constantAttenuation = 0.3F;
		light.linearAttenuation = 0.6F;
		light.quadraticAttenuation = 0.1F;
		//light.brightness = 0;
		/*LightSource light = new LightSource();
		light.position = new Vector4f(2, 2.3F, 2, 1);
		light.brightness = 0.5F;
		lightingHandler.addLight(light);
		light = new LightSource();
		light.position = new Vector4f(-2, 2.3F, 2, 1);
		light.brightness = 0.5F;
		lightingHandler.addLight(light);
		light = new LightSource();
		light.position = new Vector4f(2, 2.3F, -2, 1);
		light.brightness = 0.5F;
		lightingHandler.addLight(light);
		light = new LightSource();
		light.position = new Vector4f(-2, 2.3F, -2, 1);
		light.brightness = 0.5F;
		lightingHandler.addLight(light);*/
		light = new LightSource();
		light.position = new Vector4f(2, 2.3F, 0, 1);
		light.brightness = 0.75F;
		lightingHandler.addLight(light);
		light = new LightSource();
		light.position = new Vector4f(-2, 2.3F, 0, 1);
		light.brightness = 0.75F;
		lightingHandler.addLight(light);

		if (ledcm.isEnableBloom()) {
			Dimension texSize = VRProvider.getEyeTextureSize();
			DisplayMode displayMode = LEDCubeManager.getDisplayMode();
			bloomProcessorEye = new BloomProcessor(0, 0, texSize.getWidth(), texSize.getHeight());
			bloomProcessorCenter = new BloomProcessor(ledcm.getMultisampleFBO(), ledcm.getBloomTexture(), displayMode.getWidth(), displayMode.getHeight());
		}
	}

	@Override
	public void changeDisplayMode() {
		LEDCubeManager ledcm = LEDCubeManager.getInstance();

		if (bloomProcessorEye != null) {
			bloomProcessorEye.cleanup();
			bloomProcessorCenter.cleanup();
			bloomProcessorEye = null;
			bloomProcessorCenter = null;
		}
		if (ledcm.isEnableBloom()) {
			Dimension texSize = VRProvider.getEyeTextureSize();
			DisplayMode displayMode = LEDCubeManager.getDisplayMode();
			bloomProcessorEye = new BloomProcessor(0, 0, texSize.getWidth(), texSize.getHeight());
			bloomProcessorCenter = new BloomProcessor(ledcm.getMultisampleFBO(), ledcm.getBloomTexture(), displayMode.getWidth(), displayMode.getHeight());
		}
	}

	@Override
	public void update(float delta) {
		LEDCubeManager ledcm = LEDCubeManager.getInstance();
		Vector3 hmdPos = VRProvider.getHMDPosition();
		//ledcm.getLightingHandler().getLight(0).position = new Vector4f(hmdPos.getX(), hmdPos.getY(), hmdPos.getZ(), 1);
		LEDCubeManager.getCamera().setPosition(hmdPos);
		LEDCubeManager.getCamera().setAngle(OpenVRUtil.getEulerAnglesDegYXZ(Util.convertQuaternion(VRProvider.getHMDRotation())));
		roomModel = LEDCubeManager.getModelManager().getModel("room.model");
		Vector3f averageColor = getAverageCubeColor();
		ledcm.getLightingHandler().getLight(0).diffuse = averageColor;
		ledcm.getLightingHandler().getLight(0).specular = averageColor;
	}

	@Override
	public void preRender3D() {
		LEDCubeManager ledcm = LEDCubeManager.getInstance();
		DisplayMode displayMode = LEDCubeManager.getDisplayMode();
		Vector2 playArea = VRProvider.getPlayAreaSize();
		VRStereoProvider stereoProvider = VRProvider.getStereoProvider();
		VRTrackedController leftController = VRProvider.getController(ControllerType.LEFT);
		VRTrackedController rightController = VRProvider.getController(ControllerType.RIGHT);

		if (ledcm.isShowingVRGUI()) {
			ledcm.resizeGL(displayMode.getWidth(), displayMode.getHeight());
			glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fboGui);
			glClearColor(0, 0, 0, 0);
			glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();
			glOrtho(0, displayMode.getWidth(), displayMode.getHeight(), 0, -1, 1);
			glMatrixMode(GL_MODELVIEW);
			glLoadIdentity();
			glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE);
			glDisable(GL_LIGHTING);
			glDisable(GL_DEPTH_TEST);
			glBindTexture(GL_TEXTURE_2D, 0);
			if (!ledcm.isShowingGUI()) {
				ledcm.setShowingGUI(true);
				guiPipeline.render2D();
				ledcm.setShowingGUI(false);
			} else {
				guiPipeline.render2D();
			}
			if (ledcm.getMouseOverride() != null) {
				Vector2 mouse = ledcm.getMouseOverride();
				glBlendFuncSeparate(GL_ONE_MINUS_DST_COLOR, GL_ZERO, GL_ONE, GL_ONE);
				RenderHelper.drawSquare(mouse.getX() - 10, mouse.getY() - 10, 20, 20, new Color(255, 255, 255));
			}
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			glEnable(GL_LIGHTING);
			glEnable(GL_DEPTH_TEST);
		}

		renderController(leftController, leftModels);
		renderController(rightController, rightModels);
		if (playArea != null) {
			if (playAreaInstance == null) {
				playAreaInstance = LEDCubeManager.getModelManager().getModel("playarea.model").render(VRProvider.getRoomPosition().add(VRProvider.getRoomRotation().up().multiply(0.001F)), VRProvider.getRoomRotation(), new Color(255, 255, 255, 100), new Vector3(playArea.getX(), 1, playArea.getY()).multiply(stereoProvider.getWorldScale()));
			} else {
				playAreaInstance.setTransform(VRProvider.getRoomPosition().add(VRProvider.getRoomRotation().up().multiply(0.001F)), VRProvider.getRoomRotation());
				playAreaInstance.setScale(new Vector3(playArea.getX(), 1, playArea.getY()).multiply(stereoProvider.getWorldScale()));
			}
		} else if (playAreaInstance != null) {
			InstancedRenderer.removeItem(playAreaInstance);
			playAreaInstance = null;
		}
		if (roomModelInstance == null) {
			roomModelInstance = roomModel.render(new Vector3(), new Quaternion(), new Color(200, 200, 200), stereoProvider.getWorldScale());
		}

		mainShader.use();
		ledcm.getLightingHandler().sendToShader();
		ledcm.faceCount = 0;
	}

	@Override
	public void render3D() {
		LEDCubeManager ledcm = LEDCubeManager.getInstance();
		VRTrackedController leftController = VRProvider.getController(ControllerType.LEFT);

		mainShader.use();
		ledcm.sendMatrixToProgram();

		glClearColor(0, 0, 0, 1);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		InstancedRenderer.prepareItems();
		ledcm.faceCount += InstancedRenderer.renderAll()[1];

		if (ledcm.isShowingVRGUI() && leftController.isTracking()) {
			noLightingShader.use();
			ledcm.sendMatrixToProgram();
			drawGUI(leftController);
		}

		if (bloomProcessorEye != null && ledcm.getCurrentCamera() instanceof RenderCameraVR) {
			VRStereoProvider.EyeType eye = ((RenderCameraVR)ledcm.getCurrentCamera()).eye;
			BloomProcessor bloomProcessor = null;
			switch (eye) {
				case LEFT:
					bloomProcessor = bloomProcessorEye;
					bloomProcessorEye.setBaseFramebuffer(fboLeftEye);
					bloomProcessorEye.setBloomTexture(texBloomLeftEye);
					break;
				case RIGHT:
					bloomProcessor = bloomProcessorEye;
					bloomProcessorEye.setBaseFramebuffer(fboRightEye);
					bloomProcessorEye.setBloomTexture(texBloomRightEye);
					break;
				case CENTER:
					bloomProcessor = bloomProcessorCenter;
					break;
			}

			bloomProcessor.apply(ledcm.getBloomAmount());
		}
	}

	@Override
	public void postRender3D() {
		ShaderProgram.useNone();
		VRProvider.getStereoProvider().submitFrame();
	}

	@Override
	public void render2D() {
	}

	@Override
	public void loadShaders() {
		mainShader = new ShaderProgram().loadShader("main").link();
		noLightingShader = new ShaderProgram().loadShader("main_nolighting").link();
		if (bloomProcessorEye != null) {
			bloomProcessorEye.loadShaders();
			bloomProcessorCenter.loadShaders();
		}
	}

	protected void setupFramebuffers() {
		Dimension texSize = VRProvider.getEyeTextureSize();
		VRStereoProvider stereoProvider = VRProvider.getStereoProvider();
		fboLeftEye = glGenFramebuffers();
		rbLeftEye = glGenRenderbuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, fboLeftEye);
		glBindRenderbuffer(GL_RENDERBUFFER, rbLeftEye);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, texSize.getWidth(), texSize.getHeight());
		glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, stereoProvider.getEyeTextureIdLeft(), 0);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, rbLeftEye);

		texBloomLeftEye = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, texBloomLeftEye);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, texSize.getWidth(), texSize.getHeight(), 0, GL_RGB, GL_FLOAT, (ByteBuffer)null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, texBloomLeftEye, 0);

		{
			IntBuffer buf = BufferUtils.createIntBuffer(2);
			buf.put(GL_COLOR_ATTACHMENT0);
			buf.put(GL_COLOR_ATTACHMENT1);
			buf.flip();
			glDrawBuffers(buf);
		}

		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
			throw new RuntimeException("Framebuffer is invalid.");

		fboRightEye = glGenFramebuffers();
		rbRightEye = glGenRenderbuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, fboRightEye);
		glBindRenderbuffer(GL_RENDERBUFFER, rbRightEye);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, texSize.getWidth(), texSize.getHeight());
		glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, stereoProvider.getEyeTextureIdRight(), 0);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, rbRightEye);

		texBloomRightEye = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, texBloomRightEye);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, texSize.getWidth(), texSize.getHeight(), 0, GL_RGB, GL_FLOAT, (ByteBuffer)null);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, texBloomRightEye, 0);

		{
			IntBuffer buf = BufferUtils.createIntBuffer(2);
			buf.put(GL_COLOR_ATTACHMENT0);
			buf.put(GL_COLOR_ATTACHMENT1);
			buf.flip();
			glDrawBuffers(buf);
		}

		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
			throw new RuntimeException("Framebuffer is invalid.");

		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glBindRenderbuffer(GL_RENDERBUFFER, 0);
		LogHelper.info("Set up VR framebuffers.");

		if (stereoProvider.hasStencilMask()) {
			glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fboLeftEye);
			drawStencil(EyeType.LEFT);
			glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fboRightEye);
			drawStencil(EyeType.RIGHT);
			glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
			LogHelper.info("Set up stencil mask.");
		}

		setupGUIFramebuffer();
	}

	protected void setupGUIFramebuffer() {
		DisplayMode displayMode = LEDCubeManager.getDisplayMode();
		if (fboGui != 0) {
			glDeleteFramebuffers(fboGui);
			glDeleteRenderbuffers(rbGui);
			glDeleteTextures(texGui);
			LogHelper.info("Deleted GUI framebuffer.");
		}
		fboGui = glGenFramebuffers();
		rbGui = glGenRenderbuffers();
		texGui = glGenTextures();
		glBindFramebuffer(GL_FRAMEBUFFER, fboGui);
		glBindRenderbuffer(GL_RENDERBUFFER, rbGui);
		glBindTexture(GL_TEXTURE_2D, texGui);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, displayMode.getWidth(), displayMode.getHeight(), 0, GL_RGBA, GL_INT, (ByteBuffer)null);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, displayMode.getWidth(), displayMode.getHeight());
		glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, texGui, 0);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, rbGui);
		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
			throw new RuntimeException("Framebuffer is invalid.");
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glBindRenderbuffer(GL_RENDERBUFFER, 0);
		glBindTexture(GL_TEXTURE_2D, 0);
		LogHelper.info("Set up GUI framebuffer.");
	}

	protected void drawStencil(EyeType eye) {
		Dimension texSize = VRProvider.getEyeTextureSize();
		Vector2[] vertices = VRProvider.getStereoProvider().getStencilMask(eye);

		glEnable(GL_STENCIL_TEST);
		glDisable(GL_CULL_FACE);
		glDisable(GL_DEPTH_TEST);
		glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
		glStencilMask(0xFF);
		glClear(GL_STENCIL_BUFFER_BIT); // Clear stencil buffer (0 by default)
		glStencilFunc(GL_ALWAYS, 0xFF, 0xFF);
		glColor3f(0, 0, 0);
		glDepthMask(false);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		glOrtho(0, texSize.getWidth(), texSize.getHeight(), 0, -10, 10);
		glViewport(0, 0, texSize.getWidth(), texSize.getHeight());

		glBegin(GL_TRIANGLES);
		for (int i = 0; i < vertices.length; i++) {
			glVertex2f(vertices[i].getX(), vertices[i].getY());
		}
		glEnd();

		glStencilFunc(GL_NOTEQUAL, 0xFF, 1);
		glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
		glDepthMask(true); // Do write to depth buffer
		glEnable(GL_CULL_FACE);
		glEnable(GL_DEPTH_TEST);
		glStencilMask(0x00); // Don't write to stencil buffer
	}

	protected void drawGUI(VRTrackedController controller) {
		Vector2 size = LEDCubeManager.getInstance().getVRGUISize();
		glDisable(GL_CULL_FACE);
		guiModel.render(controller.getPosition().add(controller.getRotation().inverse().forward().multiply(0.5F * size.getY() + 0.05F)), controller.getRotation(), new Color(255, 255, 255), new Vector3(size.getX(), 1, size.getY()), false, false, texGui);
		glEnable(GL_CULL_FACE);
	}

	protected Vector3f getAverageCubeColor() {
		LEDArray ledArray = LEDCubeManager.getLEDCube().getLEDManager().getLEDArray();
		Dimension3D dim = LEDCubeManager.getLEDCube().getLEDManager().getDimensions();
		float totalRed = 0;
		float totalGreen = 0;
		float totalBlue = 0;
		for (int x = 0; x < dim.x; x++) {
			for (int y = 0; y < dim.y; y++) {
				for (int z = 0; z < dim.z; z++) {
					Color color = ledArray.getLEDColor(x, y, z);
					totalRed += color.getRed() / 255F;
					totalGreen += color.getGreen() / 255F;
					totalBlue += color.getBlue() / 255F;
				}
			}
		}
		float count = LEDCubeManager.getLEDCube().getLEDManager().getLEDCount();
		float gamma = 1.0F / 2.2F;
		return new Vector3f((float)Math.pow(totalRed / count, gamma), (float)Math.pow(totalGreen / count, gamma), (float)Math.pow(totalBlue / count, gamma));
	}

	protected void renderController(VRTrackedController controller, Map<VRRenderModel, InstancedRenderer.InstanceItem> modelsMap) {
		VRStereoProvider stereoProvider = VRProvider.getStereoProvider();
		VRRenderModel[] models = controller.getRenderModels();
		if (controller.isTracking() && models != null) {
			Vector3 controllerPos = controller.getPosition();
			Quaternion controllerRot = controller.getRotation();
			for (VRRenderModel model : models) {
				if (model.visible) {
					Matrix4f matrix = new Matrix4f();
					matrix.translate(Util.convertVector(controllerPos));
					Matrix4f.mul(matrix, controllerRot.getMatrix(), matrix);
					Matrix4f.mul(matrix, model.transform, matrix);
					if (modelsMap.containsKey(model)) {
						InstancedRenderer.InstanceItem item = modelsMap.get(model);
						item.setTransform(matrix);
						item.setScale(stereoProvider.getWorldScale());
					} else {
						modelsMap.put(model, model.model.render(matrix, new Color(255, 255, 255), stereoProvider.getWorldScale()));
					}
				} else if (modelsMap.containsKey(model)) {
					InstancedRenderer.removeItem(modelsMap.remove(model));
				}
			}
		} else {
			modelsMap.values().forEach(InstancedRenderer::removeItem);
			modelsMap.clear();
		}
	}

	/*protected Model pickRoomModel() {
		Vector2 playArea = VRProvider.getPlayAreaSize();
		ModelManager modelManager = LEDCubeManager.getModelManager();
		if (playArea != null) {
			float maxSize = Math.max(playArea.getX(), playArea.getY());
			if (maxSize - 2.0F < 0.01F) return modelManager.getModel("room_small.model");
			if (maxSize - 3.0F < 0.01F) return modelManager.getModel("room_medium.model");
		}
		return modelManager.getModel("room_large.model");
	}*/
}
