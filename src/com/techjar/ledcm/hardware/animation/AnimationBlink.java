
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationBlink extends Animation {
    private boolean state;

    public AnimationBlink() {
        super();
    }

    @Override
    public String getName() {
        return "Blink";
    }

    @Override
    public void refresh() {
        if (ticks % 30 == 0) {
            state = !state;
            for (int x = 0; x < dimension.x; x++) {
                for (int y = 0; y < dimension.y; y++) {
                    for (int z = 0; z < dimension.z; z++) {
                        ledManager.setLEDColor(x, y, z, state ? LEDCubeManager.getPaintColor() : new Color());
                    }
                }
            }
        }
    }

    @Override
    public void reset() {
        state = false;
    }

    // Left here for reference purposes, will be deleted later
    /*@Override
    public AnimationOption[] getOptions() {
        AnimationOption[] array = new AnimationOption[3];
        array[0] = new AnimationOption("test", "Test", AnimationOption.OptionType.RADIOGROUP, new String[]{"2", "1", "loolololol", "2", "lol2", "3", "watican", "4", "testtesteste"});
        array[1] = new AnimationOption("test2", "Test 2", AnimationOption.OptionType.SLIDER, new String[]{"0.5", "0.1", "true"});
        array[2] = new AnimationOption("test3", "Test 3", AnimationOption.OptionType.CHECKBOX, new String[]{"false"});
        return array;
    }*/
}
