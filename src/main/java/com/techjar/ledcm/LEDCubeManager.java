package com.techjar.ledcm;
import com.techjar.ledcm.render.LightingHandler;
import com.techjar.ledcm.render.Camera;
import com.techjar.ledcm.render.Frustum;
import com.techjar.ledcm.render.InstancedRenderer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.util.glu.GLU.*;

import com.techjar.ledcm.gui.GUI;
import com.techjar.ledcm.gui.screen.Screen;
import com.techjar.ledcm.gui.screen.ScreenMainControl;
import com.techjar.ledcm.hardware.manager.LEDManager;
import com.techjar.ledcm.hardware.tcp.packet.Packet;
import com.techjar.ledcm.render.camera.RenderCamera;
import com.techjar.ledcm.render.pipeline.RenderPipeline;
import com.techjar.ledcm.render.pipeline.RenderPipelineGUI;
import com.techjar.ledcm.render.pipeline.RenderPipelineStandard;
import com.techjar.ledcm.render.pipeline.RenderPipelineVR;
import com.techjar.ledcm.util.math.Angle;
import com.techjar.ledcm.util.ArgumentParser;
import com.techjar.ledcm.util.ConfigManager;
import com.techjar.ledcm.util.Constants;
import com.techjar.ledcm.util.KeyPress;
import com.techjar.ledcm.util.LightSource;
import com.techjar.ledcm.util.OperatingSystem;
import com.techjar.ledcm.util.math.Quaternion;
import com.techjar.ledcm.util.ShaderProgram;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Tuple;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.math.Vector2;
import com.techjar.ledcm.util.math.Vector3;
import com.techjar.ledcm.util.input.InputBinding;
import com.techjar.ledcm.util.input.InputBindingManager;
import com.techjar.ledcm.util.input.InputInfo;
import com.techjar.ledcm.util.logging.LogHelper;
import com.techjar.ledcm.vr.VRInputEvent;
import com.techjar.ledcm.vr.VRProvider;
import com.techjar.ledcm.vr.VRProvider.ControllerType;
import com.techjar.ledcm.vr.VRTrackedController;
import com.techjar.ledcm.vr.VRTrackedController.AxisType;
import com.techjar.ledcm.vr.VRTrackedController.ButtonType;

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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
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
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
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

/**
 *
 * @author Techjar
 */
public class LEDCubeManager {
	@Getter private static LEDCubeManager instance;
	@Getter private static File dataDirectory = OperatingSystem.getDataDirectory("ledstripmanager");
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
	private List<Runnable> resizeHandlers = new ArrayList<>();
	private Queue<KeyPress> virtualKeyPresses = new LinkedList<>();
	private Queue<VRInputEvent> vrInputEvents = new LinkedList<>();
	private Map<String, Integer> validControllers = new HashMap<>();
	private List<Tuple<RenderPipeline, Integer>> pipelines = new ArrayList<>();
	private List<RenderCamera> renderCameras = new ArrayList<>();
	private Queue<Packet> packetProcessQueue = new ConcurrentLinkedQueue<>();
	private static List<Tuple<String, Integer>> debugText = new ArrayList<>();
	private FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(4);
	private FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
	private int fpsCounter;
	@Getter private int fpsRender;
	private long timeCounter;
	private long deltaTime;
	@Getter private static float frameDelta;
	private long renderStart;
	public long faceCount;
	private boolean screenshot;
	private boolean regrab;
	public boolean renderFPS;
	public boolean debugMode;
	public boolean debugGL;
	public boolean debugGUI;
	public boolean wireframe;
	@Getter private boolean vrMode;
	@Getter @Setter private boolean showingVRGUI;
	@Getter @Setter private boolean showingGUI = true;
	@Getter @Setter private Vector2 mouseOverride;
	public final boolean antiAliasingSupported;
	public final int antiAliasingMaxSamples;
	@Getter private boolean antiAliasing = true;
	@Getter private int antiAliasingSamples = 4;
	@Getter @Setter private float fieldOfView;
	@Getter private float nearClip = 0.001F;
	@Getter @Setter private float viewDistance;
	@Getter private boolean limitFramerate;
	@Getter private int multisampleFBO;
	private int multisampleTexture;
	private int multisampleDepthTexture;
	private int multisampleRenderbuffer;
	private int multisampleDepthRenderbuffer;
	private int shadowMapSize = 1024;
	private int depthFBO;
	private int depthTexture;
	private final Timer frameServeTimer = new Timer();
	private final Timer rateCapTimer = new Timer();
	private final Timer vrScrollTimer = new Timer();

	// Screens
	@Getter private ScreenMainControl screenMainControl;

	// Really import OpenGL matrix stuff
	private Matrix4f projectionMatrix;
	private Matrix4f viewMatrix = new Matrix4f();

	@Getter private LightingHandler lightingHandler;
	private ShaderProgram spDepthDraw; // TODO

	public LEDCubeManager(String[] args) throws LWJGLException {
		instance = this;
		System.setProperty("sun.java2d.noddraw", "true");
		ArgumentParser.parse(args, new ArgumentParser.Argument(true, "\nSpecify logging detail level", "--loglevel") {
			@Override
			public void runAction(String parameter) {
				LogHelper.setLevel(Level.parse(parameter));
			}
		}, new ArgumentParser.Argument(false, "\nDisplay frames per second", "--showfps") {
			@Override
			public void runAction(String parameter) {
				renderFPS = true;
			}
		}, new ArgumentParser.Argument(false, "\nDisplay debug output", "--debug") {
			@Override
			public void runAction(String parameter) {
				debugMode = true;
			}
		}, new ArgumentParser.Argument(false, "\nDisplay OpenGL errors", "--debug-gl") {
			@Override
			public void runAction(String parameter) {
				debugGL = true;
			}
		}, new ArgumentParser.Argument(false, "\nRender GUI boxes", "--debug-gui") {
			@Override
			public void runAction(String parameter) {
				debugGUI = true;
			}
		}, new ArgumentParser.Argument(false, "\nEnable wireframe rendering", "--wireframe") {
			@Override
			public void runAction(String parameter) {
				wireframe = true;
			}
		}, new ArgumentParser.Argument(false, "\nEnable virtual reality render mode", "--vr") {
			@Override
			public void runAction(String parameter) {
				vrMode = true;
			}
		}, new ArgumentParser.Argument(true, "<name>\nSpecify serial port name", "--serialport") {
			@Override
			public void runAction(String parameter) {
				serialPortName = parameter;
			}
		}, new ArgumentParser.Argument(true, "<port number>\nSpecify internal TCP server port", "--serverport") {
			@Override
			public void runAction(String parameter) {
				serverPort = Integer.parseInt(parameter);
			}
		}, new ArgumentParser.Argument(true, "<class name>\nSpecify PortHandler class", "--porthandler") {
			@Override
			public void runAction(String parameter) {
				portHandlerName = parameter;
			}
		}, new ArgumentParser.Argument(true, "<class name and constructor parameters (comma-separated)>\nSpecify LEDManager class", "--ledmanager") {
			@Override
			public void runAction(String parameter) {
				String[] split = parameter.split("(?<!,),");
				for (int i = 0; i < split.length; i++) split[i] = split[i].replaceAll(",,", ",");
				ledManagerName = split[0];
				ledManagerArgs = new String[split.length - 1];
				System.arraycopy(split, 1, ledManagerArgs, 0, split.length - 1);
			}
		}, new ArgumentParser.Argument(true, "<directory path>\nSpecify a custom directory for config/logs/etc.", "--datadir") {
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

		if (vrMode) { // Kick all this shit off...
			VRProvider.init();
		}

		textureManager = new TextureManager();
		modelManager = new ModelManager(textureManager);
		fontManager = new FontManager();
		soundManager = new SoundManager();
		lightingHandler = new LightingHandler();
		camera = new Camera();
		frustum = new Frustum();
		fileChooser = new JFileChooser();
		fileChooser.setFileFilter(new FileNameExtensionFilter("Audio Files (*.wav, *.mp3, *.ogg, *.flac, *.m4a, *.aac)", "wav", "mp3", "ogg", "flac", "m4a", "aac"));
		fileChooser.setMultiSelectionEnabled(false);
		if (OperatingSystem.isWindows() && new File(System.getProperty("user.home"), "Music").exists()) fileChooser.setCurrentDirectory(new File(System.getProperty("user.home"), "Music"));

		if (vrMode) {
			addRenderPipeline(new RenderPipelineVR(), 10);
			addRenderPipeline(new RenderPipelineGUI(), 20);
		} else {
			addRenderPipeline(new RenderPipelineStandard(), 10);
			addRenderPipeline(new RenderPipelineGUI(), 20);
		}

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

		for (Tuple<RenderPipeline, Integer> tuple : pipelines) {
			tuple.getA().init();
		}

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

	void shutdownInternal() {
		running = false;
		if (config != null && config.hasChanged()) config.save();
		if (soundManager != null) soundManager.getSoundSystem().cleanup();
		if (textureManager != null) textureManager.cleanup();
		if (fontManager != null) fontManager.cleanup();
		if (modelManager != null) modelManager.cleanup();
		if (ledCube != null) ledCube.cleanup();
		if (VRProvider.isInitialized()) VRProvider.destroy();
		ShaderProgram.cleanup();
		Keyboard.destroy();
		Mouse.destroy();
		Display.destroy();
		File musicDir = new File("resampled");
		for (File file : musicDir.listFiles()) {
			file.delete();
		}
	}

	private long getTime() {
		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}

	private void updateFrameDelta() {
		long time = System.nanoTime();
		float delta = (time - deltaTime) / 1000000000F;
		deltaTime = time;
		frameDelta = delta;
	}

	public void run() {
		while (!Display.isCloseRequested() && !closeRequested) {
			try {
				if (!limitFramerate || vrMode || rateCapTimer.getMilliseconds() >= 1000D / 300D) {
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

	private void runGameLoop() throws InterruptedException {
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

		updateFrameDelta();
		float delta = getFrameDelta();
		soundManager.update();
		if (vrMode) VRProvider.poll(delta);
		this.preProcess();
		this.processKeyboard();
		this.processMouse();
		this.processController();
		this.processVRInput();
		this.update(delta);
		if (vrMode || Display.isActive() || (frame.isVisible() && frame.getState() != Frame.ICONIFIED) || frameServer.numClients > 0) this.render();
		else Thread.sleep(20);
		Display.update();
	}

	private void setupAntiAliasing() {
		if (multisampleFBO != 0) {
			glDeleteFramebuffers(multisampleFBO);
			multisampleFBO = 0;
		}
		if (multisampleTexture != 0) {
			glDeleteTextures(multisampleTexture);
			glDeleteTextures(multisampleDepthTexture);
			multisampleTexture = 0;
			multisampleDepthTexture = 0;
		}
		if (multisampleRenderbuffer != 0) {
			glDeleteRenderbuffers(multisampleRenderbuffer);
			glDeleteRenderbuffers(multisampleRenderbuffer);
			multisampleRenderbuffer = 0;
			multisampleRenderbuffer = 0;
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
		} else {
			multisampleRenderbuffer = glGenRenderbuffers();
			multisampleDepthRenderbuffer = glGenRenderbuffers();
			glBindRenderbuffer(GL_RENDERBUFFER, multisampleRenderbuffer);
			glRenderbufferStorage(GL_RENDERBUFFER, GL_RGBA8, displayMode.getWidth(), displayMode.getHeight());
			glBindRenderbuffer(GL_RENDERBUFFER, multisampleDepthRenderbuffer);
			glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH24_STENCIL8, displayMode.getWidth(), displayMode.getHeight());
			multisampleFBO = glGenFramebuffers();
			glBindFramebuffer(GL_FRAMEBUFFER, multisampleFBO);
			glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, multisampleRenderbuffer);
			glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_STENCIL_ATTACHMENT, GL_RENDERBUFFER, multisampleDepthRenderbuffer);
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
		item.addActionListener(event -> {
			shutdown();
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
		config.defaultProperty("display.viewdistance", 100F);
		config.defaultProperty("display.antialiasing", true);
		config.defaultProperty("display.antialiasingsamples", 4);
		config.defaultProperty("display.limitframerate", true);
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
		limitFramerate = config.getBoolean("display.limitframerate");
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
		InputBindingManager.addBinding(new InputBinding("togglegui", "Toggle GUI", "General", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_F1)) {
			@Override
			public boolean onPressed() {
				showingGUI = !showingGUI;
				return false;
			}

			@Override
			public void whilePressed() {
			}

			@Override
			public boolean onReleased() {
				return true;
			}
		});
		InputBindingManager.addBinding(new InputBinding("screenshot", "Screenshot", "General", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_F2)) {
			@Override
			public boolean onPressed() {
				screenshot = true;
				return false;
			}

			@Override
			public void whilePressed() {
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
			public void whilePressed() {
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
			public void whilePressed() {
			}

			@Override
			public boolean onReleased() {
				return true;
			}
		});
		if (!vrMode) {
			InputBindingManager.addBinding(new InputBinding("movecamera", "Toggle Movement", "Camera", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_ESCAPE)) {
				@Override
				public boolean onPressed() {
					Mouse.setGrabbed(!Mouse.isGrabbed());
					if (Mouse.isGrabbed())
						Mouse.setCursorPosition(displayMode.getWidth() / 2, displayMode.getHeight() / 2);
					return false;
				}

				@Override
				public void whilePressed() {
				}

				@Override
				public boolean onReleased() {
					return true;
				}
			});
		}
	}

	private void init() {
		initGL();
		resizeGL(displayMode.getWidth(), displayMode.getHeight());
		setupAntiAliasing();
	}

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
		computeVRMouseAim();
	}

	private void processKeyboard() {
		boolean screenEat = false;
		toploop: while (Keyboard.next()) {
			for (Screen screen : screenList)
				if (screen.isVisible() && screen.isEnabled() && !screen.processKeyboardEvent()) {
					screenEat = true;
					break;
				}
			for (InputBinding binding : InputBindingManager.getBindings()) {
				if (binding.getBind() != null && binding.getBind().getType() == InputInfo.Type.KEYBOARD && binding.getBind().getButton() == Keyboard.getEventKey()) {
					if (screenEat) {
						binding.setPressed(false);
						continue toploop;
					}
					if (Keyboard.getEventKeyState()) {
						if (!binding.onPressed()) {
							binding.setPressed(true);
							continue toploop;
						}
					} else {
						binding.setPressed(false);
						if (!binding.onReleased()) continue toploop;
					}
				}
			}
			if (screenEat) continue;
			if (!ledCube.processKeyboardEvent()) continue;
		}
		toploop: while (!virtualKeyPresses.isEmpty()) {
			KeyPress keyPress = virtualKeyPresses.poll();
			for (Screen screen : screenList) {
				if (!screen.isVisible() || !screen.isEnabled()) continue;
				boolean cont = GUI.doKeyboardEvent(screen.getContainer(), keyPress.key, true, keyPress.character);
				boolean cont2 = GUI.doKeyboardEvent(screen.getContainer(), keyPress.key, false, keyPress.character);
				if (!cont || !cont2) continue toploop;
			}
		}
	}

	private void processMouse() {
		boolean screenEat = false;
		toploop: while (Mouse.next()) {
			for (Screen screen : screenList)
				if (screen.isVisible() && screen.isEnabled() && !screen.processMouseEvent()) {
					screenEat = true;
					break;
				}
			for (InputBinding binding : InputBindingManager.getBindings()) {
				if (binding.getBind() != null && binding.getBind().getType() == InputInfo.Type.MOUSE && binding.getBind().getButton() == Mouse.getEventButton()) {
					if (screenEat) {
						binding.setPressed(false);
						continue toploop;
					}
					if (Mouse.getEventButtonState()) {
						if (!binding.onPressed()) {
							binding.setPressed(true);
							continue toploop;
						}
					} else {
						binding.setPressed(false);
						if (!binding.onReleased()) continue toploop;
					}
				}
			}
			if (screenEat) continue;
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

	private void processVRInput() {
		toploop: while(!vrInputEvents.isEmpty()) {
			VRInputEvent event = vrInputEvents.poll();
			//System.out.println(event);
			boolean screenEat = false;
			for (Screen screen : screenList) {
				if (screen.isVisible() && screen.isEnabled() && !screen.processVRInputEvent(event)) {
					screenEat = true;
					break;
				}
			}
			if (!screenEat && event.getController().getType() == ControllerType.RIGHT && showingVRGUI && mouseOverride != null) {
				if (event.isButtonPressEvent() && event.getButton() == ButtonType.TRIGGER) {
					if (event.getButtonState()) event.getController().triggerHapticPulse(2000);
					for (Screen screen : screenList)
						if (screen.isVisible() && screen.isEnabled() && !GUI.doMouseEvent(screen.getContainer(), 0, event.getButtonState(), 0)) screenEat = true;
				}
				if (event.isAxisEvent() && event.getAxis() == AxisType.TOUCHPAD) {
					if (vrScrollTimer.getMilliseconds() > 40 && Math.abs(event.getAxisDelta().getY()) > 4.0F) {
						vrScrollTimer.restart();
						event.getController().triggerHapticPulse(500);
						for (Screen screen : screenList)
							if (screen.isVisible() && screen.isEnabled() && !GUI.doMouseEvent(screen.getContainer(), -1, false, (int)(event.getAxisDelta().getY() * 10))) screenEat = true;
					}
				}
			}
			for (InputBinding binding : InputBindingManager.getBindings()) {
				if (event.isButtonPressEvent() && binding.getBind() != null && binding.getBind().getType() == InputInfo.Type.VR && binding.getBind().getVrControllerType() == event.getController().getType() && binding.getBind().getButton() == event.getButton().ordinal()) {
					if (screenEat) {
						binding.setPressed(false);
						continue toploop;
					}
					if (event.getButtonState()) {
						if (!binding.onPressed()) {
							binding.setPressed(true);
							continue toploop;
						}
					} else {
						binding.setPressed(false);
						if (!binding.onReleased()) continue toploop;
					}
				}
			}
			if (screenEat) continue;
			if (!ledCube.processVRInputEvent(event)) continue;
		}
	}

	public Vector2 getVRGUISize() {
		float ratio = (float)displayMode.getHeight() / (float)displayMode.getWidth();
		float scale = 0.75F;
		return new Vector2(scale, ratio * scale);
	}

	private void computeVRMouseAim() {
		if (vrMode && showingVRGUI) {
			VRTrackedController leftController = VRProvider.getController(ControllerType.LEFT);
			VRTrackedController rightController = VRProvider.getController(ControllerType.RIGHT);
			Quaternion leftControllerRot = leftController.getRotation().inverse();
			Vector3 rightControllerDir = rightController.getRotation().inverse().forward();
			Vector3 guiNormal = leftControllerRot.up();
			Vector3 guiRight = leftControllerRot.right();
			Vector3 guiUp = leftControllerRot.forward().negate();

			Vector2 guiSize = getVRGUISize();
			float guiWidthHalf = guiSize.getX() / 2;
			float guiHeightHalf = guiSize.getY() / 2;

			Vector3 guiPos = leftController.getPosition().add(leftControllerRot.forward().multiply(0.5F * guiSize.getY() + 0.05F));
			Vector3 guiTopLeft = guiPos.subtract(guiUp.multiply(guiHeightHalf)).subtract(guiRight.multiply(guiWidthHalf));

			float guiControllerDot = guiNormal.dot(rightControllerDir);
			if (Math.abs(guiControllerDot) > 0.00001F) {
				float intersectDist = -guiNormal.dot(rightController.getPosition().subtract(guiTopLeft)) / guiControllerDot;
				if (intersectDist > 0) {
					Vector3 pointOnPlane = rightController.getPosition().add(rightControllerDir.multiply(intersectDist));
					Vector3 relativePoint = pointOnPlane.subtract(guiTopLeft);
					float mouseX = relativePoint.dot(guiRight.divide(guiSize.getX()));
					float mouseY = relativePoint.dot(guiUp.divide(guiSize.getY()));

					if (mouseX >= 0 && mouseY >= 0 && mouseX <= 1 && mouseY <= 1) {
						setMouseOverride(new Vector2(Math.round(mouseX * displayMode.getWidth()), Math.round(mouseY * displayMode.getHeight())));
						return;
					}
				}
			}
			setMouseOverride(null);
		}
	}

	public void update(float delta) {
		if (!packetProcessQueue.isEmpty()) {
			Packet packet;
			while ((packet = packetProcessQueue.poll()) != null) {
				packet.process();
			}
		}

		InputBindingManager.getBindings().forEach(binding -> {
			if (binding.isPressed()) {
				binding.whilePressed();
			}
		});

		if (vrMode) VRProvider.getController(ControllerType.RIGHT).setScrolling(showingVRGUI && mouseOverride != null);

		camera.update(delta);
		textureManager.update(delta);
		ledCube.update(delta);

		if (!vrMode)
			lightingHandler.getLight(0).position = new Vector4f(camera.getPosition().getX(), camera.getPosition().getY(), camera.getPosition().getZ(), 1);

		for (Tuple<RenderPipeline, Integer> tuple : pipelines) {
			tuple.getA().update(delta);
		}

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
			addInfoText("Memory: " + Util.bytesToMBString(runtime.totalMemory() - runtime.freeMemory()) + " / " + Util.bytesToMBString(runtime.maxMemory()), 1010);
			Vector3 vector = camera.getAngle().forward();
			addInfoText("Camera vector: " + vector.getX() + ", " + vector.getY() + ", " + vector.getZ(), 1020);
			vector = camera.getPosition();
			addInfoText("Camera position: " + vector.getX() + ", " + vector.getY() + ", " + vector.getZ(), 1030);
			addInfoText("Rendered faces: " + faceCount, 1040);
			//debugFont.drawString(5, 5 + y++ * 25, "Cursor position: " + Util.getMouseX() + ", " + Util.getMouseY(), debugColor);
			//debugFont.drawString(5, 5 + y++ * 25, "Cursor offset: " + (Util.getMouseX() - getWidth() / 2) + ", " + (Util.getMouseY() - getHeight() / 2 + 1), debugColor);
			//debugFont.drawString(5, 5 + y++ * 25, "Entities: " + (world != null ? world.getEntityCount() : 0), debugColor);
		}
		if (convertingAudio) addInfoText("Converting audio...", 1500);

		if (regrab) {
			Mouse.setGrabbed(true);
			regrab = false;
		}
	}

	@SneakyThrows(IOException.class)
	public void render() {
		renderStart = System.nanoTime();
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, multisampleFBO);

		// Setup and render 3D
		/*glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(45, (float)displayMode.getWidth() / (float)displayMode.getHeight(), nearClip, 1000);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();*/
		//glMatrixMode(GL_PROJECTION);
		//glLoadIdentity();
		// State stuff
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

		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
		glBindFramebuffer(GL_READ_FRAMEBUFFER, multisampleFBO);
		glBlitFramebuffer(0, 0, displayMode.getWidth(), displayMode.getHeight(), 0, 0, displayMode.getWidth(), displayMode.getHeight(), GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT, GL_NEAREST);
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

		ledCube.render();

		for (Tuple<RenderPipeline, Integer> tuple : pipelines) {
			RenderPipeline pipeline = tuple.getA();
			pipeline.preRender3D();
			for (RenderCamera cam : renderCameras) {
				cam.setup();
				pipeline.render3D();
			}
			pipeline.postRender3D();
		}
		InstancedRenderer.resetVBOIndex();

		glPopMatrix();
	}

	public void render2D() {
		glPushMatrix();

		for (Tuple<RenderPipeline, Integer> tuple : pipelines) {
			tuple.getA().render2D();
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

	public void setupView(Matrix4f projection, Matrix4f view) {
		projectionMatrix = projection;
		viewMatrix = view;
		frustum.update(Util.matrixToArray(projectionMatrix), Util.matrixToArray(viewMatrix));
	}

	public void setupView(Matrix4f projection, Vector3 position, Quaternion rotation) {
		projectionMatrix = projection;
		viewMatrix.setIdentity();
		Matrix4f.mul(viewMatrix, (Matrix4f)rotation.getMatrix().negate(), viewMatrix);
		viewMatrix.translate(Util.convertVector(position.negate()));
		frustum.update(Util.matrixToArray(projectionMatrix), Util.matrixToArray(viewMatrix));
	}

	public void setupView(Matrix4f projection, Vector3 position, Angle angle) {
		projectionMatrix = projection;
		viewMatrix.setIdentity();
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
		projectionMatrix.store(matrixBuffer);
		matrixBuffer.rewind();
		glUniformMatrix4(projectionMatrixLoc, false, matrixBuffer);
		matrixBuffer.rewind();
		viewMatrix.store(matrixBuffer);
		matrixBuffer.rewind();
		glUniformMatrix4(viewMatrixLoc, false, matrixBuffer);
	}

	public Vector3[] getCursorRay() {
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

	public void useDisplayMode() {
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
			for (Runnable callback : resizeHandlers) {
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

	public void setLimitFramerate(boolean limitFramerate) {
		this.limitFramerate = limitFramerate;
		config.setProperty("display.limitframerate", limitFramerate);
	}

	public int addResizeHandler(Runnable resizeHandler) {
		if (resizeHandlers.add(resizeHandler))
			return resizeHandlers.indexOf(resizeHandler);
		return -1;
	}

	public boolean removeResizeHandler(Runnable resizeHandler) {
		return resizeHandlers.remove(resizeHandler);
	}

	public Runnable removeResizeHandler(int index) {
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

	 public void addRenderCamera(RenderCamera cam) {
		 renderCameras.add(cam);
	 }

	public void removeRenderCamera(RenderCamera cam) {
		renderCameras.remove(cam);
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

	public List<Tuple<String, Integer>> getDebugText() {
		return Collections.unmodifiableList(debugText);
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

	 public static void queueVirtualKeyPress(int key, char character) {
		 instance.virtualKeyPresses.add(new KeyPress(key, character));
	 }

	 public static void queueVRInputEvent(VRTrackedController controller, ButtonType button, AxisType axis, boolean buttonState, boolean buttonPress, Vector2 axisDelta) {
		 instance.vrInputEvents.add(new VRInputEvent(controller, button, axis, buttonState, buttonPress, axisDelta));
	 }

	 @Value private class ScreenHolder {
		 private int index;
		 private Screen screen;
	 }
}
