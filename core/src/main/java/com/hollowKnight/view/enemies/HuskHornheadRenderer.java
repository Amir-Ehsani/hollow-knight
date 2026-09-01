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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.hollowKnight.model.enemy.HuskHornhead;

public class HuskHornheadRenderer implements Disposable {

    private static final String BASE_PATH = "animations/animation/Husk_Hornhead/";

    private static final boolean SOURCE_FRAMES_FACE_LEFT = true;

    private static final int IDLE_FRAMES = 6;
    private static final int WALK_FRAMES = 7;
    private static final int TURN_FRAMES = 2;
    private static final int ATTACK_ANTICIPATE_FRAMES = 5;
    private static final int ATTACK_LUNGE_FRAMES = 12;

    private static final int ATTACK_COOLDOWN_FRAMES = 1;

    private static final int DEATH_LAND_FRAMES = 8;
    private static final int DEATH_AIR_FRAMES = 1;

    private static final float TURN_ANIMATION_DURATION = 0.28f;
    private static final float ANTICIPATE_ANIMATION_DURATION = 0.50f;
    private static final float LUNGE_ANIMATION_DURATION = 1.05f;
    private static final float COOLDOWN_ANIMATION_DURATION = 0.28f;
    private static final float DEATH_LAND_ANIMATION_DURATION = 1.00f;

    private final Array<Texture> textures = new Array<>();

    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> turnAnimation;
    private Animation<TextureRegion> anticipateAnimation;
    private Animation<TextureRegion> lungeAnimation;
    private Animation<TextureRegion> cooldownAnimation;
    private Animation<TextureRegion> deathLandAnimation;
    private Animation<TextureRegion> deathAirAnimation;

    public HuskHornheadRenderer() {
        idleAnimation = loadAnimationByFps(IDLE_FRAMES, 5.5f, Animation.PlayMode.LOOP, "Idle");
        walkAnimation = loadAnimationByFps(WALK_FRAMES, 6.5f, Animation.PlayMode.LOOP, "Walk");
        turnAnimation = loadAnimationByDuration(TURN_FRAMES, TURN_ANIMATION_DURATION, Animation.PlayMode.NORMAL, "Turn");
        anticipateAnimation = loadAnimationByDuration(ATTACK_ANTICIPATE_FRAMES, ANTICIPATE_ANIMATION_DURATION, Animation.PlayMode.NORMAL, "Attack Anticipate");
        lungeAnimation = loadAnimationByDuration(ATTACK_LUNGE_FRAMES, LUNGE_ANIMATION_DURATION, Animation.PlayMode.NORMAL, "Attack Lunge");
        cooldownAnimation = loadAnimationByDuration(ATTACK_COOLDOWN_FRAMES, COOLDOWN_ANIMATION_DURATION, Animation.PlayMode.NORMAL, "Attack Cooldown");
        deathLandAnimation = loadAnimationByDuration(DEATH_LAND_FRAMES, DEATH_LAND_ANIMATION_DURATION, Animation.PlayMode.NORMAL, "Death Land");
        deathAirAnimation = loadAnimationByFps(DEATH_AIR_FRAMES, 6f, Animation.PlayMode.NORMAL, "Death Air");
    }

    public void draw(SpriteBatch batch, HuskHornhead enemy) {
        if (batch == null || enemy == null || enemy.isReadyToRemove()) {
            return;
        }

        TextureRegion frame = getFrameFor(enemy);

        boolean shouldFlip = SOURCE_FRAMES_FACE_LEFT
            ? !enemy.isFacingLeft()
            : enemy.isFacingLeft();

        Rectangle bounds = enemy.getBounds();

        float scale = enemy.getDrawScale();
        float drawWidth = frame.getRegionWidth() * scale;
        float drawHeight = frame.getRegionHeight() * scale;

        float drawX = Math.round(bounds.x + bounds.width / 2f - drawWidth / 2f);
        float drawY = Math.round(bounds.y - 10f * scale);

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
    }

    private TextureRegion getFrameFor(HuskHornhead enemy) {
        HuskHornhead.State state = enemy.getState();
        float stateTime = enemy.getStateTime();

        if (state == HuskHornhead.State.TURNING) {
            return turnAnimation.getKeyFrame(stateTime, false);
        }

        if (state == HuskHornhead.State.ANTICIPATING) {
            return anticipateAnimation.getKeyFrame(stateTime, false);
        }

        if (state == HuskHornhead.State.LUNGING) {
            return lungeAnimation.getKeyFrame(stateTime, false);
        }

        if (state == HuskHornhead.State.COOLDOWN) {
            return cooldownAnimation.getKeyFrame(stateTime, false);
        }

        if (state == HuskHornhead.State.DYING) {
            if (enemy.isGrounded()) {
                return deathLandAnimation.getKeyFrame(stateTime, false);
            }

            return deathAirAnimation.getKeyFrame(stateTime, false);
        }

        if (state == HuskHornhead.State.DEAD) {
            return deathLandAnimation.getKeyFrame(999f, false);
        }

        if (state == HuskHornhead.State.CHASING || state == HuskHornhead.State.WALKING) {
            return walkAnimation.getKeyFrame(stateTime, true);
        }

        return idleAnimation.getKeyFrame(stateTime, true);
    }

    private Animation<TextureRegion> loadAnimationByFps(
        int expectedFrameCount,
        float fps,
        Animation.PlayMode playMode,
        String... baseNames
    ) {
        Array<TextureRegion> frames = loadFrames(expectedFrameCount, baseNames);
        return new Animation<>(1f / Math.max(1f, fps), frames, playMode);
    }

    private Animation<TextureRegion> loadAnimationByDuration(
        int expectedFrameCount,
        float totalDuration,
        Animation.PlayMode playMode,
        String... baseNames
    ) {
        Array<TextureRegion> frames = loadFrames(expectedFrameCount, baseNames);
        float frameDuration = Math.max(0.01f, totalDuration / Math.max(1, frames.size));
        return new Animation<>(frameDuration, frames, playMode);
    }

    private Array<TextureRegion> loadFrames(int expectedFrameCount, String... baseNames) {
        Array<TextureRegion> frames = new Array<>();

        for (String baseName : baseNames) {
            loadSeparateFrames(frames, baseName, expectedFrameCount);

            if (frames.size == expectedFrameCount) {
                return frames;
            }

            disposeAndClear(frames);
        }

        for (String baseName : baseNames) {
            loadSheetFrames(frames, baseName, expectedFrameCount);

            if (frames.size == expectedFrameCount) {
                return frames;
            }

            disposeAndClear(frames);
        }

        Texture fallback = createFallbackTexture();
        textures.add(fallback);
        frames.add(new TextureRegion(fallback));
        return frames;
    }

    private void loadSeparateFrames(Array<TextureRegion> frames, String baseName, int expectedFrameCount) {
        for (int i = 0; i < expectedFrameCount; i++) {
            String path = BASE_PATH + baseName + "_" + String.format("%03d", i) + ".png";
            FileHandle file = Gdx.files.internal(path);

            if (!file.exists()) {
                return;
            }

            Texture texture = new Texture(file);
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

            textures.add(texture);
            frames.add(new TextureRegion(texture));
        }
    }

    private void loadSheetFrames(Array<TextureRegion> frames, String baseName, int expectedFrameCount) {
        String sheetPath = BASE_PATH + baseName + ".png";
        FileHandle sheetFile = Gdx.files.internal(sheetPath);

        if (!sheetFile.exists()) {
            return;
        }

        Texture sheet = new Texture(sheetFile);
        sheet.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        textures.add(sheet);

        int frameCount = Math.max(1, expectedFrameCount);

        if (frameCount == 1) {
            frames.add(new TextureRegion(sheet));
            return;
        }

        int frameWidth = sheet.getWidth() / frameCount;

        if (frameWidth <= 0 || sheet.getWidth() % frameCount != 0) {
            return;
        }

        TextureRegion[][] split = TextureRegion.split(sheet, frameWidth, sheet.getHeight());

        for (int i = 0; i < frameCount && i < split[0].length; i++) {
            frames.add(split[0][i]);
        }
    }

    private void disposeAndClear(Array<TextureRegion> frames) {
        for (TextureRegion frame : frames) {
            Texture texture = frame.getTexture();

            if (texture != null) {
                textures.removeValue(texture, true);
                texture.dispose();
            }
        }

        frames.clear();
    }

    private Texture createFallbackTexture() {
        Pixmap pixmap = new Pixmap(58, 70, Pixmap.Format.RGBA8888);

        pixmap.setColor(Color.valueOf("2c3346ff"));
        pixmap.fillCircle(30, 38, 18);
        pixmap.fillRectangle(18, 20, 26, 28);

        pixmap.setColor(Color.valueOf("8792a8ff"));
        pixmap.fillCircle(30, 46, 12);

        pixmap.setColor(Color.valueOf("e8e8ffff"));
        pixmap.fillTriangle(26, 59, 8, 68, 19, 50);

        pixmap.setColor(Color.valueOf("111522ff"));
        pixmap.fillCircle(34, 49, 2);

        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

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
    }
}
