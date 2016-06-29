package com.techjar.ledcm.gui;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Util;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Color;
import org.lwjgl.util.Dimension;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Rectangle;

/**
 *
 * @author Techjar
 */
public class GUIButton extends GUIText {
    protected GUIBackground guiBg;
    protected GUICallback clickHandler;
    protected boolean pressed;
    protected boolean windowClose;
    
    public GUIButton(UnicodeFont font, Color color, String text, GUIBackground guiBg) {
        super(font, color, text);
        this.guiBg = guiBg;
        if (guiBg != null) guiBg.setParent(this);
    }
    
    public GUIButton(UnicodeFont font, Color color, String text) {
        this(font, color, text, null);
    }

    @Override
    public boolean processKeyboardEvent() {
        return super.processKeyboardEvent();
    }

    @Override
    public boolean processMouseEvent() {
        if (Mouse.getEventButton() == 0) {
            if (Mouse.getEventButtonState()) {
                Rectangle box = new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight());
                if (checkMouseIntersect(!windowClose, box)) {
                    pressed = true;
                    LEDCubeManager.getSoundManager().playEffect("ui/click.wav", false);
                    if (clickHandler != null) {
                        clickHandler.setComponent(this);
                        clickHandler.run();
                    }
                    return false;
                }
            }
            else pressed = false;
        }
        return true;
    }
    
    @Override
    public void update(float delta) {
        if (!Mouse.isButtonDown(0)) {
            if (checkMouseIntersect(!windowClose, getComponentBox())) {
                if (!hovered) LEDCubeManager.getSoundManager().playEffect("ui/rollover.wav", false);
                hovered = true;
            }
            else hovered = false;
        }
    }

    @Override
    public void render() {
        if (guiBg != null) {
            if (pressed || hovered) {
                Color color2 = guiBg.getBorderColor(), color3 = guiBg.getBackgroundColor();
                guiBg.setBorderColor(Util.addColors(color2, new Color(50, 50, 50)));
                guiBg.setBackgroundColor(Util.addColors(color3, new Color(50, 50, 50)));
                guiBg.render();
                guiBg.setBorderColor(color2);
                guiBg.setBackgroundColor(color3);
            }
            else guiBg.render();
        }
        if (font != null && color != null) font.drawString(getPosition().getX() + ((dimension.getWidth() - font.getWidth(text.toString())) / 2), getPosition().getY() + ((dimension.getHeight() - font.getHeight(text.toString())) / 2), text.toString(), Util.convertColor(color));
    }

    @Override
    public void setDimension(Dimension dimension) {
        super.setDimension(dimension);
        if (guiBg != null) guiBg.setDimension(dimension);
    }

    public void click() {
        if (clickHandler != null) {
            clickHandler.setComponent(this);
            clickHandler.run();
        }
    }

    public GUICallback getClickHandler() {
        return clickHandler;
    }

    public void setClickHandler(GUICallback clickHandler) {
        this.clickHandler = clickHandler;
    }
}
