package com.hollowKnight.view.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class TiledParticleManager implements Disposable {

    private static final String DEFAULT_EFFECT_PATH = "particle/m-particle.p";
    private static final String DEFAULT_IMAGE_DIR = "particle";

    private static final int GLOBAL_PARTICLE_COUNT = 3;

    private static final float DEFAULT_SCALE = 0.7f;
    private static final float CAMERA_MARGIN = 140f;
    private static final float MIN_MAP_SIZE = 1f;

    private final Array<GlobalParticle> particles = new Array<>();
    private final float mapPixelWidth;
    private final float mapPixelHeight;

    private final Rectangle cameraBounds = new Rectangle();
    private final Rectangle spawnBounds = new Rectangle();

    private static class GlobalParticle {
        ParticleEffect effect;
        Vector2 position = new Vector2();

        GlobalParticle(ParticleEffect effect) {
            this.effect = effect;
        }
    }

    public TiledParticleManager(TiledMap map, String activeMapPath, float mapPixelWidth, float mapPixelHeight) {
        this.mapPixelWidth = Math.max(MIN_MAP_SIZE, mapPixelWidth);
        this.mapPixelHeight = Math.max(MIN_MAP_SIZE, mapPixelHeight);

        createGlobalParticles();
    }

    private void createGlobalParticles() {
        FileHandle effectFile = Gdx.files.internal(DEFAULT_EFFECT_PATH);
        FileHandle imagesDirectory = Gdx.files.internal(DEFAULT_IMAGE_DIR);

        for (int i = 0; i < GLOBAL_PARTICLE_COUNT; i++) {
            ParticleEffect effect = new ParticleEffect();
            effect.load(effectFile, imagesDirectory);
            if (Math.abs(DEFAULT_SCALE - 1f) > 0.001f) {
                effect.scaleEffect(DEFAULT_SCALE);
            }
            effect.start();
            particles.add(new GlobalParticle(effect));
        }
    }

    public void draw(SpriteBatch batch, float delta, Camera camera, Rectangle currentRoom, boolean frozen) {
        if (batch == null || camera == null || particles.size == 0) {
            return;
        }

        float updateDelta = frozen ? 0f : delta;
        getCameraBounds(camera, CAMERA_MARGIN, cameraBounds);
        clampToMap(cameraBounds, spawnBounds);

        for (GlobalParticle particle : particles) {
            if (particle.effect == null) {
                continue;
            }

            if (particle.position.isZero() || !cameraBounds.contains(particle.position.x, particle.position.y)) {
                placeRandomlyInBounds(particle, spawnBounds);
            }

            particle.effect.draw(batch, updateDelta);

            if (!frozen && particle.effect.isComplete()) {
                particle.effect.reset();
                placeRandomlyInBounds(particle, spawnBounds);
                particle.effect.start();
            }
        }

        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void placeRandomlyInBounds(GlobalParticle particle, Rectangle bounds) {
        if (particle == null || particle.effect == null || bounds == null) {
            return;
        }

        float x = MathUtils.random(bounds.x, bounds.x + Math.max(1f, bounds.width));
        float y = MathUtils.random(bounds.y, bounds.y + Math.max(1f, bounds.height));

        particle.position.set(x, y);
        particle.effect.setPosition(x, y);
    }

    private void getCameraBounds(Camera camera, float margin, Rectangle out) {
        float halfWidth = camera.viewportWidth / 2f;
        float halfHeight = camera.viewportHeight / 2f;

        if (camera instanceof OrthographicCamera) {
            OrthographicCamera orthographicCamera = (OrthographicCamera) camera;
            halfWidth *= orthographicCamera.zoom;
            halfHeight *= orthographicCamera.zoom;
        }

        out.set(
            camera.position.x - halfWidth - margin,
            camera.position.y - halfHeight - margin,
            halfWidth * 2f + margin * 2f,
            halfHeight * 2f + margin * 2f
        );
    }

    private void clampToMap(Rectangle bounds, Rectangle out) {
        float x1 = MathUtils.clamp(bounds.x, 0f, mapPixelWidth);
        float y1 = MathUtils.clamp(bounds.y, 0f, mapPixelHeight);
        float x2 = MathUtils.clamp(bounds.x + bounds.width, 0f, mapPixelWidth);
        float y2 = MathUtils.clamp(bounds.y + bounds.height, 0f, mapPixelHeight);

        if (x2 <= x1) {
            x1 = MathUtils.clamp(bounds.x, 0f, Math.max(0f, mapPixelWidth - 1f));
            x2 = Math.min(mapPixelWidth, x1 + 1f);
        }

        if (y2 <= y1) {
            y1 = MathUtils.clamp(bounds.y, 0f, Math.max(0f, mapPixelHeight - 1f));
            y2 = Math.min(mapPixelHeight, y1 + 1f);
        }

        out.set(x1, y1, x2 - x1, y2 - y1);
    }

    @Override
    public void dispose() {
        for (GlobalParticle particle : particles) {
            if (particle.effect != null) {
                particle.effect.dispose();
            }
        }

        particles.clear();
    }
}
