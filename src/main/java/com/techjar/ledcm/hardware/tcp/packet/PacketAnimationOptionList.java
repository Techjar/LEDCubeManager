
package com.techjar.ledcm.hardware.tcp.packet;

import com.techjar.ledcm.hardware.animation.AnimationOption;
import com.techjar.ledcm.hardware.tcp.NetworkUtil;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Techjar
 */
public class PacketAnimationOptionList extends Packet {
    private AnimationOption[] options;
    private String[] values;

    public PacketAnimationOptionList() {
    }

    public PacketAnimationOptionList(AnimationOption[] options, String[] values) {
        this.options = options;
        this.values = values;
    }

    @Override
    public void readData(DataInputStream stream) throws IOException {
        options = new AnimationOption[stream.readInt()];
        values = new String[options.length];
        for (int i = 0; i < options.length; i++) {
            String id = stream.readUTF();
            String name = stream.readUTF();
            AnimationOption.OptionType type = AnimationOption.OptionType.values()[stream.readUnsignedByte()];
            Object[] params = new Object[stream.readUnsignedByte()];
            for (int j = 0; j < params.length; j++) {
                params[j] = NetworkUtil.unmarshalObject(stream);
            }
            options[i] = new AnimationOption(id, name, type, params);
            values[i] = stream.readUTF();
        }
    }

    @Override
    public void writeData(DataOutputStream stream) throws IOException {
        stream.writeInt(options.length);
        for (int i = 0; i < options.length; i++) {
            AnimationOption option = options[i];
            stream.writeUTF(option.getId());
            stream.writeUTF(option.getName());
            stream.writeByte(option.getType().ordinal());
            stream.writeByte(option.getParams().length);
            for (Object obj : option.getParams()) {
                NetworkUtil.marshalObject(obj, stream);
            }
            stream.writeUTF(values[i]);
        }
    }

    @Override
    public int getRequiredCapabilities() {
        return Capabilities.CONTROL_DATA;
    }

    @Override
    public void process() {
        throw new UnsupportedOperationException();
    }

    public AnimationOption[] getOptions() {
        return options;
    }

    public String[] getValues() {
        return values;
    }
}
