
package com.techjar.ledcm.hardware.tcp.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Techjar
 */
public class PacketAudioData extends Packet {
    private byte[] data;

    public PacketAudioData() {
    }

    public PacketAudioData(byte[] data) {
        this.data = data;
    }

    @Override
    public void readData(DataInputStream stream) throws IOException {
        int length = stream.readShort();
        data = new byte[length];
        stream.readFully(data);
    }

    @Override
    public void writeData(DataOutputStream stream) throws IOException {
        stream.writeShort(data.length);
        stream.write(data);
    }

    @Override
    public int getRequiredCapabilities() {
        return Capabilities.AUDIO_DATA;
    }

    @Override
    public void process() {
        throw new UnsupportedOperationException();
    }

    public byte[] getData() {
        return data;
    }
}
