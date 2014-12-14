
package com.techjar.cubedesigner.hardware.animation;

import com.techjar.cubedesigner.util.MathHelper;
import com.techjar.cubedesigner.util.Vector2;
import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import ddf.minim.analysis.FFT;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.Setter;
import lombok.SneakyThrows;
import org.lwjgl.util.Color;

/**
 *
 * @author Techjar
 */
public class AnimationSpectrumAnalyzer extends Animation {
    private final Minim minim;
    private final FFTThread thread;
    @Setter public int updateRate = 60;

    public AnimationSpectrumAnalyzer() {
        super();
        this.minim = new Minim(this);
        this.thread = new FFTThread();
        //thread.player = minim.loadFile("resources/sounds/ui/click.wav");
        //thread.start();
    }

    @Override
    public String getName() {
        return "Spectrum Analyzer";
    }

    @Override
    public void refresh() {
        thread.refresh();
    }

    @Override
    public void reset() {
        thread.amplitudes = new float[64];
    }

    public String sketchPath(String fileName) {
        return new File(fileName).getAbsolutePath();
    }

    @SneakyThrows(IOException.class)
    public InputStream createInput(String fileName) {
        return new FileInputStream(fileName);
    }

    public boolean isPlaying() {
        if (thread.player != null) return thread.player.isPlaying();
        return false;
    }

    public void play() {
        if (thread.player != null) {
            if (thread.player.isPlaying()) {
                thread.player.rewind();
            } else {
                if (thread.player.position() >= thread.player.length() - 1) {
                    thread.player.rewind();
                }
                thread.player.play();
            }
        }
    }

    public void pause() {
        if (thread.player != null) thread.player.pause();
    }

    public void stop() {
        if (thread.player != null) {
            thread.player.pause();
            thread.player.rewind();
        }
    }

    public void setVolume(float volume) {
        if (thread.player != null) thread.player.setGain(volume > 0 ? (float)(20 * Math.log10(volume)) : -200);
    }

    public float getPosition() {
        if (thread.player != null) {
            return (float)thread.player.position() / (float)thread.player.length();
        }
        return 0;
    }

    public int getPositionMillis() {
        if (thread.player != null) {
            return thread.player.position();
        }
        return 0;
    }

    public void setPosition(float position) {
        if (thread.player != null) {
            thread.player.rewind();
            thread.player.skip(Math.round(thread.player.length() * position));
        }
    }

    public void loadFile(String path) {
        synchronized (thread.lock) {
            try {
                if (thread.player != null) {
                    thread.player.close();
                }
                AudioPlayer player = minim.loadFile(path);
                thread.fft = new FFT(player.bufferSize(), player.sampleRate());
                if (thread.player != null) player.setGain(thread.player.getGain());
                player.play();
                thread.player = player;
                if (!thread.isAlive()) thread.start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private class FFTThread extends Thread {
        final Object lock = new Object();
        FFT fft;
        AudioPlayer player;
        long updateTime;
        float[] amplitudes = new float[64];

        public FFTThread() {
            this.setName("Spectrum Analyzer FFT");
            this.updateTime = System.nanoTime();
        }

        @Override
        public void run() {
            while (true) {
                synchronized (lock) {
                    if (fft == null || player == null || !player.isPlaying()) {
                        /*if (player != null) {
                            if (player.position() >= player.length() - 1) {
                                player.rewind();
                            }
                        }*/
                        continue;
                    }
                    processFFT();
                }
            }
        }

        public void refresh() {
            synchronized (amplitudes) {
                for (int i = 0; i < 64; i++) {
                    float amplitude = amplitudes[i] - 2;
                    amplitudes[i] = 0;
                    Vector2 pos = spiralPosition(i);
                    for (int j = 0; j < 8; j++) {
                        float increment = (1.75F * (j + 1)) * (1 - (i / 70F));
                        //ledManager.setLEDColor(i % 8, peak, i / 8, amplitude > 0 ? colorAtY(peak) : new Color());
                        ledManager.setLEDColorRaw((int)pos.getX() + 3, j, (int)pos.getY() + 3, amplitude > 0 ? colorAtY(j, MathHelper.clamp(amplitude / increment, 0, 1)) : new Color());
                        //if (amplitude <= 0) break;
                        amplitude -= increment;
                    }
                    /*for (int j = 0; j < 8; j++) {
                        ledManager.setLEDColor(i % 8, j, i / 8, j <= peak ? colorAtY(peak) : new Color());
                    }*/
                }
            }
        }

        private void processFFT() {
            long updateInterval = 1000000000 / updateRate;
            fft.forward(player.mix);
            for (int i = 0; i < 64; i++) {
                float amplitude = fft.getBand(i * 4);
                //if (amplitude > newAmplitudes[i]) newAmplitudes[i] = amplitude;
                if (amplitude > amplitudes[i]) amplitudes[i] = amplitude;
            }
        }

        private Color colorAtY(int y, float brightness) {
            int res = ledManager.getResolution();
            if (y > 6) return new Color(Math.round(res * brightness), 0, 0);
            if (y > 5) return new Color(Math.round(res * brightness), Math.round(res * brightness), 0);
            if (y > 1) return new Color(0, Math.round(res * brightness), 0);
            return new Color(0, 0, Math.round(res * brightness));
        }

        private Vector2 spiralPosition(int index) {
            // (di, dj) is a vector - direction in which we move right now
            int di = 1;
            int dj = 0;
            // length of current segment
            int segment_length = 1;

            // current position (i, j) and how much of current segment we passed
            int i = 0;
            int j = 0;
            int segment_passed = 0;
            for (int k = 0; k < index; ++k) {
                // make a step, add 'direction' vector (di, dj) to current position (i, j)
                i += di;
                j += dj;
                ++segment_passed;

                if (segment_passed == segment_length) {
                    // done with current segment
                    segment_passed = 0;

                    // 'rotate' directions
                    int buffer = di;
                    di = -dj;
                    dj = buffer;

                    // increase segment length if necessary
                    if (dj == 0) {
                        ++segment_length;
                    }
                }
            }

            return new Vector2(i, j);
        }
    }
}
