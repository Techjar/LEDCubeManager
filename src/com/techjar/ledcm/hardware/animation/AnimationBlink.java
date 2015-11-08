
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import java.util.Random;
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
        AnimationOption[] array = new AnimationOption[6];
        Object[] objs = new Object[1000 * 2 + 1];
        objs[0] = new Random().nextInt((objs.length - 1) / 2);
        for (int i = 0; i < (objs.length - 1) / 2; i++) {
            objs[(i * 2) + 1] = i;
            objs[(i * 2) + 2] = "Thing #" + (i + 1);
        }
        array[0] = new AnimationOption("test", "Test", AnimationOption.OptionType.COMBOBUTTON, new Object[]{"2", "1", "loolololol", "2", "lol2", "3", "watican", "4", "testtesteste"});
        array[1] = new AnimationOption("test2", "Test 2", AnimationOption.OptionType.SLIDER, new Object[]{0.5, 0.1, true});
        array[2] = new AnimationOption("test3", "Test 3", AnimationOption.OptionType.CHECKBOX, new Object[]{false});
        //array[3] = new AnimationOption("testd", "Test Deluxe", AnimationOption.OptionType.COMBOBOX, objs);
        array[3] = new AnimationOption("poop", "Watwat", AnimationOption.OptionType.TEXT, new Object[]{""});
        array[4] = new AnimationOption("butts", "SPINNN", AnimationOption.OptionType.SPINNER, new Object[]{0, 0, 100, 5, 0});
        array[5] = new AnimationOption("lolol", "wooCOLOR", AnimationOption.OptionType.COLORPICKER, new Object[]{new Color()});
        return array;
    }*/
}
