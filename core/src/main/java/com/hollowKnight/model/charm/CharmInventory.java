package com.hollowKnight.model.charm;

import com.badlogic.gdx.utils.ObjectSet;

public class CharmInventory {
    public enum ToggleResult {
        EQUIPPED,
        UNEQUIPPED,
        NOT_COLLECTED,
        NO_FREE_NOTCH
    }

    public static final int MAX_NOTCHES = 3;

    private final ObjectSet<CharmId> collected = new ObjectSet<>();
    private final ObjectSet<CharmId> equipped = new ObjectSet<>();

    public CharmInventory() {
    }

    public CharmInventory(String collectedCsv, String equippedCsv) {
        loadCsv(collectedCsv, collected);
        loadCsv(equippedCsv, equipped);
        removeUncollectedEquippedCharms();
        trimToNotchLimit();
    }

    public boolean collect(CharmId id) {
        return id != null && collected.add(id);
    }

    public boolean isCollected(CharmId id) {
        return id != null && collected.contains(id);
    }

    public boolean isEquipped(CharmId id) {
        return id != null && equipped.contains(id);
    }

    public ToggleResult toggle(CharmId id) {
        if (id == null || !collected.contains(id)) {
            return ToggleResult.NOT_COLLECTED;
        }

        if (equipped.remove(id)) {
            return ToggleResult.UNEQUIPPED;
        }

        if (getUsedNotches() + id.getNotchCost() > MAX_NOTCHES) {
            return ToggleResult.NO_FREE_NOTCH;
        }

        equipped.add(id);
        return ToggleResult.EQUIPPED;
    }

    public boolean equip(CharmId id) {
        if (id == null || !collected.contains(id) || equipped.contains(id)) {
            return false;
        }

        if (getUsedNotches() + id.getNotchCost() > MAX_NOTCHES) {
            return false;
        }

        equipped.add(id);
        return true;
    }

    public boolean unequip(CharmId id) {
        return id != null && equipped.remove(id);
    }

    public int getUsedNotches() {
        int used = 0;
        for (CharmId id : equipped) {
            used += id.getNotchCost();
        }
        return used;
    }

    public int getFreeNotches() {
        return Math.max(0, MAX_NOTCHES - getUsedNotches());
    }

    public int getCollectedCount() {
        return collected.size;
    }

    public String serializeCollected() {
        return serialize(collected);
    }

    public String serializeEquipped() {
        return serialize(equipped);
    }

    private void removeUncollectedEquippedCharms() {
        ObjectSet<CharmId> invalid = new ObjectSet<>();
        for (CharmId id : equipped) {
            if (!collected.contains(id)) {
                invalid.add(id);
            }
        }
        for (CharmId id : invalid) {
            equipped.remove(id);
        }
    }

    private void trimToNotchLimit() {
        while (getUsedNotches() > MAX_NOTCHES) {
            CharmId last = null;
            for (CharmId id : equipped) {
                last = id;
            }
            if (last == null) {
                break;
            }
            equipped.remove(last);
        }
    }

    private static void loadCsv(String csv, ObjectSet<CharmId> target) {
        if (csv == null || csv.trim().isEmpty()) {
            return;
        }

        String[] parts = csv.split(",");
        for (String part : parts) {
            CharmId id = CharmId.fromString(part);
            if (id != null) {
                target.add(id);
            }
        }
    }

    private static String serialize(ObjectSet<CharmId> set) {
        StringBuilder builder = new StringBuilder();
        for (CharmId id : CharmId.values()) {
            if (!set.contains(id)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(id.name());
        }
        return builder.toString();
    }
}
