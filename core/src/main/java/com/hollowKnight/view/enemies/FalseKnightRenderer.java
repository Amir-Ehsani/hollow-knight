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
import com.hollowKnight.model.enemy.FalseKnight;

public class FalseKnightRenderer implements Disposable {

    private static final String BASE_PATH = "animations/animation/False_knight/";
    private static final boolean SOURCE_FRAMES_FACE_RIGHT = false;
    private static final String EFFECTS_PATH = "animations/animation/Effects/";
    private static final boolean SHOCKWAVE_FRAMES_FACE_RIGHT = true;

    private final Array<Texture> textures = new Array<>();
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> turnAnimation;
    private Animation<TextureRegion> attackAnticAnimation;
    private Animation<TextureRegion> attackAnimation;
    private Animation<TextureRegion> attackRecoverAnimation;
    private Animation<TextureRegion> runAnticAnimation;
    private Animation<TextureRegion> runAnimation;
    private Animation<TextureRegion> jumpAnticAnimation;
    private Animation<TextureRegion> jumpAnimation;
    private Animation<TextureRegion> jumpAttackAnimation;
    private Animation<TextureRegion> jumpAttackHitAnimation;
    private Animation<TextureRegion> landAnimation;
    private Animation<TextureRegion> bodyAnimation;
    private Animation<TextureRegion> stunRecoverAnimation;
    private Animation<TextureRegion> deathFallAnimation;
    private Animation<TextureRegion> deathHitAnimation;
    private Animation<TextureRegion> deathLandAnimation;
    private Animation<TextureRegion> shockwaveAnimation;
    private Texture pixelTexture;
    private TextureRegion pixelRegion;

    public FalseKnightRenderer() {
        idleAnimation = loadAnimation("Idle", 5, 6.5f, Animation.PlayMode.LOOP);
        turnAnimation = loadAnimation("Turn", 2, 12f, Animation.PlayMode.NORMAL);
        attackAnticAnimation = loadAnimation("Attack Antic", 6, 11f, Animation.PlayMode.NORMAL);
        attackAnimation = loadAnimation("Attack", 5, 13f, Animation.PlayMode.NORMAL);
        attackRecoverAnimation = loadAnimation("Attack Recover", 5, 10f, Animation.PlayMode.NORMAL);
        runAnticAnimation = loadAnimation("Run Antic", 2, 10f, Animation.PlayMode.NORMAL);
        runAnimation = loadAnimation("Run", 5, 12f, Animation.PlayMode.LOOP);
        jumpAnticAnimation = loadAnimation("Jump Antic", 3, 10f, Animation.PlayMode.NORMAL);
        jumpAnimation = loadAnimation("Jump", 2, 8f, Animation.PlayMode.LOOP);
        jumpAttackAnimation = loadAnimation("Jump Attack", 8, 13f, Animation.PlayMode.NORMAL);
        jumpAttackHitAnimation = loadAnimation("Jump Attack Hit", 1, 10f, Animation.PlayMode.NORMAL, "Jump Attack Hit_3_001.png");
        landAnimation = loadAnimation("Land", 5, 11f, Animation.PlayMode.NORMAL);
        bodyAnimation = loadAnimation("Body", 5, 8f, Animation.PlayMode.LOOP);
        stunRecoverAnimation = loadAnimation("Stun Recover", 6, 11f, Animation.PlayMode.NORMAL);
        deathFallAnimation = loadAnimation("DeathFall", 3, 10f, Animation.PlayMode.NORMAL);
        deathHitAnimation = loadAnimation("DeathHit", 3, 10f, Animation.PlayMode.NORMAL);
        deathLandAnimation = loadAnimation("DeathLand", 11, 10f, Animation.PlayMode.NORMAL);
        shockwaveAnimation = loadExternalAnimation(EFFECTS_PATH, "Shockwave", 8, 18f, Animation.PlayMode.LOOP);
        pixelTexture = createPixelTexture();
        pixelRegion = new TextureRegion(pixelTexture);
    }

    public void draw(SpriteBatch batch, FalseKnight boss) {
        if (batch == null || boss == null) {
            return;
        }

        drawShockwaves(batch, boss);

        TextureRegion frame = getFrameFor(boss);
        boolean shouldFlip = SOURCE_FRAMES_FACE_RIGHT ? boss.isFacingLeft() : !boss.isFacingLeft();

        Rectangle bounds = boss.getBodyBounds();
        float scale = boss.getDrawScale();
        float drawWidth = frame.getRegionWidth() * scale;
        float drawHeight = frame.getRegionHeight() * scale;
        float drawX = Math.round(bounds.x + bounds.width / 2f - drawWidth / 2f);
        float drawY = Math.round(bounds.y - getVerticalOffset(boss) * scale);

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

    private void drawShockwaves(SpriteBatch batch, FalseKnight boss) {
        if (boss.getShockwaves() == null) {
            return;
        }

        float oldColorBits = batch.getPackedColor();
        batch.setColor(1f, 1f, 1f, 0.86f);

        for (FalseKnight.Shockwave wave : boss.getShockwaves()) {
            if (wave == null || !wave.isAlive()) {
                continue;
            }

            Rectangle r = wave.getBounds();

            if (shockwaveAnimation != null) {
                TextureRegion frame = shockwaveAnimation.getKeyFrame(wave.getAge(), true);
                boolean shouldFlip = SHOCKWAVE_FRAMES_FACE_RIGHT
                    ? wave.getDirection() < 0
                    : wave.getDirection() > 0;

                float drawWidth = Math.max(r.width, frame.getRegionWidth());
                float drawHeight = frame.getRegionHeight();
                float drawX = r.x + r.width / 2f - drawWidth / 2f;
                float drawY = r.y;
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
            } else if (pixelRegion != null) {
                float pulse = 1f + Math.min(0.45f, wave.getAge() * 0.25f);
                batch.draw(pixelRegion, r.x, r.y + r.height * 0.33f, r.width * pulse, Math.max(5f, r.height * 0.20f));
                batch.draw(pixelRegion, r.x + r.width * 0.18f, r.y + r.height * 0.56f, r.width * 0.62f * pulse, Math.max(4f, r.height * 0.14f));
            }
        }

        batch.setPackedColor(oldColorBits);
    }

    private float getVerticalOffset(FalseKnight boss) {
        float offset = boss.getSpriteYOffset();

        switch (boss.getState()) {
            case STUNNED:
                return Math.max(28f, offset - 12f);
            case STUN_FALL:
            case DEATH_FALL:
            case DEATH_HIT:
            case DEATH_LAND:
            case DEAD:
                return Math.max(24f, offset - 14f);
            case JUMPING:
            case JUMP_ATTACK:
                return Math.max(22f, offset - 8f);
            default:
                return offset;
        }
    }

    private TextureRegion getFrameFor(FalseKnight boss) {
        FalseKnight.State state = boss.getState();
        float time = boss.getStateTime();

        switch (state) {
            case TURNING:
                return turnAnimation.getKeyFrame(time, false);
            case ATTACK_ANTIC:
                return attackAnticAnimation.getKeyFrame(time, false);
            case ATTACKING:
                return attackAnimation.getKeyFrame(time, false);
            case ATTACK_RECOVER:
                return attackRecoverAnimation.getKeyFrame(time, false);
            case RUN_ANTIC:
                return runAnticAnimation.getKeyFrame(time, false);
            case RUNNING:
                return runAnimation.getKeyFrame(time, true);
            case JUMP_ANTIC:
                return jumpAnticAnimation.getKeyFrame(time, false);
            case JUMPING:
                return jumpAnimation.getKeyFrame(time, true);
            case JUMP_ATTACK:
                return jumpAttackAnimation.getKeyFrame(time, false);
            case LANDING:
                return landAnimation.getKeyFrame(time, false);
            case STUN_FALL:
                return deathFallAnimation.getKeyFrame(time, false);
            case STUNNED:
                return bodyAnimation.getKeyFrame(time, true);
            case STUN_RECOVER:
                return stunRecoverAnimation.getKeyFrame(time, false);
            case DEATH_HIT:
                return deathHitAnimation.getKeyFrame(time, false);
            case DEATH_FALL:
                return deathFallAnimation.getKeyFrame(time, false);
            case DEATH_LAND:
                return deathLandAnimation.getKeyFrame(time, false);
            case DEAD:
                return deathLandAnimation.getKeyFrame(999f, false);
            case IDLE:
            default:
                return idleAnimation.getKeyFrame(time, true);
        }
    }

    private Animation<TextureRegion> loadAnimation(String baseName, int sheetFrameCount, float fps, Animation.PlayMode playMode, String... extraNames) {
        Array<TextureRegion> frames = new Array<>();

        for (int i = 0; i < 96; i++) {
            String path = BASE_PATH + baseName + "_" + String.format("%03d", i) + ".png";
            FileHandle file = Gdx.files.internal(path);

            if (!file.exists()) {
                if (i == 0) {
                    continue;
                }

                break;
            }

            addFrame(frames, file);
        }

        if (frames.size == 0 && extraNames != null) {
            for (String extraName : extraNames) {
                FileHandle file = Gdx.files.internal(BASE_PATH + extraName);

                if (file.exists()) {
                    addFrame(frames, file);
                }
            }
        }

        if (frames.size == 0) {
            FileHandle sheetFile = Gdx.files.internal(BASE_PATH + baseName + ".png");

            if (sheetFile.exists()) {
                Texture sheet = new Texture(sheetFile);
                sheet.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                textures.add(sheet);
                int frameCount = Math.max(1, sheetFrameCount);
                int frameWidth = Math.max(1, sheet.getWidth() / frameCount);
                TextureRegion[][] split = TextureRegion.split(sheet, frameWidth, sheet.getHeight());

                for (int i = 0; i < split[0].length; i++) {
                    frames.add(split[0][i]);
                }
            }
        }

        if (frames.size == 0) {
            Texture fallback = createFallbackTexture(baseName);
            textures.add(fallback);
            frames.add(new TextureRegion(fallback));
        }

        return new Animation<>(1f / Math.max(1f, fps), frames, playMode);
    }

    private Animation<TextureRegion> loadExternalAnimation(String path, String baseName, int sheetFrameCount, float fps, Animation.PlayMode playMode) {
        Array<TextureRegion> frames = new Array<>();

        for (int i = 0; i < 96; i++) {
            String framePath = path + baseName + "_" + String.format("%03d", i) + ".png";
            FileHandle file = Gdx.files.internal(framePath);

            if (!file.exists()) {
                if (i == 0) {
                    continue;
                }

                break;
            }

            addFrame(frames, file);
        }

        if (frames.size == 0) {
            FileHandle sheetFile = Gdx.files.internal(path + baseName + ".png");

            if (sheetFile.exists()) {
                Texture sheet = new Texture(sheetFile);
                sheet.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                textures.add(sheet);
                int frameCount = Math.max(1, sheetFrameCount);
                int frameWidth = Math.max(1, sheet.getWidth() / frameCount);
                TextureRegion[][] split = TextureRegion.split(sheet, frameWidth, sheet.getHeight());

                for (int i = 0; i < split[0].length; i++) {
                    frames.add(split[0][i]);
                }
            }
        }

        if (frames.size == 0) {
            return null;
        }

        return new Animation<>(1f / Math.max(1f, fps), frames, playMode);
    }

    private void addFrame(Array<TextureRegion> frames, FileHandle file) {
        Texture texture = new Texture(file);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        textures.add(texture);
        frames.add(new TextureRegion(texture));
    }

    private Texture createPixelTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createFallbackTexture(String name) {
        Pixmap pixmap = new Pixmap(128, 128, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.valueOf("273047ff"));
        pixmap.fillRectangle(24, 34, 80, 58);
        pixmap.setColor(Color.valueOf("9aa7c7ff"));
        pixmap.fillCircle(64, 78, 34);
        pixmap.setColor(Color.valueOf("111827ff"));
        pixmap.fillRectangle(35, 96, 58, 14);
        pixmap.setColor(Color.valueOf("6d7ea5ff"));
        pixmap.fillRectangle(80, 28, 42, 16);
        pixmap.setColor(Color.valueOf("d5defaff"));
        pixmap.drawRectangle(8, 8, 112, 112);

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

        if (pixelTexture != null) {
            pixelTexture.dispose();
            pixelTexture = null;
        }
    }
}
