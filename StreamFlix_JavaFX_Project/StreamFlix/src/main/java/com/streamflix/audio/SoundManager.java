package com.streamflix.audio;

import javafx.scene.media.AudioClip;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

/**
 * SoundManager - plays the short sound effects (hover tick, click, whoosh ...).
 *
 * PORTION: Audio & Loading animation  |  OWNER: Lethako Khabele
 *
 * Design: one central class so that no other class needs to know about files or AudioClip.
 * Anywhere in the app you just write   SoundManager.play(Sound.CLICK);
 * If a sound file is missing, or the computer has no audio device, the app keeps working silently.
 */
public final class SoundManager {

    /** Every sound effect in the app, with its file name and default volume (0.0 - 1.0). */
    public enum Sound {
        HOVER("hover.wav", 0.30),
        CLICK("click.wav", 0.60),
        OPEN("open.wav", 0.70),
        BACK("back.wav", 0.60),
        SCROLL("scroll.wav", 0.45),
        TOGGLE("toggle.wav", 0.55),
        STARTUP("startup.wav", 0.75);

        private final String file;
        private final double volume;

        Sound(String file, double volume) {
            this.file = file;
            this.volume = volume;
        }
    }

    private static final Map<Sound, AudioClip> CLIPS = new EnumMap<>(Sound.class);
    private static boolean loaded = false;
    private static boolean muted = false;
    private static long lastHoverTime = 0;

    private SoundManager() { }

    /** Loads all clips once. Called at start-up (safe to call again). */
    public static synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        for (Sound s : Sound.values()) {
            try {
                URL url = SoundManager.class.getResource("/sounds/" + s.file);
                if (url == null) {
                    System.err.println("Sound file missing: " + s.file);
                    continue;
                }
                AudioClip clip = new AudioClip(url.toExternalForm());
                clip.setVolume(s.volume);
                CLIPS.put(s, clip);
            } catch (Exception e) {
                System.err.println("Could not load sound " + s.file + ": " + e.getMessage());
            }
        }
    }

    public static void play(Sound sound) {
        if (muted) {
            return;
        }
        if (!loaded) {
            load();
        }
        if (sound == Sound.HOVER) {                       // stop rapid hover "machine-gun" ticks
            long now = System.currentTimeMillis();
            if (now - lastHoverTime < 70) {
                return;
            }
            lastHoverTime = now;
        }
        AudioClip clip = CLIPS.get(sound);
        if (clip != null) {
            clip.play();
        }
    }

    public static boolean isMuted()          { return muted; }
    public static void setMuted(boolean m)   { muted = m; }
}
