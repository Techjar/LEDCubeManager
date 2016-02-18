
package com.techjar.ledcm.hardware;

import com.techjar.ledcm.LEDCubeManager;
import com.techjar.ledcm.hardware.animation.Animation;
import com.techjar.ledcm.hardware.animation.AnimationSpectrumAnalyzer;
import com.techjar.ledcm.hardware.tcp.packet.Packet;
import com.techjar.ledcm.hardware.tcp.packet.PacketAudioData;
import com.techjar.ledcm.hardware.tcp.packet.PacketAudioInit;
import com.techjar.ledcm.util.BufferHelper;
import com.techjar.ledcm.util.MathHelper;
import com.techjar.ledcm.util.PrintStreamRelayer;
import com.techjar.ledcm.util.Timer;
import com.techjar.ledcm.util.Util;
import com.techjar.ledcm.util.logging.LogHelper;
import ddf.minim.AudioListener;
import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class SpectrumAnalyzer {
    private final int baseBufferSize = 2048;
    private final int sampleRate = 48000;
    private final Minim minim;
    private FFT fft;
    private BeatDetect beatDetect;
    private int beatDetectMode = BeatDetect.FREQ_ENERGY;
    private AudioPlayer player;
    @Getter private String currentTrack = "";
    @Getter private Map<String, Mixer> mixers = new LinkedHashMap<>();
    @Getter private Mixer currentMixer;
    @Getter private String currentMixerName;
    @Getter @Setter private float mixerGain = 1;
    private TargetDataLine dataLine;
    private AudioFormat dataLineFormat;
    private Thread inputThread;
    private Timer audioInputRestartTimer = new Timer();
    private List<String> converting = Collections.synchronizedList(new ArrayList<String>());

    public SpectrumAnalyzer() {
        super();
        this.minim = new Minim(this);
        //this.minim.debugOn();
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            Mixer mixer = AudioSystem.getMixer(info);
            for (Line.Info lineInfo : mixer.getTargetLineInfo()) {
                if (lineInfo instanceof TargetDataLine.Info) {
                    mixers.put(info.getName(), mixer);
                    if (currentMixer == null || info.getName().equals(LEDCubeManager.getConfig().getString("sound.inputdevice"))) {
                        currentMixer = mixer;
                        currentMixerName = info.getName();
                    }
                    LogHelper.config("Input device: %s", info.getName());
                    break;
                }
            }
        }
        mixers = Collections.unmodifiableMap(mixers);
        LEDCubeManager.getConfig().setProperty("sound.inputdevice", currentMixerName);
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
        if (player != null) player.setGain(volume > 0 ? (float)(MathHelper.log(volume, 4) * 20) : -200);
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

    public int getLengthMillis() {
        if (player != null) {
            return player.length();
        }
        return 0;
    }

    public void setPosition(float position) {
        if (player != null) {
            player.rewind();
            player.skip(Math.round(player.length() * position));
        }
    }

    public AudioFormat getAudioFormat() {
        if (player != null) {
            return player.getFormat();
        }
        return dataLineFormat;
    }

    public void setMixer(String name) {
        Mixer mixer = mixers.get(name);
        if (mixer != null) {
            currentMixer = mixer;
            currentMixerName = name;
            if (dataLine != null) {
                stopAudioInput();
                while (dataLine != null);
                startAudioInput();
            }
            LEDCubeManager.getConfig().setProperty("sound.inputdevice", currentMixerName);
        }
    }

    public boolean isRunningAudioInput() {
        return dataLine != null;
    }

    public void startAudioInput() {
        if (player != null) {
            player.close();
            player = null;
            currentTrack = "";
        }
        try {
            TargetDataLine.Info lineInfo = (TargetDataLine.Info)currentMixer.getTargetLineInfo()[0];
            AudioFormat supportedFormat = null;
            outerLoop: for (int i = 2; i >= 1; i--) {
                for (AudioFormat fmt : lineInfo.getFormats()) {
                    if (fmt.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED) && fmt.getSampleSizeInBits() == 16 && fmt.getChannels() == i) {
                        supportedFormat = fmt;
                        break outerLoop;
                    }
                }
            }
            if (supportedFormat == null) {
                LogHelper.severe("Couldn't find desired AudioFormat!");
                return;
            }
            final AudioFormat format = new AudioFormat(supportedFormat.getEncoding(), sampleRate, supportedFormat.getSampleSizeInBits(), supportedFormat.getChannels(), supportedFormat.getFrameSize(), sampleRate, false);
            dataLineFormat = format;
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!currentMixer.isLineSupported(info)) {
                LogHelper.severe("Mixer doesn't support requested line!");
                return;
            }
            final int bufferSize = baseBufferSize * format.getChannels();
            dataLine = (TargetDataLine)currentMixer.getLine(info);
            dataLine.open(format, bufferSize * 8);
            dataLine.start();
            fft = new FFT(baseBufferSize / 2, sampleRate);
            beatDetect = new BeatDetect(baseBufferSize / 2, sampleRate);
            beatDetect.detectMode(beatDetectMode);
            LEDCubeManager.getLEDCube().getCommThread().getTcpServer().sendPacket(new PacketAudioInit(format));
            final AudioListener listener = new AnalyzerAudioListener(fft, beatDetect);
            final AudioListener listener2 = new StreamingAudioListener(true);
            inputThread = new Thread("Audio Input") {
                @Override
                public void run() {
                    audioInputRestartTimer.restart();
                    byte[] buffer = new byte[bufferSize];
                    float[] floats = new float[bufferSize / 2];
                    float[] floatsL = new float[bufferSize / 4];
                    float[] floatsR = new float[bufferSize / 4];
                    while (dataLine.isOpen()) {
                        dataLine.read(buffer, 0, bufferSize);
                        if (format.getChannels() == 2) {
                            for (int i = 0; i < bufferSize; i += 4) {
                                int val = (short)((buffer[i] & 0xFF) | ((buffer[i + 1] & 0xFF) << 8));
                                floatsL[i / 4] = MathHelper.clamp((val / 32768F) * mixerGain, -1, 1);
                            }
                            for (int i = 2; i < bufferSize; i += 4) {
                                int val = (short)((buffer[i] & 0xFF) | ((buffer[i + 1] & 0xFF) << 8));
                                floatsR[i / 4] = MathHelper.clamp((val / 32768F) * mixerGain, -1, 1);
                            }
                            listener.samples(floatsL, floatsR);
                            listener2.samples(floatsL, floatsR);
                        } else {
                            for (int i = 0; i < bufferSize; i += 2) {
                                int val = (short)((buffer[i] & 0xFF) | ((buffer[i + 1] & 0xFF) << 8));
                                floats[i / 2] = MathHelper.clamp((val / 32768F) * mixerGain, -1, 1);
                            }
                            listener.samples(floats);
                            listener2.samples(floats);
                        }
                        // Really dumb fix for weird latency build-up issue
                        if (audioInputRestartTimer.getMinutes() >= 30) {
                            audioInputRestartTimer.restart();
                            try {
                                dataLine.close();
                                dataLine.open(format, bufferSize * 8);
                                dataLine.start();
                            } catch (LineUnavailableException ex) {
                                ex.printStackTrace();
                                LEDCubeManager.getInstance().getScreenMainControl().audioInputBtnBg.setBackgroundColor(new Color(255, 127, 0));
                            }
                        }
                    }
                    dataLine = null;
                }
            };
            inputThread.setPriority(Thread.MAX_PRIORITY);
            inputThread.setDaemon(true);
            inputThread.start();
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
            dataLineFormat = null;
        }
    }

    public void stopAudioInput() {
        if (dataLine != null) {
            dataLine.close();
            dataLineFormat = null;
            inputThread = null;
        }
    }

    public void loadFile(File file) {
        try {
            String hash = Util.getChecksum("SHA1", file.getCanonicalPath());
            File file2 = convertFile(file, true);
            stopAudioInput();
            LEDCubeManager.getInstance().getScreenMainControl().audioInputBtnBg.setBackgroundColor(new Color(255, 0, 0));
            if (player != null) {
                player.close();
            }
            player = minim.loadFile(file2.getAbsolutePath());
            LEDCubeManager.getLEDCube().getCommThread().getTcpServer().sendPacket(new PacketAudioInit(player.getFormat()));
            currentTrack = file.getName().substring(0, file.getName().lastIndexOf('.'));
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

    /**
     * Converts audio file to PCM format for playback.
     *
     * @param file File to convert
     * @param block If true, will block and return the file when done. If false, will return the file if already converted, or null if not and do the conversion on a thread. If already being converted, will either block until done or return null.
     */
    public File convertFile(final File file, boolean block) throws IOException, NoSuchAlgorithmException, InterruptedException {
        final String hash = Util.getChecksum("SHA1", file.getCanonicalPath());
        final File file2 = new File("resampled/" + hash + ".wav");
        if (converting.contains(hash)) {
            if (block) {
                while (converting.contains(hash)) Thread.sleep(1);
                return file2;
            } else {
                return null;
            }
        }
        if (!file2.exists()) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        ProcessBuilder pb = new ProcessBuilder();
                        pb.directory(new File(System.getProperty("user.dir")));
                        pb.redirectErrorStream(true);
                        pb.command(LEDCubeManager.getConfig().getString("misc.ffmpegpath"), "-i", file.getAbsolutePath(), "-af", "aresample=resampler=soxr", "-sample_fmt", "s16", "-ar", Integer.toString(sampleRate), file2.getAbsolutePath());
                        Process proc = pb.start();
                        LEDCubeManager.setConvertingAudio(true);
                        Thread psrThread = new PrintStreamRelayer(proc.getInputStream(), System.out);
                        psrThread.setDaemon(true); psrThread.start();
                        proc.waitFor();
                        if (converting.size() <= 1) LEDCubeManager.setConvertingAudio(false);
                        converting.remove(hash);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            };
            converting.add(hash);
            if (block) runnable.run();
            else {
                Thread thread = new Thread(runnable, "Audio Conversion");
                thread.setDaemon(true);
                thread.start();
            }
        }
        return file2;
    }

    public void loadFile(String file) {
        loadFile(new File(file));
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
            Animation animation = LEDCubeManager.getLEDCube().getCommThread().getCurrentAnimation();
            if (animation instanceof AnimationSpectrumAnalyzer) {
                AnimationSpectrumAnalyzer anim = (AnimationSpectrumAnalyzer)animation;
                if (anim.isFFT()) {
                    fft.forward(floats);
                    anim.processFFT(fft);
                }
                if (anim.isBeatDetect()) {
                    if (anim.getBeatDetectMode() != beatDetectMode) {
                        beatDetectMode = anim.getBeatDetectMode();
                        beatDetect.detectMode(beatDetectMode);
                    }
                    beatDetect.detect(floats);
                    anim.processBeatDetect(beatDetect);
                }
            }
        }

        @Override
        public void samples(float[] floatsL, float[] floatsR) {
            Animation animation = LEDCubeManager.getLEDCube().getCommThread().getCurrentAnimation();
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
            AudioFormat format = getAudioFormat();
            if (format == null) return;
            ByteBuffer buf = ByteBuffer.allocate(samples.length * 2);
            buf.order(format.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
            for (int i = 0; i < samples.length; i++) {
                buf.putShort(samples[i]);
            }
            LEDCubeManager.getLEDCube().getCommThread().getTcpServer().sendPacket(new PacketAudioData(buf.array()));
        }
    }
}
