
package com.techjar.cubedesigner.hardware;

import com.techjar.cubedesigner.CubeDesigner;
import com.techjar.cubedesigner.hardware.animation.Animation;
import jssc.SerialPort;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

/**
 *
 * @author Techjar
 */
public class SerialThread extends Thread {
    private final Object lock = new Object();
    private final LEDManager ledManager;
    private long updateTime;
    @Getter @Setter private int refreshRate = 60;
    @Getter private Animation currentAnimation;
    private SerialPort port;

    public SerialThread() {
        this.setName("Animation / Serial Writer Thread");
        ledManager = CubeDesigner.getLEDManager();
        updateTime = System.nanoTime();
        port = new SerialPort(CubeDesigner.getSerialPortName());
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
    }

    @Override
    public void run() {
        while (true) {
            long interval = 1000000000 / refreshRate;
            if (System.nanoTime() - updateTime >= interval) {
                updateTime = System.nanoTime();
                synchronized (lock) {
                    if (currentAnimation != null) currentAnimation.refresh();
                    try {
                        if (port.isOpened()) {
                            port.writeBytes(ledManager.getConcatenatedArray());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        closePort();
                    }
                }
            }
        }
    }

    public void setCurrentAnimation(Animation currentAnimation) {
        synchronized (lock) {
            this.currentAnimation = currentAnimation;
            if (currentAnimation != null) currentAnimation.reset();
        }
    }

    public void openPort() {
        synchronized (lock) {
            if (!port.isOpened()) {
                try {
                    port.openPort();
                    port.setParams(1000000, 8, 1, 0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void closePort() {
        synchronized (lock) {
            if (port.isOpened()) {
                try {
                    port.closePort();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public boolean isPortOpen() {
        return port.isOpened();
    }

    private class ShutdownThread extends Thread {
        @Override
        public void run() {
            closePort();
        }
    }
}
