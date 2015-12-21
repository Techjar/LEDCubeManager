
package com.techjar.ledcm;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;
import lombok.Getter;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;

/**
 *
 * @author Techjar
 */
public enum CursorType {
    DEFAULT("cursor/default.png", 0, 0),
    MOVE("cursor/move.png", 12, 12),
    N_RESIZE("cursor/resize_ns.png", 12, 12),
    S_RESIZE("cursor/resize_ns.png", 12, 12),
    E_RESIZE("cursor/resize_ew.png", 12, 12),
    W_RESIZE("cursor/resize_ew.png", 12, 12),
    NE_RESIZE("cursor/resize_nesw.png", 12, 12),
    NW_RESIZE("cursor/resize_nwse.png", 12, 12),
    SE_RESIZE("cursor/resize_nwse.png", 12, 12),
    SW_RESIZE("cursor/resize_nesw.png", 12, 12);

    private static boolean loaded = false;
    private final String image;
    private final int xHotspot;
    private final int yHotspot;
    @Getter private Cursor cursor;

    CursorType(String image, int xHotspot, int yHotspot) {
        this.image = image;
        this.xHotspot = xHotspot;
        this.yHotspot = yHotspot;
    }

    public static void loadCursors() throws IOException, LWJGLException {
        if (loaded) throw new IllegalStateException("Already loaded!");
        for (CursorType ct : CursorType.values()) {
            BufferedImage image = ImageIO.read(new File("resources/textures", ct.image));
            IntBuffer buf = IntBuffer.allocate(image.getWidth() * image.getHeight());
            for (int y = image.getHeight() - 1; y >= 0; y--) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int color = image.getRGB(x, y);
                    if ((Cursor.getCapabilities() & Cursor.CURSOR_8_BIT_ALPHA) == 0) {
                        int alpha = (color >>> 24);
                        alpha = alpha < 128 ? 0 : 255;
                        color &= ~(255 << 24);
                        color |= alpha << 24;
                    }
                    buf.put(color);
                }
            }
            buf.rewind();
            ct.cursor = new Cursor(image.getWidth(), image.getHeight(), ct.xHotspot, (image.getHeight() - 1) - ct.yHotspot, 1, buf, null);
        }
        loaded = true;
    }
}
