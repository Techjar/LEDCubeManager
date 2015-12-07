
package com.techjar.ledcm.gui;

import com.techjar.ledcm.RenderHelper;
import org.lwjgl.util.Color;
import org.lwjgl.util.Dimension;

/**
 *
 * @author Techjar
 */
public class GUIColorPicker extends GUI {
    protected Color value = new Color();
    protected GUISlider redSlider;
    protected GUISlider greenSlider;
    protected GUISlider blueSlider;
    protected GUICallback changeHandler;

    public GUIColorPicker(Color sliderLineColor) {
        redSlider = new GUISlider(new Color(255, 0, 0), sliderLineColor);
        redSlider.setIncrement(1F / 255F);
        redSlider.setShowNotches(false);
        redSlider.setParent(this);
        redSlider.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                value.setRed(Math.round(redSlider.getValue() * 255));
                if (changeHandler != null) {
                    changeHandler.setComponent(GUIColorPicker.this);
                    changeHandler.run();
                }
            }
        });
        greenSlider = new GUISlider(new Color(0, 255, 0), sliderLineColor);
        greenSlider.setIncrement(1F / 255F);
        greenSlider.setShowNotches(false);
        greenSlider.setParent(this);
        greenSlider.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                value.setGreen(Math.round(greenSlider.getValue() * 255));
                if (changeHandler != null) {
                    changeHandler.setComponent(GUIColorPicker.this);
                    changeHandler.run();
                }
            }
        });
        blueSlider = new GUISlider(new Color(0, 0, 255), sliderLineColor);
        blueSlider.setIncrement(1F / 255F);
        blueSlider.setShowNotches(false);
        blueSlider.setParent(this);
        blueSlider.setChangeHandler(new GUICallback() {
            @Override
            public void run() {
                value.setBlue(Math.round(blueSlider.getValue() * 255));
                if (changeHandler != null) {
                    changeHandler.setComponent(GUIColorPicker.this);
                    changeHandler.run();
                }
            }
        });
    }

    @Override
    public boolean processKeyboardEvent() {
        if (!redSlider.processKeyboardEvent()) return false;
        if (!greenSlider.processKeyboardEvent()) return false;
        if (!blueSlider.processKeyboardEvent()) return false;
        return true;
    }

    @Override
    public boolean processMouseEvent() {
        if (!redSlider.processMouseEvent()) return false;
        if (!greenSlider.processMouseEvent()) return false;
        if (!blueSlider.processMouseEvent()) return false;
        return true;
    }

    @Override
    public void update(float delta) {
        redSlider.update(delta);
        greenSlider.update(delta);
        blueSlider.update(delta);
    }

    @Override
    public void render() {
        redSlider.render();
        greenSlider.render();
        blueSlider.render();
        RenderHelper.drawSquare(getPosition().getX() + (dimension.getWidth() - dimension.getHeight()), getPosition().getY(), dimension.getHeight(), dimension.getHeight(), value);
    }

    @Override
    public void setDimension(Dimension dimension) {
        super.setDimension(dimension);
        int sliderWidth = (dimension.getWidth() - dimension.getHeight()) / 3;
        redSlider.setDimension(sliderWidth - 4, dimension.getHeight());
        greenSlider.setDimension(sliderWidth - 4, dimension.getHeight());
        greenSlider.setPosition(sliderWidth, 0);
        blueSlider.setDimension(sliderWidth - 4, dimension.getHeight());
        blueSlider.setPosition(sliderWidth * 2, 0);
    }

    public Color getValue() {
        return new Color(value);
    }

    public void setValue(Color color) {
        value.setRed(color.getRed());
        value.setGreen(color.getGreen());
        value.setBlue(color.getBlue());
        redSlider.setValue(color.getRed() / 255F);
        greenSlider.setValue(color.getGreen() / 255F);
        blueSlider.setValue(color.getBlue() / 255F);
        if (changeHandler != null) {
            changeHandler.setComponent(this);
            changeHandler.run();
        }
    }

    public GUICallback getChangeHandler() {
        return changeHandler;
    }

    public void setChangeHandler(GUICallback changeHandler) {
        this.changeHandler = changeHandler;
    }
}
