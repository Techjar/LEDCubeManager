package com.techjar.ledcm.gui;

import static org.lwjgl.opengl.GL11.*;

import com.techjar.ledcm.render.RenderHelper;
import com.techjar.ledcm.vr.VRInputEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.input.Controller;
import org.lwjgl.util.Color;
import org.newdawn.slick.geom.Rectangle;

/**
 *
 * @author Techjar
 */
public abstract class GUIContainer extends GUI {
	protected List<GUI> components;

	public GUIContainer() {
		components = new ArrayList<>();
	}

	@Override
	protected boolean keyboardEvent(int key, boolean state, char character) {
		for (GUI gui : components) {
			if (gui.isVisible() && gui.isEnabled() && !gui.keyboardEvent(key, state, character)) return false;
		}
		return true;
	}

	@Override
	protected boolean mouseEvent(int button, boolean state, int dwheel) {
		for (GUI gui : components) {
			if (gui.isVisible() && gui.isEnabled() && !gui.mouseEvent(button, state, dwheel)) {
				closeComboBoxesRecursive(this, gui);
				return false;
			}
		}
		return true;
	}

	@Override
    public boolean processControllerEvent(Controller controller) {
        for (GUI gui : components) {
            if (gui.isVisible() && gui.isEnabled() && !gui.processControllerEvent(controller)) return false;
        }
        return true;
    }

	@Override
    public boolean processVRInputEvent(VRInputEvent event) {
        for (GUI gui : components) {
            if (gui.isVisible() && gui.isEnabled() && !gui.processVRInputEvent(event)) return false;
        }
        return true;
    }

	@Override
	public void update(float delta) {
		GUIWindow lastWin = null, lastTopWin = null;
		List<GUI> toAdd = new ArrayList<>();
		Iterator<GUI> it = components.iterator();
		while (it.hasNext()) {
			GUI gui = it.next();
			if (gui.isRemoveRequested()) it.remove();
			else {
				if (gui.isVisible() && gui.isEnabled()) {
					gui.update(delta);
					if (gui.isRemoveRequested()) it.remove();
					else if (gui instanceof GUIWindow) {
						GUIWindow win = (GUIWindow)gui;
						if (lastWin != null && lastWin != lastTopWin) lastWin.onTop = false;
						lastWin = win;
						if (win.isToBePutOnTop()) {
							it.remove();
							toAdd.add(gui);
							win.onTop = true;
							win.setToBePutOnTop(false);
							if (lastTopWin != null) lastTopWin.onTop = false;
							lastTopWin = win;
						}
					} else if (gui instanceof GUIComboBox) {
						if (((GUIComboBox)gui).isOpened() && components.get(components.size() - 1) != gui) {
							it.remove();
							toAdd.add(gui);
						}
					}
				}
			}
		}
		components.addAll(toAdd);
	}

	@Override
	public void render() {
		RenderHelper.beginScissor(getScissorBox());
		Rectangle containerBox = getContainerBox();
		glPushAttrib(GL_STENCIL_BUFFER_BIT);
		glStencilMask(0xFF);
		glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
		glEnable(GL_STENCIL_TEST);
		for (GUI gui : components) {
			if (gui.isVisible() && gui.getComponentBox().intersects(containerBox)) {
				glClear(GL_STENCIL_BUFFER_BIT);
				glStencilFunc(GL_ALWAYS, 0xFF, 0xFF);
				gui.render();
				glStencilFunc(GL_EQUAL, 0xFF, 1);
				if (!gui.isEnabled())
					RenderHelper.drawSquare(containerBox.getX(), containerBox.getY(), containerBox.getWidth(), containerBox.getHeight(), new Color(40, 40, 40, 150));
			}
		}
		glPopAttrib();
		RenderHelper.endScissor();
	}

	@Override
	public void remove() {
		this.removeAllComponents();
		super.remove();
	}

	@Override
	public Rectangle getContainerBox() {
		return new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight());
	}

	protected Rectangle getScissorBox() {
		return getContainerBox();
	}

	protected void closeComboBoxesRecursive(GUIContainer container, GUI triggered) {
		for (GUI gui : container.components) {
			if (gui == triggered) continue;
			if (gui instanceof GUIComboBox) {
				((GUIComboBox)gui).setOpened(false);
			} else if (gui instanceof GUIContainer) {
				closeComboBoxesRecursive((GUIContainer)gui, triggered);
			}
		}
	}

	public boolean containsComponent(GUI component) {
		return components.contains(component);
	}

	public GUI getComponent(int index) {
		return components.get(index);
	}

	public List<GUI> getAllComponents() {
		return Collections.unmodifiableList(components);
	}

	public void addComponent(GUI component) {
		components.add(component);
		component.setParent(this);
	}

	public void removeComponent(GUI component) {
		components.remove(component);
		component.setParent(null);
	}

	public GUI removeComponent(int index) {
		GUI component = components.get(index);
		removeComponent(component);
		return component;
	}

	public void removeAllComponents() {
		Iterator<GUI> it = components.iterator();
		while (it.hasNext()) {
			GUI gui = it.next();
			gui.setParent(null);
			it.remove();
		}
	}

	public List<GUI> findComponentsByName(String name) {
		ArrayList<GUI> list = new ArrayList<>();
		for (GUI gui : components) {
			if (name.equals(gui.getName())) {
				list.add(gui);
			}
		}
		return list;
	}
}
