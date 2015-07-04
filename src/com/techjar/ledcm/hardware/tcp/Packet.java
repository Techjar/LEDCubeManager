
package com.techjar.ledcm.hardware.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Techjar
 */
public class Packet {
    public enum ID {
        CUBE_FRAME,
        AUDIO_DATA,
        AUDIO_INIT,
        VISUAL_FRAME;
    }

    private ID id;
    private byte[] data;

    public Packet(ID id) {
        this.id = id;
    }

    public Packet(int id) {
        this.id = ID.values()[id];
    }

    public ID getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public static Packet readPacket(DataInputStream stream) throws IOException {
        Packet packet = null;
        int id = stream.readUnsignedByte();
        int length = stream.readShort();
        if (id > ID.values().length) throw new IOException(new StringBuilder("Unknown packet ID: ").append(id).toString());
        packet = new Packet(id);
        packet.data = new byte[length];
        stream.readFully(packet.data);
        return packet;
    }

    public static void writePacket(DataOutputStream stream, Packet packet) throws IOException {
        //stream.write(packet.getId().ordinal());
        //stream.writeShort(packet.data.length);
        stream.write(packet.data);
    }
}
