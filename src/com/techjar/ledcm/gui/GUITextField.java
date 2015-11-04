package com.techjar.ledcm.gui;

import com.techjar.ledcm.util.MathHelper;
import org.lwjgl.util.Color;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Dimension;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Rectangle;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.RenderHelper;
import com.techjar.ledcm.util.Timer;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 *
 * @author Techjar
 */
public class GUITextField extends GUIText {
    protected GUIBackground guiBg;
    protected int maxLength = Short.MAX_VALUE;
    protected String validationRegex = ".*";
    protected boolean focused;
    protected boolean canLoseFocus = true;
    //protected int cursorPosition;
    protected GUICallback changeHandler;
    
    // Timing stuff
    protected Timer cursorTimer = new Timer();
    protected Timer repeatStartTimer = new Timer();
    protected Timer repeatTimer = new Timer();
    protected boolean cursorState;
    protected int repeatLastKey;
    protected char repeatLastChar;
    protected boolean repeatState;
    protected boolean ctrlPressed;
    
    public GUITextField(UnicodeFont font, Color color, GUIBackground guiBg, String text) {
        super(font, color, text);
        guiBg.setParent(this);
        this.guiBg = guiBg;
    }
    
    public GUITextField(UnicodeFont font, Color color, GUIBackground guiBg) {
        this(font, color, guiBg, "");
    }
    
    @Override
    public boolean processKeyboardEvent() {
        super.processKeyboardEvent();
        if (focused) {
            if (Keyboard.getEventKey() == Keyboard.KEY_LCONTROL || Keyboard.getEventKey() == Keyboard.KEY_RCONTROL) {
                ctrlPressed = Keyboard.getEventKeyState();
            }
            else if (Keyboard.getEventKeyState()) {
                if (ctrlPressed) {
                    try {
                        if (Keyboard.getEventKey() == Keyboard.KEY_V) {
                            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                            Transferable data = clipboard.getContents(this);
                            if (data != null && data.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                                String str = (String)data.getTransferData(DataFlavor.stringFlavor);
                                if (text.length() + str.length() > maxLength) {
                                    str = str.substring(0, maxLength - text.length());
                                    if (str.length() < 1) return false;
                                }
                                str = str.replaceAll("[\r\n]", "");
                                for (char ch : str.toCharArray()) {
                                    if (!Util.isPrintableCharacter(ch)) {
                                        return false;
                                    }
                                }
                                if ((text.toString() + str).matches(validationRegex)) {
                                    text.append(str);
                                }
                                if (changeHandler != null) {
                                    changeHandler.setComponent(this);
                                    changeHandler.run();
                                }
                            }
                            return false;
                        }
                    } catch (HeadlessException | UnsupportedFlavorException | IOException ex) {
                        ex.printStackTrace();
                    }
                    return false;
                }
                char ch = Keyboard.getEventCharacter();
                handleKeyOrCharacter(Keyboard.getEventKey(), ch);
                repeatLastKey = Keyboard.getEventKey();
                repeatLastChar = Keyboard.getEventCharacter();
                repeatStartTimer.restart();
            }
            else if (Keyboard.getEventKey() == repeatLastKey || Keyboard.getEventCharacter() == repeatLastChar) {
                repeatState = false;
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean processMouseEvent() {
        return super.processMouseEvent();
    }
    
    @Override
    public void update(float delta) {
        super.update(delta);
        if (cursorTimer.getMilliseconds() >= 500) {
            cursorState = !cursorState;
            cursorTimer.restart();
        }

        if (Mouse.isButtonDown(0)) {
            Rectangle box = new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight());
            if (checkMouseIntersect(box)) {
                focused = true;
                cursorState = true;
                cursorTimer.restart();
            }
            else if(focused && canLoseFocus) {
                focused = false;
            }
        }
        
        if (repeatState && repeatStartTimer.getMilliseconds() >= 400) {
            if (repeatTimer.getMilliseconds() >= 25) {
                handleKeyOrCharacter(repeatLastKey, repeatLastChar);
                repeatTimer.restart();
            }
        }
    }

    @Override
    public void render() {
        if (!enabled) focused = false;
        Rectangle box = new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight());
        if (checkMouseIntersect(box)) {
            Color borderColor2 = new Color(guiBg.getBorderColor());
            guiBg.setBorderColor(Util.addColors(guiBg.getBorderColor(), new Color(50, 50, 50)));
            guiBg.render();
            guiBg.setBorderColor(borderColor2);
        }
        else guiBg.render();
        int textWidth = font.getWidth(text.toString());
        float boxWidth = dimension.getWidth() - (guiBg.getBorderSize() * 2);
        RenderHelper.beginScissor(new Rectangle(getPosition().getX() + guiBg.getBorderSize(), getPosition().getY() + guiBg.getBorderSize(), boxWidth, dimension.getHeight() - (guiBg.getBorderSize() * 2)));
        if (textWidth < boxWidth) font.drawString(getPosition().getX() + guiBg.getBorderSize(), getPosition().getY() + guiBg.getBorderSize(), text.toString(), Util.convertColor(color));
        else font.drawString(getPosition().getX() + guiBg.getBorderSize() - (textWidth - boxWidth), getPosition().getY() + guiBg.getBorderSize(), text.toString(), Util.convertColor(color));
        RenderHelper.endScissor();
        if (focused && cursorState) RenderHelper.drawSquare(getPosition().getX() + getCursorPos(textWidth, boxWidth), getPosition().getY() + guiBg.getBorderSize() + 2, guiBg.getBorderSize(), dimension.getHeight() - (guiBg.getBorderSize() * 2) - 4, color);
    }

    private void handleKeyOrCharacter(int key, char ch) {
        if (text.length() < maxLength && Util.isPrintableCharacter(ch) && (text.toString() + ch).matches(validationRegex)) {
            text.append(ch);
            if (changeHandler != null) {
                changeHandler.setComponent(this);
                changeHandler.run();
            }
            repeatState = true;
        }
        else if (key == Keyboard.KEY_BACK && text.length() > 0) {
            text.deleteCharAt(text.length() - 1);
            if (changeHandler != null) {
                changeHandler.setComponent(this);
                changeHandler.run();
            }
            repeatState = true;
        }
    }

    protected float getCursorPos(int textWidth, float boxWidth) {
        return MathHelper.clamp(textWidth, guiBg.getBorderSize() * 2, boxWidth - guiBg.getBorderSize());
    }

    protected float getCursorPos() {
        return getCursorPos(font.getWidth(text.toString()), dimension.getWidth() - (guiBg.getBorderSize() * 2));
    }

    public GUIBackground getGuiBackground() {
        return guiBg;
    }

    public void setGuiBackground(GUIBackground guiBg) {
        this.guiBg = guiBg;
    }
    
    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public String getValidationRegex() {
        return validationRegex;
    }

    public void setValidationRegex(String validationRegex) {
        this.validationRegex = validationRegex;
    }

    public GUICallback getChangeHandler() {
        return changeHandler;
    }

    public void setChangeHandler(GUICallback changeHandler) {
        this.changeHandler = changeHandler;
    }
    
    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean getCanLoseFocus() {
        return canLoseFocus;
    }

    public void setCanLoseFocus(boolean canLoseFocus) {
        this.canLoseFocus = canLoseFocus;
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        if (changeHandler != null) {
            changeHandler.setComponent(this);
            changeHandler.run();
        }
    }

    @Override
    public void setDimension(Dimension dimension) {
        super.setDimension(dimension);
        guiBg.setDimension(dimension);
    }
}
