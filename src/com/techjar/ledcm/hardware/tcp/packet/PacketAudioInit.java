
package com.techjar.ledcm.hardware.tcp.packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import lombok.Getter;

/**
 *
 * @author Techjar
 */
public class PacketAudioInit extends Packet {
    private AudioFormat format;

    public PacketAudioInit() {
    }

    public PacketAudioInit(AudioFormat format) {
        this.format = format;
    }

    @Override
    public void readData(DataInputStream stream) throws IOException {
        AudioFormat.Encoding encoding = new AudioFormat.Encoding(stream.readUTF());
        float sampleRate = stream.readFloat();
        int sampleSizeInBits = stream.readInt();
        int channels = stream.readUnsignedByte();
        int frameSize = stream.readInt();
        float frameRate = stream.readFloat();
        boolean bigEndian = stream.readBoolean();
        format = new AudioFormat(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);
    }

    @Override
    public void writeData(DataOutputStream stream) throws IOException {
        stream.writeUTF(format.getEncoding().toString());
        stream.writeFloat(format.getSampleRate());
        stream.writeInt(format.getSampleSizeInBits());
        stream.writeByte(format.getChannels());
        stream.writeInt(format.getFrameSize());
        stream.writeFloat(format.getFrameRate());
        stream.writeBoolean(format.isBigEndian());
    }

    @Override
    public int getRequiredCapabilities() {
        return Capabilities.AUDIO_DATA;
    }

    @Override
    public void process() {
        throw new UnsupportedOperationException();
    }

    public AudioFormat getFormat() {
        return format;
    }
}
