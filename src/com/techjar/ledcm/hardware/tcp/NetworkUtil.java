
package com.techjar.ledcm.hardware.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import lombok.SneakyThrows;

/**
 *
 * @author Techjar
 */
public final class NetworkUtil {
    private NetworkUtil() {
    }

    public static void marshalObject(Object obj, DataOutputStream stream) throws IOException {
        if (obj instanceof Byte) {
            stream.writeByte(0);
            stream.writeByte((Byte)obj);
        } else if (obj instanceof Short) {
            stream.writeByte(1);
            stream.writeShort((Short)obj);
        } else if (obj instanceof Integer) {
            stream.writeByte(2);
            stream.writeInt((Integer)obj);
        } else if (obj instanceof Long) {
            stream.writeByte(3);
            stream.writeLong((Long)obj);
        } else if (obj instanceof Float) {
            stream.writeByte(4);
            stream.writeFloat((Float)obj);
        } else if (obj instanceof Double) {
            stream.writeByte(5);
            stream.writeDouble((Double)obj);
        } else if (obj instanceof Boolean) {
            stream.writeByte(6);
            stream.writeBoolean((Boolean)obj);
        } else if (obj instanceof Character) {
            stream.writeByte(7);
            stream.writeChar((Character)obj);
        } else if (obj instanceof String) {
            stream.writeByte(8);
            stream.writeUTF((String)obj);
        } else if (obj instanceof byte[]) {
            stream.writeByte(9);
            stream.writeInt(((byte[])obj).length);
            stream.write((byte[])obj);
        } else {
            throw new IllegalArgumentException(obj.getClass().getName() + " is not marshallable!");
        }
    }

    @SneakyThrows(Exception.class)
    public static Object unmarshalObject(DataInputStream stream) throws IOException {
        int valueType = stream.readUnsignedByte();
        if (valueType == 0) {
            return stream.readUnsignedByte();
        } else if (valueType == 1) {
            return stream.readShort();
        } else if (valueType == 2) {
            return stream.readInt();
        } else if (valueType == 3) {
            return stream.readLong();
        } else if (valueType == 4) {
            return stream.readFloat();
        } else if (valueType == 5) {
            return stream.readDouble();
        } else if (valueType == 6) {
            return stream.readBoolean();
        } else if (valueType == 7) {
            return stream.readChar();
        } else if (valueType == 8) {
            return stream.readUTF();
        } else if (valueType == 9) {
            byte[] bytes = new byte[stream.readInt()];
            stream.readFully(bytes);
            return bytes;
        } else {
            throw new IllegalArgumentException("Unknown value type: " + valueType);
        }
    }
}

