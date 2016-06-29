
package com.techjar.ledcm.hardware.tcp.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Techjar
 */
public class PacketVisualFrame extends Packet {
    private int width;
    private int height;
    private byte[] data;

    public PacketVisualFrame() {
    }

    public PacketVisualFrame(byte[] data) {
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
        return Capabilities.FRAME_DATA;
    }

    @Override
    public void process() {
        throw new UnsupportedOperationException();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public byte[] getData() {
        return data;
    }
}
