package com.techjar.ledcm.gui;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.render.RenderHelper;
import org.lwjgl.input.Keyboard;
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
    protected int draggerSize;
    protected int lineSize;
    protected float increment;
    protected boolean showNotches;
    protected boolean vertical;
    protected boolean dragging;
    protected long repeatTime;
    protected GUICallback changeHandler;

    public GUISlider(Color color, Color lineColor) {
        this.color = color;
        this.lineColor = lineColor;
        this.draggerSize = 10;
        this.lineSize = 2;
        this.increment = 0;
        this.showNotches = true;
        this.vertical = false;
    }
    
    @Override
    public boolean processKeyboardEvent() {
        if (Keyboard.getEventKeyState()) {
            if (checkMouseIntersect(getComponentBox())) {
                float incr = increment > 0 ? increment : 0.05F;
                switch (Keyboard.getEventKey()) {
                    case Keyboard.KEY_LEFT:
                    case Keyboard.KEY_DOWN:
                        value = MathHelper.clamp(value - incr, 0, 1);
                        if (changeHandler != null && lastValue != value) {
                            changeHandler.setComponent(this);
                            changeHandler.run();
                        }
                        lastValue = value;
                        return false;
                    case Keyboard.KEY_RIGHT:
                    case Keyboard.KEY_UP:
                        value = MathHelper.clamp(value + incr, 0, 1);
                        if (changeHandler != null && lastValue != value) {
                            changeHandler.setComponent(this);
                            changeHandler.run();
                        }
                        lastValue = value;
                        return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean processMouseEvent() {
        if (Mouse.getEventButton() == 0) {
            if (Mouse.getEventButtonState()) {
                Rectangle box = getSliderBox();
                if (checkMouseIntersect(getComponentBox())) {
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
            if (vertical) {
                int mouseY = Util.getMouseY() - (int)getPosition().getY() - draggerSize / 2;
                value = 1 - MathHelper.clamp(mouseY, 0, dimension.getHeight() - draggerSize) / (float)(dimension.getHeight() - draggerSize);
            } else {
                int mouseX = Util.getMouseX() - (int)getPosition().getX() - draggerSize / 2;
                value = MathHelper.clamp(mouseX, 0, dimension.getWidth() - draggerSize) / (float)(dimension.getWidth() - draggerSize);
            }
            if (increment > 0) {
                float mult = 1 / increment;
                value = MathHelper.clamp(Math.round(value * mult) / mult, 0, 1);
            }
            if (changeHandler != null && lastValue != value) {
                changeHandler.setComponent(this);
                changeHandler.run();
            }
            lastValue = value;
        }
        
        if (!Mouse.isButtonDown(0)) {
            Rectangle box = getSliderBox();
            if (checkMouseIntersect(box)) {
                if (!hovered && !dragging) LEDCubeManager.getSoundManager().playEffect("ui/rollover.wav", false);
                hovered = true;
            }
            else hovered = false;
        }
    }

    @Override
    public void render() {
        Color theColor = new Color(color);
        Rectangle box = getSliderBox();
        if (dragging || checkMouseIntersect(box))
            theColor = Util.addColors(theColor, new Color(50, 50, 50));
        if (vertical) {
            if (showNotches && increment > 0) {
                for (float i = increment; 1 - i > 0.0001F; i += increment) {
                    RenderHelper.drawSquare(getPosition().getX() + dimension.getWidth() / 2 - lineSize * 2, getPosition().getY() + draggerSize / 2 + (dimension.getHeight() - draggerSize) * i - 1, lineSize * 4, lineSize, lineColor);
                }
            }
            RenderHelper.drawSquare(getPosition().getX() + dimension.getWidth() / 2 - lineSize / 2, getPosition().getY() + draggerSize / 2, lineSize, dimension.getHeight() - draggerSize, lineColor);
            RenderHelper.drawSquare(getPosition().getX(), getPosition().getY() + getSliderPos(), dimension.getWidth(), draggerSize, theColor);
        } else {
            if (showNotches && increment > 0) {
                for (float i = increment; 1 - i > 0.0001F; i += increment) {
                    RenderHelper.drawSquare(getPosition().getX() + draggerSize / 2 + (dimension.getWidth() - draggerSize) * i - 1, getPosition().getY() + dimension.getHeight() / 2 - lineSize * 2, lineSize, lineSize * 4, lineColor);
                }
            }
            RenderHelper.drawSquare(getPosition().getX() + draggerSize / 2, getPosition().getY() + dimension.getHeight() / 2 - lineSize / 2, dimension.getWidth() - draggerSize, lineSize, lineColor);
            RenderHelper.drawSquare(getPosition().getX() + getSliderPos(), getPosition().getY(), draggerSize, dimension.getHeight(), theColor);
        }
    }
    
    protected int getSliderPos() {
        if (vertical) return (int)((1 - value) * (dimension.getHeight() - draggerSize));
        return (int)(value * (dimension.getWidth() - draggerSize));
    }

    protected Rectangle getSliderBox() {
        if (vertical) return new Rectangle(getPosition().getX(), getPosition().getY() + (getSliderPos()), dimension.getWidth(), draggerSize);
        return new Rectangle(getPosition().getX() + getSliderPos(), getPosition().getY(), draggerSize, dimension.getHeight());
    }
    
    public float getValue() {
        return value;
    }
    
    public void setValue(float value) {
        this.value = MathHelper.clamp(value, 0, 1);
        if (increment > 0) {
            float mult = 1 / increment;
            this.value = MathHelper.clamp(Math.round(this.value * mult) / mult, 0, 1);
        }
        if (changeHandler != null) {
            changeHandler.setComponent(this);
            changeHandler.run();
        }
    }

    public void setValueWithoutNotify(float value) {
        this.value = MathHelper.clamp(value, 0, 1);
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

    public int getDraggerSize() {
        return draggerSize;
    }

    public void setDraggerSize(int draggerWidth) {
        this.draggerSize = draggerWidth;
    }

    public int getLineSize() {
        return lineSize;
    }

    public void setLineSize(int lineHeight) {
        this.lineSize = lineHeight;
    }

    public float getIncrement() {
        return increment;
    }

    public void setIncrement(float increment) {
        this.increment = increment;
        setValue(value);
    }

    public boolean getShowNotches() {
        return showNotches;
    }

    public void setShowNotches(boolean showNotches) {
        this.showNotches = showNotches;
    }

    public boolean isVertical() {
        return vertical;
    }

    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }

    public GUICallback getChangeHandler() {
        return changeHandler;
    }

    public void setChangeHandler(GUICallback changeHandler) {
        this.changeHandler = changeHandler;
    }
}
