package com.hollowKnight.model.enemy;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hollowKnight.model.Damageable;

public class HuskHornhead implements Damageable {

    public enum State {
        WALKING, CHASING, TURNING, ANTICIPATING, LUNGING, COOLDOWN, DYING, DEAD
    }

    public static final float DEFAULT_HITBOX_WIDTH = 48f;
    public static final float DEFAULT_HITBOX_HEIGHT = 58f;
    public static final int DEFAULT_HEALTH = 3;

    public static final float DEFAULT_SPEED = 32f;
    public static final float DEFAULT_LUNGE_SPEED = 300f;
    public static final float DEFAULT_DETECTION_RANGE = 700f;
    public static final float DEFAULT_ATTACK_RANGE = 360f;
    public static final float DEFAULT_DRAW_SCALE = 0.78f;

    private static final float GRAVITY = -1500f;
    private static final float MOVE_STEP = 3f;
    private static final float GROUND_CHECK_DISTANCE = 3f;
    private static final float CLIFF_CHECK_DISTANCE_X = 10f;
    private static final float CLIFF_CHECK_DISTANCE_Y = 10f;

    private static final float TURN_DURATION = 0.28f;
    private static final float ANTICIPATE_DURATION = 0.50f;
    private static final float LUNGE_DURATION = 1.05f;
    private static final float COOLDOWN_DURATION = 0.45f;
    private static final float DEATH_DURATION = 1.00f;
    private static final float HURT_KNOCKBACK_DURATION = 0.15f;

    private static final float DAMAGE_BOUNDS_EXTRA_FRONT = 42f;
    private static final float DAMAGE_BOUNDS_EXTRA_TOP = 4f;
    private static final float FACE_KNIGHT_DEAD_ZONE_X = 14f;
    private static final float EPS = 0.0001f;

    private final Vector2 position;
    private final Vector2 velocity;
    private final Vector2 spawnPosition;
    private final Rectangle bounds;
    private final Rectangle damageBounds;
    private final Rectangle probeBounds;

    private final int maxHealth;
    private int currentHealth;

    private final float speed;
    private final float lungeSpeed;
    private final float detectionRange;
    private final float attackRange;
    private final float drawScale;

    private int direction;
    private final int spawnDirection;
    private State state;
    private float stateTime;
    private float hurtKnockbackTimer;
    private boolean grounded;
    private boolean hasSeenKnight;

    public HuskHornhead(float x, float y) {
        this(
            x,
            y,
            DEFAULT_HITBOX_WIDTH,
            DEFAULT_HITBOX_HEIGHT,
            DEFAULT_HEALTH,
            DEFAULT_SPEED,
            DEFAULT_LUNGE_SPEED,
            DEFAULT_DETECTION_RANGE,
            DEFAULT_ATTACK_RANGE,
            1,
            DEFAULT_DRAW_SCALE
        );
    }

    public HuskHornhead(
        float x,
        float y,
        float width,
        float height,
        int health,
        float speed,
        float lungeSpeed,
        float detectionRange,
        float attackRange,
        int direction,
        float drawScale
    ) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0f, 0f);
        this.spawnPosition = new Vector2(x, y);
        this.bounds = new Rectangle(x, y, Math.max(8f, width), Math.max(8f, height));
        this.damageBounds = new Rectangle(bounds);
        this.probeBounds = new Rectangle();

        this.maxHealth = Math.max(1, health);
        this.currentHealth = this.maxHealth;
        this.speed = Math.max(5f, speed);
        this.lungeSpeed = Math.max(this.speed + 20f, lungeSpeed);
        this.detectionRange = Math.max(32f, detectionRange);
        this.attackRange = Math.max(24f, attackRange);
        this.direction = direction < 0 ? -1 : 1;
        this.spawnDirection = this.direction;
        this.drawScale = Math.max(0.1f, drawScale);

        this.state = State.WALKING;
        this.stateTime = 0f;
        this.hurtKnockbackTimer = 0f;
        this.grounded = false;
        this.hasSeenKnight = false;
    }

    public void update(float delta, Rectangle knightBounds, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        if (state == State.DEAD) {
            return;
        }

        stateTime += delta;

        if (state == State.DYING) {
            updateDying(delta, platforms, polygonPlatforms);
            return;
        }

        if (hurtKnockbackTimer > 0f) {
            updateHurtKnockback(delta, platforms, polygonPlatforms);
            return;
        }

        if (state == State.TURNING) {
            updateTurning(delta, platforms, polygonPlatforms);
            return;
        }

        if (state == State.ANTICIPATING) {
            updateAnticipating(delta, platforms, polygonPlatforms);
            return;
        }

        if (state == State.LUNGING) {
            updateLunging(delta, platforms, polygonPlatforms);
            return;
        }

        if (state == State.COOLDOWN) {
            updateCooldown(delta, knightBounds, platforms, polygonPlatforms);
            return;
        }

        updateTargetAwareness(knightBounds);

        if (hasSeenKnight && knightBounds != null) {
            updateChasingOrAttack(delta, knightBounds, platforms, polygonPlatforms);
            return;
        }

        updatePatrol(delta, platforms, polygonPlatforms);
    }

    private void updateDying(float delta, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        if (!grounded && Math.abs(velocity.x) > 0.01f) {
            moveAxis(velocity.x * delta, 0f, platforms, polygonPlatforms);
            velocity.x *= Math.max(0f, 1f - delta * 3.2f);
        }

        applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);

        if (grounded) {
            velocity.x = 0f;

            if (stateTime >= DEATH_DURATION) {
                state = State.DEAD;
                stateTime = DEATH_DURATION;
            }
        }
    }

    private void updateHurtKnockback(float delta, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        hurtKnockbackTimer -= delta;

        moveAxis(velocity.x * delta, 0f, platforms, polygonPlatforms);
        applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);

        if (hurtKnockbackTimer <= 0f) {
            hurtKnockbackTimer = 0f;
            velocity.x = 0f;
            state = hasSeenKnight ? State.CHASING : State.WALKING;
            stateTime = 0f;
        }
    }

    private void updateTurning(float delta, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        velocity.x = 0f;
        applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);

        if (stateTime >= TURN_DURATION) {
            direction *= -1;
            state = State.WALKING;
            stateTime = 0f;
        }
    }

    private void updateAnticipating(float delta, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        velocity.x = 0f;
        applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);

        if (stateTime >= ANTICIPATE_DURATION) {
            state = State.LUNGING;
            stateTime = 0f;
        }
    }

    private void updateLunging(float delta, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        velocity.x = direction * lungeSpeed;

        boolean hitWall = moveAxis(velocity.x * delta, 0f, platforms, polygonPlatforms);
        applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);

        boolean reachedCliff = grounded && isAtCliffEdge(platforms, polygonPlatforms);

        if (hitWall || reachedCliff || stateTime >= LUNGE_DURATION) {
            velocity.x = 0f;
            state = State.COOLDOWN;
            stateTime = 0f;
        }
    }

    private void updateCooldown(
        float delta,
        Rectangle knightBounds,
        Array<Rectangle> platforms,
        Array<Polygon> polygonPlatforms
    ) {
        velocity.x = 0f;
        applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);
        updateTargetAwareness(knightBounds);

        if (stateTime >= COOLDOWN_DURATION) {
            state = hasSeenKnight ? State.CHASING : State.WALKING;
            stateTime = 0f;
        }
    }

    private void updateChasingOrAttack(
        float delta,
        Rectangle knightBounds,
        Array<Rectangle> platforms,
        Array<Polygon> polygonPlatforms
    ) {
        faceKnight(knightBounds);

        if (shouldStartAttack(knightBounds)) {
            state = State.ANTICIPATING;
            stateTime = 0f;
            velocity.x = 0f;
            return;
        }

        state = State.CHASING;

        if (grounded && isAtCliffEdge(platforms, polygonPlatforms)) {
            velocity.x = 0f;
            applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);
            return;
        }

        velocity.x = direction * speed;
        boolean hitWall = moveAxis(velocity.x * delta, 0f, platforms, polygonPlatforms);

        if (hitWall) {
            velocity.x = 0f;
        }

        applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);
    }

    private void updatePatrol(float delta, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        state = State.WALKING;
        velocity.x = direction * speed;

        boolean hitWall = moveAxis(velocity.x * delta, 0f, platforms, polygonPlatforms);

        if (hitWall) {
            velocity.x = 0f;
            beginTurn();
        }

        applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);
        grounded = isTouchingGround(platforms, polygonPlatforms);

        if (grounded && state == State.WALKING && isAtCliffEdge(platforms, polygonPlatforms)) {
            beginTurn();
        }
    }

    private void updateTargetAwareness(Rectangle knightBounds) {
        if (knightBounds == null || !isAlive()) {
            return;
        }

        if (isKnightInDetectionRange(knightBounds)) {
            hasSeenKnight = true;
        }
    }

    private boolean shouldStartAttack(Rectangle knightBounds) {
        if (knightBounds == null || !grounded) {
            return false;
        }

        float selfCenterX = bounds.x + bounds.width / 2f;
        float selfCenterY = bounds.y + bounds.height / 2f;
        float knightCenterX = knightBounds.x + knightBounds.width / 2f;
        float knightCenterY = knightBounds.y + knightBounds.height / 2f;

        float dx = knightCenterX - selfCenterX;
        float dy = Math.abs(knightCenterY - selfCenterY);

        boolean closeEnough = Math.abs(dx) <= attackRange;
        boolean sameHeightBand = dy <= Math.max(44f, bounds.height * 0.78f);
        boolean knightIsInFront = direction > 0 ? dx >= -10f : dx <= 10f;

        return closeEnough && sameHeightBand && knightIsInFront;
    }

    private boolean isKnightInDetectionRange(Rectangle knightBounds) {
        float selfCenterX = bounds.x + bounds.width / 2f;
        float selfCenterY = bounds.y + bounds.height / 2f;
        float knightCenterX = knightBounds.x + knightBounds.width / 2f;
        float knightCenterY = knightBounds.y + knightBounds.height / 2f;

        float dx = knightCenterX - selfCenterX;
        float dy = knightCenterY - selfCenterY;

        return dx * dx + dy * dy <= detectionRange * detectionRange;
    }

    private void faceKnight(Rectangle knightBounds) {
        if (knightBounds == null) {
            return;
        }

        float selfCenterX = bounds.x + bounds.width / 2f;
        float knightCenterX = knightBounds.x + knightBounds.width / 2f;
        float dx = knightCenterX - selfCenterX;

        if (Math.abs(dx) > FACE_KNIGHT_DEAD_ZONE_X) {
            direction = dx < 0f ? -1 : 1;
        }
    }

    private void applyGravityAndVerticalCollision(float delta, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        if (!grounded) {
            velocity.y += GRAVITY * delta;
        } else if (velocity.y < 0f) {
            velocity.y = 0f;
        }

        boolean hitVertical = moveAxis(0f, velocity.y * delta, platforms, polygonPlatforms);

        if (hitVertical) {
            velocity.y = 0f;
        }

        grounded = isTouchingGround(platforms, polygonPlatforms);
    }

    private void beginTurn() {
        if (state != State.WALKING) {
            return;
        }

        state = State.TURNING;
        stateTime = 0f;
        velocity.x = 0f;
    }

    private boolean isAtCliffEdge(Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        float probeX = direction > 0
            ? bounds.x + bounds.width + CLIFF_CHECK_DISTANCE_X
            : bounds.x - CLIFF_CHECK_DISTANCE_X;

        probeBounds.set(
            probeX,
            bounds.y - CLIFF_CHECK_DISTANCE_Y,
            7f,
            CLIFF_CHECK_DISTANCE_Y + 5f
        );

        return !overlapsAny(probeBounds, platforms, polygonPlatforms);
    }

    private boolean isTouchingGround(Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        probeBounds.set(
            bounds.x + 4f,
            bounds.y - GROUND_CHECK_DISTANCE,
            Math.max(2f, bounds.width - 8f),
            GROUND_CHECK_DISTANCE + 1f
        );

        return overlapsAny(probeBounds, platforms, polygonPlatforms);
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

    public Rectangle getDamageBounds() {
        updateBounds();

        if (state == State.LUNGING || state == State.ANTICIPATING) {
            if (direction < 0) {
                damageBounds.set(
                    bounds.x - DAMAGE_BOUNDS_EXTRA_FRONT,
                    bounds.y,
                    bounds.width + DAMAGE_BOUNDS_EXTRA_FRONT,
                    bounds.height + DAMAGE_BOUNDS_EXTRA_TOP
                );
            } else {
                damageBounds.set(
                    bounds.x,
                    bounds.y,
                    bounds.width + DAMAGE_BOUNDS_EXTRA_FRONT,
                    bounds.height + DAMAGE_BOUNDS_EXTRA_TOP
                );
            }
        } else {
            damageBounds.set(bounds);
        }

        return damageBounds;
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
            velocity.x = knockbackX * 0.52f;
            velocity.y = Math.max(velocity.y, knockbackY * 0.35f);
            grounded = false;
        } else {
            state = hasSeenKnight ? State.CHASING : State.WALKING;
            stateTime = 0f;
            hurtKnockbackTimer = HURT_KNOCKBACK_DURATION;
            velocity.x = knockbackX * 0.75f;
            velocity.y = Math.max(velocity.y, knockbackY * 0.55f);
            grounded = false;
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
        currentHealth = maxHealth;
        direction = spawnDirection;
        state = State.WALKING;
        stateTime = 0f;
        hurtKnockbackTimer = 0f;
        grounded = false;
        hasSeenKnight = false;
        updateBounds();
        damageBounds.set(bounds);
    }

    public State getState() {
        return state;
    }

    public float getStateTime() {
        return stateTime;
    }

    public int getDirection() {
        return direction;
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

    public boolean hasSeenKnight() {
        return hasSeenKnight;
    }
}
