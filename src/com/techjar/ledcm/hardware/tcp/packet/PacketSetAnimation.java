
package com.techjar.ledcm.hardware.tcp.packet;

import com.techjar.ledcm.LEDCubeManager;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Techjar
 */
public class PacketSetAnimation extends Packet {
    private String name;

    public PacketSetAnimation() {
    }

    public PacketSetAnimation(String name) {
        this.name = name;
    }

    @Override
    public void readData(DataInputStream stream) throws IOException {
        name = stream.readUTF();
    }

    @Override
    public void writeData(DataOutputStream stream) throws IOException {
        stream.writeUTF(name);
    }

    @Override
    public int getRequiredCapabilities() {
        return Capabilities.CONTROL_DATA;
    }

    @Override
    public void process() {
        LEDCubeManager.getInstance().getScreenMainControl().animComboBox.setSelectedItem(name);
    }

    public String getName() {
        return name;
    }
}
