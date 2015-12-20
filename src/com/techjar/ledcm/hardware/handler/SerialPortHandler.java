
package com.techjar.ledcm.hardware.handler;

import com.techjar.ledcm.LEDCubeManager;
import java.io.IOException;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

/**
 *
 * @author Techjar
 */
public class SerialPortHandler implements PortHandler {
    private final SerialPort port;

    public SerialPortHandler() {
        port = new SerialPort(LEDCubeManager.getSerialPortName());
    }

    @Override
    public boolean isOpened() {
        return port.isOpened();
    }

    @Override
    public void open(int baudRate) throws IOException {
        try {
            port.openPort();
            port.setParams(baudRate, 8, 1, 0);
        } catch (SerialPortException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            port.closePort();
        } catch (SerialPortException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public byte[] readBytes() throws IOException {
        try {
            return port.readBytes();
        } catch (SerialPortException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public byte[] readBytes(int count) throws IOException {
        try {
            return port.readBytes(count);
        } catch (SerialPortException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public byte[] readBytes(int count, int timeout) throws IOException {
        try {
            return port.readBytes(count, timeout);
        } catch (SerialPortException | SerialPortTimeoutException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void writeByte(byte b) throws IOException {
        try {
            port.writeByte(b);
        } catch (SerialPortException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void writeBytes(byte[] b) throws IOException {
        try {
            port.writeBytes(b);
        } catch (SerialPortException ex) {
            throw new IOException(ex);
        }
    }
}
