package com.techjar.cubedesigner;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.util.glu.GLU.*;

import com.techjar.cubedesigner.gui.GUICallback;
import com.techjar.cubedesigner.gui.screen.Screen;
import com.techjar.cubedesigner.util.Angle;
import com.techjar.cubedesigner.util.ArgumentParser;
import com.techjar.cubedesigner.util.Axis;
import com.techjar.cubedesigner.util.ConfigManager;
import com.techjar.cubedesigner.util.Constants;
import com.techjar.cubedesigner.util.Model;
import com.techjar.cubedesigner.util.Quaternion;
import com.techjar.cubedesigner.util.Util;
import com.techjar.cubedesigner.util.Vector2;
import com.techjar.cubedesigner.util.Vector3;
import com.techjar.cubedesigner.util.logging.LogHelper;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Value;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.vector.Matrix4f;
import org.newdawn.slick.UnicodeFont;

/**
 *
 * @author Techjar
 */
public class CubeDesigner {
    //public static final int SCREEN_WIDTH = 1024;
    //public static final int SCREEN_HEIGHT = 768;
    @Getter private static CubeDesigner instance;
    @Getter private static DisplayMode displayMode /*= new DisplayMode(1024, 768)*/;
    private DisplayMode newDisplayMode;
    private DisplayMode configDisplayMode;
    private boolean fullscreen;
    private boolean newFullscreen;
    @Getter private static ConfigManager config;
    @Getter private static JFrame frame;
    private List<DisplayMode> displayModeList;
    private Canvas canvas;
    private boolean closeRequested = false;
    private boolean running = false;
    @Getter private static TextureManager textureManager;
    @Getter private static ModelManager modelManager;
    @Getter private static FontManager fontManager;
    @Getter private static SoundManager soundManager;
    @Getter private static Camera camera;
    @Getter private static Frustum frustum;
    private List<Screen> screenList = new ArrayList<>();
    private List<ScreenHolder> screensToAdd = new ArrayList<>();
    private List<GUICallback> resizeHandlers = new ArrayList<>();
    private Map<String, Integer> validControllers = new HashMap<>();
    private FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(4);
    private FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
    private int fpsCounter;
    private int fpsRender;
    private long timeCounter;
    private long deltaTime;
    private long renderStart;
    private long faceCount;
    private boolean screenshot;
    private boolean regrab;
    public boolean renderDebug;
    public boolean wireframe;
    public final boolean antiAliasingSupported;
    public final int antiAliasingMaxSamples;
    private boolean antiAliasing = true;
    private int antiAliasingSamples = 4;
    private int multisampleFBO;
    private int multisampleTexture;
    private int multisampleDepthTexture;
    private int shadowMapSize = 1024;
    private int depthFBO;
    private int depthTexture;

    public CubeDesigner(String[] args) throws LWJGLException {
        instance = this;
        System.setProperty("sun.java2d.noddraw", "true");
        LogHelper.init(new File(Constants.DATA_DIRECTORY, "logs"));

        ArgumentParser.parse(args, new ArgumentParser.Argument(true, "--loglevel") {
            @Override
            public void runAction(String paramater) {
                LogHelper.setLevel(Level.parse(paramater));
            }
        }, new ArgumentParser.Argument(false, "--debug") {
            @Override
            public void runAction(String paramater) {
                renderDebug = true;
            }
        }, new ArgumentParser.Argument(false, "--wireframe") {
            @Override
            public void runAction(String paramater) {
                wireframe = true;
            }
        });

        Pbuffer pb = new Pbuffer(800, 600, new PixelFormat(32, 0, 24, 8, 0), null);
        pb.makeCurrent();
        antiAliasingMaxSamples = glGetInteger(GL_MAX_SAMPLES);
        antiAliasingSupported = antiAliasingMaxSamples > 0;
        pb.destroy();
        LogHelper.config("AA Supported: %s / Max Samples: %d", antiAliasingSupported ? "yes" : "no", antiAliasingMaxSamples);
    }

    public void start() throws LWJGLException {
        if (running) throw new IllegalStateException("Client already running!");
        running = true;
        //Runtime.getRuntime().addShutdownHook(new ShutdownThread());
        initDisplayModes();
        initConfig();

        Display.setDisplayMode(displayMode);
        makeFrame();
        
        Display.create();
        Keyboard.create();
        Mouse.create();

        Controllers.create();
        String defaultController = "";
        for (int i = 0; i < Controllers.getControllerCount(); i++) {
            Controller con = Controllers.getController(i);
            if (con.getAxisCount() >= 2) {
                validControllers.put(con.getName(), i);
                config.defaultProperty("controls.controller", con.getName());
                if (defaultController.isEmpty()) defaultController = con.getName();
                LogHelper.config("Found controller: %s (%d Rumblers)", con.getName(), con.getRumblerCount());
            }
        }
        if (validControllers.size() < 1) config.setProperty("controls.controller", "");
        else if (!validControllers.containsKey(config.getString("controls.controller"))) config.setProperty("controls.controller", defaultController);
        if (config.hasChanged()) config.save();

        textureManager = new TextureManager();
        init();

        modelManager = new ModelManager();
        fontManager = new FontManager();
        soundManager = new SoundManager();
        camera = new Camera();
        frustum = new Frustum();

        timeCounter = getTime();
        deltaTime = System.nanoTime();

        run();
    }

    private void makeFrame() throws LWJGLException {
        if (frame != null) frame.dispose();
        frame = new JFrame(Constants.APP_TITLE);
        frame.setLayout(new BorderLayout());
        frame.setResizable(false);
        frame.setAlwaysOnTop(false);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        canvas = new Canvas();

        /*canvas.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                newCanvasSize.set(canvas.getSize());
            }
        });*/

        frame.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                canvas.requestFocusInWindow();
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeRequested = true;
            }
        });

        frame.add(canvas, BorderLayout.CENTER);
        resizeFrame(false);
    }

    private void resizeFrame(boolean fullscreen) throws LWJGLException {
        Display.setParent(null);
        frame.dispose();
        frame.setUndecorated(fullscreen);
        if (fullscreen) GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(frame);
        else GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);
        canvas.setPreferredSize(new java.awt.Dimension(displayMode.getWidth(), displayMode.getHeight()));
        frame.pack();
        java.awt.Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((dim.width - frame.getSize().width) / 2, (dim.height - frame.getSize().height) / 2);
        frame.setVisible(true);
        Display.setParent(canvas);
    }

    private void shutdownInternal() throws LWJGLException {
        running = false;
        //if (config != null && config.hasChanged()) config.save();
        if (soundManager != null) soundManager.getSoundSystem().cleanup();
        if (textureManager != null) textureManager.cleanup();
        if (fontManager != null) fontManager.cleanup();
        Keyboard.destroy();
        Mouse.destroy();
        Display.destroy();
    }

    private long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    private float getDelta() {
        long time = System.nanoTime();
        float delta = (time - deltaTime) / 1000000000F;
        deltaTime = time;
        return delta;
    }

    public void run() throws LWJGLException {
        while (!Display.isCloseRequested() && !closeRequested) {
            try {
                runGameLoop();
            } catch (Exception ex) {
                ex.printStackTrace();
                closeRequested = true;
            }
        }
        shutdownInternal();
    }

    private void runGameLoop() throws LWJGLException {
        if (fullscreen && !frame.isFocused()) setFullscreen(false);
        if (newDisplayMode != null || newFullscreen != fullscreen) {
            if (newDisplayMode != null) {
                displayMode = newDisplayMode;
                configDisplayMode = newDisplayMode;
                config.setProperty("display.width", configDisplayMode.getWidth());
                config.setProperty("display.height", configDisplayMode.getHeight());
            }
            fullscreen = newFullscreen;
            newDisplayMode = null;
            useDisplayMode();
        }

        if (getTime() - timeCounter >= 1000) {
            fpsRender = fpsCounter;
            fpsCounter = 0;
            timeCounter += 1000;
        }
        fpsCounter++;


        soundManager.update();
        this.processKeyboard();
        this.processMouse();
        this.processController();
        this.update();
        this.render();
        Display.update();
    }

    private void setupAntiAliasing() {
        if (multisampleFBO != 0) {
            glDeleteTextures(multisampleTexture);
            glDeleteTextures(multisampleDepthTexture);
            glDeleteFramebuffers(multisampleFBO);
            multisampleTexture = 0;
            multisampleDepthTexture = 0;
            multisampleFBO = 0;
        }
        if (antiAliasing) {
            multisampleTexture = glGenTextures();
            multisampleDepthTexture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, multisampleTexture);
            glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, antiAliasingSamples, GL_RGBA8, displayMode.getWidth(), displayMode.getHeight(), false);
            glBindTexture(GL_TEXTURE_2D_MULTISAMPLE, multisampleDepthTexture);
            glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, antiAliasingSamples, GL_DEPTH_COMPONENT, displayMode.getWidth(), displayMode.getHeight(), false);
            //glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, displayMode.getWidth(), displayMode.getHeight(), 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_BYTE, (ByteBuffer)null);
            multisampleFBO = glGenFramebuffers();
            glBindFramebuffer(GL_FRAMEBUFFER, multisampleFBO);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D_MULTISAMPLE, multisampleTexture, 0);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D_MULTISAMPLE, multisampleDepthTexture, 0);
            if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
                throw new RuntimeException("Anti-aliasing framebuffer is invalid.");
            }
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
    }

    private void setupDepthTexture() {
        if (depthFBO != 0) {
            glDeleteFramebuffers(depthFBO);
            glDeleteTextures(depthTexture);
            depthFBO = 0;
            depthTexture = 0;
        }
        depthTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, depthTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT16, shadowMapSize, shadowMapSize, 0, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer)null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        depthFBO = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, depthFBO);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, depthTexture, 0);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Depth texture framebuffer is invalid.");
        }
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    private void initDisplayModes() throws LWJGLException {
        displayModeList = new ArrayList<>();
        DisplayMode desktop = Display.getDesktopDisplayMode();
        for (DisplayMode mode : Display.getAvailableDisplayModes()) {
            if(mode.getBitsPerPixel() == desktop.getBitsPerPixel() && mode.getFrequency() == desktop.getFrequency()) {
                displayModeList.add(mode);
                if (mode.getWidth() == 1024 && mode.getHeight() == 768) displayMode = mode;
            }
        }
        Collections.sort(displayModeList, new ResolutionSorter());
    }

    private void initConfig() {
        config = new ConfigManager(new File(Constants.DATA_DIRECTORY, "options.yml"));
        config.load();
        config.defaultProperty("display.width", displayMode.getWidth());
        config.defaultProperty("display.height", displayMode.getHeight());
        config.defaultProperty("display.antialiasing", true);
        config.defaultProperty("display.antialiasingsamples", 4);
        //config.defaultProperty("display.fullscreen", false);
        config.defaultProperty("sound.effectvolume", 1.0F);
        config.defaultProperty("sound.musicvolume", 1.0F);

        if (!internalSetDisplayMode(config.getInteger("display.width"), config.getInteger("display.height"))) {
            config.setProperty("display.width", displayMode.getWidth());
            config.setProperty("display.height", displayMode.getHeight());
        }
        antiAliasing = config.getBoolean("display.antialiasing");
        antiAliasingSamples = config.getInteger("display.antialiasingsamples");
        //fullscreen = config.getBoolean("display.fullscreen");

        if (!antiAliasingSupported) {
            antiAliasing = false;
            config.setProperty("display.antialiasing", false);
        } else if (antiAliasingSamples < 2 || antiAliasingSamples > antiAliasingMaxSamples || !Util.isPowerOfTwo(antiAliasingSamples)) {
            antiAliasingSamples = 4;
            config.setProperty("display.antialiasingsamples", 4);
        }

        /*if (config.getInteger("version") < Constants.VERSION) {
            config.setProperty("version", Constants.VERSION);
        }*/

        if (config.hasChanged()) config.save();
    }

    private void init() {
        initGL();
        resizeGL(displayMode.getWidth(), displayMode.getHeight());
        setupAntiAliasing();
    }

    private void initGL() {
        // 3D Initialization
        glClearColor(0, 0, 0, 0);
        glClearDepth(1);
        glDepthFunc(GL_LEQUAL);
        glDepthMask(true);
        glShadeModel(GL_SMOOTH);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glEnable(GL_LIGHTING);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_ALPHA_TEST);
        glEnable(GL_BLEND);
        glAlphaFunc(GL_GREATER, 0);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
        //glCullFace(GL_FRONT); // What

        // Setup lighting
        floatBuffer.rewind();
        floatBuffer.put(new float[]{1, 1, 1, 1});
        floatBuffer.rewind();
        glMaterial(GL_FRONT, GL_SPECULAR, floatBuffer);
        glMaterialf(GL_FRONT, GL_SHININESS, 50);

        floatBuffer.rewind();
        floatBuffer.put(new float[]{1, 1, 1, 0});
        floatBuffer.rewind();
        glLight(GL_LIGHT0, GL_POSITION, floatBuffer);

        //glColorMaterial(GL_FRONT, GL_AMBIENT_AND_DIFFUSE);
        //glEnable(GL_COLOR_MATERIAL);

        glEnable(GL_LIGHT0);
    }

    public void resizeGL(int width, int height) {
        // Viewport setup
        glViewport(0, 0, width, height);
    }

    private void processKeyboard() {
        toploop: while (Keyboard.next()) {
            for (Screen screen : screenList)
                if (screen.isVisible() && screen.isEnabled() && !screen.processKeyboardEvent()) continue toploop;
            //if (world != null && !world.processKeyboardEvent()) continue;
            if (Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_F2) { // TODO: Implement key binding system
                screenshot = true;
                continue;
            }
            if (Keyboard.getEventKeyState()) {
                if (Keyboard.getEventKey() == Keyboard.KEY_F11) setFullscreen(!fullscreen);
                if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
                    Mouse.setGrabbed(!Mouse.isGrabbed());
                    if (Mouse.isGrabbed()) Mouse.setCursorPosition(displayMode.getWidth() / 2, displayMode.getHeight() / 2);
                }
            }
            /*float moveSpeed = 0.01F;
            if (Keyboard.getEventKey() == Keyboard.KEY_W) {
                if (Keyboard.getEventKeyState()) camera.setVelocity(camera.getVelocity().add(camera.getAngle().forward().multiply(moveSpeed)));
                else camera.setVelocity(camera.getVelocity().subtract(camera.getAngle().forward().multiply(moveSpeed)));
            }
            if (Keyboard.getEventKey() == Keyboard.KEY_S) {
                if (Keyboard.getEventKeyState()) camera.setVelocity(camera.getVelocity().subtract(camera.getAngle().forward().multiply(moveSpeed)));
                else camera.setVelocity(camera.getVelocity().add(camera.getAngle().forward().multiply(moveSpeed)));
            }*/
        }
    }

    private void processMouse() {
        toploop: while (Mouse.next()) {
            for (Screen screen : screenList)
                if (screen.isVisible() && screen.isEnabled() && !screen.processMouseEvent()) continue toploop;
            //if (world != null && !world.processMouseEvent()) continue;
            //if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() && !asteroids.containsKey(getMousePos())) asteroids.put(getMousePos(), AsteroidGenerator.generate());
        }
    }

    private void processController() {
        toploop: while (Controllers.next()) {
            Controller con = Controllers.getEventSource();
            if (con.getName().equals(config.getString("controls.controller"))) {
                for (Screen screen : screenList)
                    if (screen.isVisible() && screen.isEnabled() && !screen.processControllerEvent(con)) continue toploop;
                //if (world != null && !world.processControllerEvent(con)) continue;
            }
        }
    }

    public void update() {
        long time = System.nanoTime();
        float delta = getDelta();

        camera.update(delta);
        textureManager.update(delta);

        /*Vector3 unit = new Vector3(0, 0, -1);
        //Angle angle = new Angle(90, 0, 0);
        Angle angle = new Angle(253, 45, 111);
        System.out.println(angle);
        Vector3 vector = angle.forward();
        System.out.println(vector);
        //Angle angle2 = new Vector3().angle(vector);
        //System.out.println(angle2);
        System.out.println(new Angle(new Quaternion(angle)));
        System.out.println(new Angle(new Quaternion(angle)).forward());
        //System.out.println(unit.multiply(new Quaternion(angle).getMatrix()));
        //System.out.println(new Quaternion(new Vector3(1, 0, 0), 90).multiply(unit));
        //System.out.println(unit.multiply(angle.getMatrix()));
        //System.out.println(new Quaternion(angle));
        //System.out.println(angle2.forward());
        System.out.println(" ");*/
        //System.out.println(fpsRender);

        //camera.setAngle(new Angle(70, camera.getAngle().getYaw() + 1, 0));
        //camera.setAngle(camera.getAngle().rotate(Axis.YAW, 1));
        //camera.setAngle(camera.getAngle().rotate(Axis.ROLL, 1));
        //camera.setAngle(new Angle(0, camera.getAngle().getYaw() + 1, camera.getAngle().getRoll() + 1));
        //camera.setPosition(new Vector3(0, 0, -2));

        Iterator<Screen> it = screenList.iterator();
        while (it.hasNext()) {
            Screen screen = it.next();
            if (screen.isRemoveRequested()) it.remove();
            else if (screen.isVisible() && screen.isEnabled()) screen.update(delta);
        }

        for (ScreenHolder holder : screensToAdd) {
            if (holder.getIndex() < 0) {
                screenList.add(holder.getScreen());
            } else {
                screenList.add(holder.getIndex(), holder.getScreen());
            }
        }
        screensToAdd.clear();

        if (regrab) {
            Mouse.setGrabbed(true);
            regrab = false;
        }
    }

    @SneakyThrows(IOException.class)
    public void render() {
        checkGLError("Pre render");
        renderStart = System.nanoTime();
        if (antiAliasing) glBindFramebuffer(GL_DRAW_FRAMEBUFFER, multisampleFBO);
        
        // Setup and render 3D
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(45, (float)displayMode.getWidth() / (float)displayMode.getHeight(), 0.1F, 1000);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_LIGHTING);
        glBindTexture(GL_TEXTURE_2D, 0);
        if (wireframe) glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        render3D();

        // Setup and render 2D
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, displayMode.getWidth(), displayMode.getHeight(), 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        //glClear(GL_DEPTH_BUFFER_BIT);
        glDisable(GL_LIGHTING);
        glBindTexture(GL_TEXTURE_2D, 0);
        if (wireframe) glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        render2D();

        if (antiAliasing) {
            glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
            glBindFramebuffer(GL_READ_FRAMEBUFFER, multisampleFBO);
            glBlitFramebuffer(0, 0, displayMode.getWidth(), displayMode.getHeight(), 0, 0, displayMode.getWidth(), displayMode.getHeight(), GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT, GL_NEAREST);
        }
        if (screenshot) {
            screenshot = false;
            ByteBuffer buffer = BufferUtils.createByteBuffer(displayMode.getWidth() * displayMode.getHeight() * 3);
            glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
            glReadPixels(0, 0, displayMode.getWidth(), displayMode.getHeight(), GL_RGB, GL_UNSIGNED_BYTE, buffer);
            BufferedImage image = new BufferedImage(displayMode.getWidth(), displayMode.getHeight(), BufferedImage.TYPE_INT_RGB);
            buffer.rewind();
            for (int y = 0; y < displayMode.getHeight(); y++) {
                for (int x = 0; x < displayMode.getWidth(); x++) {
                    image.setRGB(x, displayMode.getHeight() - y - 1, (buffer.get() & 0xFF) << 16 | (buffer.get() & 0xFF) << 8 | (buffer.get() & 0xFF));
                }
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
            File screenshotDir = new File(Constants.DATA_DIRECTORY, "screenshots");
            screenshotDir.mkdirs();
            File file = new File(screenshotDir, dateFormat.format(Calendar.getInstance().getTime()) + ".png");
            for (int i = 2; file.exists(); i++) {
                file = new File(screenshotDir, dateFormat.format(Calendar.getInstance().getTime()) + "_" + i + ".png");
            }
            ImageIO.write(image, "png", file);
        }
        checkGLError("Post render");
    }

    public void render2D() {
        glPushMatrix();

        long renderTime = System.nanoTime() - renderStart;
        if (renderDebug) {
            Runtime runtime = Runtime.getRuntime();
            UnicodeFont debugFont = fontManager.getFont("batmfa_", 20, false, false).getUnicodeFont();
            int y = 0;
            debugFont.drawString(5, 5 + y++ * 25, "FPS: " + fpsRender, org.newdawn.slick.Color.yellow);
            debugFont.drawString(5, 5 + y++ * 25, "Memory: " + Util.bytesToMBString(runtime.totalMemory() - runtime.freeMemory()) + " / " + Util.bytesToMBString(runtime.maxMemory()), org.newdawn.slick.Color.yellow);
            //debugFont.drawString(5, 5 + y++ * 25, "Update time: " + (updateTime / 1000000D), org.newdawn.slick.Color.yellow);
            debugFont.drawString(5, 5 + y++ * 25, "Render time: " + (renderTime / 1000000D), org.newdawn.slick.Color.yellow);
            Vector3 vector = camera.getAngle().forward();
            debugFont.drawString(5, 5 + y++ * 25, "Camera vector: " + vector.getX() + ", " + vector.getY() + ", " + vector.getZ(), org.newdawn.slick.Color.yellow);
            //vector = camera.getPosition();
            //debugFont.drawString(5, 5 + y++ * 25, "Camera position: " + vector.getX() + ", " + vector.getY() + ", " + vector.getZ(), org.newdawn.slick.Color.yellow);
            //debugFont.drawString(5, 5 + y++ * 25, "Cursor position: " + Util.getMouseX() + ", " + Util.getMouseY(), org.newdawn.slick.Color.yellow);
            //debugFont.drawString(5, 5 + y++ * 25, "Cursor offset: " + (Util.getMouseX() - getWidth() / 2) + ", " + (Util.getMouseY() - getHeight() / 2 + 1), org.newdawn.slick.Color.yellow);
            debugFont.drawString(5, 5 + y++ * 25, "Rendered faces: " + faceCount, org.newdawn.slick.Color.yellow);
            //debugFont.drawString(5, 5 + y++ * 25, "Entities: " + (world != null ? world.getEntityCount() : 0), org.newdawn.slick.Color.yellow);
        }
        
        glPopMatrix();
    }

    public void render3D() {
        glPushMatrix();

        // Position and orient the camera
        Vector3 camPos = camera.getPosition();
        //Vector3 camLook = camera.getAngle().forward();
        //Vector3 camLookPos = camPos.add(camLook);
        //glRotatef(camera.getAngle().getRoll(), 0, 0, 1);
        //gluLookAt(camPos.getX(), camPos.getY(), camPos.getZ(), camLookPos.getX(), camLookPos.getY(), camLookPos.getZ(), 0, 1, 0);
        glRotatef(camera.getAngle().getRoll(), 0, 0, -1);
        glRotatef(camera.getAngle().getPitch(), -1, 0, 0);
        glRotatef(camera.getAngle().getYaw(), 0, -1, 0);
        glTranslatef(-camPos.getX(), -camPos.getY(), -camPos.getZ());
        frustum.update();

        floatBuffer.rewind();
        floatBuffer.put(new float[]{1, 1, 1, 0});
        floatBuffer.rewind();
        glLight(GL_LIGHT0, GL_POSITION, floatBuffer);
        faceCount = 0;

        String[] modelNames = new String[]{"ohgod.model"/*, "dentsphere.model", "cube.model"*/};
        float mult = 5;
        Random rand = new Random(1000);

        int width = 10;
        int length = 1;
        int height = 1;
        int size = width * length * height * 3 * 4;
        if (posBuf == null || posBuf.capacity() != size) posBuf = BufferUtils.createByteBuffer(size);
        if (posVbo == 0) posVbo = glGenBuffers();
        posBuf.rewind();
        posBuf.limit(posBuf.capacity());
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    float xx = x * mult;
                    float yy = y * mult;
                    float zz = z * mult;
                    posBuf.putFloat(xx);
                    posBuf.putFloat(yy);
                    posBuf.putFloat(zz);
                    Model model = modelManager.getModel(modelNames[rand.nextInt(modelNames.length)]);
                    if (model.isInFrustum(new Vector3(xx, yy, zz))) {
                        faceCount += model.getFaceCount();
                    }
                    /*Vector3 center = model.getCenter();
                    if (frustum.sphereInFrustum(center.getX() + xx, center.getY() + yy, center.getZ() + zz, model.getRadius()) > 0) {
                        glTranslatef(xx, yy, zz);
                        glRotatef(180, 0, 1, 0);
                        model.render();
                        faceCount += model.getFaceCount();
                        glRotatef(-180, 0, 1, 0);
                        glTranslatef(-xx, -yy, -zz);
                    }*/
                    //if (model.render(new Vector3(xx, yy, zz), new Quaternion()))
                        //faceCount += model.getFaceCount();
                }
            }
        }
        posBuf.limit(posBuf.position());
        posBuf.rewind();
        glBindBuffer(GL_ARRAY_BUFFER, posVbo);
        glBufferData(GL_ARRAY_BUFFER, posBuf, GL_STREAM_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);


        /*
        if (vbo == 0) throw new IllegalStateException("VBO not initialized");
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_NORMAL_ARRAY);
        if (hasTexCoords) glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glVertexPointer(3, GL_FLOAT, hasTexCoords ? 22 : 18, 0);
        glNormalPointer(GL_HALF_FLOAT, hasTexCoords ? 22 : 18, 12);
        if (hasTexCoords) glTexCoordPointer(2, GL_HALF_FLOAT, 22, 18);
        glDrawArrays(GL_TRIANGLES, 0, indices);
        glDisableClientState(GL_VERTEX_ARRAY);
        glDisableClientState(GL_NORMAL_ARRAY);
        if (hasTexCoords) glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        */

        /*glTranslatef(0, 0, -5);
        modelManager.getModel(model).render();
        glTranslatef(0, 0, 10);
        modelManager.getModel(model).render();
        glTranslatef(-5, 0, -5);
        modelManager.getModel(model).render();
        glTranslatef(10, 0, 0);
        modelManager.getModel(model).render();

        glTranslatef(-30, 0, 0);
        
        glTranslatef(0, 0, -5);
        modelManager.getModel(model).render();
        glTranslatef(0, 0, 10);
        modelManager.getModel(model).render();
        glTranslatef(-5, 0, -5);
        modelManager.getModel(model).render();
        glTranslatef(10, 0, 0);
        modelManager.getModel(model).render();*/
        
        glPopMatrix();
    }private int posVbo; private ByteBuffer posBuf;

    private void checkGLError(String stage) {
        int error;
        while ((error = glGetError()) != GL_NO_ERROR) {
            LogHelper.severe("########## GL ERROR ##########");
            LogHelper.severe("@ %s", stage);
            LogHelper.severe("%d: %s", error, gluErrorString(error));
        }
    }

    public DisplayMode findDisplayMode(int width, int height) {
        for (DisplayMode mode : displayModeList) {
            if(mode.getWidth() == width && mode.getHeight() == height) {
                return mode;
            }
        }
        return null;
    }

    public void useDisplayMode() throws LWJGLException {
        try {
            regrab = Mouse.isGrabbed();
            Mouse.setGrabbed(false);
            DisplayMode desktopMode = Display.getDesktopDisplayMode();
            if (fullscreen) {
                if (!desktopMode.equals(displayMode)) displayMode = desktopMode;
            } else displayMode = configDisplayMode;
            resizeFrame(fullscreen);
            Display.setDisplayMode(displayMode);
            resizeGL(displayMode.getWidth(), displayMode.getHeight());
            setupAntiAliasing();
            for (GUICallback callback : resizeHandlers) {
                callback.run();
            }
        }
        catch (LWJGLException ex) {
            ex.printStackTrace();
            shutdownInternal();
        }
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.newDisplayMode = displayMode;
    }

    public boolean setDisplayMode(int width, int height) {
        DisplayMode mode = findDisplayMode(width, height);
        if (mode != null) {
            setDisplayMode(mode);
            return true;
        }
        return false;
    }

    private boolean internalSetDisplayMode(int width, int height) {
        DisplayMode mode = findDisplayMode(width, height);
        if (mode != null) {
            displayMode = mode;
            configDisplayMode = mode;
            return true;
        }
        return false;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        this.newFullscreen = fullscreen;
    }

    public int addResizeHandler(GUICallback resizeHandler) {
        if (resizeHandlers.add(resizeHandler))
            return resizeHandlers.indexOf(resizeHandler);
        return -1;
    }

    public boolean removeResizeHandler(GUICallback resizeHandler) {
        return resizeHandlers.remove(resizeHandler);
    }

    public GUICallback removeResizeHandler(int index) {
        return resizeHandlers.remove(index);
    }

    public void clearResizeHandlers() {
        resizeHandlers.clear();
    }

    public List<Screen> getScreenList() {
        return Collections.unmodifiableList(screenList);
    }

    public void addScreen(Screen screen) {
        screensToAdd.add(new ScreenHolder(-1, screen));
    }

    public void addScreen(int index, Screen screen) {
        screensToAdd.add(new ScreenHolder(index, screen));
    }

    public boolean removeScreen(Screen screen) {
        boolean ret = screenList.remove(screen);
        if (ret) screen.remove();
        return ret;
    }

    public Screen removeScreen(int index) {
        Screen screen = screenList.get(index);
        screen.remove();
        return screen;
    }

    public void clearScreens() {
        for (Screen screen : screenList)
            screen.remove();
        screenList.clear();
    }

    public static int getWidth() {
        return displayMode.getWidth();
    }

    public static int getHeight() {
        return displayMode.getHeight();
    }

    @Value private class ScreenHolder {
        private int index;
        private Screen screen;
    }
}
