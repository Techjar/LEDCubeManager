
package com.techjar.ledcm.hardware;

import com.techjar.ledcm.hardware.manager.LEDManager;
import com.techjar.ledcm.hardware.handler.PortHandler;
import com.techjar.ledcm.ControlUtil;
import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.gui.screen.ScreenMainControl;
import com.techjar.ledcm.hardware.animation.Animation;
import com.techjar.ledcm.hardware.animation.AnimationSequence;
import com.techjar.ledcm.hardware.tcp.TCPClient;
import com.techjar.ledcm.hardware.tcp.packet.Packet;
import com.techjar.ledcm.hardware.tcp.TCPServer;
import com.techjar.ledcm.hardware.tcp.packet.PacketAnimationList;
import com.techjar.ledcm.hardware.tcp.packet.PacketAudioInit;
import com.techjar.ledcm.hardware.tcp.packet.PacketCubeFrame;
import com.techjar.ledcm.hardware.tcp.packet.PacketSetColorPicker;
import com.techjar.ledcm.util.Timer;
import java.io.IOException;
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
    private final PortHandler portHandler;
    private long updateTime;
    private long ticks;
    private int fpsCounter;
    private volatile int fpsDisplay;
    private Timer frameTimer = new Timer();
    @Getter @Setter private int refreshRate = 60;
    @Getter private Animation currentAnimation;
    @Getter private AnimationSequence currentSequence;
    @Getter private TCPServer tcpServer;
    @Getter @Setter private boolean frozen;

    public CommThread(PortHandler portHandler) throws IOException {
        this.setName("Animation / Communication");
        this.portHandler = portHandler;
        ledManager = LEDCubeManager.getLEDCube().getLEDManager();
        updateTime = System.nanoTime();
        tcpServer = new TCPServer(LEDCubeManager.getServerPort());
        tcpServer.setConnectHandler(new TCPServer.ConnectHandler() {
            @Override
            public boolean onClientConnected(TCPClient client) {
                if (client.hasCapabilities(Packet.Capabilities.AUDIO_DATA)) {
                    if (LEDCubeManager.getLEDCube().getSpectrumAnalyzer().getAudioFormat() != null) {
                        client.queuePacket(new PacketAudioInit(LEDCubeManager.getLEDCube().getSpectrumAnalyzer().getAudioFormat()));
                    }
                }
                if (client.hasCapabilities(Packet.Capabilities.CONTROL_DATA)) {
                    ScreenMainControl screen = LEDCubeManager.getInstance().getScreenMainControl();
                    client.queuePacket(new PacketAnimationList(LEDCubeManager.getLEDCube().getAnimationNames().toArray(new String[0]), LEDCubeManager.getLEDCube().getCommThread().getCurrentAnimation() == null ? "" : LEDCubeManager.getLEDCube().getCommThread().getCurrentAnimation().getName()));
                    client.queuePacket(new PacketSetColorPicker(screen.redColorSlider.getValue(), screen.greenColorSlider.getValue(), screen.blueColorSlider.getValue()));
                    client.queuePacket(ControlUtil.getAnimationOptionsPacket());
                }
                return true;
            }
        });
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
                if (!frozen) {
                    if (frameTimer.getMilliseconds() >= 1000) {
                        fpsDisplay = fpsCounter;
                        fpsCounter = 0;
                        frameTimer.restart();
                    }
                    fpsCounter++;
                    ticks++;
                    synchronized (lock) {
                        if (currentSequence != null) currentSequence.update();
                        if (currentAnimation != null) {
                            try {
                                currentAnimation.refresh();
                                currentAnimation.incTicks();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                currentAnimation = null;
                            }
                        }
                    }
                    ledManager.updateLEDArray();
                    byte[] data = ledManager.getCommData();
                    tcpServer.sendPacket(new PacketCubeFrame(data));
                    synchronized (lock) {
                        try {
                            if (portHandler.isOpened()) {
                                portHandler.writeBytes(data);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            closePort();
                        }
                    }
                } else {
                    fpsCounter = 0;
                    fpsDisplay = 0;
                    ledManager.updateLEDArray();
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
                tcpServer.sendPacket(ControlUtil.getAnimationOptionsPacket());
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
            if (!portHandler.isOpened()) {
                try {
                    portHandler.open(ledManager.getBaudRate());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void closePort() {
        synchronized (lock) {
            if (portHandler.isOpened()) {
                try {
                    portHandler.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public boolean isPortOpen() {
        return portHandler.isOpened();
    }

    public int getNumTCPClients() {
        return tcpServer.getNumClients();
    }

    public int getFPS() {
        return fpsDisplay;
    }

    private class ShutdownThread extends Thread {
        @Override
        public void run() {
            closePort();
        }
    }
}
