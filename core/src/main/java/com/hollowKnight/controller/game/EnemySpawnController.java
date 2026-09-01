package com.hollowKnight.controller.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hollowKnight.Main;
import com.hollowKnight.config.ControlBindings;
import com.hollowKnight.controller.KnightController;
import com.hollowKnight.model.Damageable;
import com.hollowKnight.model.Knight;
import com.hollowKnight.model.achievement.AchievementManager;
import com.hollowKnight.model.charm.CharmId;
import com.hollowKnight.model.charm.CharmInventory;
import com.hollowKnight.model.charm.CharmPickup;
import com.hollowKnight.model.enemy.CrystalCrawler;
import com.hollowKnight.model.enemy.Crystallized;
import com.hollowKnight.model.enemy.FalseKnight;
import com.hollowKnight.model.enemy.Crawlid;
import com.hollowKnight.model.enemy.HuskHornhead;
import com.hollowKnight.model.enemy.Mosquito;
import com.hollowKnight.model.npc.Zote;
import com.hollowKnight.model.spell.HowlingWraithsCast;
import com.hollowKnight.model.spell.VengefulSpiritProjectile;
import com.hollowKnight.save.SaveManager;
import com.hollowKnight.view.hud.GameHud;
import com.hollowKnight.view.hud.AchievementNotification;
import com.hollowKnight.view.hud.InventoryOverlay;
import com.hollowKnight.view.hud.ZoteDialogueBox;
import com.hollowKnight.view.enemies.CrystalCrawlerRenderer;
import com.hollowKnight.view.enemies.CrystallizedRenderer;
import com.hollowKnight.view.enemies.FalseKnightRenderer;
import com.hollowKnight.view.enemies.CrawlidRenderer;
import com.hollowKnight.view.enemies.HuskHornheadRenderer;
import com.hollowKnight.view.enemies.MosquitoRenderer;
import com.hollowKnight.view.npc.ZoteRenderer;
import com.hollowKnight.view.effects.TiledParticleManager;
import com.hollowKnight.view.effects.CombatEffectManager;
import com.hollowKnight.view.charm.CharmIconLibrary;

import com.hollowKnight.view.screens.EndGameScreen;
import com.hollowKnight.view.screens.GameSettings;
import java.util.HashMap;
import java.util.Map;

abstract class EnemySpawnController extends MapController {
    protected EnemySpawnController(Game game, int saveSlot, boolean loadSavedGame) {
        super(game, saveSlot, loadSavedGame);
    }

    protected void loadEnemiesFromTiled() {
            MapLayer enemiesLayer = map.getLayers().get("Enemies");

            if (enemiesLayer == null) {
                enemiesLayer = map.getLayers().get("Enemy");
            }

            if (enemiesLayer == null) {
                return;
            }

            for (MapObject object : enemiesLayer.getObjects()) {
                if (isZoteObject(object)) {
                    addZoteObject(object);
                } else if (isFalseKnightObject(object)) {
                    addFalseKnightObject(object);
                } else if (isCrystallizedObject(object)) {
                    addCrystallizedObject(object);
                } else if (isCrawlidObject(object)) {
                    addCrawlidObject(object);
                } else if (isHuskHornheadObject(object)) {
                    addHuskHornheadObject(object);
                } else if (isCrystalCrawlerObject(object)) {
                    addCrystalCrawlerObject(object);
                } else if (isMosquitoObject(object)) {
                    addMosquitoObject(object);
                } else {
                    System.out.println("Unknown enemy object in Tiled: name=" + object.getName() + ", properties=" + object.getProperties());
                }
            }
        }

    protected void loadFalseKnightsFromTiled() {
            String[] layerNames = {"Bosses", "Boss", "FalseKnight", "False Knight", "False_knight"};

            for (String layerName : layerNames) {
                MapLayer layer = map.getLayers().get(layerName);

                if (layer == null) {
                    continue;
                }

                for (MapObject object : layer.getObjects()) {
                    if (isFalseKnightObject(object)) {
                        addFalseKnightObject(object);
                    }
                }
            }
        }

    protected void addFalseKnightObject(MapObject object) {
            if (!(object instanceof RectangleMapObject)) {
                System.out.println("FalseKnight must be a Rectangle Object in Tiled: " + object.getName());
                return;
            }

            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            MapProperties properties = object.getProperties();
            float width = rect.width > 1f ? rect.width : FalseKnight.DEFAULT_HITBOX_WIDTH;
            float height = rect.height > 1f ? rect.height : FalseKnight.DEFAULT_HITBOX_HEIGHT;
            width = getFloatProperty(properties, width, "hitboxWidth", "hitbox_width", "bodyWidth", "body_width");
            height = getFloatProperty(properties, height, "hitboxHeight", "hitbox_height", "bodyHeight", "body_height");

            int health = getIntProperty(properties, FalseKnight.DEFAULT_HEALTH, "hp", "health");
            float scale = getFloatProperty(properties, FalseKnight.DEFAULT_DRAW_SCALE, "scale", "drawScale", "draw_scale");
            float spriteYOffset = getFloatProperty(properties, FalseKnight.DEFAULT_SPRITE_Y_OFFSET, "spriteYOffset", "sprite_y_offset", "drawYOffset", "draw_y_offset");
            float detectionRange = getFloatProperty(properties, FalseKnight.DEFAULT_DETECTION_RANGE, "detectionRange", "detectRange", "range", "viewRange", "view_range");
            int direction = getDirectionProperty(properties, 1);
            Rectangle arena = getFalseKnightArenaBounds(properties, rect, width, height);
            float spawnX = rect.width > 1f ? rect.x + rect.width / 2f - width / 2f : rect.x;
            float spawnY = rect.y;

            FalseKnight falseKnight = new FalseKnight(spawnX, spawnY, width, height, health, direction, scale, detectionRange, arena, spriteYOffset);
            falseKnights.add(falseKnight);
            enemies.add(falseKnight);
        }

    protected Rectangle getFalseKnightArenaBounds(MapProperties properties, Rectangle rect, float width, float height) {
            float bossCenterX = rect.x + Math.max(rect.width, width) / 2f;
            float bossCenterY = rect.y + Math.max(rect.height, height) / 2f;
            Rectangle containingRoom = findRoomContaining(bossCenterX, bossCenterY);

            if (properties != null && isTrueProperty(properties.get("useCurrentRoomArena")) && containingRoom != null) {
                return new Rectangle(containingRoom);
            }

            boolean arenaEnabled = properties != null && (
                isTrueProperty(properties.get("enableBossArena")) ||
                    isTrueProperty(properties.get("enable_boss_arena")) ||
                    isTrueProperty(properties.get("clampArena")) ||
                    isTrueProperty(properties.get("clamp_arena"))
            );

            if (!arenaEnabled && containingRoom != null) {
                return new Rectangle(containingRoom);
            }

            float padding = getFloatProperty(properties, FalseKnight.DEFAULT_ARENA_PADDING, "arenaPadding", "arena_padding");
            float arenaX = getFloatProperty(properties, rect.x - padding, "arenaX", "arena_x", "bossArenaX", "boss_arena_x");
            float arenaY = getFloatProperty(properties, Math.max(0f, rect.y - 8f), "arenaY", "arena_y", "bossArenaY", "boss_arena_y");
            float arenaWidth = getFloatProperty(properties, width + padding * 2f, "arenaWidth", "arena_width", "bossArenaWidth", "boss_arena_width");
            float arenaHeight = getFloatProperty(properties, Math.max(480f, height + 260f), "arenaHeight", "arena_height", "bossArenaHeight", "boss_arena_height");

            return new Rectangle(
                arenaX,
                arenaY,
                Math.max(width + 64f, arenaWidth),
                Math.max(height + 96f, arenaHeight)
            );
        }

    protected Rectangle findRoomContaining(float x, float y) {
            if (rooms == null) {
                return null;
            }

            for (Rectangle room : rooms) {
                if (room != null && room.contains(x, y)) {
                    return room;
                }
            }

            return null;
        }

    protected void addCrystallizedObject(MapObject object) {
            if (!(object instanceof RectangleMapObject)) {
                System.out.println("Crystallized must be a Rectangle Object in Tiled: " + object.getName());
                return;
            }

            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            MapProperties properties = object.getProperties();

            float width = getFloatProperty(
                properties,
                Crystallized.DEFAULT_HITBOX_WIDTH,
                "width",
                "hitboxWidth",
                "hitbox_width"
            );

            float height = getFloatProperty(
                properties,
                Crystallized.DEFAULT_HITBOX_HEIGHT,
                "height",
                "hitboxHeight",
                "hitbox_height"
            );

            if (rect.width > 1f) {
                width = rect.width;
            }

            if (rect.height > 1f) {
                height = rect.height;
            }

            int health = getIntProperty(properties, Crystallized.DEFAULT_HEALTH, "hp", "health");
            float speed = getFloatProperty(properties, Crystallized.DEFAULT_SPEED, "speed");
            float evadeSpeed = getFloatProperty(properties, Crystallized.DEFAULT_EVADE_SPEED, "evadeSpeed", "evade_speed");
            float detectionRange = getFloatProperty(properties, Crystallized.DEFAULT_DETECTION_RANGE, "detectionRange", "detectRange", "range", "viewRange", "view_range");
            float shootRange = getFloatProperty(properties, Crystallized.DEFAULT_SHOOT_RANGE, "shootRange", "shoot_range", "attackRange", "attack_range");
            float laserLength = getFloatProperty(properties, Crystallized.DEFAULT_LASER_LENGTH, "laserLength", "laser_length");
            float scale = getFloatProperty(properties, Crystallized.DEFAULT_DRAW_SCALE, "scale", "drawScale", "draw_scale");
            int direction = getDirectionProperty(properties, 1);

            Crystallized crystallized = new Crystallized(
                rect.x,
                rect.y,
                width,
                height,
                health,
                speed,
                evadeSpeed,
                detectionRange,
                shootRange,
                laserLength,
                direction,
                scale
            );

            crystallizedEnemies.add(crystallized);
            enemies.add(crystallized);
        }

    protected void addCrystalCrawlerObject(MapObject object) {
            if (!(object instanceof RectangleMapObject)) {
                System.out.println("CrystalCrawler must be a Rectangle Object in Tiled: " + object.getName());
                return;
            }

            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            MapProperties properties = object.getProperties();

            float width = getFloatProperty(
                properties,
                CrystalCrawler.DEFAULT_HITBOX_WIDTH,
                "width",
                "hitboxWidth",
                "hitbox_width"
            );

            float height = getFloatProperty(
                properties,
                CrystalCrawler.DEFAULT_HITBOX_HEIGHT,
                "height",
                "hitboxHeight",
                "hitbox_height"
            );

            if (rect.width > 1f) {
                width = rect.width;
            }

            if (rect.height > 1f) {
                height = rect.height;
            }

            int health = getIntProperty(properties, CrystalCrawler.DEFAULT_HEALTH, "hp", "health");
            float speed = getFloatProperty(properties, CrystalCrawler.DEFAULT_SPEED, "speed");
            float scale = getFloatProperty(properties, CrystalCrawler.DEFAULT_DRAW_SCALE, "scale", "drawScale", "draw_scale");
            int direction = getDirectionProperty(properties, 1);

            CrystalCrawler crawler = new CrystalCrawler(
                rect.x,
                rect.y,
                width,
                height,
                health,
                speed,
                direction,
                scale
            );

            crystalCrawlers.add(crawler);
            enemies.add(crawler);
        }

    protected void addCrawlidObject(MapObject object) {
            if (!(object instanceof RectangleMapObject)) {
                System.out.println("Crawlid must be a Rectangle Object in Tiled: " + object.getName());
                return;
            }

            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            MapProperties properties = object.getProperties();

            float width = getFloatProperty(
                properties,
                Crawlid.DEFAULT_HITBOX_WIDTH,
                "width",
                "hitboxWidth",
                "hitbox_width"
            );

            float height = getFloatProperty(
                properties,
                Crawlid.DEFAULT_HITBOX_HEIGHT,
                "height",
                "hitboxHeight",
                "hitbox_height"
            );

            if (rect.width > 1f) {
                width = rect.width;
            }

            if (rect.height > 1f) {
                height = rect.height;
            }

            int health = getIntProperty(properties, Crawlid.DEFAULT_HEALTH, "hp", "health");
            float speed = getFloatProperty(properties, Crawlid.DEFAULT_SPEED, "speed");
            float scale = getFloatProperty(properties, Crawlid.DEFAULT_DRAW_SCALE, "scale", "drawScale", "draw_scale");
            int direction = getDirectionProperty(properties, 1);

            Crawlid crawlid = new Crawlid(
                rect.x,
                rect.y,
                width,
                height,
                health,
                speed,
                direction,
                scale
            );

            crawlids.add(crawlid);
            enemies.add(crawlid);
        }

    protected void addHuskHornheadObject(MapObject object) {
            if (!(object instanceof RectangleMapObject)) {
                System.out.println("HuskHornhead must be a Rectangle Object in Tiled: " + object.getName());
                return;
            }

            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            MapProperties properties = object.getProperties();

            float width = getFloatProperty(
                properties,
                HuskHornhead.DEFAULT_HITBOX_WIDTH,
                "width",
                "hitboxWidth",
                "hitbox_width"
            );

            float height = getFloatProperty(
                properties,
                HuskHornhead.DEFAULT_HITBOX_HEIGHT,
                "height",
                "hitboxHeight",
                "hitbox_height"
            );

            if (rect.width > 1f) {
                width = rect.width;
            }

            if (rect.height > 1f) {
                height = rect.height;
            }

            int health = getIntProperty(properties, HuskHornhead.DEFAULT_HEALTH, "hp", "health");
            float speed = getFloatProperty(properties, HuskHornhead.DEFAULT_SPEED, "speed");
            float lungeSpeed = getFloatProperty(properties, HuskHornhead.DEFAULT_LUNGE_SPEED, "lungeSpeed", "lunge_speed", "attackSpeed", "attack_speed");
            float detectionRange = getFloatProperty(properties, HuskHornhead.DEFAULT_DETECTION_RANGE, "detectionRange", "detectRange", "range", "viewRange", "view_range");
            float attackRange = getFloatProperty(properties, HuskHornhead.DEFAULT_ATTACK_RANGE, "attackRange", "attack_range");
            float scale = getFloatProperty(properties, HuskHornhead.DEFAULT_DRAW_SCALE, "scale", "drawScale", "draw_scale");
            int direction = getDirectionProperty(properties, 1);

            HuskHornhead huskHornhead = new HuskHornhead(
                rect.x,
                rect.y,
                width,
                height,
                health,
                speed,
                lungeSpeed,
                detectionRange,
                attackRange,
                direction,
                scale
            );

            huskHornheads.add(huskHornhead);
            enemies.add(huskHornhead);
        }

    protected void addMosquitoObject(MapObject object) {
            if (!(object instanceof RectangleMapObject)) {
                System.out.println("Mosquito must be a Rectangle Object in Tiled: " + object.getName());
                return;
            }

            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            MapProperties properties = object.getProperties();

            float width = getFloatProperty(
                properties,
                Mosquito.DEFAULT_HITBOX_WIDTH,
                "width",
                "hitboxWidth",
                "hitbox_width"
            );

            float height = getFloatProperty(
                properties,
                Mosquito.DEFAULT_HITBOX_HEIGHT,
                "height",
                "hitboxHeight",
                "hitbox_height"
            );

            if (isTrueProperty(properties.get("useObjectSize")) || isTrueProperty(properties.get("use_object_size"))) {
                if (rect.width > 1f) {
                    width = rect.width;
                }

                if (rect.height > 1f) {
                    height = rect.height;
                }
            }

            int health = getIntProperty(properties, Mosquito.DEFAULT_HEALTH, "hp", "health");
            float speed = getFloatProperty(properties, Mosquito.DEFAULT_SPEED, "speed");
            float chargeSpeed = getFloatProperty(properties, Mosquito.DEFAULT_CHARGE_SPEED, "chargeSpeed", "charge_speed", "attackSpeed", "attack_speed");
            float detectionRange = getFloatProperty(properties, Mosquito.DEFAULT_DETECTION_RANGE, "detectionRange", "detectRange", "range", "viewRange", "view_range");
            float scale = getFloatProperty(properties, Mosquito.DEFAULT_DRAW_SCALE, "scale", "drawScale", "draw_scale");
            int direction = getDirectionProperty(properties, 1);

            Mosquito mosquito = new Mosquito(
                rect.x,
                rect.y,
                width,
                height,
                health,
                speed,
                chargeSpeed,
                detectionRange,
                direction,
                scale
            );

            mosquitoes.add(mosquito);
            enemies.add(mosquito);
        }

    protected boolean isFalseKnightObject(MapObject object) {
            if (object == null) {
                return false;
            }

            if (containsFalseKnightWord(object.getName())) {
                return true;
            }

            MapProperties properties = object.getProperties();
            Object typeProperty = properties.get("type");

            if (containsFalseKnightWord(typeProperty)) {
                return true;
            }

            Object classProperty = properties.get("class");

            if (containsFalseKnightWord(classProperty)) {
                return true;
            }

            Object enemyProperty = properties.get("enemy");

            if (containsFalseKnightWord(enemyProperty)) {
                return true;
            }

            Object bossProperty = properties.get("boss");

            if (containsFalseKnightWord(bossProperty)) {
                return true;
            }

            return false;
        }

    protected boolean containsFalseKnightWord(Object value) {
            if (value == null) {
                return false;
            }

            String normalized = String.valueOf(value).trim().toLowerCase();
            String compact = normalized.replace("_", "").replace("-", "").replace(" ", "");
            return compact.contains("falseknight") || normalized.contains("false knight") || normalized.contains("false_knight") || normalized.contains("false-knight") || normalized.contains("fknight");
        }

    protected boolean isCrystallizedObject(MapObject object) {
            if (object == null) {
                return false;
            }

            if (containsCrystallizedWord(object.getName())) {
                return true;
            }

            MapProperties properties = object.getProperties();

            Object typeProperty = properties.get("type");
            if (containsCrystallizedWord(typeProperty)) {
                return true;
            }

            Object classProperty = properties.get("class");
            if (containsCrystallizedWord(classProperty)) {
                return true;
            }

            Object enemyProperty = properties.get("enemy");
            if (containsCrystallizedWord(enemyProperty)) {
                return true;
            }

            return false;
        }

    protected boolean containsCrystallizedWord(Object value) {
            if (value == null) {
                return false;
            }

            String normalized = String.valueOf(value).trim().toLowerCase();

            return normalized.contains("crystallized") ||
                normalized.contains("crystalized") ||
                normalized.contains("crystal_lized") ||
                normalized.contains("crystal-lized") ||
                normalized.contains("crystal shooter") ||
                normalized.contains("crystal_shooter");
        }

    protected boolean isCrawlidObject(MapObject object) {
            if (object == null) {
                return false;
            }

            if (containsCrawlidWord(object.getName())) {
                return true;
            }

            MapProperties properties = object.getProperties();

            Object typeProperty = properties.get("type");
            if (containsCrawlidWord(typeProperty)) {
                return true;
            }

            Object classProperty = properties.get("class");
            if (containsCrawlidWord(classProperty)) {
                return true;
            }

            Object enemyProperty = properties.get("enemy");
            if (containsCrawlidWord(enemyProperty)) {
                return true;
            }

            return false;
        }

    protected boolean containsCrawlidWord(Object value) {
            if (value == null) {
                return false;
            }

            String normalized = String.valueOf(value).trim().toLowerCase();

            return normalized.contains("crawlid") ||
                normalized.contains("crawl id") ||
                normalized.contains("crawl_id") ||
                normalized.contains("forgotten crawler") ||
                normalized.contains("basic crawler");
        }

    protected boolean isHuskHornheadObject(MapObject object) {
            if (object == null) {
                return false;
            }

            if (containsHuskHornheadWord(object.getName())) {
                return true;
            }

            MapProperties properties = object.getProperties();

            Object typeProperty = properties.get("type");
            if (containsHuskHornheadWord(typeProperty)) {
                return true;
            }

            Object classProperty = properties.get("class");
            if (containsHuskHornheadWord(classProperty)) {
                return true;
            }

            Object enemyProperty = properties.get("enemy");
            if (containsHuskHornheadWord(enemyProperty)) {
                return true;
            }

            return false;
        }

    protected boolean containsHuskHornheadWord(Object value) {
            if (value == null) {
                return false;
            }

            String normalized = String.valueOf(value).trim().toLowerCase();

            return normalized.contains("huskhornhead") ||
                normalized.contains("husk_hornhead") ||
                normalized.contains("husk hornhead") ||
                normalized.contains("hornhead") ||
                normalized.contains("horn head") ||
                normalized.contains("husk horn");
        }

    protected boolean isCrystalCrawlerObject(MapObject object) {
            if (object == null) {
                return false;
            }

            if (containsCrystalCrawlerWord(object.getName())) {
                return true;
            }

            MapProperties properties = object.getProperties();

            Object typeProperty = properties.get("type");
            if (containsCrystalCrawlerWord(typeProperty)) {
                return true;
            }

            Object classProperty = properties.get("class");
            if (containsCrystalCrawlerWord(classProperty)) {
                return true;
            }

            Object enemyProperty = properties.get("enemy");
            if (containsCrystalCrawlerWord(enemyProperty)) {
                return true;
            }

            return false;
        }

    protected boolean containsCrystalCrawlerWord(Object value) {
            if (value == null) {
                return false;
            }

            String normalized = String.valueOf(value).trim().toLowerCase();

            return normalized.contains("crystalcrawler") ||
                normalized.contains("crystal_crawler") ||
                normalized.contains("crystal crawler") ||
                normalized.contains("crawler");
        }

    protected boolean isMosquitoObject(MapObject object) {
            if (object == null) {
                return false;
            }

            if (containsMosquitoWord(object.getName())) {
                return true;
            }

            MapProperties properties = object.getProperties();

            Object typeProperty = properties.get("type");
            if (containsMosquitoWord(typeProperty)) {
                return true;
            }

            Object classProperty = properties.get("class");
            if (containsMosquitoWord(classProperty)) {
                return true;
            }

            Object enemyProperty = properties.get("enemy");
            if (containsMosquitoWord(enemyProperty)) {
                return true;
            }

            return false;
        }

    protected boolean containsMosquitoWord(Object value) {
            if (value == null) {
                return false;
            }

            String normalized = String.valueOf(value).trim().toLowerCase();
            return normalized.contains("mosquito") || normalized.contains("mosq");
        }

    protected int getDirectionProperty(MapProperties properties, int defaultValue) {
            Object value = firstProperty(properties, "direction", "dir", "facing");

            if (value == null) {
                return defaultValue < 0 ? -1 : 1;
            }

            String text = String.valueOf(value).trim().toLowerCase();

            if (text.contains("left") || "-1".equals(text)) {
                return -1;
            }

            if (text.contains("right") || "1".equals(text)) {
                return 1;
            }

            try {
                return Float.parseFloat(text) < 0f ? -1 : 1;
            } catch (Exception ignored) {
                return defaultValue < 0 ? -1 : 1;
            }
        }

    protected float getFloatProperty(MapProperties properties, float defaultValue, String... names) {
            Object value = firstProperty(properties, names);

            if (value == null) {
                return defaultValue;
            }

            if (value instanceof Number) {
                return ((Number) value).floatValue();
            }

            try {
                return Float.parseFloat(String.valueOf(value).trim());
            } catch (Exception ignored) {
                return defaultValue;
            }
        }

    protected int getIntProperty(MapProperties properties, int defaultValue, String... names) {
            Object value = firstProperty(properties, names);

            if (value == null) {
                return defaultValue;
            }

            if (value instanceof Number) {
                return ((Number) value).intValue();
            }

            try {
                return Integer.parseInt(String.valueOf(value).trim());
            } catch (Exception ignored) {
                return defaultValue;
            }
        }

    protected Object firstProperty(MapProperties properties, String... names) {
            if (properties == null || names == null) {
                return null;
            }

            for (String name : names) {
                Object value = properties.get(name);

                if (value != null) {
                    return value;
                }
            }

            return null;
        }
}
