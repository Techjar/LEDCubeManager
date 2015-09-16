package com.techjar.ledcm.hardware;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.animation.Animation;
import com.techjar.ledcm.hardware.animation.AnimationSequence;
import com.techjar.ledcm.hardware.tcp.TCPClient;
import com.techjar.ledcm.hardware.tcp.TCPServer;
import com.techjar.ledcm.hardware.tcp.packet.PacketAudioInit;
import com.techjar.ledcm.hardware.tcp.packet.PacketCubeFrame;
import jssc.SerialPort;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author Techjar
 */
public class CommThread extends Thread {

    private final Object lock = new Object();
    private final LEDManager ledManager;
    OutputStream outputStream;
    private long updateTime;
    private long ticks;
    @Getter
    @Setter
    private int refreshRate = 60;
    @Getter
    private Animation currentAnimation;
    @Getter
    private AnimationSequence currentSequence;
    private SerialPort port;
    @Getter
    @Setter
    private boolean clientOnline;
    @Getter
    @Setter
    private boolean frozen;
    /*int numRecv;
    Timer timer = new Timer();
    int lastRecv = -1;*/
    @Getter
    private TCPServer tcpServer;

    public CommThread() throws IOException {
        this.setName("Animation / Communication");
        ledManager = LEDCubeManager.getLEDManager();
        updateTime = System.nanoTime();
        port = new SerialPort(LEDCubeManager.getSerialPortName());
        tcpServer = new TCPServer(LEDCubeManager.getServerPort());
        tcpServer.setConnectHanlder(new TCPServer.ConnectHandler() {

            @Override
            public boolean onClientConnected(TCPClient client) {
                if (LEDCubeManager.getLEDCube().getSpectrumAnalyzer().playerExists()) {
                    client.queuePacket(new PacketAudioInit(LEDCubeManager.getLEDCube().getSpectrumAnalyzer().getAudioFormat()));
                }
                return true;
            }
        });

        String clientIp = LEDCubeManager.getClientIp();
        if (clientIp != null && !clientIp.isEmpty())
            outputStream = new Socket(clientIp, LEDCubeManager.getClientPort()).getOutputStream();

        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
    }

    @Override
    @SneakyThrows(InterruptedException.class)
    public void run() {
        while (true) {

            long interval = 1000000000 / refreshRate;
            long diff = System.nanoTime() - updateTime;
            if (diff >= interval) {
                updateTime = System.nanoTime();
                ticks++;
                synchronized (ledManager) {
                    if (currentSequence != null && !frozen)
                        currentSequence.update();
                    if (currentAnimation != null && !frozen) {
                        try {
                            currentAnimation.refresh();
                            currentAnimation.incTicks();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            currentAnimation = null;
                        }
                    }
                }
                byte[] data = ledManager.getCommData();
                tcpServer.sendPacket(new PacketCubeFrame(data));
                synchronized (lock) {
                    try {
                        if (clientOnline) {
                            outputStream.write(data);
                            outputStream.flush();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        clientOnline = false;
                    }
                    try {
                        if (port.isOpened()) {
                            port.writeBytes(data);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        closePort();
                    }
                }
            } else if (interval - diff > 1000000) {
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
                LEDCubeManager.getControlServer().sendAnimationOptions();
            }
        }
    }

    public void setCurrentSequence(AnimationSequence currentSequence) {
        synchronized (lock) {
            this.currentSequence = currentSequence;
            LEDCubeManager.getInstance().getScreenMainControl().animComboBox.setEnabled(currentSequence == null);
            if (currentSequence != null)
                currentSequence.start();
        }
    }

    public void openPort() {
        synchronized (lock) {
            if (!port.isOpened()) {
                try {
                    port.openPort();
                    port.setParams(ledManager.getBaudrate(), 8, 1, 0);

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
