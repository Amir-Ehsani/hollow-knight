package com.hollowKnight.view.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.hollowKnight.model.enemy.Crystallized;

public class CrystallizedRenderer implements Disposable {

    private static final String BASE_PATH = "animations/animation/Crystallized/";

    private static final boolean SOURCE_FRAMES_FACE_RIGHT = false;

    private final Array<Texture> textures = new Array<>();

    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> runAnimation;
    private Animation<TextureRegion> turnAnimation;
    private Animation<TextureRegion> evadeAnimation;
    private Animation<TextureRegion> shootAnimation;
    private Animation<TextureRegion> laserCircleAnimation;
    private Animation<TextureRegion> deathLandAnimation;
    private Animation<TextureRegion> deathAirAnimation;

    private Texture pixelTexture;
    private TextureRegion pixelRegion;

    public CrystallizedRenderer() {
        idleAnimation = loadAnimation("Idle", 5, 5f, Animation.PlayMode.LOOP);
        runAnimation = loadAnimation("Run", 6, 8f, Animation.PlayMode.LOOP);
        turnAnimation = loadAnimation("Turn", 3, 12f, Animation.PlayMode.NORMAL);
        evadeAnimation = loadAnimation("Evade", 7, 14f, Animation.PlayMode.NORMAL);
        shootAnimation = loadAnimation("Shoot", 7, 6f, Animation.PlayMode.NORMAL);
        laserCircleAnimation = loadAnimation("LaserCircle", 11, 8f, Animation.PlayMode.LOOP);
        deathLandAnimation = loadAnimation("Death Land", 3, 11f, Animation.PlayMode.NORMAL);
        deathAirAnimation = loadAnimation("Death Air", 3, 11f, Animation.PlayMode.NORMAL);

        pixelTexture = createSolidTexture(Color.WHITE);
        pixelRegion = new TextureRegion(pixelTexture);
    }

    public void draw(SpriteBatch batch, Crystallized enemy) {
        if (batch == null || enemy == null || enemy.isReadyToRemove()) {
            return;
        }

        drawLaser(batch, enemy);

        TextureRegion frame = getFrameFor(enemy);
        boolean shouldFlip = SOURCE_FRAMES_FACE_RIGHT ? enemy.isFacingLeft() : !enemy.isFacingLeft();

        Rectangle bounds = enemy.getBounds();
        float scale = enemy.getDrawScale();
        float drawWidth = frame.getRegionWidth() * scale;
        float drawHeight = frame.getRegionHeight() * scale;

        float drawX = Math.round(bounds.x + bounds.width / 2f - drawWidth / 2f);
        float drawY = Math.round(bounds.y - 12f * scale);

        batch.draw(
            frame.getTexture(),
            drawX,
            drawY,
            0f,
            0f,
            drawWidth,
            drawHeight,
            1f,
            1f,
            0f,
            frame.getRegionX(),
            frame.getRegionY(),
            frame.getRegionWidth(),
            frame.getRegionHeight(),
            shouldFlip,
            false
        );

        drawChargeCircle(batch, enemy, scale);
    }

    private TextureRegion getFrameFor(Crystallized enemy) {
        Crystallized.State state = enemy.getState();
        float stateTime = enemy.getStateTime();

        if (state == Crystallized.State.TURNING) {
            return turnAnimation.getKeyFrame(stateTime, false);
        }

        if (state == Crystallized.State.EVADING) {
            return evadeAnimation.getKeyFrame(stateTime, false);
        }

        if (state == Crystallized.State.SHOOTING) {
            return shootAnimation.getKeyFrame(stateTime, false);
        }

        if (state == Crystallized.State.DYING) {
            if (enemy.isGrounded()) {
                return deathLandAnimation.getKeyFrame(stateTime, false);
            }

            return deathAirAnimation.getKeyFrame(stateTime, false);
        }

        if (state == Crystallized.State.DEAD) {
            return deathLandAnimation.getKeyFrame(999f, false);
        }

        if (state == Crystallized.State.RUNNING) {
            return runAnimation.getKeyFrame(stateTime, true);
        }

        return idleAnimation.getKeyFrame(stateTime, true);
    }

    private void drawChargeCircle(SpriteBatch batch, Crystallized enemy, float enemyScale) {
        if (laserCircleAnimation == null || enemy.getState() != Crystallized.State.SHOOTING) {
            return;
        }

        float progress = enemy.getLaserChargeProgress();

        if (progress <= 0.01f) {
            return;
        }

        TextureRegion circle = laserCircleAnimation.getKeyFrame(enemy.getStateTime(), true);
        Vector2 start = enemy.getLaserStart();
        float scale = enemyScale * (0.55f + progress * 0.35f);
        float width = circle.getRegionWidth() * scale;
        float height = circle.getRegionHeight() * scale;

        float oldColorBits = batch.getPackedColor();
        batch.setColor(1f, 0.78f, 1f, 0.45f + progress * 0.45f);
        batch.draw(circle, Math.round(start.x - width / 2f), Math.round(start.y - height / 2f), width, height);
        batch.setPackedColor(oldColorBits);
    }

    private void drawLaser(SpriteBatch batch, Crystallized enemy) {
        if (pixelRegion == null || !enemy.isLaserActive()) {
            return;
        }

        Vector2 start = enemy.getLaserStart();
        Vector2 end = enemy.getLaserEnd();
        float dx = end.x - start.x;
        float dy = end.y - start.y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length <= 1f) {
            return;
        }

        float rotation = (float) Math.toDegrees(Math.atan2(dy, dx));
        float beamWidth = enemy.getLaserWidth();
        float oldColorBits = batch.getPackedColor();

        batch.setColor(1f, 0.58f, 1f, 0.34f);
        drawBeam(batch, start.x, start.y, length, beamWidth * 1.85f, rotation);

        batch.setColor(1f, 0.82f, 1f, 0.78f);
        drawBeam(batch, start.x, start.y, length, beamWidth, rotation);

        batch.setColor(1f, 1f, 1f, 0.95f);
        drawBeam(batch, start.x, start.y, length, Math.max(3f, beamWidth * 0.32f), rotation);

        batch.setPackedColor(oldColorBits);
    }

    private void drawBeam(SpriteBatch batch, float x, float y, float length, float thickness, float rotation) {
        batch.draw(
            pixelRegion,
            x,
            y - thickness / 2f,
            0f,
            thickness / 2f,
            length,
            thickness,
            1f,
            1f,
            rotation
        );
    }

    private Animation<TextureRegion> loadAnimation(String baseName, int sheetFrameCount, float fps, Animation.PlayMode playMode) {
        Array<TextureRegion> frames = new Array<>();

        String sheetPath = BASE_PATH + baseName + ".png";
        FileHandle sheetFile = Gdx.files.internal(sheetPath);

        if (sheetFile.exists()) {
            Texture sheet = new Texture(sheetFile);
            sheet.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            textures.add(sheet);

            int frameCount = Math.max(1, sheetFrameCount);
            int frameWidth = Math.max(1, sheet.getWidth() / frameCount);
            TextureRegion[][] split = TextureRegion.split(sheet, frameWidth, sheet.getHeight());

            for (int i = 0; i < split[0].length && i < frameCount; i++) {
                frames.add(split[0][i]);
            }
        }

        if (frames.size == 0) {
            for (int i = 0; i < 64; i++) {
                String path = BASE_PATH + baseName + "_" + String.format("%03d", i) + ".png";
                FileHandle file = Gdx.files.internal(path);

                if (!file.exists()) {
                    if (i == 0) {
                        continue;
                    }

                    break;
                }

                Texture texture = new Texture(file);
                texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                textures.add(texture);
                frames.add(new TextureRegion(texture));
            }
        }

        if (frames.size == 0) {
            Texture fallback = createFallbackTexture();
            textures.add(fallback);
            frames.add(new TextureRegion(fallback));
        }

        return new Animation<>(1f / fps, frames, playMode);
    }

    private Texture createFallbackTexture() {
        Pixmap pixmap = new Pixmap(64, 56, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.valueOf("1d2030ff"));
        pixmap.fillRectangle(12, 16, 38, 30);
        pixmap.setColor(Color.valueOf("e4a6ffff"));
        pixmap.fillTriangle(5, 30, 19, 6, 26, 38);
        pixmap.fillTriangle(30, 16, 42, 0, 54, 34);
        pixmap.setColor(Color.valueOf("f8f0ffff"));
        pixmap.fillCircle(43, 40, 5);

        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    private Texture createSolidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void dispose() {
        for (Texture texture : textures) {
            if (texture != null) {
                texture.dispose();
            }
        }

        textures.clear();

        if (pixelTexture != null) {
            pixelTexture.dispose();
            pixelTexture = null;
        }
    }
}
