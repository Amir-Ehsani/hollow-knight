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

abstract class WorldController extends AbilityController {
    protected WorldController(Game game, int saveSlot, boolean loadSavedGame) {
        super(game, saveSlot, loadSavedGame);
    }

    protected void triggerHazardWarning() {
            hazardFadeTimer = HAZARD_FADE_TIME;

            if (hazardLabel != null) {
                hazardLabel.setVisible(true);
            }
        }

    protected void updateFalseKnightArenaState(Rectangle playerBounds) {
            if (lockedBoss != null) {
                if (lockedBoss.getState() == FalseKnight.State.DEAD) {
                    lockedBoss = null;
                    activeBossArena = null;
                    bossArenaEngaged = false;
                    showCheatMessage("False Knight defeated - arena exits opened");
                } else {
                    activeBossArena = lockedBoss.getArenaBounds();
                    bossArenaEngaged = activeBossArena != null;
                }
                return;
            }

            if (falseKnights == null || playerBounds == null) {
                activeBossArena = null;
                bossArenaEngaged = false;
                return;
            }

            float playerCenterX = playerBounds.x + playerBounds.width / 2f;
            float playerCenterY = playerBounds.y + playerBounds.height / 2f;

            for (FalseKnight falseKnight : falseKnights) {
                if (falseKnight == null || falseKnight.getState() == FalseKnight.State.DEAD) {
                    continue;
                }

                Rectangle arena = falseKnight.getArenaBounds();

                if (arena != null && arena.contains(playerCenterX, playerCenterY)) {
                    lockedBoss = falseKnight;
                    activeBossArena = arena;
                    bossArenaEngaged = true;
                    showCheatMessage("Boss arena sealed - defeat the False Knight to leave");
                    constrainPlayerToBossArena();
                    return;
                }
            }

            activeBossArena = null;
            bossArenaEngaged = false;
        }

    protected boolean isFalseKnightArenaLocked() {
            return bossArenaEngaged &&
                lockedBoss != null &&
                lockedBoss.getState() != FalseKnight.State.DEAD &&
                activeBossArena != null;
        }

    protected void constrainPlayerToBossArena() {
            if (!isFalseKnightArenaLocked() || knightModel == null) {
                return;
            }

            Rectangle knightBounds = knightModel.getBounds();
            float minX = activeBossArena.x + BOSS_ARENA_INSET;
            float maxX = activeBossArena.x + activeBossArena.width - knightBounds.width - BOSS_ARENA_INSET;
            float minY = activeBossArena.y + BOSS_ARENA_INSET;
            float maxY = activeBossArena.y + activeBossArena.height - knightBounds.height - BOSS_ARENA_INSET;

            float oldX = knightModel.getPosition().x;
            float oldY = knightModel.getPosition().y;
            float newX = maxX < minX ? activeBossArena.x + (activeBossArena.width - knightBounds.width) / 2f
                : MathUtils.clamp(oldX, minX, maxX);
            float newY = maxY < minY ? activeBossArena.y + (activeBossArena.height - knightBounds.height) / 2f
                : MathUtils.clamp(oldY, minY, maxY);

            knightModel.getPosition().set(newX, newY);

            if (Math.abs(newX - oldX) > EPS) {
                knightModel.getVelocity().x = 0f;
            }
            if (Math.abs(newY - oldY) > EPS) {
                knightModel.getVelocity().y = 0f;
            }
        }

    protected void refreshNearbyCollisionGeometry(boolean force) {
            if (knightModel == null || platforms == null || polygonPlatforms == null) {
                return;
            }

            Rectangle knightBounds = knightModel.getBounds();
            float centerX = knightBounds.x + knightBounds.width / 2f;
            float centerY = knightBounds.y + knightBounds.height / 2f;

            if (!force && !Float.isNaN(collisionCacheCenterX)) {
                float dx = centerX - collisionCacheCenterX;
                float dy = centerY - collisionCacheCenterY;
                float thresholdSquared =
                    COLLISION_CACHE_REBUILD_DISTANCE * COLLISION_CACHE_REBUILD_DISTANCE;

                if (dx * dx + dy * dy < thresholdSquared) {
                    return;
                }
            }

            collisionCacheCenterX = centerX;
            collisionCacheCenterY = centerY;

            float viewportHalfWidth = camera == null ? 960f : camera.viewportWidth / 2f;
            float viewportHalfHeight = camera == null ? 540f : camera.viewportHeight / 2f;
            float halfWidth = viewportHalfWidth + COLLISION_CACHE_EXTRA_X;
            float halfHeight = viewportHalfHeight + COLLISION_CACHE_EXTRA_Y;

            collisionCacheBounds.set(
                centerX - halfWidth,
                centerY - halfHeight,
                halfWidth * 2f,
                halfHeight * 2f
            );

            nearbyPlatforms.clear();
            for (Rectangle platform : platforms) {
                if (platform != null && platform.overlaps(collisionCacheBounds)) {
                    nearbyPlatforms.add(platform);
                }
            }

            nearbyPolygonPlatforms.clear();
            for (Polygon polygon : polygonPlatforms) {
                if (polygon != null &&
                    polygon.getBoundingRectangle().overlaps(collisionCacheBounds)) {
                    nearbyPolygonPlatforms.add(polygon);
                }
            }
        }

    protected void updateEnemySimulationBounds() {
            if (knightModel == null) {
                enemySimulationBounds.set(0f, 0f, 0f, 0f);
                return;
            }

            Rectangle knightBounds = knightModel.getBounds();
            float centerX = knightBounds.x + knightBounds.width / 2f;
            float centerY = knightBounds.y + knightBounds.height / 2f;
            float halfWidth =
                (camera == null ? 960f : camera.viewportWidth / 2f) +
                    ENEMY_SIMULATION_MARGIN_X;
            float halfHeight =
                (camera == null ? 540f : camera.viewportHeight / 2f) +
                    ENEMY_SIMULATION_MARGIN_Y;

            enemySimulationBounds.set(
                centerX - halfWidth,
                centerY - halfHeight,
                halfWidth * 2f,
                halfHeight * 2f
            );
        }

    protected boolean isInEnemySimulationRange(Rectangle bounds) {
            return bounds != null && bounds.overlaps(enemySimulationBounds);
        }

    protected boolean isInCameraRenderRange(Rectangle bounds, float margin) {
            if (bounds == null || camera == null) {
                return false;
            }

            float halfWidth = camera.viewportWidth / 2f + margin;
            float halfHeight = camera.viewportHeight / 2f + margin;
            float left = camera.position.x - halfWidth;
            float right = camera.position.x + halfWidth;
            float bottom = camera.position.y - halfHeight;
            float top = camera.position.y + halfHeight;

            return bounds.x + bounds.width >= left &&
                bounds.x <= right &&
                bounds.y + bounds.height >= bottom &&
                bounds.y <= top;
        }

    protected void drawTextureRegion(
            TextureRegion region,
            float x,
            float y,
            float width,
            float height,
            boolean flipX
        ) {
            if (region == null) {
                return;
            }

            batch.draw(
                region.getTexture(),
                x,
                y,
                0f,
                0f,
                width,
                height,
                1f,
                1f,
                0f,
                region.getRegionX(),
                region.getRegionY(),
                region.getRegionWidth(),
                region.getRegionHeight(),
                flipX,
                false
            );
        }

    protected void configureOptimizedTileRendering(int tileWidth, int tileHeight) {
            oversizedTileLayerIndices.clear();

            float requiredOversizedMargin = 0f;

            if (map != null) {
                for (int layerIndex = 0; layerIndex < map.getLayers().getCount(); layerIndex++) {
                    MapLayer mapLayer = map.getLayers().get(layerIndex);

                    if (!(mapLayer instanceof TiledMapTileLayer)) {
                        continue;
                    }

                    TiledMapTileLayer tileLayer = (TiledMapTileLayer) mapLayer;
                    boolean layerUsesOversizedTile = false;
                    float layerRequiredMargin = 0f;

                    for (int x = 0; x < tileLayer.getWidth(); x++) {
                        for (int y = 0; y < tileLayer.getHeight(); y++) {
                            TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);

                            if (cell == null || cell.getTile() == null ||
                                cell.getTile().getTextureRegion() == null) {
                                continue;
                            }

                            TiledMapTile tile = cell.getTile();
                            float regionWidth = tile.getTextureRegion().getRegionWidth();
                            float regionHeight = tile.getTextureRegion().getRegionHeight();

                            float horizontalOverhang =
                                Math.max(0f, regionWidth - tileWidth) + Math.abs(tile.getOffsetX());
                            float verticalOverhang =
                                Math.max(0f, regionHeight - tileHeight) + Math.abs(tile.getOffsetY());

                            float overhang = Math.max(horizontalOverhang, verticalOverhang);

                            if (overhang > TRUE_OVERSIZED_TILE_OVERHANG) {
                                layerUsesOversizedTile = true;
                                layerRequiredMargin = Math.max(layerRequiredMargin, overhang + 64f);
                            }
                        }
                    }

                    if (layerUsesOversizedTile) {
                        oversizedTileLayerIndices.add(layerIndex);
                        requiredOversizedMargin =
                            Math.max(requiredOversizedMargin, layerRequiredMargin);
                    }
                }
            }

            oversizedTileCullMargin =
                MathUtils.clamp(requiredOversizedMargin, 256f, 1024f);

            deepestBackgroundRenderRuns =
                buildLayerRenderRuns(DEEPEST_BACKGROUND_LAYER_INDICES);
            backgroundRenderRuns =
                buildLayerRenderRuns(BACKGROUND_LAYER_INDICES);
            foregroundRenderRuns =
                buildLayerRenderRuns(FOREGROUND_LAYER_INDICES);
        }

    protected Array<LayerRenderRun> buildLayerRenderRuns(int[] sourceIndices) {
            Array<LayerRenderRun> runs = new Array<>();

            if (sourceIndices == null || sourceIndices.length == 0) {
                return runs;
            }

            IntArray currentRun = new IntArray();
            boolean currentOversized =
                oversizedTileLayerIndices.contains(sourceIndices[0]);

            for (int layerIndex : sourceIndices) {
                boolean oversized = oversizedTileLayerIndices.contains(layerIndex);

                if (currentRun.size > 0 && oversized != currentOversized) {
                    runs.add(new LayerRenderRun(currentRun.toArray(), currentOversized));
                    currentRun.clear();
                }

                currentOversized = oversized;
                currentRun.add(layerIndex);
            }

            if (currentRun.size > 0) {
                runs.add(new LayerRenderRun(currentRun.toArray(), currentOversized));
            }

            return runs;
        }

    protected void renderLayerRuns(Array<LayerRenderRun> runs) {
            if (mapRenderer == null || runs == null) {
                return;
            }

            for (LayerRenderRun run : runs) {
                if (run == null || run.layerIndices == null ||
                    run.layerIndices.length == 0) {
                    continue;
                }

                setMapRendererView(
                    run.needsOversizedMargin
                        ? oversizedTileCullMargin
                        : NORMAL_TILE_CULL_MARGIN
                );
                mapRenderer.render(run.layerIndices);
            }
        }

    protected void setMapRendererView(float margin) {
            float safeMargin = Math.max(0f, margin);
            float startX =
                camera.position.x - (camera.viewportWidth / 2f) - safeMargin;
            float startY =
                camera.position.y - (camera.viewportHeight / 2f) - safeMargin;
            float renderWidth = camera.viewportWidth + (safeMargin * 2f);
            float renderHeight = camera.viewportHeight + (safeMargin * 2f);

            mapRenderer.setView(
                camera.combined,
                startX,
                startY,
                renderWidth,
                renderHeight
            );
        }

    protected float clampCameraAxis(float target, float boundsStart, float boundsSize, float halfViewport) {
            if (boundsSize <= halfViewport * 2f) {
                return boundsStart + boundsSize / 2f;
            }

            return MathUtils.clamp(target, boundsStart + halfViewport, boundsStart + boundsSize - halfViewport);
        }

    protected void updateRoomForCamera(float playerX, float playerY) {
            currentRoom = null;

            for (Rectangle room : rooms) {
                if (room.contains(playerX, playerY)) {
                    currentRoom = room;
                    return;
                }
            }
        }

    protected void updateMusicZone(float playerX, float playerY) {
            String targetMusicPath = null;

            for (int i = 0; i < musicZones.size; i++) {
                Rectangle zone = musicZones.get(i);

                if (zone.contains(playerX, playerY)) {
                    targetMusicPath = musicZonePaths.get(i);
                    break;
                }
            }

            if (targetMusicPath != null) {
                requestRoomMusic(targetMusicPath);
            }
        }

    protected void requestRoomMusic(String musicPath) {
            if (musicPath == null || musicPath.trim().isEmpty()) {
                return;
            }

            musicPath = musicPath.trim();

            if (musicPath.equals(currentMusicPath)) {
                return;
            }

            Music nextMusic = getOrLoadMusic(musicPath);

            if (nextMusic == null) {
                return;
            }

            nextMusic.setLooping(true);

            if (currentMusic == null) {
                currentMusic = nextMusic;
                currentMusicPath = musicPath;
                currentMusic.setVolume(GameSettings.getMusicOutputVolume(MUSIC_VOLUME));
                currentMusic.play();
                return;
            }

            if (fadingOutMusic != null && fadingOutMusic != currentMusic) {
                fadingOutMusic.stop();
                fadingOutMusic.setVolume(0f);
            }

            fadingOutMusic = currentMusic;
            currentMusic = nextMusic;
            currentMusicPath = musicPath;

            currentMusic.setVolume(0f);
            currentMusic.play();

            musicFadeTimer = 0f;
            musicFading = true;
        }

    protected Music getOrLoadMusic(String musicPath) {
            if (musicCache.containsKey(musicPath)) {
                return musicCache.get(musicPath);
            }

            if (!Gdx.files.internal(musicPath).exists()) {
                System.out.println("Music file not found: " + musicPath);
                return null;
            }

            Music music = Gdx.audio.newMusic(Gdx.files.internal(musicPath));
            music.setLooping(true);
            music.setVolume(0f);

            musicCache.put(musicPath, music);
            return music;
        }

    protected void updateMusicFade(float delta) {
            if (!musicFading) {
                return;
            }

            musicFadeTimer += delta;

            float progress = MathUtils.clamp(musicFadeTimer / MUSIC_FADE_DURATION, 0f, 1f);

            if (fadingOutMusic != null) {
                fadingOutMusic.setVolume(GameSettings.getMusicOutputVolume(MUSIC_VOLUME) * (1f - progress));
            }

            if (currentMusic != null) {
                currentMusic.setVolume(GameSettings.getMusicOutputVolume(MUSIC_VOLUME) * progress);
            }

            if (progress >= 1f) {
                if (fadingOutMusic != null && fadingOutMusic != currentMusic) {
                    fadingOutMusic.stop();
                    fadingOutMusic.setVolume(0f);
                }

                fadingOutMusic = null;

                if (currentMusic != null) {
                    currentMusic.setVolume(GameSettings.getMusicOutputVolume(MUSIC_VOLUME));
                }

                musicFading = false;
            }
        }
}
