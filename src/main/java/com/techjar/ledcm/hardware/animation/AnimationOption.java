
package com.techjar.ledcm.hardware.animation;

/**
 *
 * @author Techjar
 */
public class AnimationOption {
	public final String id;
	public final String name;
	public final OptionType type;
	public final Object[] params;

	public AnimationOption(String id, String name, OptionType type, Object[] params) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.params = params;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public OptionType getType() {
		return type;
	}

	public Object[] getParams() {
		return params;
	}

	public static enum OptionType {
		/** 1+ params: default value, validation regex, max length */
		TEXT,
		/** 1-3 params: default value, increment, show notches */
		SLIDER,
		/** 3+ params: default value, items (id & name interleaved) */
		COMBOBOX,
		/** 2+ params: default value, items (id & name interleaved) */
		COMBOBUTTON,
		/** 1 param: default value */
		CHECKBOX,
		/** 3+ params: default value, items (id & name interleaved) */
		RADIOGROUP,
		/** 1 param: text */
		BUTTON,
		/** 5 params: default value, min value, max value, increment, decimal places */
		SPINNER,
		/** 1 param: default value */
		COLORPICKER,
	}
}
