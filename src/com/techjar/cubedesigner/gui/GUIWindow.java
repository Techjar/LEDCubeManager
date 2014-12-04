package com.techjar.cubedesigner.gui;

import com.techjar.cubedesigner.CubeDesigner;
import com.techjar.cubedesigner.RenderHelper;
import com.techjar.cubedesigner.util.Util;
import com.techjar.cubedesigner.util.Vector2;
import java.awt.Cursor;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Color;
import org.lwjgl.util.Dimension;
import org.newdawn.slick.geom.Rectangle;

/**
 *
 * @author Techjar
 */
public class GUIWindow extends GUIContainer {
    public static final int HIDE_ON_CLOSE = 0;
    public static final int REMOVE_ON_CLOSE = 1;

    protected GUIBackground guiBg;
    protected GUIButton closeBtn;
    protected Dimension minSize = new Dimension(50, 50);
    protected Dimension maxSize = new Dimension();
    protected boolean canMove = true;
    protected boolean canResize = true;
    protected int closeAction = HIDE_ON_CLOSE;
    protected boolean onTop;
    
    protected Vector2 mouseLast;
    protected Cursor currentCursor;
    protected boolean dragging;
    protected boolean startResize;
    protected boolean mouseLockX, mouseLockY;
    protected boolean wasMousePressed;
    protected boolean toBePutOnTop;
    protected int resizeX;
    protected int resizeY;

    public GUIWindow(GUIBackground guiBg) {
        this.guiBg = guiBg;
        this.guiBg.setParent(this);
        this.closeBtn = new GUIButton(null, null, "", new GUIBackground(new Color(0, 0, 0), new Color(0, 0, 0, 0), 0, CubeDesigner.getTextureManager().getTexture("ui/windowclose.png")));
        this.closeBtn.setDimension(20, 20);
        this.closeBtn.setParent(this);
        this.closeBtn.windowClose = true;
        this.closeBtn.setClickHandler(new GUICallback() {
            @Override
            public void run() {
                if (closeAction == REMOVE_ON_CLOSE) remove();
                else if (closeAction == HIDE_ON_CLOSE) setVisible(false);
            }
        });
    }

    @Override
    public boolean processKeyboardEvent() {
        return super.processKeyboardEvent();
    }

    @Override
    public boolean processMouseEvent() {
        if (!super.processMouseEvent()) return false;
        if (!closeBtn.processMouseEvent()) return false;
        if (Mouse.getEventButton() == 0) {
            if (!onTop && checkMouseIntersect(getComponentBox())) setToBePutOnTop(true);
            if (Mouse.getEventButtonState()) {
                if (canResize) {
                    Rectangle[] boxes = getBoxes();
                    if (checkMouseIntersect(boxes[0])) {
                        resizeY = -1;
                        if (checkMouseIntersect(boxes[1])) {
                            resizeX = -1;
                        }
                        else if (checkMouseIntersect(boxes[3])) {
                            resizeX = 1;
                        }
                    }
                    else if (checkMouseIntersect(boxes[2])) {
                        resizeY = 1;
                        if (checkMouseIntersect(boxes[1])) {
                            resizeX = -1;
                        }
                        else if (checkMouseIntersect(boxes[3])) {
                            resizeX = 1;
                        }
                    }
                    else if (checkMouseIntersect(boxes[1])) {
                        resizeX = -1;
                    }
                    else if (checkMouseIntersect(boxes[3])) {
                        resizeX = 1;
                    }
                    if (isResizing()) startResize = true;
                }
                if (!isResizing() && canMove) {
                    Rectangle head = new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), 20);
                    if (checkMouseIntersect(head)) dragging = true;
                }
                if (dragging || isResizing()) {
                    mouseLast = Util.getMousePos();
                    return false;
                }
            }
            else {
                dragging = false;
                resizeX = 0;
                resizeY = 0;
            }
        }
        return true;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        closeBtn.update(delta);
        /*if (!wasMousePressed && checkMouseButtons()) {
            wasMousePressed = true;
            if (!onTop) setToBePutOnTop(checkMouseIntersect(getComponentBox()));
        }
        else if (wasMousePressed && !checkMouseButtons()) {
            wasMousePressed = false;
        }*/
        if (canResize && !Util.getMousePos().equals(mouseLast)) {
            if (isResizing()) {
                Vector2 mouseDiff = Util.getMousePos().subtract(mouseLast);
                Vector2 newPos = position.copy();
                Dimension newDim = new Dimension(dimension);
                if (resizeX == 1) {
                    newDim.setWidth(dimension.getWidth() + (int)mouseDiff.getX());
                }
                else if (resizeX == -1) {
                    newPos.setX(position.getX() + mouseDiff.getX());
                    newDim.setWidth(dimension.getWidth() - (int)mouseDiff.getX());
                }
                if (resizeY == 1) {
                    newDim.setHeight(dimension.getHeight() + (int)mouseDiff.getY());
                }
                else if (resizeY == -1) {
                    newPos.setY(position.getY() + mouseDiff.getY());
                    newDim.setHeight(dimension.getHeight() - (int)mouseDiff.getY());
                }
                if (newDim.getWidth() > minSize.getWidth() && (maxSize.getWidth() == 0 || newDim.getWidth() < maxSize.getWidth())) {
                    if (newDim.getWidth() != dimension.getWidth()) setDimension(newDim.getWidth(), dimension.getHeight());
                    if (newPos.getX() != position.getX()) setPosition(newPos.getX(), position.getY());
                    mouseLockX = false;
                }
                else mouseLockX = true;
                if (newDim.getHeight() > minSize.getHeight() && (maxSize.getHeight() == 0 || newDim.getHeight() < maxSize.getHeight())) {
                    if (newDim.getHeight() != dimension.getHeight()) setDimension(dimension.getWidth(), newDim.getHeight());
                    if (newPos.getY() != position.getY()) setPosition(position.getX(), newPos.getY());
                    mouseLockY = false;
                }
                else mouseLockY = true;
            }
            
            if (!Mouse.isButtonDown(0) || startResize) {
                startResize = false;
                Rectangle[] boxes = getBoxes();
                if (checkMouseIntersect(boxes[0])) {
                    if (checkMouseIntersect(boxes[1])) {
                        currentCursor = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
                    }
                    else if (checkMouseIntersect(boxes[3])) {
                        currentCursor = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
                    }
                    else {
                        currentCursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
                    }
                }
                else if (checkMouseIntersect(boxes[2])) {
                    if (checkMouseIntersect(boxes[1])) {
                        currentCursor = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
                    }
                    else if (checkMouseIntersect(boxes[3])) {
                        currentCursor = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
                    }
                    else {
                        currentCursor = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
                    }
                }
                else if (checkMouseIntersect(boxes[1])) {
                    currentCursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
                }
                else if (checkMouseIntersect(boxes[3])) {
                    currentCursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
                }
                else {
                    currentCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
                }
            }
            if (checkMouseIntersect(getComponentBox())) CubeDesigner.getFrame().setCursor(currentCursor);
        }
        if (dragging) {
            setPosition(position.add(Util.getMousePos().subtract(mouseLast)));
        }
        if (mouseLockX && !mouseLockY) mouseLast.setY(Util.getMouseY());
        else if (!mouseLockX && mouseLockY) mouseLast.setX(Util.getMouseX());
        else if (!mouseLockX && !mouseLockY) mouseLast = Util.getMousePos();
    }

    @Override
    public void render() {
        guiBg.render();
        RenderHelper.drawSquare(getPosition().getX(), getPosition().getY(), dimension.getWidth(), 20, guiBg.getBorderColor());
        closeBtn.render();
        super.render();
    }

    @Override
    public void setDimension(Dimension dimension) {
        super.setDimension(dimension);
        guiBg.setDimension(dimension);
        closeBtn.setPosition(dimension.getWidth() - 20, 0);
    }

    @Override
    public Rectangle getContainerBox() {
        return new Rectangle(getPosition().getX() + guiBg.getBorderSize(), getPosition().getY() + 20, dimension.getWidth() - (guiBg.getBorderSize() * 2), dimension.getHeight() - guiBg.getBorderSize() - 20);
    }

    @Override
    public Vector2 getContainerPosition() {
        return getPosition().add(new Vector2(guiBg.getBorderSize(), 20));
    }

    protected boolean checkMouseButtons() {
        for (int i = 0; i < Mouse.getButtonCount(); i++) {
            if (Mouse.isButtonDown(i)) return true;
        }
        return false;
    }

    public boolean isMoveable() {
        return canMove;
    }

    public void setMoveable(boolean canMove) {
        this.canMove = canMove;
    }

    public boolean isResizable() {
        return canResize;
    }

    public void setResizable(boolean canResize) {
        this.canResize = canResize;
    }

    public int getCloseAction() {
        return closeAction;
    }

    public void setCloseAction(int closeAction) {
        this.closeAction = closeAction;
    }
    
    public Dimension getMinimumSize() {
        return minSize;
    }

    public void setMinimumSize(Dimension minSize) {
        this.minSize = minSize;
    }

    public Dimension getMaximumSize() {
        return maxSize;
    }

    public void setMaximumSize(Dimension maxSize) {
        this.maxSize = maxSize;
    }
    
    protected boolean isResizing() {
        return resizeX != 0 || resizeY != 0;
    }

    public boolean isOnTop() {
        return onTop;
    }

    /**
     * Used internally for setting the onTop field! Setting this will NOT put the window on top, but instead may cause problems!
     */
    public void setOnTop(boolean onTop) {
        this.onTop = onTop;
    }

    public boolean isToBePutOnTop() {
        return toBePutOnTop;
    }

    public void setToBePutOnTop(boolean toBePutOnTop) {
        this.toBePutOnTop = toBePutOnTop;
    }
    
    protected Rectangle[] getBoxes() {
        Rectangle[] boxes = new Rectangle[4];
        boxes[0] = new Rectangle(getPosition().getX(), getPosition().getY(), dimension.getWidth(), guiBg.getBorderSize());
        boxes[1] = new Rectangle(getPosition().getX(), getPosition().getY(), guiBg.getBorderSize(), dimension.getHeight());
        boxes[2] = new Rectangle(getPosition().getX(), getPosition().getY() + (dimension.getHeight() - guiBg.getBorderSize()), dimension.getWidth(), guiBg.getBorderSize());
        boxes[3] = new Rectangle(getPosition().getX() + (dimension.getWidth() - guiBg.getBorderSize()), getPosition().getY(), guiBg.getBorderSize(), dimension.getHeight());
        return boxes;
    }
}
