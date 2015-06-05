
package com.techjar.ledcm.hardware.animation;

/**
 *
 * @author Techjar
 */
public class AnimationOption {
    public final String id;
    public final String name;
    public final OptionType type;
    public final String[] params;

    public AnimationOption(String id, String name, OptionType type, String[] params) {
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

    public String[] getParams() {
        return params;
    }

    public static enum OptionType {
        TEXT, // 0-1 params: default value
        SLIDER, // 0-3 params: default value, increment, show notches
        COMBOBOX, // 2+ params: default value, items
        COMBOBUTTON, // 2+ params: default value, items
        CHECKBOX, // 0-1 params: default value
        RADIOGROUP, // 2+ params: default value, items
    }
}
