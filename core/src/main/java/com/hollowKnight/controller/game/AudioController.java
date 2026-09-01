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

abstract class AudioController extends EnemySpawnController {
    protected AudioController(Game game, int saveSlot, boolean loadSavedGame) {
        super(game, saveSlot, loadSavedGame);
    }

    protected String getMusicPathForMusicObject(MapObject musicObject) {
        Object value = musicObject.getProperties().get("music");
        if (value != null) {
            String path = String.valueOf(value).trim();
            return path.isEmpty() ? null : path;
        }

        String name = musicObject.getName();
        if (name == null) {
            return null;
        }

        String normalized = name.toLowerCase();
        if (normalized.contains("crystal")) {
            return "music/Crystal Main.wav";
        }
        if (normalized.contains("cross") || normalized.contains("forgotten")) {
            return "music/Crossroads Main.wav";
        }
        return null;
    }

    protected void loadSfx() {
        dashSfx = loadSoundFirst("sfx/hero_dash.wav");
        deathSfx = loadSoundFirst("sfx/hero_death_v2.wav");
        deathExtraSfx = loadSoundFirst("sfx/hero_death_extra_details.wav");
        heroDamageSfx = loadSoundFirst("sfx/hero_damage_less_harsh.wav");
        doubleJumpSfx = loadOptionalSound("music/sfx/hero_double_jump.wav");
        fallingSfx = loadOptionalSound("music/sfx/hero_falling.wav");
        fireballSfx = loadOptionalSound("music/sfx/hero_fireball.wav");
        landSoftSfx = loadOptionalSound("music/sfx/hero_land_soft.wav");
        footstepsStoneSfx = loadSoundFirst("sfx/hero_run_footsteps_stone.wav");

        attackSfxVariants = new Array<>();
        for (int i = 1; i <= 5; i++) {
            addSoundToArray(attackSfxVariants, "sfx/sword_" + i + ".wav");
        }

        attackSfx = null;
        soulPickupSfx = new Array<>();
        for (int i = 1; i <= 7; i++) {
            addSoundToArray(soulPickupSfx, "sfx/soul_pickup_" + i + ".wav");
        }

        howlingWraithsSfx = loadSoundFirst("sfx/hero_scream_spell.wav");
        focusChargingSfx = loadSoundFirst("sfx/focus_health_charging.wav");
        focusHealSfx = loadSoundFirst("sfx/focus_health_heal.wav");
        focusReadySfx = loadSoundFirst("sfx/focus_ready.wav");
        heartbeatSfx = loadSoundFirst("sfx/heartbeat_B_01.wav");
        flyFlyingSfx = loadSoundFirst("sfx/fly_flying.wav");
        terrainRejectSfx = loadSoundFirst("sfx/sword_hit_reject.wav");
        breakableWallHitOneSfx = loadSoundFirst("sfx/breakable_wall_hit_1.wav");
        breakableWallHitTwoSfx = loadSoundFirst("sfx/breakable_wall_hit_2.wav");
        breakableWallDeathSfx = loadSoundFirst("sfx/breakable_wall_death.wav");
    }

    protected void loadZoteSfx() {
        zoteVoiceSfx = new Array<>();
        addZoteVoiceSfx("sfx/Zote_01.wav");
        addZoteVoiceSfx("sfx/Zote_02.wav");
        addZoteVoiceSfx("sfx/Zote_03.wav");
        addZoteVoiceSfx("sfx/Zote_04.wav");
        addZoteVoiceSfx("sfx/Zote_05.wav");
    }

    protected void addZoteVoiceSfx(String path) {
            if (Gdx.files.internal(path).exists()) {
                zoteVoiceSfx.add(Gdx.audio.newSound(Gdx.files.internal(path)));
            }
        }

    protected void playRandomZoteVoice() {
            if (zoteVoiceSfx == null || zoteVoiceSfx.size == 0) {
                return;
            }

            playSfx(zoteVoiceSfx.random(), ZOTE_VOICE_VOLUME);
        }

    protected void loadFalseKnightSfx() {
        falseKnightAttackVoiceSfx = new Array<>();
        falseKnightHitSfx = new Array<>();

        falseKnightSwingSfx = loadSoundFirst("sfx/false_knight_swing.wav");
        falseKnightStrikeGroundSfx = loadSoundFirst("sfx/false_knight_strike_ground.wav");
        falseKnightJumpSfx = loadSoundFirst("sfx/false_knight_jump.wav");
        falseKnightLandSfx = loadSoundFirst("sfx/false_knight_land.wav");
        falseKnightLandFirstSfx = loadSoundFirst("sfx/false_knight_land_1st_time.wav");
        falseKnightRollSfx = loadSoundFirst("sfx/false_knight_roll.wav");
        falseKnightDamageArmorSfx = loadSoundFirst("sfx/false_knight_damage_armour.wav");
        falseKnightDamageArmorFinalSfx = loadSoundFirst("sfx/false_knight_damage_armour_final.wav");
        falseKnightHeadDamageSfx = loadSoundFirst("sfx/false_knight_head_damage_2.wav");
        falseKnightDazeSfx = loadSoundFirst("Audio_Files/Fknight_daze.wav");
        falseKnightDeathSfx = loadSoundFirst("sfx/boss_final_hit.wav");
        falseKnightBossExplodeSfx = loadSoundFirst("sfx/boss_explode.wav");
        falseKnightFallSfx = loadSoundFirst("Audio_Files/FKnight_fall_option_02.wav");
        falseKnightFlumpOneSfx = loadSoundFirst("Audio_Files/Fknight_flump_01.wav");
        falseKnightFlumpTwoSfx = loadSoundFirst("Audio_Files/Fknight_flump_02.wav");
        falseKnightRageSfx = loadSoundFirst("Audio_Files/FKnight_Rage.wav");
        falseKnightCeilingBreakSfx = loadSoundFirst("sfx/false_knight_ceiling_break.wav");

        for (int i = 1; i <= 5; i++) {
            addFalseKnightSound(
                falseKnightAttackVoiceSfx,
                "sfx/False_Knight_Attack_New_0" + i + ".wav"
            );
        }

        addFalseKnightSound(falseKnightHitSfx, "Audio_Files/Fknight_hit_01.wav");
        addFalseKnightSound(falseKnightHitSfx, "Audio_Files/Fknight_hit_02.wav");
        addFalseKnightSound(falseKnightHitSfx, "Audio_Files/Fknight_hit_03.wav");
        addFalseKnightSound(falseKnightHitSfx, "Audio_Files/Fknight_hit_05.wav");
        addFalseKnightSound(falseKnightHitSfx, "Audio_Files/Fknight_hit_06.wav");
    }

    protected void addFalseKnightSound(Array<Sound> sounds, String path) {
        Sound sound = loadSoundFirst(path);
        if (sound != null) {
            sounds.add(sound);
        }
    }

    protected void processFalseKnightEvents(FalseKnight falseKnight) {
            if (falseKnight == null) {
                return;
            }

            FalseKnight.Event event;

            while ((event = falseKnight.pollEvent()) != null) {
                playFalseKnightEvent(event);
            }
        }

    protected void playFalseKnightEvent(FalseKnight.Event event) {
            if (event == null) {
                return;
            }

            switch (event) {
                case SWING:
                    playSfx(falseKnightSwingSfx, FALSE_KNIGHT_SFX_VOLUME);
                    playRandomSound(falseKnightAttackVoiceSfx, FALSE_KNIGHT_SFX_VOLUME);
                    break;
                case SLAM_IMPACT:
                    playSfx(falseKnightStrikeGroundSfx, FALSE_KNIGHT_HEAVY_SFX_VOLUME);
                    startScreenShake(18f);
                    break;
                case POWER_SLAM_IMPACT:
                    playSfx(falseKnightStrikeGroundSfx, FALSE_KNIGHT_HEAVY_SFX_VOLUME);
                    playSfx(falseKnightLandFirstSfx, FALSE_KNIGHT_HEAVY_SFX_VOLUME);
                    startScreenShake(26f);
                    break;
                case JUMP:
                    playSfx(falseKnightJumpSfx, FALSE_KNIGHT_SFX_VOLUME);
                    break;
                case LAND:
                    playSfx(falseKnightLandSfx, FALSE_KNIGHT_SFX_VOLUME);
                    startScreenShake(13f);
                    break;
                case CHARGE:
                    playSfx(falseKnightRollSfx, FALSE_KNIGHT_SFX_VOLUME);
                    break;
                case ARMOR_HIT:
                    playSfx(falseKnightDamageArmorSfx, FALSE_KNIGHT_SFX_VOLUME);
                    playRandomSound(falseKnightHitSfx, FALSE_KNIGHT_SFX_VOLUME);
                    break;
                case ARMOR_FINAL_HIT:
                    playSfx(falseKnightDamageArmorFinalSfx, FALSE_KNIGHT_HEAVY_SFX_VOLUME);
                    playRandomSound(falseKnightHitSfx, FALSE_KNIGHT_HEAVY_SFX_VOLUME);
                    startScreenShake(18f);
                    break;
                case HEAD_HIT:
                    playSfx(falseKnightHeadDamageSfx, FALSE_KNIGHT_SFX_VOLUME);
                    playRandomSound(falseKnightHitSfx, FALSE_KNIGHT_SFX_VOLUME);
                    break;
                case ENTER_STUN:
                    playSfx(falseKnightDazeSfx, FALSE_KNIGHT_HEAVY_SFX_VOLUME);
                    playSfx(falseKnightFlumpOneSfx, FALSE_KNIGHT_SFX_VOLUME);
                    startScreenShake(17f);
                    break;
                case RAGE:
                    playSfx(falseKnightRageSfx, FALSE_KNIGHT_HEAVY_SFX_VOLUME);
                    startScreenShake(16f);
                    break;
                case DEATH:
                    playSfx(falseKnightDeathSfx, FALSE_KNIGHT_HEAVY_SFX_VOLUME);
                    playSfx(falseKnightBossExplodeSfx, FALSE_KNIGHT_HEAVY_SFX_VOLUME);
                    startScreenShake(22f);
                    break;
                case FALL:
                    playSfx(falseKnightFallSfx, FALSE_KNIGHT_SFX_VOLUME);
                    break;
                case STUN_RECOVER:
                    playSfx(falseKnightFlumpTwoSfx, FALSE_KNIGHT_SFX_VOLUME);
                    break;
                case CEILING_BREAK:
                    playSfx(falseKnightCeilingBreakSfx, FALSE_KNIGHT_HEAVY_SFX_VOLUME);
                    break;
                default:
                    break;
            }
        }

    protected void playRandomSound(Array<Sound> sounds, float volume) {
            if (sounds == null || sounds.size == 0) {
                return;
            }

            playSfx(sounds.random(), volume);
        }

    protected void disposeFalseKnightSfx() {
            disposeSoundArray(falseKnightAttackVoiceSfx);
            disposeSoundArray(falseKnightHitSfx);
            falseKnightAttackVoiceSfx = null;
            falseKnightHitSfx = null;

            if (falseKnightSwingSfx != null) falseKnightSwingSfx.dispose();
            if (falseKnightStrikeGroundSfx != null) falseKnightStrikeGroundSfx.dispose();
            if (falseKnightJumpSfx != null) falseKnightJumpSfx.dispose();
            if (falseKnightLandSfx != null) falseKnightLandSfx.dispose();
            if (falseKnightLandFirstSfx != null) falseKnightLandFirstSfx.dispose();
            if (falseKnightRollSfx != null) falseKnightRollSfx.dispose();
            if (falseKnightDamageArmorSfx != null) falseKnightDamageArmorSfx.dispose();
            if (falseKnightDamageArmorFinalSfx != null) falseKnightDamageArmorFinalSfx.dispose();
            if (falseKnightHeadDamageSfx != null) falseKnightHeadDamageSfx.dispose();
            if (falseKnightDazeSfx != null) falseKnightDazeSfx.dispose();
            if (falseKnightDeathSfx != null) falseKnightDeathSfx.dispose();
            if (falseKnightFallSfx != null) falseKnightFallSfx.dispose();
            if (falseKnightFlumpOneSfx != null) falseKnightFlumpOneSfx.dispose();
            if (falseKnightFlumpTwoSfx != null) falseKnightFlumpTwoSfx.dispose();
            if (falseKnightRageSfx != null) falseKnightRageSfx.dispose();
            if (falseKnightCeilingBreakSfx != null) falseKnightCeilingBreakSfx.dispose();
            if (falseKnightBossExplodeSfx != null) falseKnightBossExplodeSfx.dispose();
            falseKnightSwingSfx = null;
            falseKnightStrikeGroundSfx = null;
            falseKnightJumpSfx = null;
            falseKnightLandSfx = null;
            falseKnightLandFirstSfx = null;
            falseKnightRollSfx = null;
            falseKnightDamageArmorSfx = null;
            falseKnightDamageArmorFinalSfx = null;
            falseKnightHeadDamageSfx = null;
            falseKnightDazeSfx = null;
            falseKnightDeathSfx = null;
            falseKnightFallSfx = null;
            falseKnightFlumpOneSfx = null;
            falseKnightFlumpTwoSfx = null;
            falseKnightRageSfx = null;
            falseKnightCeilingBreakSfx = null;
            falseKnightBossExplodeSfx = null;
        }

    protected void disposeSoundArray(Array<Sound> sounds) {
            if (sounds == null) {
                return;
            }

            for (Sound sound : sounds) {
                if (sound != null) {
                    sound.dispose();
                }
            }

            sounds.clear();
        }

    protected Sound loadSoundFirst(String path) {
        if (!Gdx.files.internal(path).exists()) {
            return null;
        }
        return Gdx.audio.newSound(Gdx.files.internal(path));
    }

    protected Sound loadSoundIfExists(String path) {
            if (!Gdx.files.internal(path).exists()) {
                System.out.println("SFX file not found: " + path);
                return null;
            }

            return Gdx.audio.newSound(Gdx.files.internal(path));
        }

    protected Sound loadOptionalSound(String path) {
        return loadSoundFirst(path);
    }

    protected void addSoundToArray(Array<Sound> sounds, String path) {
        Sound sound = loadSoundFirst(path);
        if (sound != null) {
            sounds.add(sound);
        }
    }

    protected void playAttackSfx() {
            if (attackSfxVariants != null && attackSfxVariants.size > 0) {
                playRandomSound(attackSfxVariants, SFX_ATTACK_VOLUME);
            } else {
                playAttackSfx();
            }
        }

    protected void playSfx(Sound sound, float volume) {
            float outputVolume = GameSettings.getSfxOutputVolume(volume);

            if (sound != null && outputVolume > 0f) {
                sound.play(outputVolume);
            }
        }

    protected void resetFalseKnightCombatAfterPlayerDeath() {
            stopFalseKnightSfx();
            screenShakeTimer = 0f;
            screenShakeDuration = 0f;
            screenShakeMagnitude = 0f;
            activeBossArena = null;
            lockedBoss = null;
            bossArenaEngaged = false;

            if (falseKnights == null) {
                return;
            }

            for (FalseKnight falseKnight : falseKnights) {
                if (falseKnight != null) {
                    falseKnight.resetToSpawn();
                }
            }
        }

    protected void stopFalseKnightSfx() {
            stopSound(falseKnightSwingSfx);
            stopSound(falseKnightStrikeGroundSfx);
            stopSound(falseKnightJumpSfx);
            stopSound(falseKnightLandSfx);
            stopSound(falseKnightLandFirstSfx);
            stopSound(falseKnightRollSfx);
            stopSound(falseKnightDamageArmorSfx);
            stopSound(falseKnightDamageArmorFinalSfx);
            stopSound(falseKnightHeadDamageSfx);
            stopSound(falseKnightDazeSfx);
            stopSound(falseKnightDeathSfx);
            stopSound(falseKnightFallSfx);
            stopSound(falseKnightFlumpOneSfx);
            stopSound(falseKnightFlumpTwoSfx);
            stopSound(falseKnightRageSfx);
            stopSound(falseKnightCeilingBreakSfx);
            stopSound(falseKnightBossExplodeSfx);
            stopSoundArray(falseKnightAttackVoiceSfx);
            stopSoundArray(falseKnightHitSfx);
        }

    protected void stopSound(Sound sound) {
            if (sound != null) {
                sound.stop();
            }
        }

    protected void stopSoundArray(Array<Sound> sounds) {
            if (sounds == null) {
                return;
            }

            for (Sound sound : sounds) {
                stopSound(sound);
            }
        }

    public void playFireballSfx() {
            playSfx(fireballSfx, SFX_FIREBALL_VOLUME);
        }

    protected void updatePlayerSfx(float delta) {
            boolean isGrounded = knightModel.isGrounded();
            boolean isFalling = !isGrounded && knightModel.getVelocity().y < -80f;
            boolean isHealing = knightModel.isHealing();
            int currentHealth = knightModel.getCurrentHealth();
            int currentSoul = knightModel.getCurrentSoul();
            boolean isRunningOnGround =
                currentHealth > 0 &&
                    isGrounded &&
                    !knightModel.isDashing() &&
                    !knightModel.isAttacking() &&
                    Math.abs(knightModel.getVelocity().x) > 30f;

            if (!sfxStateInitialized) {
                wasGroundedForSfx = isGrounded;
                wasFallingForSfx = isFalling;
                previousHealthForSfx = currentHealth;
                previousSoulForSfx = currentSoul;
                wasHealingForSfx = isHealing;
                sfxStateInitialized = true;
            }

            if (currentHealth < previousHealthForSfx && currentHealth > 0) {
                playSfx(heroDamageSfx, SFX_DAMAGE_VOLUME);
            }

            if (!wasHealingForSfx && isHealing) {
                startFocusChargingSfx();
            } else if (wasHealingForSfx && !isHealing) {
                stopFocusChargingSfx();
                if (currentHealth > previousHealthForSfx) {
                    playSfx(focusHealSfx, SFX_FOCUS_VOLUME);
                }
            }

            if (previousSoulForSfx < knightModel.getHealSoulCost() &&
                currentSoul >= knightModel.getHealSoulCost() &&
                currentHealth < knightModel.getMaxHealth()) {
                playSfx(focusReadySfx, SFX_FOCUS_VOLUME);
            }

            if (!wasGroundedForSfx && isGrounded) {
                stopFallingSfx();
                playSfx(landSoftSfx, SFX_LAND_VOLUME);
            }

            if (isFalling) {
                startFallingSfx();
            } else {
                stopFallingSfx();
            }

            if (isRunningOnGround) {
                startFootsteps();
            } else {
                stopFootsteps();
            }

            updateHeartbeatSfx(currentHealth);
            updateFlyingEnemySfx();

            if (currentHealth <= 0) {
                stopFootsteps();
                stopFallingSfx();
                stopFocusChargingSfx();
                stopHeartbeatSfx();
                stopFlyFlyingSfx();

                if (!deathSfxPlayed) {
                    playSfx(deathSfx, SFX_DEATH_VOLUME);
                    playSfx(deathExtraSfx, SFX_DEATH_VOLUME);
                    deathSfxPlayed = true;
                }
            } else {
                deathSfxPlayed = false;
            }

            wasGroundedForSfx = isGrounded;
            wasFallingForSfx = isFalling;
            previousHealthForSfx = currentHealth;
            previousSoulForSfx = currentSoul;
            wasHealingForSfx = isHealing;
        }

    protected void startFootsteps() {
            if (footstepsStoneSfx == null) {
                return;
            }

            if (footstepLoopId != -1L) {
                return;
            }

            float outputVolume = GameSettings.getSfxOutputVolume(SFX_FOOTSTEP_VOLUME);
            if (outputVolume <= 0f) {
                return;
            }

            footstepLoopId = footstepsStoneSfx.loop(outputVolume);
        }

    protected void stopFootsteps() {
            if (footstepsStoneSfx != null && footstepLoopId != -1L) {
                footstepsStoneSfx.stop(footstepLoopId);
            }

            footstepLoopId = -1L;
        }

    protected void startFallingSfx() {
            if (fallingSfx == null || fallingSoundId != -1L) {
                return;
            }

            float outputVolume = GameSettings.getSfxOutputVolume(SFX_FALLING_VOLUME);
            if (outputVolume <= 0f) {
                return;
            }

            fallingSoundId = fallingSfx.loop(outputVolume);
        }

    protected void stopFallingSfx() {
            if (fallingSfx != null && fallingSoundId != -1L) {
                fallingSfx.stop(fallingSoundId);
            }

            fallingSoundId = -1L;
        }

    protected void startFocusChargingSfx() {
            if (focusChargingSfx == null || focusChargingLoopId != -1L) {
                return;
            }

            float volume = GameSettings.getSfxOutputVolume(SFX_FOCUS_VOLUME);
            if (volume > 0f) {
                focusChargingLoopId = focusChargingSfx.loop(volume);
            }
        }

    protected void stopFocusChargingSfx() {
            if (focusChargingSfx != null && focusChargingLoopId != -1L) {
                focusChargingSfx.stop(focusChargingLoopId);
            }
            focusChargingLoopId = -1L;
        }

    protected void updateHeartbeatSfx(int currentHealth) {
            boolean shouldBeat = currentHealth == 1 && !deathRespawnPending;
            if (!shouldBeat) {
                stopHeartbeatSfx();
                return;
            }

            if (heartbeatSfx == null || heartbeatLoopId != -1L) {
                return;
            }

            float volume = GameSettings.getSfxOutputVolume(SFX_HEARTBEAT_VOLUME);
            if (volume > 0f) {
                heartbeatLoopId = heartbeatSfx.loop(volume);
            }
        }

    protected void stopHeartbeatSfx() {
            if (heartbeatSfx != null && heartbeatLoopId != -1L) {
                heartbeatSfx.stop(heartbeatLoopId);
            }
            heartbeatLoopId = -1L;
        }

    protected void updateFlyingEnemySfx() {
            boolean nearbyFlyingEnemy = false;
            if (mosquitoes != null && knightModel != null) {
                Rectangle knightBounds = knightModel.getBounds();
                float knightX = knightBounds.x + knightBounds.width / 2f;
                float knightY = knightBounds.y + knightBounds.height / 2f;
                float maxDistanceSquared = 900f * 900f;

                for (Mosquito mosquito : mosquitoes) {
                    if (mosquito == null || !mosquito.isAlive()) {
                        continue;
                    }

                    Rectangle bounds = mosquito.getBounds();
                    float dx = bounds.x + bounds.width / 2f - knightX;
                    float dy = bounds.y + bounds.height / 2f - knightY;
                    if (dx * dx + dy * dy <= maxDistanceSquared) {
                        nearbyFlyingEnemy = true;
                        break;
                    }
                }
            }

            if (!nearbyFlyingEnemy) {
                stopFlyFlyingSfx();
                return;
            }

            if (flyFlyingSfx == null || flyFlyingLoopId != -1L) {
                return;
            }

            float volume = GameSettings.getSfxOutputVolume(SFX_FLY_VOLUME);
            if (volume > 0f) {
                flyFlyingLoopId = flyFlyingSfx.loop(volume);
            }
        }

    protected void stopFlyFlyingSfx() {
            if (flyFlyingSfx != null && flyFlyingLoopId != -1L) {
                flyFlyingSfx.stop(flyFlyingLoopId);
            }
            flyFlyingLoopId = -1L;
        }

    protected void stopPlayerLoopingSfx() {
            stopFootsteps();
            stopFallingSfx();
            stopFocusChargingSfx();
            stopHeartbeatSfx();
            stopFlyFlyingSfx();
        }
}
