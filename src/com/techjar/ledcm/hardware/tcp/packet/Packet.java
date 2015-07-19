
package com.techjar.ledcm.hardware.tcp.packet;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Techjar
 */
public abstract class Packet {
    public enum ID {
        CUBE_FRAME,
        AUDIO_DATA,
        AUDIO_INIT,
        VISUAL_FRAME,
        SET_ANIMATION,
        ANIMATION_LIST,
        SET_COLOR_PICKER,
        ANIMATION_OPTION_LIST,
        SET_ANIMATION_OPTION,
        CLIENT_CAPABILITIES;
    }

    public static class Capabilities {
        private Capabilities() {
        }

        public static final int LED_DATA = 0b1;
        public static final int CONTROL_DATA = 0b10;
        public static final int AUDIO_DATA = 0b100;
        public static final int FRAME_DATA = 0b1000;
    }

    public static final BiMap<ID, Class<? extends Packet>> packetMap = HashBiMap.create();
    static {
        packetMap.put(ID.CUBE_FRAME, PacketCubeFrame.class);
        packetMap.put(ID.AUDIO_DATA, PacketAudioData.class);
        packetMap.put(ID.AUDIO_INIT, PacketAudioInit.class);
        packetMap.put(ID.VISUAL_FRAME, PacketVisualFrame.class);
        packetMap.put(ID.SET_ANIMATION, PacketSetAnimation.class);
        packetMap.put(ID.ANIMATION_LIST, PacketAnimationList.class);
        packetMap.put(ID.SET_COLOR_PICKER, PacketSetColorPicker.class);
        packetMap.put(ID.ANIMATION_OPTION_LIST, PacketAnimationOptionList.class);
        packetMap.put(ID.SET_ANIMATION_OPTION, PacketSetAnimationOption.class);
        packetMap.put(ID.CLIENT_CAPABILITIES, PacketClientCapabilities.class);
    }

    public abstract void readData(DataInputStream stream) throws IOException;
    public abstract void writeData(DataOutputStream stream) throws IOException;
    public abstract int getRequiredCapabilities();
    public abstract void process();

    public ID getId() {
        return packetMap.inverse().get(this.getClass());
    }

    public static Packet readPacket(DataInputStream stream) throws IOException, InstantiationException, IllegalAccessException {
        int id = stream.readUnsignedByte();
        if (id > ID.values().length) throw new IOException(new StringBuilder("Unknown packet ID: ").append(id).toString());
        Class<? extends Packet> cls = packetMap.get(ID.values()[id]);
        Packet packet = cls.newInstance();
        packet.readData(stream);
        return packet;
    }

    public static void writePacket(DataOutputStream stream, Packet packet) throws IOException {
        stream.write(packet.getId().ordinal());
        packet.writeData(stream);
    }
}
