
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.gui.GUI;
import com.techjar.ledcm.gui.GUIAlignment;
import com.techjar.ledcm.gui.GUIBackground;
import com.techjar.ledcm.gui.GUIBox;
import com.techjar.ledcm.gui.GUIButton;
import com.techjar.ledcm.gui.GUICallback;
import com.techjar.ledcm.gui.GUICheckBox;
import com.techjar.ledcm.gui.GUIComboBox;
import com.techjar.ledcm.gui.GUIComboButton;
import com.techjar.ledcm.gui.GUIContainer;
import com.techjar.ledcm.gui.GUILabel;
import com.techjar.ledcm.gui.GUIRadioButton;
import com.techjar.ledcm.gui.GUIScrollBox;
import com.techjar.ledcm.gui.GUISlider;
import com.techjar.ledcm.gui.GUITextField;
import com.techjar.ledcm.gui.screen.ScreenMainControl;
import com.techjar.ledcm.hardware.LEDManager;
import com.techjar.ledcm.util.Dimension3D;
import com.techjar.ledcm.util.Vector3;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public abstract class Animation {
    protected final LEDManager ledManager;
    protected final Dimension3D dimension;
    protected long ticks;

    public Animation() {
        this.ledManager = LEDCubeManager.getLEDManager();
        this.dimension = this.ledManager.getDimensions();
    }

    public abstract String getName();
    public abstract void refresh();
    public abstract void reset();

    public boolean isHidden() {
        return false;
    }
    
    public AnimationOption[] getOptions() {
        return new AnimationOption[0];
    }

    public void optionChanged(String name, String value) {
    }

    public void incTicks() {
        ticks++;
    }

    /*
        TEXT, // 0-1 params: default value
        SLIDER, // 0-3 params: default value, increment, show notches
        COMBOBOX, // 2+ params: default value, items
        COMBOBUTTON, // 2+ params: default value, items
        CHECKBOX, // 0-1 params: default value
        RADIOGROUP, // 2+ params: default value, items (id + name interleaved)
    */
    public final void loadOptions() {
        ScreenMainControl screen = LEDCubeManager.getInstance().getScreenMainControl();
        GUIScrollBox box = screen.animOptionsScrollBox;
        box.setScrollOffset(0, 0);
        box.removeAllComponents();
        AnimationOption[] options = getOptions();
        if (options.length > 0) {
            screen.animOptionsBtn.setEnabled(true);
            int boxWidth = (int)box.getContainerBox().getWidth();
            float position = 5;
            int labelWidth = 0;
            for (AnimationOption option : options) {
                int width = screen.font.getWidth(option.name);
                if (width > labelWidth) labelWidth = width;
            }
            for (final AnimationOption option : options) {
                GUI gui = null;
                switch (option.getType()) {
                    case TEXT:
                        final GUITextField textField = new GUITextField(screen.font, new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
                        gui = textField;
                        if (option.params.length >= 1) textField.setText(option.params[0].toString());
                        textField.setHeight(35);
                        textField.setCanLoseFocus(true);
                        textField.setChangeHandler(new GUICallback() {
                            @Override
                            public void run() {
                                optionChanged(option.id, textField.getText());
                            }
                        });
                        break;
                    case SLIDER:
                        final GUISlider slider = new GUISlider(new Color(255, 0, 0), new Color(50, 50, 50));
                        gui = slider;
                        if (option.params.length >= 1) slider.setValue(Float.parseFloat(option.params[0].toString()));
                        if (option.params.length >= 2) slider.setIncrement(Float.parseFloat(option.params[1].toString()));
                        if (option.params.length >= 3) slider.setShowNotches(Boolean.parseBoolean(option.params[2].toString()));
                        slider.setHeight(30);
                        slider.setChangeHandler(new GUICallback() {
                            @Override
                            public void run() {
                                optionChanged(option.id, Float.toString(slider.getValue()));
                            }
                        });
                        break;
                    case COMBOBOX:
                        final GUIComboBox comboBox = new GUIComboBox(screen.font, new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
                        gui = comboBox;
                        for (int i = 1; i < option.params.length; i++) {
                            comboBox.addItem(option.params[i].toString());
                        }
                        comboBox.setSelectedItem(option.params[0].toString());
                        comboBox.setHeight(35);
                        comboBox.setChangeHandler(new GUICallback() {
                            @Override
                            public void run() {
                                optionChanged(option.id, comboBox.getSelectedItem().toString());
                            }
                        });
                        break;
                    case COMBOBUTTON:
                        final GUIComboButton comboButton = new GUIComboButton(screen.font, new Color(255, 255, 255), new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
                        gui = comboButton;
                        for (int i = 1; i < option.params.length; i++) {
                            comboButton.addItem(option.params[i].toString());
                        }
                        comboButton.setSelectedItem(option.params[0].toString());
                        comboButton.setHeight(35);
                        comboButton.setChangeHandler(new GUICallback() {
                            @Override
                            public void run() {
                                optionChanged(option.id, comboButton.getSelectedItem().toString());
                            }
                        });
                        break;
                    case CHECKBOX:
                        final GUICheckBox checkBox = new GUICheckBox(new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
                        gui = checkBox;
                        if (option.params.length >= 1) checkBox.setChecked(Boolean.parseBoolean(option.params[0].toString()));
                        checkBox.setDimension(30, 30);
                        checkBox.setChangeHandler(new GUICallback() {
                            @Override
                            public void run() {
                                optionChanged(option.id, Boolean.toString(checkBox.isChecked()));
                            }
                        });
                        break;
                    case RADIOGROUP:
                        final GUIBox radioBox = new GUIBox();
                        gui = radioBox;
                        int height = 0;
                        for (int i = 1; i < option.params.length; i += 2) {
                            final GUIRadioButton radioButton = new GUIRadioButton(new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
                            if (option.params[0].toString().equals(option.params[i].toString())) radioButton.setSelected(true);
                            radioButton.setDimension(30, 30);
                            radioButton.setPosition(0, height);
                            final int j = i;
                            radioButton.setSelectHandler(new GUICallback() {
                                @Override
                                public void run() {
                                    optionChanged(option.id, option.params[j].toString());
                                }
                            });
                            GUILabel radioLabel = new GUILabel(screen.font, new Color(255, 255, 255), option.params[i + 1].toString());
                            radioLabel.setDimension(screen.font.getWidth(option.params[i + 1].toString()), 30);
                            radioLabel.setPosition(35, height);
                            radioButton.setLabel(radioLabel);
                            radioBox.addComponent(radioLabel);
                            radioBox.addComponent(radioButton);
                            height += 35;
                        }
                        radioBox.setHeight(height - 5);
                        break;
                    case BUTTON:
                        final GUIButton button = new GUIButton(screen.font, new Color(255, 255, 255), option.params[0].toString(), new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
                        gui = button;
                        button.setHeight(35);
                        button.setClickHandler(new GUICallback() {
                            @Override
                            public void run() {
                                optionChanged(option.id, null);
                            }
                        });
                        break;
                }
                GUILabel label = new GUILabel(screen.font, new Color(255, 255, 255), option.name);
                label.setPosition(5, position + (gui instanceof GUIContainer ? 0 : ((gui.getHeight() - 30) / 2F)));
                label.setDimension(labelWidth, 30);
                if (gui instanceof GUICheckBox) ((GUICheckBox)gui).setLabel(label);
                box.addComponent(label);
                gui.setPosition(labelWidth + 15, position);
                if (!(gui instanceof GUICheckBox)) gui.setWidth(boxWidth - labelWidth - 30);
                box.addComponent(gui);
                position += gui.getHeight() + 5;
            }
        } else {
            screen.animOptionsWindow.setVisible(false);
            screen.animOptionsBtn.setEnabled(false);
        }
    }
}
