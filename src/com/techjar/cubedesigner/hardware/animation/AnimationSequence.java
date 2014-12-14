
package com.techjar.cubedesigner.hardware.animation;

import com.techjar.cubedesigner.CubeDesigner;
import com.techjar.cubedesigner.util.Timer;
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
            if (CubeDesigner.getSpectrumAnalyzer().isPlaying()) {
                int time = CubeDesigner.getSpectrumAnalyzer().getPositionMillis();
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
        if (musicFile != null) CubeDesigner.getSpectrumAnalyzer().loadFile(musicFile.getAbsolutePath());
    }

    private void changeItem(SequenceItem item) {
        if (item.color != null) CubeDesigner.setPaintColor(item.color);
        Animation anim = CubeDesigner.getInstance().getAnimationByClassName("Animation" + item.animation);
        if (anim != null) {
            CubeDesigner.getSerialThread().setCurrentAnimation(anim);
            CubeDesigner.getInstance().getScreenMainControl().animComboBox.setSelectedItem(anim.getName());
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
                sequence.items.add(new SequenceItem(split[0], Long.parseLong(split[1]), new Color(Integer.parseInt(split[2]), Integer.parseInt(split[3]), Integer.parseInt(split[4]))));
            } else if (split.length > 5) {
                sequence.items.add(new SequenceItem(split[0], Long.parseLong(split[1]), new Color(Integer.parseInt(split[2]), Integer.parseInt(split[3]), Integer.parseInt(split[4])), split[5].split("\\s+")));
            } else if (split.length >= 2) {
                sequence.items.add(new SequenceItem(split[0], Long.parseLong(split[1])));
            }
            first = false;
        }
        return sequence;
    }

    private static class SequenceItem {
        public final String animation;
        public final long time;
        public final Color color;
        public final String[] args;

        public SequenceItem(String animation, long time) {
            this.animation = animation;
            this.time = time;
            this.color = null;
            this.args = null;
        }
        
        public SequenceItem(String animation, long time, Color color) {
            this.animation = animation;
            this.time = time;
            this.color = color;
            this.args = null;
        }

        public SequenceItem(String animation, long time, Color color, String[] args) {
            this.animation = animation;
            this.time = time;
            this.color = color;
            this.args = args;
        }
    }
}
