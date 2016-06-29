
package com.techjar.ledcm.util.input;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.gui.GUI;
import com.techjar.ledcm.gui.GUIBackground;
import com.techjar.ledcm.gui.GUICallback;
import com.techjar.ledcm.gui.GUIContainer;
import com.techjar.ledcm.gui.GUIInputOption;
import com.techjar.ledcm.gui.GUILabel;
import com.techjar.ledcm.gui.GUIScrollBox;
import com.techjar.ledcm.gui.GUISpacer;
import com.techjar.ledcm.gui.GUITabbed;
import com.techjar.ledcm.gui.screen.ScreenMainControl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class InputBindingManager {
    private static final List<InputBinding> bindings = new ArrayList<>();
    private static boolean configLoaded;
    private static boolean settingsLoaded;

    private InputBindingManager() {
    }

    public static List<InputBinding> getBindings() {
        return Collections.unmodifiableList(bindings);
    }

    public static void addBinding(InputBinding binding) {
        if (bindings.contains(binding)) return;
        for (InputBinding bind : bindings) {
            if (bind.getId().equalsIgnoreCase(binding.getId())) {
                throw new IllegalArgumentException("Binding with ID \"" + binding.getId() + "\" already exists!");
            }
        }
        bindings.add(binding);
        if (configLoaded) loadConfig(binding);
        if (settingsLoaded) setupSettings();
    }

    public static void removeBinding(InputBinding binding) {
        if (!bindings.contains(binding)) return;
        bindings.remove(binding);
        if (settingsLoaded) setupSettings();
    }

    public static void loadAllConfig() {
        for (InputBinding binding : bindings) {
            loadConfig(binding);
        }
        configLoaded = true;
    }

    public static void setupSettings() {
        ScreenMainControl screen = LEDCubeManager.getInstance().getScreenMainControl();
        final GUITabbed tabbed = screen.controlsTabbed;
        tabbed.clear();
        List<String> categories = new ArrayList<>();
        for (InputBinding binding : bindings) {
            if (!categories.contains(binding.getCategory())) {
                categories.add(binding.getCategory());
            }
        }
        final List<GUIContainer> boxes = new ArrayList<>();
        for (String category : categories) {
            GUIScrollBox box = new GUIScrollBox(new Color(255, 0, 0));
            box.setScrollXMode(GUIScrollBox.ScrollMode.DISABLED);
            boxes.add(box);
            tabbed.addTab(category, box);
            box.setScrollYMode(GUIScrollBox.ScrollMode.ENABLED);
            int boxWidth = (int)box.getContainerBox().getWidth();
            box.setScrollYMode(GUIScrollBox.ScrollMode.AUTOMATIC);
            float position = 10;
            int labelWidth = 0;
            for (InputBinding binding : bindings) {
                int width = screen.font.getWidth(binding.getName());
                if (width > labelWidth) labelWidth = width;
            }
            int componentWidth = boxWidth - labelWidth - 20;
            for (InputBinding binding : bindings) {
                if (!binding.getCategory().equals(category)) continue;
                GUIInputOption gui = new GUIInputOption(binding, screen.font, new Color(255, 255, 255), new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
                gui.setDimension(componentWidth, 35);
                gui.setName(binding.getId());
                gui.setPosition(labelWidth + 15, position);
                gui.setChangeHandler(new GUICallback() {
                    @Override
                    public void run() {
                        saveConfig();
                        refreshConflicts();
                    }
                });
                GUILabel label = new GUILabel(screen.font, new Color(255, 255, 255), binding.getName());
                int width = screen.font.getWidth(binding.getName());
                label.setPosition(5 + (labelWidth - width), position + Math.round((gui.getHeight() - 30) / 2F));
                label.setDimension(width, 30);
                box.addComponent(label);
                box.addComponent(gui);
                position += 45;
            }
            GUISpacer spacer = new GUISpacer();
            spacer.setDimension(10, 5);
            spacer.setPosition(0, position - 5);
            box.addComponent(spacer);
        }
        tabbed.setSelectedTab(0);
        refreshConflicts();
        settingsLoaded = true;
    }

    public static void refreshConflicts() {
        List<GUI> components = new ArrayList<>();
        for (GUITabbed.TabInfo tab : LEDCubeManager.getInstance().getScreenMainControl().controlsTabbed.getAllTabs()) {
            components.addAll(tab.getContainer().getAllComponents());
        }
        for (GUI gui : components) {
            if (gui instanceof GUIInputOption) {
                GUIInputOption guiInput = (GUIInputOption)gui;
                guiInput.setColor(new Color(255, 255, 255));
            }
        }
        for (GUI gui : components) {
            if (gui instanceof GUIInputOption) {
                GUIInputOption guiInput = (GUIInputOption)gui;
                for (GUI gui2 : components) {
                    if (gui2 == gui) continue;
                    if (gui2 instanceof GUIInputOption) {
                        GUIInputOption guiInput2 = (GUIInputOption)gui2;
                        if (guiInput.getBinding().getBind() != null && guiInput.getBinding().getBind().equals(guiInput2.getBinding().getBind())) {
                            guiInput.setColor(new Color(64, 0, 0));
                            guiInput2.setColor(new Color(64, 0, 0));
                        }
                    }
                }
            }
        }
    }

    public static void saveConfig() {
        for (InputBinding binding : bindings) {
            LEDCubeManager.getConfig().setProperty("controls." + binding.getId(), binding.getBind() == null ? "" : binding.getBind().toString());
        }
        LEDCubeManager.getConfig().save();
    }

    private static void loadConfig(InputBinding binding) {
        LEDCubeManager.getConfig().defaultProperty("controls." + binding.getId(), binding.getBind().toString());
        binding.setBind(InputInfo.fromString(LEDCubeManager.getConfig().getString("controls." + binding.getId())));
    }
}
