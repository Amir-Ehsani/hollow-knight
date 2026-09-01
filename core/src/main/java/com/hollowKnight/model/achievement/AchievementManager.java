package com.hollowKnight.model.achievement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;

public final class AchievementManager {
    private static final String PREFERENCES_NAME = "hollow_knight_achievements";
    private static final float SPEEDRUN_LIMIT_SECONDS = 15f * 60f;

    private static final String[] REQUIRED_ENEMY_TYPES = {
        "CrystalCrawler",
        "Crystallized",
        "FalseKnight",
        "Crawlid",
        "HuskHornhead",
        "Mosquito"
    };

    private static AchievementManager instance;

    private final Preferences preferences;
    private final Array<AchievementListener> listeners;

    private AchievementManager() {
        preferences = Gdx.app.getPreferences(PREFERENCES_NAME);
        listeners = new Array<>();
    }

    public static AchievementManager getInstance() {
        if (instance == null) {
            instance = new AchievementManager();
        }
        return instance;
    }

    public boolean isUnlocked(AchievementId achievement) {
        return achievement != null && preferences.getBoolean(unlockKey(achievement), false);
    }

    public void addListener(AchievementListener listener) {
        if (listener != null && !listeners.contains(listener, true)) {
            listeners.add(listener);
        }
    }

    public void removeListener(AchievementListener listener) {
        if (listener != null) {
            listeners.removeValue(listener, true);
        }
    }

    public boolean unlock(AchievementId achievement) {
        if (achievement == null || isUnlocked(achievement)) {
            return false;
        }

        preferences.putBoolean(unlockKey(achievement), true);
        preferences.flush();

        Array<AchievementListener> snapshot = new Array<>(listeners);
        for (AchievementListener listener : snapshot) {
            listener.onAchievementUnlocked(achievement);
        }
        return true;
    }

    public void recordEnemyKill(String enemyType) {
        if (enemyType == null || enemyType.trim().isEmpty()) {
            return;
        }

        String normalized = normalize(enemyType);
        preferences.putBoolean("enemy_killed_" + normalized, true);
        preferences.flush();

        for (String requiredType : REQUIRED_ENEMY_TYPES) {
            if (!preferences.getBoolean("enemy_killed_" + normalize(requiredType), false)) {
                return;
            }
        }

        unlock(AchievementId.TRUE_HUNTER);
    }

    public void recordSpellUse(String spellName) {
        if (spellName == null) {
            return;
        }

        String normalized = normalize(spellName);
        if (normalized.contains("vengeful")) {
            preferences.putBoolean("used_vengeful_spirit", true);
        } else if (normalized.contains("howling")) {
            preferences.putBoolean("used_howling_wraiths", true);
        } else {
            return;
        }

        preferences.flush();

        if (preferences.getBoolean("used_vengeful_spirit", false)
            && preferences.getBoolean("used_howling_wraiths", false)) {
            unlock(AchievementId.SOUL_MASTER);
        }
    }

    public void recordFalseKnightDefeat(float elapsedSeconds) {
        unlock(AchievementId.FALSE_KNIGHT);
        unlock(AchievementId.COMPLETION);

        if (elapsedSeconds >= 0f && elapsedSeconds <= SPEEDRUN_LIMIT_SECONDS) {
            unlock(AchievementId.SPEEDRUN);
        }
    }

    public float getSpeedrunLimitSeconds() {
        return SPEEDRUN_LIMIT_SECONDS;
    }

    public int getUnlockedCount() {
        int unlocked = 0;
        for (AchievementId achievement : AchievementId.values()) {
            if (isUnlocked(achievement)) {
                unlocked++;
            }
        }
        return unlocked;
    }

    private String unlockKey(AchievementId achievement) {
        return "unlocked_" + achievement.name().toLowerCase();
    }

    private String normalize(String value) {
        return value.trim().toLowerCase().replaceAll("[^a-z0-9]+", "_");
    }
}
