package com.techjar.ledcm;

import com.techjar.ledcm.hardware.animation.AnimationStacker;
import com.techjar.ledcm.hardware.animation.AnimationFaucetFill;
import com.techjar.ledcm.hardware.animation.AnimationRandomize;
import com.techjar.ledcm.hardware.animation.AnimationPulsateHue;
import com.techjar.ledcm.hardware.animation.AnimationSnakeBattle;
import com.techjar.ledcm.hardware.animation.AnimationFaucet;
import com.techjar.ledcm.hardware.animation.AnimationSnakeInfinite;
import com.techjar.ledcm.hardware.animation.AnimationTwinkle;
import com.techjar.ledcm.hardware.animation.AnimationSpectrumBars;
import com.techjar.ledcm.hardware.animation.AnimationDrain;
import com.techjar.ledcm.hardware.animation.AnimationCandyCaneStacker;
import com.techjar.ledcm.hardware.animation.AnimationMatrix;
import com.techjar.ledcm.hardware.animation.AnimationScrollers;
import com.techjar.ledcm.hardware.animation.AnimationMultiFaucet;
import com.techjar.ledcm.hardware.animation.AnimationSnake;
import com.techjar.ledcm.hardware.animation.Animation;
import com.techjar.ledcm.hardware.animation.AnimationIndividualTest;
import com.techjar.ledcm.hardware.animation.AnimationBlink;
import com.techjar.ledcm.hardware.animation.AnimationFaucetFillRainbow;
import com.techjar.ledcm.hardware.animation.AnimationStrobe;
import com.techjar.ledcm.hardware.animation.AnimationSine;
import com.techjar.ledcm.hardware.animation.AnimationNone;
import com.techjar.ledcm.hardware.animation.AnimationSineDouble;
import com.techjar.ledcm.hardware.animation.AnimationRainbowStacker;
import com.techjar.ledcm.hardware.animation.AnimationStaticFill;
import com.techjar.ledcm.hardware.animation.AnimationProgressiveFill;
import com.techjar.ledcm.hardware.animation.AnimationRain;
import com.techjar.ledcm.hardware.animation.AnimationPulsate;
import com.techjar.ledcm.hardware.animation.AnimationSpectrumShooters;
import com.techjar.ledcm.hardware.SpectrumAnalyzer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.opengl.GL41.*;
import static org.lwjgl.util.glu.GLU.*;

import com.hackoeur.jglm.Mat3;
import com.hackoeur.jglm.Mat4;
import com.hackoeur.jglm.Matrices;
import com.obj.WavefrontObject;
import com.techjar.ledcm.gui.GUICallback;
import com.techjar.ledcm.gui.screen.Screen;
import com.techjar.ledcm.gui.screen.ScreenMainControl;
import com.techjar.ledcm.hardware.LEDManager;
import com.techjar.ledcm.hardware.ArduinoLEDManager;
import com.techjar.ledcm.hardware.CommThread;
import com.techjar.ledcm.util.Angle;
import com.techjar.ledcm.util.ArgumentParser;
import com.techjar.ledcm.util.Axis;
import com.techjar.ledcm.util.ConfigManager;
import com.techjar.ledcm.util.Constants;
import com.techjar.ledcm.util.Direction;
import com.techjar.ledcm.util.LightSource;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Model;
import com.techjar.ledcm.util.OperatingSystem;
import com.techjar.ledcm.util.Quaternion;
import com.techjar.ledcm.util.ShaderProgram;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector2;
import com.techjar.ledcm.util.Vector3;
import com.techjar.ledcm.util.logging.LogHelper;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;
import lombok.Getter;
import lombok.Setter;
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
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.Color;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.newdawn.slick.UnicodeFont;

/**
 *
 * @author Techjar
 */
public class LEDCubeManager {
    //public static final int SCREEN_WIDTH = 1024;
    //public static final int SCREEN_HEIGHT = 768;
    @Getter private static LEDCubeManager instance;
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
    @Getter private static SpectrumAnalyzer spectrumAnalyzer;
    @Getter private static JFileChooser fileChooser;
    private List<Screen> screenList = new ArrayList<>();
    private List<ScreenHolder> screensToAdd = new ArrayList<>();
    private List<GUICallback> resizeHandlers = new ArrayList<>();
    private Map<String, Integer> validControllers = new HashMap<>();
    private Map<String, Animation> animations = new HashMap<>();
    private List<String> animationNames = new ArrayList<>();
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
    public boolean renderFPS;
    public boolean renderDebug;
    public boolean wireframe;
    public final boolean antiAliasingSupported;
    public final int antiAliasingMaxSamples;
    private boolean antiAliasing = true;
    private int antiAliasingSamples = 4;
    private float fieldOfView;
    private int multisampleFBO;
    private int multisampleTexture;
    private int multisampleDepthTexture;
    private int shadowMapSize = 1024;
    private int depthFBO;
    private int depthTexture;

    // Screens
    @Getter private ScreenMainControl screenMainControl;

    // Really import OpenGL matrix stuff
    private Mat4 projectionMatrix;
    private Matrix4f viewMatrix;
    public Matrix4f modelMatrix;

    // Shaders
    private ShaderProgram progInstanceDraw;
    private int sampler0;

    // Arduino stuff
    private static LEDManager ledManager;
    private static boolean drawClick;
    private static boolean trueColor;
    @Getter private static CommThread commThread;
    @Getter private static String serialPortName = "COM3";
    @Getter private static int serverPort = 7545;
    @Getter private static Color paintColor = new Color(255, 255, 255);
    @Getter private static boolean[] highlight = new boolean[512];
    @Getter private static Vector3 paintSize = new Vector3(0, 0, 0);
    @Getter @Setter private static int layerIsolation = 0;
    @Getter @Setter private static int selectedLayer = 0;
    @Getter @Setter private static boolean convertingAudio;

    public LEDCubeManager(String[] args) throws LWJGLException {
        instance = this;
        System.setProperty("sun.java2d.noddraw", "true");
        LogHelper.init(new File(Constants.DATA_DIRECTORY, "logs"));
        LongSleeperThread.startSleeper();

        ArgumentParser.parse(args, new ArgumentParser.Argument(true, "--loglevel") {
            @Override
            public void runAction(String parameter) {
                LogHelper.setLevel(Level.parse(parameter));
            }
        }, new ArgumentParser.Argument(false, "--showfps") {
            @Override
            public void runAction(String parameter) {
                renderFPS = true;
            }
        }, new ArgumentParser.Argument(false, "--debug") {
            @Override
            public void runAction(String parameter) {
                renderDebug = true;
            }
        }, new ArgumentParser.Argument(false, "--wireframe") {
            @Override
            public void runAction(String parameter) {
                wireframe = true;
            }
        }, new ArgumentParser.Argument(true, "--serialport") {
            @Override
            public void runAction(String parameter) {
                serialPortName = parameter;
            }
        }, new ArgumentParser.Argument(true, "--serverport") {
            @Override
            public void runAction(String parameter) {
                serverPort = Integer.parseInt(parameter);
            }
        });

        Pbuffer pb = new Pbuffer(800, 600, new PixelFormat(32, 0, 24, 8, 0), null);
        pb.makeCurrent();
        antiAliasingMaxSamples = glGetInteger(GL_MAX_SAMPLES);
        antiAliasingSupported = antiAliasingMaxSamples > 0;
        pb.destroy();
        LogHelper.config("AA Supported: %s / Max Samples: %d", antiAliasingSupported ? "yes" : "no", antiAliasingMaxSamples);
    }

    public void start() throws LWJGLException, IOException {
        if (running) throw new IllegalStateException("Client already running!");
        running = true;
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
        initDisplayModes();
        initConfig();

        File musicDir = new File("resampled");
        if (!musicDir.exists()) musicDir.mkdirs();

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
        modelManager = new ModelManager(textureManager);
        fontManager = new FontManager();
        soundManager = new SoundManager();
        camera = new Camera();
        frustum = new Frustum();
        ledManager = new ArduinoLEDManager(4, false);
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Audio Files (*.wav, *.mp3, *.ogg, *.flac)", "wav", "mp3", "ogg", "flac"));
        fileChooser.setMultiSelectionEnabled(false);
        if (OperatingSystem.isWindows() && new File(System.getProperty("user.home"), "Music").exists()) fileChooser.setCurrentDirectory(new File(System.getProperty("user.home"), "Music"));
        init();

        camera.setPosition(new Vector3(-80, 85, 28));
        camera.setAngle(new Angle(-31, -90, 0));

        timeCounter = getTime();
        deltaTime = System.nanoTime();

        spectrumAnalyzer = new SpectrumAnalyzer();
        loadAnimations();

        commThread = new CommThread();
        commThread.start();
        /*for (int i = 0; i < 64; i++) {
            double j = i;
            LogHelper.info(Math.round(MathHelper.cie1931(j/63)*63));
        }*/

        screenList.add(screenMainControl = new ScreenMainControl());

        run();
    }

    public static LEDManager getLEDManager() {
        return ledManager;
    }

    private void addAnimation(Animation animation) {
        animations.put(animation.getName(), animation);
        animationNames.add(animation.getName());
    }

    public Map<String, Animation> getAnimations() {
        return Collections.unmodifiableMap(animations);
    }

    public List<String> getAnimationNames() {
        return Collections.unmodifiableList(animationNames);
    }

    public Animation getAnimationByClassName(String name) {
        for (Animation animation : animations.values()) {
            if (name.equals(animation.getClass().getSimpleName())) {
                return animation;
            }
        }
        return null;
    }

    public static void setPaintColor(Color color) {
        paintColor.set(color.getRed(), color.getGreen(), color.getBlue());
        instance.screenMainControl.redColorSlider.setValue(color.getRed() / 255F);
        instance.screenMainControl.greenColorSlider.setValue(color.getGreen() / 255F);
        instance.screenMainControl.blueColorSlider.setValue(color.getBlue() / 255F);
    }

    public void loadAnimations() {
        animations.clear();
        animationNames.clear();
        addAnimation(new AnimationNone());
        addAnimation(new AnimationSpectrumBars());
        addAnimation(new AnimationSpectrumShooters());
        addAnimation(new AnimationIndividualTest());
        addAnimation(new AnimationStaticFill());
        addAnimation(new AnimationPulsate());
        addAnimation(new AnimationPulsateHue());
        addAnimation(new AnimationRandomize());
        addAnimation(new AnimationRain());
        addAnimation(new AnimationMatrix());
        //addAnimation(new AnimationFolder());
        addAnimation(new AnimationTwinkle());
        addAnimation(new AnimationBlink());
        addAnimation(new AnimationStrobe());
        addAnimation(new AnimationSnake());
        addAnimation(new AnimationSnakeBattle());
        addAnimation(new AnimationSnakeInfinite());
        addAnimation(new AnimationScrollers());
        addAnimation(new AnimationProgressiveFill());
        addAnimation(new AnimationSine());
        addAnimation(new AnimationSineDouble());
        addAnimation(new AnimationStacker());
        addAnimation(new AnimationRainbowStacker());
        addAnimation(new AnimationCandyCaneStacker());
        addAnimation(new AnimationDrain());
        addAnimation(new AnimationFaucet());
        addAnimation(new AnimationMultiFaucet());
        addAnimation(new AnimationFaucetFill());
        addAnimation(new AnimationFaucetFillRainbow());
        if (screenMainControl != null) {
            screenMainControl.populateAnimationList();
        }
    }

    public static boolean isLEDWithinIsolation(int x, int y, int z) {
        switch (layerIsolation) {
            case 1: return x == selectedLayer;
            case 2: return y == selectedLayer;
            case 3: return z == selectedLayer;
        }
        return true;
    }

    public static boolean isLEDWithinIsolation(Vector3 vector) {
        return isLEDWithinIsolation((int)vector.getX(), (int)vector.getY(), (int)vector.getZ());
    }

    private void makeFrame() throws LWJGLException {
        if (frame != null) frame.dispose();
        frame = new JFrame(Constants.APP_TITLE);
        frame.setLayout(new BorderLayout());
        frame.setResizable(false);
        frame.setAlwaysOnTop(false);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        List<Image> images = new ArrayList<>();
        images.add(Toolkit.getDefaultToolkit().getImage("resources/textures/icon16.png"));
        images.add(Toolkit.getDefaultToolkit().getImage("resources/textures/icon32.png"));
        images.add(Toolkit.getDefaultToolkit().getImage("resources/textures/icon64.png"));
        images.add(Toolkit.getDefaultToolkit().getImage("resources/textures/icon128.png"));
        frame.setIconImages(images);
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

    public void shutdown() {
        closeRequested = true;
    }

    private void shutdownInternal() throws LWJGLException {
        running = false;
        //if (config != null && config.hasChanged()) config.save();
        if (soundManager != null) soundManager.getSoundSystem().cleanup();
        if (textureManager != null) textureManager.cleanup();
        if (fontManager != null) fontManager.cleanup();
        if (modelManager != null) modelManager.cleanup();
        ShaderProgram.cleanup();
        Keyboard.destroy();
        Mouse.destroy();
        Display.destroy();
        spectrumAnalyzer.close();
        File musicDir = new File("resampled");
        for (File file : musicDir.listFiles()) {
            file.delete();
        }
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
        config.defaultProperty("display.fieldofview", 45F);
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
        fieldOfView = config.getFloat("display.fieldofview");
        //fullscreen = config.getBoolean("display.fullscreen");

        if (!antiAliasingSupported) {
            antiAliasing = false;
            config.setProperty("display.antialiasing", false);
        } else if (antiAliasingSamples < 2 || antiAliasingSamples > antiAliasingMaxSamples || !Util.isPowerOfTwo(antiAliasingSamples)) {
            antiAliasingSamples = 4;
            config.setProperty("display.antialiasingsamples", 4);
        }
        if (fieldOfView < 10 || fieldOfView > 170) {
            fieldOfView = 45;
            config.setProperty("display.fieldofview", 45F);
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

    @SneakyThrows(LWJGLException.class)
    private void initGL() {
        // 3D Initialization
        glClearColor(0.08F, 0.08F, 0.08F, 1);
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

        // Setup samplers
        sampler0 = glGenSamplers();
        glSamplerParameteri(sampler0, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glSamplerParameteri(sampler0, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glSamplerParameteri(sampler0, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glSamplerParameteri(sampler0, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        // Shader init, catch errors and exit
        try {
            initShaders();
        } catch (Exception ex) {
            ex.printStackTrace();
            shutdownInternal();
            System.exit(0);
        }
    }
    
    private void initShaders() {
        progInstanceDraw = new ShaderProgram().loadShader("instancedraw").link();
    }
    
    private void postInit() {
        glBindBuffer(GL_ARRAY_BUFFER, modelManager.getModel("golfball.model").getVBO());
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 22, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_HALF_FLOAT, false, 22, 12);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 2, GL_HALF_FLOAT, false, 22, 18);
        glEnableVertexAttribArray(2);
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
                //if (Keyboard.getEventKey() == Keyboard.KEY_F11) setFullscreen(!fullscreen);
                if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
                    Mouse.setGrabbed(!Mouse.isGrabbed());
                    if (Mouse.isGrabbed()) Mouse.setCursorPosition(displayMode.getWidth() / 2, displayMode.getHeight() / 2);
                } else if (Keyboard.getEventKey() == Keyboard.KEY_R && commThread.getCurrentSequence() == null) {
                    loadAnimations();
                } else if (Keyboard.getEventKey() == Keyboard.KEY_F) {
                    camera.setPosition(new Vector3(-80, 85, 28));
                    camera.setAngle(new Angle(-31, -90, 0));
                } else if (Keyboard.getEventKey() == Keyboard.KEY_H) {
                    trueColor = !trueColor;
                    float increment = trueColor ? 1F / ledManager.getResolution() : 1F / 255F;
                    screenMainControl.redColorSlider.setIncrement(increment);
                    screenMainControl.greenColorSlider.setIncrement(increment);
                    screenMainControl.blueColorSlider.setIncrement(increment);
                }
            }
            if (!camera.processKeyboardEvent()) continue;
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
            highlight = new boolean[512];
            if (Mouse.getEventButtonState() && Mouse.getEventButton() == 0) drawClick = true;
            else if (!Mouse.getEventButtonState() && Mouse.getEventButton() == 0) drawClick = false;
            if (!Mouse.isGrabbed()) {
                Vector3 led = traceCursorToLED();
                if (led != null) {
                    for (int x = (int)led.getX(); x <= Math.min((int)led.getX() + (int)paintSize.getX(), 7); x++) {
                        for (int y= (int)led.getY(); y <= Math.min((int)led.getY() + (int)paintSize.getY(), 7); y++) {
                            for (int z = (int)led.getZ(); z <= Math.min((int)led.getZ() + (int)paintSize.getZ(), 7); z++) {
                                if (isLEDWithinIsolation(x, y, z)) {
                                    highlight[x | (z << 3) | (y << 6)] = true;
                                    if (drawClick) {
                                        ledManager.setLEDColor(x, y, z, paintColor);
                                    }
                                }
                            }
                        }
                    }
                    if (!drawClick && Mouse.getEventButtonState() && Mouse.getEventButton() == 1) {
                        Color targetColor = ledManager.getLEDColor((int)led.getX(), (int)led.getY(), (int)led.getZ());
                        if (!targetColor.equals(paintColor)) {
                            boolean[] processed = new boolean[512];
                            LinkedList<Vector3> stack = new LinkedList<>();
                            stack.push(led);
                            while (!stack.isEmpty()) {
                                Vector3 current = stack.pop();
                                Color color = ledManager.getLEDColor((int)current.getX(), (int)current.getY(), (int)current.getZ());
                                if (color.equals(targetColor) && isLEDWithinIsolation(current)) {
                                    ledManager.setLEDColor((int)current.getX(), (int)current.getY(), (int)current.getZ(), paintColor);
                                    processed[Util.encodeCubeVector(current)] = true;
                                    //Vector3 offset = null;
                                    for (int i = 0; i < 6; i++) {
                                        Vector3 offset = Direction.values()[i].getVector();
                                        Vector3 node = current.add(offset);
                                        if (node.getX() >= 0 && node.getX() <= 7 && node.getY() >= 0 && node.getY() <= 7 && node.getZ() >= 0 && node.getZ() <= 7) {
                                            if (!processed[Util.encodeCubeVector(node)]) {
                                                stack.push(node);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
        renderStart = System.nanoTime();
        if (antiAliasing) glBindFramebuffer(GL_DRAW_FRAMEBUFFER, multisampleFBO);
        
        // Setup and render 3D
        /*glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(45, (float)displayMode.getWidth() / (float)displayMode.getHeight(), 0.1F, 1000);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();*/
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        // Setup projection matrix
        projectionMatrix = Matrices.perspective(fieldOfView, (float)displayMode.getWidth() / (float)displayMode.getHeight(), 0.1F, 1000);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glEnable(GL_LIGHTING);
        glEnable(GL_DEPTH_TEST);
        glBindTexture(GL_TEXTURE_2D, 0);
        if (wireframe) glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        checkGLError("Pre render 3D");
        render3D();
        checkGLError("Post render 3D");

        // Setup and render 2D
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, displayMode.getWidth(), displayMode.getHeight(), 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        //glClear(GL_DEPTH_BUFFER_BIT);
        glDisable(GL_LIGHTING);
        glDisable(GL_DEPTH_TEST);
        glBindTexture(GL_TEXTURE_2D, 0);
        if (wireframe) glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        checkGLError("Pre render 2D");
        render2D();
        checkGLError("Post render 2D");

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
    }

    public void render3D() {
        glPushMatrix();

        viewMatrix = new Matrix4f();
        modelMatrix = new Matrix4f();
        Vector3 camPos = camera.getPosition();
        Angle camAngle = camera.getAngle();
        viewMatrix.rotate((float)Math.toRadians(camAngle.getRoll()), new Vector3f(0, 0, -1));
        viewMatrix.rotate((float)Math.toRadians(camAngle.getPitch()), new Vector3f(-1, 0, 0));
        viewMatrix.rotate((float)Math.toRadians(camAngle.getYaw()), new Vector3f(0, -1, 0));
        viewMatrix.translate(Util.convertVector(camPos.negate()));
        frustum.update(Util.matrixToArray(projectionMatrix), Util.matrixToArray(viewMatrix));
        
        progInstanceDraw.use();
        sendMatrixToProgram();
        glActiveTexture(GL_TEXTURE0);
        glBindSampler(0, sampler0);
        
        LightSource light = new LightSource();
        //light.position = new Vector4f(0, 1, 0, 0);
        light.position = new Vector4f(camera.getPosition().getX(), camera.getPosition().getY(), camera.getPosition().getZ(), 1);
        light.sendToShader(1, 0);
        glUniform1i(0, 1);
        

        /*floatBuffer.rewind();
        floatBuffer.put(new float[]{1, 1, 1, 0});
        floatBuffer.rewind();
        glLight(GL_LIGHT0, GL_POSITION, floatBuffer);*/
        
        faceCount = 0;
        String modelName = "led.model";
        float mult = 8;
        Random rand = new Random();

        int width = 8;
        int length = 8;
        int height = 8;
        int size = width * length * height * 16 * 4;
        Model model = modelManager.getModel(modelName);
        Color[] colors = new Color[512];
        synchronized (ledManager) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    for (int x = 0; x < width; x++) {
                        if (trueColor) {
                            Color color = ledManager.getLEDColorReal(x, y, z);
                            colors[x | (z << 3) | (y << 6)] = new Color(Math.round(color.getRed() * ledManager.getFactor()), Math.round(color.getGreen() * ledManager.getFactor()), Math.round(color.getBlue() * ledManager.getFactor()));
                        } else {
                            colors[x | (z << 3) | (y << 6)] = ledManager.getLEDColor(x, y, z);
                        }
                    }

                }
            }
        }
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    float xx = z * mult;
                    float yy = y * mult;
                    float zz = x * mult;
                    Vector3 pos = new Vector3(xx, yy, zz);
                    if (model.isInFrustum(pos) && isLEDWithinIsolation(x, y, z)) {
                        faceCount += model.getFaceCount();
                        model.render(pos, new Quaternion(), colors[x | (z << 3) | (y << 6)], false);
                    }
                }
            }
        }

        model = modelManager.getModel("led_larger.model");
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    if (highlight[x | (z << 3) | (y << 6)]) {
                        float xx = z * mult;
                        float yy = y * mult;
                        float zz = x * mult;
                        Vector3 pos = new Vector3(xx, yy, zz);
                        if (model.isInFrustum(pos) && isLEDWithinIsolation(x, y, z)) {
                            faceCount += model.getFaceCount();
                            model.render(pos, new Quaternion(), new Color(paintColor.getRed(), paintColor.getGreen(), paintColor.getBlue(), 32), false);
                        }
                    }
                }
            }
        }
        ShaderProgram.useNone();
        
        glPopMatrix();
    }

    public void render2D() {
        glPushMatrix();

        for (Screen screen : screenList)
            if (screen.isVisible()) screen.render();

        long renderTime = System.nanoTime() - renderStart;
        if (/*renderFPS || renderDebug ||*/ true) {
            UnicodeFont debugFont = fontManager.getFont("chemrea", 20, false, false).getUnicodeFont();
            org.newdawn.slick.Color debugColor = org.newdawn.slick.Color.yellow;
            int y = 0;
            if (renderFPS || renderDebug) debugFont.drawString(5, 5 + y++ * 25, "FPS: " + fpsRender, debugColor);
            debugFont.drawString(5, 5 + y++ * 25, "Serial port: " + (commThread.isPortOpen() ? "open" : "closed"), debugColor);
            debugFont.drawString(5, 5 + y++ * 25, "TCP clients: " + commThread.getNumTCPClients(), debugColor);
            debugFont.drawString(5, 5 + y++ * 25, "Current music: " + spectrumAnalyzer.getCurrentTrack(), debugColor);
            debugFont.drawString(5, 5 + y++ * 25, "Music time: " + spectrumAnalyzer.getPositionMillis(), debugColor);
            debugFont.drawString(5, 5 + y++ * 25, "Color mode: " + (trueColor ? "true" : "full"), debugColor);
            if (convertingAudio) debugFont.drawString(5, 5 + y++ * 25, "Converting audio...", debugColor);
            if (renderDebug) {
                Runtime runtime = Runtime.getRuntime();
                debugFont.drawString(5, 5 + y++ * 25, "Memory: " + Util.bytesToMBString(runtime.totalMemory() - runtime.freeMemory()) + " / " + Util.bytesToMBString(runtime.maxMemory()), debugColor);
                //debugFont.drawString(5, 5 + y++ * 25, "Update time: " + (updateTime / 1000000D), debugColor);
                //debugFont.drawString(5, 5 + y++ * 25, "Render time: " + (renderTime / 1000000D), debugColor);
                Vector3 vector = camera.getAngle().forward();
                debugFont.drawString(5, 5 + y++ * 25, "Camera vector: " + vector.getX() + ", " + vector.getY() + ", " + vector.getZ(), debugColor);
                vector = camera.getPosition();
                debugFont.drawString(5, 5 + y++ * 25, "Camera position: " + vector.getX() + ", " + vector.getY() + ", " + vector.getZ(), debugColor);
                //debugFont.drawString(5, 5 + y++ * 25, "Cursor position: " + Util.getMouseX() + ", " + Util.getMouseY(), debugColor);
                //debugFont.drawString(5, 5 + y++ * 25, "Cursor offset: " + (Util.getMouseX() - getWidth() / 2) + ", " + (Util.getMouseY() - getHeight() / 2 + 1), debugColor);
                debugFont.drawString(5, 5 + y++ * 25, "Rendered faces: " + faceCount, debugColor);
                //debugFont.drawString(5, 5 + y++ * 25, "Entities: " + (world != null ? world.getEntityCount() : 0), debugColor);
            }
        }

        glPopMatrix();
    }

    private void checkGLError(String stage) {
        int error;
        while ((error = glGetError()) != GL_NO_ERROR) {
            LogHelper.severe("########## GL ERROR ##########");
            LogHelper.severe("@ %s", stage);
            LogHelper.severe("%d: %s", error, gluErrorString(error));
        }
    }

    private void sendMatrixToProgram() {
        ShaderProgram program = ShaderProgram.getCurrent();
        if (program == null) return;
        int projectionMatrixLoc = program.getUniformLocation("projection_matrix");
        int viewMatrixLoc = program.getUniformLocation("view_matrix");
        matrixBuffer.rewind();
        Util.storeMatrixInBuffer(projectionMatrix, matrixBuffer);
        matrixBuffer.rewind();
        glUniformMatrix4(projectionMatrixLoc, false, matrixBuffer);
        matrixBuffer.rewind();
        viewMatrix.store(matrixBuffer);
        matrixBuffer.rewind();
        glUniformMatrix4(viewMatrixLoc, false, matrixBuffer);
    }

    public Vector3[] getCursorRay() {
        //Vector2 cursorPos = Util.getMousePos();
        float nearClip = 0.1F;
        Vector3 look = camera.getAngle().forward();
        Vector3 lookH = camera.getAngle().right();
        Vector3 lookV = camera.getAngle().up().negate();
        float fovRad = (float)Math.toRadians(fieldOfView);
        float vLength = (float)Math.tan(fovRad / 2) * nearClip;
        float hLength = vLength * ((float)displayMode.getWidth() / (float)displayMode.getHeight());
        lookH = lookH.multiply(hLength);
        lookV = lookV.multiply(vLength);
        float mouseX = (Util.getMouseX() - displayMode.getWidth() / 2F) / (displayMode.getWidth() / 2F);
        float mouseY = (Util.getMouseY() - displayMode.getHeight() / 2F) / (displayMode.getHeight() / 2F);
        Vector3 position = camera.getPosition().add(look.multiply(nearClip)).add(lookH.multiply(mouseX)).add(lookV.multiply(mouseY));
        Vector3 direction = position.subtract(camera.getPosition()).normalized();
        return new Vector3[]{position, direction};
    }

    public Vector3 traceCursorToLED() {
        Vector3[] ray = getCursorRay();
        Vector3 position = ray[0];
        Vector3 direction = ray[1].multiply(0.5F);

        float mult = 8;
        int width = 8;
        int length = 8;
        int height = 8;
        Model model = modelManager.getModel("led.model");
        for (float step = 1; step < 1000; step += 2) {
            Vector3 rayPos = position.add(direction.multiply(step));
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    for (int x = 0; x < width; x++) {
                        float xx = z * mult;
                        float yy = y * mult;
                        float zz = x * mult;
                        Vector3 pos = new Vector3(xx, yy, zz);
                        if (model.getAABB().containsPoint(pos, rayPos) && isLEDWithinIsolation(x, y, z)) {
                            return new Vector3(x, y, z);
                        }
                    }
                }
            }
        }
        return null;
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
