
package com.techjar.ledcm.hardware.handler;

import java.io.IOException;

/**
 * Abstract class to implement different data ports to the cube.
 * Implementations must be thread-safe, and have a zero-argument constructor.
 *
 * @author Techjar
 */
public interface PortHandler {
    /**
     * Returns whether or not the port is currently in use.
     * @return whether or not port is open
     */
    public boolean isOpened();

    /**
     * Opens the port so data can be sent. Baud rate is not strictly enforced, it's more of a guideline.
     * @param baudRate desired baud rate
     * @throws IOException
     */
    public void open(int baudRate) throws IOException;

    /**
     * Closes the port so data can no longer be sent.
     * @throws IOException
     */
    public void close() throws IOException;

    /**
     * Reads as many bytes as possible (whatever is in the buffer, usually).
     * @return array of bytes read
     * @throws IOException
     */
    public byte[] readBytes() throws IOException;

    /**
     * Blocks until <em>count</em> bytes have been read.
     * @param count number of bytes
     * @return array of bytes read
     * @throws IOException
     */
    public byte[] readBytes(int count) throws IOException;

    /**
     * Blocks until <em>count</em> bytes have been read, or timeout has been exceeded.
     * @param count number of bytes
     * @param timeout timeout in milliseconds
     * @return array of bytes read
     * @throws IOException if an I/O error occurs or timeout is exceeded
     */
    public byte[] readBytes(int count, int timeout) throws IOException;

    /**
     * Write a single byte to the port.
     * @param b byte to write
     * @throws IOException
     */
    public void writeByte(byte b) throws IOException;

    /**
     * Write an array of bytes to the port.
     * @param b byte array to write
     * @throws IOException
     */
    public void writeBytes(byte[] b) throws IOException;
}
