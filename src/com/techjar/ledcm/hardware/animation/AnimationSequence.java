
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.manager.LEDManager;
import com.techjar.ledcm.util.Dimension3D;
import com.techjar.ledcm.util.Timer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationSequence {
    private Timer timer = new Timer();
    private List<SequenceItem> items = new ArrayList<>();
    @Getter private File musicFile;
    @Getter private boolean musicSynced = false;
    private int currentItem;

    private AnimationSequence() {
    }

    public void update() {
        if (currentItem == -1) {
            currentItem = 0;
            SequenceItem item = items.get(currentItem);
            changeItem(item);
        }
        if (musicSynced) {
            if (LEDCubeManager.getLEDCube().getSpectrumAnalyzer().isPlaying()) {
                int time = LEDCubeManager.getLEDCube().getSpectrumAnalyzer().getPositionMillis();
                int lastItem = currentItem;
                for (int i = items.size() - 1; i >= 0; i--) {
                    SequenceItem item = items.get(i);
                    if (time >= item.time) {
                        currentItem = i;
                        break;
                    }
                }
                if (currentItem != lastItem) {
                    changeItem(items.get(currentItem));
                }
            }
        } else {
            SequenceItem item = items.get(currentItem);
            if (timer.getMilliseconds() >= item.time) {
                timer.restart();
                if (++currentItem >= items.size()) currentItem = 0;
                changeItem(items.get(currentItem));
            }
        }
    }

    public void start() {
        timer.restart();
        currentItem = -1;
        if (musicFile != null) LEDCubeManager.getLEDCube().getSpectrumAnalyzer().loadFile(musicFile.getAbsolutePath());
    }

    private void changeItem(SequenceItem item) {
        if (item.color != null) LEDCubeManager.setPaintColor(item.color);
        if (item.clear) {
            LEDManager ledManager = LEDCubeManager.getLEDManager();
            Dimension3D dim = ledManager.getDimensions();
            for (int x = 0; x < dim.x; x++) {
                for (int y = 0; y < dim.y; y++) {
                    for (int z = 0; z < dim.z; z++) {
                        ledManager.setLEDColor(x, y, z, new Color());
                    }
                }
            }
        }
        Animation anim = LEDCubeManager.getLEDCube().getAnimationByClassName("Animation" + item.animation);
        if (anim != null) {
            LEDCubeManager.getLEDCube().getCommThread().setCurrentAnimation(anim);
            LEDCubeManager.getInstance().getScreenMainControl().animComboBox.setSelectedItem(anim.getName());
        }
        if (item.options != null) {
            for (SequenceItem.Option option : item.options) {
                LEDCubeManager.getLEDCube().getCommThread().getCurrentAnimation().setOption(option.name, option.value);
            }
        }
    }

    public static AnimationSequence loadFromFile(File file) throws IOException {
        @Cleanup BufferedReader br = new BufferedReader(new FileReader(file));
        AnimationSequence sequence = new AnimationSequence();
        String line;
        boolean first = true;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.length() < 1 || line.charAt(0) == '#') continue;
            boolean clear = false;
            if (line.charAt(0) == '!') {
                clear = true;
                line = line.substring(1);
            }
            String[] split = line.split("\\s+", 6);
            if (first) {
                String str = line;
                if (str.charAt(0) == '@') {
                    str = str.substring(1);
                    if (str.charAt(0) == '@') {
                        sequence.musicSynced = true;
                        str = str.substring(1);
                    }
                    sequence.musicFile = new File(str);
                    continue;
                }
            } if (split.length == 5) {
                sequence.items.add(new SequenceItem(split[0], Long.parseLong(split[1]), clear, new Color(Integer.parseInt(split[2]), Integer.parseInt(split[3]), Integer.parseInt(split[4]))));
            } else if (split.length > 5) {
                List<SequenceItem.Option> options = new ArrayList<>();
                String[] args = split[5].split("\\s+");
                for (int i = 0; i < args.length; i++) {
                    String[] arg = args[i].split(":", 2);
                    options.add(new SequenceItem.Option(arg[0], arg[1]));
                }
                sequence.items.add(new SequenceItem(split[0], Long.parseLong(split[1]), clear, new Color(Integer.parseInt(split[2]), Integer.parseInt(split[3]), Integer.parseInt(split[4])), options.toArray(new SequenceItem.Option[options.size()])));
            } else if (split.length >= 2) {
                sequence.items.add(new SequenceItem(split[0], Long.parseLong(split[1]), clear));
            }
            first = false;
        }
        return sequence;
    }

    private static class SequenceItem {
        public final String animation;
        public final long time;
        public final boolean clear;
        public final Color color;
        public final Option[] options;

        public SequenceItem(String animation, long time, boolean clear) {
            this.animation = animation;
            this.time = time;
            this.clear = clear;
            this.color = null;
            this.options = null;
        }
        
        public SequenceItem(String animation, long time, boolean clear, Color color) {
            this.animation = animation;
            this.time = time;
            this.clear = clear;
            this.color = color;
            this.options = null;
        }

        public SequenceItem(String animation, long time, boolean clear, Color color, Option[] options) {
            this.animation = animation;
            this.time = time;
            this.clear = clear;
            this.color = color;
            this.options = options;
        }

        public static class Option {
            public final String name;
            public final String value;

            public Option(String name, String value) {
                this.name = name;
                this.value = value;
            }
        }
    }
}
