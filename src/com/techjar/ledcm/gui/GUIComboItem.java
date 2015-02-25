package com.techjar.ledcm.gui;

import com.techjar.ledcm.RenderHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Color;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;

/**
 *
 * @author Techjar
 */
public class GUIComboItem extends GUIText {
    protected Object value;
    protected Color hoverBgColor;
    protected GUIComboBox comboBox;

    protected boolean pressed;

    public GUIComboItem(GUIComboBox comboBox, UnicodeFont font, Color color, Color hoverBgColor, Object value) {
        super(font, color, value.toString());
        this.value = value;
        this.comboBox = comboBox;
        this.hoverBgColor = hoverBgColor;
    }

    @Override
    public boolean processMouseEvent() {
        if (Mouse.getEventButton() == 0) {
            if (Mouse.getEventButtonState()) {
                Rectangle box = new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight());
                if (checkMouseIntersect(box)) {
                    pressed = true;
                    comboBox.setOpened(false);
                    comboBox.setSelectedItem(this);
                    return false;
                }
            }
            else pressed = false;
        }
        return true;
    }

    @Override
    public void render() {
        Shape box = getComponentBox();
        if (checkMouseIntersect(box)) RenderHelper.drawSquare(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight(), hoverBgColor);
        super.render();
    }

    public Object getValue() {
        return value;
    }
}
