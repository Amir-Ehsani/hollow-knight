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

abstract class MapController extends UiController {
    protected MapController(Game game, int saveSlot, boolean loadSavedGame) {
        super(game, saveSlot, loadSavedGame);
    }

    protected void addPlatformObject(MapObject object) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                platforms.add(rect);
            } else if (object instanceof PolygonMapObject) {
                Polygon polygon = ((PolygonMapObject) object).getPolygon();
                polygonPlatforms.add(polygon);
            }
        }

    protected void addSecretWallObject(MapObject object) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                platforms.add(rect);
                secretWalls.add(new SecretWallHitbox(object, rect, null));
            } else if (object instanceof PolygonMapObject) {
                Polygon polygon = ((PolygonMapObject) object).getPolygon();
                polygonPlatforms.add(polygon);
                secretWalls.add(new SecretWallHitbox(object, null, polygon));
            }
        }

    protected boolean isSecretWallObject(MapObject object) {
            if (object == null) {
                return false;
            }

            MapProperties properties = object.getProperties();

            return isTrueProperty(properties.get("SecretWall")) ||
                isTrueProperty(properties.get("secretWall")) ||
                isTrueProperty(properties.get("secretwall"));
        }

    protected void addSpikeObject(MapObject object) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                spikeRectangles.add(rect);
            } else if (object instanceof PolygonMapObject) {
                Polygon polygon = ((PolygonMapObject) object).getPolygon();
                spikePolygons.add(polygon);
            }
        }

    protected boolean isSpikeObject(MapObject object) {
            if (object == null) {
                return false;
            }

            if (containsSpikeWord(object.getName())) {
                return true;
            }

            MapProperties properties = object.getProperties();

            Object typeProperty = properties.get("type");
            if (typeProperty != null && containsSpikeWord(String.valueOf(typeProperty))) {
                return true;
            }

            Object classProperty = properties.get("class");
            if (classProperty != null && containsSpikeWord(String.valueOf(classProperty))) {
                return true;
            }

            return isTrueProperty(properties.get("spike")) ||
                isTrueProperty(properties.get("hazard"));
        }

    protected boolean containsSpikeWord(String value) {
            if (value == null) {
                return false;
            }

            String normalized = value.trim().toLowerCase();

            return normalized.contains("spike") ||
                normalized.contains("thorn") ||
                normalized.contains("hazard") ||
                normalized.contains("tigh");
        }

    protected boolean isTrueProperty(Object value) {
            if (value == null) {
                return false;
            }

            if (value instanceof Boolean) {
                return (Boolean) value;
            }

            return "true".equalsIgnoreCase(String.valueOf(value).trim()) ||
                "1".equals(String.valueOf(value).trim()) ||
                "yes".equalsIgnoreCase(String.valueOf(value).trim());
        }

    protected void loadZotesFromTiled() {
            String[] layerNames = {"NPCs", "Npcs", "NPC", "Npc", "Characters", "Zote"};

            for (String layerName : layerNames) {
                MapLayer layer = map.getLayers().get(layerName);

                if (layer == null) {
                    continue;
                }

                for (MapObject object : layer.getObjects()) {
                    if (isZoteObject(object)) {
                        addZoteObject(object);
                    }
                }
            }
        }

    protected void addZoteObject(MapObject object) {
            if (!(object instanceof RectangleMapObject)) {
                System.out.println("Zote must be a Rectangle Object in Tiled: " + object.getName());
                return;
            }

            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            MapProperties properties = object.getProperties();
            float width = getFloatProperty(properties, Zote.DEFAULT_HITBOX_WIDTH, "width", "hitboxWidth", "hitbox_width");
            float height = getFloatProperty(properties, Zote.DEFAULT_HITBOX_HEIGHT, "height", "hitboxHeight", "hitbox_height");

            if (rect.width > 1f) {
                width = rect.width;
            }

            if (rect.height > 1f) {
                height = rect.height;
            }

            float scale = getFloatProperty(properties, Zote.DEFAULT_DRAW_SCALE, "scale", "drawScale", "draw_scale");
            float interactionRange = getFloatProperty(properties, Zote.DEFAULT_INTERACTION_RANGE, "interactionRange", "interaction_range", "talkRange", "talk_range");
            int direction = getDirectionProperty(properties, 1);

            Zote zote = new Zote(rect.x, rect.y, width, height, direction, scale, interactionRange);
            zotes.add(zote);
        }

    protected boolean isZoteObject(MapObject object) {
            if (object == null) {
                return false;
            }

            if (containsZoteWord(object.getName())) {
                return true;
            }

            MapProperties properties = object.getProperties();
            Object typeProperty = properties.get("type");

            if (containsZoteWord(typeProperty)) {
                return true;
            }

            Object classProperty = properties.get("class");

            if (containsZoteWord(classProperty)) {
                return true;
            }

            Object npcProperty = properties.get("npc");

            if (containsZoteWord(npcProperty)) {
                return true;
            }

            Object characterProperty = properties.get("character");

            if (containsZoteWord(characterProperty)) {
                return true;
            }

            return false;
        }

    protected boolean containsZoteWord(Object value) {
            if (value == null) {
                return false;
            }

            String normalized = String.valueOf(value).trim().toLowerCase();
            return normalized.contains("zote") || normalized.contains("mighty");
        }

    protected void loadCharmsFromTiled() {
            if (map == null || charmPickups == null || charmInventory == null) {
                return;
            }

            String[] layerNames = {"Charms", "Charm", "CharmSpawns", "Charm Spawns"};
            ObjectSet<CharmId> added = new ObjectSet<>();

            for (String layerName : layerNames) {
                MapLayer layer = map.getLayers().get(layerName);
                if (layer == null) {
                    continue;
                }

                for (MapObject object : layer.getObjects()) {
                    MapProperties properties = object.getProperties();
                    Object idValue = firstProperty(
                        properties,
                        "charmId", "charm_id", "charm", "id", "type", "class"
                    );

                    CharmId id = CharmId.fromString(idValue);
                    if (id == null) {
                        id = CharmId.fromString(object.getName());
                    }

                    if (id == null) {
                        System.out.println("Unknown charm object in Tiled layer: " + object.getName());
                        continue;
                    }

                    if (charmInventory.isCollected(id) || added.contains(id)) {
                        continue;
                    }

                    float x = getFloatProperty(properties, 0f, "x");
                    float y = getFloatProperty(properties, 0f, "y");
                    float width = getFloatProperty(properties, 56f, "width", "pickupWidth", "pickup_width");
                    float height = getFloatProperty(properties, 56f, "height", "pickupHeight", "pickup_height");

                    if (object instanceof RectangleMapObject) {
                        Rectangle rect = ((RectangleMapObject) object).getRectangle();
                        x = rect.x;
                        y = rect.y;
                        if (rect.width > 1f) width = rect.width;
                        if (rect.height > 1f) height = rect.height;
                    }

                    float scale = getFloatProperty(properties, 1f, "scale", "drawScale", "draw_scale");
                    charmPickups.add(new CharmPickup(id, x, y, width, height, scale));
                    added.add(id);
                }
            }
        }

    protected boolean isCharmEquipped(CharmId id) {
            return charmInventory != null && charmInventory.isEquipped(id);
        }

    protected void applyCharmLoadout() {
            if (knightModel == null) {
                return;
            }

            int soulGain = isCharmEquipped(CharmId.SOUL_CATCHER)
                ? SOUL_CATCHER_GAIN
                : Knight.DEFAULT_SOUL_GAIN_ON_HIT;
            int nailDamage = isCharmEquipped(CharmId.UNBREAKABLE_STRENGTH)
                ? UNBREAKABLE_STRENGTH_DAMAGE
                : Knight.DEFAULT_ATTACK_DAMAGE;
            float dashDuration = Knight.DEFAULT_DASH_DURATION *
                (isCharmEquipped(CharmId.SHARP_SHADOW) ? SHARP_SHADOW_DASH_MULTIPLIER : 1f);
            float dashCooldown = isCharmEquipped(CharmId.DASHMASTER)
                ? DASHMASTER_COOLDOWN
                : Knight.DEFAULT_DASH_COOLDOWN;
            float attackDuration = isCharmEquipped(CharmId.QUICK_SLASH)
                ? QUICK_SLASH_ATTACK_DURATION
                : Knight.DEFAULT_ATTACK_DURATION;
            float attackCooldown = isCharmEquipped(CharmId.QUICK_SLASH)
                ? QUICK_SLASH_ATTACK_COOLDOWN
                : Knight.DEFAULT_ATTACK_COOLDOWN;
            float healDuration = isCharmEquipped(CharmId.QUICK_FOCUS)
                ? QUICK_FOCUS_HEAL_DURATION
                : Knight.DEFAULT_HEAL_DURATION;

            knightModel.configureCharmModifiers(
                soulGain,
                nailDamage,
                dashDuration,
                dashCooldown,
                attackDuration,
                attackCooldown,
                healDuration
            );
        }
}
