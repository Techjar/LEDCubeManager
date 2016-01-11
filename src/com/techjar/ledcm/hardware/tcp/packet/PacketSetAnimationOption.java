
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
import com.techjar.ledcm.hardware.animation.Animation;
import com.techjar.ledcm.hardware.animation.AnimationOption;
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
        Animation anim = LEDCubeManager.getLEDCube().getCommThread().getCurrentAnimation();
        if (anim != null) {
            for (AnimationOption option : anim.getOptions()) {
                if (option.getId().equals(optionId)) {
                    Util.setOptionInGUI(option, value);
                    break;
                }
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
