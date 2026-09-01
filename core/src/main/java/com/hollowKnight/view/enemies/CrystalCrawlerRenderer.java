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
import com.hollowKnight.model.enemy.CrystalCrawler;

public class CrystalCrawlerRenderer implements Disposable {

    private static final String BASE_PATH = "animations/animation/Crystal_Crawler/";

    private static final boolean SOURCE_FRAMES_FACE_LEFT = true;

    private final Array<Texture> textures = new Array<>();

    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> turnAnimation;
    private Animation<TextureRegion> deathLandAnimation;
    private Animation<TextureRegion> deathAirAnimation;

    public CrystalCrawlerRenderer() {
        walkAnimation = loadAnimation("Walk", 4, 10f, Animation.PlayMode.LOOP);
        turnAnimation = loadAnimation("Turn", 3, 13f, Animation.PlayMode.NORMAL);
        deathLandAnimation = loadAnimation("Death Land", 3, 12f, Animation.PlayMode.NORMAL);
        deathAirAnimation = loadAnimation("Death Air", 4, 12f, Animation.PlayMode.NORMAL);
    }

    public void draw(SpriteBatch batch, CrystalCrawler crawler) {
        if (batch == null || crawler == null || crawler.isReadyToRemove()) {
            return;
        }

        TextureRegion frame = getFrameFor(crawler);

        boolean shouldFlip = SOURCE_FRAMES_FACE_LEFT ? !crawler.isFacingLeft() : crawler.isFacingLeft();

        Rectangle bounds = crawler.getBounds();
        float scale = crawler.getDrawScale();
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
    }

    private TextureRegion getFrameFor(CrystalCrawler crawler) {
        CrystalCrawler.State state = crawler.getState();
        float stateTime = crawler.getStateTime();

        if (state == CrystalCrawler.State.TURNING) {
            return turnAnimation.getKeyFrame(stateTime, false);
        }

        if (state == CrystalCrawler.State.DYING) {
            if (crawler.isGrounded()) {
                return deathLandAnimation.getKeyFrame(stateTime, false);
            }

            return deathAirAnimation.getKeyFrame(stateTime, false);
        }

        if (state == CrystalCrawler.State.DEAD) {
            return deathLandAnimation.getKeyFrame(999f, false);
        }

        return walkAnimation.getKeyFrame(stateTime, true);
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

        if (frames.size == 0) {
            Texture fallback = createFallbackTexture();
            textures.add(fallback);
            frames.add(new TextureRegion(fallback));
        }

        return new Animation<>(1f / fps, frames, playMode);
    }

    private Texture createFallbackTexture() {
        Pixmap pixmap = new Pixmap(48, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.valueOf("7b4ab8ff"));
        pixmap.fillRectangle(0, 10, 48, 18);
        pixmap.setColor(Color.valueOf("f2a7ffff"));
        pixmap.fillTriangle(8, 28, 16, 8, 24, 28);
        pixmap.fillTriangle(22, 30, 30, 8, 40, 30);
        pixmap.setColor(Color.valueOf("ffc16bff"));
        pixmap.fillCircle(13, 17, 4);
        pixmap.fillCircle(34, 17, 4);

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
