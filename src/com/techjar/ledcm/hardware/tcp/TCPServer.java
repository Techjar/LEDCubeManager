
package com.techjar.ledcm.hardware.tcp;

import com.techjar.ledcm.LEDCubeManager;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
                        client.setTcpNoDelay(true);
                        BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        if (!"LEDCUBE".equals(br.readLine())) {
                            client.close();
                            continue;
                        }
                        TCPClient tcpClient = new TCPClient(client, clientIndex++);
                        if (LEDCubeManager.getLEDCube().getSpectrumAnalyzer().playerExists()) sendPacket(Packet.ID.AUDIO_INIT, LEDCubeManager.getLEDCube().getSpectrumAnalyzer().getAudioInit(), tcpClient);
                        clients.add(tcpClient);
                    } catch (IOException ex) {
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
                                    client.queuePacket(pkt);
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

    public void sendPacket(Packet.ID id, byte[] data) {
        Packet packet = new Packet(id);
        packet.setData(data);
        sendQueue.add(packet);
        /*synchronized (clients) {
            for (TCPClient client : clients) {
                client.queuePacket(packet);
            }
        }*/
    }

    public void sendPacket(Packet.ID id, byte[] data, TCPClient client) {
        Packet packet = new Packet(id);
        packet.setData(data);
        client.queuePacket(packet);
    }

    public int getNumClients() {
        return numClients;
    }
}
