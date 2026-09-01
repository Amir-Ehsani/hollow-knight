package com.hollowKnight.model.enemy;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hollowKnight.model.Damageable;

public class Mosquito implements Damageable {

    public enum State {
        IDLE, ANTICIPATING, CHARGING, TURNING, DYING, DEAD
    }

    public static final float DEFAULT_HITBOX_WIDTH = 34f;
    public static final float DEFAULT_HITBOX_HEIGHT = 24f;
    public static final int DEFAULT_HEALTH = 2;
    public static final float DEFAULT_SPEED = 95f;
    public static final float DEFAULT_CHARGE_SPEED = 440f;
    public static final float DEFAULT_DETECTION_RANGE = 850f;
    public static final float DEFAULT_DRAW_SCALE = 0.58f;

    private static final float GRAVITY = -1500f;
    private static final float MOVE_STEP = 3f;
    private static final float EPS = 0.0001f;

    private static final float ANTICIPATE_DURATION = 0.45f;
    private static final float CHARGE_DURATION = 0.78f;
    private static final float RECOVER_DURATION = 0.36f;
    private static final float TURN_DURATION = 0.20f;
    private static final float ATTACK_COOLDOWN = 1.05f;
    private static final float ATTACK_START_DISTANCE = 620f;
    private static final float HURT_KNOCKBACK_DURATION = 0.15f;
    private static final float DEATH_AIR_MIN_TIME = 0.22f;
    private static final float DEATH_LAND_DURATION = 0.45f;

    private final Vector2 position;
    private final Vector2 velocity;
    private final Vector2 spawnPosition;
    private final Vector2 lockedTarget;
    private final Rectangle bounds;
    private final Rectangle probeBounds;

    private final int maxHealth;
    private int currentHealth;

    private final float speed;
    private final float chargeSpeed;
    private final float detectionRange;
    private final float drawScale;

    private int direction;
    private final int spawnDirection;
    private State state;
    private float stateTime;
    private float hurtKnockbackTimer;
    private float attackCooldownTimer;
    private float hoverTimer;
    private boolean grounded;
    private boolean landedDeath;
    private boolean hasSeenKnight;

    public Mosquito(float x, float y) {
        this(
            x,
            y,
            DEFAULT_HITBOX_WIDTH,
            DEFAULT_HITBOX_HEIGHT,
            DEFAULT_HEALTH,
            DEFAULT_SPEED,
            DEFAULT_CHARGE_SPEED,
            DEFAULT_DETECTION_RANGE,
            1,
            DEFAULT_DRAW_SCALE
        );
    }

    public Mosquito(
        float x,
        float y,
        float width,
        float height,
        int health,
        float speed,
        float chargeSpeed,
        float detectionRange,
        int direction,
        float drawScale
    ) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2();
        this.spawnPosition = new Vector2(x, y);
        this.lockedTarget = new Vector2(x, y);
        this.bounds = new Rectangle(x, y, Math.max(8f, width), Math.max(8f, height));
        this.probeBounds = new Rectangle();

        this.maxHealth = Math.max(1, health);
        this.currentHealth = this.maxHealth;
        this.speed = Math.max(10f, speed);
        this.chargeSpeed = Math.max(80f, chargeSpeed);
        this.detectionRange = Math.max(32f, detectionRange);
        this.direction = direction < 0 ? -1 : 1;
        this.spawnDirection = this.direction;
        this.drawScale = Math.max(0.1f, drawScale);

        this.state = State.IDLE;
        this.stateTime = 0f;
        this.hurtKnockbackTimer = 0f;
        this.attackCooldownTimer = 0.25f;
        this.hoverTimer = 0f;
        this.grounded = false;
        this.landedDeath = false;
        this.hasSeenKnight = false;
    }

    public void update(float delta, Rectangle knightBounds, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        if (state == State.DEAD) {
            return;
        }

        stateTime += delta;
        hoverTimer += delta;

        if (attackCooldownTimer > 0f) {
            attackCooldownTimer -= delta;
        }

        if (state == State.DYING) {
            updateDying(delta, platforms, polygonPlatforms);
            return;
        }

        if (hurtKnockbackTimer > 0f) {
            updateHurtKnockback(delta, platforms, polygonPlatforms);
            return;
        }

        if (state == State.ANTICIPATING) {
            velocity.set(0f, 0f);

            if (stateTime >= ANTICIPATE_DURATION) {
                beginCharge();
            }

            return;
        }

        if (state == State.CHARGING) {
            boolean hitHorizontal = moveAxis(velocity.x * delta, 0f, platforms, polygonPlatforms);
            boolean hitVertical = moveAxis(0f, velocity.y * delta, platforms, polygonPlatforms);

            if (hitHorizontal || hitVertical || stateTime >= CHARGE_DURATION) {
                finishCharge();
            }

            return;
        }

        if (state == State.TURNING) {
            if (stateTime >= TURN_DURATION) {
                state = State.IDLE;
                stateTime = 0f;
            }

            return;
        }

        updateIdleMovement(delta, knightBounds, platforms, polygonPlatforms);
    }

    private void updateIdleMovement(float delta, Rectangle knightBounds, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        Vector2 playerCenter = getKnightCenter(knightBounds);
        boolean canSeePlayer = playerCenter != null && isInsideDetectionRange(playerCenter);

        if (canSeePlayer) {
            hasSeenKnight = true;
        }

        boolean shouldChasePlayer = hasSeenKnight && playerCenter != null;

        if (shouldChasePlayer && attackCooldownTimer <= 0f && isInsideAttackStartDistance(playerCenter)) {
            beginAnticipation(playerCenter);
            return;
        }

        Vector2 target = new Vector2(spawnPosition.x, spawnPosition.y + (float) Math.sin(hoverTimer * 2.2f) * 12f);

        if (shouldChasePlayer) {
            target.set(playerCenter.x - bounds.width / 2f, playerCenter.y - bounds.height / 2f);
        }

        moveToward(delta, target, shouldChasePlayer ? speed : speed * 0.55f, platforms, polygonPlatforms);
    }

    private void beginAnticipation(Vector2 playerCenter) {
        lockedTarget.set(playerCenter);
        updateDirectionToward(playerCenter.x);
        state = State.ANTICIPATING;
        stateTime = 0f;
        velocity.set(0f, 0f);
    }

    private void beginCharge() {
        Vector2 center = getCenter();
        Vector2 chargeVector = new Vector2(lockedTarget).sub(center);

        if (chargeVector.len2() < 1f) {
            chargeVector.set(direction, 0f);
        }

        chargeVector.nor().scl(chargeSpeed);
        velocity.set(chargeVector);
        updateDirectionToward(lockedTarget.x);

        state = State.CHARGING;
        stateTime = 0f;
    }

    private void finishCharge() {
        velocity.set(0f, 0f);
        state = State.IDLE;
        stateTime = 0f;
        attackCooldownTimer = ATTACK_COOLDOWN;
    }

    private void updateHurtKnockback(float delta, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        hurtKnockbackTimer -= delta;

        moveAxis(velocity.x * delta, 0f, platforms, polygonPlatforms);
        moveAxis(0f, velocity.y * delta, platforms, polygonPlatforms);

        velocity.scl(Math.max(0f, 1f - delta * 4.5f));

        if (hurtKnockbackTimer <= 0f) {
            hurtKnockbackTimer = 0f;
            velocity.set(0f, 0f);
            state = State.IDLE;
            stateTime = 0f;
            attackCooldownTimer = Math.max(attackCooldownTimer, 0.25f);
        }
    }

    private void updateDying(float delta, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        if (!landedDeath) {
            velocity.y += GRAVITY * delta;

            if (Math.abs(velocity.x) > 0.01f) {
                moveAxis(velocity.x * delta, 0f, platforms, polygonPlatforms);
                velocity.x *= Math.max(0f, 1f - delta * 2.8f);
            }

            boolean hitGround = moveAxis(0f, velocity.y * delta, platforms, polygonPlatforms);

            if (hitGround && velocity.y <= 0f) {
                velocity.set(0f, 0f);
                grounded = true;
                landedDeath = true;
                stateTime = 0f;
            }

            if (position.y < -4000f) {
                landedDeath = true;
                grounded = true;
                velocity.set(0f, 0f);
                stateTime = 0f;
            }

            return;
        }

        if (stateTime >= DEATH_LAND_DURATION) {
            state = State.DEAD;
            stateTime = DEATH_LAND_DURATION;
        }
    }

    private void moveToward(float delta, Vector2 target, float moveSpeed, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        Vector2 desired = new Vector2(target.x - position.x, target.y - position.y);

        if (desired.len2() < 16f) {
            return;
        }

        desired.nor().scl(moveSpeed);
        velocity.set(desired);

        if (Math.abs(velocity.x) > 5f) {
            int newDirection = velocity.x < 0f ? -1 : 1;

            if (newDirection != direction) {
                direction = newDirection;
                state = State.TURNING;
                stateTime = 0f;
                return;
            }
        }

        boolean hitHorizontal = moveAxis(velocity.x * delta, 0f, platforms, polygonPlatforms);
        boolean hitVertical = moveAxis(0f, velocity.y * delta, platforms, polygonPlatforms);

        if (hitHorizontal) {
            velocity.x = 0f;
        }

        if (hitVertical) {
            velocity.y = 0f;
        }
    }

    private Vector2 getKnightCenter(Rectangle knightBounds) {
        if (knightBounds == null) {
            return null;
        }

        return new Vector2(knightBounds.x + knightBounds.width / 2f, knightBounds.y + knightBounds.height / 2f);
    }

    private boolean isInsideDetectionRange(Vector2 playerCenter) {
        Vector2 center = getCenter();
        float dx = playerCenter.x - center.x;
        float dy = playerCenter.y - center.y;
        return dx * dx + dy * dy <= detectionRange * detectionRange;
    }

    private boolean isInsideAttackStartDistance(Vector2 playerCenter) {
        Vector2 center = getCenter();
        float dx = playerCenter.x - center.x;
        float dy = playerCenter.y - center.y;
        float attackStartDistance = Math.max(ATTACK_START_DISTANCE, detectionRange * 0.70f);
        return dx * dx + dy * dy <= attackStartDistance * attackStartDistance;
    }

    private void updateDirectionToward(float targetX) {
        float centerX = bounds.x + bounds.width / 2f;

        if (Math.abs(targetX - centerX) > 2f) {
            direction = targetX < centerX ? -1 : 1;
        }
    }

    private boolean moveAxis(float amountX, float amountY, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        float distance = Math.max(Math.abs(amountX), Math.abs(amountY));

        if (distance <= 0f) {
            return false;
        }

        int steps = Math.max(1, (int) Math.ceil(distance / MOVE_STEP));
        float stepX = amountX / steps;
        float stepY = amountY / steps;

        for (int i = 0; i < steps; i++) {
            float oldX = position.x;
            float oldY = position.y;

            position.x += stepX;
            position.y += stepY;
            updateBounds();

            if (overlapsAny(bounds, platforms, polygonPlatforms)) {
                position.x = oldX;
                position.y = oldY;
                updateBounds();
                return true;
            }
        }

        return false;
    }

    private boolean overlapsAny(Rectangle rectangle, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        if (platforms != null) {
            for (Rectangle platform : platforms) {
                if (rectangle.overlaps(platform)) {
                    return true;
                }
            }
        }

        if (polygonPlatforms != null) {
            for (Polygon polygon : polygonPlatforms) {
                if (rectOverlapsPolygon(rectangle, polygon)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean rectOverlapsPolygon(Rectangle rect, Polygon polygon) {
        Rectangle polygonBounds = polygon.getBoundingRectangle();

        if (!rect.overlaps(polygonBounds)) {
            return false;
        }

        float[] vertices = polygon.getTransformedVertices();

        float rx1 = rect.x;
        float ry1 = rect.y;
        float rx2 = rect.x + rect.width;
        float ry2 = rect.y + rect.height;

        if (polygon.contains(rx1, ry1) ||
            polygon.contains(rx2, ry1) ||
            polygon.contains(rx2, ry2) ||
            polygon.contains(rx1, ry2)) {
            return true;
        }

        for (int i = 0; i < vertices.length; i += 2) {
            if (rect.contains(vertices[i], vertices[i + 1])) {
                return true;
            }
        }

        for (int i = 0; i < vertices.length; i += 2) {
            int next = (i + 2) % vertices.length;

            float x1 = vertices[i];
            float y1 = vertices[i + 1];
            float x2 = vertices[next];
            float y2 = vertices[next + 1];

            if (segmentsIntersect(x1, y1, x2, y2, rx1, ry1, rx2, ry1) ||
                segmentsIntersect(x1, y1, x2, y2, rx2, ry1, rx2, ry2) ||
                segmentsIntersect(x1, y1, x2, y2, rx2, ry2, rx1, ry2) ||
                segmentsIntersect(x1, y1, x2, y2, rx1, ry2, rx1, ry1)) {
                return true;
            }
        }

        return false;
    }

    private boolean segmentsIntersect(
        float ax, float ay, float bx, float by,
        float cx, float cy, float dx, float dy
    ) {
        float d1 = cross(ax, ay, bx, by, cx, cy);
        float d2 = cross(ax, ay, bx, by, dx, dy);
        float d3 = cross(cx, cy, dx, dy, ax, ay);
        float d4 = cross(cx, cy, dx, dy, bx, by);

        if (((d1 > EPS && d2 < -EPS) || (d1 < -EPS && d2 > EPS)) &&
            ((d3 > EPS && d4 < -EPS) || (d3 < -EPS && d4 > EPS))) {
            return true;
        }

        if (Math.abs(d1) <= EPS && onSegment(ax, ay, bx, by, cx, cy)) return true;
        if (Math.abs(d2) <= EPS && onSegment(ax, ay, bx, by, dx, dy)) return true;
        if (Math.abs(d3) <= EPS && onSegment(cx, cy, dx, dy, ax, ay)) return true;
        if (Math.abs(d4) <= EPS && onSegment(cx, cy, dx, dy, bx, by)) return true;

        return false;
    }

    private float cross(float ax, float ay, float bx, float by, float px, float py) {
        return (bx - ax) * (py - ay) - (by - ay) * (px - ax);
    }

    private boolean onSegment(float ax, float ay, float bx, float by, float px, float py) {
        return px >= Math.min(ax, bx) - EPS &&
            px <= Math.max(ax, bx) + EPS &&
            py >= Math.min(ay, by) - EPS &&
            py <= Math.max(ay, by) + EPS;
    }

    private void updateBounds() {
        bounds.set(position.x, position.y, bounds.width, bounds.height);
    }

    @Override
    public Rectangle getBounds() {
        updateBounds();
        return bounds;
    }

    public Vector2 getCenter() {
        return new Vector2(bounds.x + bounds.width / 2f, bounds.y + bounds.height / 2f);
    }

    @Override
    public void takeDamage(int damage) {
        takeDamage(damage, 0f, 0f);
    }

    public void takeDamage(int damage, float knockbackX, float knockbackY) {
        if (damage <= 0 || !isAlive()) {
            return;
        }

        currentHealth = Math.max(0, currentHealth - damage);

        if (currentHealth <= 0) {
            state = State.DYING;
            stateTime = 0f;
            hurtKnockbackTimer = 0f;
            landedDeath = false;
            grounded = false;
            velocity.x = knockbackX * 0.55f;
            velocity.y = Math.max(180f, knockbackY * 0.65f);
        } else {
            state = State.IDLE;
            stateTime = 0f;
            hurtKnockbackTimer = HURT_KNOCKBACK_DURATION;
            velocity.x = knockbackX;
            velocity.y = knockbackY * 0.35f;
            attackCooldownTimer = Math.max(attackCooldownTimer, 0.25f);
        }
    }

    @Override
    public boolean isAlive() {
        return currentHealth > 0 && state != State.DYING && state != State.DEAD;
    }

    public boolean isReadyToRemove() {
        return false;
    }

    public void respawnIfDeadAndFar(Rectangle knightBounds, float respawnDistance) {
        if (state != State.DEAD || knightBounds == null) {
            return;
        }

        float knightCenterX = knightBounds.x + knightBounds.width / 2f;
        float knightCenterY = knightBounds.y + knightBounds.height / 2f;
        float dx = knightCenterX - spawnPosition.x;
        float dy = knightCenterY - spawnPosition.y;
        float distance = Math.max(32f, respawnDistance);

        if (dx * dx + dy * dy >= distance * distance) {
            resetToSpawn();
        }
    }

    private void resetToSpawn() {
        position.set(spawnPosition);
        velocity.set(0f, 0f);
        lockedTarget.set(spawnPosition);
        currentHealth = maxHealth;
        direction = spawnDirection;
        state = State.IDLE;
        stateTime = 0f;
        hurtKnockbackTimer = 0f;
        attackCooldownTimer = 0.25f;
        hoverTimer = 0f;
        grounded = false;
        landedDeath = false;
        hasSeenKnight = false;
        updateBounds();
    }

    public State getState() {
        return state;
    }

    public float getStateTime() {
        return stateTime;
    }

    public boolean isFacingLeft() {
        return direction < 0;
    }

    public float getDrawScale() {
        return drawScale;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isGrounded() {
        return grounded;
    }

    public boolean hasLandedDeath() {
        return landedDeath;
    }
}
