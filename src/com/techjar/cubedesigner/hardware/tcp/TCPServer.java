
package com.techjar.cubedesigner.hardware.tcp;

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
import java.util.List;
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
                    /*if (!"MICRO".equals(br.readLine())) {
                        client.close();
                        continue;
                    }*/
                    clients.add(new TCPClient(client, clientIndex++));
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
                        numClients = clients.size();
                        Iterator<TCPClient> it = clients.iterator();
                        while (it.hasNext()) {
                            TCPClient client = it.next();
                            if (client.isClosed()) it.remove();
                        }
                    }
                    Thread.sleep(100);
                }
            }
        };
        watchThread.start();
    }

    public void sendData(byte[] data) {
        synchronized (clients) {
            for (TCPClient client : clients) {
                client.queueData(data);
            }
        }
    }

    public int getNumClients() {
        return numClients;
    }
}
