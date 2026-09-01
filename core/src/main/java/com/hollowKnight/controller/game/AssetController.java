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

abstract class AssetController extends AudioController {
    protected AssetController(Game game, int saveSlot, boolean loadSavedGame) {
        super(game, saveSlot, loadSavedGame);
    }

    protected void loadMapBackground() {
            Object backgroundProperty = map.getProperties().get("background");
            String mapBackground = backgroundProperty == null ? null : String.valueOf(backgroundProperty).trim();

            if (mapBackground != null && !mapBackground.isEmpty() && requestMapBackground(mapBackground)) {
                return;
            }

            requestMapBackground(DEFAULT_BACKGROUND_PATH);
        }

    protected String getBackgroundPathForBackgroundObject(MapObject object) {
            Object backgroundProperty = object.getProperties().get("background");

            if (backgroundProperty != null) {
                String path = String.valueOf(backgroundProperty).trim();
                if (!path.isEmpty()) {
                    return path;
                }
            }

            Object pathProperty = object.getProperties().get("path");

            if (pathProperty != null) {
                String path = String.valueOf(pathProperty).trim();
                if (!path.isEmpty()) {
                    return path;
                }
            }

            return null;
        }

    protected void updateBackgroundZone(float playerX, float playerY) {
            String targetBackgroundPath = null;

            for (int i = 0; i < backgroundZones.size; i++) {
                Rectangle zone = backgroundZones.get(i);

                if (zone.contains(playerX, playerY)) {
                    targetBackgroundPath = backgroundZonePaths.get(i);
                    break;
                }
            }

            requestMapBackground(targetBackgroundPath);
        }

    protected boolean requestMapBackground(String backgroundPath) {
        String requested = normalizeBackgroundPath(backgroundPath);
        if (requested == null) {
            requested = DEFAULT_BACKGROUND_PATH;
        }

        if (requested.equals(lastBackgroundRequestKey) && mapBackgroundTexture != null) {
            return true;
        }

        String resolved = resolveBackgroundPath(requested);
        if (resolved == null) {
            resolved = DEFAULT_BACKGROUND_PATH;
        }

        if (!Gdx.files.internal(resolved).exists()) {
            return false;
        }

        lastBackgroundRequestKey = requested;
        if (resolved.equals(currentBackgroundPath)) {
            return true;
        }

        Texture texture = new Texture(resolved);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        if (mapBackgroundTexture != null) {
            mapBackgroundTexture.dispose();
        }
        mapBackgroundTexture = texture;
        currentBackgroundPath = resolved;
        return true;
    }

    protected String resolveBackgroundPath(String backgroundPath) {
        String path = normalizeBackgroundPath(backgroundPath);
        if (path == null || !Gdx.files.internal(path).exists()) {
            return null;
        }
        return path;
    }

    protected String normalizeBackgroundPath(String path) {
            if (path == null) {
                return null;
            }

            String normalizedPath = path.trim().replace('\\', '/');

            while (normalizedPath.startsWith("./")) {
                normalizedPath = normalizedPath.substring(2);
            }

            while (normalizedPath.startsWith("/")) {
                normalizedPath = normalizedPath.substring(1);
            }

            return normalizedPath.isEmpty() ? null : normalizedPath;
        }

    protected void drawMapBackground() {
            if (batch == null || blackPixel == null) {
                return;
            }

            float viewLeft = camera.position.x - camera.viewportWidth / 2f;
            float viewBottom = camera.position.y - camera.viewportHeight / 2f;

            batch.setProjectionMatrix(camera.combined);
            batch.begin();

            batch.setColor(0.02f, 0.025f, 0.04f, 1f);
            batch.draw(blackPixel, viewLeft, viewBottom, camera.viewportWidth, camera.viewportHeight);
            batch.setColor(1f, 1f, 1f, 1f);

            if (mapBackgroundTexture != null) {
                float textureWidth = mapBackgroundTexture.getWidth();
                float textureHeight = mapBackgroundTexture.getHeight();
                float scale = Math.max(camera.viewportWidth / textureWidth, camera.viewportHeight / textureHeight);
                float drawWidth = textureWidth * scale;
                float drawHeight = textureHeight * scale;
                float drawX = viewLeft + (camera.viewportWidth - drawWidth) / 2f;
                float drawY = viewBottom + (camera.viewportHeight - drawHeight) / 2f;

                batch.draw(mapBackgroundTexture, drawX, drawY, drawWidth, drawHeight);
            }

            batch.end();
        }

    protected void loadAnimations() {
        idleSheet = loadTextureFirst("animations/animation/Idle.png");
        idleAnimation = makeAnimation(idleSheet, 9, 0, 8, 5f, Animation.PlayMode.LOOP);

        runSheet = loadTextureFirst("animations/animation/Run.png");
        runAnimation = makeAnimation(runSheet, 13, 4, 12, 9f, Animation.PlayMode.LOOP);

        jumpSheet = loadTextureFirst("animations/animation/Airborne.png");
        jumpAnimation = makeAnimation(jumpSheet, 12, 0, 2, 12f, Animation.PlayMode.NORMAL);

        doubleJumpSheet = loadTextureFirst("animations/animation/Double Jump.png");
        doubleJumpAnimation = makeAnimation(
            doubleJumpSheet,
            8,
            0,
            7,
            DOUBLE_JUMP_ANIMATION_FPS,
            Animation.PlayMode.NORMAL
        );
        doubleJumpFrameTextures = new Array<>();

        fallTextures = new Array<>();
        Array<TextureRegion> fallFrames = new Array<>(TextureRegion.class);
        for (int i = 1; i <= 5; i++) {
            Texture texture = new Texture(
                "animations/animation/Fall_" + String.format("%03d", i) + ".png"
            );
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            fallTextures.add(texture);
            fallFrames.add(new TextureRegion(texture));
        }
        fallAnimation = new Animation<>(1f / 5f, fallFrames, Animation.PlayMode.NORMAL);

        dashSheet = loadTextureFirst("animations/animation/Dash.png");
        dashAnimation = makeAnimation(
            dashSheet,
            12,
            0,
            11,
            DASH_ANIMATION_FPS,
            Animation.PlayMode.NORMAL
        );
        dashFrameTextures = new Array<>();

        deathSheet = loadTextureFirst("animations/animation/Death.png");
        deathAnimation = makeAnimation(
            deathSheet,
            18,
            0,
            17,
            DEATH_ANIMATION_FPS,
            Animation.PlayMode.NORMAL
        );
        deathFrameTextures = new Array<>();

        fireballCastSheet = loadTextureFirst("animations/animation/Fireball Cast.png");
        fireballCastAnimation = makeAnimation(
            fireballCastSheet,
            9,
            0,
            8,
            FIREBALL_CAST_ANIMATION_FPS,
            Animation.PlayMode.NORMAL
        );
        fireballCastFrameTextures = new Array<>();

        attackSheet = loadTextureFirst("animations/animation/Slash.png");
        attackAnimation = makeAnimation(
            attackSheet,
            ATTACK_FRAME_COUNT,
            0,
            ATTACK_FRAME_COUNT - 1,
            18f,
            Animation.PlayMode.NORMAL
        );

        attackUpSheet = loadTextureFirst("animations/animation/UpSlash.png");
        attackUpAnimation = makeAnimation(
            attackUpSheet,
            ATTACK_UP_FRAME_COUNT,
            0,
            ATTACK_UP_FRAME_COUNT - 1,
            18f,
            Animation.PlayMode.NORMAL
        );

        pogoAttackSheet = loadTextureFirst("animations/animation/DownSlash.png");
        pogoAttackAnimation = makeAnimation(
            pogoAttackSheet,
            POGO_ATTACK_FRAME_COUNT,
            0,
            POGO_ATTACK_FRAME_COUNT - 1,
            18f,
            Animation.PlayMode.NORMAL
        );

        lookUpSheet = loadTextureFirst("animations/animation/LookUp.png");
        lookUpAnimation = makeAnimation(
            lookUpSheet,
            LOOK_UP_FRAME_COUNT,
            0,
            LOOK_UP_FRAME_COUNT - 1,
            10f,
            Animation.PlayMode.LOOP
        );

        healSheet = loadTextureFirst("animations/animation/Focus.png");
        healAnimation = makeAutoHorizontalAnimation(
            healSheet,
            12f,
            Animation.PlayMode.LOOP
        );
        healAuraSheet = null;
        healAuraAnimation = null;

        slashEffectSheet = loadTextureFirst("animations/animation/Effects/SlashEffect.png");
        slashEffectAnimation = makeAnimation(
            slashEffectSheet,
            SLASH_EFFECT_FRAME_COUNT,
            0,
            SLASH_EFFECT_FRAME_COUNT - 1,
            20f,
            Animation.PlayMode.NORMAL
        );

        slashEffectUpSheet = loadTextureFirst("animations/animation/Effects/UpSlashEffect.png");
        slashEffectUpAnimation = makeAnimation(
            slashEffectUpSheet,
            SLASH_EFFECT_UP_FRAME_COUNT,
            0,
            SLASH_EFFECT_UP_FRAME_COUNT - 1,
            20f,
            Animation.PlayMode.NORMAL
        );

        slashEffectDownSheet = loadTextureFirst("animations/animation/Effects/DownSlashEffect.png");
        slashEffectDownAnimation = makeAnimation(
            slashEffectDownSheet,
            SLASH_EFFECT_DOWN_FRAME_COUNT,
            0,
            SLASH_EFFECT_DOWN_FRAME_COUNT - 1,
            20f,
            Animation.PlayMode.NORMAL
        );

        dashEffectSheet = loadTextureFirst("animations/animation/Effects/Dash Effect.png");
        dashEffectAnimation = makeAnimation(
            dashEffectSheet,
            8,
            0,
            7,
            8f,
            Animation.PlayMode.NORMAL
        );

        loadSpellAnimations();
    }

    protected void loadSpellAnimations() {
        soulBallFrameTextures = new Array<>();
        blastFrameTextures = new Array<>();
        shadowScreamFrameTextures = new Array<>();

        soulBallAnimation = loadNumberedFrameAnimation(
            soulBallFrameTextures,
            0,
            3,
            16f,
            Animation.PlayMode.LOOP,
            "animations/animation/Projectile/SoulBall_"
        );

        blastAnimation = loadNumberedFrameAnimation(
            blastFrameTextures,
            0,
            7,
            20f,
            Animation.PlayMode.NORMAL,
            "animations/animation/Projectile/Blast_"
        );

        shadowScreamAnimation = loadNumberedFrameAnimation(
            shadowScreamFrameTextures,
            0,
            12,
            20f,
            Animation.PlayMode.NORMAL,
            "animations/animation/Effects/ShadowScream_"
        );
    }

    protected Animation<TextureRegion> loadNumberedFrameAnimation(
        Array<Texture> ownedTextures,
        int firstFrame,
        int lastFrame,
        float fps,
        Animation.PlayMode playMode,
        String prefix
    ) {
        Array<TextureRegion> frames = new Array<>(TextureRegion.class);
        for (int frame = firstFrame; frame <= lastFrame; frame++) {
            String path = prefix + String.format("%03d.png", frame);
            Texture texture = new Texture(path);
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            ownedTextures.add(texture);
            frames.add(new TextureRegion(texture));
        }
        return new Animation<>(1f / fps, frames, playMode);
    }

    protected Texture loadTextureFirst(String path) {
        Texture texture = new Texture(path);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return texture;
    }

    protected Animation<TextureRegion> makeAnimation(Texture sheet, int totalFrames, int startFrame, int endFrame, float fps, Animation.PlayMode playMode) {
            TextureRegion[][] split = TextureRegion.split(
                sheet,
                sheet.getWidth() / totalFrames,
                sheet.getHeight()
            );

            Array<TextureRegion> frames = new Array<>(TextureRegion.class);

            for (int i = startFrame; i <= endFrame && i < split[0].length; i++) {
                frames.add(split[0][i]);
            }

            return new Animation<>(1f / fps, frames, playMode);
        }

    protected Animation<TextureRegion> makeAutoHorizontalAnimation(Texture sheet, float fps, Animation.PlayMode playMode) {
            int frameCount = Math.max(1, Math.round(sheet.getWidth() / (float) sheet.getHeight()));

            if (frameCount < 2 || frameCount > 32 || sheet.getWidth() % frameCount != 0) {
                frameCount = 1;

                for (int i = 32; i >= 2; i--) {
                    if (sheet.getWidth() % i == 0 && sheet.getWidth() / i >= 16) {
                        frameCount = i;
                        break;
                    }
                }
            }

            TextureRegion[][] split = TextureRegion.split(
                sheet,
                sheet.getWidth() / frameCount,
                sheet.getHeight()
            );

            Array<TextureRegion> frames = new Array<>(TextureRegion.class);

            for (int i = 0; i < split[0].length; i++) {
                frames.add(split[0][i]);
            }

            return new Animation<>(1f / fps, frames, playMode);
        }
}
