
package com.techjar.ledcm.hardware;

import java.io.IOException;

/**
 *
 * @author Techjar
 */
public interface PortHandler {
    public boolean isOpened();
    public void open(int baudRate) throws IOException;
    public void close() throws IOException;
    public byte[] readBytes() throws IOException;
    public byte[] readBytes(int count) throws IOException;
    public byte[] readBytes(int count, int timeout) throws IOException;
    public void writeByte(byte b) throws IOException;
    public void writeBytes(byte[] b) throws IOException;
}
