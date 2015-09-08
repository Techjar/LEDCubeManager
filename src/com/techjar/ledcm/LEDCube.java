
package com.techjar.ledcm;

import com.techjar.ledcm.gui.screen.ScreenMainControl;
import com.techjar.ledcm.hardware.ArduinoLEDManager;
import com.techjar.ledcm.hardware.CommThread;
import com.techjar.ledcm.hardware.LEDManager;
import com.techjar.ledcm.hardware.LEDUtil;
import com.techjar.ledcm.hardware.SpectrumAnalyzer;
import com.techjar.ledcm.hardware.StripLEDManager;
import com.techjar.ledcm.hardware.TLC5940LEDManager;
import com.techjar.ledcm.hardware.TestHugeLEDManager;
import com.techjar.ledcm.hardware.TestReallyHugeLEDManager;
import com.techjar.ledcm.hardware.animation.*;
import com.techjar.ledcm.util.Angle;
import com.techjar.ledcm.util.AxisAlignedBB;
import com.techjar.ledcm.util.Dimension3D;
import com.techjar.ledcm.util.Direction;
import com.techjar.ledcm.util.LEDCubeOctreeNode;
import com.techjar.ledcm.util.Model;
import com.techjar.ledcm.util.Quaternion;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector3;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Color;

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
    private boolean postInited;
    @Getter private boolean trueColor;
    @Getter private CommThread commThread;
    @Getter private SpectrumAnalyzer spectrumAnalyzer;
    @Getter private Color paintColor = new Color(255, 255, 255);
    @Getter private boolean[] highlight;
    @Getter private Vector3 paintSize = new Vector3(0, 0, 0);
    @Getter @Setter private int layerIsolation = 0;
    @Getter @Setter private int selectedLayer = 0;
    @Getter private Model model;

    public LEDCube() {
        ledManager = new StripLEDManager(110, true);
        //ledManager = new ArduinoLEDManager(4, false);
        //ledManager = new TLC5940LEDManager(true);
        //ledManager = new TestHugeLEDManager(true);
        //ledManager = new TestReallyHugeLEDManager(true);
        highlight = new boolean[ledManager.getLEDCount()];
        model = LEDCubeManager.getModelManager().getModel("flatled.model");
        initOctree();
        /*for (int i = 0; i < 64; i++) {
            double j = i;
            LogHelper.info(Math.round(MathHelper.cie1931(j/63)*63));
        }*/
    }

    public void postInit() throws IOException {
        if (postInited) throw new IllegalStateException();
        postInited = true;
        spectrumAnalyzer = new SpectrumAnalyzer();
        commThread = new CommThread();
        commThread.start();
        LEDCubeManager.getCamera().setPosition(new Vector3(-500, 184, -30));
        LEDCubeManager.getCamera().setAngle(new Angle(-21, -123, 0));
    }

    public boolean processKeyboardEvent() {
        if (Keyboard.getEventKeyState()) {
            //if (Keyboard.getEventKey() == Keyboard.KEY_F11) setFullscreen(!fullscreen);
            if (Keyboard.getEventKey() == Keyboard.KEY_Y && commThread.getCurrentSequence() == null) {
                loadAnimations();
                return false;
            } else if (Keyboard.getEventKey() == Keyboard.KEY_R && commThread.getCurrentAnimation() != null) {
                Animation anim = commThread.getCurrentAnimation();
                try {
                    animations.put(anim.getName(), anim.getClass().newInstance());
                    commThread.setCurrentAnimation(animations.get(anim.getName()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return false;
            } else if (Keyboard.getEventKey() == Keyboard.KEY_F) {
                LEDCubeManager.getCamera().setPosition(new Vector3(-500, 184, -30));
                LEDCubeManager.getCamera().setAngle(new Angle(-21, -123, 0));
                return false;
            } else if (Keyboard.getEventKey() == Keyboard.KEY_H) {
                trueColor = !trueColor;
                float increment = trueColor ? 1F / ledManager.getResolution() : 1F / 255F;
                LEDCubeManager.getInstance().getScreenMainControl().redColorSlider.setIncrement(increment);
                LEDCubeManager.getInstance().getScreenMainControl().greenColorSlider.setIncrement(increment);
                LEDCubeManager.getInstance().getScreenMainControl().blueColorSlider.setIncrement(increment);
                return false;
            } else if (Keyboard.getEventKey() == Keyboard.KEY_C) {
                LEDUtil.clear(ledManager);
                return false;
            }
        }
        return true;
    }

    public boolean processMouseEvent() {
        for (int i = 0; i < highlight.length; i++) {
            highlight[i] = false;
        }
        if (Mouse.getEventButtonState() && Mouse.getEventButton() == 0) drawClick = true;
        else if (!Mouse.getEventButtonState() && Mouse.getEventButton() == 0) drawClick = false;
        if (!Mouse.isGrabbed()) {
            Vector3 led = traceCursorToLED();
            if (led != null) {
                Dimension3D dim = ledManager.getDimensions();
                for (int x = (int)led.getX(); x <= Math.min((int)led.getX() + (int)paintSize.getX(), dim.x - 1); x++) {
                    for (int y = (int)led.getY(); y <= Math.min((int)led.getY() + (int)paintSize.getY(), dim.y - 1); y++) {
                        for (int z = (int)led.getZ(); z <= Math.min((int)led.getZ() + (int)paintSize.getZ(), dim.z - 1); z++) {
                            if (isLEDWithinIsolation(x, y, z)) {
                                highlight[Util.encodeCubeVector(x, y, z)] = true;
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
                        boolean[] processed = new boolean[ledManager.getLEDCount()];
                        LinkedList<Vector3> stack = new LinkedList<>();
                        stack.push(led);
                        while (!stack.isEmpty()) {
                            Vector3 current = stack.pop();
                            Color color = ledManager.getLEDColor((int)current.getX(), (int)current.getY(), (int)current.getZ());
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
                }
            }
        }
        return !drawClick;
    }

    public boolean processControllerEvent(Controller controller) {
        return true;
    }

    public void update(float delta) {
        // we don't do anything here... yet...
    }

    public int render() {
        int faceCount = 0;
        float mult = ledSpaceMult;

        Dimension3D dim = ledManager.getDimensions();
        Color[] colors = new Color[ledManager.getLEDCount()];
        synchronized (ledManager) {
            for (int y = 0; y < dim.y; y++) {
                for (int z = 0; z < dim.z; z++) {
                    for (int x = 0; x < dim.x; x++) {
                        if (trueColor) {
                            Color color = ledManager.getLEDColorReal(x, y, z);
                            colors[Util.encodeCubeVector(x, y, z)] = new Color(Math.round(color.getRed() * ledManager.getFactor()), Math.round(color.getGreen() * ledManager.getFactor()), Math.round(color.getBlue() * ledManager.getFactor()));
                        } else {
                            colors[Util.encodeCubeVector(x, y, z)] = ledManager.getLEDColor(x, y, z);
                        }
                    }

                }
            }
        }
        for (int y = 0; y < dim.y; y++) {
            for (int z = 0; z < dim.z; z++) {
                for (int x = 0; x < dim.x; x++) {
                    if (isLEDWithinIsolation(x, y, z)) {
                        Vector3 pos = new Vector3(z * mult, y * mult, x * mult);
                        faceCount += model.render(pos, new Quaternion(), colors[Util.encodeCubeVector(x, y, z)]);
                    }
                }
            }
        }

        for (int y = 0; y < dim.y; y++) {
            for (int x = 0; x < dim.x; x++) {
                for (int z = 0; z < dim.z; z++) {
                    if (highlight[Util.encodeCubeVector(x, y, z)]) {
                        if (isLEDWithinIsolation(x, y, z)) {
                            Vector3 pos = new Vector3(z * mult, y * mult, x * mult);
                            faceCount += model.render(pos, new Quaternion(), new Color(paintColor.getRed(), paintColor.getGreen(), paintColor.getBlue(), 32), new Vector3(1.2F, 1.2F, 1.2F));
                        }
                    }
                }
            }
        }
        return faceCount;
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

    private void initOctree() {
        Dimension3D dim =  ledManager.getDimensions();
        if (/*dim.x != dim.y || dim.x != dim.z || dim.y != dim.z ||*/ !Util.isPowerOfTwo(dim.x) || !Util.isPowerOfTwo(dim.y) || !Util.isPowerOfTwo(dim.z)) return; // Non-cubes and non-powers-of-two need special handling here
        int minDim = Math.min(dim.x, Math.min(dim.y, dim.z));
        float octreeSize = ledSpaceMult * minDim;
        ArrayList<LEDCubeOctreeNode> list = new ArrayList<>();
        for (int x = 0; x < dim.x; x += minDim) {
            for (int y = 0; y < dim.y; y += minDim) {
                for (int z = 0; z < dim.z; z += minDim) {
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
                LEDCubeOctreeNode newNode = new LEDCubeOctreeNode(new AxisAlignedBB(nodeAABB.getMinPoint().add(new Vector3(xOffset, yOffset, zOffset)), nodeAABB.getMinPoint().add(new Vector3(xOffset + size, yOffset + size, zOffset + size))));
                node.setNode(i, newNode);
                recursiveFillOctree(newNode, size / 2, count / 2, ledPos.add(new Vector3((count / 2) * x, (count / 2) * y, (count / 2) * z)));
            } else {
                AxisAlignedBB modelAABB = model.getAABB();
                node.setNode(i, new LEDCubeOctreeNode(new AxisAlignedBB(modelAABB.getMinPoint().add(ledPos.multiply(ledSpaceMult)), modelAABB.getMaxPoint().add(ledPos.multiply(ledSpaceMult))), new Vector3(ledPos.getZ(), ledPos.getY(), ledPos.getX())));
            }
        }
    }

    private Vector3 recursiveIntersectOctree(LEDCubeOctreeNode node, Vector3 point) {
        if (node.getNode(0) != null) {
            for (int i = 0; i < 8; i++) {
                LEDCubeOctreeNode nextNode = node.getNode(i);
                if (nextNode.getAABB().containsPoint(point)) {
                    Vector3 ret = recursiveIntersectOctree(nextNode, point);
                    if (ret != null) return ret;
                }
            }
        } else {
            return node.getLEDPosition();
        }
        return null;
    }

    public Vector3 traceCursorToLED() {
        Vector3[] ray = LEDCubeManager.getInstance().getCursorRay();
        Vector3 position = ray[0];
        Vector3 direction = ray[1].multiply(0.5F);

        float mult = ledSpaceMult;
        Dimension3D dim = ledManager.getDimensions();
        for (float step = 1; step < 5000; step += 2) {
            Vector3 rayPos = position.add(direction.multiply(step));
            if (octrees == null) {
                for (int y = 0; y < dim.y; y++) {
                    for (int z = 0; z < dim.z; z++) {
                        for (int x = 0; x < dim.x; x++) {
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

    public void loadAnimations() {
        animations.clear();
        animationNames.clear();
        addAnimation(new AnimationNone());
        addAnimation(new AnimationSpectrumDots());
        addAnimation(new AnimationSpectrumTicker());
        //addAnimation(new AnimationScreenBlend()); // Causes massive system lag, do not use!
        //addAnimation(new AnimationIndividualTest());
        addAnimation(new AnimationStaticFill());
        addAnimation(new AnimationStaticHue());
        addAnimation(new AnimationGradient());
        addAnimation(new AnimationPulsate());
        addAnimation(new AnimationPulsateHue());
        addAnimation(new AnimationRandomize());
        addAnimation(new AnimationMatrix());
        addAnimation(new AnimationTwinkle());
        addAnimation(new AnimationStrobe());
        addAnimation(new AnimationPingPong());
        addAnimation(new AnimationTicker());
        addAnimation(new AnimationProgressiveFill());
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
