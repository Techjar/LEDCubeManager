package com.techjar.ledcm.gui;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.MathHelper;
import org.lwjgl.util.Color;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Dimension;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Rectangle;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.render.RenderHelper;
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
    protected int cursorPosition;
    protected int textPosition;
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
    protected boolean mouse0Pressed;
    
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
                                if ((text.substring(0, cursorPosition) + str + text.substring(cursorPosition)).matches(validationRegex)) {
                                    text.insert(cursorPosition, str);
                                    for (int i = 0; i < str.length(); i++) {
                                        moveCursor(true);
                                    }
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

        if (!mouse0Pressed && Mouse.isButtonDown(0)) {
            mouse0Pressed = true;
            Rectangle box = new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight());
            if (checkMouseIntersect(box)) {
                int mouseX = Util.getMouseX() - (int)box.getX() - guiBg.getBorderSize();
                int[] range = getVisibleTextRange();
                for (int i = range[1]; i >= range[0]; i--) {
                    int width = font.getWidth(text.substring(range[0], i));
                    if (width <= mouseX) {
                        if (width == mouseX || i == range[1]) setCursorPosition(i);
                        else {
                            int leftDist = Math.abs(mouseX - width);
                            int rightDist = Math.abs(mouseX - font.getWidth(text.substring(range[0], i + 1)));
                            if (leftDist < rightDist) setCursorPosition(i);
                            else setCursorPosition(i + 1);
                        }
                        break;
                    }
                }
                focused = true;
                cursorState = true;
                cursorTimer.restart();
            } else if(focused && canLoseFocus) {
                focused = false;
            }
        } else if (mouse0Pressed && !Mouse.isButtonDown(0)) {
            mouse0Pressed = false;
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
        if (!enabled || !visible) focused = false;
        Rectangle box = new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight());
        if (checkMouseIntersect(box)) {
            Color borderColor2 = new Color(guiBg.getBorderColor());
            guiBg.setBorderColor(Util.addColors(guiBg.getBorderColor(), new Color(50, 50, 50)));
            guiBg.render();
            guiBg.setBorderColor(borderColor2);
        }
        else guiBg.render();
        String visibleText = getRenderedText();
        float boxWidth = dimension.getWidth() - (guiBg.getBorderSize() * 2);
        RenderHelper.beginScissor(new Rectangle(getPosition().getX() + guiBg.getBorderSize(), getPosition().getY() + guiBg.getBorderSize(), boxWidth, dimension.getHeight() - (guiBg.getBorderSize() * 2)));
        font.drawString(getPosition().getX() + guiBg.getBorderSize(), getPosition().getY() + guiBg.getBorderSize(), visibleText, Util.convertColor(color));
        RenderHelper.endScissor();
        if (focused && (cursorState || repeatState)) RenderHelper.drawSquare(getPosition().getX() + getCursorRenderOffset(), getPosition().getY() + guiBg.getBorderSize() + 2, guiBg.getBorderSize(), dimension.getHeight() - (guiBg.getBorderSize() * 2) - 4, color);
    }

    protected void handleKeyOrCharacter(int key, char ch) {
        if (text.length() < maxLength && Util.isPrintableCharacter(ch) && (text.substring(0, cursorPosition) + ch + text.substring(cursorPosition)).matches(validationRegex)) {
            text.insert(cursorPosition, ch);
            moveCursor(true);
            if (changeHandler != null) {
                changeHandler.setComponent(this);
                changeHandler.run();
            }
            repeatState = true;
        } else if (key == Keyboard.KEY_BACK && text.length() > 0) {
            if (cursorPosition > 0 && (text.substring(0, cursorPosition - 1) + text.substring(cursorPosition)).matches(validationRegex)) {
                text.deleteCharAt(cursorPosition - 1);
                moveCursor(false);
                if (cursorPosition == textPosition && textPosition > 0) textPosition--;
                if (changeHandler != null) {
                    changeHandler.setComponent(this);
                    changeHandler.run();
                }
                repeatState = true;
            }
        } else if (key == Keyboard.KEY_LEFT) {
            repeatState = moveCursor(false);
        } else if (key == Keyboard.KEY_RIGHT) {
            repeatState = moveCursor(true);
        }
    }

    protected boolean moveCursor(boolean right) {
        if (right) {
            if (cursorPosition < text.length()) {
                cursorPosition++;
                while (textPosition < text.length() - 1 && cursorPosition > getVisibleTextRange()[1]) textPosition++;
                return true;
            }
        } else {
            if (cursorPosition > 0) {
                cursorPosition--;
                if (cursorPosition < textPosition) textPosition--;
                return true;
            }
        }
        return false;
    }

    public int getCursorPosition() {
        return cursorPosition;
    }

    public void setCursorPosition(int position) {
        int oldPos = cursorPosition;
        if (position > oldPos) {
            for (int i = 0; i < position - oldPos; i++) {
                moveCursor(true);
            }
        } else {
            for (int i = 0; i < oldPos - position; i++) {
                moveCursor(false);
            }
        }
    }

    protected float getCursorRenderOffset() {
        int[] range = getVisibleTextRange();
        return MathHelper.clamp(guiBg.getBorderSize() + font.getWidth(text.substring(range[0], cursorPosition)), guiBg.getBorderSize(), dimension.getWidth() - guiBg.getBorderSize());
    }

    public int[] getVisibleTextRange() {
        if (text.length() < 1) return new int[]{0, 0};
        float boxWidth = dimension.getWidth() - (guiBg.getBorderSize() * 2);
        int end;
        for (end = textPosition; end <= text.length(); end++) {
            int width = font.getWidth(text.substring(textPosition, end));
            if (width > boxWidth) {
                end--;
                break;
            }
        }
        if (end > text.length()) end = text.length();
        return new int[]{textPosition, end};
    }

    public String getVisibleText() {
        int[] range = getVisibleTextRange();
        return text.substring(range[0], range[1]);
    }

    public String getRenderedText() {
        int[] range = getVisibleTextRange();
        return text.substring(range[0], range[1] < text.length() ? range[1] + 1 : range[1]);
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
