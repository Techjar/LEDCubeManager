package com.techjar.ledcm;
import com.techjar.ledcm.render.LightingHandler;
import com.techjar.ledcm.render.Camera;
import com.techjar.ledcm.render.Frustum;
import com.techjar.ledcm.render.InstancedRenderer;
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
import static org.lwjgl.opengl.GL40.*;
import static org.lwjgl.util.glu.GLU.*;

import com.hackoeur.jglm.Mat3;
import com.hackoeur.jglm.Mat4;
import com.hackoeur.jglm.Matrices;
import com.hackoeur.jglm.Vec3;
import com.obj.WavefrontObject;
import com.techjar.ledcm.gui.GUICallback;
import com.techjar.ledcm.gui.screen.Screen;
import com.techjar.ledcm.gui.screen.ScreenMainControl;
import com.techjar.ledcm.hardware.manager.LEDManager;
import com.techjar.ledcm.hardware.manager.ArduinoLEDManager;
import com.techjar.ledcm.hardware.CommThread;
import com.techjar.ledcm.hardware.LEDUtil;
import com.techjar.ledcm.hardware.SpectrumAnalyzer;
import com.techjar.ledcm.hardware.manager.TLC5940LEDManager;
import com.techjar.ledcm.hardware.manager.TestLEDManager;
import com.techjar.ledcm.hardware.animation.*;
import com.techjar.ledcm.hardware.tcp.TCPServer;
import com.techjar.ledcm.hardware.tcp.packet.Packet;
import com.techjar.ledcm.render.pipeline.RenderPipeline;
import com.techjar.ledcm.render.pipeline.RenderPipelineStandard;
import com.techjar.ledcm.util.Angle;
import com.techjar.ledcm.util.ArgumentParser;
import com.techjar.ledcm.util.Axis;
import com.techjar.ledcm.util.AxisAlignedBB;
import com.techjar.ledcm.util.ConfigManager;
import com.techjar.ledcm.util.Constants;
import com.techjar.ledcm.util.Dimension3D;
import com.techjar.ledcm.util.Direction;
import com.techjar.ledcm.util.LEDCubeOctreeNode;
import com.techjar.ledcm.util.LightSource;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Model;
import com.techjar.ledcm.util.ModelMesh;
import com.techjar.ledcm.util.OperatingSystem;
import com.techjar.ledcm.util.Quaternion;
import com.techjar.ledcm.util.ShaderProgram;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Tuple;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector2;
import com.techjar.ledcm.util.Vector3;
import com.techjar.ledcm.util.input.InputBinding;
import com.techjar.ledcm.util.input.InputBindingManager;
import com.techjar.ledcm.util.input.InputInfo;
import com.techjar.ledcm.util.logging.LogHelper;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileOutputStream;
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
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
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
import org.lwjgl.input.Cursor;
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
    @Getter private static File dataDirectory = OperatingSystem.getDataDirectory("ledcubemanager");
    @Getter private static DisplayMode displayMode /*= new DisplayMode(1024, 768)*/;
    private DisplayMode newDisplayMode;
    private DisplayMode configDisplayMode;
    private ByteBuffer[] icons;
    private boolean fullscreen;
    private boolean newFullscreen;
    @Getter private static ConfigManager config;
    @Getter private static JFrame frame;
    @Getter private List<DisplayMode> displayModeList;
    private Canvas canvas;
    private boolean closeRequested = false;
    private boolean running = false;
    @Getter private static TextureManager textureManager;
    @Getter private static ModelManager modelManager;
    @Getter private static FontManager fontManager;
    @Getter private static SoundManager soundManager;
    @Getter private static Camera camera;
    @Getter private static Frustum frustum;
    @Getter private static JFileChooser fileChooser;
    @Getter private static String serialPortName = "COM3";
    @Getter private static String portHandlerName = "SerialPortHandler";
    @Getter private static String ledManagerName = null;
    @Getter private static String[] ledManagerArgs = new String[0];
    @Getter private static int serverPort = 7545;
    @Getter private static FrameServer frameServer;
    @Getter private static SystemTray systemTray;
    @Getter @Setter private static boolean convertingAudio;
    private static Cursor currentCursor;
    private static Cursor lastCursor;
    private static LEDCube ledCube;
    private List<Screen> screenList = new ArrayList<>();
    private List<ScreenHolder> screensToAdd = new ArrayList<>();
    private List<GUICallback> resizeHandlers = new ArrayList<>();
    private Map<String, Integer> validControllers = new HashMap<>();
    private List<Tuple<RenderPipeline, Integer>> pipelines = new ArrayList<>();
    private Queue<Packet> packetProcessQueue = new ConcurrentLinkedQueue<>();
    private static List<Tuple<String, Integer>> debugText = new ArrayList<>();
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
    public boolean debugMode;
    public boolean debugGL;
    public boolean wireframe;
    public final boolean antiAliasingSupported;
    public final int antiAliasingMaxSamples;
    @Getter private boolean antiAliasing = true;
    @Getter private int antiAliasingSamples = 4;
    @Getter @Setter private float fieldOfView;
    @Getter @Setter private float viewDistance;
    private int multisampleFBO;
    private int multisampleTexture;
    private int multisampleDepthTexture;
    private int shadowMapSize = 1024;
    private int depthFBO;
    private int depthTexture;
    private final Timer frameServeTimer = new Timer();
    private final Timer rateCapTimer = new Timer();

    // Screens
    @Getter private ScreenMainControl screenMainControl;

    // Really import OpenGL matrix stuff
    private Mat4 projectionMatrix;
    private Matrix4f viewMatrix = new Matrix4f();
    public Matrix4f modelMatrix = new Matrix4f();

    @Getter private LightingHandler lightingHandler;
    private ShaderProgram spDepthDraw; // TODO

    public LEDCubeManager(String[] args) throws LWJGLException {
        instance = this;
        System.setProperty("sun.java2d.noddraw", "true");
        ArgumentParser.parse(args, new ArgumentParser.Argument(true, "Specify logging detail level", "--loglevel") {
            @Override
            public void runAction(String parameter) {
                LogHelper.setLevel(Level.parse(parameter));
            }
        }, new ArgumentParser.Argument(false, "Display frames per second", "--showfps") {
            @Override
            public void runAction(String parameter) {
                renderFPS = true;
            }
        }, new ArgumentParser.Argument(false, "Displaydebug output", "--debug") {
            @Override
            public void runAction(String parameter) {
                debugMode = true;
            }
        }, new ArgumentParser.Argument(false, "Display OpenGL errors", "--debug-gl") {
            @Override
            public void runAction(String parameter) {
                debugGL = true;
            }
        }, new ArgumentParser.Argument(false, "Enable wireframe rendering", "--wireframe") {
            @Override
            public void runAction(String parameter) {
                wireframe = true;
            }
        }, new ArgumentParser.Argument(true, "Specify serial port name\nArgs: <name>", "--serialport") {
            @Override
            public void runAction(String parameter) {
                serialPortName = parameter;
            }
        }, new ArgumentParser.Argument(true, "Specify internal TCP server port\nArgs: <port number>", "--serverport") {
            @Override
            public void runAction(String parameter) {
                serverPort = Integer.parseInt(parameter);
            }
        }, new ArgumentParser.Argument(true, "Specify PortHandler class\nArgs: <class name>", "--porthandler") {
            @Override
            public void runAction(String parameter) {
                portHandlerName = parameter;
            }
        }, new ArgumentParser.Argument(true, "Specify LEDManager class\nArgs: <class name and constructor parameters (comma-separated)>", "--ledmanager") {
            @Override
            public void runAction(String parameter) {
                String[] split = parameter.split("(?<!,),");
                for (int i = 0; i < split.length; i++) split[i] = split[i].replaceAll(",,", ",");
                ledManagerName = split[0];
                ledManagerArgs = new String[split.length - 1];
                System.arraycopy(split, 1, ledManagerArgs, 0, split.length - 1);
            }
        }, new ArgumentParser.Argument(true, "Specify a custom directory for config/logs/etc.\nArgs: <directory path>", "--datadir") {
            @Override
            public void runAction(String parameter) {
                dataDirectory = new File(parameter);
                if (!dataDirectory.exists()) {
                    if (!dataDirectory.mkdirs()) {
                        System.out.println("Failed to create directory: " + dataDirectory);
                        System.exit(0);
                    }
                }
            }
        });

        LogHelper.init(new File(dataDirectory, "logs"));
        LongSleeperThread.startSleeper();

        Pbuffer pb = new Pbuffer(800, 600, new PixelFormat(32, 0, 24, 8, 0), null);
        pb.makeCurrent();
        antiAliasingMaxSamples = glGetInteger(GL_MAX_SAMPLES);
        antiAliasingSupported = antiAliasingMaxSamples > 0;
        pb.destroy();
        LogHelper.config("AA Supported: %s / Max Samples: %d", antiAliasingSupported ? "yes" : "no", antiAliasingMaxSamples);
    }

    public void start() throws LWJGLException, IOException, AWTException {
        if (running) throw new IllegalStateException("Client already running!");
        running = true;
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
        initDisplayModes();
        initConfig();
        initBindings();

        File musicDir = new File("resampled");
        if (!musicDir.exists()) musicDir.mkdirs();

        Display.setDisplayMode(displayMode);
        makeFrame();
        setupSystemTray();
        
        loadIcons();
        Display.setIcon(icons);
        Display.create();
        Keyboard.create();
        Mouse.create();
        CursorType.loadCursors();
        setCursorType(CursorType.DEFAULT);
        Display.setTitle(Constants.APP_TITLE);

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

        addRenderPipeline(new RenderPipelineStandard(), 0);

        textureManager = new TextureManager();
        modelManager = new ModelManager(textureManager);
        fontManager = new FontManager();
        soundManager = new SoundManager();
        lightingHandler = new LightingHandler();
        camera = new Camera();
        frustum = new Frustum();
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Audio Files (*.wav, *.mp3, *.ogg, *.flac)", "wav", "mp3", "ogg", "flac"));
        fileChooser.setMultiSelectionEnabled(false);
        if (OperatingSystem.isWindows() && new File(System.getProperty("user.home"), "Music").exists()) fileChooser.setCurrentDirectory(new File(System.getProperty("user.home"), "Music"));
        init();
        InstancedRenderer.init();

        LightSource light = new LightSource();
        light.position = new Vector4f(0, 0, 0, 1);
        lightingHandler.addLight(light);

        ledCube = new LEDCube();
        ledCube.postInit();
        frameServer = new FrameServer();

        timeCounter = getTime();
        deltaTime = System.nanoTime();

        screenList.add(screenMainControl = new ScreenMainControl());
        InputBindingManager.setupSettings();
        ledCube.loadAnimations();

        run();
    }

    public static LEDCube getLEDCube() {
        return ledCube;
    }

    /**
     *
     * @return
     * @deprecated Get the {@link LEDManager} from the {@link LEDCube} instance.
     */
    @Deprecated
    public static LEDManager getLEDManager() {
        return ledCube.getLEDManager();
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
                if (systemTray == null) {
                    closeRequested = true;
                } else {
                    frame.setVisible(false);
                }
            }
        });

        frame.add(canvas, BorderLayout.CENTER);
        resizeFrame(fullscreen);
    }

    private void resizeFrame(boolean fullscreen) throws LWJGLException {
        Display.setParent(null);
        frame.dispose();
        if (fullscreen) {
            Display.setDisplayMode(displayMode);
            Display.setFullscreen(true);
        } else {
            Display.setFullscreen(false);
            if (fullscreen) GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(frame);
            else GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(null);
            canvas.setPreferredSize(new java.awt.Dimension(displayMode.getWidth(), displayMode.getHeight()));
            frame.pack();
            java.awt.Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setLocation((dim.width - frame.getSize().width) / 2, (dim.height - frame.getSize().height) / 2);
            frame.setVisible(true);
            Display.setParent(canvas);
            Display.setDisplayMode(displayMode);
        }
    }

    private void loadIcons() throws IOException {
        icons = new ByteBuffer[4];
        for (int i = 0; i < 4; i++) {
            BufferedImage image = ImageIO.read(new File("resources/textures/icon" + (int)Math.pow(2, 4 + i) + ".png"));
            icons[i] = ByteBuffer.allocate(image.getWidth() * image.getHeight() * 4);
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int color = image.getRGB(x, y);
                    icons[i].put((byte)((color >> 16) & 255));
                    icons[i].put((byte)((color >> 8) & 255));
                    icons[i].put((byte)(color & 255));
                    icons[i].put((byte)((color >>> 24) & 255));
                }
            }
            icons[i].rewind();
        }
    }

    public void shutdown() {
        closeRequested = true;
    }

    private void shutdownInternal() throws LWJGLException {
        running = false;
        if (config != null && config.hasChanged()) config.save();
        if (soundManager != null) soundManager.getSoundSystem().cleanup();
        if (textureManager != null) textureManager.cleanup();
        if (fontManager != null) fontManager.cleanup();
        if (modelManager != null) modelManager.cleanup();
        ShaderProgram.cleanup();
        Keyboard.destroy();
        Mouse.destroy();
        Display.destroy();
        ledCube.getSpectrumAnalyzer().close();
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
                if (rateCapTimer.getMilliseconds() >= 1000D / 300D) {
                    rateCapTimer.restart();
                    runGameLoop();
                } else if (1000D / 300D - rateCapTimer.getMilliseconds() > 1) {
                    Thread.sleep(1);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                closeRequested = true;
            }
        }
        shutdownInternal();
    }

    private void runGameLoop() throws LWJGLException, InterruptedException {
        //if (fullscreen && !frame.isFocused()) setFullscreen(false);
        if (newDisplayMode != null || newFullscreen != fullscreen) {
            fullscreen = newFullscreen;
            if (newDisplayMode != null) {
                displayMode = newDisplayMode;
                configDisplayMode = newDisplayMode;
                config.setProperty("display.width", configDisplayMode.getWidth());
                config.setProperty("display.height", configDisplayMode.getHeight());
                config.setProperty("display.fullscreen", fullscreen);
                config.setProperty("display.antialiasing", antiAliasing);
                config.setProperty("display.antialiasingsamples", antiAliasingSamples);
                config.save();
            }
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
        this.preProcess();
        this.processKeyboard();
        this.processMouse();
        this.processController();
        this.update();
        if (Display.isActive() || (frame.isVisible() && frame.getState() != Frame.ICONIFIED) || frameServer.numClients > 0) this.render();
        else Thread.sleep(20);
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
            glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, antiAliasingSamples, GL_DEPTH_STENCIL, displayMode.getWidth(), displayMode.getHeight(), false);
            multisampleFBO = glGenFramebuffers();
            glBindFramebuffer(GL_FRAMEBUFFER, multisampleFBO);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D_MULTISAMPLE, multisampleTexture, 0);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_TEXTURE_2D_MULTISAMPLE, multisampleDepthTexture, 0);
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

    private void setupSystemTray() throws AWTException {
        if (!SystemTray.isSupported()) {
            LogHelper.warning("System tray is not supported.");
            return;
        }

        systemTray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().getImage("resources/textures/icon16.png");
        PopupMenu menu = new PopupMenu();
        MenuItem item = new MenuItem("Exit");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shutdown();
            }
        });
        menu.add(item);
        TrayIcon trayIcon = new TrayIcon(image, Constants.APP_TITLE, menu);
        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
                    if (!fullscreen) frame.setVisible(true);
                    e.consume();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        systemTray.add(trayIcon);
        LogHelper.info("System tray icon initialized.");
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
        if (displayMode == null) displayMode = new DisplayMode(1024, 768);
        config = new ConfigManager(new File(dataDirectory, "options.yml"), false);
        config.load();
        config.defaultProperty("display.width", displayMode.getWidth());
        config.defaultProperty("display.height", displayMode.getHeight());
        config.defaultProperty("display.fullscreen", false);
        config.defaultProperty("display.fieldofview", 45F);
        config.defaultProperty("display.viewdistance", 1000F);
        config.defaultProperty("display.antialiasing", true);
        config.defaultProperty("display.antialiasingsamples", 4);
        config.defaultProperty("sound.effectvolume", 1.0F);
        config.defaultProperty("sound.musicvolume", 1.0F);
        config.defaultProperty("sound.inputdevice", "");
        config.defaultProperty("sound.inputgain", 0.05F);
        config.defaultProperty("misc.ffmpegpath", "ffmpeg");

        if (!internalSetDisplayMode(config.getInteger("display.width"), config.getInteger("display.height"))) {
            config.setProperty("display.width", displayMode.getWidth());
            config.setProperty("display.height", displayMode.getHeight());
        }
        antiAliasing = config.getBoolean("display.antialiasing");
        antiAliasingSamples = config.getInteger("display.antialiasingsamples");
        fieldOfView = config.getFloat("display.fieldofview");
        viewDistance = config.getFloat("display.viewdistance");
        fullscreen = config.getBoolean("display.fullscreen");
        newFullscreen = fullscreen;

        if (!antiAliasingSupported) {
            antiAliasing = false;
            config.setProperty("display.antialiasing", false);
        } else if (antiAliasingSamples < 2 || antiAliasingSamples > antiAliasingMaxSamples || !Util.isPowerOfTwo(antiAliasingSamples)) {
            antiAliasingSamples = 4;
            config.setProperty("display.antialiasingsamples", 4);
        }
        if (fieldOfView < 10 || fieldOfView > 179) {
            fieldOfView = 45;
            config.setProperty("display.fieldofview", 45F);
        }

        /*if (config.getInteger("version") < Constants.VERSION) {
            config.setProperty("version", Constants.VERSION);
        }*/
        
        InputBindingManager.loadAllConfig();
        if (config.hasChanged()) config.save();
    }

    private void initBindings() {
        InputBindingManager.addBinding(new InputBinding("screenshot", "Screenshot", "General", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_F2)) {
            @Override
            public boolean onPressed() {
                screenshot = true;
                return false;
            }

            @Override
            public boolean onReleased() {
                return true;
            }
        });
        InputBindingManager.addBinding(new InputBinding("reloadshaders", "Reload Shaders", "General", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_F5)) {
            @Override
            public boolean onPressed() {
                ShaderProgram.cleanup();
                initShaders();
                return false;
            }

            @Override
            public boolean onReleased() {
                return true;
            }
        });
        InputBindingManager.addBinding(new InputBinding("wireframe", "Wireframe", "General", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_F6)) {
            @Override
            public boolean onPressed() {
                wireframe = !wireframe;
                return false;
            }

            @Override
            public boolean onReleased() {
                return true;
            }
        });
        InputBindingManager.addBinding(new InputBinding("movecamera", "Toggle Movement", "Camera", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_ESCAPE)) {
            @Override
            public boolean onPressed() {
                Mouse.setGrabbed(!Mouse.isGrabbed());
                if (Mouse.isGrabbed()) Mouse.setCursorPosition(displayMode.getWidth() / 2, displayMode.getHeight() / 2);
                return false;
            }

            @Override
            public boolean onReleased() {
                return true;
            }
        });
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
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
        glStencilMask(0x00);
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
        for (Tuple<RenderPipeline, Integer> tuple : pipelines) {
            tuple.getA().loadShaders();
        }
    }

    public void resizeGL(int width, int height) {
        // Viewport setup
        glViewport(0, 0, width, height);
    }

    private void preProcess() {
        lastCursor = currentCursor;
        currentCursor = null;
        debugText.clear();
        ledCube.preProcess();
    }

    private void processKeyboard() {
        toploop: while (Keyboard.next()) {
            for (Screen screen : screenList)
                if (screen.isVisible() && screen.isEnabled() && !screen.processKeyboardEvent()) continue toploop;
            //if (world != null && !world.processKeyboardEvent()) continue;
            for (InputBinding binding : InputBindingManager.getBindings()) {
                if (binding.getBind() != null && binding.getBind().getType() == InputInfo.Type.KEYBOARD && binding.getBind().getButton() == Keyboard.getEventKey()) {
                    if (Keyboard.getEventKeyState()) {
                        if (!binding.onPressed()) continue toploop;
                    } else {
                        if (!binding.onReleased()) continue toploop;
                    }
                }
            }
            if (!ledCube.processKeyboardEvent()) continue;
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
            for (InputBinding binding : InputBindingManager.getBindings()) {
                if (binding.getBind() != null && binding.getBind().getType() == InputInfo.Type.MOUSE && binding.getBind().getButton() == Mouse.getEventButton()) {
                    if (Mouse.getEventButtonState()) {
                        if (!binding.onPressed()) continue toploop;
                    } else {
                        if (!binding.onReleased()) continue toploop;
                    }
                }
            }
            //if (world != null && !world.processMouseEvent()) continue;
            //if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() && !asteroids.containsKey(getMousePos())) asteroids.put(getMousePos(), AsteroidGenerator.generate());
            if (!ledCube.processMouseEvent()) continue;
        }
    }

    private void processController() { // TODO
        toploop: while (Controllers.next()) {
            Controller con = Controllers.getEventSource();
            if (con.getName().equals(config.getString("controls.controller"))) {
                for (Screen screen : screenList)
                    if (screen.isVisible() && screen.isEnabled() && !screen.processControllerEvent(con)) continue toploop;
                //if (world != null && !world.processControllerEvent(con)) continue;
                if (!ledCube.processControllerEvent(con)) continue;
            }
        }
    }

    public void update() {
        long time = System.nanoTime();
        float delta = getDelta();

        if (!packetProcessQueue.isEmpty()) {
            Packet packet;
            while ((packet = packetProcessQueue.poll()) != null) {
                packet.process();
            }
        }

        camera.update(delta);
        textureManager.update(delta);
        ledCube.update(delta);

        lightingHandler.getLight(0).position = new Vector4f(camera.getPosition().getX(), camera.getPosition().getY(), camera.getPosition().getZ(), 1);

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

        try {
            if (currentCursor != null) Mouse.setNativeCursor(currentCursor);
            else if (lastCursor != null) Mouse.setNativeCursor(CursorType.DEFAULT.getCursor());
        } catch (LWJGLException ex) {
            ex.printStackTrace();
        }

        if (debugMode) {
            Runtime runtime = Runtime.getRuntime();
            if (convertingAudio) addInfoText("Converting audio...", 1000);
            addInfoText("Memory: " + Util.bytesToMBString(runtime.totalMemory() - runtime.freeMemory()) + " / " + Util.bytesToMBString(runtime.maxMemory()), 1010);
            Vector3 vector = camera.getAngle().forward();
            addInfoText("Camera vector: " + vector.getX() + ", " + vector.getY() + ", " + vector.getZ(), 1020);
            vector = camera.getPosition();
            addInfoText("Camera position: " + vector.getX() + ", " + vector.getY() + ", " + vector.getZ(), 1030);
            //debugFont.drawString(5, 5 + y++ * 25, "Cursor position: " + Util.getMouseX() + ", " + Util.getMouseY(), debugColor);
            //debugFont.drawString(5, 5 + y++ * 25, "Cursor offset: " + (Util.getMouseX() - getWidth() / 2) + ", " + (Util.getMouseY() - getHeight() / 2 + 1), debugColor);
            //debugFont.drawString(5, 5 + y++ * 25, "Entities: " + (world != null ? world.getEntityCount() : 0), debugColor);
        }

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
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        glEnable(GL_LIGHTING);
        glEnable(GL_DEPTH_TEST);
        glBindTexture(GL_TEXTURE_2D, 0);
        //wireframe = true;
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
            glBlitFramebuffer(0, 0, displayMode.getWidth(), displayMode.getHeight(), 0, 0, displayMode.getWidth(), displayMode.getHeight(), GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT, GL_NEAREST);
        }
        boolean frameServe = frameServer.numClients > 0 && frameServeTimer.getMilliseconds() >= 1000D / 60D;
        if (screenshot || frameServe) {
            frameServeTimer.restart();
            ByteBuffer buffer = BufferUtils.createByteBuffer(displayMode.getWidth() * displayMode.getHeight() * 3);
            glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
            glReadPixels(0, 0, displayMode.getWidth(), displayMode.getHeight(), GL_RGB, GL_UNSIGNED_BYTE, buffer);
            BufferedImage image = new BufferedImage(displayMode.getWidth(), displayMode.getHeight(), BufferedImage.TYPE_INT_RGB);
            int[] pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
            buffer.rewind();
            for (int y = displayMode.getHeight() - 1; y >= 0; y--) {
                for (int x = 0; x < displayMode.getWidth(); x++) {
                    pixels[x + (y * displayMode.getWidth())] = (buffer.get() & 0xFF) << 16 | (buffer.get() & 0xFF) << 8 | (buffer.get() & 0xFF);
                }
            }

            if (screenshot) {
                screenshot = false;
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
                File screenshotDir = new File(dataDirectory, "screenshots");
                screenshotDir.mkdirs();
                File file = new File(screenshotDir, dateFormat.format(Calendar.getInstance().getTime()) + ".png");
                for (int i = 2; file.exists(); i++) {
                    file = new File(screenshotDir, dateFormat.format(Calendar.getInstance().getTime()) + "_" + i + ".png");
                }
                ImageIO.write(image, "png", file);
            }
            
            if (frameServe) {
                frameServer.queueFrame(image);
            }
        }
    }

    public void render3D() {
        glPushMatrix();
        
        faceCount = ledCube.render();
        addInfoText("Rendered faces: " + faceCount, 1040);

        for (Tuple<RenderPipeline, Integer> tuple : pipelines) {
            for (int i = 0; i < tuple.getA().get3DPasses(); i++) {
                tuple.getA().render3D(i);
            }
        }
        InstancedRenderer.resetItems();
        
        glPopMatrix();
    }

    public void render2D() {
        glPushMatrix();

        for (Tuple<RenderPipeline, Integer> tuple : pipelines) {
            for (int i = 0; i < tuple.getA().get2DPasses(); i++) {
                tuple.getA().render2D(i);
            }
        }

        for (Screen screen : screenList)
            if (screen.isVisible()) screen.render();

        UnicodeFont debugFont = fontManager.getFont("chemrea", 20, false, false).getUnicodeFont();
        org.newdawn.slick.Color infoColor = org.newdawn.slick.Color.yellow;
        int y = 0;
        if (renderFPS || debugMode) {
            debugFont.drawString(5, 5 + y++ * 25, "FPS: " + fpsRender, infoColor);
            debugFont.drawString(5, 5 + y++ * 25, "Animation FPS: " + ledCube.getCommThread().getFPS(), infoColor);
        }
        for (Tuple<String, Integer> tuple : debugText) {
            debugFont.drawString(5, (y++ * 25) + 5, tuple.getA(), infoColor);
        }

        glPopMatrix();
    }

    private void checkGLError(String stage) {
        if (debugGL) {
            for (int error = glGetError(); error != GL_NO_ERROR; error = glGetError()) {
                LogHelper.severe("########## GL ERROR ##########");
                LogHelper.severe("@ %s", stage);
                LogHelper.severe("%d: %s", error, gluErrorString(error));
            }
        }
    }

    public void setupView(Mat4 projection, Vector3 position, Quaternion rotation) {
        projectionMatrix = projection;
        viewMatrix.setIdentity();
        modelMatrix.setIdentity();
        Matrix4f.mul(viewMatrix, (Matrix4f)rotation.getMatrix().negate(), viewMatrix);
        viewMatrix.translate(Util.convertVector(position.negate()));
        frustum.update(Util.matrixToArray(projectionMatrix), Util.matrixToArray(viewMatrix));
    }

    public void setupView(Mat4 projection, Vector3 position, Angle angle) {
        projectionMatrix = projection;
        viewMatrix.setIdentity();
        modelMatrix.setIdentity();
        viewMatrix.rotate((float)Math.toRadians(angle.getRoll()), new Vector3f(0, 0, -1));
        viewMatrix.rotate((float)Math.toRadians(angle.getPitch()), new Vector3f(-1, 0, 0));
        viewMatrix.rotate((float)Math.toRadians(angle.getYaw()), new Vector3f(0, -1, 0));
        viewMatrix.translate(Util.convertVector(position.negate()));
        frustum.update(Util.matrixToArray(projectionMatrix), Util.matrixToArray(viewMatrix));
    }

    public void sendMatrixToProgram() {
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

    public static Color getPaintColor() {
        return ledCube.getPaintColor();
    }

    public static void setPaintColor(Color color) {
        ledCube.setPaintColor(color);
    }

    /**
     * Cursor will reset to default every frame, so you must set it every frame to keep it persistent.
     * This is to avoid the cursor being left in odd states when it shouldn't be.
     */
    public static void setCursorType(CursorType ct) {
        currentCursor = ct == null ? null : ct.getCursor();
    }

    public Controller getController(String name) {
        Integer index = validControllers.get(name);
        return index != null ? Controllers.getController(index) : null;
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
            /*DisplayMode desktopMode = Display.getDesktopDisplayMode();
            if (fullscreen) {
                displayMode = desktopMode;
            } else displayMode = configDisplayMode;*/
            displayMode = configDisplayMode;
            resizeFrame(fullscreen);
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

    public void setAntiAliasing(boolean enabled, int samples) {
        antiAliasing = enabled;
        antiAliasingSamples = samples;
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

    /**
     * Lower priority number is rendered earlier.
     */
    public void addRenderPipeline(RenderPipeline pipeline, int priority) {
        if (pipelines.size() < 1) {
            pipelines.add(new Tuple<>(pipeline, priority));
        } else {
            for (int i = 0; i <= pipelines.size(); i++) {
                Tuple<RenderPipeline, Integer> tuple = i == pipelines.size() ? null : pipelines.get(i);
                if (tuple == null || priority <= tuple.getB()) {
                    pipelines.add(i, new Tuple<>(pipeline, priority));
                    break;
                }
            }
        }
    }

    /**
     * Adds the text at the top left of the screen. Lower priority number is higher on the list.
     * FPS always appears at the top and cannot be overridden through this method.
     *
     * The list is cleared every frame, so you should call this on every update().
     */
    public static void addInfoText(String text, int priority) {
        if (debugText.size() < 1) {
            debugText.add(new Tuple<>(text, priority));
        } else {
            for (int i = 0; i <= debugText.size(); i++) {
                Tuple<String, Integer> tuple = i == debugText.size() ? null : debugText.get(i);
                if (tuple == null || priority <= tuple.getB()) {
                    debugText.add(i, new Tuple<>(text, priority));
                    break;
                }
            }
        }
    }

    public static int getWidth() {
        return displayMode.getWidth();
    }

    public static int getHeight() {
        return displayMode.getHeight();
    }

    public static void queuePacketForProcessing(Packet packet) {
        instance.packetProcessQueue.add(packet);
    }

    @Value private class ScreenHolder {
        private int index;
        private Screen screen;
    }
}
