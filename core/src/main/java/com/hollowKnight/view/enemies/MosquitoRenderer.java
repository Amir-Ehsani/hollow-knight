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
import com.hollowKnight.model.enemy.Mosquito;

public class MosquitoRenderer implements Disposable {

    private static final String BASE_PATH = "animations/animation/Mosquito/";

    private static final boolean SOURCE_FRAMES_FACE_LEFT = true;

    private final Array<Texture> textures = new Array<>();

    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> attackAnticipateAnimation;
    private Animation<TextureRegion> attackAnimation;
    private Animation<TextureRegion> turnAnimation;
    private Animation<TextureRegion> deathAirAnimation;
    private Animation<TextureRegion> deathLandAnimation;

    public MosquitoRenderer() {
        idleAnimation = loadAnimation("Idle", 8, 12f, Animation.PlayMode.LOOP);
        attackAnticipateAnimation = loadAnimation("Attack Anticipate", 6, 13f, Animation.PlayMode.NORMAL);
        attackAnimation = loadAnimation("Attack", 3, 18f, Animation.PlayMode.LOOP);
        turnAnimation = loadAnimation("Turn", 3, 14f, Animation.PlayMode.NORMAL);
        deathAirAnimation = loadAnimation("Death Air", 3, 12f, Animation.PlayMode.NORMAL);
        deathLandAnimation = loadAnimation("Death Land", 2, 12f, Animation.PlayMode.NORMAL);
    }

    public void draw(SpriteBatch batch, Mosquito mosquito) {
        if (batch == null || mosquito == null || mosquito.isReadyToRemove()) {
            return;
        }

        TextureRegion frame = getFrameFor(mosquito);
        boolean shouldFlip = SOURCE_FRAMES_FACE_LEFT ? !mosquito.isFacingLeft() : mosquito.isFacingLeft();

        Rectangle bounds = mosquito.getBounds();
        float scale = mosquito.getDrawScale();
        float drawWidth = frame.getRegionWidth() * scale;
        float drawHeight = frame.getRegionHeight() * scale;

        float drawX = Math.round(bounds.x + bounds.width / 2f - drawWidth / 2f);
        float drawY = Math.round(bounds.y + bounds.height / 2f - drawHeight / 2f);

        if (mosquito.getState() == Mosquito.State.DEAD || mosquito.hasLandedDeath()) {
            drawY = Math.round(bounds.y - 8f * scale);
        }

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

    private TextureRegion getFrameFor(Mosquito mosquito) {
        Mosquito.State state = mosquito.getState();
        float stateTime = mosquito.getStateTime();

        if (state == Mosquito.State.ANTICIPATING) {
            return attackAnticipateAnimation.getKeyFrame(stateTime, false);
        }

        if (state == Mosquito.State.CHARGING) {
            return attackAnimation.getKeyFrame(stateTime, true);
        }

        if (state == Mosquito.State.TURNING) {
            return turnAnimation.getKeyFrame(stateTime, false);
        }

        if (state == Mosquito.State.DYING) {
            if (mosquito.hasLandedDeath()) {
                return deathLandAnimation.getKeyFrame(stateTime, false);
            }

            return deathAirAnimation.getKeyFrame(stateTime, false);
        }

        if (state == Mosquito.State.DEAD) {
            return deathLandAnimation.getKeyFrame(999f, false);
        }

        return idleAnimation.getKeyFrame(stateTime, true);
    }

    private Animation<TextureRegion> loadAnimation(String baseName, int sheetFrameCount, float fps, Animation.PlayMode playMode) {
        Array<TextureRegion> frames = new Array<>();

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

        if (frames.size == 0) {
            String sheetPath = BASE_PATH + baseName + ".png";
            FileHandle sheetFile = Gdx.files.internal(sheetPath);

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

        if (frames.size == 0 && "Turn".equals(baseName)) {
            return loadAnimation("Turn2", 3, fps, playMode);
        }

        if (frames.size == 0) {
            Texture fallback = createFallbackTexture();
            textures.add(fallback);
            frames.add(new TextureRegion(fallback));
        }

        return new Animation<>(1f / fps, frames, playMode);
    }

    private Texture createFallbackTexture() {
        Pixmap pixmap = new Pixmap(54, 36, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.valueOf("69d7dfff"));
        pixmap.fillTriangle(4, 18, 32, 8, 32, 28);
        pixmap.setColor(Color.valueOf("26334eff"));
        pixmap.fillCircle(32, 18, 11);
        pixmap.setColor(Color.valueOf("d7e8ffff"));
        pixmap.fillTriangle(27, 30, 36, 14, 45, 32);
        pixmap.fillTriangle(27, 6, 36, 22, 45, 4);
        pixmap.setColor(Color.valueOf("f2f9ffff"));
        pixmap.fillCircle(39, 18, 3);

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
