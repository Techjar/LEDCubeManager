package com.techjar.ledcm.util.input;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.vr.VRProvider.ControllerType;
import com.techjar.ledcm.vr.VRTrackedController.ButtonType;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import org.lwjgl.input.Controller;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 *
 * @author Techjar
 */
@Value @RequiredArgsConstructor
public class InputInfo {
	private final Type type;
	private final int button;
	private final ControllerType vrControllerType;

	public InputInfo(Type type, int button) {
		this.type = type;
		this.button = button;
		this.vrControllerType = null;
	}

	public String getDisplayString() {
		switch (type) {
			case KEYBOARD:
				if (Keyboard.getKeyName(button) == null) return "";
				return Keyboard.getKeyName(button);
			case MOUSE:
				if (Mouse.getButtonName(button) == null) return "";
				return "MOUSE" + button;
			/*case CONTROLLER:
                Controller con = LEDCubeManager.getInstance().getController(LEDCubeManager.getConfig().getString("controls.controller"));
                if (con == null || con.getButtonName(button) == null) return "";
                return con.getButtonName(button);*/
			case CONTROLLER:
				return "";
			case VR:
				if (button < 0 && button >= ButtonType.values().length) return "";
				return (vrControllerType == ControllerType.LEFT ? "L_" : "R_") + ButtonType.values()[button].name();
		}
		return "";
	}

	public static InputInfo fromString(String str) {
		if (str == null) return null;
		Type type = Type.fromString(str);
		if (type == null) return null;
		int code = Integer.valueOf(str.substring(1));
		switch (type) {
			case KEYBOARD:
				if (code != Keyboard.KEY_NONE)
					return new InputInfo(type, code);
			case MOUSE:
				if (code != -1)
					return new InputInfo(type, code);
			/*case CONTROLLER:
                Controller con = LEDCubeManager.getInstance().getController(LEDCubeManager.getConfig().getString("controls.controller"));
                if (code != -1) {
                    return new InputInfo(type, code);
                else {

                }*/
			case CONTROLLER:
				return null;
			case VR:
				code = Integer.valueOf(str.substring(2));
				int controllerType = Integer.valueOf(str.substring(1, 2));
				if (code >= 0 && code < ButtonType.values().length && controllerType >= 0 && controllerType < ControllerType.values().length)
					return new InputInfo(type, code, ControllerType.values()[controllerType]);
		}
		return null;
	}

	@Override
	public String toString() {
		return new StringBuilder(type.toString()).append(vrControllerType != null ? vrControllerType.ordinal() : "").append(button).toString();
	}

	public static enum Type {
		KEYBOARD,
		MOUSE,
		CONTROLLER,
		VR;

		@Override
		public String toString() {
			switch (this) {
				case KEYBOARD: return "K";
				case MOUSE: return "M";
				case CONTROLLER: return "C";
				case VR: return "V";
			}
			return "";
		}

		public static Type fromString(String str) {
			if (str == null || str.length() < 1) return null;
			switch (str.charAt(0)) {
				case 'K': return KEYBOARD;
				case 'M': return MOUSE;
				case 'C': return CONTROLLER;
				case 'V': return VR;
			}
			return null;
		}
	}
}
