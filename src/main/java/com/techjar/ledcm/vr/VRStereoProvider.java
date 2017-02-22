package com.techjar.ledcm.vr;

import static org.lwjgl.opengl.GL11.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.techjar.ledcm.util.math.Vector2;
import jopenvr.HmdVector2_t;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.math.Vector3;
import com.techjar.ledcm.util.logging.LogHelper;

import jopenvr.HiddenAreaMesh_t;
import jopenvr.HmdMatrix44_t;
import jopenvr.JOpenVRLibrary;
import jopenvr.OpenVRUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class VRStereoProvider {
	@Getter private int eyeTextureIdLeft;
	@Getter private int eyeTextureIdRight;
	private int[] eyeTextureSize;
	private boolean stencilMask;
	@Getter @Setter protected Vector3 worldScale = new Vector3(1, 1, 1);

	private HiddenAreaMesh_t[] hiddenMeshes = new HiddenAreaMesh_t[2];
	private Vector2[][] hiddenMeshVertices = new Vector2[2][];

	public void submitFrame() {
		VRProvider.vrCompositor.Submit.apply(JOpenVRLibrary.EVREye.EVREye_Eye_Left, VRProvider.texType0, null, JOpenVRLibrary.EVRSubmitFlags.EVRSubmitFlags_Submit_Default);
		VRProvider.vrCompositor.Submit.apply(JOpenVRLibrary.EVREye.EVREye_Eye_Right, VRProvider.texType1, null, JOpenVRLibrary.EVRSubmitFlags.EVRSubmitFlags_Submit_Default);
		VRProvider.vrCompositor.PostPresentHandoff.apply();
	}

	public int[] getEyeTextureSize() {
		if (eyeTextureSize == null) {
			IntBuffer rtx = IntBuffer.allocate(1);
			IntBuffer rty = IntBuffer.allocate(1);
			VRProvider.vrSystem.GetRecommendedRenderTargetSize.apply(rtx, rty);

			int[] size = new int[]{rtx.get(0), rty.get(0)};
			if (size[0] % 2 != 0) size[0]++;
			if (size[1] % 2 != 0) size[1]++;

			eyeTextureSize = size;
		}

		return eyeTextureSize;
	}

	public void setupStencilMask(int width, int height) {
		for (int i = 0; i < 2; i++) {
			hiddenMeshes[i] = VRProvider.vrSystem.GetHiddenAreaMesh.apply(i, 0);
			hiddenMeshes[i].read();
			if (hiddenMeshes[i].unTriangleCount > 0) {
				stencilMask = true;
				hiddenMeshVertices[i] = new Vector2[hiddenMeshes[i].unTriangleCount * 3];
				int structSize = new HmdVector2_t().size();
				for (int j = 0; j < hiddenMeshes[i].unTriangleCount * 3; j++) {
					HmdVector2_t vertex = new HmdVector2_t(hiddenMeshes[i].pVertexData.getPointer().share(j * structSize));
					vertex.read();
					hiddenMeshVertices[i][j] = new Vector2(vertex.v[0] * width, vertex.v[1] * height);
				}
				LogHelper.info("Loaded stencil mask for " + EyeType.values()[i] + " eye. Has " + hiddenMeshes[i].unTriangleCount + " triangles.");
			} else {
				LogHelper.info("No stencil mask found.");
			}
		}
	}

	public int[] setupEyeTextures(int width, int height) {
		if (eyeTextureIdLeft != 0 || eyeTextureIdRight != 0) {
			glDeleteTextures(eyeTextureIdLeft);
			glDeleteTextures(eyeTextureIdRight);
			LogHelper.info("Deleted existing eye textures.");
		}

		// Generate left eye texture
		eyeTextureIdLeft = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, eyeTextureIdLeft);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_INT, (ByteBuffer)null);

		VRProvider.texType0.handle = eyeTextureIdLeft;
		VRProvider.texType0.eColorSpace = JOpenVRLibrary.EColorSpace.EColorSpace_ColorSpace_Gamma;
		VRProvider.texType0.eType = JOpenVRLibrary.EGraphicsAPIConvention.EGraphicsAPIConvention_API_OpenGL;
		VRProvider.texType0.write();
		LogHelper.info("Set up left eye texture.");

		// Generate right eye texture
		eyeTextureIdRight = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, eyeTextureIdRight);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_INT, (ByteBuffer)null);

		VRProvider.texType1.handle = eyeTextureIdRight;
		VRProvider.texType1.eColorSpace = JOpenVRLibrary.EColorSpace.EColorSpace_ColorSpace_Gamma;
		VRProvider.texType1.eType = JOpenVRLibrary.EGraphicsAPIConvention.EGraphicsAPIConvention_API_OpenGL;
		VRProvider.texType1.write();
		LogHelper.info("Set up right eye texture.");

		return new int[]{eyeTextureIdLeft, eyeTextureIdRight};
	}

	public Matrix4f getProjectionMatrix(int eye, float nearClip, float farClip) {
		if (eye == 0) {
			if (VRProvider.hmdProjectionLeftEye == null) {
				HmdMatrix44_t mat = VRProvider.vrSystem.GetProjectionMatrix.apply(JOpenVRLibrary.EVREye.EVREye_Eye_Left, nearClip, farClip, JOpenVRLibrary.EGraphicsAPIConvention.EGraphicsAPIConvention_API_OpenGL);
				VRProvider.hmdProjectionLeftEye = new Matrix4f();
				OpenVRUtil.convertSteamVRMatrix4ToMatrix4f(mat, VRProvider.hmdProjectionLeftEye).transpose(VRProvider.hmdProjectionLeftEye);
			}
			return VRProvider.hmdProjectionLeftEye;
		} else if (eye == 1) {
			if (VRProvider.hmdProjectionRightEye == null) {
				HmdMatrix44_t mat = VRProvider.vrSystem.GetProjectionMatrix.apply(JOpenVRLibrary.EVREye.EVREye_Eye_Right, nearClip, farClip, JOpenVRLibrary.EGraphicsAPIConvention.EGraphicsAPIConvention_API_OpenGL);
				VRProvider.hmdProjectionRightEye = new Matrix4f();
				OpenVRUtil.convertSteamVRMatrix4ToMatrix4f(mat, VRProvider.hmdProjectionRightEye).transpose(VRProvider.hmdProjectionRightEye);
			}
			return VRProvider.hmdProjectionRightEye;
		}
		throw new IllegalArgumentException("Unknown eye type: " + eye);
	}

	public Vector3 getEyePosition(@NonNull EyeType eye) {
		switch (eye) {
			case LEFT:
				Matrix4f pose = Matrix4f.mul(VRProvider.hmdTransformLeftEye, VRProvider.hmdPose, null);
				Vector3f pos = OpenVRUtil.convertMatrix4ftoTranslationVector(pose);
				return Util.convertVector(pos).multiply(worldScale);
			case RIGHT:
				pose = Matrix4f.mul(VRProvider.hmdTransformRightEye, VRProvider.hmdPose, null);
				pos = OpenVRUtil.convertMatrix4ftoTranslationVector(pose);
				return Util.convertVector(pos).multiply(worldScale);
			case CENTER:
				return VRProvider.getHMDPositionRoom();
			default:
				throw new IllegalArgumentException("Unknown eye type: " + eye);
		}
	}

	public Vector2[] getStencilMask(@NonNull EyeType eye) {
		switch (eye) {
			case LEFT:
				return hiddenMeshVertices[0];
			case RIGHT:
				return hiddenMeshVertices[1];
			default:
				return null;
		}
	}

	public boolean hasStencilMask() {
		return stencilMask;
	}

	public static enum EyeType {
		LEFT,
		RIGHT,
		CENTER;
	}
}
