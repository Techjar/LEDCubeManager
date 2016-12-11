package com.techjar.ledcm.render.pipeline;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;

import java.nio.ByteBuffer;
import java.util.Random;

import com.techjar.ledcm.gui.GUICallback;
import com.techjar.ledcm.hardware.LEDArray;
import com.techjar.ledcm.hardware.manager.LEDManager;
import com.techjar.ledcm.render.LightingHandler;
import com.techjar.ledcm.render.RenderHelper;
import com.techjar.ledcm.util.*;
import com.techjar.ledcm.vr.VRRenderModel;
import jopenvr.OpenVRUtil;

import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.Color;
import org.lwjgl.util.Dimension;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.hackoeur.jglm.Matrices;
import com.techjar.ledcm.LEDCube;
import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.ModelManager;
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
	private Model controllerModel;
	private Model roomModel;
	private Model guiModel;
	private int fboLeftEye;
	private int fboRightEye;
	private int fboGui;
	private int rbLeftEye;
	private int rbRightEye;
	private int rbGui;
	private int texGui;
	private RenderPipeline guiPipeline;
	private Vector3 cubePos = new Vector3(0, 1, 0);
	Timer timer = new Timer();

	@Override
	public void init() {
		guiPipeline = new RenderPipelineGUI();
		setupFramebuffers();
		//VRProvider.getStereoProvider().setRenderScale(new Vector3(100, 100, 100));
		LEDCubeManager ledcm = LEDCubeManager.getInstance();
		LEDCube ledCube = LEDCubeManager.getLEDCube();
		ledCube.setRenderOffset(Util.convertVector(ledCube.getCenterPoint()).negate().multiply(ledCube.getSpaceMult()).add(cubePos));
		controllerModel = LEDCubeManager.getModelManager().getModel("vive_controller.model");
		guiModel = LEDCubeManager.getModelManager().getModel("gui.model");

		ledcm.addResizeHandler(() -> setupGUIFramebuffer());

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
	public void render2D() {
	}

	@Override
	public void render3D() {
		Dimension texSize = VRProvider.getEyeTextureSize();
		DisplayMode displayMode = LEDCubeManager.getDisplayMode();
		Vector2 playArea = VRProvider.getPlayAreaSize();
		VRStereoProvider stereoProvider = VRProvider.getStereoProvider();
		LEDCubeManager ledcm = LEDCubeManager.getInstance();
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

		if (leftController.isTracking()) {
			//controllerModel.render(leftController.getPosition(), leftController.getRotation(), new Color(255, 255, 255), stereoProvider.getWorldScale());
			VRRenderModel[] models = leftController.getRenderModels();
			if (models != null) {
				Vector3 controllerPos = leftController.getPosition();
				Quaternion controllerRot = leftController.getRotation();
				for (VRRenderModel model : models) {
					if (model.visible) {
						Matrix4f matrix = new Matrix4f();
						matrix.translate(Util.convertVector(controllerPos));
						Matrix4f.mul(matrix, controllerRot.getMatrix(), matrix);
						Matrix4f.mul(matrix, model.transform, matrix);
						model.model.render(matrix, new Color(255, 255, 255), stereoProvider.getWorldScale());
					}
				}
			}
		}
		if (rightController.isTracking()) {
			//controllerModel.render(rightController.getPosition(), rightController.getRotation(), new Color(255, 255, 255), stereoProvider.getWorldScale());
			VRRenderModel[] models = rightController.getRenderModels();
			if (models != null) {
				Vector3 controllerPos = rightController.getPosition();
				Quaternion controllerRot = rightController.getRotation();
				for (VRRenderModel model : models) {
					if (model.visible) {
						Matrix4f matrix = new Matrix4f();
						matrix.translate(Util.convertVector(controllerPos));
						Matrix4f.mul(matrix, controllerRot.getMatrix(), matrix);
						Matrix4f.mul(matrix, model.transform, matrix);
						model.model.render(matrix, new Color(255, 255, 255), stereoProvider.getWorldScale());
					}
				}
			}
		}
		if (playArea != null)
			LEDCubeManager.getModelManager().getModel("playarea.model").render(VRProvider.getRoomPosition().add(VRProvider.getRoomRotation().up().multiply(0.001F)), VRProvider.getRoomRotation(), new Color(255, 255, 255, 100), new Vector3(playArea.getX(), 1, playArea.getY()).multiply(stereoProvider.getWorldScale()));
		roomModel.render(new Vector3(), new Quaternion(), new Color(200, 200, 200), stereoProvider.getWorldScale());

		Matrix4f view = getView(EyeType.CENTER);
		Matrix4f leftView = getView(EyeType.LEFT);
		Matrix4f rightView = getView(EyeType.RIGHT);
		LEDCubeManager.getInstance().faceCount = 0;

		mainShader.use();
		ledcm.getLightingHandler().sendToShader();
		ledcm.resizeGL(texSize.getWidth(), texSize.getHeight());
		glClearColor(0, 0, 0, 1);

		ledcm.setupView(stereoProvider.getProjectionMatrix(0, ledcm.getNearClip(), ledcm.getViewDistance()), leftView);
		ledcm.sendMatrixToProgram();
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fboLeftEye);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glEnable(GL_STENCIL_TEST);
		InstancedRenderer.prepareItems();
		LEDCubeManager.getInstance().faceCount += InstancedRenderer.renderAll()[1];
		if (ledcm.isShowingVRGUI() && leftController.isTracking()) {
			noLightingShader.use();
			ledcm.sendMatrixToProgram();
			drawGUI(leftController);
			mainShader.use();
		}

		ledcm.setupView(stereoProvider.getProjectionMatrix(1, ledcm.getNearClip(), ledcm.getViewDistance()), rightView);
		ledcm.sendMatrixToProgram();
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fboRightEye);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		InstancedRenderer.prepareItems();
		LEDCubeManager.getInstance().faceCount += InstancedRenderer.renderAll()[1];
		if (ledcm.isShowingVRGUI() && leftController.isTracking()) {
			noLightingShader.use();
			ledcm.sendMatrixToProgram();
			drawGUI(leftController);
			mainShader.use();
		}

		ledcm.resizeGL(displayMode.getWidth(), displayMode.getHeight());
		ledcm.setupView(Util.convertMatrix(Matrices.perspective(ledcm.getFieldOfView(), (float)LEDCubeManager.getDisplayMode().getWidth() / (float)LEDCubeManager.getDisplayMode().getHeight(), ledcm.getNearClip(), ledcm.getViewDistance())), view);
		ledcm.sendMatrixToProgram();
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, ledcm.isAntiAliasing() ? ledcm.getMultisampleFBO() : 0);
		glDisable(GL_STENCIL_TEST);
		InstancedRenderer.prepareItems();
		LEDCubeManager.getInstance().faceCount += InstancedRenderer.renderAll()[1];
		if (ledcm.isShowingVRGUI() && leftController.isTracking()) {
			noLightingShader.use();
			ledcm.sendMatrixToProgram();
			drawGUI(leftController);
			mainShader.use();
		}

		ShaderProgram.useNone();
		stereoProvider.submitFrame();
	}

	@Override
	public void loadShaders() {
		mainShader = new ShaderProgram().loadShader("main").link();
		noLightingShader = new ShaderProgram().loadShader("main_nolighting").link();
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
		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
			throw new RuntimeException("Framebuffer is invalid.");
		fboRightEye = glGenFramebuffers();
		rbRightEye = glGenRenderbuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, fboRightEye);
		glBindRenderbuffer(GL_RENDERBUFFER, rbRightEye);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, texSize.getWidth(), texSize.getHeight());
		glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, stereoProvider.getEyeTextureIdRight(), 0);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, rbRightEye);
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

	protected Matrix4f getView(EyeType eye) {
		Matrix4f view = new Matrix4f();
		Matrix4f.mul(view, VRProvider.getHMDRotationRoom().getMatrix(), view);
		view.translate(Util.convertVector(VRProvider.getStereoProvider().getEyePosition(eye).negate()));
		Matrix4f.mul(view, VRProvider.getRoomRotation().getMatrix(), view);
		view.translate(Util.convertVector(VRProvider.getRoomPosition().negate()));
		return view;
	}

	protected void drawStencil(EyeType eye) {
		Dimension texSize = VRProvider.getEyeTextureSize();
		float[] vertices = VRProvider.getStereoProvider().getStencilMask(eye);

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
		for (int i = 0; i < vertices.length; i += 2) {
			glVertex2f(vertices[i], vertices[i + 1]);
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
		guiModel.render(controller.getPosition().add(controller.getRotation().inverse().forward().multiply(0.5F * size.getY() + 0.05F)), controller.getRotation(), new Color(255, 255, 255), new Vector3(size.getX(), 1, size.getY()), true, false, texGui);
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
