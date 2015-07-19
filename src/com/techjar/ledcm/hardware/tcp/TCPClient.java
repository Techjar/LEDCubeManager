
package com.techjar.ledcm.hardware.tcp;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.tcp.packet.Packet;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.Getter;
import lombok.SneakyThrows;

/**
 *
 * @author Techjar
 */
public class TCPClient {
    private Thread sendThread;
    private Thread recvThread;
    @Getter private Socket socket;
    @Getter private InputStream inputStream;
    @Getter private OutputStream outputStream;
    private int capabilities;
    private Queue<Packet> sendQueue = new ConcurrentLinkedQueue<>();

    public TCPClient(Socket socket, int index, int capabilities) throws IOException {
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
        this.capabilities = capabilities;
        sendThread = new Thread("Client Send Thread #" + index) {
            @Override
            @SneakyThrows(InterruptedException.class)
            public void run() {
                Packet packet;
                DataOutputStream out = new DataOutputStream(TCPClient.this.outputStream);
                while (!TCPClient.this.socket.isClosed()) {
                    while ((packet = sendQueue.poll()) != null) {
                        try {
                            Packet.writePacket(out, packet);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            try {
                                close();
                            } catch (IOException ex2) {
                                ex2.printStackTrace();
                            }
                            return;
                        }
                    }
                    Thread.sleep(1);
                }
            }
        };
        sendThread.start();
        recvThread = new Thread("Client Recv Thread #" + index) {
            @Override
            public void run() {
                Packet packet;
                DataInputStream in = new DataInputStream(TCPClient.this.inputStream);
                try {
                    while (!TCPClient.this.socket.isClosed()) {
                        packet = Packet.readPacket(in);
                        LEDCubeManager.queuePacketForProcessing(packet);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    try {
                        close();
                    } catch (IOException ex2) {
                        ex2.printStackTrace();
                    }
                }
            }
        };
        recvThread.setDaemon(true);
        recvThread.start();
    }

    public InetAddress getInetAddress() {
        return socket.getInetAddress();
    }

    public int getPort() {
        return socket.getPort();
    }

    public synchronized void close() throws IOException {
        if (hasCapabilities(Packet.Capabilities.FRAME_DATA)) {
            LEDCubeManager.getFrameServer().numClients--;
        }
        socket.close();
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public boolean hasCapabilities(int value) {
        return (capabilities & value) == value;
    }

    public void queuePacket(Packet packet) {
        sendQueue.add(packet);
    }
}
