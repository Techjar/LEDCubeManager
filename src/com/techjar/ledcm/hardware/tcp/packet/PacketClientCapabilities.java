
package com.techjar.ledcm.hardware.tcp.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Techjar
 */
public class PacketClientCapabilities extends Packet {
    private int value;

    public PacketClientCapabilities() {
    }

    public PacketClientCapabilities(int value) {
        this.value = value;
    }

    @Override
    public void readData(DataInputStream stream) throws IOException {
        value = stream.readInt();
    }

    @Override
    public void writeData(DataOutputStream stream) throws IOException {
        stream.writeInt(value);
    }

    @Override
    public int getRequiredCapabilities() {
        return 0;
    }

    @Override
    public void process() {
        throw new UnsupportedOperationException();
    }

    public int getValue() {
        return value;
    }
}
