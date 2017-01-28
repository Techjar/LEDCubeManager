package com.techjar.ledcm.vr;

import jopenvr.*;
import lombok.Getter;
import lombok.NonNull;

import lombok.Setter;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.math.Quaternion;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.math.Vector2;
import com.techjar.ledcm.util.math.Vector3;
import com.techjar.ledcm.vr.VRProvider.ControllerType;

public class VRTrackedController {
	int deviceIndex;
	@Getter final ControllerType type;
	@Getter boolean tracking;
	@Getter @Setter(onParam = @__({@NonNull})) TouchpadMode touchpadMode = TouchpadMode.SINGLE;
	final Matrix4f pose = new Matrix4f();
	Vector3 position = new Vector3();
	Quaternion rotation = new Quaternion();
	Vector3 positionTransformed = new Vector3();
	Quaternion rotationTransformed = new Quaternion();
	String renderModelName;
	@Getter VRRenderModel[] renderModels;
	@Getter @Setter boolean scrolling;

	VRControllerState_t.ByReference state;
	VRControllerState_t lastState;
	Vector2f[] touchpadSampleBuffer = new Vector2f[90];
	int touchpadSampleIndex;
	boolean triggerClicked;
	boolean lastTriggerClicked;
	ButtonType lastTouchpadButton;

	static final long k_buttonAppMenu = (1L << JOpenVRLibrary.EVRButtonId.EVRButtonId_k_EButton_ApplicationMenu);
	static final long k_buttonGrip =  (1L << JOpenVRLibrary.EVRButtonId.EVRButtonId_k_EButton_Grip);
	static final long k_buttonTouchpad = (1L << JOpenVRLibrary.EVRButtonId.EVRButtonId_k_EButton_SteamVR_Touchpad);
	static final long k_buttonTrigger = (1L << JOpenVRLibrary.EVRButtonId.EVRButtonId_k_EButton_SteamVR_Trigger);
	static final int k_axisTouchpad = 0;
	static final int k_axisTrigger = 1;

	public VRTrackedController(ControllerType type) {
		this.type = type;
		state = new VRControllerState_t.ByReference();
		lastState = new VRControllerState_t();
		for (int i = 0; i < lastState.rAxis.length; i++)
			lastState.rAxis[i] = new VRControllerAxis_t();
		for (int i = 0; i < touchpadSampleBuffer.length; i++)
			touchpadSampleBuffer[i] = new Vector2f();
	}

	void updatePose(Matrix4f matrix) {
		OpenVRUtil.Matrix4fCopy(matrix, pose);
		position = Util.convertVector(OpenVRUtil.convertMatrix4ftoTranslationVector(pose)).multiply(VRProvider.getStereoProvider().worldScale);
		rotation = new Quaternion(OpenVRUtil.convertMatrix4ftoRotationQuat(pose.transpose(null)));
	}

	void updateButtonState() {
		lastState.unPacketNum = state.unPacketNum;
		lastState.ulButtonPressed = state.ulButtonPressed;
		lastState.ulButtonTouched = state.ulButtonTouched;
		for (int i = 0; i < 5; i++) {
			if (state.rAxis[i] != null) {
				lastState.rAxis[i].x = state.rAxis[i].x;
				lastState.rAxis[i].y = state.rAxis[i].y;
			}
		}

		if (deviceIndex != -1) {
			VRProvider.vrSystem.GetControllerState.apply(deviceIndex, state, state.size());
			state.read();
		} else {
			state.ulButtonPressed = 0;
			state.ulButtonTouched = 0;
			for (int i = 0; i < 5; i++) {
				if (state.rAxis[i] != null) {
					state.rAxis[i].x = 0;
					state.rAxis[i].y = 0;
				}
			}
		}
	}

	void updateTouchpadSampleBuffer() {
		if (state.rAxis[k_axisTouchpad] != null && (state.ulButtonTouched & k_buttonTouchpad) > 0) {
			touchpadSampleBuffer[touchpadSampleIndex].x = state.rAxis[k_axisTouchpad].x;
			touchpadSampleBuffer[touchpadSampleIndex].y = state.rAxis[k_axisTouchpad].y;
			if (++touchpadSampleIndex >= touchpadSampleBuffer.length) touchpadSampleIndex = 0;
		} else {
			for (int i = 0; i < touchpadSampleBuffer.length; i++) {
				touchpadSampleBuffer[touchpadSampleIndex].x = 0;
				touchpadSampleBuffer[touchpadSampleIndex].y = 0;
			}
		}
	}

	void processInput(float delta) {
		// button touch
		if ((state.ulButtonTouched & k_buttonTouchpad) != (lastState.ulButtonTouched & k_buttonTouchpad) || ((state.ulButtonTouched & k_buttonTouchpad) > 0 && getTouchpadButton() != lastTouchpadButton)) {
			if (state.rAxis[k_axisTouchpad] != null) {
				lastState.rAxis[k_axisTouchpad].x = state.rAxis[k_axisTouchpad].x;
				lastState.rAxis[k_axisTouchpad].y = state.rAxis[k_axisTouchpad].y;
			}
			if ((state.ulButtonTouched & k_buttonTouchpad) > 0 && (lastState.ulButtonTouched & k_buttonTouchpad) > 0 && getTouchpadButton() != lastTouchpadButton) {
				LEDCubeManager.queueVRInputEvent(this, lastTouchpadButton, null, false, false, null);
			}
			lastTouchpadButton = getTouchpadButton();
			LEDCubeManager.queueVRInputEvent(this, getTouchpadButton(), null, (state.ulButtonTouched & k_buttonTouchpad) > 0, false, null);
		}

		// button press
		if ((state.ulButtonPressed & k_buttonAppMenu) != (lastState.ulButtonPressed & k_buttonAppMenu)) {
			LEDCubeManager.queueVRInputEvent(this, ButtonType.MENU, null, (state.ulButtonPressed & k_buttonAppMenu) > 0, true, null);
		}
		if ((state.ulButtonPressed & k_buttonGrip) != (lastState.ulButtonPressed & k_buttonGrip)) {
			LEDCubeManager.queueVRInputEvent(this, ButtonType.GRIP, null, (state.ulButtonPressed & k_buttonGrip) > 0, true, null);
		}
		if ((state.ulButtonPressed & k_buttonTouchpad) != (lastState.ulButtonPressed & k_buttonTouchpad)) {
			LEDCubeManager.queueVRInputEvent(this, getTouchpadButton(), null, (state.ulButtonPressed & k_buttonTouchpad) > 0, true, null);
		}
		if (state.rAxis[k_axisTrigger] != null) { // ulButtonPressed returns true on partial press for some reason, but we want actual click
			triggerClicked = state.rAxis[k_axisTrigger].x > 0.99F;
			if (triggerClicked != lastTriggerClicked) {
				lastTriggerClicked = triggerClicked;
				LEDCubeManager.queueVRInputEvent(this, ButtonType.TRIGGER, null, triggerClicked, true, null);
			}
		}

		// axis change
		if (state.rAxis[k_axisTouchpad] != null && (state.rAxis[k_axisTouchpad].x != lastState.rAxis[k_axisTouchpad].x || state.rAxis[k_axisTouchpad].y != lastState.rAxis[k_axisTouchpad].y)) {
			Vector2 deltaVec = new Vector2((state.rAxis[k_axisTouchpad].x - lastState.rAxis[k_axisTouchpad].x) * (1 / delta), (state.rAxis[k_axisTouchpad].y - lastState.rAxis[k_axisTouchpad].y) * (1 / delta));
			LEDCubeManager.queueVRInputEvent(this, null, AxisType.TOUCHPAD, false, false, deltaVec);
		}
		if (state.rAxis[k_axisTrigger] != null && state.rAxis[k_axisTrigger].x != lastState.rAxis[k_axisTrigger].x) {
			Vector2 deltaVec = new Vector2((state.rAxis[k_axisTrigger].x - lastState.rAxis[k_axisTrigger].x) * (1 / delta), (state.rAxis[k_axisTrigger].y - lastState.rAxis[k_axisTrigger].y) * (1 / delta));
			LEDCubeManager.queueVRInputEvent(this, null, AxisType.TRIGGER, false, false, deltaVec);
		}
	}

	void updateTransform() {
		positionTransformed = VRProvider.transformToRoom(position);
		rotationTransformed = VRProvider.transformToRoom(rotation);
	}

	ButtonType getTouchpadButton() {
		Vector2 axis = getAxis(AxisType.TOUCHPAD);
		switch (touchpadMode) {
			case SINGLE:
				return ButtonType.TOUCHPAD;
			case SPLIT_UD:
				if (axis.getY() > 0) return ButtonType.TOUCHPAD_U;
				else return ButtonType.TOUCHPAD_D;
			case SPLIT_LR:
				if (axis.getX() > 0) return ButtonType.TOUCHPAD_R;
				else return ButtonType.TOUCHPAD_L;
			case SPLIT_QUAD:
				if (axis.getX() > 0) {
					if (axis.getY() > 0) return ButtonType.TOUCHPAD_UR;
					else return ButtonType.TOUCHPAD_LR;
				} else {
					if (axis.getY() > 0) return ButtonType.TOUCHPAD_UL;
					else return ButtonType.TOUCHPAD_LL;
				}
			case SPLIT_QUAD_CENTER:
				if (Math.abs(axis.getX()) < 0.3F && Math.abs(axis.getY()) < 0.3F) {
					return ButtonType.TOUCHPAD_C;
				}
				if (axis.getX() > 0) {
					if (axis.getY() > 0) return ButtonType.TOUCHPAD_UR;
					else return ButtonType.TOUCHPAD_LR;
				} else {
					if (axis.getY() > 0) return ButtonType.TOUCHPAD_UL;
					else return ButtonType.TOUCHPAD_LL;
				}
			default:
				return null;
		}
	}

	public Vector3 getPosition() {
		return positionTransformed;
	}

	public Quaternion getRotation() {
		return rotationTransformed;
	}

	public Vector3 getPositionRoom() {
		return position;
	}

	public Quaternion getRotationRoom() {
		return rotation;
	}

	public boolean isButtonTouched(@NonNull ButtonType button) {
		switch (button) {
			case TOUCHPAD:
				return (state.ulButtonTouched & k_buttonTouchpad) > 0;
			case TOUCHPAD_U:
			case TOUCHPAD_D:
			case TOUCHPAD_L:
			case TOUCHPAD_R:
			case TOUCHPAD_UL:
			case TOUCHPAD_UR:
			case TOUCHPAD_LR:
			case TOUCHPAD_LL:
			case TOUCHPAD_C:
				return (state.ulButtonTouched & k_buttonTouchpad) > 0 && button == getTouchpadButton();
			default:
				return false;
		}
	}

	public boolean isButtonTouched(int button) {
		return isButtonTouched(ButtonType.values()[button]);
	}

	public boolean isButtonPressed(@NonNull ButtonType button) {
		switch (button) {
			case MENU:
				return (state.ulButtonPressed & k_buttonAppMenu) > 0;
			case GRIP:
				return (state.ulButtonPressed & k_buttonGrip) > 0;
			case TOUCHPAD:
				return (state.ulButtonPressed & k_buttonTouchpad) > 0;
			case TRIGGER:
				return triggerClicked;
			case TOUCHPAD_U:
			case TOUCHPAD_D:
			case TOUCHPAD_L:
			case TOUCHPAD_R:
			case TOUCHPAD_UL:
			case TOUCHPAD_UR:
			case TOUCHPAD_LR:
			case TOUCHPAD_LL:
			case TOUCHPAD_C:
				return (state.ulButtonPressed & k_buttonTouchpad) > 0 && button == getTouchpadButton();
			default:
				return false;
		}
	}

	public boolean isButtonPressed(int button) {
		return isButtonPressed(ButtonType.values()[button]);
	}

	public Vector2 getAxis(@NonNull AxisType axis) {
		switch (axis) {
			case TOUCHPAD:
				if (state.rAxis[k_axisTouchpad] != null) {
					return new Vector2(state.rAxis[k_axisTouchpad].x, state.rAxis[k_axisTouchpad].y);
				}
				return new Vector2();
			case TRIGGER:
				if (state.rAxis[k_axisTrigger] != null) {
					return new Vector2(state.rAxis[k_axisTrigger].x, 0);
				}
				return new Vector2();
			default:
				return new Vector2();
		}
	}

	public Vector2 getAxis(int axis) {
		return getAxis(AxisType.values()[axis]);
	}

	/*public Vector2 getSampledTouchpadDelta(float period) {
		if (period > 1.0F) throw new IllegalArgumentException("Not enough samples for period: " + period);
		Vector2 delta = new Vector2();
		int max = Math.min(Math.round((touchpadSampleBuffer.length - 1) * period * (0.01111F / LEDCubeManager.getInstance().getFrameDelta())), touchpadSampleBuffer.length - 1);
		for (int i = 0; i < max; i++) {
			int j = Math.floorMod(touchpadSampleIndex - i, touchpadSampleBuffer.length);
			int k = Math.floorMod(j - 1, touchpadSampleBuffer.length);
			delta.setX(delta.getX() + (touchpadSampleBuffer[j].x - touchpadSampleBuffer[k].x));
			delta.setY(delta.getY() + (touchpadSampleBuffer[j].y - touchpadSampleBuffer[k].y));
		}
		return delta.multiply(1 / LEDCubeManager.getFrameDelta());
	}*/

	public void triggerHapticPulse(int duration) {
		if (duration < 0) throw new IllegalArgumentException("Duration invalid: " + duration + " < 0");
		if (duration > 3999) throw new IllegalArgumentException("Duration too long: " + duration + " > 3999");
		if (deviceIndex == -1) return;
		VRProvider.vrSystem.TriggerHapticPulse.apply(deviceIndex, 0, (short)duration);
	}

	public static enum ButtonType {
		MENU,
		GRIP,
		TOUCHPAD,
		TRIGGER,
		TOUCHPAD_U,
		TOUCHPAD_D,
		TOUCHPAD_L,
		TOUCHPAD_R,
		TOUCHPAD_UL,
		TOUCHPAD_UR,
		TOUCHPAD_LR,
		TOUCHPAD_LL,
		TOUCHPAD_C;
	}

	public static enum AxisType {
		TOUCHPAD,
		TRIGGER;
	}

	public static enum TouchpadMode {
		SINGLE,
		SPLIT_UD,
		SPLIT_LR,
		SPLIT_QUAD,
		SPLIT_QUAD_CENTER;
	}
}
