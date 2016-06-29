
package com.techjar.ledcm.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import lombok.SneakyThrows;

/**
 *
 * @author Techjar
 */
public class BufferHelper {
    private BufferHelper() {
    }

    @SneakyThrows(UnsupportedEncodingException.class)
    public static int getStringSize(String str) {
        return str.getBytes("UTF-8").length + 4;
    }

    @SneakyThrows(UnsupportedEncodingException.class)
    public static void putString(ByteBuffer buf, String str) {
        byte[] bytes = str.getBytes("UTF-8");
        buf.putInt(bytes.length);
        buf.put(bytes);
    }

    @SneakyThrows(UnsupportedEncodingException.class)
    public static String getString(ByteBuffer buf) {
        int length = buf.getInt();
        byte[] bytes = new byte[length];
        buf.get(bytes);
        return new String(bytes, "UTF-8");
    }
}
