package com.techjar.ledcm.gui;

import com.techjar.ledcm.render.RenderHelper;
import com.techjar.ledcm.util.MathHelper;

import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class GUIProgressBar extends GUI {
	protected Color color;
	protected Color bgColor;
	protected float value;

	public GUIProgressBar(Color color, Color bgColor) {
		this.color = color;
		this.bgColor = bgColor;
	}

	@Override
	protected boolean keyboardEvent(int key, boolean state, char character) {
		return true;
	}

	@Override
	protected boolean mouseEvent(int button, boolean state, int dwheel) {
		return true;
	}

	@Override
	public void update(float delta) {
	}

	@Override
	public void render() {
		if (value < 1) RenderHelper.drawSquare(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight(), bgColor);
		if (value > 0) RenderHelper.drawSquare(getPosition().getX(), getPosition().getY(), (float)dimension.getWidth() * value, dimension.getHeight(), bgColor);
	}

	public Color getBackgroundColor() {
		return bgColor;
	}

	public void setBackgroundColor(Color bgColor) {
		this.bgColor = bgColor;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = MathHelper.clamp(value, 0, 1);
	}
}
