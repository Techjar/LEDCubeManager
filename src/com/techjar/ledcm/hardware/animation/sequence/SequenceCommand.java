
package com.techjar.ledcm.hardware.animation.sequence;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.lang.reflect.Modifier;

/**
 *
 * @author Techjar
 */
public abstract class SequenceCommand {
    private static final BiMap<String, Class<? extends SequenceCommand>> commandMap = HashBiMap.create();
    static {
        registerCommand("wait", SequenceCommandWait.class);
        registerCommand("loadmusic", SequenceCommandLoadMusic.class);
        registerCommand("musicsync", SequenceCommandMusicSync.class);
        registerCommand("setanimation", SequenceCommandSetAnimation.class);
        registerCommand("setoptions", SequenceCommandSetOptions.class);
        registerCommand("setcolorpicker", SequenceCommandSetColorPicker.class);
        registerCommand("clear", SequenceCommandClear.class);
        registerCommand("finishwait", SequenceCommandFinishWait.class);
        registerCommand("setled", SequenceCommandSetLED.class);
        registerCommand("resetanimation", SequenceCommandResetAnimation.class);
        registerCommand("freeze", SequenceCommandFreeze.class);
        registerCommand("setreflection", SequenceCommandSetReflection.class);
        registerCommand("resettransform", SequenceCommandResetTransform.class);
        registerCommand("rotatetransform", SequenceCommandRotateTransform.class);
    }

    protected AnimationSequence sequence;

    public SequenceCommand(AnimationSequence sequence) {
        this.sequence = sequence;
    }
    public abstract boolean execute(String[] args);

    public boolean onSequenceLoad(String[] args) {
        return true;
    }

    public final String getId() {
        return commandMap.inverse().get(this.getClass());
    }

    private static void registerCommand(String id, Class<? extends SequenceCommand> clazz) {
        if (Modifier.isAbstract(clazz.getModifiers())) throw new IllegalArgumentException("Cannot register abstract command class: " + clazz.getName());
        if (commandMap.containsKey(id)) throw new IllegalArgumentException("Command with ID \"" + id + "\" already exists!");
        if (commandMap.containsValue(clazz)) throw new IllegalArgumentException("Command class \"" + clazz.getName() + "\" is already registered!");
        commandMap.put(id, clazz);
    }

    public static Class<? extends SequenceCommand> getCommandClassByID(String id) {
        return commandMap.get(id);
    }

    public static String getCommandIDByClass(Class<? extends SequenceCommand> clazz) {
        return commandMap.inverse().get(clazz);
    }
}
