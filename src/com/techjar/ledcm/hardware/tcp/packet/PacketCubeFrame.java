
package com.techjar.ledcm.hardware.tcp.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Techjar
 */
public class PacketCubeFrame extends Packet {
    private byte[] data;

    public PacketCubeFrame() {
    }

    public PacketCubeFrame(byte[] data) {
        this.data = data;
    }

    @Override
    public void readData(DataInputStream stream) throws IOException {
        int length = stream.readInt();
        data = new byte[length];
        stream.readFully(data);
    }

    @Override
    public void writeData(DataOutputStream stream) throws IOException {
        stream.writeInt(data.length);
        stream.write(data);
    }

    @Override
    public int getRequiredCapabilities() {
        return Capabilities.LED_DATA;
    }

    @Override
    public void process() {
        throw new UnsupportedOperationException();
    }

    public byte[] getData() {
        return data;
    }
}
