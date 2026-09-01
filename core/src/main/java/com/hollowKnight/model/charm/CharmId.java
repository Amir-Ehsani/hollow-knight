package com.hollowKnight.model.charm;

import java.util.Locale;

public enum CharmId {
    SOUL_CATCHER(
        "Soul Catcher",
        "Successful Nail hits grant more Soul.",
        false,
        new String[]{"soul catcher", "soul_more_soul", "more_soul"}
    ),
    DASHMASTER(
        "Dashmaster",
        "Reduces Dash cooldown, allowing more frequent dashes.",
        false,
        new String[]{"dashmaster", "charm_generic_03"}
    ),
    UNBREAKABLE_STRENGTH(
        "Unbreakable Strength",
        "Increases damage dealt by normal Nail attacks.",
        false,
        new String[]{"unbreakable strength", "glass_attack_up_full", "attack_up_full"}
    ),
    QUICK_SLASH(
        "Quick Slash",
        "Greatly reduces the cooldown between Nail attacks.",
        false,
        new String[]{"quick slash", "spiral_slash_speed_up", "slash_speed"}
    ),
    QUICK_FOCUS(
        "Quick Focus",
        "Shortens the time required to Focus and heal.",
        false,
        new String[]{"quick focus", "fast_focus"}
    ),
    HEAVY_BLOW(
        "Heavy Blow",
        "Nail attacks knock enemies farther away.",
        true,
        new String[]{"heavy blow", "nail_damage_up"}
    ),
    SHARP_SHADOW(
        "Sharp Shadow",
        "Dash through enemies without taking contact damage. The dash is 20% longer and damages each enemy once.",
        true,
        new String[]{"sharp shadow", "shadow_impact"}
    ),
    VOID_HEART(
        "Void Heart",
        "Empowers spells, increasing their damage and using a dark visual effect.",
        true,
        new String[]{"void heart", "charm_black"}
    );

    private final String displayName;
    private final String description;
    private final boolean optional;
    private final String[] assetHints;

    CharmId(String displayName, String description, boolean optional, String[] assetHints) {
        this.displayName = displayName;
        this.description = description;
        this.optional = optional;
        this.assetHints = assetHints;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isOptional() {
        return optional;
    }

    public int getNotchCost() {
        return 1;
    }

    public String[] getAssetHints() {
        return assetHints;
    }

    public String getSaveKey() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static CharmId fromString(Object value) {
        if (value == null) {
            return null;
        }

        String normalized = normalize(String.valueOf(value));
        if (normalized.isEmpty()) {
            return null;
        }

        for (CharmId id : values()) {
            if (normalized.equals(normalize(id.name())) ||
                normalized.equals(normalize(id.displayName)) ||
                normalized.contains(normalize(id.displayName))) {
                return id;
            }

            for (String hint : id.assetHints) {
                if (normalized.contains(normalize(hint))) {
                    return id;
                }
            }
        }

        return null;
    }

    private static String normalize(String value) {
        return value == null
            ? ""
            : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");
    }
}
