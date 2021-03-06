package com.techjar.ledcm.gui;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.input.InputBinding;
import com.techjar.ledcm.util.input.InputInfo;
import com.techjar.ledcm.vr.VRInputEvent;

import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Color;
import org.lwjgl.util.Dimension;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Rectangle;

/**
 *
 * @author Techjar
 */
public class GUIInputOption extends GUI {
	protected Timer bindTimer = new Timer();
	protected UnicodeFont font;
	protected Color color;
	protected GUIBackground guiBg;
	protected InputBinding binding;
	protected GUICallback changeHandler;

	protected boolean assign;

	public GUIInputOption(InputBinding binding, UnicodeFont font, Color color, GUIBackground guiBg) {
		this.binding = binding;
		this.font = font;
		this.color = color;
		this.guiBg = guiBg;
		if (guiBg != null) guiBg.setParent(this);
	}

	public GUIInputOption(InputBinding binding, UnicodeFont font, Color color) {
		this(binding, font, color, null);
	}

	@Override
	protected boolean keyboardEvent(int key, boolean state, char character) {
		if (assign && state) {
			binding.setBind(new InputInfo(InputInfo.Type.KEYBOARD, key));
			if (changeHandler != null) {
				changeHandler.run(this);
			}
			assign = false;
			return false;
		}
		return true;
	}

	@Override
	protected boolean mouseEvent(int button, boolean state, int dwheel) {
		if (state) {
			if (assign) {
				binding.setBind(new InputInfo(InputInfo.Type.MOUSE, button));
				if (changeHandler != null) {
					changeHandler.run(this);
				}
				assign = false;
				return false;
			} else if (button == 0) {
				Rectangle box = new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight());
				if (checkMouseIntersect(box)) {
					assign = true;
					bindTimer.restart();
					return false;
				}
				else assign = false;
			} else if (button == 1) {
				Rectangle box = new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight());
				if (checkMouseIntersect(box)) {
					if (binding.isUnbindable()) {
						binding.setBind(null);
						if (changeHandler != null) {
							changeHandler.run(this);
						}
					}
					return false;
				}
			}
		}
		return true;
	}

	@Override
    public boolean processControllerEvent(Controller controller) { // TODO n' stuff
        if (assign && Controllers.isEventButton() && Controllers.getEventButtonState()) {
            binding.setBind(new InputInfo(InputInfo.Type.CONTROLLER, Controllers.getEventControlIndex()));
            if (changeHandler != null) {
                changeHandler.run(this);
            }
            assign = false;
            return false;
        }
        return true;
    }

	@Override
    public boolean processVRInputEvent(VRInputEvent event) { // TODO n' stuff
        if (assign && event.isButtonPressEvent() && event.getButtonState()) {
            binding.setBind(new InputInfo(InputInfo.Type.VR, event.getButton().ordinal(), event.getController().getType()));
            if (changeHandler != null) {
                changeHandler.run(this);
            }
            assign = false;
            return false;
        }
        return true;
    }

    @Override
    public void update(float delta) {
    	if (assign && bindTimer.getSeconds() >= 3) {
    		assign = false;
    	}
    	if (!mouseState[0] && !assign) {
    		Rectangle box = new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight());
    		if (checkMouseIntersect(box)) {
    			if (!hovered) LEDCubeManager.getSoundManager().playEffect("ui/rollover.wav", false);
    			hovered = true;
    		}
    		else hovered = false;
    	}
    }

    @Override
    public void render() {
    	if (guiBg != null) {
    		if (hovered || assign) {
    			Color color2 = guiBg.getBorderColor(), color3 = guiBg.getBackgroundColor();
    			guiBg.setBorderColor(Util.addColors(color2, new Color(50, 50, 50)));
    			guiBg.setBackgroundColor(Util.addColors(color3, new Color(50, 50, 50)));
    			guiBg.render();
    			guiBg.setBorderColor(color2);
    			guiBg.setBackgroundColor(color3);
    		}
    		else guiBg.render();
    	}
    	//Color color2 = color;
    	//if (hovered || assign) color2 = Util.addColors(color2, new Color(50, 50, 50));
    	String text = assign ? "" : (binding.getBind() == null ? "None" : binding.getBind().getDisplayString());
    	font.drawString(getPosition().getX() + ((dimension.getWidth() - font.getWidth(text)) / 2), getPosition().getY() + ((dimension.getHeight() - font.getHeight(text)) / 2), text, Util.convertColor(color));
    }

    @Override
    public void setDimension(Dimension dimension) {
    	super.setDimension(dimension);
    	if (guiBg != null) guiBg.setDimension(dimension);
    }

    public InputBinding getBinding() {
    	return binding;
    }

    public Color getColor() {
    	return color;
    }

    public void setColor(Color color) {
    	this.color = color;
    }

    public GUICallback getChangeHandler() {
    	return changeHandler;
    }

    public void setChangeHandler(GUICallback changeHandler) {
    	this.changeHandler = changeHandler;
    }
}
