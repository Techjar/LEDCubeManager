package com.techjar.ledcm;

import com.techjar.ledcm.util.MathHelper;
import de.cuina.fireandfuel.CodecJLayerMP3;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.codecs.CodecJOgg;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

/**
 *
 * @author Techjar
 */
public class SoundManager {
    protected final SoundSystem soundSystem;
    protected final File soundPath;
    protected final File soundPathCustom;
    protected float effectVolume = 1;
    protected float musicVolume = 1;
    protected long nextId;
    protected Map<String, SourceType> sources = new HashMap<>();
    protected List<String> tempSources = new ArrayList<>();

    public SoundManager() {
        soundSystem = new SoundSystem();
        soundPath = new File("resources/sounds/");
        soundPathCustom = new File("resources/sounds/custom/");
    }

    public void update() {
        Iterator<String> it = tempSources.iterator();
        while (it.hasNext()) {
            String source = it.next();
            if (!soundSystem.playing(source)) {
                soundSystem.removeSource(source);
                sources.remove(source);
                it.remove();
            }
        }
    }

    private File getFile(String file) {
        File fil = new File(soundPathCustom, file);
        if (!fil.exists()) fil = new File(soundPath, file);
        return fil;
    }

    @SneakyThrows(MalformedURLException.class)
    public void loadSound(String file) {
        soundSystem.loadSound(getFile(file).toURI().toURL(), file);
    }

    public void unloadSound(String file) {
        soundSystem.unloadSound(file);
    }

    public boolean isPlaying(String source) {
        return soundSystem.playing(source);
    }

    public String playEffect(String file, boolean loop) {
        String source = playTemporarySound(file, loop, SourceType.EFFECT);
        if (source != null) soundSystem.setVolume(source, effectVolume);
        return source;
    }

    public String playMusic(String file, boolean loop) {
        String source = playStreamingSound(file, loop, SourceType.MUSIC);
        if (source != null) soundSystem.setVolume(source, musicVolume);
        return source;
    }

    public String playLoadedMusic(String file, boolean loop) {
        String source = playSound(file, loop, SourceType.MUSIC);
        if (source != null) soundSystem.setVolume(source, musicVolume);
        return source;
    }

    public String playSound(String file, boolean loop, SourceType type) {
        String source = "source_" + nextId++;
        try {
            soundSystem.newSource(false, source, getFile(file).toURI().toURL(), file, loop, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            return null;
        }
        soundSystem.play(source);
        sources.put(source, type);
        return source;
    }

    public String playStreamingSound(String file, boolean loop, SourceType type) {
        String source = "source_" + nextId++;
        try {
            soundSystem.newStreamingSource(false, source, getFile(file).toURI().toURL(), file, loop, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            return null;
        }
        soundSystem.play(source);
        sources.put(source, type);
        return source;
    }

    public String playTemporarySound(String file, boolean loop, SourceType type) {
        String source = playSound(file, loop, type);
        if (source != null) tempSources.add(source);
        return source;
    }

    public String playTemporaryStreamingSound(String file, boolean loop, SourceType type) {
        String source = playStreamingSound(file, loop, type);
        if (source != null) tempSources.add(source);
        return source;
    }

    public void playSound(String source) {
        soundSystem.play(source);
    }

    public void pauseSound(String source) {
        soundSystem.pause(source);
    }

    public void stopSound(String source) {
        soundSystem.stop(source);
    }

    public void rewindSound(String source) {
        soundSystem.rewind(source);
    }

    public void removeSound(String source) {
        soundSystem.stop(source);
        soundSystem.removeSource(source);
        sources.remove(source);
        tempSources.remove(source);
    }

    public void stopTemporarySounds() {
        for (String source : tempSources) {
            soundSystem.removeSource(source);
            sources.remove(source);
        }
        tempSources.clear();
    }

    public float getMasterVolume() {
        return soundSystem.getMasterVolume();
    }

    public void setMasterVolume(float volume) {
        soundSystem.setMasterVolume(volume);
    }

    public float getEffectVolume() {
        return effectVolume;
    }

    public void setEffectVolume(float effectVolume) {
        this.effectVolume = MathHelper.clamp(effectVolume, 0, 1);
        for (Map.Entry<String, SourceType> entry : sources.entrySet()) {
            if (entry.getValue() == SourceType.EFFECT) {
                soundSystem.setVolume(entry.getKey(), effectVolume);
            }
        }
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float musicVolume) {
        this.musicVolume = MathHelper.clamp(musicVolume, 0, 1);
        for (Map.Entry<String, SourceType> entry : sources.entrySet()) {
            if (entry.getValue() == SourceType.MUSIC) {
                soundSystem.setVolume(entry.getKey(), musicVolume);
            }
        }
    }

    public float getSoundVolume(String source) {
        return soundSystem.getPitch(source);
    }

    public float getSoundPitch(String source) {
        return soundSystem.getPitch(source);
    }

    public void setSoundVolume(String source, float volume) {
        soundSystem.setVolume(source, volume);
    }

    public void setSoundPitch(String source, float pitch) {
        soundSystem.setPitch(source, pitch);
    }

    public SoundSystem getSoundSystem() {
        return soundSystem;
    }


    static {
        try {
            SoundSystemConfig.addLibrary(LibraryLWJGLOpenAL.class);
            SoundSystemConfig.setCodec("mp3", CodecJLayerMP3.class);
            SoundSystemConfig.setCodec("ogg", CodecJOgg.class);
            SoundSystemConfig.setCodec("wav", CodecWav.class);
            SoundSystemConfig.setNumberNormalChannels(256);
            SoundSystemConfig.setNumberStreamingChannels(4);
        }
        catch (SoundSystemException ex) {
            ex.printStackTrace();
        }
    }

    public enum SourceType {
        EFFECT,
        MUSIC
    }
}
