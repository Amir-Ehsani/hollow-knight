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

abstract class EffectController extends WorldController {
    protected EffectController(Game game, int saveSlot, boolean loadSavedGame) {
        super(game, saveSlot, loadSavedGame);
    }

    protected void drawDashEffects(float delta, float playerX, float playerY, float hitboxWidth) {
            if (dashEffectAnimation == null) {
                return;
            }

            float animDuration = dashEffectAnimation.getAnimationDuration();

            for (int i = dashEffects.size - 1; i >= 0; i--) {
                DashEffectAnim effect = dashEffects.get(i);
                effect.stateTime += delta;

                if (dashEffectAnimation.isAnimationFinished(effect.stateTime)) {
                    dashEffects.removeIndex(i);
                } else {
                    TextureRegion effectFrame =
                        dashEffectAnimation.getKeyFrame(effect.stateTime);

                    float scale = 1.3f;
                    float scaledWidth = effectFrame.getRegionWidth() * scale;
                    float scaledHeight = effectFrame.getRegionHeight() * scale;

                    float effectDrawX;
                    float overlap = 25f;

                    if (effect.flipX) {
                        effectDrawX = playerX + hitboxWidth - overlap;
                    } else {
                        effectDrawX = playerX - scaledWidth + overlap;
                    }

                    float hitboxCenterY = playerY + 35f;
                    float effectDrawY = hitboxCenterY - (scaledHeight / 2f);

                    float alpha = 1.0f - (effect.stateTime / animDuration);
                    if (alpha < 0f) {
                        alpha = 0f;
                    }

                    if (isCharmEquipped(CharmId.SHARP_SHADOW)) {
                        batch.setColor(0.34f, 0.15f, 0.62f, alpha);
                    } else {
                        batch.setColor(1f, 1f, 1f, alpha);
                    }

                    drawTextureRegion(
                        effectFrame,
                        Math.round(effectDrawX),
                        Math.round(effectDrawY),
                        scaledWidth,
                        scaledHeight,
                        effect.flipX
                    );

                    batch.setColor(1f, 1f, 1f, 1f);
                }
            }
        }

    protected void spawnSlashEffect() {
            Knight.AttackDirection direction = knightModel.getCurrentAttackDirection();
            Animation<TextureRegion> animation = getSlashAnimation(direction);

            if (animation == null) {
                return;
            }

            float playerX = knightModel.getPosition().x;
            float playerY = knightModel.getPosition().y;

            TextureRegion firstFrame = animation.getKeyFrame(0f);

            float scale = 1.0f;
            float width = firstFrame.getRegionWidth() * scale;
            float height = firstFrame.getRegionHeight() * scale;

            float x;
            float y;
            boolean flipX;

            if (direction == Knight.AttackDirection.UP) {
                Rectangle attackRect = knightModel.getAttackBounds();

                flipX = knightModel.isFacingLeft();
                x = attackRect.x + (attackRect.width / 2f) - (width / 2f);
                y = playerY + 52f;
            } else if (direction == Knight.AttackDirection.DOWN) {
                Rectangle attackRect = knightModel.getAttackBounds();

                flipX = knightModel.isFacingLeft();
                x = attackRect.x + (attackRect.width / 2f) - (width / 2f);
                y = playerY - height + 18f;
            } else {
                flipX = !knightModel.isFacingLeft();

                float slashRightOffsetX = -100f;
                float slashLeftOffsetX = 130f;

                if (knightModel.isFacingLeft()) {
                    x = playerX + slashLeftOffsetX - width;
                } else {
                    x = playerX + slashRightOffsetX;
                }

                y = playerY - 10f;
            }

            slashEffects.add(new SlashEffectAnim(x, y, scale, flipX, direction));
        }

    protected void drawSlashEffects(float delta) {
            for (int i = slashEffects.size - 1; i >= 0; i--) {
                SlashEffectAnim effect = slashEffects.get(i);
                Animation<TextureRegion> animation = getSlashAnimation(effect.direction);

                if (animation == null) {
                    slashEffects.removeIndex(i);
                    continue;
                }

                effect.stateTime += delta;

                if (animation.isAnimationFinished(effect.stateTime)) {
                    slashEffects.removeIndex(i);
                    continue;
                }

                TextureRegion frame = animation.getKeyFrame(effect.stateTime);

                drawTextureRegion(
                    frame,
                    Math.round(effect.x),
                    Math.round(effect.y),
                    frame.getRegionWidth() * effect.scale,
                    frame.getRegionHeight() * effect.scale,
                    effect.flipX
                );
            }
        }

    protected void drawCombatEffects(float delta) {
            if (combatEffectManager != null) {
                combatEffectManager.draw(batch, delta);
            }
        }

    protected Animation<TextureRegion> getSlashAnimation(Knight.AttackDirection direction) {
            if (direction == Knight.AttackDirection.UP) {
                if (slashEffectUpAnimation != null) {
                    return slashEffectUpAnimation;
                }

                return slashEffectAnimation;
            }

            if (direction == Knight.AttackDirection.DOWN) {
                if (slashEffectDownAnimation != null) {
                    return slashEffectDownAnimation;
                }

                return slashEffectAnimation;
            }

            return slashEffectAnimation;
        }

    protected void startScreenShake(float magnitude) {
            startScreenShake(magnitude, DAMAGE_SCREEN_SHAKE_DURATION);
        }

    protected void startScreenShake(float magnitude, float duration) {
            float safeMagnitude = Math.max(0f, magnitude);
            float safeDuration = Math.max(0.04f, duration);

            if (screenShakeTimer > 0f) {
                screenShakeMagnitude = Math.max(screenShakeMagnitude, safeMagnitude);
                screenShakeDuration = Math.max(screenShakeDuration, safeDuration);
                screenShakeTimer = Math.max(screenShakeTimer, safeDuration);
            } else {
                screenShakeMagnitude = safeMagnitude;
                screenShakeDuration = safeDuration;
                screenShakeTimer = safeDuration;
            }
        }

    protected void handleSpikeCollision() {
            if (knightModel.getCurrentHealth() <= 0) {
                return;
            }

            if (spikeDamageCooldown > 0f) {
                return;
            }

            if (!knightBodyOverlapsSpikes()) {
                return;
            }

            if (!knightModel.canTakeDamage()) {
                return;
            }

            knightModel.takeDamage(SPIKE_DAMAGE);
            stopFootsteps();
            triggerHazardWarning();
            startScreenShake(SPIKE_SCREEN_SHAKE_MAGNITUDE);

            spikeDamageCooldown = SPIKE_DAMAGE_COOLDOWN_TIME;

            if (knightModel.getCurrentHealth() > 0 && lastSafeRespawnPosition != null) {
                knightModel.respawnAt(lastSafeRespawnPosition.x, lastSafeRespawnPosition.y);
            }
        }

    protected void updateLastSafeRespawnPosition() {
            if (knightModel.getCurrentHealth() <= 0) {
                return;
            }

            if (!knightModel.isGrounded()) {
                return;
            }

            if (knightBodyOverlapsSpikes()) {
                return;
            }

            lastSafeRespawnPosition.set(
                knightModel.getPosition().x,
                knightModel.getPosition().y + SPIKE_RESPAWN_OFFSET_Y
            );
        }

    protected boolean knightBodyOverlapsSpikes() {
            Rectangle knightBounds = knightModel.getBounds();

            if (spikeRectangles != null) {
                for (Rectangle spike : spikeRectangles) {
                    if (knightBounds.overlaps(spike)) {
                        return true;
                    }
                }
            }

            if (spikePolygons != null) {
                for (Polygon spike : spikePolygons) {
                    if (rectOverlapsPolygonForAttack(knightBounds, spike)) {
                        return true;
                    }
                }
            }

            return false;
        }

    protected boolean attackOverlapsSpikes(Rectangle attackRect) {
            if (spikeRectangles != null) {
                for (Rectangle spike : spikeRectangles) {
                    if (attackRect.overlaps(spike)) {
                        return true;
                    }
                }
            }

            if (spikePolygons != null) {
                for (Polygon spike : spikePolygons) {
                    if (rectOverlapsPolygonForAttack(attackRect, spike)) {
                        return true;
                    }
                }
            }

            return false;
        }

    protected boolean handleSecretWallHit(Rectangle attackRect) {
            if (secretWalls == null) {
                return false;
            }

            for (SecretWallHitbox secretWall : secretWalls) {
                if (secretWall == null || secretWall.destroyed) {
                    continue;
                }

                boolean overlaps = false;

                if (secretWall.rectangle != null) {
                    overlaps = attackRect.overlaps(secretWall.rectangle);
                } else if (secretWall.polygon != null) {
                    overlaps = rectOverlapsPolygonForAttack(attackRect, secretWall.polygon);
                }

                if (!overlaps) {
                    continue;
                }

                if (secretWall.rectangle != null) {
                    spawnNailTerrainEffect(attackRect, secretWall.rectangle);
                } else if (secretWall.polygon != null) {
                    spawnNailTerrainEffect(attackRect, secretWall.polygon);
                }

                Sound wallHitSound = secretWall.hits % 2 == 0
                    ? breakableWallHitOneSfx
                    : breakableWallHitTwoSfx;
                playSfx(wallHitSound, SFX_BREAKABLE_WALL_VOLUME);
                secretWall.hits++;

                if (knightModel.isPogoAttack()) {
                    knightModel.pogoBounce();
                } else {
                    knightModel.markAttackHit();
                }

                if (secretWall.hits >= SECRET_WALL_REQUIRED_HITS) {
                    destroySecretWall(secretWall);
                }

                return true;
            }

            return false;
        }

    protected void destroySecretWall(SecretWallHitbox secretWall) {
            if (secretWall == null || secretWall.destroyed) {
                return;
            }

            secretWall.destroyed = true;
            playSfx(breakableWallDeathSfx, SFX_BREAKABLE_WALL_VOLUME);

            if (secretWall.object != null) {
                secretWall.object.setVisible(false);
            }

            if (secretWall.rectangle != null && platforms != null) {
                platforms.removeValue(secretWall.rectangle, true);
            }

            if (secretWall.polygon != null && polygonPlatforms != null) {
                polygonPlatforms.removeValue(secretWall.polygon, true);
            }

            refreshNearbyCollisionGeometry(true);

            if (secretWallLayer == null && map != null) {
                secretWallLayer = map.getLayers().get("SecretWall");
            }

            if (secretWallLayer != null) {
                secretWallLayer.setVisible(false);
            }
        }
}
