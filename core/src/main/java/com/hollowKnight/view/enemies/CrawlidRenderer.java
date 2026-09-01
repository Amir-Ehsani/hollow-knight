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
import com.hollowKnight.model.enemy.Crawlid;

public class CrawlidRenderer implements Disposable {

    private static final String BASE_PATH = "animations/animation/Crawlid/";

    private static final boolean SOURCE_FRAMES_FACE_RIGHT = false;

    private final Array<Texture> textures = new Array<>();

    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> turnAnimation;
    private Animation<TextureRegion> deathLandAnimation;
    private Animation<TextureRegion> deathAirAnimation;

    public CrawlidRenderer() {
        walkAnimation = loadAnimation(4, 10f, Animation.PlayMode.LOOP, "Walk");
        turnAnimation = loadAnimation(3, 13f, Animation.PlayMode.NORMAL, "Turn", "turn");
        deathLandAnimation = loadAnimation(3, 12f, Animation.PlayMode.NORMAL, "Death Land");
        deathAirAnimation = loadAnimation(4, 12f, Animation.PlayMode.NORMAL, "Death Air");
    }

    public void draw(SpriteBatch batch, Crawlid crawlid) {
        if (batch == null || crawlid == null || crawlid.isReadyToRemove()) {
            return;
        }

        TextureRegion frame = getFrameFor(crawlid);

        boolean shouldFlip = SOURCE_FRAMES_FACE_RIGHT
            ? crawlid.isFacingLeft()
            : !crawlid.isFacingLeft();

        Rectangle bounds = crawlid.getBounds();

        float scale = crawlid.getDrawScale();
        float drawWidth = frame.getRegionWidth() * scale;
        float drawHeight = frame.getRegionHeight() * scale;

        float drawX = Math.round(bounds.x + bounds.width / 2f - drawWidth / 2f);
        float drawY = Math.round(bounds.y - 8f * scale);

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

    private TextureRegion getFrameFor(Crawlid crawlid) {
        Crawlid.State state = crawlid.getState();
        float stateTime = crawlid.getStateTime();

        if (state == Crawlid.State.TURNING) {
            return turnAnimation.getKeyFrame(stateTime, false);
        }

        if (state == Crawlid.State.DYING) {
            if (crawlid.isGrounded()) {
                return deathLandAnimation.getKeyFrame(stateTime, false);
            }

            return deathAirAnimation.getKeyFrame(stateTime, false);
        }

        if (state == Crawlid.State.DEAD) {
            return deathLandAnimation.getKeyFrame(999f, false);
        }

        return walkAnimation.getKeyFrame(stateTime, true);
    }

    private Animation<TextureRegion> loadAnimation(
        int sheetFrameCount,
        float fps,
        Animation.PlayMode playMode,
        String... baseNames
    ) {
        Array<TextureRegion> frames = new Array<>();

        for (String baseName : baseNames) {
            loadSeparateFrames(frames, baseName);

            if (frames.size > 0) {
                break;
            }
        }

        if (frames.size == 0) {
            for (String baseName : baseNames) {
                loadSheetFrames(frames, baseName, sheetFrameCount);

                if (frames.size > 0) {
                    break;
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

    private void loadSeparateFrames(Array<TextureRegion> frames, String baseName) {
        for (int i = 0; i < 64; i++) {
            String path = BASE_PATH + baseName + "_" + String.format("%03d", i) + ".png";
            FileHandle file = Gdx.files.internal(path);

            if (!file.exists()) {
                if (i == 0) {
                    return;
                }

                break;
            }

            Texture texture = new Texture(file);
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

            textures.add(texture);
            frames.add(new TextureRegion(texture));
        }
    }

    private void loadSheetFrames(Array<TextureRegion> frames, String baseName, int sheetFrameCount) {
        String sheetPath = BASE_PATH + baseName + ".png";
        FileHandle sheetFile = Gdx.files.internal(sheetPath);

        if (!sheetFile.exists()) {
            return;
        }

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

    private Texture createFallbackTexture() {
        Pixmap pixmap = new Pixmap(44, 24, Pixmap.Format.RGBA8888);

        pixmap.setColor(Color.valueOf("283653ff"));
        fillEllipse(pixmap, 3, 7, 36, 14);

        pixmap.setColor(Color.valueOf("5d759dff"));
        pixmap.fillCircle(11, 13, 5);
        pixmap.fillCircle(26, 13, 5);

        pixmap.setColor(Color.valueOf("cdd9ffff"));
        pixmap.fillCircle(33, 13, 2);

        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        pixmap.dispose();

        return texture;
    }

    private static void fillEllipse(Pixmap pixmap, int x, int y, int width, int height) {
        float rx = width / 2f;
        float ry = height / 2f;
        float cx = x + rx;
        float cy = y + ry;

        for (int py = y; py < y + height; py++) {
            for (int px = x; px < x + width; px++) {
                float dx = (px + 0.5f - cx) / rx;
                float dy = (py + 0.5f - cy) / ry;

                if (dx * dx + dy * dy <= 1f) {
                    pixmap.drawPixel(px, py);
                }
            }
        }
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
