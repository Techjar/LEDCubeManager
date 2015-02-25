
package com.techjar.ledcm.hardware;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.animation.Animation;
import com.techjar.ledcm.hardware.animation.AnimationSpectrumAnalyzer;
import com.techjar.ledcm.hardware.tcp.Packet;
import com.techjar.ledcm.util.BufferHelper;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.PrintStreamRelayer;
import com.techjar.ledcm.util.Vector2;
import ddf.minim.AudioListener;
import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.sound.sampled.AudioFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class SpectrumAnalyzer {
    private final Minim minim;
    private FFT fft;
    private BeatDetect beatDetect;
    private int beatDetectMode = BeatDetect.FREQ_ENERGY;
    private AudioPlayer player;
    @Setter public int updateRate = 60;
    @Getter private String currentTrack = "";

    public SpectrumAnalyzer() {
        super();
        this.minim = new Minim(this);
        //this.minim.debugOn();
        //thread.player = minim.loadFile("resources/sounds/ui/click.wav");
        //thread.start();
    }

    public String sketchPath(String fileName) {
        return new File(fileName).getAbsolutePath();
    }

    @SneakyThrows(IOException.class)
    public InputStream createInput(String fileName) {
        return new FileInputStream(fileName);
    }

    public boolean isPlaying() {
        if (player != null) return player.isPlaying();
        return false;
    }

    public boolean playerExists() {
        return player != null;
    }

    public void play() {
        if (player != null) {
            if (player.isPlaying()) {
                player.rewind();
            } else {
                if (player.position() >= player.length() - 1) {
                    player.rewind();
                }
                player.play();
            }
        }
    }

    public void pause() {
        if (player != null) player.pause();
    }

    public void stop() {
        if (player != null) {
            player.pause();
            player.rewind();
        }
    }

    public void close() {
        if (player != null) {
            player.close();
            player = null;
        }
    }

    public void setVolume(float volume) {
        if (player != null) player.setGain(volume > 0 ? (float)(20 * Math.log10(volume)) : -200);
    }

    public float getPosition() {
        if (player != null) {
            return (float)player.position() / (float)player.length();
        }
        return 0;
    }

    public int getPositionMillis() {
        if (player != null) {
            return player.position();
        }
        return 0;
    }

    public void setPosition(float position) {
        if (player != null) {
            player.rewind();
            player.skip(Math.round(player.length() * position));
        }
    }

    public void loadFile(File file) {
        try {

            File file2 = new File("resampled/" + file.getName().substring(0, file.getName().lastIndexOf('.')) + ".wav");
            String path = file2.getAbsolutePath();
            if (!file2.exists()) {
                ProcessBuilder pb = new ProcessBuilder();
                pb.directory(new File(System.getProperty("user.dir")));
                pb.redirectErrorStream(true);
                pb.command("ffmpeg", "-i", file.getAbsolutePath(), "-af", "aresample=resampler=soxr", "-sample_fmt", "s16", "-ar", "48000", file2.getAbsolutePath());
                Process proc = pb.start();
                LEDCubeManager.setConvertingAudio(true);
                Thread psrThread = new PrintStreamRelayer(proc.getInputStream(), System.out);
                psrThread.setDaemon(true); psrThread.start();
                proc.waitFor();
                LEDCubeManager.setConvertingAudio(false);
            }
            if (player != null) {
                player.close();
            }
            AudioPlayer oldPlayer = player;
            player = minim.loadFile(path);
            LEDCubeManager.getCommThread().getTcpServer().sendPacket(Packet.ID.AUDIO_INIT, getAudioInit());
            String path2 = path.replaceAll("\\\\", "/");
            currentTrack = path2.contains("/") ? path2.substring(path2.lastIndexOf('/') + 1) : path2;
            currentTrack = currentTrack.substring(0, currentTrack.lastIndexOf('.'));
            setVolume(LEDCubeManager.getInstance().getScreenMainControl().volumeSlider.getValue());
            fft = new FFT(player.bufferSize(), player.sampleRate());
            beatDetect = new BeatDetect(player.bufferSize(), player.sampleRate());
            beatDetect.detectMode(beatDetectMode);
            player.addListener(new AnalyzerAudioListener(fft, beatDetect));
            player.addListener(new StreamingAudioListener(true));
            player.play();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void loadFile(String file) {
        loadFile(new File(file));
    }

    public byte[] getAudioInit() {
        AudioFormat format = player.getFormat();
        ByteBuffer buf = ByteBuffer.allocate(BufferHelper.getStringSize(format.getEncoding().toString()) + 20);
        buf.order(ByteOrder.BIG_ENDIAN);
        BufferHelper.putString(buf, format.getEncoding().toString());
        buf.putFloat(format.getSampleRate());
        buf.putInt(format.getSampleSizeInBits());
        buf.put((byte)format.getChannels());
        buf.putInt(format.getFrameSize());
        buf.putFloat(format.getFrameRate());
        buf.put((byte)(format.isBigEndian() ? 1 : 0));
        return buf.array();
    }

    private class AnalyzerAudioListener implements AudioListener {
        private final FFT fft;
        private final BeatDetect beatDetect;

        public AnalyzerAudioListener(FFT fft, BeatDetect beatDetect) {
            this.fft = fft;
            this.beatDetect = beatDetect;
        }

        @Override
        public void samples(float[] floats) {
            Animation animation = LEDCubeManager.getCommThread().getCurrentAnimation();
            if (animation instanceof AnimationSpectrumAnalyzer) {
                AnimationSpectrumAnalyzer anim = (AnimationSpectrumAnalyzer)animation;
                if (anim.isFFT()) {
                    fft.forward(floats);
                    anim.processFFT(fft);
                }
                if (anim.isBeatDetect()) {
                    beatDetect.detect(floats);
                    anim.processBeatDetect(beatDetect);
                }
            }
        }

        @Override
        public void samples(float[] floatsL, float[] floatsR) {
            Animation animation = LEDCubeManager.getCommThread().getCurrentAnimation();
            if (animation instanceof AnimationSpectrumAnalyzer) {
                AnimationSpectrumAnalyzer anim = (AnimationSpectrumAnalyzer)animation;
                if (anim.isFFT()) {
                    fft.forward(floatsL, floatsR);
                    anim.processFFT(fft);
                }
                if (anim.isBeatDetect()) {
                    if (anim.getBeatDetectMode() != beatDetectMode) {
                        beatDetectMode = anim.getBeatDetectMode();
                        beatDetect.detectMode(beatDetectMode);
                    }
                    float[] floats = new float[floatsL.length];
                    for (int i = 0; i < floatsL.length; i++) {
                        floats[i] = MathHelper.clamp(floatsL[i] / 2 + floatsR[i] / 2, -1, 1);
                    }
                    beatDetect.detect(floats);
                    anim.processBeatDetect(beatDetect);
                }
            }
        }
    }

    private class StreamingAudioListener implements AudioListener {
        private final boolean useStereo;

        public StreamingAudioListener(boolean useStereo) {
            this.useStereo = useStereo;
        }

        @Override
        public void samples(float[] floats) {
            //if (!player.isPlaying()) return;
            short[] resampled = new short[floats.length];
            for (int i = 0; i < floats.length; i++) {
                resampled[i] = (short)MathHelper.clamp(Math.round(32768 * floats[i]), -32768, 32767);
            }
            send(resampled);
        }

        @Override
        public void samples(float[] floatsL, float[] floatsR) {
            //if (!player.isPlaying()) return;
            short[] resampledL = new short[floatsL.length];
            short[] resampledR = new short[floatsR.length];
            for (int i = 0; i < floatsL.length; i++) {
                resampledL[i] = (short)MathHelper.clamp(Math.round(32768 * floatsL[i]), -32768, 32767);
            }
            for (int i = 0; i < floatsR.length; i++) {
                resampledR[i] = (short)MathHelper.clamp(Math.round(32768 * floatsR[i]), -32768, 32767);
            }
            if (useStereo) {
                short[] stereo = new short[resampledL.length * 2];
                for (int i = 0; i < resampledL.length; i++) {
                    stereo[i * 2] = resampledL[i];
                    stereo[i * 2 + 1] = resampledR[i];
                }
                send(stereo);
            } else {
                short[] mono = new short[resampledL.length];
                for (int i = 0; i < resampledL.length; i++) {
                    mono[i] = (short)MathHelper.clamp(Math.round(resampledL[i] / 2 + resampledR[i] / 2), -32768, 32767);
                }
                send(mono);
            }
        }

        private void send(short[] samples) {
            ByteBuffer buf = ByteBuffer.allocate(samples.length * 2 + 2);
            buf.order(ByteOrder.BIG_ENDIAN);
            buf.putShort((short)samples.length);
            for (int i = 0; i < samples.length; i++) {
                buf.putShort(samples[i]);
            }
            LEDCubeManager.getCommThread().getTcpServer().sendPacket(Packet.ID.AUDIO_DATA, buf.array());
        }
    }
}
