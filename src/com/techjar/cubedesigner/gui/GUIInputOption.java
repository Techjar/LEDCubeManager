package com.techjar.cubedesigner.gui;

import com.techjar.cubedesigner.CubeDesigner;
import com.techjar.cubedesigner.util.Util;
import com.techjar.cubedesigner.util.InputInfo;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Color;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Rectangle;

/**
 *
 * @author Techjar
 */
public class GUIInputOption extends GUI {
    protected UnicodeFont font;
    protected Color color;
    protected GUIBackground guiBg;
    protected InputInfo button;
    protected GUICallback changeHandler;

    protected boolean assign;

    public GUIInputOption(UnicodeFont font, Color color, GUIBackground guiBg) {
        this.font = font;
        this.color = color;
        this.guiBg = guiBg;
    }

    public GUIInputOption(UnicodeFont font, Color color) {
        this(font, color, null);
    }

    @Override
    public boolean processKeyboardEvent() {
        if (assign && Keyboard.getEventKeyState()) {
            if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) button = null;
            else button = new InputInfo(InputInfo.Type.KEYBOARD, Keyboard.getEventKey());
            if (changeHandler != null) {
                changeHandler.setComponent(this);
                changeHandler.run();
            }
            assign = false;
            return false;
        }
        return true;
    }

    @Override
    public boolean processMouseEvent() {
        if (Mouse.getEventButtonState()) {
            if (assign) {
                setButton(InputInfo.Type.MOUSE, Mouse.getEventButton());
                assign = false;
                return false;
            }
            else if (Mouse.getEventButton() == 0) {
                Rectangle box = new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight());
                if (checkMouseIntersect(box)) {
                    assign = true;
                    return false;
                }
                else assign = false;
            }
        }
        return true;
    }

    @Override
    public boolean processControllerEvent(Controller controller) {
        if (assign && Controllers.isEventButton()) {
            setButton(InputInfo.Type.CONTROLLER, Controllers.getEventControlIndex());
            assign = false;
            return false;
        }
        return true;
    }

    @Override
    public void update(float delta) {
        if (!Mouse.isButtonDown(0) && !assign) {
            Rectangle box = new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight());
            if (checkMouseIntersect(box)) {
                if (!hovered) CubeDesigner.getSoundManager().playEffect("ui/rollover.wav", false);
                hovered = true;
            }
            else hovered = false;
        }
    }

    @Override
    public void render() {
        int posAdd = 0;
        if (guiBg != null) {
            guiBg.render();
            posAdd = guiBg.getBorderSize() + 2;
        }
        Color color2 = color;
        if (hovered || assign) color2 = Util.addColors(color2, new Color(50, 50, 50));
        font.drawString(getPosition().getX() + posAdd, getPosition().getY() + posAdd, assign ? "_" : (button == null ? "None" : button.getDisplayString()), Util.convertColor(color2));
    }

    public InputInfo getButton() {
        return button;
    }

    public void setButton(InputInfo button) {
        this.button = button;
        if (changeHandler != null) {
            changeHandler.setComponent(this);
            changeHandler.run();
        }
    }

    public void setButton(InputInfo.Type type, int button) {
        setButton(new InputInfo(type, button));
    }

    public void setButton(String button) {
        setButton(InputInfo.fromString(button));
    }

    public GUICallback getChangeHandler() {
        return changeHandler;
    }

    public void setChangeHandler(GUICallback changeHandler) {
        this.changeHandler = changeHandler;
    }
}
