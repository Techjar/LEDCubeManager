package com.techjar.ledcm.vr;

import lombok.Getter;

import com.techjar.ledcm.util.math.Vector2;
import com.techjar.ledcm.vr.VRTrackedController.AxisType;
import com.techjar.ledcm.vr.VRTrackedController.ButtonType;

public class VRInputEvent {
	@Getter private final VRTrackedController controller;
	@Getter private final ButtonType button;
	@Getter private final AxisType axis;
	private final boolean buttonState;
	private final boolean buttonPress;
	private final Vector2 axisDelta;

	public VRInputEvent(VRTrackedController controller, ButtonType button, AxisType axis, boolean buttonState, boolean buttonPress, Vector2 axisDelta) {
		this.controller = controller;
		this.button = button;
		this.axis = axis;
		this.buttonState = buttonState;
		this.buttonPress = buttonPress;
		this.axisDelta = axisDelta;
	}

	public boolean isButtonTouchEvent() {
		return button != null && !buttonPress;
	}

	public boolean isButtonPressEvent() {
		return button != null && buttonPress;
	}

	public boolean isAxisEvent() {
		return axis != null;
	}

	public boolean getButtonState() {
		return buttonState;
	}

	public Vector2 getAxisDelta() {
		return axisDelta.copy();
	}

	@Override
	public String toString() {
		return "VRInputEvent [controller=" + controller.getType() + ", button=" + button + ", axis=" + axis + ", buttonState=" + buttonState + ", buttonPress=" + buttonPress + ", axisDelta=" + axisDelta + "]";
	}
}
