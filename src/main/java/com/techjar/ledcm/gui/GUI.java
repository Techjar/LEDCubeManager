package com.techjar.ledcm.gui;

import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.math.Vector2;
import com.techjar.ledcm.vr.VRInputEvent;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Controller;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Dimension;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;

/**
 *
 * @author Techjar
 */
public abstract class GUI {
	protected Vector2 position = new Vector2();
	protected Vector2 relativePosition = new Vector2();
	protected Vector2 cachedParentPos = null;
	protected Dimension dimension = new Dimension();
	protected Rectangle baseBox = new Rectangle(0, 0, 0, 0);
	protected GUICallback dimensionChangeHandler;
	protected GUICallback positionChangeHandler;
	protected GUICallback removeHandler;
	protected GUIAlignment parentAlign = GUIAlignment.TOP_LEFT;
	protected GUI parent;
	protected String name;
	protected boolean visible = true;
	protected boolean enabled = true;
	protected boolean removeRequested;
	protected boolean hovered;
	protected static boolean[] keyState = new boolean[256];
	protected static boolean[] mouseState = new boolean[Mouse.getButtonCount()];

	protected abstract boolean keyboardEvent(int key, boolean state, char character);
	protected abstract boolean mouseEvent(int button, boolean state, int dwheel);
	public abstract void update(float delta);
	public abstract void render();

	public static boolean doKeyboardEvent(GUI gui, int key, boolean state, char character) {
		if (key >= 0 && key < keyState.length) keyState[key] = state;
		return gui.keyboardEvent(key, state, character);
	}

	public static boolean doMouseEvent(GUI gui, int button, boolean state, int dwheel) {
		if (button >= 0 && button < mouseState.length) mouseState[button] = state;
		return gui.mouseEvent(button, state, dwheel);
	}

	public final boolean processKeyboardEvent() {
		return doKeyboardEvent(this, Keyboard.getEventKey(), Keyboard.getEventKeyState(), Keyboard.getEventCharacter());
	}

	public final boolean processMouseEvent() {
		return doMouseEvent(this, Mouse.getEventButton(), Mouse.getEventButtonState(), Mouse.getEventDWheel());
	}

	public boolean processControllerEvent(Controller controller) {
		return true;
	}

	public boolean processVRInputEvent(VRInputEvent event) {
		return true;
	}

	/**
	 * Returns the position of this component relative to it's parent.
	 * For the non-relative position, use {@link #getRawPosition}.
	 *
	 * @return The position of this component as a {@link Vector2}
	 */
	protected Vector2 getPosition() {
		if (parent != null) {
			boolean realParent = this instanceof GUIBackground || (this instanceof GUIButton && ((GUIButton)this).windowClose);
			Vector2 parentPos = realParent ? parent.getPosition() : parent.getContainerPosition();
			if (!parentPos.equals(cachedParentPos)) {
				Dimension parentDim = realParent ? parent.getDimension() : parent.getContainerDimension();
				switch (parentAlign) {
					case TOP_LEFT:
						relativePosition = position.add(parentPos);
						break;
					case TOP_RIGHT:
						relativePosition = new Vector2(position.getX() + parentPos.getX() + parentDim.getWidth() - dimension.getWidth(), position.getY() + parentPos.getY());
						break;
					case BOTTOM_LEFT:
						relativePosition = new Vector2(position.getX() + parentPos.getX(), position.getY() + parentPos.getY() + parentDim.getHeight() - dimension.getHeight());
						break;
					case BOTTOM_RIGHT:
						relativePosition = new Vector2(position.getX() + parentPos.getX() + parentDim.getWidth() - dimension.getWidth(), position.getY() + parentPos.getY() + parentDim.getHeight() - dimension.getHeight());
						break;
					case TOP_CENTER:
						relativePosition = new Vector2(position.getX() + parentPos.getX() + (parentDim.getWidth() / 2) - (dimension.getWidth() / 2), position.getY() + parentPos.getY());
						break;
					case BOTTOM_CENTER:
						relativePosition = new Vector2(position.getX() + parentPos.getX() + (parentDim.getWidth() / 2) - (dimension.getWidth() / 2), position.getY() + parentPos.getY() + parentDim.getHeight() - dimension.getHeight());
						break;
					case LEFT_CENTER:
						relativePosition = new Vector2(position.getX() + parentPos.getX(), position.getY() + parentPos.getY() + (parentDim.getHeight() / 2) - (dimension.getHeight() / 2));
						break;
					case RIGHT_CENTER:
						relativePosition = new Vector2(position.getX() + parentPos.getX() + parentDim.getWidth() - dimension.getWidth(), position.getY() + parentPos.getY() + (parentDim.getHeight() / 2) - (dimension.getHeight() / 2));
						break;
					case CENTER:
						relativePosition = new Vector2(position.getX() + parentPos.getX() + (parentDim.getWidth() / 2) - (dimension.getWidth() / 2), position.getY() + parentPos.getY() + (parentDim.getHeight() / 2) - (dimension.getHeight() / 2));
						break;
					default:
						throw new RuntimeException("Illegal value for parentAlign (this should never happen!)");
				}
				cachedParentPos = parentPos.copy();
			}
			return relativePosition;
		}
		return position;
	}

	public Vector2 getContainerPosition() {
		return getPosition();
	}

	/**
	 * Returns the position of this component as set by {@link #setPosition}.
	 * For the non-relative position, use {@link #getPosition}.
	 *
	 * @return The position of this component as a {@link Vector2}
	 */
	public Vector2 getRawPosition() {
		return position;
	}

	public void setPosition(Vector2 position) {
		this.position = position;
		this.cachedParentPos = null;
		if (positionChangeHandler != null) {
			positionChangeHandler.run(this);
		}
	}

	public void setPosition(float x, float y) {
		setPosition(new Vector2(x, y));
	}

	public float getX() {
		return getPosition().getX();
	}

	public void setX(float x) {
		setPosition(x, position.getY());
	}

	public float getY() {
		return getPosition().getY();
	}

	public void setY(float y) {
		setPosition(position.getX(), y);
	}

	public Dimension getDimension() {
		return new Dimension(dimension);
	}

	public Dimension getContainerDimension() {
		return getDimension();
	}

	public void setDimension(Dimension dimension) {
		this.dimension.setSize(dimension);
		if (dimensionChangeHandler != null) {
			dimensionChangeHandler.run(this);
		}
	}

	public void setDimension(int width, int height) {
		setDimension(new Dimension(width, height));
	}

	public int getWidth() {
		return dimension.getWidth();
	}

	public void setWidth(int width) {
		setDimension(width, dimension.getHeight());
	}

	public int getHeight() {
		return dimension.getHeight();
	}

	public void setHeight(int height) {
		setDimension(dimension.getWidth(), height);
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public GUICallback getPositionChangeHandler() {
		return positionChangeHandler;
	}

	public void setPositionChangeHandler(GUICallback positionChangeHandler) {
		this.positionChangeHandler = positionChangeHandler;
	}

	public GUICallback getDimensionChangeHandler() {
		return dimensionChangeHandler;
	}

	public void setDimensionChangeHandler(GUICallback dimensionChangeHandler) {
		this.dimensionChangeHandler = dimensionChangeHandler;
	}

	public GUICallback getRemoveHandler() {
		return removeHandler;
	}

	public void setRemoveHandler(GUICallback removeHandler) {
		this.removeHandler = removeHandler;
	}

	public boolean checkMouseIntersect(boolean checkParentContainerBox, boolean checkContainerBox, Shape... boxes) {
		Shape mouseBox = Util.getMouseHitbox();
		boolean intersect1 = true, intersect2 = true;
		if (checkContainerBox) {
			Shape cBox = getContainerBox();
			if (cBox != null) intersect1 = cBox.intersects(mouseBox);
		}
		if (intersect1 && parent != null) intersect2 = parent.checkMouseIntersect(true, checkParentContainerBox);
		if (!intersect1 || !intersect2) return false;
		List<GUI> guiList = getContainerList();
		int thisIndex = guiList.indexOf(this);
		if (thisIndex > -1) {
			for (int i = 0; i < guiList.size(); i++) {
				if (i <= thisIndex) continue;
				GUI gui = guiList.get(i);
				if (gui.isVisible() && gui.getComponentBox().intersects(mouseBox)) return false;
			}
		}
		for (Shape box : boxes) {
			if (!box.intersects(mouseBox)) return false;
		}
		return true;
	}

	public boolean checkMouseIntersect(boolean checkParentContainerBox, Shape... boxes) {
		return checkMouseIntersect(checkParentContainerBox, false, boxes);
	}

	public boolean checkMouseIntersect(Shape... boxes) {
		return checkMouseIntersect(true, false, boxes);
	}

	public boolean checkWithinContainer(Shape... boxes) {
		if (parent == null || !(parent instanceof GUIContainer)) return true;
		if (boxes.length < 1) {
			return parent.getContainerBox().contains(getComponentBox());
		}
		for (Shape box : boxes) {
			if (!parent.getContainerBox().contains(box)) return false;
		}
		return true;
	}

	public List<GUI> getContainerList() {
		if (parent != null && parent instanceof GUIContainer) return ((GUIContainer)parent).getAllComponents();
		return new ArrayList<>();
	}

	public Rectangle getContainerBox() {
		return null;
	}

	public Shape getComponentBox() {
		Vector2 pos = getPosition();
		baseBox.setLocation(pos.getX(), pos.getY());
		baseBox.setSize(dimension.getWidth(), dimension.getHeight());
		return baseBox;
	}

	public GUI getParent() {
		return parent;
	}

	public void setParent(GUI parent) {
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GUIAlignment getParentAlignment() {
		return parentAlign;
	}

	public void setParentAlignment(GUIAlignment parentAlign) {
		this.parentAlign = parentAlign;
	}

	public void remove() {
		this.removeRequested = true;
		if (removeHandler != null) {
			removeHandler.run(this);
		}
	}

	public boolean isRemoveRequested() {
		return removeRequested;
	}
}
