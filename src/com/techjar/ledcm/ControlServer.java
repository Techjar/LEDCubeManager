
package com.techjar.ledcm;

import com.techjar.ledcm.gui.GUI;
import com.techjar.ledcm.gui.GUIBox;
import com.techjar.ledcm.gui.GUICheckBox;
import com.techjar.ledcm.gui.GUIComboBox;
import com.techjar.ledcm.gui.GUIComboButton;
import com.techjar.ledcm.gui.GUIRadioButton;
import com.techjar.ledcm.gui.GUISlider;
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
import java.io.IOException;
import java.util.List;
import lombok.Getter;

/**
 *
 * @author Techjar
 */
public class ControlServer {
    @Getter private TCPServer tcpServer;

    public ControlServer(int port) throws IOException {
        tcpServer = new TCPServer(port);
        tcpServer.setConnectHanlder(new TCPServer.ConnectHandler() {
            @Override
            public boolean onClientConnected(TCPClient client) {
                ScreenMainControl screen = LEDCubeManager.getInstance().getScreenMainControl();
                client.queuePacket(new PacketAnimationList(LEDCubeManager.getLEDCube().getAnimationNames().toArray(new String[0]), LEDCubeManager.getLEDCube().getCommThread().getCurrentAnimation().getName()));
                client.queuePacket(new PacketSetColorPicker(screen.redColorSlider.getValue(), screen.greenColorSlider.getValue(), screen.blueColorSlider.getValue()));
                sendAnimationOptions();
                return true;
            }
        });
    }

    public void sendPacket(Packet packet) {
        tcpServer.sendPacket(packet);
    }

    public void sendAnimationOptions() {
        Animation anim = LEDCubeManager.getLEDCube().getCommThread().getCurrentAnimation();
        AnimationOption[] options = anim.getOptions();
        String[] optionValues = new String[options.length];
        for (int i = 0; i < options.length; i++) {
            optionValues[i] = fetchOptionValue(options[i].getId());
        }
        sendPacket(new PacketAnimationOptionList(options, optionValues));
    }

    private String fetchOptionValue(String optionId) {
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
            }
        }
        return "";
    }
}
