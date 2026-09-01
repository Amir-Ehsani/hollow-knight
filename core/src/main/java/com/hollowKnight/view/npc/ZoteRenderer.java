package com.hollowKnight.view.npc;

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
import com.hollowKnight.model.npc.Zote;

public class ZoteRenderer implements Disposable {

    private static final String BASE_PATH = "animations/animation/Zote/";
    private static final boolean SOURCE_FRAMES_FACE_RIGHT = false;

    private final Array<Texture> textures = new Array<>();

    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> talkAnimation;
    private Animation<TextureRegion> attackAnimation;
    private Animation<TextureRegion> rollAnimation;
    private Animation<TextureRegion> turnAnimation;
    private Animation<TextureRegion> fallAnimation;
    private Animation<TextureRegion> getUpAnimation;

    public ZoteRenderer() {
        idleAnimation = loadAnimation(5, 8f, Animation.PlayMode.LOOP, "Idle");
        talkAnimation = loadAnimation(5, 11f, Animation.PlayMode.LOOP, "Talk");
        attackAnimation = loadAnimation(4, 13f, Animation.PlayMode.NORMAL, "Attack");
        rollAnimation = loadAnimation(3, 13f, Animation.PlayMode.LOOP, "Roll");
        turnAnimation = loadAnimation(2, 11f, Animation.PlayMode.NORMAL, "Turn");
        fallAnimation = loadAnimation(5, 12f, Animation.PlayMode.NORMAL, "Fall");
        getUpAnimation = loadAnimation(4, 10f, Animation.PlayMode.NORMAL, "Get Up", "GetUp");
    }

    public void draw(SpriteBatch batch, Zote zote) {
        if (batch == null || zote == null) {
            return;
        }

        TextureRegion frame = getFrameFor(zote);
        boolean shouldFlip = SOURCE_FRAMES_FACE_RIGHT ? zote.isFacingLeft() : !zote.isFacingLeft();

        Rectangle bounds = zote.getBounds();
        float scale = zote.getDrawScale();
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

    private TextureRegion getFrameFor(Zote zote) {
        Zote.State state = zote.getState();
        float stateTime = zote.getStateTime();

        if (state == Zote.State.TALKING) {
            return talkAnimation.getKeyFrame(stateTime, true);
        }

        if (state == Zote.State.ATTACKING) {
            return attackAnimation.getKeyFrame(stateTime, false);
        }

        if (state == Zote.State.ROLLING) {
            return rollAnimation.getKeyFrame(stateTime, true);
        }

        if (state == Zote.State.TURNING) {
            return turnAnimation.getKeyFrame(stateTime, false);
        }

        if (state == Zote.State.FALLING) {
            return fallAnimation.getKeyFrame(stateTime, false);
        }

        if (state == Zote.State.GETTING_UP) {
            return getUpAnimation.getKeyFrame(stateTime, false);
        }

        return idleAnimation.getKeyFrame(stateTime, true);
    }

    private Animation<TextureRegion> loadAnimation(int sheetFrameCount, float fps, Animation.PlayMode playMode, String... baseNames) {
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
        Pixmap pixmap = new Pixmap(42, 62, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.valueOf("f2edf5ff"));
        pixmap.fillCircle(21, 43, 14);
        pixmap.setColor(Color.valueOf("0f1120ff"));
        pixmap.fillRectangle(15, 20, 12, 23);
        pixmap.setColor(Color.valueOf("f2edf5ff"));
        pixmap.drawLine(13, 53, 5, 61);
        pixmap.drawLine(29, 53, 37, 61);
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
