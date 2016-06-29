package com.techjar.ledcm.gui;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Color;
import org.lwjgl.util.Dimension;
import org.newdawn.slick.UnicodeFont;

/**
 *
 * @author Techjar
 */
public class GUIComboButton extends GUI {
    protected UnicodeFont font;
    protected Color color;
    protected GUIBackground guiBg;
    protected List<Object> items = new ArrayList<>();
    protected GUICallback changeHandler;

    protected int selectedItem = -1;

    public GUIComboButton(UnicodeFont font, Color color, GUIBackground guiBg) {
        this.font = font;
        this.color = color;
        this.guiBg = guiBg;
        if (guiBg != null) guiBg.setParent(this);
    }

    @Override
    public boolean processKeyboardEvent() {
        return true;
    }

    @Override
    public boolean processMouseEvent() {
        if (Mouse.getEventButtonState()) {
            if (checkMouseIntersect(getComponentBox())) {
                if (Mouse.getEventButton() == 0) {
                    LEDCubeManager.getSoundManager().playEffect("ui/click.wav", false);
                    if (++selectedItem >= items.size()) selectedItem = 0;
                    if (changeHandler != null) {
                        changeHandler.setComponent(this);
                        changeHandler.run();
                    }
                }
                else if (Mouse.getEventButton() == 1) {
                    LEDCubeManager.getSoundManager().playEffect("ui/click.wav", false);
                    if (--selectedItem < 0) selectedItem = items.size() - 1;
                    if (changeHandler != null) {
                        changeHandler.setComponent(this);
                        changeHandler.run();
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void update(float delta) {
        if (!Mouse.isButtonDown(0)) {
            if (checkMouseIntersect(getComponentBox())) {
                if (!hovered) LEDCubeManager.getSoundManager().playEffect("ui/rollover.wav", false);
                hovered = true;
            }
            else hovered = false;
        }
    }

    @Override
    public void render() {
        if (guiBg != null) {
            if (hovered) {
                Color color2 = guiBg.getBorderColor(), color3 = guiBg.getBackgroundColor();
                guiBg.setBorderColor(Util.addColors(color2, new Color(50, 50, 50)));
                guiBg.setBackgroundColor(Util.addColors(color3, new Color(50, 50, 50)));
                guiBg.render();
                guiBg.setBorderColor(color2);
                guiBg.setBackgroundColor(color3);
            }
            else guiBg.render();
        }
        if (getSelectedItem() != null) {
            String text = getSelectedItem().toString();
            if (getSelectedItem() != null) font.drawString(getPosition().getX() + ((dimension.getWidth() - font.getWidth(text.toString())) / 2), getPosition().getY() + ((dimension.getHeight() - font.getHeight(text.toString())) / 2), text, Util.convertColor(color));
        }
    }

    @Override
    public void setDimension(Dimension dimension) {
        super.setDimension(dimension);
        if (guiBg != null) guiBg.setDimension(dimension);
    }

    public void setSelectedItem(int selectedItem) {
        if (selectedItem != this.selectedItem) {
            this.selectedItem = MathHelper.clamp(selectedItem, -1, items.size() - 1);
            if (changeHandler != null) {
                changeHandler.setComponent(this);
                changeHandler.run();
            }
        }
    }

    public void setSelectedItem(Object o) {
        setSelectedItem(items.indexOf(o));
    }

    public Object getSelectedItem() {
        if (selectedItem < 0 || selectedItem >= items.size()) return null;
        return items.get(selectedItem);
    }

    public int getSelectedIndex() {
        return selectedItem;
    }

    public boolean addAllItems(int index, Collection<? extends Object> c) {
        if (index <= selectedItem) selectedItem += c.size();
        return items.addAll(index, c);
    }

    public boolean addAllItems(Collection<? extends Object> c) {
        return items.addAll(c);
    }

    public void addItem(int index, Object element) {
        items.add(index, element);
        if (index <= selectedItem) selectedItem++;
    }

    public boolean addItem(String e) {
        return items.add(e);
    }

    public int getItemCount() {
        return items.size();
    }

    public Object removeItem(int index) {
        if (index < selectedItem) selectedItem--;
        else if (index == selectedItem) selectedItem = -1;
        return items.remove(index);
    }

    public boolean removeItem(Object o) {
        if (items.contains(o)) {
            int index = items.indexOf(o);
            if (index < selectedItem) selectedItem--;
            else if (index == selectedItem) selectedItem = -1;
            return items.remove(o);
        }
        return false;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Object getItem(int index) {
        return items.get(index);
    }

    public List<Object> getAllItems() {
        return Collections.unmodifiableList(items);
    }

    public void clearItems() {
        items.clear();
    }

    public GUICallback getChangeHandler() {
        return changeHandler;
    }

    public void setChangeHandler(GUICallback changeHandler) {
        this.changeHandler = changeHandler;
    }
}
