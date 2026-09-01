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

abstract class EnemyController extends EffectController {
    protected EnemyController(Game game, int saveSlot, boolean loadSavedGame) {
        super(game, saveSlot, loadSavedGame);
    }

    protected void updateEnemies(float delta) {
            if (crystalCrawlers != null) {
                for (int i = crystalCrawlers.size - 1; i >= 0; i--) {
                    CrystalCrawler crawler = crystalCrawlers.get(i);

                    if (crawler == null) {
                        crystalCrawlers.removeIndex(i);
                        continue;
                    }

                    if (isInEnemySimulationRange(crawler.getBounds()) || !crawler.isAlive()) {
                        crawler.update(delta, nearbyPlatforms, nearbyPolygonPlatforms);
                    }
                    crawler.respawnIfDeadAndFar(knightModel == null ? null : knightModel.getBounds(), ENEMY_RESPAWN_PLAYER_DISTANCE);

                    if (crawler.isReadyToRemove()) {
                        enemies.removeValue(crawler, true);
                        crystalCrawlers.removeIndex(i);
                    }
                }
            }

            if (crystallizedEnemies != null) {
                Rectangle knightBounds = knightModel == null ? null : knightModel.getBounds();

                for (int i = crystallizedEnemies.size - 1; i >= 0; i--) {
                    Crystallized crystallized = crystallizedEnemies.get(i);

                    if (crystallized == null) {
                        crystallizedEnemies.removeIndex(i);
                        continue;
                    }

                    if (isInEnemySimulationRange(crystallized.getBounds()) || !crystallized.isAlive()) {
                        crystallized.update(delta, knightBounds, nearbyPlatforms, nearbyPolygonPlatforms);
                    }
                    crystallized.respawnIfDeadAndFar(knightBounds, ENEMY_RESPAWN_PLAYER_DISTANCE);

                    if (crystallized.isReadyToRemove()) {
                        enemies.removeValue(crystallized, true);
                        crystallizedEnemies.removeIndex(i);
                    }
                }
            }

            if (falseKnights != null) {
                Rectangle knightBounds = knightModel == null ? null : knightModel.getBounds();

                for (int i = falseKnights.size - 1; i >= 0; i--) {
                    FalseKnight falseKnight = falseKnights.get(i);

                    if (falseKnight == null) {
                        falseKnights.removeIndex(i);
                        continue;
                    }

                    boolean activeBoss =
                        (bossArenaEngaged && falseKnight == lockedBoss) ||
                            isInEnemySimulationRange(falseKnight.getBodyBounds());

                    if (activeBoss) {

                        falseKnight.update(delta, knightBounds, platforms, polygonPlatforms);
                        processFalseKnightEvents(falseKnight);
                    }

                    if (falseKnight.isReadyToRemove()) {
                        enemies.removeValue(falseKnight, true);
                        falseKnights.removeIndex(i);
                    }
                }
            }

            if (crawlids != null) {
                for (int i = crawlids.size - 1; i >= 0; i--) {
                    Crawlid crawlid = crawlids.get(i);

                    if (crawlid == null) {
                        crawlids.removeIndex(i);
                        continue;
                    }

                    if (isInEnemySimulationRange(crawlid.getBounds()) || !crawlid.isAlive()) {
                        crawlid.update(delta, nearbyPlatforms, nearbyPolygonPlatforms);
                    }
                    crawlid.respawnIfDeadAndFar(knightModel == null ? null : knightModel.getBounds(), ENEMY_RESPAWN_PLAYER_DISTANCE);

                    if (crawlid.isReadyToRemove()) {
                        enemies.removeValue(crawlid, true);
                        crawlids.removeIndex(i);
                    }
                }
            }

            if (huskHornheads != null) {
                Rectangle knightBounds = knightModel == null ? null : knightModel.getBounds();

                for (int i = huskHornheads.size - 1; i >= 0; i--) {
                    HuskHornhead huskHornhead = huskHornheads.get(i);

                    if (huskHornhead == null) {
                        huskHornheads.removeIndex(i);
                        continue;
                    }

                    if (isInEnemySimulationRange(huskHornhead.getBounds()) || !huskHornhead.isAlive()) {
                        huskHornhead.update(delta, knightBounds, nearbyPlatforms, nearbyPolygonPlatforms);
                    }
                    huskHornhead.respawnIfDeadAndFar(knightBounds, ENEMY_RESPAWN_PLAYER_DISTANCE);

                    if (huskHornhead.isReadyToRemove()) {
                        enemies.removeValue(huskHornhead, true);
                        huskHornheads.removeIndex(i);
                    }
                }
            }

            if (mosquitoes != null) {
                Rectangle knightBounds = knightModel == null ? null : knightModel.getBounds();

                for (int i = mosquitoes.size - 1; i >= 0; i--) {
                    Mosquito mosquito = mosquitoes.get(i);

                    if (mosquito == null) {
                        mosquitoes.removeIndex(i);
                        continue;
                    }

                    if (isInEnemySimulationRange(mosquito.getBounds()) || !mosquito.isAlive()) {
                        mosquito.update(delta, knightBounds, nearbyPlatforms, nearbyPolygonPlatforms);
                    }
                    mosquito.respawnIfDeadAndFar(knightBounds, ENEMY_RESPAWN_PLAYER_DISTANCE);

                    if (mosquito.isReadyToRemove()) {
                        enemies.removeValue(mosquito, true);
                        mosquitoes.removeIndex(i);
                    }
                }
            }
        }

    protected void updateZotes(float delta) {
            if (zotes == null) {
                return;
            }

            Rectangle knightBounds = knightModel == null ? null : knightModel.getBounds();

            for (Zote zote : zotes) {
                if (zote != null && isInEnemySimulationRange(zote.getBounds())) {
                    zote.update(delta, knightBounds, nearbyPlatforms, nearbyPolygonPlatforms);
                }
            }
        }

    protected void updateZoteDialogue(float delta) {
            if (zoteDialogueBox != null) {
                zoteDialogueBox.update(delta);
            }

            updateZotePrompt();
        }

    protected void updateZotePrompt() {
            if (zoteDialogueBox == null) {
                return;
            }

            boolean visible = !paused && !deathRespawnPending && !isZoteDialogueActive() && getNearestInteractiveZote() != null;
            zoteDialogueBox.setPromptVisible(visible);
        }

    protected boolean handleZoteKeyDown(int keycode) {
            if (isZoteDialogueActive()) {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.E || keycode == Input.Keys.SPACE) {
                    boolean stillActive = zoteDialogueBox.advance();

                    if (stillActive) {
                        playRandomZoteVoice();
                    } else {
                        finishZoteDialogue();
                    }

                    return true;
                }

                if (keycode == Input.Keys.ESCAPE) {
                    closeZoteDialogue();
                    return true;
                }

                return true;
            }

            if (keycode == Input.Keys.E) {
                Zote zote = getNearestInteractiveZote();

                if (zote != null) {
                    beginZoteDialogue(zote);
                    return true;
                }
            }

            return false;
        }

    protected Zote getNearestInteractiveZote() {
            if (zotes == null || knightModel == null) {
                return null;
            }

            Rectangle knightBounds = knightModel.getBounds();
            Zote nearest = null;
            float bestDistance = Float.MAX_VALUE;
            float knightCenterX = knightBounds.x + knightBounds.width / 2f;
            float knightCenterY = knightBounds.y + knightBounds.height / 2f;

            for (Zote zote : zotes) {
                if (zote == null || !zote.isKnightInInteractionRange(knightBounds)) {
                    continue;
                }

                Rectangle bounds = zote.getBounds();
                float centerX = bounds.x + bounds.width / 2f;
                float centerY = bounds.y + bounds.height / 2f;
                float dx = centerX - knightCenterX;
                float dy = centerY - knightCenterY;
                float distance = dx * dx + dy * dy;

                if (distance < bestDistance) {
                    bestDistance = distance;
                    nearest = zote;
                }
            }

            return nearest;
        }

    protected void beginZoteDialogue(Zote zote) {
            if (zote == null || zoteDialogueBox == null || knightModel == null) {
                return;
            }

            activeDialogueZote = zote;
            zote.startTalking(knightModel.getBounds());
            zoteDialogueBox.begin(zote.getNextDialogue());
            playRandomZoteVoice();
            stopFootsteps();
            stopFallingSfx();

            refreshKnightControllerEnabled();
        }

    protected void finishZoteDialogue() {
            if (activeDialogueZote != null) {
                activeDialogueZote.stopTalking();
            }

            activeDialogueZote = null;

            refreshKnightControllerEnabled();
        }

    protected void closeZoteDialogue() {
            if (zoteDialogueBox != null) {
                zoteDialogueBox.close();
            }

            finishZoteDialogue();
        }

    protected boolean isZoteDialogueActive() {
            return zoteDialogueBox != null && zoteDialogueBox.isActive();
        }

    protected boolean handleZoteHit(Rectangle attackRect) {
            if (zotes == null || attackRect == null || knightModel == null) {
                return false;
            }

            for (Zote zote : zotes) {
                if (zote == null || !attackRect.overlaps(zote.getBounds())) {
                    continue;
                }

                Rectangle knightBounds = knightModel.getBounds();
                Rectangle zoteBounds = zote.getBounds();
                float knightCenterX = knightBounds.x + knightBounds.width / 2f;
                float zoteCenterX = zoteBounds.x + zoteBounds.width / 2f;
                float knockbackX = knightCenterX < zoteCenterX ? ENEMY_HIT_KNOCKBACK_X : -ENEMY_HIT_KNOCKBACK_X;
                float effectX = zoteBounds.x + zoteBounds.width / 2f;
                float effectY = zoteBounds.y + zoteBounds.height / 2f;

                if (activeDialogueZote == zote) {
                    closeZoteDialogue();
                }

                zote.takeNailHit(knightBounds, knockbackX, ENEMY_HIT_KNOCKBACK_Y);
                playRandomZoteVoice();

                if (combatEffectManager != null) {
                    combatEffectManager.spawnEnemyHit(effectX, effectY);
                }

                if (knightModel.isPogoAttack()) {
                    knightModel.pogoBounce();
                } else {
                    knightModel.markAttackHit();
                }

                return true;
            }

            return false;
        }

    protected void drawZotes() {
            if (zoteRenderer == null || zotes == null) {
                return;
            }

            for (Zote zote : zotes) {
                if (zote != null && isInCameraRenderRange(zote.getBounds(), ENEMY_RENDER_MARGIN)) {
                    zoteRenderer.draw(batch, zote);
                }
            }
        }

    protected void drawCrystalCrawlers() {
            if (crystalCrawlerRenderer == null || crystalCrawlers == null) {
                return;
            }

            for (CrystalCrawler crawler : crystalCrawlers) {
                if (crawler != null && isInCameraRenderRange(crawler.getBounds(), ENEMY_RENDER_MARGIN)) {
                    crystalCrawlerRenderer.draw(batch, crawler);
                }
            }
        }

    protected void drawCrystallizedEnemies() {
            if (crystallizedRenderer == null || crystallizedEnemies == null) {
                return;
            }

            for (Crystallized crystallized : crystallizedEnemies) {
                if (crystallized != null &&
                    isInCameraRenderRange(crystallized.getBounds(), ENEMY_RENDER_MARGIN + 700f)) {
                    crystallizedRenderer.draw(batch, crystallized);
                }
            }
        }

    protected void drawFalseKnights() {
            if (falseKnightRenderer == null || falseKnights == null) {
                return;
            }

            for (FalseKnight falseKnight : falseKnights) {
                if (falseKnight != null &&
                    isInCameraRenderRange(falseKnight.getBodyBounds(), ENEMY_RENDER_MARGIN + 500f)) {
                    falseKnightRenderer.draw(batch, falseKnight);
                }
            }
        }

    protected void drawCrawlids() {
            if (crawlidRenderer == null || crawlids == null) {
                return;
            }

            for (Crawlid crawlid : crawlids) {
                if (crawlid != null && isInCameraRenderRange(crawlid.getBounds(), ENEMY_RENDER_MARGIN)) {
                    crawlidRenderer.draw(batch, crawlid);
                }
            }
        }

    protected void drawHuskHornheads() {
            if (huskHornheadRenderer == null || huskHornheads == null) {
                return;
            }

            for (HuskHornhead huskHornhead : huskHornheads) {
                if (huskHornhead != null &&
                    isInCameraRenderRange(huskHornhead.getBounds(), ENEMY_RENDER_MARGIN)) {
                    huskHornheadRenderer.draw(batch, huskHornhead);
                }
            }
        }

    protected void drawMosquitoes() {
            if (mosquitoRenderer == null || mosquitoes == null) {
                return;
            }

            for (Mosquito mosquito : mosquitoes) {
                if (mosquito != null &&
                    isInCameraRenderRange(mosquito.getBounds(), ENEMY_RENDER_MARGIN)) {
                    mosquitoRenderer.draw(batch, mosquito);
                }
            }
        }

    protected Rectangle getEnemyDamageBounds(Damageable enemy) {
            if (enemy instanceof HuskHornhead) {
                return ((HuskHornhead) enemy).getDamageBounds();
            }

            if (enemy instanceof Crystallized) {
                return ((Crystallized) enemy).getDamageBounds();
            }

            if (enemy instanceof FalseKnight) {
                return ((FalseKnight) enemy).getDamageBounds();
            }

            return enemy.getBounds();
        }

    protected void handleEnemyCollision() {
            if (enemies == null || knightModel == null) {
                return;
            }

            Rectangle knightBounds = knightModel.getBounds();

            for (Damageable enemy : enemies) {
                if (enemy == null || !enemy.isAlive()) {
                    continue;
                }

                if (!isInEnemySimulationRange(enemy.getBounds())) {
                    continue;
                }

                Rectangle enemyBounds = getEnemyDamageBounds(enemy);
                boolean hitKnight;

                if (enemy instanceof Crystallized) {
                    hitKnight = ((Crystallized) enemy).canDamageKnight(knightBounds);
                } else if (enemy instanceof FalseKnight) {
                    hitKnight = ((FalseKnight) enemy).canDamageKnight(knightBounds);
                } else {
                    hitKnight = knightBounds.overlaps(enemyBounds);
                }

                if (!hitKnight) {
                    continue;
                }

                if (knightModel.isDashing() &&
                    isCharmEquipped(CharmId.SHARP_SHADOW) &&
                    knightBounds.overlaps(enemy.getBounds())) {
                    if (sharpShadowDamagedEnemies != null && sharpShadowDamagedEnemies.add(enemy)) {
                        float direction = knightModel.isFacingLeft() ? -1f : 1f;
                        applyDamageToEnemy(enemy, SHARP_SHADOW_DAMAGE, direction * 240f, 65f);
                        startScreenShake(7f, 0.10f);
                    }
                    continue;
                }

                if (enemyDamageCooldown > 0f || !knightModel.canTakeDamage()) {
                    return;
                }

                knightModel.takeDamage(ENEMY_TOUCH_DAMAGE);
                enemyDamageCooldown = ENEMY_DAMAGE_COOLDOWN_TIME;
                startScreenShake(DAMAGE_SCREEN_SHAKE_MAGNITUDE);

                if (knightModel.getCurrentHealth() > 0) {
                    float knightCenterX = knightBounds.x + knightBounds.width / 2f;
                    float enemyCenterX = enemyBounds.x + enemyBounds.width / 2f;
                    float knockbackX = knightCenterX < enemyCenterX ? -ENEMY_KNOCKBACK_X : ENEMY_KNOCKBACK_X;
                    knightModel.applyKnockback(knockbackX, ENEMY_KNOCKBACK_Y, PLAYER_KNOCKBACK_DURATION);
                }

                return;
            }
        }

    protected void applyDamageToEnemyWithKnockback(Damageable enemy) {
            Rectangle knightBounds = knightModel.getBounds();
            Rectangle enemyBounds = enemy.getBounds();
            float knightCenterX = knightBounds.x + knightBounds.width / 2f;
            float enemyCenterX = enemyBounds.x + enemyBounds.width / 2f;
            float knockbackMultiplier = isCharmEquipped(CharmId.HEAVY_BLOW) ? HEAVY_BLOW_MULTIPLIER : 1f;
            float knockbackX = (knightCenterX < enemyCenterX ? ENEMY_HIT_KNOCKBACK_X : -ENEMY_HIT_KNOCKBACK_X) * knockbackMultiplier;

            applyDamageToEnemy(
                enemy,
                knightModel.getAttackDamage(),
                knockbackX,
                ENEMY_HIT_KNOCKBACK_Y * knockbackMultiplier
            );
        }

    protected void applyDamageToEnemy(Damageable enemy, int damage, float knockbackX, float knockbackY) {
            if (enemy == null || damage <= 0 || !enemy.isAlive()) {
                return;
            }

            Rectangle enemyBounds = enemy.getBounds();
            float effectX = enemyBounds.x + enemyBounds.width / 2f;
            float effectY = enemyBounds.y + enemyBounds.height / 2f;
            boolean wasAlive = enemy.isAlive();

            if (enemy instanceof CrystalCrawler) {
                ((CrystalCrawler) enemy).takeDamage(damage, knockbackX, knockbackY);
            } else if (enemy instanceof Crystallized) {
                ((Crystallized) enemy).takeDamage(damage, knockbackX, knockbackY);
            } else if (enemy instanceof FalseKnight) {
                ((FalseKnight) enemy).takeDamage(damage, knockbackX, knockbackY);
            } else if (enemy instanceof Crawlid) {
                ((Crawlid) enemy).takeDamage(damage, knockbackX, knockbackY);
            } else if (enemy instanceof HuskHornhead) {
                ((HuskHornhead) enemy).takeDamage(damage, knockbackX, knockbackY);
            } else if (enemy instanceof Mosquito) {
                ((Mosquito) enemy).takeDamage(damage, knockbackX, knockbackY);
            } else {
                enemy.takeDamage(damage);
            }

            boolean killedNow = wasAlive && !enemy.isAlive();

            if (killedNow) {
                enemyKillCount++;
                AchievementManager achievements = AchievementManager.getInstance();
                achievements.recordEnemyKill(enemy.getClass().getSimpleName());

                if (enemy instanceof FalseKnight) {
                    achievements.recordFalseKnightDefeat(runElapsedSeconds);
                    beginVictorySequence((FalseKnight) enemy);
                }
            }

            if (combatEffectManager != null) {
                combatEffectManager.spawnEnemyHit(effectX, effectY);

                if (killedNow) {
                    combatEffectManager.spawnEnemyDeath(effectX, effectY);
                }
            }
        }

    protected void handleAttackHits() {
            if (!knightModel.isAttacking()) {
                return;
            }

            if (!knightModel.canAttackHit()) {
                return;
            }

            Rectangle attackRect = knightModel.getAttackBounds();

            if (handleZoteHit(attackRect)) {
                return;
            }

            if (enemies != null) {
                for (Damageable enemy : enemies) {
                    if (enemy != null && enemy.isAlive()) {
                        if (attackRect.overlaps(enemy.getBounds())) {
                            applyDamageToEnemyWithKnockback(enemy);
                            knightModel.addSoulOnHit();
                            playRandomSound(soulPickupSfx, SFX_SOUL_PICKUP_VOLUME);

                            if (knightModel.isPogoAttack()) {
                                knightModel.pogoBounce();
                            } else {
                                knightModel.markAttackHit();
                            }

                            return;
                        }
                    }
                }
            }

            if (handleSecretWallHit(attackRect)) {
                return;
            }

            if (handleNailTerrainHit(attackRect)) {
                return;
            }
        }

    protected boolean handleNailTerrainHit(Rectangle attackRect) {
            if (spikeRectangles != null) {
                for (Rectangle spike : spikeRectangles) {
                    if (attackRect.overlaps(spike)) {
                        spawnNailTerrainEffect(attackRect, spike);
                        finishNailTerrainHit();
                        return true;
                    }
                }
            }

            if (spikePolygons != null) {
                for (Polygon spike : spikePolygons) {
                    if (rectOverlapsPolygonForAttack(attackRect, spike)) {
                        spawnNailTerrainEffect(attackRect, spike);
                        finishNailTerrainHit();
                        return true;
                    }
                }
            }

            if (nearbyPlatforms != null) {
                for (Rectangle platform : nearbyPlatforms) {
                    if (attackRect.overlaps(platform)) {
                        spawnNailTerrainEffect(attackRect, platform);
                        finishNailTerrainHit();
                        return true;
                    }
                }
            }

            if (nearbyPolygonPlatforms != null) {
                for (Polygon polygon : nearbyPolygonPlatforms) {
                    if (rectOverlapsPolygonForAttack(attackRect, polygon)) {
                        spawnNailTerrainEffect(attackRect, polygon);
                        finishNailTerrainHit();
                        return true;
                    }
                }
            }

            return false;
        }

    protected void finishNailTerrainHit() {
            playSfx(terrainRejectSfx, SFX_TERRAIN_HIT_VOLUME);

            if (knightModel.isPogoAttack()) {
                knightModel.pogoBounce();
            } else {
                knightModel.markAttackHit();
            }
        }

    protected void spawnNailTerrainEffect(Rectangle attackRect, Rectangle targetRect) {
            if (combatEffectManager == null) {
                return;
            }

            Vector2 point = getIntersectionCenter(attackRect, targetRect);
            combatEffectManager.spawnNailTerrainHit(
                point.x,
                point.y,
                getNailTerrainEffectRotation(),
                knightModel.isFacingLeft()
            );
        }

    protected void spawnNailTerrainEffect(Rectangle attackRect, Polygon targetPolygon) {
            if (combatEffectManager == null || targetPolygon == null) {
                return;
            }

            Vector2 point = getIntersectionCenter(attackRect, targetPolygon.getBoundingRectangle());
            combatEffectManager.spawnNailTerrainHit(
                point.x,
                point.y,
                getNailTerrainEffectRotation(),
                knightModel.isFacingLeft()
            );
        }

    protected Vector2 getIntersectionCenter(Rectangle first, Rectangle second) {
            float minX = Math.max(first.x, second.x);
            float minY = Math.max(first.y, second.y);
            float maxX = Math.min(first.x + first.width, second.x + second.width);
            float maxY = Math.min(first.y + first.height, second.y + second.height);

            if (maxX >= minX && maxY >= minY) {
                return new Vector2((minX + maxX) / 2f, (minY + maxY) / 2f);
            }

            return new Vector2(first.x + first.width / 2f, first.y + first.height / 2f);
        }

    protected float getNailTerrainEffectRotation() {
            Knight.AttackDirection direction = knightModel.getCurrentAttackDirection();

            if (direction == Knight.AttackDirection.UP) {
                return 90f;
            }

            if (direction == Knight.AttackDirection.DOWN) {
                return -90f;
            }

            return knightModel.isFacingLeft() ? 180f : 0f;
        }
}
