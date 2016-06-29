package com.techjar.ledcm.gui;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.render.RenderHelper;
import com.techjar.ledcm.util.Vector2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.Color;
import org.lwjgl.util.Dimension;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.geom.Rectangle;

/**
 *
 * @author Techjar
 */
public class GUITabbed extends GUI {
    protected UnicodeFont font;
    protected Color color;
    protected GUIBackground guiBg;
    protected GUICallback changeHandler;
    protected List<TabInfo> tabs = new ArrayList<>();
    protected int selectedTab = -1;
    protected int hoveredTab = -1;

    public GUITabbed(UnicodeFont font, Color color, GUIBackground guiBg) {
        this.font = font;
        this.color = color;
        this.guiBg = guiBg;
    }

    @Override
    public boolean processKeyboardEvent() {
        TabInfo tab = getSelectedTab();
        if (tab != null && !tab.getContainer().processKeyboardEvent()) return false;
        return true;
    }

    @Override
    public boolean processMouseEvent() {
        TabInfo tab = getSelectedTab();
        if (tab != null && !tab.getContainer().processMouseEvent()) return false;
        if (Mouse.getEventButtonState()) {
            if (Mouse.getEventButton() == 0) {
                Rectangle[] boxes = getTabBoxes();
                for (int i = 0; i < boxes.length; i++) {
                    if (i == selectedTab) continue;
                    if (checkMouseIntersect(boxes[i])) {
                        LEDCubeManager.getSoundManager().playEffect("ui/click.wav", false);
                        setSelectedTab(i);
                        break;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void update(float delta) {
        TabInfo tab = getSelectedTab();
        if (tab != null) tab.getContainer().update(delta);
        Rectangle[] boxes = getTabBoxes();
        boolean tabIntersected = false;
        for (int i = 0; i < boxes.length; i++) {
            if (checkMouseIntersect(boxes[i])) {
                if (i != hoveredTab) {
                    if (i != selectedTab) LEDCubeManager.getSoundManager().playEffect("ui/rollover.wav", false);
                    hoveredTab = i;
                }
                tabIntersected = true;
                break;
            }
        }
        if (!tabIntersected) hoveredTab = -1;
    }

    @Override
    public void render() {
        RenderHelper.beginScissor((Rectangle)getComponentBox());
        TabInfo tab = getSelectedTab();
        if (tab != null) tab.getContainer().render();
        Rectangle[] boxes = getTabBoxes();
        for (int i = 0; i < boxes.length; i++) {
            Rectangle box = boxes[i];
            RenderHelper.drawSquare(box.getX(), box.getY(), box.getWidth(), box.getHeight(), i == selectedTab ? guiBg.getBorderColor() : (i == hoveredTab ? Util.addColors(guiBg.getBackgroundColor(), new Color(50, 50, 50)) : guiBg.getBackgroundColor()));
            RenderHelper.drawBorder(box.getX(), box.getY(), box.getWidth(), box.getHeight(), guiBg.getBorderSize(), guiBg.getBorderColor(), true, false, true, true);
            RenderHelper.beginScissor(new Rectangle(box.getX() + guiBg.getBorderSize(), box.getY() + guiBg.getBorderSize(), box.getWidth() - (guiBg.getBorderSize() * 2), box.getHeight() - (guiBg.getBorderSize() * 2)));
            font.drawString(box.getX() + guiBg.getBorderSize() + 2, box.getY() + guiBg.getBorderSize(), tabs.get(i).getName(), Util.convertColor(color));
            RenderHelper.endScissor();
        }
        RenderHelper.drawSquare(getPosition().getX(), getPosition().getY() + (getTabHeight() - 2), getWidth(), 2, guiBg.getBorderColor());
        RenderHelper.endScissor();
    }

    @Override
    public Vector2 getContainerPosition() {
        return getPosition().add(new Vector2(0, getTabHeight()));
    }

    @Override
    public void setDimension(Dimension dimension) {
        super.setDimension(dimension);
        for (TabInfo tab : tabs) {
            tab.getContainer().setDimension(dimension.getWidth(), dimension.getHeight() - getTabHeight());
        }
    }

    public int getTabHeight() {
        return 26;
    }

    public int getTabsWidth() {
        Rectangle[] boxes = getTabBoxes();
        Rectangle box = boxes[boxes.length - 1];
        return (int)((box.getX() - getPosition().getX()) + box.getWidth());
    }

    public GUICallback getChangeHandler() {
        return changeHandler;
    }

    public void setChangeHandler(GUICallback changeHandler) {
        this.changeHandler = changeHandler;
    }

    protected Rectangle[] getTabBoxes() {
        Rectangle[] boxes = new Rectangle[tabs.size()];
        int offset = 0;
        for (int i = 0; i < tabs.size(); i++) {
            TabInfo tab = tabs.get(i);
            int width = font.getWidth(tab.getName()) + ((guiBg.getBorderSize() + 2) * 2);
            boxes[i] = new Rectangle(getPosition().getX() + offset, getPosition().getY(), width, getTabHeight() - 2);
            offset += width;
        }
        return boxes;
    }

    public TabInfo getSelectedTab() {
        if (selectedTab < 0 || selectedTab >= tabs.size()) return null;
        return tabs.get(selectedTab);
    }

    public int getSelectedTabIndex() {
        return selectedTab;
    }

    public void setSelectedTab(int selectedTab) {
        if (selectedTab != this.selectedTab) {
            this.selectedTab = selectedTab;
            if (changeHandler != null) {
                changeHandler.setComponent(this);
                changeHandler.run();
            }
        }
    }

    public void setSelectedTab(String name) {
        if (name == null) return;
        for (int i = 0; i < tabs.size(); i++) {
            TabInfo tab = tabs.get(i);
            if (name.equals(tab.getName())) {
                setSelectedTab(i);
                return;
            }
        }
    }

    public TabInfo removeTab(int index) {
        if (index < 0 || index >= tabs.size()) return null;
        if (index == selectedTab) setSelectedTab(-1);
        TabInfo tab = tabs.remove(index);
        tab.getContainer().setParent(null);
        return tab;
    }

    public boolean removeTab(String name) {
        if (name == null) return false;
        Iterator it = tabs.iterator();
        for (int i = 0; it.hasNext(); i++) {
            TabInfo tab = (TabInfo)it.next();
            if (name.equals(tab.getName())) {
                if (i == selectedTab) setSelectedTab(-1);
                tab.getContainer().setParent(null);
                return tabs.remove(tab);
            }
        }
        return false;
    }

    public boolean removeTab(TabInfo tab) {
        if (!tabs.contains(tab)) return false;
        if (tabs.indexOf(tab) == selectedTab) setSelectedTab(-1);
        tab.getContainer().setParent(null);
        return tabs.remove(tab);
    }

    public TabInfo getTab(int index) {
        if (index < 0 || index >= tabs.size()) return null;
        return tabs.get(index);
    }

    public List<TabInfo> getAllTabs() {
        return Collections.unmodifiableList(tabs);
    }

    public void clear() {
        for (TabInfo tab : tabs) {
            tab.getContainer().setParent(null);
        }
        tabs.clear();
    }

    public void addTab(int index, String name, GUIContainer container) {
        if (index <= selectedTab) selectedTab++;
        container.setParent(this);
        container.setDimension(dimension.getWidth(), dimension.getHeight() - getTabHeight());
        tabs.add(index, new TabInfo(name, container));
    }

    public boolean addTab(String name, GUIContainer container) {
        container.setParent(this);
        container.setDimension(dimension.getWidth(), dimension.getHeight() - getTabHeight());
        return tabs.add(new TabInfo(name, container));
    }

    public static class TabInfo {
        private final String name;
        private final GUIContainer container;

        public TabInfo(String name, GUIContainer container) {
            this.name = name;
            this.container = container;
        }

        public GUIContainer getContainer() {
            return container;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TabInfo other = (TabInfo) obj;
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            if (this.container != other.container && (this.container == null || !this.container.equals(other.container))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 61 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 61 * hash + (this.container != null ? this.container.hashCode() : 0);
            return hash;
        }
    }
}
