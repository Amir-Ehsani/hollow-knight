package com.hollowKnight.save;

import com.badlogic.gdx.Gdx;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class SaveManager {

    public static class SaveData {
        public int slotId;
        public String mapPath;
        public float playerX;
        public float playerY;
        public float lastSafeX;
        public float lastSafeY;
        public int currentHealth;
        public int currentSoul;
        public String collectedCharms;
        public String equippedCharms;
        public long savedAt;

        public SaveData() {
            slotId = 1;
            collectedCharms = "";
            equippedCharms = "";
        }

        public SaveData(int slotId, String mapPath, float playerX, float playerY, float lastSafeX, float lastSafeY, int currentHealth, int currentSoul) {
            this(slotId, mapPath, playerX, playerY, lastSafeX, lastSafeY, currentHealth, currentSoul, "", "");
        }

        public SaveData(
            int slotId,
            String mapPath,
            float playerX,
            float playerY,
            float lastSafeX,
            float lastSafeY,
            int currentHealth,
            int currentSoul,
            String collectedCharms,
            String equippedCharms
        ) {
            this.slotId = normalizeSlot(slotId);
            this.mapPath = mapPath;
            this.playerX = playerX;
            this.playerY = playerY;
            this.lastSafeX = lastSafeX;
            this.lastSafeY = lastSafeY;
            this.currentHealth = currentHealth;
            this.currentSoul = currentSoul;
            this.collectedCharms = collectedCharms == null ? "" : collectedCharms;
            this.equippedCharms = equippedCharms == null ? "" : equippedCharms;
            this.savedAt = System.currentTimeMillis();
        }

        public SaveData(String mapPath, float playerX, float playerY, float lastSafeX, float lastSafeY, int currentHealth, int currentSoul) {
            this(1, mapPath, playerX, playerY, lastSafeX, lastSafeY, currentHealth, currentSoul);
        }
    }

    private static final int DEFAULT_SLOT_ID = 1;
    private final String databaseUrl;

    public SaveManager() {
        this.databaseUrl = "jdbc:sqlite:" + Gdx.files.local("savegame.db").file().getAbsolutePath();
        initialize();
    }

    private Connection openConnection() throws Exception {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection(databaseUrl);
    }

    private void initialize() {
        try (Connection connection = openConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS save_game (" +
                        "slot_id INTEGER PRIMARY KEY," +
                        "map_path TEXT NOT NULL," +
                        "player_x REAL NOT NULL," +
                        "player_y REAL NOT NULL," +
                        "last_safe_x REAL NOT NULL," +
                        "last_safe_y REAL NOT NULL," +
                        "current_health INTEGER NOT NULL," +
                        "current_soul INTEGER NOT NULL," +
                        "collected_charms TEXT NOT NULL DEFAULT ''," +
                        "equipped_charms TEXT NOT NULL DEFAULT ''," +
                        "saved_at INTEGER NOT NULL" +
                        ")"
                );
            }

            ensureColumn(connection, "save_game", "collected_charms", "TEXT NOT NULL DEFAULT ''");
            ensureColumn(connection, "save_game", "equipped_charms", "TEXT NOT NULL DEFAULT ''");
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize SQLite save database", e);
        }
    }

    private void ensureColumn(Connection connection, String table, String column, String definition) throws Exception {
        boolean exists = false;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (resultSet.next()) {
                if (column.equalsIgnoreCase(resultSet.getString("name"))) {
                    exists = true;
                    break;
                }
            }
        }

        if (!exists) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
            }
        }
    }

    public void saveGame(SaveData data) {
        if (data == null) {
            return;
        }

        data.slotId = normalizeSlot(data.slotId);
        data.savedAt = System.currentTimeMillis();

        String sql = "INSERT OR REPLACE INTO save_game " +
            "(slot_id, map_path, player_x, player_y, last_safe_x, last_safe_y, current_health, current_soul, collected_charms, equipped_charms, saved_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, data.slotId);
            statement.setString(2, data.mapPath == null ? "" : data.mapPath);
            statement.setFloat(3, data.playerX);
            statement.setFloat(4, data.playerY);
            statement.setFloat(5, data.lastSafeX);
            statement.setFloat(6, data.lastSafeY);
            statement.setInt(7, data.currentHealth);
            statement.setInt(8, data.currentSoul);
            statement.setString(9, data.collectedCharms == null ? "" : data.collectedCharms);
            statement.setString(10, data.equippedCharms == null ? "" : data.equippedCharms);
            statement.setLong(11, data.savedAt);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Could not save game", e);
        }
    }

    public void saveGame(int slotId, SaveData data) {
        if (data == null) {
            return;
        }

        data.slotId = normalizeSlot(slotId);
        saveGame(data);
    }

    public SaveData loadGame() {
        return loadGame(DEFAULT_SLOT_ID);
    }

    public SaveData loadGame(int slotId) {
        String sql = "SELECT slot_id, map_path, player_x, player_y, last_safe_x, last_safe_y, current_health, current_soul, collected_charms, equipped_charms, saved_at FROM save_game WHERE slot_id = ?";

        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, normalizeSlot(slotId));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                SaveData data = new SaveData();
                data.slotId = resultSet.getInt("slot_id");
                data.mapPath = resultSet.getString("map_path");
                data.playerX = resultSet.getFloat("player_x");
                data.playerY = resultSet.getFloat("player_y");
                data.lastSafeX = resultSet.getFloat("last_safe_x");
                data.lastSafeY = resultSet.getFloat("last_safe_y");
                data.currentHealth = resultSet.getInt("current_health");
                data.currentSoul = resultSet.getInt("current_soul");
                data.collectedCharms = resultSet.getString("collected_charms");
                data.equippedCharms = resultSet.getString("equipped_charms");
                data.savedAt = resultSet.getLong("saved_at");
                return data;
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not load game", e);
        }
    }

    public boolean hasSave() {
        return hasSave(DEFAULT_SLOT_ID);
    }

    public boolean hasSave(int slotId) {
        String sql = "SELECT 1 FROM save_game WHERE slot_id = ?";

        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, normalizeSlot(slotId));

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not check save game", e);
        }
    }

    public void deleteSave() {
        deleteSave(DEFAULT_SLOT_ID);
    }

    public void deleteSave(int slotId) {
        String sql = "DELETE FROM save_game WHERE slot_id = ?";

        try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, normalizeSlot(slotId));
            statement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Could not delete save game", e);
        }
    }

    public void deleteAllSaves() {
        try (Connection connection = openConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM save_game");
        } catch (Exception e) {
            throw new RuntimeException("Could not delete all save games", e);
        }
    }

    public static int normalizeSlot(int slotId) {
        if (slotId < 1) {
            return 1;
        }

        if (slotId > 4) {
            return 4;
        }

        return slotId;
    }
}
