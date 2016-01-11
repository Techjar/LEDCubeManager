
package com.techjar.ledcm.hardware.animation.sequence;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

/**
 * Sequence files can contain comments, by placing a # at the start of the line.<br><br>
 * "Immediate" mode on a command (put a ^ at the start of the line) will make the command
 * that follows it execute immediately after it finishes rather than on the next frame.
 *
 * @author Techjar
 */
public class AnimationSequence {
    private List<SequenceItem> items = new ArrayList<>();
    private int currentIndex = -1;
    private SequenceItem currentItem;
    private SequenceCommand currentCommand;
    @Getter @Setter private boolean musicSynced = false;
    @Getter @Setter private String name;

    private AnimationSequence() {
    }

    public synchronized void update() throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (currentIndex >= 0 && currentIndex < items.size()) {
            while (currentIndex < items.size()) {
                if (currentItem != items.get(currentIndex)) {
                    currentItem = items.get(currentIndex);
                    currentCommand = currentItem.command.getConstructor(AnimationSequence.class).newInstance(this);
                }
                if (currentCommand.execute(currentItem.args)) {
                    currentIndex++;
                }
                if (!currentItem.immediate || currentItem == items.get(currentIndex)) break;
            }
        }
        if (currentIndex >= items.size()) currentIndex = -1;
    }

    public synchronized void start() {
        currentIndex = 0;
        //if (musicFile != null) LEDCubeManager.getLEDCube().getSpectrumAnalyzer().loadFile(musicFile.getAbsolutePath());
    }

    public synchronized void stop() {
        currentIndex = -1;
        currentItem = null;
        currentCommand = null;
        LEDCubeManager.getLEDCube().getSpectrumAnalyzer().stop();
    }

    public boolean isStarted() {
        return currentIndex >= 0;
    }

    public static AnimationSequence loadFromStream(InputStream stream) throws IOException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
            AnimationSequence sequence = new AnimationSequence();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() < 1 || line.charAt(0) == '#') continue;
                boolean immediate = false;
                if (line.charAt(0) == '^') {
                    immediate = true;
                    line = line.substring(1);
                }
                String[] split = line.split(" ", 2);
                Class<? extends SequenceCommand> clazz = SequenceCommand.getCommandClassByID(split[0]);
                if (clazz == null) throw new IllegalArgumentException("Unknown command: " + split[0]);
                String[] args = split.length > 1 ? Util.parseArgumentString(split[1]) : new String[0];
                SequenceCommand command = clazz.getConstructor(AnimationSequence.class).newInstance(sequence);
                if (command.onSequenceLoad(args)) sequence.items.add(new SequenceItem(clazz, args, immediate));
            }
            return sequence;
        }
    }

    public static AnimationSequence loadFromFile(File file) throws IOException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return loadFromStream(fis);
        }
    }

    @Value private static class SequenceItem {
        private Class<? extends SequenceCommand> command;
        private String[] args;
        private boolean immediate;
    }
}
