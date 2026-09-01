package com.hollowKnight.view.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.MathUtils;

public class GameSettings {
    private static final String PREF_NAME = "hollow_knight_settings";
    private static final String KEY_MUSIC_VOLUME = "music_volume";
    private static final String KEY_SFX_VOLUME = "sfx_volume";
    private static final String KEY_MUSIC_ENABLED = "music_enabled";
    private static final String KEY_SFX_ENABLED = "sfx_enabled";
    private static final String KEY_BRIGHTNESS = "brightness";
    private static final float STEP = 0.05f;

    private static boolean loaded = false;
    private static float musicVolume = 0.7f;
    private static float sfxVolume = 0.8f;
    private static boolean musicEnabled = true;
    private static boolean sfxEnabled = true;
    private static float brightness = 1.0f;

    private GameSettings() {
    }

    public static void load() {
        if (loaded) {
            return;
        }

        try {
            Preferences preferences = Gdx.app.getPreferences(PREF_NAME);
            musicVolume = snapToStep(MathUtils.clamp(preferences.getFloat(KEY_MUSIC_VOLUME, musicVolume), 0f, 1f));
            sfxVolume = snapToStep(MathUtils.clamp(preferences.getFloat(KEY_SFX_VOLUME, sfxVolume), 0f, 1f));
            musicEnabled = preferences.getBoolean(KEY_MUSIC_ENABLED, musicEnabled);
            sfxEnabled = preferences.getBoolean(KEY_SFX_ENABLED, sfxEnabled);
            brightness = snapToStep(MathUtils.clamp(preferences.getFloat(KEY_BRIGHTNESS, brightness), 0f, 1f));
        } catch (Exception ignored) {
        }

        loaded = true;
    }

    public static void save() {
        try {
            Preferences preferences = Gdx.app.getPreferences(PREF_NAME);
            preferences.putFloat(KEY_MUSIC_VOLUME, musicVolume);
            preferences.putFloat(KEY_SFX_VOLUME, sfxVolume);
            preferences.putBoolean(KEY_MUSIC_ENABLED, musicEnabled);
            preferences.putBoolean(KEY_SFX_ENABLED, sfxEnabled);
            preferences.putFloat(KEY_BRIGHTNESS, brightness);
            preferences.flush();
        } catch (Exception ignored) {
        }
    }

    public static String getCheatGuideText() {
        return "Ctrl + 1  - Teleport to the False Knight arena\n" +
            "Ctrl + 2  - Toggle Noclip / Spectator mode\n" +
            "Ctrl + 3  - Emergency heal: restore one health mask\n" +
            "Ctrl + 4  - Refill the Soul vessel\n" +
            "Ctrl + 5  - Toggle God Mode\n" +
            "Ctrl + 6  - Insta-kill enemies currently visible on screen";
    }

    public static void setMusicVolume(float value) {
        load();
        musicVolume = snapToStep(MathUtils.clamp(value, 0f, 1f));
        save();
    }

    public static void addMusicVolume(float amount) {
        setMusicVolume(musicVolume + amount);
    }

    public static void addMusicStep(int direction) {
        setMusicVolume(musicVolume + (STEP * direction));
    }

    public static void setSfxVolume(float value) {
        load();
        sfxVolume = snapToStep(MathUtils.clamp(value, 0f, 1f));
        save();
    }

    public static void addSfxVolume(float amount) {
        setSfxVolume(sfxVolume + amount);
    }

    public static void addSfxStep(int direction) {
        setSfxVolume(sfxVolume + (STEP * direction));
    }

    public static void setBrightness(float value) {
        load();
        brightness = snapToStep(MathUtils.clamp(value, 0f, 1f));
        save();
    }

    public static void addBrightness(float amount) {
        setBrightness(brightness + amount);
    }

    public static void addBrightnessStep(int direction) {
        setBrightness(brightness + (STEP * direction));
    }

    public static void toggleMusic() {
        load();
        musicEnabled = !musicEnabled;
        save();
    }

    public static void toggleSfx() {
        load();
        sfxEnabled = !sfxEnabled;
        save();
    }

    public static void reset() {
        musicVolume = 0.7f;
        sfxVolume = 0.8f;
        musicEnabled = true;
        sfxEnabled = true;
        brightness = 1.0f;
        loaded = true;
        save();
    }

    public static float getMusicVolume() {
        load();
        return musicVolume;
    }

    public static float getSfxVolume() {
        load();
        return sfxVolume;
    }

    public static boolean isMusicEnabled() {
        load();
        return musicEnabled;
    }

    public static boolean isSfxEnabled() {
        load();
        return sfxEnabled;
    }

    public static float getBrightness() {
        load();
        return brightness;
    }

    public static float getMusicOutputVolume(float baseVolume) {
        load();
        if (!musicEnabled) {
            return 0f;
        }
        return baseVolume * musicVolume;
    }

    public static float getSfxOutputVolume(float baseVolume) {
        load();
        if (!sfxEnabled) {
            return 0f;
        }
        return baseVolume * sfxVolume;
    }

    public static float getWorldDarknessAlpha() {
        load();
        return MathUtils.clamp((1f - brightness) * 0.8f, 0f, 0.8f);
    }

    public static String getMusicLabel() {
        load();
        return Math.round(musicVolume * 100f) + "%";
    }

    public static String getSfxLabel() {
        load();
        return Math.round(sfxVolume * 100f) + "%";
    }

    public static String getBrightnessLabel() {
        load();
        return Math.round(brightness * 100f) + "%";
    }

    public static String getMusicMuteLabel() {
        load();
        return musicEnabled ? "mute" : "unmute";
    }

    public static String getSfxMuteLabel() {
        load();
        return sfxEnabled ? "mute" : "unmute";
    }

    private static float snapToStep(float value) {
        return MathUtils.clamp(Math.round(value / STEP) * STEP, 0f, 1f);
    }
}
