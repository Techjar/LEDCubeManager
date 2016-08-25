
package com.techjar.ledcm;

import com.techjar.ledcm.gui.screen.ScreenMainControl;
import com.techjar.ledcm.hardware.manager.ArduinoLEDManager;
import com.techjar.ledcm.hardware.CommThread;
import com.techjar.ledcm.hardware.LEDArray;
import com.techjar.ledcm.hardware.manager.LEDManager;
import com.techjar.ledcm.hardware.LEDUtil;
import com.techjar.ledcm.hardware.handler.SerialPortHandler;
import com.techjar.ledcm.hardware.SpectrumAnalyzer;
import com.techjar.ledcm.hardware.manager.TLC5940LEDManager;
import com.techjar.ledcm.hardware.manager.TestLEDManager;
import com.techjar.ledcm.hardware.animation.*;
import com.techjar.ledcm.hardware.handler.PortHandler;
import com.techjar.ledcm.util.Angle;
import com.techjar.ledcm.util.AxisAlignedBB;
import com.techjar.ledcm.util.Dimension3D;
import com.techjar.ledcm.util.Direction;
import com.techjar.ledcm.util.LEDCubeOctreeNode;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Model;
import com.techjar.ledcm.util.Quaternion;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector3;
import com.techjar.ledcm.util.input.InputBinding;
import com.techjar.ledcm.util.input.InputBindingManager;
import com.techjar.ledcm.util.input.InputInfo;
import com.techjar.ledcm.util.logging.LogHelper;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/**
 *
 * @author Techjar
 */
public class LEDCube {
    private Map<String, Animation> animations = new HashMap<>();
    private List<String> animationNames = new ArrayList<>();
    private LEDManager ledManager;
    private LEDCubeOctreeNode[] octrees;
    private int ledSpaceMult = 8;
    private boolean drawClick;
    private ReadableColor drawColor;
    private boolean postInited;
    private Vector3 cursorTrace;
    private Matrix4f transform = new Matrix4f();
    private Matrix4f renderTransform = new Matrix4f();
    @Getter private Vector3f centerPoint;
    @Getter private boolean reflectX;
    @Getter private boolean reflectY;
    @Getter private boolean reflectZ;
    @Getter private boolean trueColor;
    @Getter private CommThread commThread;
    @Getter private SpectrumAnalyzer spectrumAnalyzer;
    @Getter private Color paintColor = new Color(255, 255, 255);
    @Getter private boolean[] highlight;
    @Getter private Vector3 paintSize = new Vector3(0, 0, 0);
    @Getter @Setter private int layerIsolation = 0;
    @Getter @Setter private int selectedLayer = 0;
    @Getter @Setter private boolean previewTransform = true;
    @Getter private Model model;

    @SneakyThrows(Exception.class)
    public LEDCube() {
        if (LEDCubeManager.getLedManagerName() != null) {
            ledManager = (LEDManager)Class.forName("com.techjar.ledcm.hardware.manager." + LEDCubeManager.getLedManagerName()).getConstructor(String[].class).newInstance((Object)LEDCubeManager.getLedManagerArgs());
        } else {
            ledManager = new ArduinoLEDManager(4, false);
            //ledManager = new TLC5940LEDManager(true);
            //ledManager = new TestLEDManager(true, 16, 16, 16, false, new Color(255, 0, 0));
        }
        Dimension3D dim = ledManager.getDimensions();
        centerPoint = new Vector3f((dim.x - 1) / 2F, (dim.y - 1) / 2F, (dim.z - 1) / 2F);
        //setRenderOffset(Util.convertVector(centerPoint).multiply(ledSpaceMult).negate());
        highlight = new boolean[ledManager.getLEDCount()];
        model = LEDCubeManager.getModelManager().getModel("led.model");
        initOctree();
        initBindings();
        /*for (int i = 0; i < 256; i++) {
            double j = i;
            LogHelper.info(i + " | " + Math.round(MathHelper.cie1931(i / 255D) * 4095D));
        }*/
    }

    @SneakyThrows(Exception.class)
    public void postInit() {
        if (postInited) throw new IllegalStateException();
        postInited = true;
        spectrumAnalyzer = new SpectrumAnalyzer();
        commThread = new CommThread((PortHandler)Class.forName("com.techjar.ledcm.hardware.handler." + LEDCubeManager.getPortHandlerName()).newInstance());
        commThread.start();
        resetCameraPosition();
    }

    private void computeLEDHighlight() {
        for (int i = 0; i < highlight.length; i++) {
            highlight[i] = false;
        }
        if (cursorTrace != null && !Mouse.isGrabbed()) {
            Dimension3D dim = ledManager.getDimensions();
            for (int x = (int)cursorTrace.getX(); x <= Math.min((int)cursorTrace.getX() + (int)paintSize.getX(), dim.x - 1); x++) {
                for (int y = (int)cursorTrace.getY(); y <= Math.min((int)cursorTrace.getY() + (int)paintSize.getY(), dim.y - 1); y++) {
                    for (int z = (int)cursorTrace.getZ(); z <= Math.min((int)cursorTrace.getZ() + (int)paintSize.getZ(), dim.z - 1); z++) {
                        if (isLEDWithinIsolation(x, y, z)) {
                            highlight[Util.encodeCubeVector(x, y, z)] = true;
                        }
                    }
                }
            }
        }
    }

    private void paintLEDHighlight(ReadableColor color) {
        Dimension3D dim = ledManager.getDimensions();
        for (int x = 0; x < dim.x; x++) {
            for (int y = 0; y < dim.y; y++) {
                for (int z = 0; z < dim.z; z++) {
                    if (highlight[Util.encodeCubeVector(x, y, z)]) {
                        ledManager.setLEDColor(x, y, z, color);
                    }
                }
            }
        }
    }

    public void preProcess() {
        cursorTrace = traceCursorToLED();
        computeLEDHighlight();
    }

    public boolean processKeyboardEvent() {
        if (Keyboard.getEventKeyState()) {
            //if (Keyboard.getEventKey() == Keyboard.KEY_F11) setFullscreen(!fullscreen);
        }
        return true;
    }

    public boolean processMouseEvent() {
        if (!Mouse.isGrabbed() && drawClick) {
            paintLEDHighlight(drawColor);
        }
        return !drawClick;
    }

    public boolean processControllerEvent(Controller controller) {
        return true;
    }

    public void update(float delta) {
        LEDCubeManager.addInfoText("Serial port: " + (commThread.isPortOpen() ? "open" : "closed"), 100);
        LEDCubeManager.addInfoText("TCP clients: " + commThread.getNumTCPClients(), 110);
        if (!spectrumAnalyzer.getCurrentTrack().isEmpty()) {
            LEDCubeManager.addInfoText("Music: " + spectrumAnalyzer.getCurrentTrack(), 120);
            LEDCubeManager.addInfoText("Music time: " + spectrumAnalyzer.getPositionMillis(), 130);
        }
        if (commThread.getCurrentSequence() != null) LEDCubeManager.addInfoText("Sequence: " + commThread.getCurrentSequence().getName(), 140);
        if (commThread.isFrozen()) LEDCubeManager.addInfoText("Animation Frozen", 150);
        if (ledManager.getResolution() < 255) LEDCubeManager.addInfoText("Color mode: " + (trueColor ? "true" : "full"), 160);
        if (cursorTrace == null) {
            LEDCubeManager.addInfoText("Hovered LED: none", 900);
        } else {
            Color color = ledManager.getLEDColor((int)cursorTrace.getX(), (int)cursorTrace.getY(), (int)cursorTrace.getZ());
            LEDCubeManager.addInfoText("Hovered LED: " + (int)cursorTrace.getX() + ", " + (int)cursorTrace.getY() + ", " + (int)cursorTrace.getZ() + " (" + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue() + ")", 900);
        }
    }

    public void render() {
        float mult = ledSpaceMult;

        Dimension3D dim = ledManager.getDimensions();
        LEDArray ledArray = previewTransform ? ledManager.getLEDArray().getTransformed() : ledManager.getLEDArray();
        for (int y = 0; y < dim.y; y++) {
            for (int z = 0; z < dim.z; z++) {
                for (int x = 0; x < dim.x; x++) {
                    if (isLEDWithinIsolation(x, y, z)) {
                        Vector3 pos = new Vector3(x * mult, y * mult, z * mult);
                        Color color;
                        if (trueColor) {
                            Color ledColor = ledArray.getLEDColorReal(x, y, z);
                            color = new Color(Math.round(ledColor.getRed() * ledManager.getFactor()), Math.round(ledColor.getGreen() * ledManager.getFactor()), Math.round(ledColor.getBlue() * ledManager.getFactor()));
                        } else color = ledArray.getLEDColor(x, y, z);
                        model.render(Util.transformVector(pos, renderTransform, false), new Quaternion(), color);
                    }
                }
            }
        }

        for (int y = 0; y < dim.y; y++) {
            for (int x = 0; x < dim.x; x++) {
                for (int z = 0; z < dim.z; z++) {
                    if (highlight[Util.encodeCubeVector(x, y, z)]) {
                        if (isLEDWithinIsolation(x, y, z)) {
                            Vector3 pos = new Vector3(x * mult, y * mult, z * mult);
                            model.render(Util.transformVector(pos, renderTransform, false), new Quaternion(), new Color(paintColor.getRed(), paintColor.getGreen(), paintColor.getBlue(), 32), new Vector3(1.2F, 1.2F, 1.2F));
                        }
                    }
                }
            }
        }
    }

    public LEDManager getLEDManager() {
        return ledManager;
    }

    public boolean isLEDWithinIsolation(Vector3 vector) {
        return isLEDWithinIsolation((int)vector.getX(), (int)vector.getY(), (int)vector.getZ());
    }

    public void setPaintColor(Color color) {
        paintColor.set(color.getRed(), color.getGreen(), color.getBlue());
        LEDCubeManager.getInstance().getScreenMainControl().redColorSlider.setValue(color.getRed() / 255F);
        LEDCubeManager.getInstance().getScreenMainControl().greenColorSlider.setValue(color.getGreen() / 255F);
        LEDCubeManager.getInstance().getScreenMainControl().blueColorSlider.setValue(color.getBlue() / 255F);
    }

    public void resetCameraPosition() {
        LEDCubeManager.getCamera().setPosition(new Vector3(-80, 85, 28));
        LEDCubeManager.getCamera().setAngle(new Angle(-31, -90, 0));
        // Some crazy code I experimented with...
        /*Dimension3D dim = ledManager.getDimensions();
        int distDim = dim.x > dim.z && dim.x > dim.y * 2 ? dim.x / 2 : (dim.z > dim.y * 2 ? dim.z / 2 : dim.y);
        int heightDim = Math.max(dim.y, dim.z);
        Angle angle = new Angle(-31, -90, 0);
        LEDCubeManager.getCamera().setPosition(Util.convertVector(centerPoint).multiply(ledSpaceMult).subtract(new Vector3(0, ledSpaceMult * (heightDim / 8), 0)).add(angle.forward().negate().multiply(130 * (distDim / 8))));
        LEDCubeManager.getCamera().setAngle(angle);*/
    }

    private void initBindings() {
        InputBindingManager.addBinding(new InputBinding("reloadanimation", "Reload Current", "Animation", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_R)) {
            @Override
            public boolean onPressed() {
                if (commThread.getCurrentSequence() == null) {
                    Animation anim = commThread.getCurrentAnimation();
                    try {
                        animations.put(anim.getName(), anim.getClass().newInstance());
                        animations.get(anim.getName()).postLoadInitOptions();
                        commThread.setCurrentAnimation(animations.get(anim.getName()));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return false;
                }
                return true;
            }

            @Override
            public boolean onReleased() {
                return true;
            }
        });
        InputBindingManager.addBinding(new InputBinding("reloadallanimations", "Reload All", "Animation", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_Y)) {
            @Override
            public boolean onPressed() {
                if (commThread.getCurrentSequence() == null) {
                    loadAnimations();
                    return false;
                }
                return true;
            }

            @Override
            public boolean onReleased() {
                return true;
            }
        });
        InputBindingManager.addBinding(new InputBinding("resetcamera", "Reset Position", "Camera", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_F)) {
            @Override
            public boolean onPressed() {
                resetCameraPosition();
                return false;
            }

            @Override
            public boolean onReleased() {
                return true;
            }
        });
        InputBindingManager.addBinding(new InputBinding("togglecolor", "Toggle Color", "Cube", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_H)) {
            @Override
            public boolean onPressed() {
                trueColor = !trueColor;
                float increment = trueColor ? 1F / ledManager.getResolution() : 1F / 255F;
                LEDCubeManager.getInstance().getScreenMainControl().redColorSlider.setIncrement(increment);
                LEDCubeManager.getInstance().getScreenMainControl().greenColorSlider.setIncrement(increment);
                LEDCubeManager.getInstance().getScreenMainControl().blueColorSlider.setIncrement(increment);
                return false;
            }

            @Override
            public boolean onReleased() {
                return true;
            }
        });
        InputBindingManager.addBinding(new InputBinding("clearleds", "Clear LEDs", "Cube", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_C)) {
            @Override
            public boolean onPressed() {
                LEDUtil.clear(ledManager);
                return false;
            }

            @Override
            public boolean onReleased() {
                return true;
            }
        });
        InputBindingManager.addBinding(new InputBinding("freezeanimation", "Freeze", "Animation", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_X)) {
            @Override
            public boolean onPressed() {
                commThread.setFrozen(!commThread.isFrozen());
                return false;
            }

            @Override
            public boolean onReleased() {
                return true;
            }
        });
        InputBindingManager.addBinding(new InputBinding("paintleds", "Paint LEDs", "Cube", true, new InputInfo(InputInfo.Type.MOUSE, 0)) {
            @Override
            public boolean onPressed() {
                if (!Mouse.isGrabbed()) {
                    drawColor = paintColor;
                    drawClick = true;
                    paintLEDHighlight(drawColor);
                    return false;
                }
                return true;
            }

            @Override
            public boolean onReleased() {
                drawClick = false;
                return true;
            }
        });
        InputBindingManager.addBinding(new InputBinding("eraseleds", "Erase LEDs", "Cube", true, new InputInfo(InputInfo.Type.MOUSE, 2)) {
            @Override
            public boolean onPressed() {
                if (!Mouse.isGrabbed()) {
                    drawColor = ReadableColor.BLACK;
                    drawClick = true;
                    paintLEDHighlight(drawColor);
                    return false;
                }
                return true;
            }

            @Override
            public boolean onReleased() {
                drawClick = false;
                return true;
            }
        });
        InputBindingManager.addBinding(new InputBinding("floodfill", "Flood Fill", "Cube", true, new InputInfo(InputInfo.Type.MOUSE, 1)) {
            @Override
            public boolean onPressed() {
                if (!Mouse.isGrabbed()) {
                    if (cursorTrace != null) {
                        Dimension3D dim = ledManager.getDimensions();
                        LEDArray ledArray = ledManager.getLEDArray();
                        Color targetColor = ledArray.getLEDColor((int)cursorTrace.getX(), (int)cursorTrace.getY(), (int)cursorTrace.getZ());
                        if (!targetColor.equals(paintColor)) {
                            boolean[] processed = new boolean[ledManager.getLEDCount()];
                            LinkedList<Vector3> stack = new LinkedList<>();
                            stack.push(cursorTrace);
                            while (!stack.isEmpty()) {
                                Vector3 current = stack.pop();
                                Color color = ledArray.getLEDColor((int)current.getX(), (int)current.getY(), (int)current.getZ());
                                if (color.equals(targetColor) && isLEDWithinIsolation(current)) {
                                    ledManager.setLEDColor((int)current.getX(), (int)current.getY(), (int)current.getZ(), paintColor);
                                    processed[Util.encodeCubeVector(current)] = true;
                                    for (int i = 0; i < 6; i++) {
                                        Vector3 offset = Direction.values()[i].getVector();
                                        Vector3 node = current.add(offset);
                                        if (node.getX() >= 0 && node.getX() < dim.x && node.getY() >= 0 && node.getY() < dim.y && node.getZ() >= 0 && node.getZ() < dim.z && !processed[Util.encodeCubeVector(node)]) {
                                            stack.push(node);
                                        }
                                    }
                                }
                            }
                        }
                        return false;
                    }
                }
                return true;
            }

            @Override
            public boolean onReleased() {
                return true;
            }
        });
        InputBindingManager.addBinding(new InputBinding("resettransform", "Reset Rotation", "Cube", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_F3)) {
            @Override
            public boolean onPressed() {
                resetTransform();
                return false;
            }

            @Override
            public boolean onReleased() {
                return true;
            }
        });
    }

    private void initOctree() {
        Dimension3D dim = ledManager.getDimensions();
        int minDim = Util.getNextPowerOfTwo(Math.min(dim.x, Math.min(dim.y, dim.z)));
        float octreeSize = ledSpaceMult * minDim;
        ArrayList<LEDCubeOctreeNode> list = new ArrayList<>();
        int multX = (int)Math.ceil(dim.x / (float)minDim) * minDim;
        int multY = (int)Math.ceil(dim.y / (float)minDim) * minDim;
        int multZ = (int)Math.ceil(dim.z / (float)minDim) * minDim;
        for (int x = 0; x < multX; x += minDim) {
            for (int y = 0; y < multY; y += minDim) {
                for (int z = 0; z < multZ; z += minDim) {
                    Vector3 offset = new Vector3(octreeSize * (x / minDim), octreeSize * (y / minDim), octreeSize * (z / minDim));
                    LEDCubeOctreeNode octree = new LEDCubeOctreeNode(new AxisAlignedBB(new Vector3(-ledSpaceMult / 2, -ledSpaceMult / 2, -ledSpaceMult / 2).add(offset), new Vector3(octreeSize + (ledSpaceMult / 2), octreeSize + (ledSpaceMult / 2), octreeSize + (ledSpaceMult / 2)).add(offset)));
                    recursiveFillOctree(octree, octreeSize / 2, minDim, new Vector3(x, y, z));
                    list.add(octree);
                }
            }
        }
        list.toArray(octrees = new LEDCubeOctreeNode[list.size()]);
    }

    private void recursiveFillOctree(LEDCubeOctreeNode node, float size, int count, Vector3 ledPos) {
        AxisAlignedBB nodeAABB = node.getAABB();
        for (int i = 0; i < 8; i++) {
            int x = (i & 1);
            int y = ((i >> 1) & 1);
            int z = ((i >> 2) & 1);
            float xOffset = x * size;
            float yOffset = y * size;
            float zOffset = z * size;
            if (count > 1) {
                Dimension3D dim = ledManager.getDimensions();
                Vector3 nextLedPos = ledPos.add(new Vector3((count / 2) * x, (count / 2) * y, (count / 2) * z));
                if (nextLedPos.getX() >= dim.x || nextLedPos.getY() >= dim.y || nextLedPos.getZ() >= dim.z) continue;
                LEDCubeOctreeNode newNode = new LEDCubeOctreeNode(new AxisAlignedBB(nodeAABB.getMinPoint().add(new Vector3(xOffset, yOffset, zOffset)), nodeAABB.getMinPoint().add(new Vector3(xOffset + size, yOffset + size, zOffset + size))));
                node.setNode(i, newNode);
                recursiveFillOctree(newNode, size / 2, count / 2, nextLedPos);
            } else {
                AxisAlignedBB modelAABB = model.getAABB();
                node.setNode(i, new LEDCubeOctreeNode(new AxisAlignedBB(modelAABB.getMinPoint().add(ledPos.multiply(ledSpaceMult)), modelAABB.getMaxPoint().add(ledPos.multiply(ledSpaceMult))), ledPos));
            }
        }
    }

    private Vector3 recursiveIntersectOctree(LEDCubeOctreeNode node, Vector3 point) {
        if (node.getLEDPosition() != null) {
            return node.getLEDPosition();
        }
        for (int i = 0; i < 8; i++) {
            LEDCubeOctreeNode nextNode = node.getNode(i);
            if (nextNode != null && nextNode.getAABB().containsPoint(point)) {
                Vector3 ret = recursiveIntersectOctree(nextNode, point);
                if (ret != null) return ret;
            }
        }
        return null;
    }

    public Vector3 traceCursorToLED() {
        Vector3[] ray = LEDCubeManager.getInstance().getCursorRay();
        Vector3 position = Util.transformVector(ray[0], Matrix4f.invert(renderTransform, null), false);
        Vector3 direction = ray[1].multiply(0.5F);

        float mult = ledSpaceMult;
        Dimension3D dim = ledManager.getDimensions();
        for (float step = 1; step < 5000; step += 2) {
            Vector3 rayPos = position.add(direction.multiply(step));
            if (octrees == null) {
                for (int y = 0; y < dim.y; y++) {
                    for (int z = 0; z < dim.z; z++) {
                        for (int x = 0; x < dim.x; x++) {
                            float xx = x * mult;
                            float yy = y * mult;
                            float zz = z * mult;
                            Vector3 pos = new Vector3(xx, yy, zz);
                            if (model.getAABB().containsPoint(pos, rayPos) && isLEDWithinIsolation(x, y, z)) {
                                return new Vector3(x, y, z);
                            }
                        }
                    }
                }
            } else {
                Vector3 ret = null;
                for (int i = 0; i < octrees.length; i++) {
                    ret = recursiveIntersectOctree(octrees[i], rayPos);
                    if (ret != null) break;
                }
                if (ret != null) {
                    if (isLEDWithinIsolation((int)ret.getX(), (int)ret.getY(), (int)ret.getZ())) {
                        return ret;
                    }
                }
            }
        }
        return null;
    }

    public boolean isLEDWithinIsolation(int x, int y, int z) {
        switch (layerIsolation) {
            case 1: return x == selectedLayer;
            case 2: return y == selectedLayer;
            case 3: return z == selectedLayer;
        }
        return true;
    }

    public void rotateTransform(float radians, Vector3 axis) {
        transform.translate(centerPoint);
        transform.rotate(radians, Util.convertVector(axis));
        transform.translate(centerPoint.negate(null));
    }

    public void resetTransform() {
        transform.setIdentity();
    }

    public void setReflection(boolean x, boolean y, boolean z) {
        reflectX = x;
        reflectY = y;
        reflectZ = z;
    }

    public Vector3 applyTransform(Vector3 vector) {
        Dimension3D dim = ledManager.getDimensions();
        vector = vector.copy();
        if (reflectX) vector.setX((dim.x - 1) - vector.getX());
        if (reflectY) vector.setY((dim.y - 1) - vector.getY());
        if (reflectZ) vector.setZ((dim.z - 1) - vector.getZ());
        Vector4f vec = Matrix4f.transform(transform, new Vector4f(vector.getX(), vector.getY(), vector.getZ(), 1), null);
        return new Vector3(Math.round(vec.x), Math.round(vec.y), Math.round(vec.z));
    }
    
    public void setRenderOffset(Vector3 offset) {
    	renderTransform.setIdentity();
    	renderTransform.translate(Util.convertVector(offset));
    }

    public void loadAnimations() {
        animations.clear();
        animationNames.clear();
        addAnimation(new AnimationNone());
        addAnimation(new AnimationSpectrumBars());
        addAnimation(new AnimationSpectrumShooters());
        //addAnimation(new AnimationIndividualTest());
        //addAnimation(new AnimationCharTest());
        addAnimation(new AnimationText());
        addAnimation(new AnimationStaticFill());
        addAnimation(new AnimationGradient());
        addAnimation(new AnimationRandomize());
        addAnimation(new AnimationRain());
        addAnimation(new AnimationMatrix());
        //addAnimation(new AnimationFolder());
        addAnimation(new AnimationTwinkle());
        addAnimation(new AnimationStrobe());
        addAnimation(new AnimationSnake());
        addAnimation(new AnimationScrollers());
        addAnimation(new AnimationDissolve());
        addAnimation(new AnimationSine());
        addAnimation(new AnimationStacker());
        addAnimation(new AnimationDrain());
        addAnimation(new AnimationFaucet());
        addAnimation(new AnimationSlidingBoxes());
        addAnimation(new AnimationBalls());
        addAnimation(new AnimationColorSpectrum());
        addAnimation(new AnimationSlidingPanels());
        addAnimation(new AnimationFireworks());
        addAnimation(new AnimationWalls());
        addAnimation(new AnimationWireframe());
        addAnimation(new AnimationWipe());
        for (Animation anim : animations.values()) {
            anim.postLoadInitOptions();
        }
        if (LEDCubeManager.getInstance().getScreenMainControl() != null) {
            LEDCubeManager.getInstance().getScreenMainControl().populateAnimationList();
        }
    }

    private void addAnimation(Animation animation) {
        animations.put(animation.getName(), animation);
        if (!animation.isHidden()) animationNames.add(animation.getName());
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
}
