
package com.techjar.ledcm.hardware.animation;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.gui.GUI;
import com.techjar.ledcm.gui.GUIAlignment;
import com.techjar.ledcm.gui.GUIBackground;
import com.techjar.ledcm.gui.GUIBox;
import com.techjar.ledcm.gui.GUIButton;
import com.techjar.ledcm.gui.GUICallback;
import com.techjar.ledcm.gui.GUICheckBox;
import com.techjar.ledcm.gui.GUIColorPicker;
import com.techjar.ledcm.gui.GUIComboBox;
import com.techjar.ledcm.gui.GUIComboButton;
import com.techjar.ledcm.gui.GUIContainer;
import com.techjar.ledcm.gui.GUILabel;
import com.techjar.ledcm.gui.GUIRadioButton;
import com.techjar.ledcm.gui.GUIScrollBox;
import com.techjar.ledcm.gui.GUISlider;
import com.techjar.ledcm.gui.GUISpacer;
import com.techjar.ledcm.gui.GUISpinner;
import com.techjar.ledcm.gui.GUITextField;
import com.techjar.ledcm.gui.screen.ScreenMainControl;
import com.techjar.ledcm.hardware.manager.LEDManager;
import com.techjar.ledcm.util.Dimension3D;
import com.techjar.ledcm.util.Util;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

/**
 *
 * @author Techjar
 */
public abstract class Animation {
    protected final LEDManager ledManager;
    protected final Dimension3D dimension;
    private final Map<String, String> optionValues;
    protected long ticks;

    public Animation() {
        this.ledManager = LEDCubeManager.getLEDCube().getLEDManager();
        this.dimension = this.ledManager.getDimensions();
        this.optionValues = new HashMap<>();
    }

    public abstract String getName();
    public abstract void refresh();
    public abstract void reset();

    public boolean isHidden() {
        return false;
    }

    /**
     * Used for "finishwait" sequence command. You can override this if you want to return a not-finished state
     * until your animation is "finished". Examples are an animation loops, but "finishes" after one cycle, or one-off animations that don't loop.
     */
    public boolean isFinished() {
        return false;
    }

    public void incTicks() {
        ticks++;
    }
    
    public AnimationOption[] getOptions() {
        return new AnimationOption[0];
    }

    public void optionChanged(String name, String value) {
    }

    public final String getOption(String name, String value) {
        return optionValues.get(name);
    }

    public final void setOption(String name, String value) {
        optionChanged(name, value);
        if (optionValues.containsKey(name)) {
            optionValues.put(name, value);
            LEDCubeManager.getConfig().setProperty("animoptions." + getClass().getSimpleName().substring(9).toLowerCase() + "." + name, value);
        }
    }

    /**
     * Only intended to be called by animation loading routine, after all animations have been constructed
     */
    public final void postLoadInitOptions() {
        AnimationOption[] options = getOptions();
        for (final AnimationOption option : options) {
            if (option.getType() != AnimationOption.OptionType.BUTTON) {
                String property = "animoptions." + getClass().getSimpleName().substring(9).toLowerCase() + "." + option.getId();
                LEDCubeManager.getConfig().defaultProperty(property, option.getParams()[0] instanceof Color ? Util.colorToString((Color)option.getParams()[0], false) : option.getParams()[0].toString());
                optionValues.put(option.getId(), LEDCubeManager.getConfig().getString(property));
            }
        }
        for (Map.Entry<String, String> entry : optionValues.entrySet()) {
            optionChanged(entry.getKey(), entry.getValue());
        }
    }

    public final void loadOptions() {
        ScreenMainControl screen = LEDCubeManager.getInstance().getScreenMainControl();
        GUIScrollBox box = screen.animOptionsScrollBox;
        box.setScrollOffset(0, 0);
        box.removeAllComponents();
        AnimationOption[] options = getOptions();
        if (options.length > 0) {
            screen.animOptionsBtn.setEnabled(true);
            box.setScrollYMode(GUIScrollBox.ScrollMode.ENABLED);
            int boxWidth = (int)box.getContainerBox().getWidth();
            box.setScrollYMode(GUIScrollBox.ScrollMode.AUTOMATIC);
            float position = 5;
            int labelWidth = 0;
            for (AnimationOption option : options) {
                int width = screen.font.getWidth(option.name);
                if (width > labelWidth) labelWidth = width;
            }
            int componentWidth = boxWidth - labelWidth - 20;
            for (final AnimationOption option : options) {
                GUI gui = null;
                switch (option.getType()) {
                    case TEXT:
                        final GUITextField textField = new GUITextField(screen.font, new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
                        gui = textField;
                        textField.setText(optionValues.get(option.getId()));
                        if (option.params.length >= 2) textField.setValidationRegex(option.params[1].toString());
                        if (option.params.length >= 3) textField.setMaxLength(Integer.parseInt(option.params[2].toString()));
                        textField.setHeight(35);
                        textField.setCanLoseFocus(true);
                        textField.setChangeHandler(new GUICallback() {
                            @Override
                            public void run() {
                                setOption(option.id, textField.getText());
                            }
                        });
                        break;
                    case SLIDER:
                        final GUISlider slider = new GUISlider(new Color(255, 0, 0), new Color(50, 50, 50));
                        gui = slider;
                        slider.setValue(Float.parseFloat(optionValues.get(option.getId())));
                        if (option.params.length >= 2) slider.setIncrement(Float.parseFloat(option.params[1].toString()));
                        if (option.params.length >= 3) slider.setShowNotches(Boolean.parseBoolean(option.params[2].toString()));
                        slider.setHeight(30);
                        slider.setChangeHandler(new GUICallback() {
                            @Override
                            public void run() {
                                setOption(option.id, Float.toString(slider.getValue()));
                            }
                        });
                        break;
                    case COMBOBOX:
                        final GUIComboBox comboBox = new GUIComboBox(screen.font, new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
                        gui = comboBox;
                        String selected = null;
                        for (int i = 1; i < option.params.length; i += 2) {
                            comboBox.addItem(option.params[i + 1].toString());
                            if (option.params[i].toString().equals(optionValues.get(option.getId()))) selected = option.params[i + 1].toString();
                        }
                        comboBox.setSelectedItem(selected);
                        comboBox.setHeight(35);
                        comboBox.setChangeHandler(new GUICallback() {
                            @Override
                            public void run() {
                                setOption(option.id, comboBox.getSelectedItem() != null ? option.params[comboBox.getSelectedIndex() * 2 + 1].toString() : null);
                            }
                        });
                        break;
                    case COMBOBUTTON:
                        final GUIComboButton comboButton = new GUIComboButton(screen.font, new Color(255, 255, 255), new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
                        gui = comboButton;
                        selected = null;
                        for (int i = 1; i < option.params.length; i += 2) {
                            comboButton.addItem(option.params[i + 1].toString());
                            if (option.params[i].toString().equals(optionValues.get(option.getId()))) selected = option.params[i + 1].toString();
                        }
                        comboButton.setSelectedItem(selected);
                        comboButton.setHeight(35);
                        comboButton.setChangeHandler(new GUICallback() {
                            @Override
                            public void run() {
                                setOption(option.id, comboButton.getSelectedItem() != null ? option.params[comboButton.getSelectedIndex() * 2 + 1].toString() : null);
                            }
                        });
                        break;
                    case CHECKBOX:
                        final GUICheckBox checkBox = new GUICheckBox(new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
                        gui = checkBox;
                        checkBox.setChecked(Boolean.parseBoolean(optionValues.get(option.getId())));
                        checkBox.setDimension(30, 30);
                        checkBox.setChangeHandler(new GUICallback() {
                            @Override
                            public void run() {
                                setOption(option.id, Boolean.toString(checkBox.isChecked()));
                            }
                        });
                        break;
                    case RADIOGROUP:
                        final GUIBox radioBox = new GUIBox();
                        gui = radioBox;
                        int height = 0;
                        int xPos = 0;
                        for (int i = 1; i < option.params.length; i += 2) {
                            final GUIRadioButton radioButton = new GUIRadioButton(new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
                            if (option.params[0].toString().equals(optionValues.get(option.getId()))) radioButton.setSelected(true);
                            radioButton.setDimension(30, 30);
                            radioButton.setName(option.params[i].toString());
                            final int j = i;
                            radioButton.setSelectHandler(new GUICallback() {
                                @Override
                                public void run() {
                                    setOption(option.id, option.params[j].toString());
                                }
                            });
                            GUILabel radioLabel = new GUILabel(screen.font, new Color(255, 255, 255), option.params[i + 1].toString());
                            int textWidth = screen.font.getWidth(option.params[i + 1].toString());
                            if (xPos > 0 && xPos + 35 + textWidth > componentWidth - 1) {
                                xPos = 0;
                                height += 35;
                            }
                            radioLabel.setDimension(textWidth, 30);
                            radioLabel.setPosition(xPos + 35, height);
                            radioButton.setPosition(xPos, height);
                            radioButton.setLabel(radioLabel);
                            radioBox.addComponent(radioLabel);
                            radioBox.addComponent(radioButton);
                            xPos += 35 + textWidth + 10;
                        }
                        radioBox.setHeight(height + 35);
                        break;
                    case BUTTON:
                        final GUIButton button = new GUIButton(screen.font, new Color(255, 255, 255), option.params[0].toString(), new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
                        gui = button;
                        button.setHeight(35);
                        button.setClickHandler(new GUICallback() {
                            @Override
                            public void run() {
                                setOption(option.id, null);
                            }
                        });
                        break;
                    case SPINNER:
                        final GUISpinner spinner = new GUISpinner(screen.font, new Color(255, 255, 255), new GUIBackground(new Color(0, 0, 0), new Color(255, 0, 0), 2));
                        gui = spinner;
                        spinner.setHeight(35);
                        spinner.setMinValue(Float.parseFloat(option.params[1].toString()));
                        spinner.setMaxValue(Float.parseFloat(option.params[2].toString()));
                        spinner.setIncrement(Float.parseFloat(option.params[3].toString()));
                        spinner.setDecimalPlaces(Integer.parseInt(option.params[4].toString()));
                        spinner.setValue(Float.parseFloat(option.params[0].toString()));
                        spinner.setChangeHandler(new GUICallback() {
                            @Override
                            public void run() {
                                setOption(option.id, Float.toString(spinner.getValue()));
                            }
                        });
                        break;
                    case COLORPICKER:
                        final GUIColorPicker colorPicker = new GUIColorPicker(new Color(50, 50, 50));
                        gui = colorPicker;
                        colorPicker.setHeight(30);
                        colorPicker.setValue((ReadableColor)option.params[0]);
                        colorPicker.setChangeHandler(new GUICallback() {
                            @Override
                            public void run() {
                                setOption(option.id, Util.colorToString(colorPicker.getValue(), false));
                            }
                        });
                        break;
                }
                GUILabel label = new GUILabel(screen.font, new Color(255, 255, 255), option.name);
                int width = screen.font.getWidth(option.name);
                label.setPosition(5 + (labelWidth - width), position + (gui instanceof GUIContainer ? 0 : Math.round((gui.getHeight() - 30) / 2F)));
                label.setDimension(width, 30);
                if (gui instanceof GUICheckBox) ((GUICheckBox)gui).setLabel(label);
                box.addComponent(label);
                gui.setName(option.id);
                gui.setPosition(labelWidth + 15, position);
                if (!(gui instanceof GUICheckBox)) gui.setWidth(componentWidth);
                box.addComponent(gui);
                position += gui.getHeight() + 5;
            }
            final GUIButton resetButton = new GUIButton(screen.font, new Color(255, 255, 255), "Reset to Defaults", new GUIBackground(new Color(255, 0, 0), new Color(50, 50, 50), 2));
            resetButton.setName("resetdefaults_button");
            resetButton.setDimension(300, 35);
            resetButton.setPosition(0, position);
            resetButton.setParentAlignment(GUIAlignment.TOP_CENTER);
            resetButton.setClickHandler(new GUICallback() {
                @Override
                public void run() {
                    try {
                        Animation anim = Animation.this.getClass().newInstance();
                        AnimationOption[] options = anim.getOptions();
                        for (AnimationOption option : options) {
                            if (option.getType() != AnimationOption.OptionType.BUTTON) {
                                String value = option.getParams()[0].toString();
                                if (option.getType() == AnimationOption.OptionType.COLORPICKER) {
                                    value = Util.colorToString((ReadableColor)option.getParams()[0], false);
                                }
                                Util.setOptionInGUI(option, value);
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
            box.addComponent(resetButton);
            position += resetButton.getHeight() + 5;
            final GUISpacer spacer = new GUISpacer();
            spacer.setDimension(10, 5);
            spacer.setPosition(0, position - 5);
            box.addComponent(spacer);
        } else {
            screen.animOptionsWindow.setVisible(false);
            screen.animOptionsBtn.setEnabled(false);
        }
    }
}
