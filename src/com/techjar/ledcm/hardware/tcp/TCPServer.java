
package com.techjar.ledcm.hardware.tcp;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.tcp.packet.Packet;
import com.techjar.ledcm.hardware.tcp.packet.PacketClientCapabilities;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.SneakyThrows;

/**
 *
 * @author Techjar
 */
public class TCPServer {
    private Thread listenThread;
    private Thread watchThread;
    private ServerSocket socket;
    private ConnectHandler connectHandler;
    private List<TCPClient> clients = Collections.synchronizedList(new ArrayList<TCPClient>());
    private Queue<Packet> sendQueue = new ConcurrentLinkedQueue<>();
    private int numClients;
    private int clientIndex = 1;

    public TCPServer(int port) throws IOException {
        socket = new ServerSocket(port);
        listenThread = new Thread("Server Listen Thread") {
            @Override
            public void run() {
                while (true) {
                    try {
                        final Socket client = socket.accept();
                        int capabilities;
                        client.setTcpNoDelay(true);
                        DataInputStream dis = new DataInputStream(client.getInputStream());
                        Packet packet = Packet.readPacket(dis);
                        if (packet instanceof PacketClientCapabilities) {
                            capabilities = ((PacketClientCapabilities)packet).getValue();
                        } else {
                            client.close();
                            continue;
                        }
                        TCPClient tcpClient = new TCPClient(client, clientIndex++, capabilities);
                        if (connectHandler != null) {
                            if (!connectHandler.onClientConnected(tcpClient)) {
                                tcpClient.close();
                                continue;
                            }
                        }
                        if (tcpClient.hasCapabilities(Packet.Capabilities.FRAME_DATA)) {
                            LEDCubeManager.getFrameServer().numClients++;
                        }
                        clients.add(tcpClient);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
        listenThread.start();
        watchThread = new Thread("Client Watch Thread") {
            @Override
            @SneakyThrows(InterruptedException.class)
            public void run() {
                while (true) {
                    if (sendQueue.size() > 0) {
                        synchronized (clients) {
                            Queue<Packet> queue = new LinkedList<>();
                            Packet packet;
                            while ((packet = sendQueue.poll()) != null) {
                                queue.add(packet);
                            }
                            numClients = clients.size();
                            Iterator<TCPClient> it = clients.iterator();
                            while (it.hasNext()) {
                                TCPClient client = it.next();
                                if (client.isClosed()) it.remove();
                                else {
                                    for (Packet pkt : queue) {
                                        if (client.hasCapabilities(pkt.getRequiredCapabilities())) {
                                            client.queuePacket(pkt);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Thread.sleep(10);
                }
            }
        };
        watchThread.start();
    }

    public void sendPacket(Packet packet) {
        sendQueue.add(packet);
        /*synchronized (clients) {
            for (TCPClient client : clients) {
                client.queuePacket(packet);
            }
        }*/
    }

    public void sendPacket(Packet packet, TCPClient client) {
        client.queuePacket(packet);
    }

    public int getNumClients() {
        return numClients;
    }

    public void setConnectHandler(ConnectHandler connectHanlder) {
        this.connectHandler = connectHanlder;
    }

    public static interface ConnectHandler {
        public abstract boolean onClientConnected(TCPClient client);
    }
}
