
package com.techjar.cubedesigner.hardware.animation;

/**
 *
 * @author Techjar
 */
public class AnimationOption {
    public final String name;
    public final OptionType type;
    public final Object[] params;

    public AnimationOption(String name, OptionType type, Object[] params) {
        this.name = name;
        this.type = type;
        this.params = params;
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
        TEXT, // 0-1 params: default value
        SLIDER, // 0-3 params: default value, increment, show notches
        COMBOBUTTON, // 2+ params: default value, items
        CHECKBOX, // 0-1 params: default value
    }
}
