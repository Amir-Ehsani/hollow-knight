package com.hollowKnight.view.effects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class CombatEffectManager implements Disposable {

    private static final String EFFECTS_DIR = "Hollow Knight sprites/Particles & Effects/";

    private static final String WHITE_HIT_PARTICLE = EFFECTS_DIR + "white_hit_particle.png";
    private static final String HIT_CRACK_PREFIX = EFFECTS_DIR + "hit_crack_simple";
    private static final String NAIL_TERRAIN_PREFIX = EFFECTS_DIR + "nail_terrain_hit_effect";

    private static final int HIT_CRACK_FRAME_COUNT = 3;
    private static final int NAIL_TERRAIN_FRAME_COUNT = 4;

    private static final float ENEMY_HIT_PARTICLE_LIFE_MIN = 0.22f;
    private static final float ENEMY_HIT_PARTICLE_LIFE_MAX = 0.42f;
    private static final int ENEMY_HIT_PARTICLE_COUNT = 9;

    private final Array<Texture> ownedTextures = new Array<>();
    private final Array<WhiteHitParticle> whiteParticles = new Array<>();
    private final Array<OneShotAnimation> oneShotAnimations = new Array<>();

    private Texture whiteHitTexture;
    private Animation<TextureRegion> hitCrackAnimation;
    private Animation<TextureRegion> nailTerrainAnimation;

    private static class WhiteHitParticle {
        float x;
        float y;
        float velocityX;
        float velocityY;
        float life;
        float maxLife;
        float scale;
        float rotation;
        float rotationSpeed;
    }

    private static class OneShotAnimation {
        Animation<TextureRegion> animation;
        float x;
        float y;
        float stateTime;
        float scale;
        float rotation;
        boolean flipX;

        OneShotAnimation(Animation<TextureRegion> animation, float x, float y, float scale, float rotation, boolean flipX) {
            this.animation = animation;
            this.x = x;
            this.y = y;
            this.scale = scale;
            this.rotation = rotation;
            this.flipX = flipX;
            this.stateTime = 0f;
        }
    }

    public CombatEffectManager() {
        whiteHitTexture = loadTextureIfExists(WHITE_HIT_PARTICLE);
        hitCrackAnimation = loadNumberedAnimation(HIT_CRACK_PREFIX, HIT_CRACK_FRAME_COUNT, 24f);
        nailTerrainAnimation = loadNumberedAnimation(NAIL_TERRAIN_PREFIX, NAIL_TERRAIN_FRAME_COUNT, 26f);
    }

    public void spawnEnemyHit(float centerX, float centerY) {
        if (whiteHitTexture == null) {
            return;
        }

        for (int i = 0; i < ENEMY_HIT_PARTICLE_COUNT; i++) {
            WhiteHitParticle particle = new WhiteHitParticle();
            float angle = MathUtils.random(0f, MathUtils.PI2);
            float speed = MathUtils.random(70f, 220f);

            particle.x = centerX + MathUtils.random(-8f, 8f);
            particle.y = centerY + MathUtils.random(-6f, 8f);
            particle.velocityX = MathUtils.cos(angle) * speed;
            particle.velocityY = MathUtils.sin(angle) * speed + MathUtils.random(20f, 80f);
            particle.maxLife = MathUtils.random(ENEMY_HIT_PARTICLE_LIFE_MIN, ENEMY_HIT_PARTICLE_LIFE_MAX);
            particle.life = particle.maxLife;
            particle.scale = MathUtils.random(0.28f, 0.55f);
            particle.rotation = MathUtils.random(0f, 360f);
            particle.rotationSpeed = MathUtils.random(-480f, 480f);

            whiteParticles.add(particle);
        }
    }

    public void spawnEnemyDeath(float centerX, float centerY) {
        if (hitCrackAnimation != null) {
            oneShotAnimations.add(new OneShotAnimation(
                hitCrackAnimation,
                centerX,
                centerY,
                0.82f,
                MathUtils.random(-10f, 10f),
                false
            ));
        }
    }

    public void spawnNailTerrainHit(float centerX, float centerY, float rotation, boolean flipX) {
        if (nailTerrainAnimation == null) {
            return;
        }

        oneShotAnimations.add(new OneShotAnimation(
            nailTerrainAnimation,
            centerX,
            centerY,
            0.70f,
            rotation,
            flipX
        ));
    }

    public void draw(SpriteBatch batch, float delta) {
        updateAndDrawWhiteParticles(batch, delta);
        updateAndDrawOneShotAnimations(batch, delta);
    }

    private void updateAndDrawWhiteParticles(SpriteBatch batch, float delta) {
        if (whiteHitTexture == null) {
            return;
        }

        for (int i = whiteParticles.size - 1; i >= 0; i--) {
            WhiteHitParticle particle = whiteParticles.get(i);
            particle.life -= delta;

            if (particle.life <= 0f) {
                whiteParticles.removeIndex(i);
                continue;
            }

            particle.velocityY -= 520f * delta;
            particle.x += particle.velocityX * delta;
            particle.y += particle.velocityY * delta;
            particle.rotation += particle.rotationSpeed * delta;

            float alpha = MathUtils.clamp(particle.life / particle.maxLife, 0f, 1f);
            float width = whiteHitTexture.getWidth() * particle.scale;
            float height = whiteHitTexture.getHeight() * particle.scale;

            batch.setColor(1f, 1f, 1f, alpha);
            batch.draw(
                whiteHitTexture,
                particle.x - width / 2f,
                particle.y - height / 2f,
                width / 2f,
                height / 2f,
                width,
                height,
                1f,
                1f,
                particle.rotation,
                0,
                0,
                whiteHitTexture.getWidth(),
                whiteHitTexture.getHeight(),
                false,
                false
            );
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    private void updateAndDrawOneShotAnimations(SpriteBatch batch, float delta) {
        for (int i = oneShotAnimations.size - 1; i >= 0; i--) {
            OneShotAnimation effect = oneShotAnimations.get(i);
            effect.stateTime += delta;

            if (effect.animation.isAnimationFinished(effect.stateTime)) {
                oneShotAnimations.removeIndex(i);
                continue;
            }

            TextureRegion frame = effect.animation.getKeyFrame(effect.stateTime);

            float width = frame.getRegionWidth() * effect.scale;
            float height = frame.getRegionHeight() * effect.scale;

            batch.draw(
                frame.getTexture(),
                effect.x - width / 2f,
                effect.y - height / 2f,
                width / 2f,
                height / 2f,
                width,
                height,
                1f,
                1f,
                effect.rotation,
                frame.getRegionX(),
                frame.getRegionY(),
                frame.getRegionWidth(),
                frame.getRegionHeight(),
                effect.flipX,
                false
            );
        }
    }

    private Animation<TextureRegion> loadNumberedAnimation(String prefix, int frameCount, float fps) {
        Array<TextureRegion> frames = new Array<>();

        for (int i = 0; i < frameCount; i++) {
            String path = prefix + String.format("%04d.png", i);
            Texture texture = loadTextureIfExists(path);

            if (texture != null) {
                frames.add(new TextureRegion(texture));
            }
        }

        if (frames.size == 0) {
            return null;
        }

        return new Animation<>(1f / fps, frames, Animation.PlayMode.NORMAL);
    }

    private Texture loadTextureIfExists(String path) {
        FileHandle file = Gdx.files.internal(path);

        if (!file.exists()) {
            System.out.println("Combat effect asset not found: " + path);
            return null;
        }

        Texture texture = new Texture(file);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        ownedTextures.add(texture);
        return texture;
    }

    @Override
    public void dispose() {
        for (Texture texture : ownedTextures) {
            if (texture != null) {
                texture.dispose();
            }
        }

        ownedTextures.clear();
        whiteParticles.clear();
        oneShotAnimations.clear();
    }
}
