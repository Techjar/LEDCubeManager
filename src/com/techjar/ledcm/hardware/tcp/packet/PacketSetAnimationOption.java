
package com.techjar.ledcm.hardware.tcp.packet;

import com.techjar.ledcm.LEDCubeManager;
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
import com.techjar.ledcm.util.Util;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Techjar
 */
public class PacketSetAnimationOption extends Packet {
    private String optionId;
    private String value;

    public PacketSetAnimationOption() {
    }

    public PacketSetAnimationOption(String optionId, String value) {
        this.optionId = optionId;
        this.value = value;
    }

    @Override
    public void readData(DataInputStream stream) throws IOException {
        optionId = stream.readUTF();
        value = stream.readUTF();
    }

    @Override
    public void writeData(DataOutputStream stream) throws IOException {
        stream.writeUTF(optionId);
        stream.writeUTF(value);
    }

    @Override
    public int getRequiredCapabilities() {
        return Capabilities.CONTROL_DATA;
    }

    @Override
    public void process() {
        ScreenMainControl screen = LEDCubeManager.getInstance().getScreenMainControl();
        List<GUI> components = screen.animOptionsScrollBox.findComponentsByName(optionId);
        if (components.size() > 0) {
            GUI component = components.get(0);
            if (component instanceof GUITextField) {
                ((GUITextField)component).setText(value);
            } else if (component instanceof GUISlider) {
                ((GUISlider)component).setValue(Float.parseFloat(value));
            } else if (component instanceof GUIComboBox) {
                ((GUIComboBox)component).setSelectedItem(value);
            } else if (component instanceof GUIComboButton) {
                ((GUIComboButton)component).setSelectedItem(value);
            } else if (component instanceof GUIBox) {
                GUIBox box = (GUIBox)component;
                for (GUI gui : box.getAllComponents()) {
                    if (gui instanceof GUIRadioButton) {
                        GUIRadioButton radioButton = (GUIRadioButton)gui;
                        if (radioButton.getLabel().getText().equals(value)) {
                            radioButton.setSelected(true);
                        }
                    }
                }
            } else if (component instanceof GUICheckBox) {
                ((GUICheckBox)component).setChecked(Boolean.parseBoolean(value));
            } else if (component instanceof GUISpinner) {
                ((GUISpinner)component).setValue(Float.parseFloat(value));
            } else if (component instanceof GUIColorPicker) {
                ((GUIColorPicker)component).setValue(Util.stringToColor(value));
            } else {
                LEDCubeManager.getLEDCube().getCommThread().getCurrentAnimation().setOption(optionId, value);
            }
        }
    }

    public String getOptionId() {
        return optionId;
    }

    public String getValue() {
        return value;
    }
}
