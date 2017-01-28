
package com.techjar.ledcm;

import com.techjar.ledcm.hardware.manager.ArduinoLEDManager;
import com.techjar.ledcm.hardware.CommThread;
import com.techjar.ledcm.hardware.LEDArray;
import com.techjar.ledcm.hardware.manager.LEDManager;
import com.techjar.ledcm.hardware.LEDUtil;
import com.techjar.ledcm.hardware.SpectrumAnalyzer;
import com.techjar.ledcm.hardware.animation.*;
import com.techjar.ledcm.hardware.handler.PortHandler;
import com.techjar.ledcm.hardware.manager.TestLEDManager;
import com.techjar.ledcm.render.InstancedRenderer;
import com.techjar.ledcm.util.math.Angle;
import com.techjar.ledcm.util.AxisAlignedBB;
import com.techjar.ledcm.util.math.Dimension3D;
import com.techjar.ledcm.util.math.Direction;
import com.techjar.ledcm.util.LEDCubeOctreeNode;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Model;
import com.techjar.ledcm.util.math.MutableVector3;
import com.techjar.ledcm.util.math.PooledMutableVector3;
import com.techjar.ledcm.util.math.Quaternion;
import com.techjar.ledcm.util.Util;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.techjar.ledcm.vr.VRTrackedController.TouchpadMode;
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
	@Getter private float spaceMult = 0.08F;
	private boolean postInited;
	private List<LEDSelection> ledSelections = new ArrayList<>();
	private Matrix4f transform = new Matrix4f();
	private Matrix4f renderTransform = new Matrix4f();
	private Vector3 renderOffset = new Vector3();
	private Vector3 renderScale = new Vector3(1, 1, 1);
	@Getter private Vector3f centerPoint;
	@Getter private boolean reflectX;
	@Getter private boolean reflectY;
	@Getter private boolean reflectZ;
	@Getter private boolean trueColor;
	@Getter private CommThread commThread;
	@Getter private SpectrumAnalyzer spectrumAnalyzer;
	@Getter private Color paintColor = new Color(255, 255, 255);
	@Getter private MutableVector3 paintSize = new MutableVector3(0, 0, 0);
	@Getter @Setter private int layerIsolation = 0;
	@Getter @Setter private int selectedLayer = 0;
	@Getter @Setter private boolean previewTransform = true;
	@Getter private Model model;
	private InstancedRenderer.InstanceItem[] instanceItems;
	private InstancedRenderer.InstanceItem[] highlightInstanceItems;

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
		instanceItems = new InstancedRenderer.InstanceItem[dim.x * dim.y * dim.z];
		highlightInstanceItems = new InstancedRenderer.InstanceItem[dim.x * dim.y * dim.z];
		//setRenderOffset(Util.convertVector(centerPoint).multiply(ledSpaceMult).negate());
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
		if (LEDCubeManager.getInstance().isVrMode()) {
			VRProvider.getController(ControllerType.RIGHT).setTouchpadMode(TouchpadMode.SPLIT_UD);
		}
	}

	public void cleanup() {
		if (spectrumAnalyzer != null) spectrumAnalyzer.close();
		if (commThread != null) commThread.closePort();
	}

	private void computeLEDHighlight() {
		for (LEDSelection selection : ledSelections) {
			if (!Mouse.isGrabbed()) {
				Dimension3D dim = ledManager.getDimensions();
				Vector3 vector = selection.position;
				for (int x = (int)vector.getX(); x <= Math.min((int)vector.getX() + (int)paintSize.getX(), dim.x - 1); x++) {
					for (int y = (int)vector.getY(); y <= Math.min((int)vector.getY() + (int)paintSize.getY(), dim.y - 1); y++) {
						for (int z = (int)vector.getZ(); z <= Math.min((int)vector.getZ() + (int)paintSize.getZ(), dim.z - 1); z++) {
							if (isLEDWithinIsolation(x, y, z)) {
								selection.highlight[ledManager.encodeVector(x, y, z)] = true;
							}
						}
					}
				}
			}
		}
	}

	private boolean paintLEDHighlight(boolean[] highlight, ReadableColor color) {
		boolean changed = false;
		Dimension3D dim = ledManager.getDimensions();
		for (int x = 0; x < dim.x; x++) {
			for (int y = 0; y < dim.y; y++) {
				for (int z = 0; z < dim.z; z++) {
					if (highlight[ledManager.encodeVector(x, y, z)]) {
						Color oldColor = ledManager.getLEDColor(x, y, z);
						if (!oldColor.equals(color)) changed = true;
						ledManager.setLEDColor(x, y, z, color);
					}
				}
			}
		}
		return changed;
	}

	public void preProcess() {
		ledSelections = getLEDSelection();
		computeLEDHighlight();
	}

	public boolean processKeyboardEvent() {
		if (Keyboard.getEventKeyState()) {
			//if (Keyboard.getEventKey() == Keyboard.KEY_F11) setFullscreen(!fullscreen);
		}
		return true;
	}

	public boolean processMouseEvent() {
		return true;
	}

	public boolean processControllerEvent(Controller controller) {
		return true;
	}

	public boolean processVRInputEvent(VRInputEvent event) {
		return true;
	}

	public void update(float delta) {
		if (LEDCubeManager.getInstance().isVrMode()) {
			for (ControllerType type : ControllerType.values()) {
				VRTrackedController controller = VRProvider.getController(type);
				if (controller.getType() == ControllerType.LEFT) {
					controller.setScrolling(false);
					if (controller.isButtonPressed(ButtonType.TOUCHPAD)) {
						controller.setScrolling(true);
						float lightBrightness = (controller.getAxis(AxisType.TOUCHPAD).getY() + 1) / 2;
						LEDCubeManager.getInstance().getLightingHandler().getLight(1).brightness = lightBrightness * 0.75F;
						LEDCubeManager.getInstance().getLightingHandler().getLight(2).brightness = lightBrightness * 0.75F;
						controller.triggerHapticPulse(150);
					}
				}
			}
		}

		LEDCubeManager.addInfoText("Serial port: " + (commThread.isPortOpen() ? "open" : "closed"), 100);
		LEDCubeManager.addInfoText("TCP clients: " + commThread.getNumTCPClients(), 110);
		if (!spectrumAnalyzer.getCurrentTrack().isEmpty()) {
			LEDCubeManager.addInfoText("Music: " + spectrumAnalyzer.getCurrentTrack(), 120);
			LEDCubeManager.addInfoText("Music time: " + spectrumAnalyzer.getPositionMillis(), 130);
		}
		if (commThread.getCurrentSequence() != null) LEDCubeManager.addInfoText("Sequence: " + commThread.getCurrentSequence().getName(), 140);
		if (commThread.isFrozen()) LEDCubeManager.addInfoText("Animation Frozen", 150);
		if (ledManager.getResolution() < 255) LEDCubeManager.addInfoText("Color mode: " + (trueColor ? "true" : "full"), 160);
		if (ledSelections.isEmpty()) {
			LEDCubeManager.addInfoText("Hovered LED: none", 900);
		} else {
			for (LEDSelection selection : ledSelections) {
				StringBuilder sb = new StringBuilder("Hovered LED: ");
				Vector3 vector = selection.position;
				Color color = ledManager.getLEDColor((int)vector.getX(), (int)vector.getY(), (int)vector.getZ());
				sb.append((int)vector.getX()).append(", ").append((int)vector.getY()).append(", ").append((int)vector.getZ());
				sb.append(" (").append(color.getRed()).append(", ").append(color.getGreen()).append(", ").append(color.getBlue()).append(')');
				LEDCubeManager.addInfoText(sb.toString(), 900);
			}
		}
	}

	public void render() {
		float mult = spaceMult;

		Dimension3D dim = ledManager.getDimensions();
		LEDArray ledArray = previewTransform ? ledManager.getLEDArray().getTransformed() : ledManager.getLEDArray();
		PooledMutableVector3 pos = PooledMutableVector3.get();
		for (int y = 0; y < dim.y; y++) {
			for (int z = 0; z < dim.z; z++) {
				for (int x = 0; x < dim.x; x++) {
					int index = ledManager.encodeVector(x, y, z);
					if (isLEDWithinIsolation(x, y, z)) {
						Color color;
						if (trueColor) {
							Color ledColor = ledArray.getLEDColorReal(x, y, z);
							color = new Color(Math.round(ledColor.getRed() * ledManager.getFactor()), Math.round(ledColor.getGreen() * ledManager.getFactor()), Math.round(ledColor.getBlue() * ledManager.getFactor()));
						} else color = ledArray.getLEDColor(x, y, z);
						if (instanceItems[index] == null) {
							pos.set(x * mult, y * mult, z * mult);
							instanceItems[index] = model.render(Util.transformVector(pos, renderTransform, false), new Quaternion(), color, renderScale);
						} else {
							float distance = LEDCubeManager.getCamera().getPosition().distanceSquared(instanceItems[index].getPosition());
							if (model.getMeshByDistanceSquared(distance - model.getMesh(0).getRadius()) != instanceItems[index].getMesh()) {
								InstancedRenderer.removeItem(instanceItems[index]);
								pos.set(x * mult, y * mult, z * mult);
								instanceItems[index] = model.render(Util.transformVector(pos, renderTransform, false), new Quaternion(), color, renderScale);
							} else {
								instanceItems[index].setColor(color);
								instanceItems[index].setScale(renderScale);
							}
						}
					} else if (instanceItems[index] != null) {
						InstancedRenderer.removeItem(instanceItems[index]);
						instanceItems[index] = null;
					}
				}
			}
		}

		if (ledSelections.size() < 1) {
			for (int i = 0; i < highlightInstanceItems.length; i++) {
				if (highlightInstanceItems[i] != null) {
					InstancedRenderer.removeItem(highlightInstanceItems[i]);
					highlightInstanceItems[i] = null;
				}
			}
		} else {
			Vector3 scale = new Vector3(1.2F, 1.2F, 1.2F).multiply(renderScale);
			Color color = new Color(paintColor.getRed(), paintColor.getGreen(), paintColor.getBlue(), 32);
			for (int y = 0; y < dim.y; y++) {
				for (int x = 0; x < dim.x; x++) {
					for (int z = 0; z < dim.z; z++) {
						int index = ledManager.encodeVector(x, y, z);
						for (LEDSelection selection : ledSelections) {
							if (selection.highlight[ledManager.encodeVector(x, y, z)] && isLEDWithinIsolation(x, y, z)) {
								if (highlightInstanceItems[index] == null) {
									pos.set(x * mult, y * mult, z * mult);
									highlightInstanceItems[index] = model.render(Util.transformVector(pos, renderTransform, false), new Quaternion(), color, scale);
								} else {
									float distance = LEDCubeManager.getCamera().getPosition().distanceSquared(highlightInstanceItems[index].getPosition());
									if (model.getMeshByDistanceSquared(distance - model.getMesh(0).getRadius()) != highlightInstanceItems[index].getMesh()) {
										InstancedRenderer.removeItem(highlightInstanceItems[index]);
										pos.set(x * mult, y * mult, z * mult);
										highlightInstanceItems[index] = model.render(Util.transformVector(pos, renderTransform, false), new Quaternion(), color, scale);
									} else {
										highlightInstanceItems[index].setColor(color);
										highlightInstanceItems[index].setScale(renderScale);
									}
								}
								break;
							} else if (highlightInstanceItems[index] != null) {
								InstancedRenderer.removeItem(highlightInstanceItems[index]);
								highlightInstanceItems[index] = null;
							}
						}
					}
				}
			}
		}
		pos.release();
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
		LEDCubeManager.getCamera().setPosition(new Vector3(-0.8F, 0.85F, 0.28F));
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
		if (!LEDCubeManager.getInstance().isVrMode()) {
			InputBindingManager.addBinding(new InputBinding("resetcamera", "Reset Position", "Camera", true, new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.KEY_F)) {
				@Override
				public boolean onPressed() {
					resetCameraPosition();
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
			public void whilePressed() {
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
			public void whilePressed() {
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
			public void whilePressed() {
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
			public void whilePressed() {
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
			public void whilePressed() {
			}

			@Override
			public boolean onReleased() {
				return true;
			}
		});
		InputBindingManager.addBinding(new InputBinding("paintleds", "Paint LEDs", "Cube", true, new InputInfo(InputInfo.Type.MOUSE, 0)) {
			@Override
			public boolean onPressed() {
				return Mouse.isGrabbed();
			}

			@Override
			public void whilePressed() {
				ledSelections.forEach(selection -> {
					if (selection.selector == null) {
						paintLEDHighlight(selection.highlight, paintColor);
					}
				});
			}

			@Override
			public boolean onReleased() {
				return true;
			}
		});
		InputBindingManager.addBinding(new InputBinding("eraseleds", "Erase LEDs", "Cube", true, new InputInfo(InputInfo.Type.MOUSE, 2)) {
			@Override
			public boolean onPressed() {
				return Mouse.isGrabbed();
			}

			@Override
			public void whilePressed() {
				ledSelections.forEach(selection -> {
					if (selection.selector == null) {
						paintLEDHighlight(selection.highlight, ReadableColor.BLACK);
					}
				});
			}

			@Override
			public boolean onReleased() {
				return true;
			}
		});
		InputBindingManager.addBinding(new InputBinding("floodfill", "Flood Fill", "Cube", true, new InputInfo(InputInfo.Type.MOUSE, 1)) {
			@Override
			public boolean onPressed() {
				if (!Mouse.isGrabbed()) {
					for (LEDSelection selection : ledSelections) {
						if (selection.selector == null) {
							floodFill(selection);
							return false;
						}
					}
				}
				return true;
			}

			@Override
			public void whilePressed() {
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
			public void whilePressed() {
			}

			@Override
			public boolean onReleased() {
				return true;
			}
		});
		if (LEDCubeManager.getInstance().isVrMode()) {
			InputBindingManager.addBinding(new InputBinding("vrreloadanimation", "Reload Animation", "VR", true, new InputInfo(InputInfo.Type.VR, ButtonType.MENU.ordinal(), ControllerType.RIGHT)) {
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
				public void whilePressed() {
				}

				@Override
				public boolean onReleased() {
					return true;
				}
			});
			InputBindingManager.addBinding(new InputBinding("vrpaintleds1", "Paint LEDs 1", "VR", true, new InputInfo(InputInfo.Type.VR, ButtonType.TRIGGER.ordinal(), ControllerType.LEFT)) {
				@Override
				public boolean onPressed() {
					return false;
				}

				@Override
				public void whilePressed() {
					ledSelections.forEach(selection -> {
						VRTrackedController controller = VRProvider.getController(getBind().getVrControllerType());
						if (selection.selector == controller) {
							if (paintLEDHighlight(selection.highlight, paintColor))
								controller.triggerHapticPulse(1000);
						}
					});
				}

				@Override
				public boolean onReleased() {
					return true;
				}
			});
			InputBindingManager.addBinding(new InputBinding("vrpaintleds2", "Paint LEDs 2", "VR", true, new InputInfo(InputInfo.Type.VR, ButtonType.TRIGGER.ordinal(), ControllerType.RIGHT)) {
				@Override
				public boolean onPressed() {
					return false;
				}

				@Override
				public void whilePressed() {
					ledSelections.forEach(selection -> {
						VRTrackedController controller = VRProvider.getController(getBind().getVrControllerType());
						if (selection.selector == controller) {
							if (paintLEDHighlight(selection.highlight, paintColor))
								controller.triggerHapticPulse(1000);
						}
					});
				}

				@Override
				public boolean onReleased() {
					return true;
				}
			});
			InputBindingManager.addBinding(new InputBinding("vreraseleds1", "Erase LEDs 1", "VR", true, new InputInfo(InputInfo.Type.VR, ButtonType.GRIP.ordinal(), ControllerType.LEFT)) {
				@Override
				public boolean onPressed() {
					return false;
				}

				@Override
				public void whilePressed() {
					ledSelections.forEach(selection -> {
						VRTrackedController controller = VRProvider.getController(getBind().getVrControllerType());
						if (selection.selector == controller) {
							if (paintLEDHighlight(selection.highlight, ReadableColor.BLACK))
								controller.triggerHapticPulse(1000);
						}
					});
				}

				@Override
				public boolean onReleased() {
					return true;
				}
			});
			InputBindingManager.addBinding(new InputBinding("vreraseleds2", "Erase LEDs 2", "VR", true, new InputInfo(InputInfo.Type.VR, ButtonType.GRIP.ordinal(), ControllerType.RIGHT)) {
				@Override
				public boolean onPressed() {
					return false;
				}

				@Override
				public void whilePressed() {
					ledSelections.forEach(selection -> {
						VRTrackedController controller = VRProvider.getController(getBind().getVrControllerType());
						if (selection.selector == controller) {
							if (paintLEDHighlight(selection.highlight, ReadableColor.BLACK))
								controller.triggerHapticPulse(1000);
						}
					});
				}

				@Override
				public boolean onReleased() {
					return true;
				}
			});
			InputBindingManager.addBinding(new InputBinding("vrtogglegui", "Toggle GUI", "VR", true, new InputInfo(InputInfo.Type.VR, ButtonType.MENU.ordinal(), ControllerType.LEFT)) {
				@Override
				public boolean onPressed() {
					LEDCubeManager.getInstance().setShowingVRGUI(!LEDCubeManager.getInstance().isShowingVRGUI());
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
			InputBindingManager.addBinding(new InputBinding("vrclearleds", "Clear LEDs", "VR", true, new InputInfo(InputInfo.Type.VR, ButtonType.TOUCHPAD_U.ordinal(), ControllerType.RIGHT)) {
				@Override
				public boolean onPressed() {
					LEDUtil.clear(ledManager);
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
			InputBindingManager.addBinding(new InputBinding("vrfloodfill", "Flood Fill", "VR", true, new InputInfo(InputInfo.Type.VR, ButtonType.TOUCHPAD_D.ordinal(), ControllerType.RIGHT)) {
				@Override
				public boolean onPressed() {
					for (LEDSelection selection : ledSelections) {
						if (selection.selector == VRProvider.getController(getBind().getVrControllerType())) {
							floodFill(selection);
							return false;
						}
					}
					return true;
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

	private void initOctree() {
		Dimension3D dim = ledManager.getDimensions();
		int maxDim = Util.getNextPowerOfTwo(Math.max(dim.x, Math.max(dim.y, dim.z)));
		float octreeSize = spaceMult * maxDim;
		ArrayList<LEDCubeOctreeNode> list = new ArrayList<>();
		LEDCubeOctreeNode octree = new LEDCubeOctreeNode(new AxisAlignedBB(new Vector3(-spaceMult / 2, -spaceMult / 2, -spaceMult / 2), new Vector3(octreeSize + (spaceMult / 2), octreeSize + (spaceMult / 2), octreeSize + (spaceMult / 2))));
		recursiveFillOctree(octree, octreeSize / 2, maxDim, new Vector3());
		list.add(octree);
		list.toArray(octrees = new LEDCubeOctreeNode[list.size()]);
		LogHelper.info("LED lookup octree created");
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
				LogHelper.finest("Filling octree node at " + (int)nextLedPos.getX() + "," + (int)nextLedPos.getY() + "," + (int)nextLedPos.getZ() + " on level " + (int)MathHelper.log2(count));
				recursiveFillOctree(newNode, size / 2, count / 2, nextLedPos);
			} else {
				AxisAlignedBB modelAABB = model.getAABB();
				node.setNode(i, new LEDCubeOctreeNode(new AxisAlignedBB(modelAABB.getMinPoint().add(ledPos.multiply(spaceMult)), modelAABB.getMaxPoint().add(ledPos.multiply(spaceMult))), ledPos));
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

	private Vector3 recursiveIntersectOctree(LEDCubeOctreeNode node, AxisAlignedBB aabb) {
		if (node.getLEDPosition() != null) {
			return node.getLEDPosition();
		}
		for (int i = 0; i < 8; i++) {
			LEDCubeOctreeNode nextNode = node.getNode(i);
			if (nextNode != null && nextNode.getAABB().intersects(aabb)) {
				Vector3 ret = recursiveIntersectOctree(nextNode, aabb);
				if (ret != null) return ret;
			}
		}
		return null;
	}

	public List<LEDSelection> getLEDSelection() {
		List<LEDSelection> list = new ArrayList<>();
		if (LEDCubeManager.getInstance().isVrMode()) {
			float area = 0.02F;
			for (ControllerType type : ControllerType.values()) {
				VRTrackedController controller = VRProvider.getController(type);
				if (controller.isTracking()) {
					Quaternion rot = controller.getRotation().inverse();
					Vector3 donutPos = controller.getPosition().add(rot.forward().multiply(0.014F)).add(rot.up().negate().multiply(0.035F));
					Vector3 position = Util.transformVector(donutPos, Matrix4f.invert(renderTransform, null), false);
					Vector3 vec = getLEDIntersecting(new AxisAlignedBB(position.subtract(area), position.add(area)));
					if (vec != null) list.add(new LEDSelection(controller, vec));
				}
			}
		}
		Vector3 vec = traceRayToLED(LEDCubeManager.getInstance().getCursorRay());
		if (vec != null) list.add(new LEDSelection(null, vec));
		return list;
	}

	public Vector3 traceRayToLED(Vector3[] ray) {
		Vector3 position = Util.transformVector(ray[0], Matrix4f.invert(renderTransform, null), false);
		Vector3 direction = ray[1].multiply(0.005F);
		PooledMutableVector3 rayPos = PooledMutableVector3.get();
		PooledMutableVector3 rayDir = PooledMutableVector3.get();

		for (float step = 1; step < 5000; step++) {
			rayPos.set(position).add(rayDir.set(direction).multiply(step));
			Vector3 ledPos = getLEDAtPosition(rayPos);
			if (ledPos != null) {
				rayPos.release();
				rayDir.release();
				return ledPos;
			}
		}
		rayPos.release();
		rayDir.release();
		return null;
	}

	public Vector3 getLEDAtPosition(Vector3 position) {
		Dimension3D dim = ledManager.getDimensions();
		if (octrees == null) {
			PooledMutableVector3 pos = PooledMutableVector3.get();
			for (int y = 0; y < dim.y; y++) {
				for (int z = 0; z < dim.z; z++) {
					for (int x = 0; x < dim.x; x++) {
						float xx = x * spaceMult;
						float yy = y * spaceMult;
						float zz = z * spaceMult;
						pos.set(xx, yy, zz);
						if (model.getAABB().offset(pos).containsPoint(position) && isLEDWithinIsolation(x, y, z)) {
							pos.release();
							return new Vector3(x, y, z);
						}
					}
				}
			}
			pos.release();
		} else {
			Vector3 ret = null;
			for (int i = 0; i < octrees.length; i++) {
				ret = recursiveIntersectOctree(octrees[i], position);
				if (ret != null) break;
			}
			if (ret != null) {
				if (isLEDWithinIsolation((int)ret.getX(), (int)ret.getY(), (int)ret.getZ())) {
					return ret;
				}
			}
		}
		return null;
	}

	public Vector3 getLEDIntersecting(AxisAlignedBB aabb) {
		Dimension3D dim = ledManager.getDimensions();
		if (octrees == null) {
			PooledMutableVector3 pos = PooledMutableVector3.get();
			for (int y = 0; y < dim.y; y++) {
				for (int z = 0; z < dim.z; z++) {
					for (int x = 0; x < dim.x; x++) {
						float xx = x * spaceMult;
						float yy = y * spaceMult;
						float zz = z * spaceMult;
						pos.set(xx, yy, zz);
						if (model.getAABB().offset(pos).intersects(aabb) && isLEDWithinIsolation(x, y, z)) {
							pos.release();
							return new Vector3(x, y, z);
						}
					}
				}
			}
			pos.release();
		} else {
			Vector3 ret = null;
			for (int i = 0; i < octrees.length; i++) {
				ret = recursiveIntersectOctree(octrees[i], aabb);
				if (ret != null) break;
			}
			if (ret != null) {
				if (isLEDWithinIsolation((int)ret.getX(), (int)ret.getY(), (int)ret.getZ())) {
					return ret;
				}
			}
		}
		return null;
	}

	private void floodFill(LEDSelection selection) {
		Dimension3D dim = ledManager.getDimensions();
		LEDArray ledArray = ledManager.getLEDArray();
		Vector3 vector = selection.position;
		Color targetColor = ledArray.getLEDColor((int)vector.getX(), (int)vector.getY(), (int)vector.getZ());
		if (!targetColor.equals(paintColor)) {
			boolean[] processed = new boolean[ledManager.getLEDCount()];
			LinkedList<Vector3> stack = new LinkedList<>();
			stack.push(vector);
			while (!stack.isEmpty()) {
				Vector3 current = stack.pop();
				Color color = ledArray.getLEDColor((int)current.getX(), (int)current.getY(), (int)current.getZ());
				if (color.equals(targetColor) && isLEDWithinIsolation(current)) {
					ledManager.setLEDColor((int)current.getX(), (int)current.getY(), (int)current.getZ(), paintColor);
					processed[ledManager.encodeVector(current)] = true;
					for (int i = 0; i < 6; i++) {
						Vector3 offset = Direction.values()[i].getVector();
						Vector3 node = current.add(offset);
						if (node.getX() >= 0 && node.getX() < dim.x && node.getY() >= 0 && node.getY() < dim.y && node.getZ() >= 0 && node.getZ() < dim.z && !processed[ledManager.encodeVector(node)]) {
							stack.push(node);
						}
					}
				}
			}
		}
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

	public Vector3 applyTransform(Vector3 vec) {
		Dimension3D dim = ledManager.getDimensions();
		PooledMutableVector3 vector = PooledMutableVector3.get(vec);
		if (reflectX) vector.setX((dim.x - 1) - vector.getX());
		if (reflectY) vector.setY((dim.y - 1) - vector.getY());
		if (reflectZ) vector.setZ((dim.z - 1) - vector.getZ());
		Vector4f vec4 = new Vector4f(vector.getX(), vector.getY(), vector.getZ(), 1);
		vector.release();
		Matrix4f.transform(transform, vec4, vec4);
		return new Vector3(Math.round(vec4.x), Math.round(vec4.y), Math.round(vec4.z));
	}

	public MutableVector3 applyTransform(MutableVector3 vector) {
		Dimension3D dim = ledManager.getDimensions();
		if (reflectX) vector.setX((dim.x - 1) - vector.getX());
		if (reflectY) vector.setY((dim.y - 1) - vector.getY());
		if (reflectZ) vector.setZ((dim.z - 1) - vector.getZ());
		Vector4f vec4 = new Vector4f(vector.getX(), vector.getY(), vector.getZ(), 1);
		Matrix4f.transform(transform, vec4, vec4);
		vector.set(Math.round(vec4.x), Math.round(vec4.y), Math.round(vec4.z));
		return vector;
	}

	public void setRenderOffset(Vector3 offset) {
		renderOffset = offset;
		updateRenderTransform();
	}

	public void setRenderScale(Vector3 scale) {
		renderScale = scale;
		updateRenderTransform();
	}

	private void updateRenderTransform() {
		renderTransform.setIdentity();
		Vector3 center = Util.convertVector(centerPoint).multiply(spaceMult);
		renderTransform.translate(Util.convertVector(renderOffset.add(center)));
		renderTransform.scale(Util.convertVector(renderScale));
		renderTransform.translate(Util.convertVector(center.negate()));
		for (int i = 0; i < instanceItems.length; i++) {
			InstancedRenderer.InstanceItem item = instanceItems[i];
			if (item != null) {
				item.setTransform(Util.transformVector(ledManager.decodeVector(i), renderTransform, false), new Quaternion());
			}
			InstancedRenderer.InstanceItem highlightItem = highlightInstanceItems[i];
			if (highlightItem != null) {
				highlightItem.setTransform(Util.transformVector(ledManager.decodeVector(i), renderTransform, false), new Quaternion());
			}
		}
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

	private class LEDSelection {
		public Object selector;
		public Vector3 position;
		public boolean[] highlight;

		public LEDSelection(Object selector, Vector3 position) {
			this.selector = selector;
			this.position = position;
			this.highlight = new boolean[ledManager.getLEDCount()];
		}
	}
}
