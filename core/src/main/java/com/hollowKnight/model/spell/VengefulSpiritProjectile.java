package com.hollowKnight.model.spell;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectSet;
import com.hollowKnight.model.Damageable;

public class VengefulSpiritProjectile {
    public static final float DEFAULT_WIDTH = 176f;
    public static final float DEFAULT_HEIGHT = 92f;
    public static final float DEFAULT_SPEED = 820f;
    public static final float DEFAULT_LIFETIME = 1.8f;
    public static final int DEFAULT_DAMAGE = 2;

    private static final float COLLISION_WIDTH = 124f;
    private static final float COLLISION_HEIGHT = 46f;
    private static final float TERRAIN_COLLISION_GRACE = 0.10f;

    private final Vector2 position;
    private final Rectangle bounds;
    private final Rectangle collisionBounds;
    private final int direction;
    private final float speed;
    private final float lifetime;
    private final int damage;
    private final ObjectSet<Damageable> damagedEnemies;

    private float age;
    private boolean active;

    public VengefulSpiritProjectile(float x, float y, int direction) {
        this(x, y, direction, DEFAULT_SPEED, DEFAULT_LIFETIME, DEFAULT_DAMAGE);
    }

    public VengefulSpiritProjectile(
        float x,
        float y,
        int direction,
        float speed,
        float lifetime,
        int damage
    ) {
        this.position = new Vector2(x, y);
        this.bounds = new Rectangle(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        this.collisionBounds = new Rectangle();
        this.direction = direction < 0 ? -1 : 1;
        this.speed = Math.max(1f, speed);
        this.lifetime = Math.max(0.05f, lifetime);
        this.damage = Math.max(1, damage);
        this.damagedEnemies = new ObjectSet<>();
        this.age = 0f;
        this.active = true;
        updateBounds();
    }

    public void update(float delta) {
        if (!active || delta <= 0f) {
            return;
        }

        age += delta;
        position.x += direction * speed * delta;
        updateBounds();

        if (age >= lifetime) {
            active = false;
        }
    }

    public void deactivate() {
        active = false;
    }

    public boolean canDamage(Damageable enemy) {
        return active && enemy != null && enemy.isAlive() && !damagedEnemies.contains(enemy);
    }

    public void markDamaged(Damageable enemy) {
        if (enemy != null) {
            damagedEnemies.add(enemy);
        }
    }

    private void updateBounds() {
        bounds.set(position.x, position.y, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        collisionBounds.set(
            position.x + (DEFAULT_WIDTH - COLLISION_WIDTH) / 2f,
            position.y + (DEFAULT_HEIGHT - COLLISION_HEIGHT) / 2f,
            COLLISION_WIDTH,
            COLLISION_HEIGHT
        );
    }

    public Vector2 getPosition() {
        return position;
    }

    public Rectangle getBounds() {
        updateBounds();
        return bounds;
    }

    public Rectangle getCollisionBounds() {
        updateBounds();
        return collisionBounds;
    }

    public boolean canHitTerrain() {
        return active && age >= TERRAIN_COLLISION_GRACE;
    }

    public int getDirection() {
        return direction;
    }

    public int getDamage() {
        return damage;
    }

    public float getAge() {
        return age;
    }

    public boolean isActive() {
        return active;
    }
}
