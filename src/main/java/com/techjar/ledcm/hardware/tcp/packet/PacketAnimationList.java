
package com.techjar.ledcm.hardware.tcp.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Techjar
 */
public class PacketAnimationList extends Packet {
    private String[] names;
    private String current;

    public PacketAnimationList() {
    }

    public PacketAnimationList(String[] names, String current) {
        this.names = names;
        this.current = current;
    }

    @Override
    public void readData(DataInputStream stream) throws IOException {
        names = new String[stream.readInt()];
        for (int i = 0; i < names.length; i++) {
            names[i] = stream.readUTF();
        }
        current = stream.readUTF();
    }

    @Override
    public void writeData(DataOutputStream stream) throws IOException {
        stream.writeInt(names.length);
        for (String name : names) {
            stream.writeUTF(name);
        }
        stream.writeUTF(current);
    }

    @Override
    public int getRequiredCapabilities() {
        return Capabilities.CONTROL_DATA;
    }

    @Override
    public void process() {
        throw new UnsupportedOperationException();
    }

    public String[] getNames() {
        return names;
    }

    public String getCurrent() {
        return current;
    }
}
