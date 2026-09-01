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

abstract class AbilityController extends RenderController {
    protected AbilityController(Game game, int saveSlot, boolean loadSavedGame) {
        super(game, saveSlot, loadSavedGame);
    }

    protected void castVengefulSpirit() {
            if (knightModel == null || paused || deathRespawnPending || victoryPending || noclipMode || isZoteDialogueActive()) {
                return;
            }

            if (!knightModel.startSpellCast(SPELL_SOUL_COST, VENGEFUL_CAST_LOCK)) {
                if (knightModel.getCurrentSoul() < SPELL_SOUL_COST) {
                    showCheatMessage("Vengeful Spirit needs 33 Soul");
                }
                return;
            }

            knightController.clearMovementState();
            Rectangle knightBounds = knightModel.getBounds();
            int direction = knightModel.isFacingLeft() ? -1 : 1;
            float projectileX = direction < 0
                ? knightBounds.x - VengefulSpiritProjectile.DEFAULT_WIDTH - 12f
                : knightBounds.x + knightBounds.width + 12f;
            float projectileY = knightBounds.y + knightBounds.height * 0.52f
                - VengefulSpiritProjectile.DEFAULT_HEIGHT / 2f;

            int spellDamage = isCharmEquipped(CharmId.VOID_HEART)
                ? Math.round(BASE_VENGEFUL_DAMAGE * 1.5f)
                : BASE_VENGEFUL_DAMAGE;
            VengefulSpiritProjectile projectile = new VengefulSpiritProjectile(
                projectileX,
                projectileY,
                direction,
                VengefulSpiritProjectile.DEFAULT_SPEED,
                VengefulSpiritProjectile.DEFAULT_LIFETIME,
                spellDamage
            );
            vengefulProjectiles.add(projectile);

            showVengefulCastAnimation = true;
            fireballCastStateTime = 0f;
            AchievementManager.getInstance().recordSpellUse("Vengeful Spirit");
            startScreenShake(VENGEFUL_SCREEN_SHAKE_MAGNITUDE, VENGEFUL_SCREEN_SHAKE_DURATION);
            playSfx(fireballSfx, SFX_FIREBALL_VOLUME);
        }

    protected void castHowlingWraiths() {
            if (knightModel == null || paused || deathRespawnPending || victoryPending || noclipMode || isZoteDialogueActive()) {
                return;
            }

            if (!knightModel.startSpellCast(SPELL_SOUL_COST, HOWLING_CAST_LOCK)) {
                if (knightModel.getCurrentSoul() < SPELL_SOUL_COST) {
                    showCheatMessage("Howling Wraiths needs 33 Soul");
                }
                return;
            }

            knightController.clearMovementState();
            Rectangle knightBounds = knightModel.getBounds();
            float centerX = knightBounds.x + knightBounds.width / 2f;
            float bottomY = knightBounds.y + knightBounds.height - 8f;
            int damagePerTick = isCharmEquipped(CharmId.VOID_HEART)
                ? Math.round(BASE_HOWLING_DAMAGE_PER_TICK * 1.5f)
                : BASE_HOWLING_DAMAGE_PER_TICK;
            howlingWraithCasts.add(new HowlingWraithsCast(
                centerX,
                bottomY,
                HowlingWraithsCast.DEFAULT_WIDTH,
                HowlingWraithsCast.DEFAULT_HEIGHT,
                HowlingWraithsCast.DEFAULT_DURATION,
                damagePerTick
            ));
            AchievementManager.getInstance().recordSpellUse("Howling Wraiths");
            startScreenShake(HOWLING_SCREEN_SHAKE_MAGNITUDE, HOWLING_SCREEN_SHAKE_DURATION);
            playSfx(howlingWraithsSfx, SFX_FIREBALL_VOLUME);
        }

    protected void updateCharmPickups(float delta) {
            if (charmPickups == null || knightModel == null || charmInventory == null) {
                return;
            }

            Rectangle knightBounds = knightModel.getBounds();
            for (CharmPickup pickup : charmPickups) {
                if (pickup == null || pickup.isCollected()) {
                    continue;
                }

                pickup.update(delta);
                if (!knightBounds.overlaps(pickup.getBounds())) {
                    continue;
                }

                pickup.collect();
                if (charmInventory.collect(pickup.getCharmId())) {
                    showCheatMessage("Charm found: " + pickup.getCharmId().getDisplayName());
                    if (inventoryOverlay != null) {
                        inventoryOverlay.refresh();
                    }
                    saveCurrentGame();
                }
            }
        }

    protected void drawCharmPickups(boolean hiddenBehindSecretWallPass) {
            if (charmPickups == null || charmIconLibrary == null) {
                return;
            }

            for (CharmPickup pickup : charmPickups) {
                if (pickup == null || pickup.isCollected()) {
                    continue;
                }

                boolean hiddenBehindSecretWall = pickup.getCharmId() == CharmId.VOID_HEART;
                if (hiddenBehindSecretWall != hiddenBehindSecretWallPass) {
                    continue;
                }

                Rectangle bounds = pickup.getBounds();
                if (!isInCameraRenderRange(bounds, ENEMY_RENDER_MARGIN)) {
                    continue;
                }

                TextureRegion icon = charmIconLibrary.get(pickup.getCharmId());
                if (icon == null) {
                    continue;
                }

                float bobY = MathUtils.sin(pickup.getStateTime() * CHARM_PICKUP_BOB_SPEED) * CHARM_PICKUP_BOB_AMOUNT;
                float drawWidth = Math.max(42f, bounds.width * pickup.getDrawScale());
                float drawHeight = Math.max(42f, bounds.height * pickup.getDrawScale());
                float drawX = bounds.x + (bounds.width - drawWidth) / 2f;
                float drawY = bounds.y + (bounds.height - drawHeight) / 2f + bobY;

                batch.setColor(1f, 1f, 1f, 0.98f);
                batch.draw(icon, drawX, drawY, drawWidth, drawHeight);
                batch.setColor(Color.WHITE);
            }
        }

    protected void updateSpells(float delta) {
            if (vengefulProjectiles != null) {
                for (int i = vengefulProjectiles.size - 1; i >= 0; i--) {
                    VengefulSpiritProjectile projectile = vengefulProjectiles.get(i);

                    if (projectile == null) {
                        vengefulProjectiles.removeIndex(i);
                        continue;
                    }

                    projectile.update(delta);
                    Rectangle projectileBounds = projectile.getCollisionBounds();

                    if (projectile.canHitTerrain() && spellHitsTerrain(projectileBounds)) {
                        spawnSpellBlast(
                            projectileBounds.x + projectileBounds.width / 2f,
                            projectileBounds.y + projectileBounds.height / 2f,
                            projectile.getDirection() < 0,
                            0.92f
                        );
                        projectile.deactivate();
                    }

                    if (projectile.isActive() && enemies != null) {
                        for (Damageable enemy : enemies) {
                            if (enemy == null || !projectile.canDamage(enemy)) {
                                continue;
                            }

                            Rectangle enemyBounds = enemy.getBounds();
                            if (!isInEnemySimulationRange(enemyBounds) ||
                                !projectileBounds.overlaps(enemyBounds)) {
                                continue;
                            }

                            float knockbackX = projectile.getDirection() * 115f;
                            applyDamageToEnemy(enemy, projectile.getDamage(), knockbackX, 55f);
                            projectile.markDamaged(enemy);
                            spawnSpellBlast(
                                enemyBounds.x + enemyBounds.width / 2f,
                                enemyBounds.y + enemyBounds.height / 2f,
                                projectile.getDirection() < 0,
                                0.72f
                            );
                        }
                    }

                    if (!projectile.isActive() ||
                        projectileBounds.x + projectileBounds.width < -256f ||
                        projectileBounds.x > mapPixelWidth + 256f) {
                        vengefulProjectiles.removeIndex(i);
                    }
                }
            }

            if (howlingWraithCasts != null) {
                for (int i = howlingWraithCasts.size - 1; i >= 0; i--) {
                    HowlingWraithsCast cast = howlingWraithCasts.get(i);

                    if (cast == null) {
                        howlingWraithCasts.removeIndex(i);
                        continue;
                    }

                    cast.update(delta);
                    int damageTicks = cast.consumePendingTicks();

                    for (int tick = 0; tick < damageTicks; tick++) {
                        if (enemies == null) {
                            break;
                        }

                        for (Damageable enemy : enemies) {
                            if (enemy == null || !enemy.isAlive()) {
                                continue;
                            }

                            Rectangle enemyBounds = enemy.getBounds();
                            if (isInEnemySimulationRange(enemyBounds) &&
                                cast.getBounds().overlaps(enemyBounds)) {
                                applyDamageToEnemy(enemy, cast.getDamagePerTick(), 0f, 95f);
                                spawnSpellBlast(
                                    enemyBounds.x + enemyBounds.width / 2f,
                                    enemyBounds.y + enemyBounds.height / 2f,
                                    false,
                                    0.68f
                                );
                            }
                        }
                    }

                    if (!cast.isActive()) {
                        howlingWraithCasts.removeIndex(i);
                    }
                }
            }

            if (spellBlastEffects != null) {
                for (int i = spellBlastEffects.size - 1; i >= 0; i--) {
                    SpellBlastAnim blast = spellBlastEffects.get(i);
                    blast.stateTime += delta;

                    if (blastAnimation == null || blastAnimation.isAnimationFinished(blast.stateTime)) {
                        spellBlastEffects.removeIndex(i);
                    }
                }
            }
        }

    protected boolean spellHitsTerrain(Rectangle bounds) {
            if (bounds == null) {
                return false;
            }

            if (nearbyPlatforms != null) {
                for (Rectangle platform : nearbyPlatforms) {
                    if (platform != null && bounds.overlaps(platform)) {
                        return true;
                    }
                }
            }

            if (nearbyPolygonPlatforms != null) {
                for (Polygon polygon : nearbyPolygonPlatforms) {
                    if (polygon != null && rectOverlapsPolygonForAttack(bounds, polygon)) {
                        return true;
                    }
                }
            }

            if (secretWalls != null) {
                for (SecretWallHitbox wall : secretWalls) {
                    if (wall == null || wall.destroyed) {
                        continue;
                    }
                    if (wall.rectangle != null && bounds.overlaps(wall.rectangle)) {
                        return true;
                    }
                    if (wall.polygon != null && rectOverlapsPolygonForAttack(bounds, wall.polygon)) {
                        return true;
                    }
                }
            }

            return false;
        }

    protected void spawnSpellBlast(float centerX, float centerY, boolean flipX, float scale) {
            if (spellBlastEffects == null || blastAnimation == null) {
                return;
            }

            spellBlastEffects.add(new SpellBlastAnim(centerX, centerY, flipX, Math.max(0.2f, scale)));
        }

    protected void drawSpellEffects() {
            boolean voidHeartVisual = isCharmEquipped(CharmId.VOID_HEART);
            if (voidHeartVisual) {
                batch.setColor(0.38f, 0.18f, 0.58f, 1f);
            }

            if (shadowScreamAnimation != null && howlingWraithCasts != null) {
                for (HowlingWraithsCast cast : howlingWraithCasts) {
                    if (cast == null || !cast.isActive()) {
                        continue;
                    }

                    TextureRegion frame = shadowScreamAnimation.getKeyFrame(cast.getAge(), false);
                    Rectangle bounds = cast.getBounds();
                    batch.draw(frame, bounds.x, bounds.y, bounds.width, bounds.height);
                }
            }

            if (soulBallAnimation != null && vengefulProjectiles != null) {
                for (VengefulSpiritProjectile projectile : vengefulProjectiles) {
                    if (projectile == null || !projectile.isActive()) {
                        continue;
                    }

                    TextureRegion frame = soulBallAnimation.getKeyFrame(projectile.getAge(), true);
                    boolean shouldFlip = projectile.getDirection() < 0;
                    Rectangle bounds = projectile.getBounds();
                    drawTextureRegion(
                        frame,
                        bounds.x,
                        bounds.y,
                        bounds.width,
                        bounds.height,
                        shouldFlip
                    );
                }
            }

            if (blastAnimation != null && spellBlastEffects != null) {
                for (SpellBlastAnim blast : spellBlastEffects) {
                    TextureRegion frame = blastAnimation.getKeyFrame(blast.stateTime, false);
                    float width = frame.getRegionWidth() * blast.scale;
                    float height = frame.getRegionHeight() * blast.scale;
                    drawTextureRegion(
                        frame,
                        blast.x - width / 2f,
                        blast.y - height / 2f,
                        width,
                        height,
                        blast.flipX
                    );
                }
            }

            if (voidHeartVisual) {
                batch.setColor(Color.WHITE);
            }
        }

    protected boolean handleCheatKeyDown(int keycode) {
            if (!isControlPressed()) {
                return false;
            }

            switch (keycode) {
                case Input.Keys.NUM_1:
                    teleportToBossArenaCheat();
                    return true;
                case Input.Keys.NUM_2:
                    toggleNoclipCheat();
                    return true;
                case Input.Keys.NUM_3:
                    emergencyHealCheat();
                    return true;
                case Input.Keys.NUM_4:
                    refillSoulCheat();
                    return true;
                case Input.Keys.NUM_5:
                    godMode = !godMode;
                    showCheatMessage("God Mode: " + (godMode ? "ON" : "OFF"));
                    return true;
                case Input.Keys.NUM_6:
                    instaKillVisibleEnemiesCheat();
                    return true;
                default:
                    return false;
            }
        }

    protected boolean isControlPressed() {
            return Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ||
                Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
        }

    protected void showCheatMessage(String message) {
            if (cheatStatusLabel == null) {
                return;
            }

            cheatStatusLabel.setText(message == null ? "" : message);
            cheatStatusLabel.setVisible(true);
            cheatStatusTimer = CHEAT_MESSAGE_TIME;
        }

    protected void updateCheatStatus(float delta) {
            if (cheatStatusLabel == null || cheatStatusTimer <= 0f) {
                return;
            }

            cheatStatusTimer = Math.max(0f, cheatStatusTimer - delta);
            if (cheatStatusTimer <= 0f) {
                cheatStatusLabel.setVisible(false);
            }
        }

    protected void teleportToBossArenaCheat() {
            if (falseKnights == null || falseKnights.size == 0 || knightModel == null) {
                showCheatMessage("No False Knight was found on this map");
                return;
            }

            FalseKnight target = null;
            for (FalseKnight candidate : falseKnights) {
                if (candidate != null && candidate.getState() != FalseKnight.State.DEAD) {
                    target = candidate;
                    break;
                }
            }

            if (target == null) {
                showCheatMessage("The False Knight is already defeated");
                return;
            }

            Rectangle arena = target.getArenaBounds();
            if (arena == null) {
                Rectangle body = target.getBodyBounds();
                arena = new Rectangle(
                    Math.max(0f, body.x - FalseKnight.DEFAULT_ARENA_PADDING),
                    Math.max(0f, body.y - 16f),
                    FalseKnight.DEFAULT_ARENA_PADDING * 2f + body.width,
                    Math.max(520f, body.height + 300f)
                );
                target.setArenaBounds(arena);
            }

            Rectangle bossBounds = target.getBodyBounds();
            float maxX = arena.x + arena.width - knightModel.getBounds().width - 40f;
            float teleportX = MathUtils.clamp(bossBounds.x - 230f, arena.x + 40f, Math.max(arena.x + 40f, maxX));
            float maxY = arena.y + arena.height - knightModel.getBounds().height - 12f;
            float teleportY = MathUtils.clamp(bossBounds.y, arena.y + 8f, Math.max(arena.y + 8f, maxY));

            knightModel.respawnAt(teleportX, teleportY);
            lastSafeRespawnPosition.set(teleportX, teleportY + SPIKE_RESPAWN_OFFSET_Y);
            lockedBoss = target;
            activeBossArena = arena;
            bossArenaEngaged = true;
            noclipMode = false;
            refreshKnightControllerEnabled();
            showCheatMessage("Teleported to boss arena - exits sealed");
        }

    protected void toggleNoclipCheat() {
            if (knightModel == null) {
                return;
            }

            noclipMode = !noclipMode;
            knightModel.cancelHealing();
            knightModel.cancelSpellCast();
            knightModel.getVelocity().set(0f, 0f);
            knightModel.setCurrentState(Knight.State.IDLE);
            knightController.clearMovementState();
            refreshKnightControllerEnabled();
            showCheatMessage("Noclip: " + (noclipMode ? "ON" : "OFF"));
        }

    protected void updateNoclipMovement(float delta) {
            if (!noclipMode || knightModel == null) {
                return;
            }

            float moveX = 0f;
            float moveY = 0f;

            if (ControlBindings.isPressed(ControlBindings.Action.MOVE_LEFT)) moveX -= 1f;
            if (ControlBindings.isPressed(ControlBindings.Action.MOVE_RIGHT)) moveX += 1f;
            if (ControlBindings.isPressed(ControlBindings.Action.LOOK_DOWN)) moveY -= 1f;
            if (ControlBindings.isPressed(ControlBindings.Action.LOOK_UP)) moveY += 1f;

            if (moveX != 0f || moveY != 0f) {
                float length = (float) Math.sqrt(moveX * moveX + moveY * moveY);
                moveX /= length;
                moveY /= length;
                knightModel.getPosition().x += moveX * NOCLIP_SPEED * delta;
                knightModel.getPosition().y += moveY * NOCLIP_SPEED * delta;

                if (moveX < 0f) knightModel.setFacingLeft(true);
                if (moveX > 0f) knightModel.setFacingLeft(false);
            }

            Rectangle bounds = knightModel.getBounds();
            knightModel.getPosition().x = MathUtils.clamp(
                knightModel.getPosition().x,
                -bounds.width,
                Math.max(-bounds.width, mapPixelWidth)
            );
            knightModel.getPosition().y = MathUtils.clamp(
                knightModel.getPosition().y,
                -bounds.height,
                Math.max(-bounds.height, mapPixelHeight)
            );
            knightModel.getVelocity().set(0f, 0f);
            knightModel.setCurrentState(Knight.State.IDLE);
        }

    protected void emergencyHealCheat() {
            if (knightModel == null) {
                return;
            }

            if (knightModel.getCurrentHealth() <= 0 || deathRespawnPending) {
                deathRespawnPending = false;
                deathRespawnTimer = 0f;
                deathStateTime = 0f;
                showVengefulCastAnimation = false;
                knightModel.setCurrentHealth(1);
                Vector2 revive = lastSafeRespawnPosition != null ? lastSafeRespawnPosition : spawnPosition;
                knightModel.respawnAt(revive.x, revive.y);
                deathSfxPlayed = false;
                refreshKnightControllerEnabled();
                showCheatMessage("Emergency heal: revived with 1 mask");
                return;
            }

            if (knightModel.getCurrentHealth() >= knightModel.getMaxHealth()) {
                showCheatMessage("Emergency heal: health is already full");
                return;
            }

            knightModel.setCurrentHealth(knightModel.getCurrentHealth() + 1);
            showCheatMessage("Emergency heal: +1 mask");
        }

    protected void refillSoulCheat() {
            if (knightModel == null) {
                return;
            }

            knightModel.setCurrentSoul(knightModel.getMaxSoul());
            showCheatMessage("Soul refilled");
        }

    protected void instaKillVisibleEnemiesCheat() {
            if (enemies == null || camera == null) {
                showCheatMessage("No enemies to defeat");
                return;
            }

            Rectangle viewBounds = new Rectangle(
                camera.position.x - camera.viewportWidth / 2f,
                camera.position.y - camera.viewportHeight / 2f,
                camera.viewportWidth,
                camera.viewportHeight
            );
            int count = 0;

            for (Damageable enemy : enemies) {
                if (enemy != null && enemy.isAlive() && viewBounds.overlaps(enemy.getBounds())) {
                    applyDamageToEnemy(enemy, 9999, 0f, 0f);
                    count++;
                }
            }

            showCheatMessage("Insta-kill: defeated " + count + " visible enem" + (count == 1 ? "y" : "ies"));
        }

    protected void handleDeathRespawn(float delta) {
            if (!deathRespawnPending && knightModel.getCurrentHealth() <= 0) {
                deathCount++;
                deathRespawnPending = true;
                deathRespawnTimer = DEATH_RESPAWN_DELAY;
                deathStateTime = 0f;
                showVengefulCastAnimation = false;
                stopFootsteps();
                closeZoteDialogue();
                resetFalseKnightCombatAfterPlayerDeath();

                refreshKnightControllerEnabled();
            }

            if (!deathRespawnPending) {
                return;
            }

            deathRespawnTimer -= delta;

            if (deathRespawnTimer > 0f) {
                return;
            }

            knightModel.resetForNewRun(spawnPosition.x, spawnPosition.y);
            lastSafeRespawnPosition.set(spawnPosition.x, spawnPosition.y + SPIKE_RESPAWN_OFFSET_Y);
            spikeDamageCooldown = 0f;
            deathRespawnPending = false;
            deathSfxPlayed = false;
            stateTime = 0f;
            attackStateTime = 0f;
            healStateTime = 0f;
            lookUpStateTime = 0f;
            doubleJumpStateTime = 0f;
            doubleJumpVisualActive = false;
            wasDoubleJumping = false;
            dashStateTime = 0f;
            deathStateTime = 0f;
            fireballCastStateTime = 0f;
            showVengefulCastAnimation = false;

            refreshKnightControllerEnabled();
        }

    protected void beginVictorySequence(FalseKnight boss) {
            if (victoryPending || endScreenTransitionScheduled) {
                return;
            }

            victoryPending = true;
            victoryBoss = boss;
            finalRunTimeSeconds = runElapsedSeconds;
            victoryDelayTimer = 0f;
            knightController.clearMovementState();
            refreshKnightControllerEnabled();
        }

    protected void updateVictoryTransition(float delta) {
            if (!victoryPending || endScreenTransitionScheduled || victoryBoss == null) {
                return;
            }

            if (victoryBoss.getState() != FalseKnight.State.DEAD) {
                return;
            }

            victoryDelayTimer += Math.max(0f, delta);
            if (victoryDelayTimer < END_GAME_DELAY_AFTER_BOSS_DEATH) {
                return;
            }

            endScreenTransitionScheduled = true;
            stopFootsteps();
            stopFallingSfx();
            refreshKnightControllerEnabled();

            Gdx.app.postRunnable(() -> {
                game.setScreen(new EndGameScreen(
                    game,
                    saveSlot,
                    finalRunTimeSeconds,
                    deathCount,
                    enemyKillCount
                ));
                dispose();
            });
        }
}
