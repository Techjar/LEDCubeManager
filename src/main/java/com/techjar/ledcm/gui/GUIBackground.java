package com.techjar.ledcm.gui;

import com.techjar.ledcm.render.RenderHelper;
import org.lwjgl.util.Color;
import org.newdawn.slick.opengl.Texture;

/**
 *
 * @author Techjar
 */
public class GUIBackground extends GUI {
    protected Color bgColor;
    protected Color borderColor;
    protected int borderSize;
    protected Texture texture;

    public GUIBackground(Color bgColor, Color borderColor, int borderSize, Texture texture) {
        this.bgColor = bgColor;
        this.borderColor = borderColor;
        this.borderSize = borderSize;
        this.texture = texture;
    }

    public GUIBackground(Color bgColor, Color borderColor, int borderSize) {
        this(bgColor, borderColor, borderSize, null);
    }
    
    public GUIBackground() {
        this(new Color(50, 50, 50), new Color(200, 0, 0), 2);
    }
    
    @Override
    public boolean processKeyboardEvent() {
        return true;
    }

    @Override
    public boolean processMouseEvent() {
        return true;
    }

    @Override
    public void update(float delta) {
    }

    @Override
    public void render() {
        if (texture != null) {
            texture.bind();
            RenderHelper.drawSquare(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight(), bgColor, texture);
        }
        else RenderHelper.drawSquare(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight(), bgColor);
        if (borderSize > 0) RenderHelper.drawBorder(getPosition().getX(), getPosition().getY(), dimension.getWidth(), dimension.getHeight(), borderSize, borderColor);
    }
    
    public Color getBackgroundColor() {
        return bgColor;
    }

    public void setBackgroundColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    public int getBorderSize() {
        return borderSize;
    }

    public void setBorderSize(int borderSize) {
        this.borderSize = borderSize;
    }
}
