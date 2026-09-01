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

abstract class LifecycleController extends InteractionController {
    protected LifecycleController(Game game, int saveSlot, boolean loadSavedGame) {
        super(game, saveSlot, loadSavedGame);
    }

    public void show() {
            GameSettings.load();
            saveManager = new SaveManager();
            SaveManager.SaveData loadedData = loadSavedGame ? saveManager.loadGame(saveSlot) : null;
            activeMapPath = DEFAULT_MAP_PATH;

            if (loadedData != null && loadedData.mapPath != null && !loadedData.mapPath.trim().isEmpty()) {
                activeMapPath = loadedData.mapPath.trim();
            }

            hudStage = new Stage(new ScreenViewport());
            uiSkin = new Skin(Gdx.files.internal("ui/alternative/main.json"));
            blackPixel = createSolidTexture(Color.BLACK);
            whitePixel = createSolidTexture(Color.WHITE);

            map = new TmxMapLoader().load(activeMapPath);
            secretWallLayer = map.getLayers().get("SecretWall");

            if (secretWallLayer != null) {
                secretWallLayer.setVisible(true);
            }

            rooms = new Array<>();
            musicZones = new Array<>();
            musicZonePaths = new Array<>();
            backgroundZones = new Array<>();
            backgroundZonePaths = new Array<>();
            currentBackgroundPath = null;
            musicCache = new HashMap<>();

            MapLayer roomsLayer = map.getLayers().get("Rooms");

            if (roomsLayer != null) {
                for (MapObject object : roomsLayer.getObjects()) {
                    if (object instanceof RectangleMapObject) {
                        rooms.add(((RectangleMapObject) object).getRectangle());
                    }
                }
            }

            if (rooms.size > 0) {
                currentRoom = rooms.first();
            } else {
                currentRoom = new Rectangle(0, 0, 10000, 10000);
            }

            MapLayer musicLayer = map.getLayers().get("Music");

            if (musicLayer != null) {
                for (MapObject object : musicLayer.getObjects()) {
                    if (object instanceof RectangleMapObject) {
                        Rectangle rect = ((RectangleMapObject) object).getRectangle();
                        String musicPath = getMusicPathForMusicObject(object);

                        if (musicPath != null && !musicPath.trim().isEmpty()) {
                            musicZones.add(rect);
                            musicZonePaths.add(musicPath);
                        }
                    }
                }
            }

            MapLayer backgroundsLayer = map.getLayers().get("Backgrounds");

            if (backgroundsLayer != null) {
                for (MapObject object : backgroundsLayer.getObjects()) {
                    if (object instanceof RectangleMapObject) {
                        Rectangle rect = ((RectangleMapObject) object).getRectangle();
                        String backgroundPath = getBackgroundPathForBackgroundObject(object);

                        if (backgroundPath != null && !backgroundPath.trim().isEmpty()) {
                            backgroundZones.add(rect);
                            backgroundZonePaths.add(backgroundPath);
                        }
                    }
                }
            }

            doors = new Array<>();

            MapLayer doorsLayer = map.getLayers().get("Doors");
            if (doorsLayer != null) {
                for (MapObject object : doorsLayer.getObjects()) {
                    if (object instanceof RectangleMapObject) {
                        doors.add((RectangleMapObject) object);
                    }
                }
            }

            MapProperties prop = map.getProperties();
            int mapWidthInTiles = prop.get("width", Integer.class);
            int mapHeightInTiles = prop.get("height", Integer.class);
            int tilePixelWidth = prop.get("tilewidth", Integer.class);
            int tilePixelHeight = prop.get("tileheight", Integer.class);

            mapPixelWidth = mapWidthInTiles * tilePixelWidth;
            mapPixelHeight = mapHeightInTiles * tilePixelHeight;
            configureOptimizedTileRendering(tilePixelWidth, tilePixelHeight);

            mapRenderer = new OrthogonalTiledMapRenderer(map, 1f);
            particleManager = new TiledParticleManager(map, activeMapPath, mapPixelWidth, mapPixelHeight);
            combatEffectManager = new CombatEffectManager();

            MapLayer objectLayer = map.getLayers().get("Object Layer 1");
            spawnPosition = null;

            if (objectLayer != null) {
                MapObject spawnObject = objectLayer.getObjects().get("spawnPoint");

                if (spawnObject instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) spawnObject).getRectangle();
                    spawnPosition = new Vector2(rect.x, rect.y);
                }
            }

            if (spawnPosition == null) {
                spawnPosition = new Vector2(100f, 150f);
            }

            if (loadedData != null) {
                knightModel = new Knight(loadedData.playerX, loadedData.playerY);
                knightModel.setCurrentHealth(loadedData.currentHealth);
                knightModel.setCurrentSoul(loadedData.currentSoul);

                if (knightModel.getCurrentHealth() <= 0) {
                    knightModel.resetForNewRun(spawnPosition.x, spawnPosition.y);
                }
            } else {
                knightModel = new Knight(spawnPosition.x, spawnPosition.y);
            }

            charmInventory = new CharmInventory(
                loadedData == null ? "" : loadedData.collectedCharms,
                loadedData == null ? "" : loadedData.equippedCharms
            );
            applyCharmLoadout();

            gameHud = new GameHud(knightModel);

            platforms = new Array<>();
            polygonPlatforms = new Array<>();
            secretWalls = new Array<>();
            spikeRectangles = new Array<>();
            spikePolygons = new Array<>();
            enemies = new Array<>();
            crystalCrawlers = new Array<>();
            crystallizedEnemies = new Array<>();
            falseKnights = new Array<>();
            crawlids = new Array<>();
            huskHornheads = new Array<>();
            mosquitoes = new Array<>();
            zotes = new Array<>();
            charmPickups = new Array<>();
            sharpShadowDamagedEnemies = new ObjectSet<>();
            vengefulProjectiles = new Array<>();
            howlingWraithCasts = new Array<>();
            spellBlastEffects = new Array<>();

            if (loadedData != null) {
                lastSafeRespawnPosition = new Vector2(loadedData.lastSafeX, loadedData.lastSafeY);
            } else {
                lastSafeRespawnPosition = new Vector2(
                    knightModel.getPosition().x,
                    knightModel.getPosition().y + SPIKE_RESPAWN_OFFSET_Y
                );
            }

            if (objectLayer != null) {
                for (MapObject object : objectLayer.getObjects()) {
                    if ("spawnPoint".equals(object.getName())) {
                        continue;
                    }

                    if (isZoteObject(object)) {
                        addZoteObject(object);
                    } else if (isFalseKnightObject(object)) {
                        addFalseKnightObject(object);
                    } else if (isSecretWallObject(object)) {
                        addSecretWallObject(object);
                    } else if (isSpikeObject(object)) {
                        addSpikeObject(object);
                    } else {
                        addPlatformObject(object);
                    }
                }
            }

            MapLayer spikesLayer = map.getLayers().get("Spikes");

            if (spikesLayer != null) {
                for (MapObject object : spikesLayer.getObjects()) {
                    addSpikeObject(object);
                }
            }

            loadEnemiesFromTiled();
            loadFalseKnightsFromTiled();
            loadZotesFromTiled();
            loadCharmsFromTiled();
            crystalCrawlerRenderer = new CrystalCrawlerRenderer();
            crystallizedRenderer = new CrystallizedRenderer();
            falseKnightRenderer = new FalseKnightRenderer();
            crawlidRenderer = new CrawlidRenderer();
            huskHornheadRenderer = new HuskHornheadRenderer();
            mosquitoRenderer = new MosquitoRenderer();
            zoteRenderer = new ZoteRenderer();
            charmIconLibrary = new CharmIconLibrary();

            knightController = new KnightController(knightModel);
            createUiActors();
            achievementNotification = new AchievementNotification(uiSkin);
            achievementNotification.updatePosition(
                hudStage.getViewport().getWorldWidth(),
                hudStage.getViewport().getWorldHeight()
            );
            hudStage.addActor(achievementNotification);

            InputMultiplexer multiplexer = new InputMultiplexer();
            multiplexer.addProcessor(new InputAdapter() {
                @Override
                public boolean keyDown(int keycode) {
                    if (keycode == Input.Keys.I) {
                        if (inventoryOpen) {
                            closeInventory();
                        } else {
                            openInventory();
                        }
                        return true;
                    }

                    if (inventoryOpen) {
                        if (ControlBindings.matches(ControlBindings.Action.PAUSE, keycode)) {
                            closeInventory();
                        }
                        return true;
                    }

                    if (handleCheatKeyDown(keycode)) {
                        return true;
                    }

                    if (handleZoteKeyDown(keycode)) {
                        return true;
                    }

                    if (!paused && !deathRespawnPending && !victoryPending && !isZoteDialogueActive()) {
                        if (ControlBindings.matches(ControlBindings.Action.VENGEFUL_SPIRIT, keycode)) {
                            castVengefulSpirit();
                            return true;
                        }

                        if (ControlBindings.matches(ControlBindings.Action.HOWLING_WRAITHS, keycode)) {
                            castHowlingWraiths();
                            return true;
                        }
                    }

                    if (ControlBindings.matches(ControlBindings.Action.PAUSE, keycode)) {
                        setPaused(!paused);
                        return true;
                    }

                    if (keycode == Input.Keys.F3) {
                        showDebugHitboxes = !showDebugHitboxes;
                        return true;
                    }

                    return false;
                }
            });
            multiplexer.addProcessor(hudStage);
            multiplexer.addProcessor(knightController);
            Gdx.input.setInputProcessor(multiplexer);
            setGameplayCursorVisible(false);

            camera = new OrthographicCamera();
            viewport = new ScreenViewport(camera);

            batch = new SpriteBatch();
            debugShapeRenderer = new ShapeRenderer();
            loadMapBackground();

            loadAnimations();
            loadSfx();
            loadFalseKnightSfx();
            loadZoteSfx();

            dashEffects = new Array<>();
            slashEffects = new Array<>();
            stateTime = 0f;
            runElapsedSeconds = 0f;
            deathCount = 0;
            enemyKillCount = 0;
            victoryPending = false;
            endScreenTransitionScheduled = false;
            victoryDelayTimer = 0f;
            finalRunTimeSeconds = 0f;
            victoryBoss = null;
            dashStateTime = 0f;
            deathStateTime = 0f;
            fireballCastStateTime = 0f;
            showVengefulCastAnimation = false;
        }

    protected void setGameplayCursorVisible(boolean visible) {
            if (game instanceof Main) {
                Main main = (Main) game;

                if (visible) {
                    main.showMenuCursor();
                } else {
                    main.hideGameplayCursor();
                }
            }
        }

    public void pause() {

            setGameplayCursorVisible(true);
        }

    public void resume() {

            setGameplayCursorVisible(paused || inventoryOpen);
        }

    public void resize(int width, int height) {
            viewport.update(width, height);
            collisionCacheCenterX = Float.NaN;
            collisionCacheCenterY = Float.NaN;

            if (hudStage != null) {
                hudStage.getViewport().update(width, height);
            }

            if (gameHud != null) {
                gameHud.resize(width, height);
            }

            if (achievementNotification != null) {
                achievementNotification.updatePosition(width, height);
            }
        }

    public void hide() {
            setGameplayCursorVisible(true);

            if (achievementNotification != null) {
                achievementNotification.setListening(false);
            }
        }

    public void dispose() {
            if (achievementNotification != null) {
                achievementNotification.dispose();
                achievementNotification = null;
            }

            if (inventoryOverlay != null) {
                inventoryOverlay.dispose();
                inventoryOverlay = null;
            }

            if (charmIconLibrary != null) {
                charmIconLibrary.dispose();
                charmIconLibrary = null;
            }

            if (hudStage != null) {
                hudStage.dispose();
            }

            if (uiSkin != null) {
                uiSkin.dispose();
            }

            if (fallbackFont != null) {
                fallbackFont.dispose();
                fallbackFont = null;
            }

            if (blackPixel != null) {
                blackPixel.dispose();
            }

            if (whitePixel != null) {
                whitePixel.dispose();
            }

            if (mapBackgroundTexture != null) {
                mapBackgroundTexture.dispose();
                mapBackgroundTexture = null;
            }

            if (gameHud != null) {
                gameHud.dispose();
                gameHud = null;
            }

            if (zoteDialogueBox != null) {
                zoteDialogueBox.dispose();
                zoteDialogueBox = null;
            }

            if (batch != null) {
                batch.dispose();
            }

            if (debugShapeRenderer != null) {
                debugShapeRenderer.dispose();
            }

            if (idleSheet != null) {
                idleSheet.dispose();
            }

            if (runSheet != null) {
                runSheet.dispose();
            }

            if (dashSheet != null) {
                dashSheet.dispose();
            }

            if (deathSheet != null) {
                deathSheet.dispose();
            }

            if (fireballCastSheet != null) {
                fireballCastSheet.dispose();
            }

            if (attackSheet != null) {
                attackSheet.dispose();
            }

            if (attackUpSheet != null) {
                attackUpSheet.dispose();
            }

            if (pogoAttackSheet != null) {
                pogoAttackSheet.dispose();
            }

            if (lookUpSheet != null) {
                lookUpSheet.dispose();
            }

            if (healSheet != null) {
                healSheet.dispose();
            }

            if (healAuraSheet != null) {
                healAuraSheet.dispose();
            }

            if (slashEffectSheet != null) {
                slashEffectSheet.dispose();
            }

            if (slashEffectUpSheet != null) {
                slashEffectUpSheet.dispose();
            }

            if (slashEffectDownSheet != null) {
                slashEffectDownSheet.dispose();
            }

            if (dashEffectSheet != null) {
                dashEffectSheet.dispose();
            }

            if (fallTextures != null) {
                for (Texture tex : fallTextures) {
                    tex.dispose();
                }
            }

            disposeTextureArray(doubleJumpFrameTextures);
            disposeTextureArray(dashFrameTextures);
            disposeTextureArray(deathFrameTextures);
            disposeTextureArray(fireballCastFrameTextures);
            disposeTextureArray(soulBallFrameTextures);
            disposeTextureArray(blastFrameTextures);
            disposeTextureArray(shadowScreamFrameTextures);

            if (doubleJumpSheet != null && doubleJumpSheet != jumpSheet) {
                doubleJumpSheet.dispose();
            }

            if (jumpSheet != null) {
                jumpSheet.dispose();
            }

            if (dashSfx != null) {
                dashSfx.dispose();
            }

            if (deathSfx != null) {
                deathSfx.dispose();
            }

            if (doubleJumpSfx != null) {
                doubleJumpSfx.dispose();
            }

            stopFallingSfx();

            if (fallingSfx != null) {
                fallingSfx.dispose();
            }

            if (fireballSfx != null) {
                fireballSfx.dispose();
            }

            if (landSoftSfx != null) {
                landSoftSfx.dispose();
            }

            stopFootsteps();

            if (footstepsStoneSfx != null) {
                footstepsStoneSfx.dispose();
            }

            if (attackSfx != null) {
                attackSfx.dispose();
            }

            stopPlayerLoopingSfx();
            disposeSoundArray(attackSfxVariants);
            disposeSoundArray(soulPickupSfx);
            if (heroDamageSfx != null) heroDamageSfx.dispose();
            if (deathExtraSfx != null) deathExtraSfx.dispose();
            if (howlingWraithsSfx != null) howlingWraithsSfx.dispose();
            if (focusChargingSfx != null) focusChargingSfx.dispose();
            if (focusHealSfx != null) focusHealSfx.dispose();
            if (focusReadySfx != null) focusReadySfx.dispose();
            if (heartbeatSfx != null) heartbeatSfx.dispose();
            if (flyFlyingSfx != null) flyFlyingSfx.dispose();
            if (terrainRejectSfx != null) terrainRejectSfx.dispose();
            if (breakableWallHitOneSfx != null) breakableWallHitOneSfx.dispose();
            if (breakableWallHitTwoSfx != null) breakableWallHitTwoSfx.dispose();
            if (breakableWallDeathSfx != null) breakableWallDeathSfx.dispose();

            if (zoteVoiceSfx != null) {
                for (Sound sound : zoteVoiceSfx) {
                    if (sound != null) {
                        sound.dispose();
                    }
                }
                zoteVoiceSfx.clear();
            }

            if (musicCache != null) {
                for (Music music : musicCache.values()) {
                    if (music != null) {
                        music.stop();
                        music.dispose();
                    }
                }

                musicCache.clear();
            }

            if (crystalCrawlerRenderer != null) {
                crystalCrawlerRenderer.dispose();
                crystalCrawlerRenderer = null;
            }

            if (crystallizedRenderer != null) {
                crystallizedRenderer.dispose();
                crystallizedRenderer = null;
            }

            if (crawlidRenderer != null) {
                crawlidRenderer.dispose();
                crawlidRenderer = null;
            }

            if (huskHornheadRenderer != null) {
                huskHornheadRenderer.dispose();
                huskHornheadRenderer = null;
            }

            if (mosquitoRenderer != null) {
                mosquitoRenderer.dispose();
                mosquitoRenderer = null;
            }

            if (falseKnightRenderer != null) {
                falseKnightRenderer.dispose();
                falseKnightRenderer = null;
            }

            disposeFalseKnightSfx();

            if (combatEffectManager != null) {
                combatEffectManager.dispose();
                combatEffectManager = null;
            }

            if (particleManager != null) {
                particleManager.dispose();
                particleManager = null;
            }

            if (map != null) {
                map.dispose();
            }

            if (mapRenderer != null) {
                mapRenderer.dispose();
            }
        }
}
