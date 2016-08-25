package com.techjar.ledcm.gui;

/**
 *
 * @author Techjar
 */
public class GUISpacer extends GUI {
    public GUISpacer() {
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
    }
}
