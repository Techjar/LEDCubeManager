
package com.techjar.ledcm.hardware;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.animation.Animation;
import com.techjar.ledcm.hardware.animation.AnimationSequence;
import com.techjar.ledcm.hardware.tcp.Packet;
import com.techjar.ledcm.hardware.tcp.TCPServer;
import com.techjar.ledcm.util.Constants;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.Timer;
import java.io.IOException;
import jssc.SerialPort;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

/**
 *
 * @author Techjar
 */
public class CommThread extends Thread {
    private final Object lock = new Object();
    private final LEDManager ledManager;
    private long updateTime;
    private long ticks;
    @Getter @Setter private int refreshRate = 60;
    @Getter private Animation currentAnimation;
    @Getter private AnimationSequence currentSequence;
    private SerialPort port;
    @Getter private TCPServer tcpServer;
    /*int numRecv;
    Timer timer = new Timer();
    int lastRecv = -1;*/

    public CommThread() throws IOException {
        this.setName("Animation / Communication");
        ledManager = LEDCubeManager.getLEDManager();
        updateTime = System.nanoTime();
        port = new SerialPort(LEDCubeManager.getSerialPortName());
        tcpServer = new TCPServer(LEDCubeManager.getServerPort());
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
    }

    @Override
    @SneakyThrows(InterruptedException.class)
    public void run() {
        while (true) {
            /*if (timer.getSeconds() >= 1) {
                timer.restart();
                System.out.println("Recv rate: " + numRecv);
                numRecv = 0;
            }*/
            long interval = 1000000000 / refreshRate;
            long diff = System.nanoTime() - updateTime;
            if (diff >= interval) {
                updateTime = System.nanoTime();
                ticks++;
                synchronized (ledManager) {
                    if (currentSequence != null) currentSequence.update();
                    if (currentAnimation != null) {
                        currentAnimation.refresh();
                        currentAnimation.incTicks();
                    }
                }
                byte[] data = ledManager.getCommData();
                tcpServer.sendPacket(Packet.ID.CUBE_FRAME, data);
                synchronized (lock) {
                    try {
                        if (port.isOpened()) {
                            /*if (ticks % 30 == 0)*/ port.writeBytes(data);
                            /*byte[] bytes = port.readBytes();
                            if (bytes != null) {
                                numRecv += bytes.length;
                            }*/
                            //while (port.readBytes(1, 3000)[0] != 1){}
                            /*byte[] bytes = port.readBytes(data.length, 1000);
                            if (bytes != null) {
                                System.out.println("CHECK DATA");
                                for (int i = 0; i < data.length; i++) {
                                    if (bytes[i] != data[i]) System.out.println("ERROR @ " + i + " = " + (bytes[i] & 0xFF));
                                }
                            }*/
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        closePort();
                    }
                }
            }
            else if (interval - diff > 1000000) {
                Thread.sleep(1);
            }
        }
    }

    public void setCurrentAnimation(Animation currentAnimation) {
        synchronized (lock) {
            this.currentAnimation = currentAnimation;
            if (currentAnimation != null) {
                currentAnimation.reset();
                currentAnimation.loadOptions();
            }
        }
    }

    public void setCurrentSequence(AnimationSequence currentSequence) {
        synchronized (lock) {
            this.currentSequence = currentSequence;
            LEDCubeManager.getInstance().getScreenMainControl().animComboBox.setEnabled(currentSequence == null);
            if (currentSequence != null) currentSequence.start();
        }
    }

    public void openPort() {
        synchronized (lock) {
            if (!port.isOpened()) {
                try {
                    port.openPort();
                    port.setParams(2000000, 8, 1, 0);

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

    public int getNumTCPClients() {
        return tcpServer.getNumClients();
    }

    private class ShutdownThread extends Thread {
        @Override
        public void run() {
            closePort();
        }
    }
}
