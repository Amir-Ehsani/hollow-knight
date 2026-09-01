package com.hollowKnight.model.achievement;

public enum AchievementId {
    COMPLETION(
        "Completion",
        "Finish the game by defeating the final boss.",
        "Hollow Knight sprites/Achievements/achievement__0000_100_complete.png"
    ),
    SPEEDRUN(
        "Speedrun",
        "Finish the game in 15 minutes or less.",
        "Hollow Knight sprites/Achievements/achievement_fast_finish.png"
    ),
    TRUE_HUNTER(
        "True Hunter",
        "Defeat every enemy type implemented in the game.",
        "Hollow Knight sprites/Achievements/achievement_Hunter_Journal.png"
    ),
    FALSE_KNIGHT(
        "Defeat False Knight",
        "Defeat the False Knight boss.",
        "Hollow Knight sprites/Achievements/achievement_false_knight.png"
    ),
    SOUL_MASTER(
        "Soul Master",
        "Use both Vengeful Spirit and Howling Wraiths.",
        "Hollow Knight sprites/Achievements/achievement_steel_soul_complete.png"
    );

    private final String title;
    private final String description;
    private final String iconPath;

    AchievementId(String title, String description, String iconPath) {
        this.title = title;
        this.description = description;
        this.iconPath = iconPath;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getIconPath() {
        return iconPath;
    }
}
