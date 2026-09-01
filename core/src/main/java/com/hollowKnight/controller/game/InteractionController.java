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

abstract class InteractionController extends EnemyController {
    protected InteractionController(Game game, int saveSlot, boolean loadSavedGame) {
        super(game, saveSlot, loadSavedGame);
    }

    protected void handleDoorTeleport(float delta) {
            if (teleportCooldown > 0f) {
                teleportCooldown -= delta;
            }

            float playerX = knightModel.getPosition().x;
            float playerY = knightModel.getPosition().y;

            Rectangle playerRect = new Rectangle(playerX, playerY, 50f, 70f);

            boolean touchingAnyDoor = false;

            for (RectangleMapObject doorObj : doors) {
                Rectangle doorRect = doorObj.getRectangle();

                if (playerRect.overlaps(doorRect)) {
                    touchingAnyDoor = true;
                    break;
                }
            }

            if (!touchingAnyDoor) {
                doorLocked = false;
            }

            if (doorLocked) {
                return;
            }

            if (teleportCooldown > 0f) {
                return;
            }

            if (!touchingAnyDoor) {
                return;
            }

            for (RectangleMapObject doorObj : doors) {
                Rectangle doorRect = doorObj.getRectangle();

                if (playerRect.overlaps(doorRect)) {
                    teleportFromDoor(doorObj);
                    break;
                }
            }
        }

    protected void teleportFromDoor(RectangleMapObject currentDoor) {
            String doorName = currentDoor.getName();

            if (doorName == null) {
                return;
            }

            try {
                int currentDoorId = Integer.parseInt(doorName);
                int targetDoorId;

                if (currentDoorId % 2 != 0) {
                    targetDoorId = currentDoorId + 1;
                } else {
                    targetDoorId = currentDoorId - 1;
                }

                for (RectangleMapObject targetDoor : doors) {
                    if (String.valueOf(targetDoorId).equals(targetDoor.getName())) {
                        Rectangle targetRect = targetDoor.getRectangle();

                        knightModel.getPosition().set(targetRect.x, targetRect.y + 10f);
                        knightModel.getVelocity().set(0f, 0f);
                        stopFootsteps();

                        if (lastSafeRespawnPosition != null) {
                            lastSafeRespawnPosition.set(
                                targetRect.x,
                                targetRect.y + 10f + SPIKE_RESPAWN_OFFSET_Y
                            );
                        }

                        teleportCooldown = TELEPORT_COOLDOWN_TIME;
                        doorLocked = true;

                        return;
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Door name must be a number: " + doorName);
            }
        }

    protected boolean rectOverlapsPolygonForAttack(Rectangle rect, Polygon polygon) {
            Rectangle polygonBounds = polygon.getBoundingRectangle();

            if (!rect.overlaps(polygonBounds)) {
                return false;
            }

            float[] vertices = polygon.getTransformedVertices();

            float rx1 = rect.x;
            float ry1 = rect.y;
            float rx2 = rect.x + rect.width;
            float ry2 = rect.y + rect.height;

            if (polygon.contains(rx1, ry1) ||
                polygon.contains(rx2, ry1) ||
                polygon.contains(rx2, ry2) ||
                polygon.contains(rx1, ry2)) {
                return true;
            }

            for (int i = 0; i < vertices.length; i += 2) {
                if (rect.contains(vertices[i], vertices[i + 1])) {
                    return true;
                }
            }

            for (int i = 0; i < vertices.length; i += 2) {
                int next = (i + 2) % vertices.length;

                float x1 = vertices[i];
                float y1 = vertices[i + 1];
                float x2 = vertices[next];
                float y2 = vertices[next + 1];

                if (segmentsIntersect(x1, y1, x2, y2, rx1, ry1, rx2, ry1) ||
                    segmentsIntersect(x1, y1, x2, y2, rx2, ry1, rx2, ry2) ||
                    segmentsIntersect(x1, y1, x2, y2, rx2, ry2, rx1, ry2) ||
                    segmentsIntersect(x1, y1, x2, y2, rx1, ry2, rx1, ry1)) {
                    return true;
                }
            }

            return false;
        }

    protected boolean segmentsIntersect(
            float ax, float ay, float bx, float by,
            float cx, float cy, float dx, float dy
        ) {
            float d1 = cross(ax, ay, bx, by, cx, cy);
            float d2 = cross(ax, ay, bx, by, dx, dy);
            float d3 = cross(cx, cy, dx, dy, ax, ay);
            float d4 = cross(cx, cy, dx, dy, bx, by);

            if (((d1 > EPS && d2 < -EPS) || (d1 < -EPS && d2 > EPS)) &&
                ((d3 > EPS && d4 < -EPS) || (d3 < -EPS && d4 > EPS))) {
                return true;
            }

            if (Math.abs(d1) <= EPS && onSegment(ax, ay, bx, by, cx, cy)) return true;
            if (Math.abs(d2) <= EPS && onSegment(ax, ay, bx, by, dx, dy)) return true;
            if (Math.abs(d3) <= EPS && onSegment(cx, cy, dx, dy, ax, ay)) return true;
            if (Math.abs(d4) <= EPS && onSegment(cx, cy, dx, dy, bx, by)) return true;

            return false;
        }

    protected float cross(float ax, float ay, float bx, float by, float px, float py) {
            return (bx - ax) * (py - ay) - (by - ay) * (px - ax);
        }

    protected boolean onSegment(float ax, float ay, float bx, float by, float px, float py) {
            return px >= Math.min(ax, bx) - EPS &&
                px <= Math.max(ax, bx) + EPS &&
                py >= Math.min(ay, by) - EPS &&
                py <= Math.max(ay, by) + EPS;
        }

    protected void disposeTextureArray(Array<Texture> textures) {
            if (textures == null) {
                return;
            }

            for (Texture texture : textures) {
                if (texture != null) {
                    texture.dispose();
                }
            }
            textures.clear();
        }
}
