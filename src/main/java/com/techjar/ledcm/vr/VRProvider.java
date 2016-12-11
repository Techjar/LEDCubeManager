package com.techjar.ledcm.vr;

import static org.lwjgl.opengl.GL11.*;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

import com.sun.jna.ptr.PointerByReference;
import com.techjar.ledcm.util.AxisAlignedBB;
import com.techjar.ledcm.util.Material;
import com.techjar.ledcm.util.Model;
import jopenvr.JOpenVRLibrary.EVREventType;
import lombok.SneakyThrows;
import org.lwjgl.util.Dimension;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import jopenvr.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import com.sun.jna.Memory;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.OperatingSystem;
import com.techjar.ledcm.util.Quaternion;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector2;
import com.techjar.ledcm.util.Vector3;
import com.techjar.ledcm.util.exception.VRException;
import com.techjar.ledcm.util.logging.LogHelper;
import org.newdawn.slick.opengl.Texture;

public class VRProvider {
	private VRProvider() {
	}

	@Getter private static boolean initialized;
	static IntBuffer errorStore;
	private static Pointer renderModelNamePtr;
	private static RenderModel_ControllerMode_State_t renderModelModeState = new RenderModel_ControllerMode_State_t();
	private static RenderModel_ComponentState_t renderModelComponentState = new RenderModel_ComponentState_t();

	@Getter static VRStereoProvider stereoProvider;
	@Getter static Dimension eyeTextureSize;
	@Getter static boolean headTracking;
	@Getter static boolean keyboardShowing;

	static VR_IVRSystem_FnTable vrSystem;
	static VR_IVRCompositor_FnTable vrCompositor;
	static VR_IVROverlay_FnTable vrOverlay;
	static VR_IVRSettings_FnTable vrSettings;
	static VR_IVRChaperone_FnTable vrChaperone;
	static VR_IVRRenderModels_FnTable vrRenderModels;
	static TrackedDevicePose_t.ByReference hmdTrackedDevicePoseReference;
	static TrackedDevicePose_t[] trackedDevicePoses;

	static Matrix4f[] poseMatrices;
	static Vector3[] deviceVelocity;

	static final Matrix4f hmdPose = new Matrix4f();
	static Vector3 hmdPosition = new Vector3();
	static Quaternion hmdRotation = new Quaternion();
	static Vector3 hmdPositionTransformed = new Vector3();
	static Quaternion hmdRotationTransformed = new Quaternion();
	static Matrix4f hmdProjectionLeftEye;
	static Matrix4f hmdProjectionRightEye;
	@Getter static Matrix4f hmdTransformLeftEye = new Matrix4f();
	@Getter static Matrix4f hmdTransformRightEye = new Matrix4f();
	static boolean transformStale = true;

	final static VRTextureBounds_t texBounds = new VRTextureBounds_t();
	final static Texture_t texType0 = new Texture_t();
	final static Texture_t texType1 = new Texture_t();

	static VRTrackedController[] controllers;
	@Getter @Setter static boolean reverseHands;
	@Getter static Vector3 roomPosition = new Vector3();
	@Getter static Quaternion roomRotation = new Quaternion();

	public static void init() {
		if (initialized) throw new IllegalStateException("Already initialized!");
		if (!OperatingSystem.isWindows()) throw new RuntimeException("VR is not yet supported on this platform!");

		// Load OpenVR native library
		File workingDir = new File(System.getProperty("user.dir"));
		File openVRLib = new File(workingDir, "build/natives/jopenvr/x" + OperatingSystem.getJavaArch());
		NativeLibrary.addSearchPath("openvr_api", openVRLib.getPath());
		LogHelper.info("OpenVR %s-bit native library injected.", OperatingSystem.getJavaArch());

		if (JOpenVRLibrary.VR_IsRuntimeInstalled() == 0) {
			throw new VRException("No runtime is installed.");
		}
		if (JOpenVRLibrary.VR_IsHmdPresent() == 0) {
			throw new VRException("No HMD was detected.");
		}

		initializeJOpenVR();
		initOpenVRCompositor();
		initOpenVROverlay();
		initOpenVROSettings();
		initOpenVRChaperone();
		initOpenVRRenderModels();

		HmdMatrix34_t matL = vrSystem.GetEyeToHeadTransform.apply(JOpenVRLibrary.EVREye.EVREye_Eye_Left);
		OpenVRUtil.convertSteamVRMatrix3ToMatrix4f(matL, hmdTransformLeftEye);
		HmdMatrix34_t matR = vrSystem.GetEyeToHeadTransform.apply(JOpenVRLibrary.EVREye.EVREye_Eye_Right);
		OpenVRUtil.convertSteamVRMatrix3ToMatrix4f(matR, hmdTransformRightEye);
		LogHelper.info("Eye poses loaded.");

		deviceVelocity = new Vector3[JOpenVRLibrary.k_unMaxTrackedDeviceCount];
		for (int i = 0; i < deviceVelocity.length; i++)
			deviceVelocity[i] = new Vector3();
		controllers = new VRTrackedController[ControllerType.values().length];
		for (int i = 0; i < controllers.length; i++)
			controllers[i] = new VRTrackedController(ControllerType.values()[i]);

		stereoProvider = new VRStereoProvider();
		int[] eyeTexSize = stereoProvider.getEyeTextureSize();
		eyeTextureSize = new Dimension(eyeTexSize[0], eyeTexSize[1]);
		LogHelper.info("Eye texture size: %d x %d", eyeTextureSize.getWidth(), eyeTextureSize.getHeight());
		stereoProvider.setupEyeTextures(eyeTextureSize.getWidth(), eyeTextureSize.getHeight());
		stereoProvider.setupStencilMask(eyeTextureSize.getWidth(), eyeTextureSize.getHeight(), 1);

		LogHelper.info("OpenVR initialization complete!");
		initialized = true;
	}

	public static void destroy() {
		if (!initialized) throw new IllegalStateException("Not yet initialized!");
		JOpenVRLibrary.VR_ShutdownInternal();
		vrSystem = null;
		vrCompositor = null;
		vrOverlay = null;
		vrSettings = null;
		vrChaperone = null;
		LogHelper.info("OpenVR shut down.");
		initialized = false;
	}

	private static void initializeJOpenVR() {
		errorStore = IntBuffer.allocate(1);
		vrSystem = null;
		JOpenVRLibrary.VR_InitInternal(errorStore, JOpenVRLibrary.EVRApplicationType.EVRApplicationType_VRApplication_Scene);
		if (errorStore.get(0) == 0) {
			LogHelper.info("OpenVR initalized.");
			// ok, try and get the vrSystem pointer..
			vrSystem = new VR_IVRSystem_FnTable(JOpenVRLibrary.VR_GetGenericInterface(JOpenVRLibrary.IVRSystem_Version, errorStore));
		} else {
			throw new VRException(getVRInitError());
		}
		if (vrSystem != null && errorStore.get(0) == 0) {
			LogHelper.info("VRSystem initalized.");
			vrSystem.setAutoSynch(false);
			vrSystem.read();

			hmdTrackedDevicePoseReference = new TrackedDevicePose_t.ByReference();
			trackedDevicePoses = (TrackedDevicePose_t[])hmdTrackedDevicePoseReference.toArray(JOpenVRLibrary.k_unMaxTrackedDeviceCount);
			poseMatrices = new Matrix4f[JOpenVRLibrary.k_unMaxTrackedDeviceCount];
			for (int i = 0; i < poseMatrices.length; i++)
				poseMatrices[i] = new Matrix4f();
			LogHelper.info("Pose reference loaded.");

			// disable all this stuff which kills performance
			hmdTrackedDevicePoseReference.setAutoRead(false);
			hmdTrackedDevicePoseReference.setAutoWrite(false);
			hmdTrackedDevicePoseReference.setAutoSynch(false);
			for (int i = 0; i < JOpenVRLibrary.k_unMaxTrackedDeviceCount; i++) {
				trackedDevicePoses[i].setAutoRead(false);
				trackedDevicePoses[i].setAutoWrite(false);
				trackedDevicePoses[i].setAutoSynch(false);
			}
		} else {
			throw new VRException(getVRInitError());
		}
	}

	private static void initOpenVRCompositor() {
		if (vrSystem != null) {
			vrCompositor = new VR_IVRCompositor_FnTable(JOpenVRLibrary.VR_GetGenericInterface(JOpenVRLibrary.IVRCompositor_Version, errorStore));
			if (vrCompositor != null && errorStore.get(0) == 0) {
				LogHelper.info("OpenVR compositor initialized.");
				vrCompositor.setAutoSynch(false);
				vrCompositor.read();
				vrCompositor.SetTrackingSpace.apply(JOpenVRLibrary.ETrackingUniverseOrigin.ETrackingUniverseOrigin_TrackingUniverseStanding);
			} else {
				throw new VRException(getVRInitError());
			}
		}

		// left eye
		texBounds.uMax = 1f;
		texBounds.uMin = 0f;
		texBounds.vMax = 1f;
		texBounds.vMin = 0f;
		texBounds.setAutoSynch(false);
		texBounds.setAutoRead(false);
		texBounds.setAutoWrite(false);
		texBounds.write();

		// texture type
		texType0.eColorSpace = JOpenVRLibrary.EColorSpace.EColorSpace_ColorSpace_Gamma;
		texType0.eType = JOpenVRLibrary.EGraphicsAPIConvention.EGraphicsAPIConvention_API_OpenGL;
		texType0.setAutoSynch(false);
		texType0.setAutoRead(false);
		texType0.setAutoWrite(false);
		texType0.handle = -1;
		texType0.write();

		// texture type
		texType1.eColorSpace = JOpenVRLibrary.EColorSpace.EColorSpace_ColorSpace_Gamma;
		texType1.eType = JOpenVRLibrary.EGraphicsAPIConvention.EGraphicsAPIConvention_API_OpenGL;
		texType1.setAutoSynch(false);
		texType1.setAutoRead(false);
		texType1.setAutoWrite(false);
		texType1.handle = -1;
		texType1.write();

		LogHelper.info("Eye texture info loaded.");
	}

	// needed for in-game keyboard
	private static void initOpenVROverlay() {
		vrOverlay = new VR_IVROverlay_FnTable(JOpenVRLibrary.VR_GetGenericInterface(JOpenVRLibrary.IVROverlay_Version, errorStore));
		if (vrOverlay != null && errorStore.get(0) == 0) {
			LogHelper.info("OpenVR overlay initialized.");
			vrOverlay.setAutoSynch(false);
			vrOverlay.read();
		} else {
			throw new VRException(getVRInitError());
		}
	}

	private static void initOpenVROSettings() {
		vrSettings = new VR_IVRSettings_FnTable(JOpenVRLibrary.VR_GetGenericInterface(JOpenVRLibrary.IVRSettings_Version, errorStore));
		if (vrSettings != null && errorStore.get(0) == 0) {
			LogHelper.info("OpenVR settings initialized.");
			vrSettings.setAutoSynch(false);
			vrSettings.read();

			// do we need this?
			//IntByReference e = new IntByReference();
			//float ret = vrSettings.GetFloat.apply(pointerFromString("steamvr"), pointerFromString("renderTargetMultiplier"), -1f, e);
		} else {
			throw new VRException(getVRInitError());
		}
	}

	private static void initOpenVRChaperone() {
		vrChaperone = new VR_IVRChaperone_FnTable(JOpenVRLibrary.VR_GetGenericInterface(JOpenVRLibrary.IVRChaperone_Version, errorStore));
		if (vrChaperone != null && errorStore.get(0) == 0) {
			LogHelper.info("OpenVR chaperone initialized.");
			vrChaperone.setAutoSynch(false);
			vrChaperone.read();
		} else {
			throw new VRException(getVRInitError());
		}
	}

	private static void initOpenVRRenderModels() {
		vrRenderModels = new VR_IVRRenderModels_FnTable(JOpenVRLibrary.VR_GetGenericInterface(JOpenVRLibrary.IVRRenderModels_Version, errorStore));
		if (vrRenderModels != null && errorStore.get(0) == 0) {
			LogHelper.info("OpenVR render models initialized.");
			vrRenderModels.setAutoSynch(false);
			vrRenderModels.read();
			int count = vrRenderModels.GetRenderModelCount.apply();
			Pointer pointer = new Memory(JOpenVRLibrary.k_unMaxPropertyStringSize);
			for (int i = 0; i < count; i++) {
				vrRenderModels.GetRenderModelName.apply(i, pointer, JOpenVRLibrary.k_unMaxPropertyStringSize - 1);
				String name = pointer.getString(0);
				LogHelper.fine("Render Model " + i + ": " + name);
				int compCount = vrRenderModels.GetComponentCount.apply(pointer);
				Pointer p2 = new Memory(JOpenVRLibrary.k_unMaxPropertyStringSize);
				for (int j = 0; j < compCount; j++) {
					vrRenderModels.GetComponentName.apply(pointer, j, p2, JOpenVRLibrary.k_unMaxPropertyStringSize - 1);
					Pointer p3 = new Memory(JOpenVRLibrary.k_unMaxPropertyStringSize);
					vrRenderModels.GetComponentRenderModelName.apply(pointer, p2, p3, JOpenVRLibrary.k_unMaxPropertyStringSize - 1);
					String compName = p2.getString(0);
					String modelName = p3.getString(0);
					LogHelper.fine("\tComponent " + j + ": " + compName + " (" + (new File(modelName).exists() ? modelName : "no model") + ")");
				}
			}
		} else {
			throw new VRException(getVRInitError());
		}
	}

	public static void poll(float delta) {
		updatePoses();
		pollEvents();
		for (int i = 0; i < controllers.length; i++) {
			controllers[i].updateButtonState();
			controllers[i].updateTouchpadSampleBuffer();
			controllers[i].processInput(delta);
		}
	}

	/**
	 *
	 * @return Play area size or null if not valid
	 */
	public static Vector2 getPlayAreaSize() {
		FloatBuffer bufX = FloatBuffer.allocate(1);
		FloatBuffer bufZ = FloatBuffer.allocate(1);
		byte valid = vrChaperone.GetPlayAreaSize.apply(bufX, bufZ);
		if (valid == 1) return new Vector2(bufX.get(0), bufZ.get(0));
		return null;
	}

	private static void updatePoses() { // This also ends up functioning as the "v-sync" to the HMD's refresh rate
		vrCompositor.WaitGetPoses.apply(hmdTrackedDevicePoseReference, JOpenVRLibrary.k_unMaxTrackedDeviceCount, null, 0);

		for (int i = 0; i < JOpenVRLibrary.k_unMaxTrackedDeviceCount; i++) {
			trackedDevicePoses[i].read();
			if (trackedDevicePoses[i].bPoseIsValid != 0) {
				OpenVRUtil.convertSteamVRMatrix3ToMatrix4f(trackedDevicePoses[i].mDeviceToAbsoluteTracking, poseMatrices[i]);
				deviceVelocity[i].setX(trackedDevicePoses[i].vVelocity.v[0]);
				deviceVelocity[i].setY(trackedDevicePoses[i].vVelocity.v[1]);
				deviceVelocity[i].setZ(trackedDevicePoses[i].vVelocity.v[2]);
			}
		}

		if (trackedDevicePoses[JOpenVRLibrary.k_unTrackedDeviceIndex_Hmd].bPoseIsValid != 0) {
			OpenVRUtil.Matrix4fCopy(poseMatrices[JOpenVRLibrary.k_unTrackedDeviceIndex_Hmd], hmdPose);
			headTracking = true;
		} else {
			hmdPose.setIdentity();
			headTracking = false;
		}

		hmdPosition = Util.convertVector(OpenVRUtil.convertMatrix4ftoTranslationVector(hmdPose)).multiply(stereoProvider.worldScale);
		hmdRotation = new Quaternion(OpenVRUtil.convertMatrix4ftoRotationQuat(hmdPose));

		findControllerDevices();
		for (int i = 0; i < controllers.length; i++) {
			VRTrackedController controller = controllers[i];
			if (controller.deviceIndex != -1 && trackedDevicePoses[controller.deviceIndex].bPoseIsValid != 0) {
				controller.updatePose(poseMatrices[controller.deviceIndex]);
				controller.tracking = true;

				if (renderModelNamePtr == null) renderModelNamePtr = new Memory(JOpenVRLibrary.k_unMaxPropertyStringSize);
				vrSystem.GetStringTrackedDeviceProperty.apply(controller.deviceIndex, JOpenVRLibrary.ETrackedDeviceProperty.ETrackedDeviceProperty_Prop_RenderModelName_String, renderModelNamePtr, JOpenVRLibrary.k_unMaxPropertyStringSize - 1, errorStore);
				String modelName = renderModelNamePtr.getString(0);
				if (!modelName.equals(controller.renderModelName)) {
					try {
						if (controller.renderModels != null) {
							for (VRRenderModel model : controller.renderModels) {
								model.model.release();
								vrRenderModels.FreeRenderModel.apply(model.renderModel);
							}
						}
						int componentCount = vrRenderModels.GetComponentCount.apply(renderModelNamePtr);
						if (componentCount > 0) {
							ArrayList<VRRenderModel> renderModels = new ArrayList<>();
							for (int j = 0; j < componentCount; j++) {
								Pointer pComponentName = new Memory(JOpenVRLibrary.k_unMaxPropertyStringSize);
								vrRenderModels.GetComponentName.apply(renderModelNamePtr, j, pComponentName, JOpenVRLibrary.k_unMaxPropertyStringSize - 1);
								Pointer pComponentModelName = new Memory(JOpenVRLibrary.k_unMaxPropertyStringSize);
								vrRenderModels.GetComponentRenderModelName.apply(renderModelNamePtr, pComponentName, pComponentModelName, JOpenVRLibrary.k_unMaxPropertyStringSize - 1);
								String componentName = pComponentName.getString(0);
								String componentModelName = pComponentModelName.getString(0);
								VRRenderModel renderModel = loadRenderModel(componentModelName, modelName, componentName);
								if (renderModel != null) renderModels.add(renderModel);
							}
							controller.renderModels = renderModels.toArray(new VRRenderModel[renderModels.size()]);
						} else {
							controller.renderModels = new VRRenderModel[1];
							controller.renderModels[1] = loadRenderModel(modelName, modelName, null);
						}
						controller.renderModelName = modelName;
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

				if (controller.renderModels != null) {
					for (VRRenderModel model : controller.renderModels) {
						if (model.componentName != null) {
							renderModelModeState.bScrollWheelVisible = controller.scrolling ? (byte)1 : (byte)0;
							vrRenderModels.GetComponentState.apply(model.modelNamePtr, model.componentNamePtr, controller.state, renderModelModeState, renderModelComponentState);
							model.visible = (renderModelComponentState.uProperties & JOpenVRLibrary.EVRComponentProperty.EVRComponentProperty_VRComponentProperty_IsVisible) != 0;
							OpenVRUtil.convertSteamVRMatrix3ToMatrix4f(renderModelComponentState.mTrackingToComponentRenderModel, model.transform);
							OpenVRUtil.convertSteamVRMatrix3ToMatrix4f(renderModelComponentState.mTrackingToComponentLocal, model.transformLocal);
							model.transform.transpose();
							model.transformLocal.transpose();
						}
					}
				}
			} else {
				controller.updatePose(new Matrix4f());
				controller.tracking = false;
			}
		}

		updateTransforms();
	}

	private static void pollEvents() {
		VREvent_t event = new VREvent_t();
		while (vrSystem.PollNextEvent.apply(event, event.size()) > 0) {
			switch (event.eventType) {
				case EVREventType.EVREventType_VREvent_KeyboardClosed:
					keyboardShowing = false;
					break;
				case EVREventType.EVREventType_VREvent_KeyboardCharInput:
					byte[] bytes = event.data.getPointer().getByteArray(0, 8);
					int len = 0;
					for (byte b : bytes) {
						if (b > 0) len++;
					}
					String str = new String(bytes, 0, len, Charset.forName("UTF-8"));
					for (char ch : str.toCharArray())
						LEDCubeManager.queueVirtualKeyPress(Util.characterToLWJGLKeyCode(ch), ch);
					break;
				case EVREventType.EVREventType_VREvent_Quit:
					LEDCubeManager.getInstance().shutdown();
					break;
			}
		}
	}

	private static void findControllerDevices() {
		controllers[ControllerType.LEFT.ordinal()].deviceIndex = -1;
		controllers[ControllerType.RIGHT.ordinal()].deviceIndex = -1;
		if (reverseHands) {
			controllers[ControllerType.RIGHT.ordinal()].deviceIndex = vrSystem.GetTrackedDeviceIndexForControllerRole.apply(JOpenVRLibrary.ETrackedControllerRole.ETrackedControllerRole_TrackedControllerRole_LeftHand);
			controllers[ControllerType.LEFT.ordinal()].deviceIndex = vrSystem.GetTrackedDeviceIndexForControllerRole.apply(JOpenVRLibrary.ETrackedControllerRole.ETrackedControllerRole_TrackedControllerRole_RightHand);
		} else {
			controllers[ControllerType.LEFT.ordinal()].deviceIndex = vrSystem.GetTrackedDeviceIndexForControllerRole.apply(JOpenVRLibrary.ETrackedControllerRole.ETrackedControllerRole_TrackedControllerRole_LeftHand);
			controllers[ControllerType.RIGHT.ordinal()].deviceIndex = vrSystem.GetTrackedDeviceIndexForControllerRole.apply(JOpenVRLibrary.ETrackedControllerRole.ETrackedControllerRole_TrackedControllerRole_RightHand);
		}
	}

	@SneakyThrows(InterruptedException.class)
	private static VRRenderModel loadRenderModel(String name, String modelName, String componentName) {
		Pointer pName = pointerFromString(name);
		PointerByReference refModel = new PointerByReference();
		int error;
		while (true) {
			error = vrRenderModels.LoadRenderModel_Async.apply(pName, refModel);
			if (error != JOpenVRLibrary.EVRRenderModelError.EVRRenderModelError_VRRenderModelError_Loading)
				break;
			Thread.sleep(1);
		}
		if (error == JOpenVRLibrary.EVRRenderModelError.EVRRenderModelError_VRRenderModelError_InvalidModel) {
			return null;
		} else if (error != JOpenVRLibrary.EVRRenderModelError.EVRRenderModelError_VRRenderModelError_None) {
			throw new VRException("Error loading render model (" + name + "): error " + error + " - " + vrRenderModels.GetRenderModelErrorNameFromEnum.apply(error));
		}
		RenderModel_t renderModel = new RenderModel_t(refModel.getValue());
		renderModel.read();

		PointerByReference refTexture = new PointerByReference();
		while (true) {
			error = vrRenderModels.LoadTexture_Async.apply(renderModel.diffuseTextureId, refTexture);
			if (error != JOpenVRLibrary.EVRRenderModelError.EVRRenderModelError_VRRenderModelError_Loading)
				break;
			Thread.sleep(1);
		}
		if (error != JOpenVRLibrary.EVRRenderModelError.EVRRenderModelError_VRRenderModelError_None) {
			throw new VRException("Error loading render model texture (" + name + "): error " + error + " - " + vrRenderModels.GetRenderModelErrorNameFromEnum.apply(error));
		}
		RenderModel_TextureMap_t textureMap = new RenderModel_TextureMap_t(refTexture.getValue());
		textureMap.read();

		Texture texture = LEDCubeManager.getTextureManager().loadTexture(name, textureMap.rubTextureMapData.getByteArray(0, textureMap.unWidth * textureMap.unHeight * 4), textureMap.unWidth, textureMap.unHeight, true, GL_LINEAR);
		float[][] verticesIndexed = new float[renderModel.unVertexCount][3];
		float[][] normalsIndexed = new float[renderModel.unVertexCount][3];
		float[][] texCoordsIndexed = new float[renderModel.unVertexCount][2];
		int structSize = new RenderModel_Vertex_t().size();
		for (int i = 0; i < renderModel.unVertexCount; i++) {
			RenderModel_Vertex_t vertex = new RenderModel_Vertex_t(renderModel.rVertexData.getPointer().share(i * structSize));
			vertex.read();
			verticesIndexed[i][0] = vertex.vPosition.v[0];
			verticesIndexed[i][1] = vertex.vPosition.v[1];
			verticesIndexed[i][2] = vertex.vPosition.v[2];
			normalsIndexed[i][0] = vertex.vNormal.v[0];
			normalsIndexed[i][1] = vertex.vNormal.v[1];
			normalsIndexed[i][2] = vertex.vNormal.v[2];
			texCoordsIndexed[i][0] = vertex.rfTextureCoord[0];
			texCoordsIndexed[i][1] = vertex.rfTextureCoord[1];
		}
		short[] indices = renderModel.rIndexData.getPointer().getShortArray(0, renderModel.unTriangleCount * 3);
		float[] vertices = new float[indices.length * 3];
		float[] normals = new float[indices.length * 3];
		float[] texCoords = new float[indices.length * 2];
		Vector3 minVertex = new Vector3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		Vector3 maxVertex = new Vector3(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
		for (int i = 0; i < indices.length; i++) {
			int index = indices[i];
			vertices[i * 3] = verticesIndexed[index][0];
			vertices[i * 3 + 1] = verticesIndexed[index][1];
			vertices[i * 3 + 2] = verticesIndexed[index][2];
			normals[i * 3] = normalsIndexed[index][0];
			normals[i * 3 + 1] = normalsIndexed[index][1];
			normals[i * 3 + 2] = normalsIndexed[index][2];
			texCoords[i * 2] = texCoordsIndexed[index][0];
			texCoords[i * 2 + 1] = texCoordsIndexed[index][1];
			if (vertices[i * 3] < minVertex.getX()) minVertex.setX(vertices[i * 3]);
			if (vertices[i * 3 + 1] < minVertex.getY()) minVertex.setY(vertices[i * 3 + 1]);
			if (vertices[i * 3 + 2] < minVertex.getZ()) minVertex.setZ(vertices[i * 3 + 2]);
			if (vertices[i * 3] > maxVertex.getX()) maxVertex.setX(vertices[i * 3]);
			if (vertices[i * 3 + 1] > maxVertex.getY()) maxVertex.setY(vertices[i * 3 + 1]);
			if (vertices[i * 3 + 2] > maxVertex.getZ()) maxVertex.setZ(vertices[i * 3 + 2]);
		}
		Vector3 center = minVertex.add(maxVertex).divide(2);
		float radius = minVertex.distance(maxVertex) / 2;
		Material material = new Material();
		Model model = new Model(1, texture, LEDCubeManager.getTextureManager().getTexture("white.png"), material, false);
		model.loadMesh(0, 0, indices.length, vertices, normals, texCoords, center, radius, renderModel.unTriangleCount);
		if (model.getAABB() == null) model.setAABB(new AxisAlignedBB(minVertex, maxVertex));
		model.makeImmutable();

		LogHelper.info("Loaded render model: " + modelName + (componentName != null ? " " + componentName : ""));
		return new VRRenderModel(modelName, componentName, model, renderModel);
	}

	public static boolean setKeyboardShowing(boolean showing) {
		try {
			if (showing) {
				Pointer pointer = new Memory(6);
				pointer.setString(0, "ledcm");
				Pointer empty = new Memory(1);
				empty.setString(0, "");

				int ret = vrOverlay.ShowKeyboard.apply(0, 0, pointer, 256, empty, (byte)1, 0);
				if (ret != 0) {
					System.out.println("VR Overlay Error: " + vrOverlay.GetOverlayErrorNameFromEnum.apply(ret).getString(0));
				} else {
					keyboardShowing = true;
				}
			} else {
				vrOverlay.HideKeyboard.apply();
				keyboardShowing = false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return keyboardShowing;
	}

	public static Vector3 getHMDPosition() {
		return hmdPositionTransformed.copy();
	}

	public static Quaternion getHMDRotation() {
		return hmdRotationTransformed.copy();
	}

	public static Vector3 getHMDPositionRoom() {
		return hmdPosition.copy();
	}

	public static Quaternion getHMDRotationRoom() {
		return hmdRotation.copy();
	}

	public static VRTrackedController getController(@NonNull ControllerType controller) {
		return controllers[controller.ordinal()];
	}

	public static void setRoomPosition(Vector3 roomPosition) {
		VRProvider.roomPosition = roomPosition;
		updateTransforms();
	}

	public static void setRoomRotation(Quaternion roomRotation) {
		VRProvider.roomRotation = roomRotation;
		updateTransforms();
	}

	public static Vector3 transformToRoom(Vector3 vector) {
		Matrix4f matrix = roomRotation.getMatrix();
		matrix.invert();
		//matrix.translate(Util.convertVector(roomPosition));
		Vector4f vec = new Vector4f(vector.getX(), vector.getY(), vector.getZ(), 1);
		Matrix4f.transform(matrix, vec, vec);
		return new Vector3(vec.x, vec.y, vec.z).add(roomPosition);
	}

	public static Quaternion transformToRoom(Quaternion quat) {
		Quaternion rotation = new Quaternion((Matrix4f)roomRotation.getMatrix().invert());
		return quat.multiply(rotation);
	}

	static void updateTransforms() {
		hmdPositionTransformed = transformToRoom(hmdPosition);
		hmdRotationTransformed = transformToRoom(hmdRotation);
		for (int i = 0; i < controllers.length; i++)
			controllers[i].updateTransform();
	}

	static Pointer pointerFromString(String in) {
		Pointer p = new Memory(in.getBytes().length + 1);
		p.setString(0, in);
		return p;
	}

	private static String getVRInitError() {
		return JOpenVRLibrary.VR_GetVRInitErrorAsEnglishDescription(errorStore.get(0)).getString(0);
	}

	public static enum ControllerType {
		LEFT,
		RIGHT;
	}
}
