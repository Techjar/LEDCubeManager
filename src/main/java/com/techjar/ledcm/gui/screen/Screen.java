
package com.techjar.ledcm.gui.screen;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.gui.GUIBox;
import com.techjar.ledcm.gui.GUICallback;
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
        container.setDimension(LEDCubeManager.getWidth(), LEDCubeManager.getHeight());
        LEDCubeManager.getInstance().addResizeHandler(resizeHandler = new GUICallback() {
            @Override
            public void run() {
                container.setDimension(LEDCubeManager.getWidth(), LEDCubeManager.getHeight());
                onResized();
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
        LEDCubeManager.getInstance().removeResizeHandler(resizeHandler);
        removeRequested = true;
    }

    protected void onResized() {
    }
}
