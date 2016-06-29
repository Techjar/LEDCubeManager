package com.techjar.ledcm.gui;

import static org.lwjgl.opengl.GL11.*;

import com.techjar.ledcm.render.RenderHelper;
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
    protected Color selectedBgColor;
    protected GUIComboBox comboBox;

    protected boolean pressed;

    public GUIComboItem(GUIComboBox comboBox, UnicodeFont font, Color color, Color hoverBgColor, Color selectedBgColor, Object value) {
        super(font, color, value.toString());
        this.value = value;
        this.comboBox = comboBox;
        this.hoverBgColor = hoverBgColor;
        this.selectedBgColor = selectedBgColor;
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
        else if (comboBox.getSelectedItem() == this.getValue()) RenderHelper.drawSquare(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight(), selectedBgColor);
        glTranslatef(3, 0, 0);
        super.render();
        glTranslatef(-3, 0, 0);
    }

    public Object getValue() {
        return value;
    }
}
