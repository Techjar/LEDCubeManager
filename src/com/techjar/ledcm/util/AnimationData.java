
package com.techjar.ledcm.util;

import com.techjar.ledcm.hardware.manager.LEDManager;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public class AnimationData {
    public static final int VERSION = 1;
    private AnimationFrame[] frames;
    private int width;
    private int length;
    private int height;

    public AnimationData(int width, int length, int height, int frameCount) {
        this.width = width;
        this.length = length;
        this.height = height;
        this.frames = new AnimationFrame[frameCount];
        for (int i = 0; i < frameCount; i++) {
            this.frames[i] = new AnimationFrame(width, length, height);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getLength() {
        return length;
    }

    public int getHeight() {
        return height;
    }

    public AnimationData(int frameCount) {
        this(8, 8, 8, frameCount);
    }

    public AnimationFrame getFrame(int index) {
        return frames[index];
    }

    public AnimationFrame[] getFrames() {
        return frames;
    }

    public void saveFile(File file) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            out.writeInt(VERSION);
            out.writeInt(width);
            out.writeInt(length);
            out.writeInt(height);
            out.writeInt(frames.length);
            for (int i = 0; i < frames.length; i++) {
                frames[i].writeData(out);
            }
            out.flush();
        }
    }

    public static AnimationData loadFile(File file) throws IOException {
        AnimationData anim;
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            int version = in.readInt();
            anim = new AnimationData(in.readInt(), in.readInt(), in.readInt(), in.readInt());
            for (int i = 0; i < anim.frames.length; i++) {
                anim.frames[i].readData(in);
            }
        }
        return anim;
    }

    public static class AnimationFrame {
        private final int width;
        private final int length;
        private final int height;
        private float frameTime;
        private byte[] red;
        private byte[] green;
        private byte[] blue;

        public AnimationFrame(int width, int length, int height) {
            this.width = width;
            this.length = length;
            this.height = height;
            int count = width * length * height;
            red = new byte[count];
            green = new byte[count];
            blue = new byte[count];
        }

        public int getWidth() {
            return width;
        }

        public int getLength() {
            return length;
        }

        public int getHeight() {
            return height;
        }

        public Color getLEDColor(int x, int y, int z) {
            int index = x + z * width + y * width * length;
            return new Color(red[index] & 0xFF, green[index] & 0xFF, blue[index] & 0xFF);
        }

        public void setLEDColor(int x, int y, int z, Color color) {
            int index = x + z * width + y * width * length;
            red[index] = color.getRedByte();
            green[index] = color.getGreenByte();
            blue[index] = color.getBlueByte();
        }

        public void loadFrame(LEDManager ledManager) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < length; z++) {
                    for (int y = 0; y < height; y++) {
                        int index = x + z * width + y * width * length;
                        ledManager.setLEDColor(x, y, z, new Color(red[index] & 0xFF, green[index] & 0xFF, blue[index] & 0xFF));
                    }
                }
            }
        }

        public void readData(DataInputStream in) throws IOException {
            frameTime = in.readFloat();
            for (int i = 0; i < red.length; i++) {
                red[i] = in.readByte();
                green[i] = in.readByte();
                blue[i] = in.readByte();
            }
        }

        public void writeData(DataOutputStream out) throws IOException {
            out.writeFloat(frameTime);
            for (int i = 0; i < red.length; i++) {
                out.writeByte(red[i]);
                out.writeByte(green[i]);
                out.writeByte(blue[i]);
            }
        }
    }
}
