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

abstract class RenderController extends AssetController {
    protected RenderController(Game game, int saveSlot, boolean loadSavedGame) {
        super(game, saveSlot, loadSavedGame);
    }

    public void render(float delta) {
            ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1);

            if (!paused && !deathRespawnPending && !victoryPending) {
                runElapsedSeconds += delta;
            }

            boolean zoteDialogueActive = isZoteDialogueActive();

            if (!paused && !deathRespawnPending) {
                refreshNearbyCollisionGeometry(false);
                updateEnemySimulationBounds();
                updateZoteDialogue(delta);
                updateZotes(delta);
            }

            if (!paused && !deathRespawnPending && !zoteDialogueActive) {
                if (noclipMode) {
                    updateNoclipMovement(delta);
                } else {
                    knightModel.update(delta, nearbyPlatforms, nearbyPolygonPlatforms);
                    knightController.refreshHeldMovement();
                }

                if (spikeDamageCooldown > 0f) {
                    spikeDamageCooldown -= delta;
                }

                if (enemyDamageCooldown > 0f) {
                    enemyDamageCooldown -= delta;
                }

                updateCharmPickups(delta);
                updateEnemies(delta);
                updateSpells(delta);

                if (!noclipMode) {
                    handleAttackHits();

                    if (!godMode) {
                        handleEnemyCollision();
                        handleSpikeCollision();
                    }

                    updateLastSafeRespawnPosition();
                    updatePlayerSfx(delta);
                } else {
                    stopFootsteps();
                    stopFallingSfx();
                }

                updateFalseKnightArenaState(knightModel.getBounds());

                if (isFalseKnightArenaLocked() && !noclipMode) {
                    constrainPlayerToBossArena();
                } else if (!noclipMode) {
                    handleDoorTeleport(delta);
                }
            }

            updateCheatStatus(delta);
            handleDeathRespawn(delta);
            updateVictoryTransition(delta);

            if (deathRespawnPending && !paused) {
                deathStateTime += delta;
            }

            float playerX = knightModel.getPosition().x;
            float playerY = knightModel.getPosition().y;

            updateRoomForCamera(playerX, playerY);

            Rectangle playerBounds = knightModel.getBounds();
            updateFalseKnightArenaState(playerBounds);
            updateMusicZone(playerX, playerY);

            updateBackgroundZone(
                playerBounds.x + playerBounds.width / 2f,
                playerBounds.y + playerBounds.height / 2f
            );

            updateMusicFade(delta);

            boolean isCurrentlyDashing = knightModel.isDashing();
            boolean isCurrentlyAttacking = knightModel.isAttacking();
            boolean isCurrentlyHealing = knightModel.isHealing();
            boolean isCurrentlyCasting = knightModel.isCasting();
            boolean isCurrentlyLookingUp =
                knightModel.isLookingUp() &&
                    knightModel.isGrounded() &&
                    Math.abs(knightModel.getVelocity().x) < 0.1f;

            boolean isCurrentlyDoubleJumping =
                knightModel.getCurrentState() == Knight.State.DOUBLE_JUMPING &&
                    !knightModel.isGrounded();

            if (!paused && !deathRespawnPending && isCurrentlyDashing && !wasDashing) {
                dashStateTime = 0f;
                if (sharpShadowDamagedEnemies != null) {
                    sharpShadowDamagedEnemies.clear();
                }
                playSfx(dashSfx, SFX_DASH_VOLUME);

                if (dashEffectAnimation != null) {
                    dashEffects.add(new DashEffectAnim(knightModel.isFacingLeft()));
                }
            }

            if (!paused && !deathRespawnPending && isCurrentlyDashing) {
                dashStateTime += delta;
            }

            wasDashing = isCurrentlyDashing;

            if (showVengefulCastAnimation && isCurrentlyCasting && !paused && !deathRespawnPending) {
                fireballCastStateTime += delta;
            }

            if (!isCurrentlyCasting) {
                showVengefulCastAnimation = false;
            }

            if (isCurrentlyAttacking && !wasAttacking) {
                attackStateTime = 0f;
                spawnSlashEffect();
                playSfx(attackSfx, SFX_ATTACK_VOLUME);
            }

            if (isCurrentlyAttacking && !paused && !deathRespawnPending) {
                attackStateTime += delta;
            }

            wasAttacking = isCurrentlyAttacking;

            if (isCurrentlyHealing && !wasHealing) {
                healStateTime = 0f;
            }

            if (isCurrentlyHealing && !paused && !deathRespawnPending) {
                healStateTime += delta;
            }

            wasHealing = isCurrentlyHealing;

            if (isCurrentlyLookingUp && !wasLookingUp) {
                lookUpStateTime = 0f;
            }

            if (isCurrentlyLookingUp && !paused && !deathRespawnPending) {
                lookUpStateTime += delta;
            }

            wasLookingUp = isCurrentlyLookingUp;

            if (!paused && !deathRespawnPending && isCurrentlyDoubleJumping && !wasDoubleJumping) {
                doubleJumpStateTime = 0f;
                doubleJumpVisualActive = true;
                playSfx(doubleJumpSfx, SFX_DOUBLE_JUMP_VOLUME);
            }

            if (!paused && !deathRespawnPending && doubleJumpVisualActive) {
                doubleJumpStateTime += delta;

                if (doubleJumpAnimation == null || doubleJumpAnimation.isAnimationFinished(doubleJumpStateTime)) {
                    doubleJumpVisualActive = false;
                }
            }

            if (knightModel.isGrounded() || isCurrentlyDashing || isCurrentlyAttacking ||
                isCurrentlyHealing || isCurrentlyCasting || deathRespawnPending) {
                doubleJumpVisualActive = false;
            }

            wasDoubleJumping = isCurrentlyDoubleJumping;

            TextureRegion baseFrame;

            if (!paused && !deathRespawnPending) {
                stateTime += delta;
            }

            if (deathRespawnPending && deathAnimation != null) {
                baseFrame = deathAnimation.getKeyFrame(deathStateTime, false);
            } else if (showVengefulCastAnimation && isCurrentlyCasting && fireballCastAnimation != null) {
                baseFrame = fireballCastAnimation.getKeyFrame(fireballCastStateTime, false);
            } else if (isCurrentlyDashing) {
                baseFrame = dashAnimation.getKeyFrame(dashStateTime, false);
            } else if (isCurrentlyHealing) {
                baseFrame = healAnimation.getKeyFrame(healStateTime);
            } else if (isCurrentlyAttacking) {
                if (knightModel.getCurrentAttackDirection() == Knight.AttackDirection.UP) {
                    baseFrame = attackUpAnimation.getKeyFrame(attackStateTime);
                } else if (knightModel.getCurrentAttackDirection() == Knight.AttackDirection.DOWN) {
                    baseFrame = pogoAttackAnimation.getKeyFrame(attackStateTime);
                } else {
                    baseFrame = attackAnimation.getKeyFrame(attackStateTime);
                }
            } else if (!knightModel.isGrounded()) {
                if (doubleJumpVisualActive && doubleJumpAnimation != null) {
                    baseFrame = doubleJumpAnimation.getKeyFrame(doubleJumpStateTime, false);
                } else if (knightModel.getVelocity().y > 0) {
                    baseFrame = jumpAnimation.getKeyFrame(stateTime);
                } else {
                    baseFrame = fallAnimation.getKeyFrame(stateTime);
                }
            } else if (Math.abs(knightModel.getVelocity().x) > 0.1f) {
                baseFrame = runAnimation.getKeyFrame(stateTime);
            } else if (isCurrentlyLookingUp) {
                baseFrame = lookUpAnimation.getKeyFrame(lookUpStateTime);
            } else {
                baseFrame = idleAnimation.getKeyFrame(stateTime);
            }

            TextureRegion currentFrame = baseFrame;
            boolean flipKnightFrame = !knightModel.isFacingLeft();

            float targetX = playerX + (currentFrame.getRegionWidth() / 2f);
            float targetY = playerY + 50f;

            float camHalfWidth = camera.viewportWidth / 2f;
            float camHalfHeight = camera.viewportHeight / 2f;

            float clampedX;
            float clampedY;

            Rectangle cameraBounds = isFalseKnightArenaLocked() ? activeBossArena : currentRoom;

            if (cameraBounds != null) {
                clampedX = clampCameraAxis(targetX, cameraBounds.x, cameraBounds.width, camHalfWidth);
                clampedY = clampCameraAxis(targetY, cameraBounds.y, cameraBounds.height, camHalfHeight);
            } else {
                clampedX = clampCameraAxis(targetX, 0f, mapPixelWidth, camHalfWidth);
                clampedY = clampCameraAxis(targetY, 0f, mapPixelHeight, camHalfHeight);
            }

            camera.position.x = Math.round(clampedX);
            camera.position.y = Math.round(clampedY);

            if (!paused && !deathRespawnPending && screenShakeTimer > 0f) {
                float shakeProgress = screenShakeDuration <= 0f ? 0f : screenShakeTimer / screenShakeDuration;
                float shakeAmount = screenShakeMagnitude * MathUtils.clamp(shakeProgress, 0f, 1f);

                camera.position.x += MathUtils.random(-shakeAmount, shakeAmount);
                camera.position.y += MathUtils.random(-shakeAmount, shakeAmount);

                screenShakeTimer = Math.max(0f, screenShakeTimer - delta);
            }

            camera.update();

            setMapRendererView(NORMAL_TILE_CULL_MARGIN);
            batch.setProjectionMatrix(camera.combined);

            drawMapBackground();

            renderLayerRuns(deepestBackgroundRenderRuns);

            batch.begin();
            if (particleManager != null) {
                particleManager.draw(batch, delta, camera, currentRoom, paused || deathRespawnPending);
            }

            drawCharmPickups(true);
            batch.end();

            renderLayerRuns(backgroundRenderRuns);

            batch.begin();

            float spriteWidth = currentFrame.getRegionWidth();
            float hitboxWidth = 50f;
            float offsetX = (spriteWidth - hitboxWidth) / 2f;
            float offsetY = 15f;

            drawDashEffects(delta, playerX, playerY, hitboxWidth);
            drawCharmPickups(false);
            drawCrystalCrawlers();
            drawCrystallizedEnemies();
            drawFalseKnights();
            drawCrawlids();
            drawHuskHornheads();
            drawMosquitoes();
            drawZotes();
            drawSpellEffects();

            if (knightModel == null || knightModel.shouldDrawDuringDamageBlink()) {
                boolean sharpShadowVisual = isCurrentlyDashing && isCharmEquipped(CharmId.SHARP_SHADOW);
                boolean voidHeartCastVisual = isCurrentlyCasting && isCharmEquipped(CharmId.VOID_HEART);
                if (sharpShadowVisual || voidHeartCastVisual) {
                    batch.setColor(0.30f, 0.18f, 0.55f, 1f);
                }
                drawTextureRegion(
                    currentFrame,
                    Math.round(playerX - offsetX),
                    Math.round(playerY - offsetY),
                    currentFrame.getRegionWidth(),
                    currentFrame.getRegionHeight(),
                    flipKnightFrame
                );
                if (sharpShadowVisual || voidHeartCastVisual) {
                    batch.setColor(Color.WHITE);
                }
            }

            drawHealingEffect(delta, playerX, playerY, currentFrame, offsetX, offsetY, isCurrentlyHealing);

            batch.end();

            renderLayerRuns(foregroundRenderRuns);

            batch.begin();
            drawSlashEffects(delta);
            drawCombatEffects(paused || deathRespawnPending ? 0f : delta);
            batch.end();

            if (showDebugHitboxes) {
                drawDebugHitboxes();
            }

            if (gameHud != null) {
                gameHud.update(delta);
                gameHud.draw();
            }

            if (hudStage != null) {
                hudStage.act(delta);
                drawUiOverlays(delta);
                hudStage.draw();
            }
        }

    protected void drawDebugHitboxes() {
            if (debugShapeRenderer == null || camera == null || knightModel == null) {
                return;
            }

            debugShapeRenderer.setProjectionMatrix(camera.combined);
            debugShapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            debugShapeRenderer.setColor(Color.CYAN);
            drawDebugRectangle(knightModel.getBounds());

            if (knightModel.isAttacking()) {
                debugShapeRenderer.setColor(Color.YELLOW);
                drawDebugRectangle(knightModel.getAttackBounds());
            }

            debugShapeRenderer.setColor(Color.GREEN);
            if (nearbyPlatforms != null) {
                for (Rectangle platform : nearbyPlatforms) {
                    if (isInCameraRenderRange(platform, 80f)) {
                        drawDebugRectangle(platform);
                    }
                }
            }

            if (nearbyPolygonPlatforms != null) {
                for (Polygon platform : nearbyPolygonPlatforms) {
                    Rectangle bounds = platform.getBoundingRectangle();
                    if (isInCameraRenderRange(bounds, 80f)) {
                        drawDebugPolygon(platform);
                    }
                }
            }

            debugShapeRenderer.setColor(Color.RED);
            if (spikeRectangles != null) {
                for (Rectangle spike : spikeRectangles) {
                    drawDebugRectangle(spike);
                }
            }

            if (spikePolygons != null) {
                for (Polygon spike : spikePolygons) {
                    drawDebugPolygon(spike);
                }
            }

            debugShapeRenderer.setColor(Color.MAGENTA);
            if (secretWalls != null) {
                for (SecretWallHitbox secretWall : secretWalls) {
                    if (secretWall == null || secretWall.destroyed) {
                        continue;
                    }

                    if (secretWall.rectangle != null) {
                        drawDebugRectangle(secretWall.rectangle);
                    } else if (secretWall.polygon != null) {
                        drawDebugPolygon(secretWall.polygon);
                    }
                }
            }

            debugShapeRenderer.setColor(Color.BLUE);
            if (vengefulProjectiles != null) {
                for (VengefulSpiritProjectile projectile : vengefulProjectiles) {
                    if (projectile != null && projectile.isActive()) {
                        drawDebugRectangle(projectile.getBounds());
                    }
                }
            }

            debugShapeRenderer.setColor(Color.SKY);
            if (howlingWraithCasts != null) {
                for (HowlingWraithsCast cast : howlingWraithCasts) {
                    if (cast != null && cast.isActive()) {
                        drawDebugRectangle(cast.getBounds());
                    }
                }
            }

            debugShapeRenderer.setColor(Color.GOLD);
            if (charmPickups != null) {
                for (CharmPickup pickup : charmPickups) {
                    if (pickup != null && !pickup.isCollected()) {
                        drawDebugRectangle(pickup.getBounds());
                    }
                }
            }

            debugShapeRenderer.setColor(Color.ORANGE);
            if (enemies != null) {
                for (Damageable enemy : enemies) {
                    if (enemy == null) {
                        continue;
                    }

                    if (enemy instanceof FalseKnight) {
                        FalseKnight falseKnight = (FalseKnight) enemy;
                        drawDebugRectangle(falseKnight.getBodyBounds());

                        debugShapeRenderer.setColor(Color.RED);
                        drawDebugRectangle(falseKnight.getDamageBounds());

                        if (falseKnight.hasActiveMaceDamage()) {
                            debugShapeRenderer.setColor(Color.YELLOW);
                            drawDebugRectangle(falseKnight.getMaceDamageBounds());
                        }

                        debugShapeRenderer.setColor(Color.PURPLE);
                        drawDebugRectangle(falseKnight.getVulnerableBounds());

                        debugShapeRenderer.setColor(Color.SKY);
                        for (FalseKnight.Shockwave shockwave : falseKnight.getShockwaves()) {
                            if (shockwave != null && shockwave.isAlive()) {
                                drawDebugRectangle(shockwave.getBounds());
                            }
                        }

                        debugShapeRenderer.setColor(Color.ORANGE);
                    } else if (enemy.isAlive()) {
                        drawDebugRectangle(enemy.getBounds());
                    }
                }
            }

            debugShapeRenderer.end();
        }

    protected void drawDebugRectangle(Rectangle rectangle) {
            if (rectangle == null) {
                return;
            }

            debugShapeRenderer.rect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        }

    protected void drawDebugPolygon(Polygon polygon) {
            if (polygon == null) {
                return;
            }

            float[] vertices = polygon.getTransformedVertices();

            if (vertices != null && vertices.length >= 6) {
                debugShapeRenderer.polygon(vertices);
            }
        }

    protected void drawHealingEffect(float delta, float playerX, float playerY, TextureRegion currentFrame, float offsetX, float offsetY, boolean isCurrentlyHealing) {
            if (!isCurrentlyHealing || healAuraAnimation == null) {
                return;
            }

            TextureRegion auraFrame = healAuraAnimation.getKeyFrame(healStateTime);
            float progress = knightModel.getHealProgress();
            float scale = 1.05f + progress * 0.18f;
            float width = auraFrame.getRegionWidth() * scale;
            float height = auraFrame.getRegionHeight() * scale;
            float centerX = playerX - offsetX + currentFrame.getRegionWidth() / 2f;
            float centerY = playerY - offsetY + currentFrame.getRegionHeight() / 2f;
            float x = Math.round(centerX - width / 2f);
            float y = Math.round(centerY - height / 2f);

            batch.draw(auraFrame, x, y, width, height);
        }

    protected void drawUiOverlays(float delta) {
            float overlayAlpha = 0f;

            float deathFade = 0f;
            if (deathRespawnPending) {
                deathFade = MathUtils.clamp(
                    (deathStateTime - DEATH_OVERLAY_START)
                        / Math.max(0.01f, DEATH_RESPAWN_DELAY - DEATH_OVERLAY_START),
                    0f,
                    1f
                );
            }

            if (deathLabel != null) {
                deathLabel.setVisible(deathRespawnPending && deathFade > 0.08f);
                deathLabel.setColor(1f, 1f, 1f, deathFade);
            }

            if (paused) {
                overlayAlpha = Math.max(overlayAlpha, PAUSE_OVERLAY_ALPHA);
            }

            if (deathRespawnPending) {
                overlayAlpha = Math.max(overlayAlpha, 0.92f * deathFade);
            }

            if (deathRespawnPending) {
                hazardFadeTimer = 0f;

                if (hazardLabel != null) {
                    hazardLabel.setVisible(false);
                }
            } else if (hazardFadeTimer > 0f) {
                float elapsed = HAZARD_FADE_TIME - hazardFadeTimer;
                float hazardAlpha;

                if (elapsed < HAZARD_FADE_IN_TIME) {
                    hazardAlpha = HAZARD_MAX_ALPHA * MathUtils.clamp(elapsed / HAZARD_FADE_IN_TIME, 0f, 1f);
                } else if (hazardFadeTimer < HAZARD_FADE_OUT_TIME) {
                    hazardAlpha = HAZARD_MAX_ALPHA * MathUtils.clamp(hazardFadeTimer / HAZARD_FADE_OUT_TIME, 0f, 1f);
                } else {
                    hazardAlpha = HAZARD_MAX_ALPHA;
                }

                overlayAlpha = Math.max(overlayAlpha, hazardAlpha);

                if (hazardLabel != null) {
                    hazardLabel.setVisible(true);
                    hazardLabel.setColor(1f, 1f, 1f, MathUtils.clamp(hazardAlpha / HAZARD_MAX_ALPHA, 0f, 1f));
                }

                hazardFadeTimer -= delta;

                if (hazardFadeTimer <= 0f && hazardLabel != null) {
                    hazardLabel.setVisible(false);
                }
            } else if (hazardLabel != null) {
                hazardLabel.setVisible(false);
            }

            overlayAlpha = Math.max(overlayAlpha, GameSettings.getWorldDarknessAlpha());

            if (overlayAlpha <= 0f || blackPixel == null) {
                return;
            }

            batch.setProjectionMatrix(hudStage.getCamera().combined);
            batch.begin();
            batch.setColor(0f, 0f, 0f, overlayAlpha);
            batch.draw(blackPixel, 0f, 0f, hudStage.getViewport().getWorldWidth(), hudStage.getViewport().getWorldHeight());
            batch.setColor(1f, 1f, 1f, 1f);
            batch.end();
        }
}
