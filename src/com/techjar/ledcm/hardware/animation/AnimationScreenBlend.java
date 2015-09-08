
package com.techjar.ledcm.hardware.animation;

import java.awt.AWTException;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import lombok.SneakyThrows;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 * WARNING: This class is extremely specific to my hardware configuration and probably won't work for you without tweaking.
 *
 * @author Techjar
 */
public class AnimationScreenBlend extends Animation {
    int middleStart = 40;
    int rightStart = 70;
    Rectangle rect;

    public AnimationScreenBlend() {
        super();
    }

    @Override
    public String getName() {
        return "Screen Blend";
    }

    @Override @SneakyThrows(AWTException.class)
    public void refresh() {
        if (ticks % 12 == 0) {
            BufferedImage image = new Robot().createScreenCapture(rect);
            for (int i = 40; i < 70; i++) {
                ledManager.setLEDColor(i, 0, 0, ReadableColor.WHITE);
            }
        }
    }

    @Override
    public void reset() {
        rect = new Rectangle();
        GraphicsEnvironment localGE = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice gd : localGE.getScreenDevices()) {
            for (GraphicsConfiguration graphicsConfiguration : gd.getConfigurations()) {
                Rectangle.union(rect, graphicsConfiguration.getBounds(), rect);
            }
        }
    }
}
