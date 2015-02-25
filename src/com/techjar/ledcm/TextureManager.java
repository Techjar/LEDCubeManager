package com.techjar.ledcm;


import com.techjar.ledcm.util.Util;
import static org.lwjgl.opengl.GL11.*;

import com.techjar.ledcm.util.json.TextureMeta;
import com.techjar.ledcm.util.json.TextureMeta.Animation.Frame;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.lwjgl.BufferUtils;
import org.newdawn.slick.Image;
import org.newdawn.slick.opengl.PNGDecoder;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureImpl;
import org.newdawn.slick.opengl.TextureLoader;

/**
 *
 * @author Techjar
 */
public class TextureManager {
    protected final File texturePath;
    protected Map<String, Texture> cache;
    protected Map<String, TextureAnimated> animated;
    protected Map<String, Image> imageCache;
    protected static final Constructor<Image> imageConstructor;

    static { // We have to reflectively initialize Image to bypass the clampTexture() call...
        try {
            imageConstructor = Image.class.getDeclaredConstructor();
            imageConstructor.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public TextureManager() {
        texturePath = new File("resources/textures/");
        cache = new HashMap<>();
        animated = new HashMap<>();
        imageCache = new HashMap<>();
    }

    public void update(float delta) {
        for (TextureAnimated texture : animated.values()) {
            texture.update(delta);
        }
    }

    @SneakyThrows(IOException.class)
    public Texture getTexture(String file, int filter) {
        Texture cached = cache.get(file);
        if (cached != null) return cached;
        Texture tex;
        String fileSub = file.substring(file.lastIndexOf('.') + 1).toLowerCase();
        @Cleanup FileInputStream inputStream = new FileInputStream(new File(texturePath, file));
        File metaFile = new File(texturePath, file + ".meta");
        if (metaFile.exists()) {
            @Cleanup FileReader fr = new FileReader(metaFile);
            TextureMeta meta = Util.GSON.fromJson(fr, TextureMeta.class);
            if (meta.animation != null) {
                if (meta.animation.frametime <= 0) {
                    throw new IllegalArgumentException("Frame time must be greater than zero.");
                }
                tex = loadAnimatedTexture(fileSub, inputStream, filter, meta);
                animated.put(file, (TextureAnimated)tex);
            } else {
                tex = TextureLoader.getTexture(fileSub, inputStream, filter);
            }
        } else {
            tex = TextureLoader.getTexture(fileSub, inputStream, filter);
        }
        cache.put(file, tex);
        return tex;
    }
    
    public Texture getTexture(String file) {
        return getTexture(file, GL_LINEAR);
    }

    @SneakyThrows(Exception.class)
    public Image getImage(String file, int filter) {
        Image cached = imageCache.get(file);
        if (cached != null) return cached;
        Image img = imageConstructor.newInstance();
        img.setTexture(getTexture(file));
        imageCache.put(file, img);
        return img;
    }

    public Image getImage(String file) {
        return getImage(file, GL_LINEAR);
    }

    private TextureAnimated loadAnimatedTexture(String ref, InputStream in, int filter, TextureMeta meta) throws IOException {
        PNGDecoder decoder = new PNGDecoder(new BufferedInputStream(in));
        if (!decoder.isRGB()) throw new IOException("Only RGB formatted images are supported by the PNGDecoder");

        boolean hasAlpha = decoder.hasAlpha();
        int componentCount = hasAlpha ? 4 : 3;
        //int pixelFormat = hasAlpha ? GL_RGBA : GL_RGB;
        int width = decoder.getWidth() / meta.animation.width;
        int height = decoder.getHeight() / meta.animation.height;
        int texWidth = Util.getNextPowerOfTwo(width);
        int texHeight = Util.getNextPowerOfTwo(height);

        ByteBuffer buffer = ByteBuffer.allocate(decoder.getWidth() * decoder.getHeight() * componentCount);
        decoder.decode(buffer, decoder.getWidth() * componentCount, hasAlpha ? PNGDecoder.RGBA : PNGDecoder.RGB);

        int max = glGetInteger(GL_MAX_TEXTURE_SIZE);
        if (texWidth > max || texHeight > max) {
            throw new IOException("Attempted to allocate a texture too big for the current hardware.");
        }

        List<byte[]> textureData = new ArrayList<>();
        for (int y = 0; y < decoder.getHeight(); y += height) {
            for (int x = 0; x < decoder.getWidth(); x += width) {
                byte[] bytes = new byte[texWidth * texHeight * componentCount];
                for (int y2 = 0; y2 < height; y2++) {
                    buffer.position(((y + y2) * decoder.getWidth() + x) * componentCount);
                    buffer.get(bytes, texWidth * y2 * componentCount, width * componentCount);
                }
                textureData.add(bytes);
            }
        }

        int textureID = glGenTextures();
        TextureAnimated texture = new TextureAnimated(ref, GL_TEXTURE_2D, textureID, meta, textureData, BufferUtils.createByteBuffer(texWidth * texHeight * componentCount));
    	texture.setTextureWidth(texWidth);
    	texture.setTextureHeight(texHeight);
        texture.setWidth(width);
        texture.setHeight(height);
        texture.setAlpha(hasAlpha);

        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, texWidth, texHeight, 0, hasAlpha ? GL_RGBA : GL_RGB, GL_UNSIGNED_BYTE, (ByteBuffer)null);
        texture.uploadCurrentFrame();

        return texture;
    }
    
    public void unloadTexture(String file) {
        if (cache.containsKey(file)) {
            cache.remove(file).release();
            animated.remove(file);
            imageCache.remove(file);
        }
    }
    
    public void cleanup() {
        for (Texture texture : cache.values())
            texture.release();
        cache.clear();
        animated.clear();
        imageCache.clear();
    }

    public class TextureAnimated extends TextureImpl {
        private ByteBuffer buffer;
        private List<byte[]> textureData;
        private Frame[] frameInfo;
        private int currentFrame;
        private float timeCounter;

        public TextureAnimated(String ref, int target, int textureID, TextureMeta meta, List<byte[]> textureData, ByteBuffer buffer) {
            super(ref, target, textureID);
            this.buffer = buffer;
            this.textureData = textureData;
            this.frameInfo = meta.animation.frames;
            if (frameInfo == null || frameInfo.length < 1) {
                frameInfo = new Frame[textureData.size()];
                for (int i = 0; i < textureData.size(); i++) {
                    frameInfo[i] = new Frame();
                    frameInfo[i].index = i;
                    frameInfo[i].time = meta.animation.frametime;
                }
            } else {
                for (Frame frame : frameInfo) {
                    if (frame.index >= textureData.size()) {
                        throw new IllegalArgumentException("Invalid frame index: " + frame.index);
                    }
                    if (frame.time <= 0) {
                        frame.time = meta.animation.frametime;
                    }
                }
            }
        }

        public void update(float delta) {
            timeCounter += delta;
            int lastFrame = currentFrame;
            while (timeCounter >= frameInfo[currentFrame].time) {
                timeCounter -= frameInfo[currentFrame].time;
                currentFrame++;
                if (currentFrame >= frameInfo.length) {
                    currentFrame = 0;
                }
            }
            if (currentFrame != lastFrame) {
                uploadCurrentFrame();
            }
        }

        public void uploadCurrentFrame() {
            buffer.rewind();
            buffer.put(textureData.get(frameInfo[currentFrame].index));
            buffer.rewind();
            glBindTexture(GL_TEXTURE_2D, getTextureID());
            glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, getTextureWidth(), getTextureHeight(), hasAlpha() ? GL_RGBA : GL_RGB, GL_UNSIGNED_BYTE, buffer);
        }
    }
}
