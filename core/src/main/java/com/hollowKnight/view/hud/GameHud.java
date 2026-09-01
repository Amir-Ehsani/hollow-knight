package com.hollowKnight.view.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hollowKnight.model.Knight;

public class GameHud implements Disposable {

    private static class BreakMaskAnim {
        int slotIndex;
        float stateTime;

        BreakMaskAnim(int slotIndex) {
            this.slotIndex = slotIndex;
            this.stateTime = 0f;
        }
    }

    private static class RefillMaskAnim {
        int slotIndex;
        float stateTime;

        RefillMaskAnim(int slotIndex) {
            this.slotIndex = slotIndex;
            this.stateTime = 0f;
        }
    }

    private final Knight knight;

    private final OrthographicCamera camera;
    private final ScreenViewport viewport;
    private final SpriteBatch batch;

    private Texture healthBarTexture;
    private Texture filledHealthTexture;
    private Texture emptyHealthTexture;
    private Texture breakHealthTexture;
    private Texture healthRefillTexture;
    private Texture soulFillTexture;
    private Texture soulShellOverlayTexture;

    private TextureRegion soulShellFrame;
    private TextureRegion soulShellOverlayRegion;
    private TextureRegion filledHealthRegion;
    private TextureRegion emptyHealthRegion;

    private Animation<TextureRegion> breakHealthAnimation;
    private Animation<TextureRegion> refillHealthAnimation;

    private final Array<BreakMaskAnim> breakMaskAnims = new Array<>();
    private final Array<RefillMaskAnim> refillMaskAnims = new Array<>();

    private int lastHealth;
    private float visualSoul;

    private static final int SOUL_SHELL_FRAME_WIDTH = 257;
    private static final int SOUL_SHELL_FRAME_HEIGHT = 164;
    private static final int SOUL_SHELL_FRAME_COUNT = 6;
    private static final int SOUL_SHELL_IDLE_FRAME_INDEX = 5;

    private static final int HEALTH_FRAME_WIDTH = 126;
    private static final int HEALTH_FRAME_HEIGHT = 167;
    private static final int BREAK_HEALTH_FRAME_COUNT = 6;
    private static final int REFILL_HEALTH_FRAME_COUNT = 5;

    private static final int HEALTH_MASK_CROP_X = 35;
    private static final int HEALTH_MASK_CROP_Y = 38;
    private static final int HEALTH_MASK_CROP_HEIGHT = 69;

    private static final float HUD_LEFT = 26f;
    private static final float HUD_TOP_PADDING = 26f;

    private static final float SOUL_SHELL_DRAW_WIDTH = 165f;
    private static final float SOUL_SHELL_DRAW_HEIGHT = 105f;

    private static final float SOUL_FILL_OFFSET_X = 7f;
    private static final float SOUL_FILL_OFFSET_Y = 5f;
    private static final float SOUL_FILL_DRAW_WIDTH = 76f;
    private static final float SOUL_FILL_DRAW_HEIGHT = 76f;

    private static final float HEALTH_START_X = 118f;
    private static final float HEALTH_TOP_PADDING = 45f;
    private static final float HEALTH_MASK_DRAW_WIDTH = 34f;
    private static final float HEALTH_MASK_DRAW_HEIGHT = 42f;
    private static final float HEALTH_MASK_GAP = 8f;

    private static final float SOUL_DECREASE_SPEED = 3.5f;
    private static final float SOUL_INCREASE_SPEED = 8f;

    public GameHud(Knight knight) {
        this.knight = knight;

        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        batch = new SpriteBatch();

        loadTextures();

        lastHealth = getClampedHealth();
        visualSoul = getClampedSoul();
    }

    private void loadTextures() {
        FileHandle healthBarFile = Gdx.files.internal("HealthBar.png");

        healthBarTexture = loadTexture(healthBarFile);
        filledHealthTexture = loadTexture(Gdx.files.internal("FilledHealth.png"));
        emptyHealthTexture = loadTexture(Gdx.files.internal("EmptyHealth.png"));
        breakHealthTexture = loadTexture(Gdx.files.internal("BreakHealth.png"));
        healthRefillTexture = loadTexture(Gdx.files.internal("HealthRefill.png"));

        Array<TextureRegion> soulShellFrames = splitHorizontalFrames(
            healthBarTexture,
            SOUL_SHELL_FRAME_WIDTH,
            SOUL_SHELL_FRAME_HEIGHT,
            SOUL_SHELL_FRAME_COUNT
        );

        soulShellFrame = soulShellFrames.get(
            MathUtils.clamp(SOUL_SHELL_IDLE_FRAME_INDEX, 0, soulShellFrames.size - 1)
        );

        soulFillTexture = createSoulFillTexture();
        soulShellOverlayTexture = createSoulShellOverlayTexture(healthBarFile);
        soulShellOverlayRegion = new TextureRegion(soulShellOverlayTexture);

        filledHealthRegion = new TextureRegion(filledHealthTexture);
        emptyHealthRegion = new TextureRegion(emptyHealthTexture);

        Array<TextureRegion> breakFrames = splitHorizontalFrames(
            breakHealthTexture,
            HEALTH_FRAME_WIDTH,
            HEALTH_FRAME_HEIGHT,
            BREAK_HEALTH_FRAME_COUNT
        );

        breakHealthAnimation = new Animation<>(
            1f / 18f,
            breakFrames,
            Animation.PlayMode.NORMAL
        );

        Array<TextureRegion> refillFrames = splitHorizontalFrames(
            healthRefillTexture,
            HEALTH_FRAME_WIDTH,
            HEALTH_FRAME_HEIGHT,
            REFILL_HEALTH_FRAME_COUNT
        );

        refillHealthAnimation = new Animation<>(
            1f / 16f,
            refillFrames,
            Animation.PlayMode.NORMAL
        );
    }

    private Texture createSoulFillTexture() {
        int size = 256;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float cx = size * 0.50f;
                float cy = size * 0.50f;
                float rx = size * 0.485f;
                float ry = size * 0.485f;

                float dx = (x + 0.5f - cx) / rx;
                float dy = (y + 0.5f - cy) / ry;
                float d = dx * dx + dy * dy;

                if (d >= 1f) {
                    pixmap.drawPixel(x, y, 0x00000000);
                    continue;
                }

                float alpha = 1f;

                if (d > 0.88f) {
                    alpha = (1f - d) / 0.12f;
                    alpha = MathUtils.clamp(alpha, 0f, 1f);
                    alpha = alpha * alpha * (3f - 2f * alpha);
                }

                float vertical = y / (float) (size - 1);

                float r = 0.92f + vertical * 0.05f;
                float g = 0.88f + vertical * 0.05f;
                float b = 0.94f + vertical * 0.04f;
                float a = 0.96f * alpha;

                pixmap.drawPixel(x, y, Color.rgba8888(r, g, b, a));
            }
        }

        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        pixmap.dispose();

        return texture;
    }

    private Texture createSoulShellOverlayTexture(FileHandle healthBarFile) {
        Pixmap source = new Pixmap(healthBarFile);
        Pixmap overlay = new Pixmap(
            SOUL_SHELL_FRAME_WIDTH,
            SOUL_SHELL_FRAME_HEIGHT,
            Pixmap.Format.RGBA8888
        );

        overlay.setBlending(Pixmap.Blending.None);

        int frameStartX = SOUL_SHELL_IDLE_FRAME_INDEX * SOUL_SHELL_FRAME_WIDTH;
        Color color = new Color();

        for (int y = 0; y < SOUL_SHELL_FRAME_HEIGHT; y++) {
            for (int x = 0; x < SOUL_SHELL_FRAME_WIDTH; x++) {
                int pixel = source.getPixel(frameStartX + x, y);
                Color.rgba8888ToColor(color, pixel);

                if (color.a <= 0.01f) {
                    overlay.drawPixel(x, y, 0x00000000);
                    continue;
                }

                float holeAlpha = getSoulShellHoleAlpha(x, y);
                color.a *= holeAlpha;

                if (color.a <= 0.01f) {
                    overlay.drawPixel(x, y, 0x00000000);
                } else {
                    overlay.drawPixel(x, y, Color.rgba8888(color));
                }
            }
        }

        Texture texture = new Texture(overlay);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        source.dispose();
        overlay.dispose();

        return texture;
    }

    private float getSoulShellHoleAlpha(int x, int y) {
        float cx = 70f;
        float cy = 99f;
        float rx = 58f;
        float ry = 58f;

        float dx = (x + 0.5f - cx) / rx;
        float dy = (y + 0.5f - cy) / ry;
        float d = dx * dx + dy * dy;

        if (d < 0.74f) {
            return 0f;
        }

        if (d > 0.91f) {
            return 1f;
        }

        float t = (d - 0.74f) / 0.17f;
        t = MathUtils.clamp(t, 0f, 1f);

        return t * t * (3f - 2f * t);
    }

    private Texture loadTexture(FileHandle file) {
        Texture texture = new Texture(file);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    private Array<TextureRegion> splitHorizontalFrames(
        Texture texture,
        int frameWidth,
        int frameHeight,
        int wantedFrameCount
    ) {
        TextureRegion[][] split = TextureRegion.split(texture, frameWidth, frameHeight);
        Array<TextureRegion> frames = new Array<>();

        for (int row = 0; row < split.length; row++) {
            for (int col = 0; col < split[row].length; col++) {
                if (frames.size >= wantedFrameCount) {
                    return frames;
                }

                frames.add(split[row][col]);
            }
        }

        if (frames.size == 0) {
            throw new RuntimeException("Could not split HUD texture frames.");
        }

        return frames;
    }

    public void update(float delta) {
        updateSoul(delta);
        updateHealth(delta);
    }

    private void updateSoul(float delta) {
        int targetSoul = getClampedSoul();

        float speed = targetSoul < visualSoul
            ? SOUL_DECREASE_SPEED
            : SOUL_INCREASE_SPEED;

        visualSoul = MathUtils.lerp(
            visualSoul,
            targetSoul,
            MathUtils.clamp(delta * speed, 0f, 1f)
        );

        if (Math.abs(visualSoul - targetSoul) < 0.05f) {
            visualSoul = targetSoul;
        }
    }

    private void updateHealth(float delta) {
        int currentHealth = getClampedHealth();
        int maxHealth = Math.max(0, knight.getMaxHealth());

        if (currentHealth != lastHealth) {
            if (currentHealth < lastHealth) {
                for (int slot = currentHealth; slot < lastHealth && slot < maxHealth; slot++) {
                    removeRefillAnimation(slot);
                    startBreakAnimation(slot);
                }
            } else {
                for (int slot = lastHealth; slot < currentHealth && slot < maxHealth; slot++) {
                    removeBreakAnimation(slot);
                    startRefillAnimation(slot);
                }
            }

            lastHealth = currentHealth;
        }

        for (int i = breakMaskAnims.size - 1; i >= 0; i--) {
            BreakMaskAnim anim = breakMaskAnims.get(i);

            if (anim.slotIndex < currentHealth) {
                breakMaskAnims.removeIndex(i);
                continue;
            }

            anim.stateTime += delta;

            if (breakHealthAnimation.isAnimationFinished(anim.stateTime)) {
                breakMaskAnims.removeIndex(i);
            }
        }

        for (int i = refillMaskAnims.size - 1; i >= 0; i--) {
            RefillMaskAnim anim = refillMaskAnims.get(i);

            if (anim.slotIndex >= currentHealth) {
                refillMaskAnims.removeIndex(i);
                continue;
            }

            anim.stateTime += delta;

            if (refillHealthAnimation.isAnimationFinished(anim.stateTime)) {
                refillMaskAnims.removeIndex(i);
            }
        }
    }

    private void startBreakAnimation(int slotIndex) {
        removeBreakAnimation(slotIndex);
        breakMaskAnims.add(new BreakMaskAnim(slotIndex));
    }

    private void removeBreakAnimation(int slotIndex) {
        for (int i = breakMaskAnims.size - 1; i >= 0; i--) {
            if (breakMaskAnims.get(i).slotIndex == slotIndex) {
                breakMaskAnims.removeIndex(i);
            }
        }
    }

    private void startRefillAnimation(int slotIndex) {
        removeRefillAnimation(slotIndex);
        refillMaskAnims.add(new RefillMaskAnim(slotIndex));
    }

    private void removeRefillAnimation(int slotIndex) {
        for (int i = refillMaskAnims.size - 1; i >= 0; i--) {
            if (refillMaskAnims.get(i).slotIndex == slotIndex) {
                refillMaskAnims.removeIndex(i);
            }
        }
    }

    public void draw() {
        viewport.apply();
        camera.update();

        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.setColor(Color.WHITE);

        drawSoulOrb();
        drawHealthMasks();

        batch.setColor(Color.WHITE);
        batch.end();
    }

    private void drawSoulOrb() {
        float scale = getHudScale();

        float soulX = HUD_LEFT * scale;
        float soulY = viewport.getWorldHeight()
            - HUD_TOP_PADDING * scale
            - SOUL_SHELL_DRAW_HEIGHT * scale;

        float shellWidth = SOUL_SHELL_DRAW_WIDTH * scale;
        float shellHeight = SOUL_SHELL_DRAW_HEIGHT * scale;

        batch.setColor(Color.WHITE);
        batch.draw(soulShellFrame, soulX, soulY, shellWidth, shellHeight);

        drawSoulFill(soulX, soulY, scale);

        batch.setColor(Color.WHITE);
        batch.draw(soulShellOverlayRegion, soulX, soulY, shellWidth, shellHeight);

        batch.setColor(Color.WHITE);
    }

    private void drawSoulFill(float soulX, float soulY, float scale) {
        float soulPercent = getSoulPercent();

        if (soulPercent <= 0.01f) {
            return;
        }

        float fillX = soulX + SOUL_FILL_OFFSET_X * scale;
        float fillY = soulY + SOUL_FILL_OFFSET_Y * scale;
        float fillWidth = SOUL_FILL_DRAW_WIDTH * scale;
        float fillHeight = SOUL_FILL_DRAW_HEIGHT * scale;
        float visibleHeight = fillHeight * soulPercent;

        Rectangle clipBounds = new Rectangle(
            fillX,
            fillY,
            fillWidth,
            visibleHeight
        );

        Rectangle scissors = new Rectangle();

        batch.flush();
        ScissorStack.calculateScissors(camera, batch.getTransformMatrix(), clipBounds, scissors);

        if (ScissorStack.pushScissors(scissors)) {
            batch.setColor(Color.WHITE);
            batch.draw(soulFillTexture, fillX, fillY, fillWidth, fillHeight);
            batch.flush();
            ScissorStack.popScissors();
        }

        batch.setColor(Color.WHITE);
    }

    private void drawHealthMasks() {
        float scale = getHudScale();

        int currentHealth = getClampedHealth();
        int maxHealth = Math.max(0, knight.getMaxHealth());

        float visibleMaskBottomY = viewport.getWorldHeight()
            - HEALTH_TOP_PADDING * scale
            - HEALTH_MASK_DRAW_HEIGHT * scale;

        for (int i = 0; i < maxHealth; i++) {
            drawHealthFrame(emptyHealthRegion, i, visibleMaskBottomY, scale, 1f);
        }

        for (int i = 0; i < currentHealth; i++) {
            if (hasActiveRefillAnimation(i)) {
                continue;
            }

            drawHealthFrame(filledHealthRegion, i, visibleMaskBottomY, scale, 1f);
        }

        for (RefillMaskAnim anim : refillMaskAnims) {
            if (anim.slotIndex < 0 || anim.slotIndex >= currentHealth || anim.slotIndex >= maxHealth) {
                continue;
            }

            TextureRegion frame = refillHealthAnimation.getKeyFrame(anim.stateTime);
            drawHealthFrame(frame, anim.slotIndex, visibleMaskBottomY, scale, 1f);
        }

        for (BreakMaskAnim anim : breakMaskAnims) {
            if (anim.slotIndex < currentHealth || anim.slotIndex >= maxHealth) {
                continue;
            }

            TextureRegion frame = breakHealthAnimation.getKeyFrame(anim.stateTime);
            drawHealthFrame(frame, anim.slotIndex, visibleMaskBottomY, scale, 1f);
        }

        batch.setColor(Color.WHITE);
    }

    private void drawHealthFrame(
        TextureRegion frame,
        int slotIndex,
        float visibleMaskBottomY,
        float scale,
        float alpha
    ) {
        float frameScale = (HEALTH_MASK_DRAW_HEIGHT * scale) / HEALTH_MASK_CROP_HEIGHT;

        float drawWidth = HEALTH_FRAME_WIDTH * frameScale;
        float drawHeight = HEALTH_FRAME_HEIGHT * frameScale;

        float visibleMaskX = getHealthSlotX(slotIndex, scale);

        float drawX = visibleMaskX - HEALTH_MASK_CROP_X * frameScale;
        float drawY = visibleMaskBottomY
            - (HEALTH_FRAME_HEIGHT - HEALTH_MASK_CROP_Y - HEALTH_MASK_CROP_HEIGHT) * frameScale;

        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(frame, drawX, drawY, drawWidth, drawHeight);
        batch.setColor(Color.WHITE);
    }

    private boolean hasActiveRefillAnimation(int slotIndex) {
        for (RefillMaskAnim anim : refillMaskAnims) {
            if (anim.slotIndex == slotIndex) {
                return true;
            }
        }

        return false;
    }

    private float getHealthSlotX(int slotIndex, float scale) {
        return (HEALTH_START_X + slotIndex * (HEALTH_MASK_DRAW_WIDTH + HEALTH_MASK_GAP)) * scale;
    }

    private float getSoulPercent() {
        int maxSoul = knight.getMaxSoul();

        if (maxSoul <= 0) {
            return 0f;
        }

        return MathUtils.clamp(visualSoul / (float) maxSoul, 0f, 1f);
    }

    private int getClampedSoul() {
        return MathUtils.clamp(
            knight.getCurrentSoul(),
            0,
            Math.max(0, knight.getMaxSoul())
        );
    }

    private int getClampedHealth() {
        return MathUtils.clamp(
            knight.getCurrentHealth(),
            0,
            Math.max(0, knight.getMaxHealth())
        );
    }

    private float getHudScale() {
        float height = viewport.getWorldHeight();

        if (height <= 0f) {
            return 1f;
        }

        return MathUtils.clamp(height / 720f, 0.85f, 1.25f);
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }

        if (healthBarTexture != null) {
            healthBarTexture.dispose();
        }

        if (filledHealthTexture != null) {
            filledHealthTexture.dispose();
        }

        if (emptyHealthTexture != null) {
            emptyHealthTexture.dispose();
        }

        if (breakHealthTexture != null) {
            breakHealthTexture.dispose();
        }

        if (healthRefillTexture != null) {
            healthRefillTexture.dispose();
        }

        if (soulFillTexture != null) {
            soulFillTexture.dispose();
        }

        if (soulShellOverlayTexture != null) {
            soulShellOverlayTexture.dispose();
        }
    }
}
