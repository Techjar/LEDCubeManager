
package com.techjar.ledcm.gui;

import com.techjar.ledcm.render.RenderHelper;

import org.lwjgl.util.Color;
import org.lwjgl.util.Dimension;
import org.lwjgl.util.ReadableColor;

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
		redSlider.setChangeHandler(component -> {
			value.setRed(Math.round(redSlider.getValue() * 255));
			if (changeHandler != null) {
				changeHandler.run(this);
			}
		});
		greenSlider = new GUISlider(new Color(0, 255, 0), sliderLineColor);
		greenSlider.setIncrement(1F / 255F);
		greenSlider.setShowNotches(false);
		greenSlider.setParent(this);
		greenSlider.setChangeHandler(component -> {
			value.setGreen(Math.round(greenSlider.getValue() * 255));
			if (changeHandler != null) {
				changeHandler.run(this);
			}
		});
		blueSlider = new GUISlider(new Color(0, 0, 255), sliderLineColor);
		blueSlider.setIncrement(1F / 255F);
		blueSlider.setShowNotches(false);
		blueSlider.setParent(this);
		blueSlider.setChangeHandler(component -> {
			value.setBlue(Math.round(blueSlider.getValue() * 255));
			if (changeHandler != null) {
				changeHandler.run(this);
			}
		});
	}

	@Override
	protected boolean keyboardEvent(int key, boolean state, char character) {
		if (!redSlider.keyboardEvent(key, state, character)) return false;
		if (!greenSlider.keyboardEvent(key, state, character)) return false;
		if (!blueSlider.keyboardEvent(key, state, character)) return false;
		return true;
	}

	@Override
	protected boolean mouseEvent(int button, boolean state, int dwheel) {
		if (!redSlider.mouseEvent(button, state, dwheel)) return false;
		if (!greenSlider.mouseEvent(button, state, dwheel)) return false;
		if (!blueSlider.mouseEvent(button, state, dwheel)) return false;
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

	public void setValue(ReadableColor color) {
		value.setRed(color.getRed());
		value.setGreen(color.getGreen());
		value.setBlue(color.getBlue());
		redSlider.setValue(color.getRed() / 255F);
		greenSlider.setValue(color.getGreen() / 255F);
		blueSlider.setValue(color.getBlue() / 255F);
		if (changeHandler != null) {
			changeHandler.run(this);
		}
	}

	public GUICallback getChangeHandler() {
		return changeHandler;
	}

	public void setChangeHandler(GUICallback changeHandler) {
		this.changeHandler = changeHandler;
	}
}
