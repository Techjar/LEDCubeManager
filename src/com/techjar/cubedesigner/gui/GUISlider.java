package com.techjar.cubedesigner.gui;

import com.techjar.cubedesigner.CubeDesigner;
import com.techjar.cubedesigner.util.MathHelper;
import com.techjar.cubedesigner.util.Util;
import com.techjar.cubedesigner.RenderHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Color;
import org.newdawn.slick.geom.Rectangle;

/**
 *
 * @author Techjar
 */
public class GUISlider extends GUI {
    protected Color color;
    protected Color lineColor;
    protected float value;
    protected float lastValue;
    protected int draggerWidth;
    protected int lineHeight;
    protected boolean dragging;
    protected GUICallback changeHandler;

    public GUISlider(Color color, Color lineColor) {
        this.color = color;
        this.lineColor = lineColor;
        this.draggerWidth = 10;
        this.lineHeight = 2;
    }
    
    @Override
    public boolean processKeyboardEvent() {
        return true;
    }

    @Override
    public boolean processMouseEvent() {
        if (Mouse.getEventButton() == 0) {
            if (Mouse.getEventButtonState()) {
                Rectangle box = new Rectangle(getPosition().getX() + getSliderPos(), getPosition().getY(), draggerWidth, dimension.getHeight());
                if (checkMouseIntersect(box)) {
                    dragging = true;
                    return false;
                }
            }
            else dragging = false;
        }
        return true;
    }

    @Override
    public void update(float delta) {
        if (dragging) {
            int mouseX = Util.getMouseX() - (int)getPosition().getX() - draggerWidth / 2;
            value = (float)MathHelper.clamp(mouseX, 0, dimension.getWidth() - draggerWidth) / (float)(dimension.getWidth() - draggerWidth);
            if (changeHandler != null && lastValue != value) {
                changeHandler.setComponent(this);
                changeHandler.run();
            }
            lastValue = value;
        }
        
        if (!Mouse.isButtonDown(0)) {
            Rectangle box = new Rectangle(getPosition().getX() + getSliderPos(), getPosition().getY(), draggerWidth, dimension.getHeight());
            if (checkMouseIntersect(box)) {
                if (!hovered && !dragging) CubeDesigner.getSoundManager().playEffect("ui/rollover.wav", false);
                hovered = true;
            }
            else hovered = false;
        }
    }

    @Override
    public void render() {
        Color theColor = new Color(color);
        Rectangle box = new Rectangle(getPosition().getX() + getSliderPos(), getPosition().getY(), draggerWidth, dimension.getHeight());
        if (dragging || checkMouseIntersect(box))
            theColor = Util.addColors(theColor, new Color(50, 50, 50));
        RenderHelper.drawSquare(getPosition().getX() + draggerWidth / 2, getPosition().getY() + dimension.getHeight() / 2 - lineHeight / 2, dimension.getWidth() - draggerWidth, lineHeight, lineColor);
        RenderHelper.drawSquare(getPosition().getX() + getSliderPos(), getPosition().getY(), draggerWidth, dimension.getHeight(), theColor);
    }
    
    protected int getSliderPos() {
        return (int)(value * (dimension.getWidth() - draggerWidth));
    }
    
    public float getValue() {
        return value;
    }
    
    public void setValue(float value) {
        this.value = MathHelper.clamp(value, 0, 1);
        if (changeHandler != null) {
            changeHandler.setComponent(this);
            changeHandler.run();
        }
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
    
    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public int getDraggerWidth() {
        return draggerWidth;
    }

    public void setDraggerWidth(int draggerWidth) {
        this.draggerWidth = draggerWidth;
    }

    public int getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    public GUICallback getChangeHandler() {
        return changeHandler;
    }

    public void setChangeHandler(GUICallback changeHandler) {
        this.changeHandler = changeHandler;
    }
}
