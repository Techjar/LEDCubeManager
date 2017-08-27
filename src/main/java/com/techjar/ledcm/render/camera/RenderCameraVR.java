package com.techjar.ledcm.render.camera;

import com.hackoeur.jglm.Matrices;
import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.vr.VRProvider;
import com.techjar.ledcm.vr.VRStereoProvider;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.util.Dimension;
import org.lwjgl.util.vector.Matrix4f;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class RenderCameraVR implements RenderCamera {
	public final VRStereoProvider.EyeType eye;
	public final int fboId;

	public RenderCameraVR(VRStereoProvider.EyeType eye, int fboId) {
		this.eye = eye;
		this.fboId = fboId;
	}

	@Override
	public boolean shouldRender() {
		if (eye == VRStereoProvider.EyeType.CENTER) {
			return Display.isActive() || (LEDCubeManager.getFrame().isVisible() && LEDCubeManager.getFrame().getState() != Frame.ICONIFIED) || LEDCubeManager.getFrameServer().numClients > 0;
		} else {
			return true;
		}
	}

	@Override
	public boolean usesMainWindow() {
		return eye == VRStereoProvider.EyeType.CENTER;
	}

	@Override
	public void setup() {
		Dimension texSize = VRProvider.getEyeTextureSize();
		DisplayMode displayMode = LEDCubeManager.getDisplayMode();
		VRStereoProvider stereoProvider = VRProvider.getStereoProvider();
		LEDCubeManager ledcm = LEDCubeManager.getInstance();

		if (eye == VRStereoProvider.EyeType.CENTER) {
			ledcm.resizeGL(displayMode.getWidth(), displayMode.getHeight());
			ledcm.setupView(Util.convertMatrix(Matrices.perspective(ledcm.getFieldOfView(), (float)LEDCubeManager.getDisplayMode().getWidth() / (float)LEDCubeManager.getDisplayMode().getHeight(), ledcm.getNearClip(), ledcm.getViewDistance())), getView(eye));
			glBindFramebuffer(GL_DRAW_FRAMEBUFFER, ledcm.isAntiAliasing() ? ledcm.getMultisampleFBO() : 0);
			glDisable(GL_STENCIL_TEST);
		} else {
			ledcm.resizeGL(texSize.getWidth(), texSize.getHeight());
			ledcm.setupView(stereoProvider.getProjectionMatrix(eye.ordinal(), ledcm.getNearClip(), ledcm.getViewDistance()), getView(eye));
			glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fboId);
			glEnable(GL_STENCIL_TEST);
		}
	}

	protected Matrix4f getView(VRStereoProvider.EyeType eye) {
		Matrix4f view = new Matrix4f();
		Matrix4f.mul(view, VRProvider.getHMDRotationRoom().getMatrix(), view);
		view.translate(Util.convertVector(VRProvider.getStereoProvider().getEyePosition(eye).negate()));
		Matrix4f.mul(view, VRProvider.getRoomRotation().getMatrix(), view);
		view.translate(Util.convertVector(VRProvider.getRoomPosition().negate()));
		return view;
	}
}
