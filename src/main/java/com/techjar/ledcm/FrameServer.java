
package com.techjar.ledcm;

import com.techjar.ledcm.hardware.tcp.packet.Packet;
import com.techjar.ledcm.hardware.tcp.TCPServer;
import com.techjar.ledcm.hardware.tcp.packet.PacketVisualFrame;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import lombok.Getter;
import lombok.SneakyThrows;

/**
 *
 * @author Techjar
 */
public class FrameServer {
    private Thread sendThread;
    public volatile int numClients;
    private Queue<BufferedImage> sendQueue = new ConcurrentLinkedQueue<>();

    public FrameServer() throws IOException {
        sendThread = new Thread("Frame Send Thread") {
            @Override
            @SneakyThrows(InterruptedException.class)
            public void run() {
                BufferedImage image;
                while (true) {
                    while ((image = sendQueue.poll()) != null) {
                        try {
                            byte[] imageBytes;
                            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                                ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
                                ImageWriteParam param = writer.getDefaultWriteParam();
                                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                                param.setCompressionQuality(0.5F);
                                writer.setOutput(new MemoryCacheImageOutputStream(baos));
                                writer.write(null, new IIOImage(image, null, null), param);
                                writer.dispose();
                                imageBytes = baos.toByteArray();
                            }
                            LEDCubeManager.getLEDCube().getCommThread().getTcpServer().sendPacket(new PacketVisualFrame(imageBytes));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    Thread.sleep(1);
                }
            }
        };
    }

    public void queueFrame(BufferedImage image) {
        sendQueue.add(image);
    }
}
