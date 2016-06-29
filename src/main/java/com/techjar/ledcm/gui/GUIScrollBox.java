package com.techjar.ledcm.gui;

import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.render.RenderHelper;
import com.techjar.ledcm.util.Vector2;
import lombok.NonNull;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Color;
import org.newdawn.slick.geom.Rectangle;

/**
 *
 * @author Techjar
 */
public class GUIScrollBox extends GUIContainer {
    protected Color color;
    protected Color bgColor;
    protected ScrollMode scrollXMode = ScrollMode.AUTOMATIC;
    protected ScrollMode scrollYMode = ScrollMode.AUTOMATIC;
    protected int scrollXIncrement;
    protected int scrollYIncrement;
    protected int scrollbarWidth = 10;
    
    protected Vector2 scrollOffset = new Vector2();
    protected Vector2 scrollOffsetStart = new Vector2();
    protected Vector2 scrollbarOffsetStart = new Vector2();
    protected Vector2 mouseStart = new Vector2();
    protected int scrolling;

    public GUIScrollBox(Color color, Color bgColor) {
        this.color = color;
        this.bgColor = bgColor;
    }

    public GUIScrollBox(Color color) {
        this(color, new Color(15, 15, 15));
    }
    
    @Override
    public boolean processKeyboardEvent() {
        return super.processKeyboardEvent();
    }

    @Override
    public boolean processMouseEvent() {
        if (!super.processMouseEvent()) return false;
        if (Mouse.getEventButton() == 0) {
            if (Mouse.getEventButtonState()) {
                Vector2 scrollbarOffset = getScrollbarOffset();
                int[] size = getScrollbarSize();
                if (getScrollX()) {
                    Rectangle box = new Rectangle(getPosition().getX() + scrollbarOffset.getX() + 1, getPosition().getY() + dimension.getHeight() - scrollbarWidth - 1, size[0], scrollbarWidth);
                    if (checkMouseIntersect(box)) {
                        scrolling = 1;
                        mouseStart.set(Util.getMousePos());
                        scrollOffsetStart.set(scrollOffset);
                        scrollbarOffsetStart.set(getScrollbarOffset());
                        return false;
                    }
                }
                if (getScrollY()) {
                    Rectangle box = new Rectangle(getPosition().getX() + dimension.getWidth() - scrollbarWidth - 1, getPosition().getY() + scrollbarOffset.getY() + 1, scrollbarWidth, size[1]);
                    if (checkMouseIntersect(box)) {
                        scrolling = 2;
                        mouseStart.set(Util.getMousePos());
                        scrollOffsetStart.set(scrollOffset);
                        scrollbarOffsetStart.set(getScrollbarOffset());
                        return false;
                    }
                }
            }
            else scrolling = 0;
        }
        if (scrolling == 0 && Mouse.getEventDWheel() != 0) {
            if (checkMouseIntersect(getComponentBox())) {
                int[] maxScrollOffset = getMaxScrollOffset();
                if (scrollYIncrement > 0) scrollOffset.setY(MathHelper.clamp(scrollOffset.getY() + (scrollYIncrement * -MathHelper.sign(Mouse.getEventDWheel())), 0, maxScrollOffset[1]));
                else scrollOffset.setY(MathHelper.clamp(scrollOffset.getY() - Mouse.getEventDWheel(), 0, maxScrollOffset[1]));
                return false;
            }
        }
        return true;
    }

    @Override
    public void update(float delta) {
        if (scrolling != 0) {
            Vector2 mouseOffset = Util.getMousePos().subtract(mouseStart);
            int[] maxScrollOffset = getMaxScrollOffset();
            int[] maxScrollbarOffset = getMaxScrollbarOffset();
            if (scrolling == 1 && maxScrollOffset[0] > 0) {
                float offset = MathHelper.clamp(scrollOffsetStart.getX() + (mouseOffset.getX() * ((float)maxScrollOffset[0] / (float)maxScrollbarOffset[0])), 0, maxScrollOffset[0]);
                if (scrollXIncrement > 0) scrollOffset.setX(offset - (offset % scrollXIncrement));
                else scrollOffset.setX(offset);
            }
            else if (scrolling == 2 && maxScrollOffset[1] > 0) {
                float offset = MathHelper.clamp(scrollOffsetStart.getY() + (mouseOffset.getY() * ((float)maxScrollOffset[1] / (float)maxScrollbarOffset[1])), 0, maxScrollOffset[1]);
                if (scrollYIncrement > 0) scrollOffset.setY(offset - (offset % scrollYIncrement));
                else scrollOffset.setY(offset);
            }
        }
        super.update(delta);
    }

    @Override
    public void render() {
        Vector2 scrollbarOffset = getScrollbarOffset();
        int[] size = getScrollbarSize();
        if (getScrollX()) {
            Color color2 = new Color(color);
            Rectangle box = new Rectangle(getPosition().getX() + scrollbarOffset.getX() + 1, getPosition().getY() + dimension.getHeight() - scrollbarWidth - 1, size[0], scrollbarWidth);
            if (scrolling == 1 || (scrolling == 0 && checkMouseIntersect(box))) {
                color2 = Util.addColors(color2, new Color(50, 50, 50));
            }
            //RenderHelper.drawSquare(getPosition().getX(), getPosition().getY() + dimension.getHeight() - 10, dimension.getWidth(), 10, bgColor);
            RenderHelper.drawSquare(getPosition().getX() + scrollbarOffset.getX() + 1, getPosition().getY() + dimension.getHeight() - scrollbarWidth - 1, size[0], scrollbarWidth, color2);
        }
        if (getScrollY()) {
            Color color2 = new Color(color);
            Rectangle box = new Rectangle(getPosition().getX() + dimension.getWidth() - scrollbarWidth - 1, getPosition().getY() + scrollbarOffset.getY() + 1, scrollbarWidth, size[1]);
            if (scrolling == 2 || (scrolling == 0 && checkMouseIntersect(box))) {
                color2 = Util.addColors(color2, new Color(50, 50, 50));
            }
            //RenderHelper.drawSquare(getPosition().getX() + dimension.getWidth() - 10, getPosition().getY(), 10, dimension.getHeight(), bgColor);
            RenderHelper.drawSquare(getPosition().getX() + dimension.getWidth() - scrollbarWidth - 1, getPosition().getY() + scrollbarOffset.getY() + 1, scrollbarWidth, size[1], color2);
        }
        if (getScrollX() && getScrollY()) {
            RenderHelper.drawSquare(getPosition().getX() + dimension.getWidth() - scrollbarWidth - 1, getPosition().getY() + dimension.getHeight() - scrollbarWidth - 1, scrollbarWidth, scrollbarWidth, color);
        }
        super.render();
    }

    @Override
    public Vector2 getContainerPosition() {
        return getPosition().subtract(scrollOffset);
    }

    @Override
    public Rectangle getContainerBox() {
        return new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth() - (getScrollY() ? scrollbarWidth + 2 : 0), dimension.getHeight() - (getScrollX() ? scrollbarWidth + 2 : 0));
    }

    private boolean getScrollX(boolean checkAuto) {
        if (checkAuto && scrollXMode == ScrollMode.AUTOMATIC) return getMaxScrollOffset()[0] > 0;
        return scrollXMode == ScrollMode.ENABLED || scrollXMode == ScrollMode.AUTOMATIC;
    }

    private boolean getScrollX() {
        return getScrollX(true);
    }

    private boolean getScrollY(boolean checkAuto) {
        if (checkAuto && scrollYMode == ScrollMode.AUTOMATIC) return getMaxScrollOffset()[1] > 0;
        return scrollYMode == ScrollMode.ENABLED || scrollYMode == ScrollMode.AUTOMATIC;
    }

    private boolean getScrollY() {
        return getScrollY(true);
    }

    public ScrollMode getScrollXMode() {
        return scrollXMode;
    }

    public void setScrollXMode(@NonNull ScrollMode scrollXMode) {
        this.scrollXMode = scrollXMode;
    }

    public ScrollMode getScrollYMode() {
        return scrollYMode;
    }

    public void setScrollYMode(@NonNull ScrollMode scrollYMode) {
        this.scrollYMode = scrollYMode;
    }

    public int getScrollXIncrement() {
        return scrollXIncrement;
    }

    public void setScrollXIncrement(int scrollXIncrement) {
        this.scrollXIncrement = scrollXIncrement;
    }

    public int getScrollYIncrement() {
        return scrollYIncrement;
    }

    public void setScrollYIncrement(int scrollYIncrement) {
        this.scrollYIncrement = scrollYIncrement;
    }

    public int getScrollbarWidth() {
        return scrollbarWidth;
    }

    public void setScrollbarWidth(int scrollbarWidth) {
        this.scrollbarWidth = scrollbarWidth;
    }

    public Vector2 getScrollOffset() {
        return scrollOffset.copy();
    }

    public void setScrollOffset(Vector2 offset) {
        int[] maxOffset = getMaxScrollOffset();
        this.scrollOffset = new Vector2(MathHelper.clamp(offset.getX(), 0, maxOffset[0]), MathHelper.clamp(offset.getY(), 0, maxOffset[1]));
    }

    public void setScrollOffset(int x, int y) {
        setScrollOffset(new Vector2(x, y));
    }
    
    public Vector2 getScrollbarOffset() {
        int[] maxOffset = getMaxScrollOffset();
        int[] maxBarOffset = getMaxScrollbarOffset();
        return new Vector2((maxOffset[0] == 0 ? 0 : maxBarOffset[0] * (scrollOffset.getX() / (float)maxOffset[0])), (maxOffset[1] == 0 ? 0 : maxBarOffset[1] * (scrollOffset.getY() / (float)maxOffset[1])));
    }
    
    public int[] getMaxScrollOffset() {
        int[] maxOffset = new int[2];
        GUI bottom = getBottomComponent(), right = getRightComponent();
        if (getScrollY(false) && bottom != null) {
            maxOffset[1] = (int)Math.max(bottom.getRawPosition().getY() + bottom.getDimension().getHeight() - dimension.getHeight() + (getScrollX(false) ? scrollbarWidth + 2 : 0), 0);
        }
        if (getScrollX(false) && right != null) {
            maxOffset[0] = (int)Math.max(right.getRawPosition().getX() + right.getDimension().getWidth() - dimension.getWidth() + (getScrollY(false) ? scrollbarWidth + 2 : 0), 0);
        }
        return maxOffset;
    }
    
    public int[] getMaxScrollbarOffset() {
        float[] sizeFactor = getScrollbarSizeFactorInverse();
        int[] offset = new int[2];
        offset[0] = Math.max((int)(dimension.getWidth() * sizeFactor[0]), 1);
        offset[1] = Math.max((int)(dimension.getHeight() * sizeFactor[1]), 1);
        return offset;
    }
    
    public float[] getScrollbarSizeFactor() {
        float[] size = new float[]{1, 1};
        GUI bottom = getBottomComponent(), right = getRightComponent();
        if (getScrollY(false) && bottom != null) {
            float compPos = bottom.getRawPosition().getY() + bottom.getDimension().getHeight();
            float dividend = dimension.getHeight() - (getScrollX() ? scrollbarWidth + 2 : 0);
            if (compPos > 0 && compPos >= dividend) size[1] = Math.max(dividend / compPos, 20F / dividend);
        }
        if (getScrollX(false) && right != null) {
            float compPos = right.getRawPosition().getX() + right.getDimension().getWidth();
            float dividend = dimension.getWidth() - (getScrollY() ? scrollbarWidth + 2 : 0);
            if (compPos > 0 && compPos >= dividend) size[0] = Math.max(dividend / compPos, 20F / dividend);
        }
        return size;
    }
    
    public float[] getScrollbarSizeFactorInverse() {
        float[] size = getScrollbarSizeFactor();
        size[0] = 1 - size[0];
        size[1] = 1 - size[1];
        return size;
    }

    public int[] getScrollbarSize() {
        int[] size = new int[2];
        float[] sizeFactor = getScrollbarSizeFactor();
        size[0] = (int)((dimension.getWidth() - 2) * sizeFactor[0]) - (getScrollX() && getScrollY() ? scrollbarWidth + 2 : 0);
        size[1] = (int)((dimension.getHeight() - 2) * sizeFactor[1]) - (getScrollX() && getScrollY() ? scrollbarWidth + 2 : 0);
        return size;
    }
    
    public GUI getBottomComponent() {
        GUI bottom = null;
        for (GUI gui : components) {
            if (bottom == null) bottom = gui;
            else if (gui.getRawPosition().getY() + gui.getHeight() > bottom.getRawPosition().getY() + bottom.getHeight()) bottom = gui;
        }
        return bottom;
    }
    
    public GUI getRightComponent() {
        GUI right = null;
        for (GUI gui : components) {
            if (right == null) right = gui;
            else if (gui.getRawPosition().getX() + gui.getWidth() > right.getRawPosition().getX() + right.getWidth()) right = gui;
        }
        return right;
    }

    public static enum ScrollMode {
        ENABLED,
        DISABLED,
        AUTOMATIC
    }
}
