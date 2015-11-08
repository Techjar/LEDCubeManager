
package com.techjar.ledcm;

import com.techjar.ledcm.gui.GUI;
import com.techjar.ledcm.gui.GUIBox;
import com.techjar.ledcm.gui.GUICheckBox;
import com.techjar.ledcm.gui.GUIColorPicker;
import com.techjar.ledcm.gui.GUIComboBox;
import com.techjar.ledcm.gui.GUIComboButton;
import com.techjar.ledcm.gui.GUIRadioButton;
import com.techjar.ledcm.gui.GUISlider;
import com.techjar.ledcm.gui.GUISpinner;
import com.techjar.ledcm.gui.GUITextField;
import com.techjar.ledcm.gui.screen.ScreenMainControl;
import com.techjar.ledcm.hardware.animation.Animation;
import com.techjar.ledcm.hardware.animation.AnimationOption;
import com.techjar.ledcm.hardware.tcp.TCPClient;
import com.techjar.ledcm.hardware.tcp.TCPServer;
import com.techjar.ledcm.hardware.tcp.packet.Packet;
import com.techjar.ledcm.hardware.tcp.packet.PacketAnimationList;
import com.techjar.ledcm.hardware.tcp.packet.PacketAnimationOptionList;
import com.techjar.ledcm.hardware.tcp.packet.PacketSetColorPicker;
import com.techjar.ledcm.util.Util;
import java.io.IOException;
import java.util.List;
import lombok.Getter;

/**
 *
 * @author Techjar
 */
public class ControlUtil {
    private ControlUtil() {
    }

    public static Packet getAnimationOptionsPacket() {
        Animation anim = LEDCubeManager.getLEDCube().getCommThread().getCurrentAnimation();
        AnimationOption[] options = anim.getOptions();
        String[] optionValues = new String[options.length];
        for (int i = 0; i < options.length; i++) {
            optionValues[i] = fetchOptionValue(options[i].getId());
        }
        return new PacketAnimationOptionList(options, optionValues);
    }

    private static String fetchOptionValue(String optionId) {
        ScreenMainControl screen = LEDCubeManager.getInstance().getScreenMainControl();
        List<GUI> components = screen.animOptionsScrollBox.findComponentsByName(optionId);
        if (components.size() > 0) {
            GUI component = components.get(0);
            if (component instanceof GUITextField) {
                return ((GUITextField)component).getText();
            } else if (component instanceof GUISlider) {
                return Float.toString(((GUISlider)component).getValue());
            } else if (component instanceof GUIComboBox) {
                return ((GUIComboBox)component).getSelectedItem().toString();
            } else if (component instanceof GUIComboButton) {
                return ((GUIComboButton)component).getSelectedItem().toString();
            } else if (component instanceof GUIBox) {
                GUIBox box = (GUIBox)component;
                for (GUI gui : box.getAllComponents()) {
                    if (gui instanceof GUIRadioButton) {
                        GUIRadioButton radioButton = (GUIRadioButton)gui;
                        if (radioButton.isSelected()) {
                            return radioButton.getLabel().getText();
                        }
                    }
                }
            } else if (component instanceof GUICheckBox) {
                return Boolean.toString(((GUICheckBox)component).isChecked());
            } else if (component instanceof GUISpinner) {
                return Float.toString(((GUISpinner)component).getValue());
            } else if (component instanceof GUIColorPicker) {
                return Util.colorToString(((GUIColorPicker)component).getValue(), false);
            }
        }
        return "";
    }
}
