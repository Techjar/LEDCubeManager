package com.techjar.ledcm.gui;

import static org.lwjgl.opengl.GL11.*;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.render.RenderHelper;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.vr.VRInputEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.input.Controller;
import org.lwjgl.util.Color;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.ShapeRenderer;

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
		glStencilFunc(GL_ALWAYS, 0xFF, 0xFF);
		glEnable(GL_STENCIL_TEST);
		glClear(GL_STENCIL_BUFFER_BIT);
		synchronized (components) {
			for (GUI gui : components) {
				if (gui.isVisible() && gui.getComponentBox().intersects(containerBox)) {
					if (gui.isEnabled()) glStencilMask(0x00);
					else glStencilMask(0x01);
					gui.render();
					if (LEDCubeManager.getInstance().debugGUI) {
						glStencilMask(0x00);
						glColor4f(1, 1, 1, 1);
						ShapeRenderer.draw(gui.getComponentBox());
						glEnable(GL_TEXTURE_2D);
					}
				}
			}
		}
		glStencilFunc(GL_EQUAL, 0xFF, 1);
		RenderHelper.drawSquare(containerBox.getX(), containerBox.getY(), containerBox.getWidth(), containerBox.getHeight(), new Color(40, 40, 40, 150));
		glPopAttrib();
		RenderHelper.endScissor();
		if (LEDCubeManager.getInstance().debugGUI) {
			glColor4f(1, 0, 0, 1);
			ShapeRenderer.draw(getContainerBox());
			glColor4f(0, 1, 0, 1);
			ShapeRenderer.draw(getScissorBox());
			if (RenderHelper.getPreviousScissor() != null) {
				glColor4f(0, 0, 1, 1);
				ShapeRenderer.draw(Util.clipRectangle(getScissorBox(), RenderHelper.getPreviousScissor()));
			}
			glEnable(GL_TEXTURE_2D);
		}
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
