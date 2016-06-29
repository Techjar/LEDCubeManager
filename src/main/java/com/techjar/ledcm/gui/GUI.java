package com.techjar.ledcm.gui;

import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.Vector2;
import com.techjar.ledcm.util.logging.LogHelper;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.input.Controller;
import org.lwjgl.util.Dimension;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Shape;

/**
 *
 * @author Techjar
 */
public abstract class GUI {
    protected Vector2 position = new Vector2();
    protected Dimension dimension = new Dimension();
    protected Rectangle baseBox = new Rectangle(0, 0, 0, 0);
    protected GUICallback dimensionChangeHandler;
    protected GUICallback positionChangeHandler;
    protected GUICallback removeHandler;
    protected GUIAlignment parentAlign = GUIAlignment.TOP_LEFT;
    protected GUI parent;
    protected String name;
    protected boolean visible = true;
    protected boolean enabled = true;
    protected boolean removeRequested;
    protected boolean hovered;
    
    public abstract boolean processKeyboardEvent();
    public abstract boolean processMouseEvent();
    public abstract void update(float delta);
    public abstract void render();

    public boolean processControllerEvent(Controller controller) {
        return true;
    }

    /**
     * Returns the position of this component relative to it's parent.
     * For the non-relative position, use {@link #getRawPosition}.
     *
     * @return The position of this component as a {@link Vector2}
     */
    protected Vector2 getPosition() {
        if (parent != null) {
            Vector2 parentPos = this instanceof GUIBackground || (this instanceof GUIButton && ((GUIButton)this).windowClose) ? parent.getPosition() : parent.getContainerPosition();
            Dimension parentDim = parent.getDimension();
            switch (parentAlign) {
                case TOP_LEFT:
                    return position.add(parentPos);
                case TOP_RIGHT:
                    return new Vector2(position.getX() + parentPos.getX() + parentDim.getWidth() - dimension.getWidth(), position.getY() + parentPos.getY());
                case BOTTOM_LEFT:
                    return new Vector2(position.getX() + parentPos.getX(), position.getY() + parentPos.getY() + parentDim.getHeight() - dimension.getHeight());
                case BOTTOM_RIGHT:
                    return new Vector2(position.getX() + parentPos.getX() + parentDim.getWidth() - dimension.getWidth(), position.getY() + parentPos.getY() + parentDim.getHeight() - dimension.getHeight());
                case TOP_CENTER:
                    return new Vector2(position.getX() + parentPos.getX() + (parentDim.getWidth() / 2) - (dimension.getWidth() / 2), position.getY() + parentPos.getY());
                case BOTTOM_CENTER:
                    return new Vector2(position.getX() + parentPos.getX() + (parentDim.getWidth() / 2) - (dimension.getWidth() / 2), position.getY() + parentPos.getY() + parentDim.getHeight() - dimension.getHeight());
                case LEFT_CENTER:
                    return new Vector2(position.getX() + parentPos.getX(), position.getY() + parentPos.getY() + (parentDim.getHeight() / 2) - (dimension.getHeight() / 2));
                case RIGHT_CENTER:
                    return new Vector2(position.getX() + parentPos.getX() + parentDim.getWidth() - dimension.getWidth(), position.getY() + parentPos.getY() + (parentDim.getHeight() / 2) - (dimension.getHeight() / 2));
                case CENTER:
                    return new Vector2(position.getX() + parentPos.getX() + (parentDim.getWidth() / 2) - (dimension.getWidth() / 2), position.getY() + parentPos.getY() + (parentDim.getHeight() / 2) - (dimension.getHeight() / 2));
                default:
                    throw new RuntimeException("Illegal value for parentAlign");
            }
        }
        return position.copy();
    }
    
    public Vector2 getContainerPosition() {
        return getPosition();
    }
    
    /**
     * Returns the position of this component as set by {@link #setPosition}.
     * For the non-relative position, use {@link #getPosition}.
     * 
     * @return The position of this component as a {@link Vector2}
     */
    public Vector2 getRawPosition() {
        return position.copy();
    }
    
    public void setPosition(Vector2 position) {
        this.position.set(position);
        if (positionChangeHandler != null) {
            positionChangeHandler.setComponent(this);
            positionChangeHandler.run();
        }
    }
    
    public void setPosition(float x, float y) {
        setPosition(new Vector2(x, y));
    }

    public float getX() {
        return getPosition().getX();
    }

    public void setX(float x) {
        setPosition(x, position.getY());
    }

    public float getY() {
        return getPosition().getY();
    }

    public void setY(float y) {
        setPosition(position.getX(), y);
    }

    public Dimension getDimension() {
        return new Dimension(dimension);
    }

    public void setDimension(Dimension dimension) {
        this.dimension.setSize(dimension);
        if (dimensionChangeHandler != null) {
            dimensionChangeHandler.setComponent(this);
            dimensionChangeHandler.run();
        }
    }
    
    public void setDimension(int width, int height) {
        setDimension(new Dimension(width, height));
    }

    public int getWidth() {
        return dimension.getWidth();
    }

    public void setWidth(int width) {
        setDimension(width, dimension.getHeight());
    }

    public int getHeight() {
        return dimension.getHeight();
    }

    public void setHeight(int height) {
        setDimension(dimension.getWidth(), height);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public GUICallback getPositionChangeHandler() {
        return positionChangeHandler;
    }

    public void setPositionChangeHandler(GUICallback positionChangeHandler) {
        this.positionChangeHandler = positionChangeHandler;
    }

    public GUICallback getDimensionChangeHandler() {
        return dimensionChangeHandler;
    }

    public void setDimensionChangeHandler(GUICallback dimensionChangeHandler) {
        this.dimensionChangeHandler = dimensionChangeHandler;
    }

    public GUICallback getRemoveHandler() {
        return removeHandler;
    }

    public void setRemoveHandler(GUICallback removeHandler) {
        this.removeHandler = removeHandler;
    }
    
    public boolean checkMouseIntersect(boolean checkParentContainerBox, boolean checkContainerBox, Shape... boxes) {
        Shape mouseBox = Util.getMouseHitbox();
        boolean intersect1 = true, intersect2 = true;
        if (checkContainerBox) {
            Shape cBox = getContainerBox();
            if (cBox != null) intersect1 = cBox.intersects(mouseBox);
        }
        if (intersect1 && parent != null) intersect2 = parent.checkMouseIntersect(true, checkParentContainerBox);
        if (!intersect1 || !intersect2) return false;
        List<GUI> guiList = getContainerList();
        int thisIndex = guiList.indexOf(this);
        if (thisIndex > -1) {
            for (int i = 0; i < guiList.size(); i++) {
                if (i <= thisIndex) continue;
                GUI gui = guiList.get(i);
                if (gui.isVisible() && gui.getComponentBox().intersects(mouseBox)) return false;
            }
        }
        if (boxes.length > 1) {
            for (Shape box : boxes) {
                if (!box.intersects(mouseBox)) return false;
            }
            return true;
        }
        if (boxes.length < 1) return true;
        return boxes[0].intersects(mouseBox);
    }
    
    public boolean checkMouseIntersect(boolean checkParentContainerBox, Shape... boxes) {
        return checkMouseIntersect(checkParentContainerBox, false, boxes);
    }
    
    public boolean checkMouseIntersect(Shape... boxes) {
        return checkMouseIntersect(true, false, boxes);
    }

    public boolean checkWithinContainer(Shape... boxes) {
        if (parent == null || !(parent instanceof GUIContainer)) return true;
        if (boxes.length > 1) {
            for (Shape box : boxes) {
                if (!parent.getContainerBox().contains(box)) return false;
            }
            return true;
        }
        if (boxes.length < 1) return parent.getContainerBox().contains(getComponentBox());
        return parent.getContainerBox().contains(boxes[0]);
    }

    public List<GUI> getContainerList() {
        if (parent != null && parent instanceof GUIContainer) return ((GUIContainer)parent).getAllComponents();
        return new ArrayList<>();
    }
    
    public Rectangle getContainerBox() {
        return null;
    }
    
    public Shape getComponentBox() {
        Vector2 pos = getPosition();
        baseBox.setLocation(pos.getX(), pos.getY());
        baseBox.setSize(dimension.getWidth(), dimension.getHeight());
        return baseBox;
    }

    public GUI getParent() {
        return parent;
    }

    public void setParent(GUI parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GUIAlignment getParentAlignment() {
        return parentAlign;
    }

    public void setParentAlignment(GUIAlignment parentAlign) {
        this.parentAlign = parentAlign;
    }
    
    public void remove() {
        this.removeRequested = true;
        if (removeHandler != null) {
            removeHandler.setComponent(this);
            removeHandler.run();
        }
    }

    public boolean isRemoveRequested() {
        return removeRequested;
    }
}
