package com.techjar.ledcm.gui;

import com.techjar.ledcm.render.RenderHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.lwjgl.input.Controller;
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
    public boolean processKeyboardEvent() {
        for (GUI gui : components)
            if (gui.isVisible() && gui.isEnabled() && !gui.processKeyboardEvent()) return false;
        return true;
    }

    @Override
    public boolean processMouseEvent() {
        for (GUI gui : components)
            if (gui.isVisible() && gui.isEnabled() && !gui.processMouseEvent()) {
                closeComboBoxesRecursive(this, gui);
                return false;
            }
        return true;
    }

    @Override
    public boolean processControllerEvent(Controller controller) {
        for (GUI gui : components)
            if (gui.isVisible() && gui.isEnabled() && !gui.processControllerEvent(controller)) return false;
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
                        if (lastWin != null && lastWin != lastTopWin) lastWin.setOnTop(false);
                        lastWin = win;
                        if (win.isToBePutOnTop()) {
                            it.remove();
                            toAdd.add(gui);
                            win.setOnTop(true);
                            win.setToBePutOnTop(false);
                            if (lastTopWin != null) lastTopWin.setOnTop(false);
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
        for (GUI gui : components) {
            if (gui.isVisible() && gui.getComponentBox().intersects(containerBox)) gui.render();
        }
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
        Iterator it = components.iterator();
        while (it.hasNext()) {
            GUI gui = (GUI)it.next();
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
