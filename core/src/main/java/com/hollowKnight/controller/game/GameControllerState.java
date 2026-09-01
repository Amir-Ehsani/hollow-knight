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

abstract class GameControllerState extends ScreenAdapter {
    protected static final float NORMAL_TILE_CULL_MARGIN = 48f;
        protected static final float ENEMY_SIMULATION_MARGIN_X = 720f;
        protected static final float ENEMY_SIMULATION_MARGIN_Y = 560f;
        protected static final float ENEMY_RENDER_MARGIN = 320f;
        protected static final float COLLISION_CACHE_EXTRA_X = 980f;
        protected static final float COLLISION_CACHE_EXTRA_Y = 820f;
        protected static final float COLLISION_CACHE_REBUILD_DISTANCE = 120f;
        protected static final float TRUE_OVERSIZED_TILE_OVERHANG = 192f;

        protected static final int[] DEEPEST_BACKGROUND_LAYER_INDICES = {
            0
        };

        protected static final int[] BACKGROUND_LAYER_INDICES = {
            1, 2, 3, 4, 5, 6, 7, 8,
            9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
            25, 26, 27, 28,29, 30, 31, 32
        };

        protected static final int[] FOREGROUND_LAYER_INDICES = {
             33,34,35,36,37,38,39
        };

        protected static final class LayerRenderRun {
            protected final int[] layerIndices;
            protected final boolean needsOversizedMargin;

            protected LayerRenderRun(int[] layerIndices, boolean needsOversizedMargin) {
                this.layerIndices = layerIndices;
                this.needsOversizedMargin = needsOversizedMargin;
            }
        }
        protected Game game;
        protected Stage hudStage;
        protected Skin uiSkin;
        protected GameHud gameHud;
        protected AchievementNotification achievementNotification;
        protected InventoryOverlay inventoryOverlay;
        protected CharmIconLibrary charmIconLibrary;
        protected CharmInventory charmInventory;
        protected Array<CharmPickup> charmPickups;
        protected ObjectSet<Damageable> sharpShadowDamagedEnemies;
        protected boolean inventoryOpen = false;
        protected Knight knightModel;
        protected KnightController knightController;
        protected SpriteBatch batch;
        protected Texture blackPixel;
        protected Texture whitePixel;
        protected Texture mapBackgroundTexture;
        protected Table pauseMenu;
        protected Table pauseMainPanel;
        protected Table cheatPanel;
        protected Label hazardLabel;
        protected Label deathLabel;
        protected Label pauseStatusLabel;
        protected Label cheatStatusLabel;
        protected float cheatStatusTimer = 0f;
        protected Table settingsPanel;
        protected Label musicSettingLabel;
        protected Label sfxSettingLabel;
        protected Label brightnessSettingLabel;
        protected TextButton musicMuteSettingButton;
        protected TextButton sfxMuteSettingButton;
        protected BitmapFont fallbackFont;
        protected LabelStyle fallbackLabelStyle;
        protected TextButtonStyle fallbackTextButtonStyle;
        protected boolean paused = false;
        protected float hazardFadeTimer = 0f;
        protected boolean deathRespawnPending = false;
        protected float deathRespawnTimer = 0f;
        protected SaveManager saveManager;
        protected int saveSlot;
        protected boolean loadSavedGame;
        protected String activeMapPath;
        protected Vector2 spawnPosition;

        protected float stateTime;
        protected float runElapsedSeconds;
        protected int deathCount;
        protected int enemyKillCount;
        protected boolean victoryPending;
        protected boolean endScreenTransitionScheduled;
        protected float victoryDelayTimer;
        protected float finalRunTimeSeconds;
        protected FalseKnight victoryBoss;

        protected float dashStateTime;
        protected Array<Texture> dashFrameTextures;

        protected Texture deathSheet;
        protected Array<Texture> deathFrameTextures;
        protected Animation<TextureRegion> deathAnimation;
        protected float deathStateTime;

        protected Texture fireballCastSheet;
        protected Array<Texture> fireballCastFrameTextures;
        protected Animation<TextureRegion> fireballCastAnimation;
        protected float fireballCastStateTime;
        protected boolean showVengefulCastAnimation;

        protected Texture idleSheet;
        protected Animation<TextureRegion> idleAnimation;

        protected Texture runSheet;
        protected Animation<TextureRegion> runAnimation;

        protected Texture jumpSheet;
        protected Animation<TextureRegion> jumpAnimation;

        protected Texture doubleJumpSheet;
        protected Array<Texture> doubleJumpFrameTextures;
        protected Animation<TextureRegion> doubleJumpAnimation;

        protected Array<Texture> fallTextures;
        protected Animation<TextureRegion> fallAnimation;

        protected Texture dashSheet;
        protected Animation<TextureRegion> dashAnimation;

        protected Texture attackSheet;
        protected Animation<TextureRegion> attackAnimation;

        protected Texture attackUpSheet;
        protected Animation<TextureRegion> attackUpAnimation;

        protected Texture pogoAttackSheet;
        protected Animation<TextureRegion> pogoAttackAnimation;

        protected Texture lookUpSheet;
        protected Animation<TextureRegion> lookUpAnimation;

        protected Texture healSheet;
        protected Animation<TextureRegion> healAnimation;

        protected Texture healAuraSheet;
        protected Animation<TextureRegion> healAuraAnimation;

        protected Texture dashEffectSheet;
        protected Animation<TextureRegion> dashEffectAnimation;

        protected Texture slashEffectSheet;
        protected Animation<TextureRegion> slashEffectAnimation;

        protected Texture slashEffectUpSheet;
        protected Animation<TextureRegion> slashEffectUpAnimation;

        protected Texture slashEffectDownSheet;
        protected Animation<TextureRegion> slashEffectDownAnimation;

        protected Array<DashEffectAnim> dashEffects;
        protected Array<SlashEffectAnim> slashEffects;

        protected Array<Texture> soulBallFrameTextures;
        protected Array<Texture> blastFrameTextures;
        protected Array<Texture> shadowScreamFrameTextures;
        protected Animation<TextureRegion> soulBallAnimation;
        protected Animation<TextureRegion> blastAnimation;
        protected Animation<TextureRegion> shadowScreamAnimation;
        protected Array<VengefulSpiritProjectile> vengefulProjectiles;
        protected Array<HowlingWraithsCast> howlingWraithCasts;
        protected Array<SpellBlastAnim> spellBlastEffects;

        protected boolean wasDashing = false;

        protected float attackStateTime = 0f;
        protected boolean wasAttacking = false;

        protected float lookUpStateTime = 0f;
        protected boolean wasLookingUp = false;

        protected float healStateTime = 0f;
        protected boolean wasHealing = false;

        protected float doubleJumpStateTime = 0f;
        protected boolean wasDoubleJumping = false;
        protected boolean doubleJumpVisualActive = false;

        protected Camera camera;
        protected ScreenViewport viewport;

        protected TiledMap map;
        protected OrthogonalTiledMapRenderer mapRenderer;
        protected TiledParticleManager particleManager;
        protected CombatEffectManager combatEffectManager;
        protected ShapeRenderer debugShapeRenderer;
        protected boolean showDebugHitboxes = false;
        protected MapLayer secretWallLayer;

        protected Array<Rectangle> platforms;
        protected Array<Polygon> polygonPlatforms;
        protected Array<SecretWallHitbox> secretWalls;

        protected final Array<Rectangle> nearbyPlatforms = new Array<>(false, 256);
        protected final Array<Polygon> nearbyPolygonPlatforms = new Array<>(false, 96);
        protected final Rectangle collisionCacheBounds = new Rectangle();
        protected final Rectangle enemySimulationBounds = new Rectangle();
        protected float collisionCacheCenterX = Float.NaN;
        protected float collisionCacheCenterY = Float.NaN;

        protected Array<Rectangle> spikeRectangles;
        protected Array<Polygon> spikePolygons;
        protected Vector2 lastSafeRespawnPosition;
        protected float spikeDamageCooldown = 0f;

        protected Array<Damageable> enemies;
        protected Array<CrystalCrawler> crystalCrawlers;
        protected Array<Crystallized> crystallizedEnemies;
        protected Array<FalseKnight> falseKnights;
        protected Array<Crawlid> crawlids;
        protected Array<HuskHornhead> huskHornheads;
        protected Array<Mosquito> mosquitoes;
        protected Array<Zote> zotes;
        protected CrystalCrawlerRenderer crystalCrawlerRenderer;
        protected CrystallizedRenderer crystallizedRenderer;
        protected FalseKnightRenderer falseKnightRenderer;
        protected CrawlidRenderer crawlidRenderer;
        protected HuskHornheadRenderer huskHornheadRenderer;
        protected MosquitoRenderer mosquitoRenderer;
        protected ZoteRenderer zoteRenderer;
        protected ZoteDialogueBox zoteDialogueBox;
        protected Zote activeDialogueZote;
        protected float enemyDamageCooldown = 0f;

        protected float screenShakeTimer = 0f;
        protected float screenShakeDuration = 0f;
        protected float screenShakeMagnitude = 0f;

        protected float mapPixelWidth;
        protected float mapPixelHeight;

        protected final IntArray oversizedTileLayerIndices = new IntArray();
        protected float oversizedTileCullMargin = 256f;
        protected Array<LayerRenderRun> deepestBackgroundRenderRuns = new Array<>();
        protected Array<LayerRenderRun> backgroundRenderRuns = new Array<>();
        protected Array<LayerRenderRun> foregroundRenderRuns = new Array<>();

        protected Array<Rectangle> rooms;
        protected Rectangle currentRoom;
        protected Rectangle activeBossArena;
        protected FalseKnight lockedBoss;
        protected boolean bossArenaEngaged = false;

        protected boolean noclipMode = false;
        protected boolean godMode = false;

        protected Array<Rectangle> musicZones;
        protected Array<String> musicZonePaths;

        protected Array<Rectangle> backgroundZones;
        protected Array<String> backgroundZonePaths;
        protected String currentBackgroundPath;
        protected String lastBackgroundRequestKey;

        protected Map<String, Music> musicCache;
        protected Music currentMusic;
        protected Music fadingOutMusic;
        protected String currentMusicPath;
        protected float musicFadeTimer = 0f;
        protected boolean musicFading = false;

        protected static final float MUSIC_VOLUME = 0.55f;
        protected static final float MUSIC_FADE_DURATION = 1.5f;

        protected Sound dashSfx;
        protected Sound deathSfx;
        protected Sound doubleJumpSfx;
        protected Sound fallingSfx;
        protected Sound fireballSfx;
        protected Sound landSoftSfx;
        protected Sound footstepsStoneSfx;
        protected Sound attackSfx;
        protected Array<Sound> attackSfxVariants;
        protected Array<Sound> soulPickupSfx;
        protected Sound heroDamageSfx;
        protected Sound deathExtraSfx;
        protected Sound howlingWraithsSfx;
        protected Sound focusChargingSfx;
        protected Sound focusHealSfx;
        protected Sound focusReadySfx;
        protected Sound heartbeatSfx;
        protected Sound flyFlyingSfx;
        protected Sound terrainRejectSfx;
        protected Sound breakableWallHitOneSfx;
        protected Sound breakableWallHitTwoSfx;
        protected Sound breakableWallDeathSfx;
        protected Array<Sound> zoteVoiceSfx;
        protected Array<Sound> falseKnightAttackVoiceSfx;
        protected Array<Sound> falseKnightHitSfx;
        protected Sound falseKnightSwingSfx;
        protected Sound falseKnightStrikeGroundSfx;
        protected Sound falseKnightJumpSfx;
        protected Sound falseKnightLandSfx;
        protected Sound falseKnightLandFirstSfx;
        protected Sound falseKnightRollSfx;
        protected Sound falseKnightDamageArmorSfx;
        protected Sound falseKnightDamageArmorFinalSfx;
        protected Sound falseKnightHeadDamageSfx;
        protected Sound falseKnightDazeSfx;
        protected Sound falseKnightDeathSfx;
        protected Sound falseKnightFallSfx;
        protected Sound falseKnightFlumpOneSfx;
        protected Sound falseKnightFlumpTwoSfx;
        protected Sound falseKnightRageSfx;
        protected Sound falseKnightCeilingBreakSfx;
        protected Sound falseKnightBossExplodeSfx;

        protected boolean sfxStateInitialized = false;
        protected boolean wasGroundedForSfx = false;
        protected boolean wasFallingForSfx = false;
        protected boolean deathSfxPlayed = false;
        protected long footstepLoopId = -1L;
        protected long fallingSoundId = -1L;
        protected long focusChargingLoopId = -1L;
        protected long heartbeatLoopId = -1L;
        protected long flyFlyingLoopId = -1L;
        protected int previousHealthForSfx = -1;
        protected int previousSoulForSfx = -1;
        protected boolean wasHealingForSfx = false;

        protected static final float SFX_DASH_VOLUME = 0.75f;
        protected static final float SFX_DEATH_VOLUME = 0.85f;
        protected static final float SFX_DOUBLE_JUMP_VOLUME = 0.75f;
        protected static final float SFX_FALLING_VOLUME = 0.45f;
        protected static final float SFX_FIREBALL_VOLUME = 0.8f;
        protected static final float SFX_LAND_VOLUME = 0.65f;
        protected static final float SFX_FOOTSTEP_VOLUME = 0.35f;
        protected static final float SFX_ATTACK_VOLUME = 0.75f;
        protected static final float SFX_SOUL_PICKUP_VOLUME = 0.34f;
        protected static final float SFX_DAMAGE_VOLUME = 0.78f;
        protected static final float SFX_FOCUS_VOLUME = 0.68f;
        protected static final float SFX_HEARTBEAT_VOLUME = 0.30f;
        protected static final float SFX_FLY_VOLUME = 0.20f;
        protected static final float SFX_TERRAIN_HIT_VOLUME = 0.55f;
        protected static final float SFX_BREAKABLE_WALL_VOLUME = 0.78f;
        protected static final float ZOTE_VOICE_VOLUME = 0.78f;
        protected static final float FALSE_KNIGHT_SFX_VOLUME = 0.82f;
        protected static final float FALSE_KNIGHT_HEAVY_SFX_VOLUME = 0.92f;

        protected Array<RectangleMapObject> doors;

        protected float teleportCooldown = 0f;
        protected boolean doorLocked = false;

        protected static final float TELEPORT_COOLDOWN_TIME = 0.25f;
        protected static final float EPS = 0.0001f;
        protected static final String DEFAULT_MAP_PATH = "tiled/main-map.tmx";
        protected static final String DEFAULT_BACKGROUND_PATH = "Map_Blue_Cavern_Background.png";
        protected static final float HAZARD_FADE_TIME = 1.45f;
        protected static final float HAZARD_FADE_IN_TIME = 0.18f;
        protected static final float HAZARD_FADE_OUT_TIME = 0.45f;
        protected static final float HAZARD_MAX_ALPHA = 0.86f;
        protected static final float PAUSE_OVERLAY_ALPHA = 0.78f;
        protected static final float DEATH_RESPAWN_DELAY = 1.6f;
        protected static final float DEATH_OVERLAY_START = 0.82f;
        protected static final float DASH_ANIMATION_FPS = 24f;
        protected static final float DEATH_ANIMATION_FPS = 14f;
        protected static final float FIREBALL_CAST_ANIMATION_FPS = 18f;
        protected static final float DOUBLE_JUMP_ANIMATION_FPS = 12f;

        protected static final int SPIKE_DAMAGE = 1;
        protected static final float SPIKE_DAMAGE_COOLDOWN_TIME = 0.75f;
        protected static final float SPIKE_RESPAWN_OFFSET_Y = 4f;

        protected static final int ENEMY_TOUCH_DAMAGE = 1;
        protected static final float ENEMY_DAMAGE_COOLDOWN_TIME = 0.15f;
        protected static final float ENEMY_KNOCKBACK_X = 560f;
        protected static final float ENEMY_KNOCKBACK_Y = 0f;
        protected static final float PLAYER_KNOCKBACK_DURATION = 0.32f;

        protected static final float DAMAGE_SCREEN_SHAKE_DURATION = 0.16f;
        protected static final float DAMAGE_SCREEN_SHAKE_MAGNITUDE = 11f;
        protected static final float SPIKE_SCREEN_SHAKE_MAGNITUDE = 14f;
        protected static final float ENEMY_RESPAWN_PLAYER_DISTANCE = 2375f;

        protected static final float ENEMY_HIT_KNOCKBACK_X = 185f;
        protected static final float ENEMY_HIT_KNOCKBACK_Y = 130f;

        protected static final int SPELL_SOUL_COST = 33;
        protected static final float VENGEFUL_CAST_LOCK = 0.50f;
        protected static final float HOWLING_CAST_LOCK = 0.36f;
        protected static final float VENGEFUL_SCREEN_SHAKE_MAGNITUDE = 9f;
        protected static final float VENGEFUL_SCREEN_SHAKE_DURATION = 0.16f;
        protected static final float HOWLING_SCREEN_SHAKE_MAGNITUDE = 16f;
        protected static final float HOWLING_SCREEN_SHAKE_DURATION = 0.26f;
        protected static final float END_GAME_DELAY_AFTER_BOSS_DEATH = 0.65f;
        protected static final float NOCLIP_SPEED = 900f;
        protected static final float BOSS_ARENA_INSET = 3f;
        protected static final float CHEAT_MESSAGE_TIME = 2.2f;

        protected static final int SOUL_CATCHER_GAIN = 16;
        protected static final float DASHMASTER_COOLDOWN = 0.34f;
        protected static final int UNBREAKABLE_STRENGTH_DAMAGE = 2;
        protected static final float QUICK_SLASH_ATTACK_DURATION = 0.22f;
        protected static final float QUICK_SLASH_ATTACK_COOLDOWN = 0.18f;
        protected static final float QUICK_FOCUS_HEAL_DURATION = 0.82f;
        protected static final float HEAVY_BLOW_MULTIPLIER = 1.70f;
        protected static final float SHARP_SHADOW_DASH_MULTIPLIER = 1.20f;
        protected static final int SHARP_SHADOW_DAMAGE = 1;
        protected static final int BASE_VENGEFUL_DAMAGE = 2;
        protected static final int BASE_HOWLING_DAMAGE_PER_TICK = 2;
        protected static final float CHARM_PICKUP_BOB_AMOUNT = 7f;
        protected static final float CHARM_PICKUP_BOB_SPEED = 2.8f;

        protected static final int SECRET_WALL_REQUIRED_HITS = 3;

        protected static final int ATTACK_FRAME_COUNT = 5;
        protected static final int ATTACK_UP_FRAME_COUNT = 5;
        protected static final int POGO_ATTACK_FRAME_COUNT = 5;
        protected static final int LOOK_UP_FRAME_COUNT = 6;
        protected static final int SLASH_EFFECT_FRAME_COUNT = 6;
        protected static final int SLASH_EFFECT_UP_FRAME_COUNT = 6;
        protected static final int SLASH_EFFECT_DOWN_FRAME_COUNT = 6;

        protected class DashEffectAnim {
            float stateTime;
            boolean flipX;

            public DashEffectAnim(boolean flipX) {
                this.flipX = flipX;
                this.stateTime = 0f;
            }
        }

        protected class SlashEffectAnim {
            float x;
            float y;
            float stateTime;
            float scale;
            boolean flipX;
            Knight.AttackDirection direction;

            public SlashEffectAnim(float x, float y, float scale, boolean flipX, Knight.AttackDirection direction) {
                this.x = x;
                this.y = y;
                this.scale = scale;
                this.flipX = flipX;
                this.direction = direction;
                this.stateTime = 0f;
            }
        }

        protected class SpellBlastAnim {
            float x;
            float y;
            float stateTime;
            boolean flipX;
            float scale;

            SpellBlastAnim(float x, float y, boolean flipX, float scale) {
                this.x = x;
                this.y = y;
                this.flipX = flipX;
                this.scale = scale;
                this.stateTime = 0f;
            }
        }

        protected class SecretWallHitbox {
            MapObject object;
            Rectangle rectangle;
            Polygon polygon;
            int hits;
            boolean destroyed;

            public SecretWallHitbox(MapObject object, Rectangle rectangle, Polygon polygon) {
                this.object = object;
                this.rectangle = rectangle;
                this.polygon = polygon;
                this.hits = 0;
                this.destroyed = false;
            }
        }

    protected GameControllerState(Game game, int saveSlot, boolean loadSavedGame) {
        this.game = game;
        this.saveSlot = SaveManager.normalizeSlot(saveSlot);
        this.loadSavedGame = loadSavedGame;
    }

    public abstract void show();
    protected abstract Texture createSolidTexture(Color color);
    protected abstract Label makeLabel(String text);
    protected abstract TextButton makeTextButton(String text);
    protected abstract LabelStyle getSafeLabelStyle();
    protected abstract TextButtonStyle getSafeTextButtonStyle();
    protected abstract BitmapFont getFallbackFont();
    protected abstract void createUiActors();
    protected abstract Table createSettingsPanel();
    protected abstract Table createPauseMusicRow();
    protected abstract Table createPauseSfxRow();
    protected abstract Table createPauseBrightnessRow();
    protected abstract void refreshSettingsPanelLabels();
    protected abstract void refreshCurrentMusicVolume();
    protected abstract void showPauseMain();
    protected abstract void showPauseSettings();
    protected abstract boolean canOpenInventory();
    protected abstract void openInventory();
    protected abstract void closeInventory();
    protected abstract void refreshKnightControllerEnabled();
    protected abstract void setPaused(boolean paused);
    protected abstract void saveCurrentGame();
    protected abstract void addPlatformObject(MapObject object);
    protected abstract void addSecretWallObject(MapObject object);
    protected abstract boolean isSecretWallObject(MapObject object);
    protected abstract void addSpikeObject(MapObject object);
    protected abstract boolean isSpikeObject(MapObject object);
    protected abstract boolean containsSpikeWord(String value);
    protected abstract boolean isTrueProperty(Object value);
    protected abstract void loadZotesFromTiled();
    protected abstract void addZoteObject(MapObject object);
    protected abstract boolean isZoteObject(MapObject object);
    protected abstract boolean containsZoteWord(Object value);
    protected abstract void loadCharmsFromTiled();
    protected abstract boolean isCharmEquipped(CharmId id);
    protected abstract void applyCharmLoadout();
    protected abstract void loadEnemiesFromTiled();
    protected abstract void loadFalseKnightsFromTiled();
    protected abstract void addFalseKnightObject(MapObject object);
    protected abstract Rectangle getFalseKnightArenaBounds(MapProperties properties, Rectangle rect, float width, float height);
    protected abstract Rectangle findRoomContaining(float x, float y);
    protected abstract void addCrystallizedObject(MapObject object);
    protected abstract void addCrystalCrawlerObject(MapObject object);
    protected abstract void addCrawlidObject(MapObject object);
    protected abstract void addHuskHornheadObject(MapObject object);
    protected abstract void addMosquitoObject(MapObject object);
    protected abstract boolean isFalseKnightObject(MapObject object);
    protected abstract boolean containsFalseKnightWord(Object value);
    protected abstract boolean isCrystallizedObject(MapObject object);
    protected abstract boolean containsCrystallizedWord(Object value);
    protected abstract boolean isCrawlidObject(MapObject object);
    protected abstract boolean containsCrawlidWord(Object value);
    protected abstract boolean isHuskHornheadObject(MapObject object);
    protected abstract boolean containsHuskHornheadWord(Object value);
    protected abstract boolean isCrystalCrawlerObject(MapObject object);
    protected abstract boolean containsCrystalCrawlerWord(Object value);
    protected abstract boolean isMosquitoObject(MapObject object);
    protected abstract boolean containsMosquitoWord(Object value);
    protected abstract int getDirectionProperty(MapProperties properties, int defaultValue);
    protected abstract float getFloatProperty(MapProperties properties, float defaultValue, String... names);
    protected abstract int getIntProperty(MapProperties properties, int defaultValue, String... names);
    protected abstract Object firstProperty(MapProperties properties, String... names);
    protected abstract String getMusicPathForMusicObject(MapObject musicObject);
    protected abstract void loadSfx();
    protected abstract void loadZoteSfx();
    protected abstract void addZoteVoiceSfx(String path);
    protected abstract void playRandomZoteVoice();
    protected abstract void loadFalseKnightSfx();
    protected abstract void addFalseKnightSound(Array<Sound> sounds, String path);
    protected abstract void processFalseKnightEvents(FalseKnight falseKnight);
    protected abstract void playFalseKnightEvent(FalseKnight.Event event);
    protected abstract void playRandomSound(Array<Sound> sounds, float volume);
    protected abstract void disposeFalseKnightSfx();
    protected abstract void disposeSoundArray(Array<Sound> sounds);
    protected abstract Sound loadSoundFirst(String path);
    protected abstract Sound loadSoundIfExists(String path);
    protected abstract Sound loadOptionalSound(String path);
    protected abstract void addSoundToArray(Array<Sound> sounds, String path);
    protected abstract void playAttackSfx();
    protected abstract void playSfx(Sound sound, float volume);
    protected abstract void resetFalseKnightCombatAfterPlayerDeath();
    protected abstract void stopFalseKnightSfx();
    protected abstract void stopSound(Sound sound);
    protected abstract void stopSoundArray(Array<Sound> sounds);
    public abstract void playFireballSfx();
    protected abstract void updatePlayerSfx(float delta);
    protected abstract void startFootsteps();
    protected abstract void stopFootsteps();
    protected abstract void startFallingSfx();
    protected abstract void stopFallingSfx();
    protected abstract void startFocusChargingSfx();
    protected abstract void stopFocusChargingSfx();
    protected abstract void updateHeartbeatSfx(int currentHealth);
    protected abstract void stopHeartbeatSfx();
    protected abstract void updateFlyingEnemySfx();
    protected abstract void stopFlyFlyingSfx();
    protected abstract void stopPlayerLoopingSfx();
    protected abstract void loadMapBackground();
    protected abstract String getBackgroundPathForBackgroundObject(MapObject object);
    protected abstract void updateBackgroundZone(float playerX, float playerY);
    protected abstract boolean requestMapBackground(String backgroundPath);
    protected abstract String resolveBackgroundPath(String backgroundPath);
    protected abstract String normalizeBackgroundPath(String path);
    protected abstract void drawMapBackground();
    protected abstract void loadAnimations();
    protected abstract void loadSpellAnimations();
    protected abstract Animation<TextureRegion> loadNumberedFrameAnimation(
        Array<Texture> ownedTextures,
        int firstFrame,
        int lastFrame,
        float fps,
        Animation.PlayMode playMode,
        String prefix
    );
    protected abstract Texture loadTextureFirst(String path);
    protected abstract Animation<TextureRegion> makeAnimation(Texture sheet, int totalFrames, int startFrame, int endFrame, float fps, Animation.PlayMode playMode);
    protected abstract Animation<TextureRegion> makeAutoHorizontalAnimation(Texture sheet, float fps, Animation.PlayMode playMode);
    public abstract void render(float delta);
    protected abstract void drawDebugHitboxes();
    protected abstract void drawDebugRectangle(Rectangle rectangle);
    protected abstract void drawDebugPolygon(Polygon polygon);
    protected abstract void drawHealingEffect(float delta, float playerX, float playerY, TextureRegion currentFrame, float offsetX, float offsetY, boolean isCurrentlyHealing);
    protected abstract void drawUiOverlays(float delta);
    protected abstract void castVengefulSpirit();
    protected abstract void castHowlingWraiths();
    protected abstract void updateCharmPickups(float delta);
    protected abstract void drawCharmPickups(boolean hiddenBehindSecretWallPass);
    protected abstract void updateSpells(float delta);
    protected abstract boolean spellHitsTerrain(Rectangle bounds);
    protected abstract void spawnSpellBlast(float centerX, float centerY, boolean flipX, float scale);
    protected abstract void drawSpellEffects();
    protected abstract boolean handleCheatKeyDown(int keycode);
    protected abstract boolean isControlPressed();
    protected abstract void showCheatMessage(String message);
    protected abstract void updateCheatStatus(float delta);
    protected abstract void teleportToBossArenaCheat();
    protected abstract void toggleNoclipCheat();
    protected abstract void updateNoclipMovement(float delta);
    protected abstract void emergencyHealCheat();
    protected abstract void refillSoulCheat();
    protected abstract void instaKillVisibleEnemiesCheat();
    protected abstract void handleDeathRespawn(float delta);
    protected abstract void beginVictorySequence(FalseKnight boss);
    protected abstract void updateVictoryTransition(float delta);
    protected abstract void triggerHazardWarning();
    protected abstract void updateFalseKnightArenaState(Rectangle playerBounds);
    protected abstract boolean isFalseKnightArenaLocked();
    protected abstract void constrainPlayerToBossArena();
    protected abstract void refreshNearbyCollisionGeometry(boolean force);
    protected abstract void updateEnemySimulationBounds();
    protected abstract boolean isInEnemySimulationRange(Rectangle bounds);
    protected abstract boolean isInCameraRenderRange(Rectangle bounds, float margin);
    protected abstract void drawTextureRegion(
            TextureRegion region,
            float x,
            float y,
            float width,
            float height,
            boolean flipX
        );
    protected abstract void configureOptimizedTileRendering(int tileWidth, int tileHeight);
    protected abstract Array<LayerRenderRun> buildLayerRenderRuns(int[] sourceIndices);
    protected abstract void renderLayerRuns(Array<LayerRenderRun> runs);
    protected abstract void setMapRendererView(float margin);
    protected abstract float clampCameraAxis(float target, float boundsStart, float boundsSize, float halfViewport);
    protected abstract void updateRoomForCamera(float playerX, float playerY);
    protected abstract void updateMusicZone(float playerX, float playerY);
    protected abstract void requestRoomMusic(String musicPath);
    protected abstract Music getOrLoadMusic(String musicPath);
    protected abstract void updateMusicFade(float delta);
    protected abstract void drawDashEffects(float delta, float playerX, float playerY, float hitboxWidth);
    protected abstract void spawnSlashEffect();
    protected abstract void drawSlashEffects(float delta);
    protected abstract void drawCombatEffects(float delta);
    protected abstract Animation<TextureRegion> getSlashAnimation(Knight.AttackDirection direction);
    protected abstract void startScreenShake(float magnitude);
    protected abstract void startScreenShake(float magnitude, float duration);
    protected abstract void handleSpikeCollision();
    protected abstract void updateLastSafeRespawnPosition();
    protected abstract boolean knightBodyOverlapsSpikes();
    protected abstract boolean attackOverlapsSpikes(Rectangle attackRect);
    protected abstract boolean handleSecretWallHit(Rectangle attackRect);
    protected abstract void destroySecretWall(SecretWallHitbox secretWall);
    protected abstract void updateEnemies(float delta);
    protected abstract void updateZotes(float delta);
    protected abstract void updateZoteDialogue(float delta);
    protected abstract void updateZotePrompt();
    protected abstract boolean handleZoteKeyDown(int keycode);
    protected abstract Zote getNearestInteractiveZote();
    protected abstract void beginZoteDialogue(Zote zote);
    protected abstract void finishZoteDialogue();
    protected abstract void closeZoteDialogue();
    protected abstract boolean isZoteDialogueActive();
    protected abstract boolean handleZoteHit(Rectangle attackRect);
    protected abstract void drawZotes();
    protected abstract void drawCrystalCrawlers();
    protected abstract void drawCrystallizedEnemies();
    protected abstract void drawFalseKnights();
    protected abstract void drawCrawlids();
    protected abstract void drawHuskHornheads();
    protected abstract void drawMosquitoes();
    protected abstract Rectangle getEnemyDamageBounds(Damageable enemy);
    protected abstract void handleEnemyCollision();
    protected abstract void applyDamageToEnemyWithKnockback(Damageable enemy);
    protected abstract void applyDamageToEnemy(Damageable enemy, int damage, float knockbackX, float knockbackY);
    protected abstract void handleAttackHits();
    protected abstract boolean handleNailTerrainHit(Rectangle attackRect);
    protected abstract void finishNailTerrainHit();
    protected abstract void spawnNailTerrainEffect(Rectangle attackRect, Rectangle targetRect);
    protected abstract void spawnNailTerrainEffect(Rectangle attackRect, Polygon targetPolygon);
    protected abstract Vector2 getIntersectionCenter(Rectangle first, Rectangle second);
    protected abstract float getNailTerrainEffectRotation();
    protected abstract void handleDoorTeleport(float delta);
    protected abstract void teleportFromDoor(RectangleMapObject currentDoor);
    protected abstract boolean rectOverlapsPolygonForAttack(Rectangle rect, Polygon polygon);
    protected abstract boolean segmentsIntersect(
            float ax, float ay, float bx, float by,
            float cx, float cy, float dx, float dy
        );
    protected abstract float cross(float ax, float ay, float bx, float by, float px, float py);
    protected abstract boolean onSegment(float ax, float ay, float bx, float by, float px, float py);
    protected abstract void disposeTextureArray(Array<Texture> textures);
    protected abstract void setGameplayCursorVisible(boolean visible);
    public abstract void pause();
    public abstract void resume();
    public abstract void resize(int width, int height);
    public abstract void hide();
    public abstract void dispose();
}
