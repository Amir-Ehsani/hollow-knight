package com.hollowKnight.model.spell;

import com.badlogic.gdx.math.Rectangle;

public class HowlingWraithsCast {
    public static final float DEFAULT_WIDTH = 230f;
    public static final float DEFAULT_HEIGHT = 270f;
    public static final float DEFAULT_DURATION = 0.66f;
    public static final int DEFAULT_DAMAGE_PER_TICK = 1;

    private static final float[] TICK_TIMES = {0.10f, 0.29f, 0.48f};

    private final Rectangle bounds;
    private final float duration;
    private final int damagePerTick;

    private float age;
    private int nextTickIndex;
    private int pendingTicks;
    private boolean active;

    public HowlingWraithsCast(float centerX, float bottomY) {
        this(centerX, bottomY, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_DURATION, DEFAULT_DAMAGE_PER_TICK);
    }

    public HowlingWraithsCast(
        float centerX,
        float bottomY,
        float width,
        float height,
        float duration,
        int damagePerTick
    ) {
        float safeWidth = Math.max(32f, width);
        float safeHeight = Math.max(48f, height);
        this.bounds = new Rectangle(centerX - safeWidth / 2f, bottomY, safeWidth, safeHeight);
        this.duration = Math.max(TICK_TIMES[TICK_TIMES.length - 1] + 0.05f, duration);
        this.damagePerTick = Math.max(1, damagePerTick);
        this.age = 0f;
        this.nextTickIndex = 0;
        this.pendingTicks = 0;
        this.active = true;
    }

    public void update(float delta) {
        if (!active || delta <= 0f) {
            return;
        }

        float previousAge = age;
        age += delta;

        while (nextTickIndex < TICK_TIMES.length &&
            TICK_TIMES[nextTickIndex] > previousAge &&
            TICK_TIMES[nextTickIndex] <= age) {
            pendingTicks++;
            nextTickIndex++;
        }

        while (nextTickIndex < TICK_TIMES.length && TICK_TIMES[nextTickIndex] <= age) {
            pendingTicks++;
            nextTickIndex++;
        }

        if (age >= duration) {
            active = false;
        }
    }

    public int consumePendingTicks() {
        int result = pendingTicks;
        pendingTicks = 0;
        return result;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public int getDamagePerTick() {
        return damagePerTick;
    }

    public float getAge() {
        return age;
    }

    public float getDuration() {
        return duration;
    }

    public boolean isActive() {
        return active || pendingTicks > 0;
    }
}
