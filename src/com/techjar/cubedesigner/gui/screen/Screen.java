
package com.techjar.cubedesigner.gui.screen;

import com.techjar.cubedesigner.CubeDesigner;
import com.techjar.cubedesigner.gui.GUIBox;
import com.techjar.cubedesigner.gui.GUICallback;
import org.lwjgl.input.Controller;

/**
 *
 * @author Techjar
 */
public abstract class Screen {
    protected GUIBox container;
    protected GUICallback resizeHandler;
    protected boolean visible = true;
    protected boolean enabled = true;
    protected boolean removeRequested;

    public Screen() {
        container = new GUIBox();
        container.setDimension(CubeDesigner.getWidth(), CubeDesigner.getHeight());
        CubeDesigner.getInstance().addResizeHandler(resizeHandler = new GUICallback() {
            @Override
            public void run() {
                container.setDimension(CubeDesigner.getWidth(), CubeDesigner.getHeight());
            }
        });
    }

    public boolean isRemoveRequested() {
        return removeRequested;
    }

    public GUIBox getContainer() {
        return container;
    }

    public boolean processKeyboardEvent() {
        return container.processKeyboardEvent();
    }

    public boolean processMouseEvent() {
        return container.processMouseEvent();
    }

    public boolean processControllerEvent(Controller controller) {
        return container.processControllerEvent(controller);
    }

    public void update(float delta) {
        container.update(delta);
    }

    public void render() {
        container.render();
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

    public void remove() {
        CubeDesigner.getInstance().removeResizeHandler(resizeHandler);
        removeRequested = true;
    }
}
