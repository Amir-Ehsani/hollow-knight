package com.hollowKnight.model.enemy;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hollowKnight.model.Damageable;

public class Crystallized implements Damageable {

    public enum State {
        IDLE, RUNNING, TURNING, EVADING, SHOOTING, DYING, DEAD
    }

    public static final float DEFAULT_HITBOX_WIDTH = 46f;
    public static final float DEFAULT_HITBOX_HEIGHT = 62f;
    public static final int DEFAULT_HEALTH = 3;
    public static final float DEFAULT_SPEED = 74f;
    public static final float DEFAULT_EVADE_SPEED = 235f;
    public static final float DEFAULT_DETECTION_RANGE = 920f;
    public static final float DEFAULT_SHOOT_RANGE = 760f;
    public static final float DEFAULT_LASER_LENGTH = 920f;
    public static final float DEFAULT_DRAW_SCALE = 0.66f;

    private static final float GRAVITY = -1500f;
    private static final float MOVE_STEP = 3f;
    private static final float GROUND_CHECK_DISTANCE = 3f;
    private static final float CLIFF_CHECK_DISTANCE_X = 10f;
    private static final float CLIFF_CHECK_DISTANCE_Y = 10f;

    private static final float TURN_DURATION = 0.22f;
    private static final float SHOOT_DURATION = 1.75f;
    private static final float LASER_START_TIME = 0.90f;
    private static final float LASER_END_TIME = 1.42f;
    private static final float ATTACK_COOLDOWN = 1.60f;
    private static final float EVADE_DURATION = 0.36f;
    private static final float MIN_EVADE_DISTANCE = 145f;
    private static final float PREFERRED_SHOOT_DISTANCE = 360f;
    private static final float MAX_SHOOT_VERTICAL_DELTA = 280f;
    private static final float HURT_KNOCKBACK_DURATION = 0.16f;
    private static final float DEATH_AIR_MIN_TIME = 0.20f;
    private static final float DEATH_LAND_DURATION = 0.55f;
    private static final float FACE_KNIGHT_DEAD_ZONE_X = 16f;
    private static final float LASER_WIDTH = 18f;
    private static final float EPS = 0.0001f;

    private final Vector2 position;
    private final Vector2 velocity;
    private final Vector2 spawnPosition;
    private final Vector2 lockedTarget;
    private final Vector2 laserStart;
    private final Vector2 laserEnd;
    private final Rectangle bounds;
    private final Rectangle probeBounds;

    private final int maxHealth;
    private int currentHealth;

    private final float speed;
    private final float evadeSpeed;
    private final float detectionRange;
    private final float shootRange;
    private final float laserLength;
    private final float drawScale;

    private int direction;
    private int turnTargetDirection;
    private int evadeMoveDirection;
    private final int spawnDirection;

    private State state;
    private float stateTime;
    private float attackCooldownTimer;
    private float hurtKnockbackTimer;
    private boolean grounded;
    private boolean landedDeath;
    private boolean hasSeenKnight;

    public Crystallized(float x, float y) {
        this(
            x,
            y,
            DEFAULT_HITBOX_WIDTH,
            DEFAULT_HITBOX_HEIGHT,
            DEFAULT_HEALTH,
            DEFAULT_SPEED,
            DEFAULT_EVADE_SPEED,
            DEFAULT_DETECTION_RANGE,
            DEFAULT_SHOOT_RANGE,
            DEFAULT_LASER_LENGTH,
            1,
            DEFAULT_DRAW_SCALE
        );
    }

    public Crystallized(
        float x,
        float y,
        float width,
        float height,
        int health,
        float speed,
        float evadeSpeed,
        float detectionRange,
        float shootRange,
        float laserLength,
        int direction,
        float drawScale
    ) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0f, 0f);
        this.spawnPosition = new Vector2(x, y);
        this.lockedTarget = new Vector2(x, y);
        this.laserStart = new Vector2(x, y);
        this.laserEnd = new Vector2(x, y);
        this.bounds = new Rectangle(x, y, Math.max(8f, width), Math.max(8f, height));
        this.probeBounds = new Rectangle();

        this.maxHealth = Math.max(1, health);
        this.currentHealth = this.maxHealth;
        this.speed = Math.max(10f, speed);
        this.evadeSpeed = Math.max(this.speed + 20f, evadeSpeed);
        this.detectionRange = Math.max(64f, detectionRange);
        this.shootRange = Math.max(90f, shootRange);
        this.laserLength = Math.max(160f, laserLength);
        this.direction = direction < 0 ? -1 : 1;
        this.turnTargetDirection = this.direction;
        this.evadeMoveDirection = -this.direction;
        this.spawnDirection = this.direction;
        this.drawScale = Math.max(0.1f, drawScale);

        this.state = State.IDLE;
        this.stateTime = 0f;
        this.attackCooldownTimer = 0.45f;
        this.hurtKnockbackTimer = 0f;
        this.grounded = false;
        this.landedDeath = false;
        this.hasSeenKnight = false;

        updateLaserSegment();
    }

    public void update(float delta, Rectangle knightBounds, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        if (state == State.DEAD) {
            return;
        }

        stateTime += delta;

        if (attackCooldownTimer > 0f) {
            attackCooldownTimer -= delta;
        }

        if (state == State.DYING) {
            updateDying(delta, platforms, polygonPlatforms);
            updateLaserSegment();
            return;
        }

        if (hurtKnockbackTimer > 0f) {
            updateHurtKnockback(delta, platforms, polygonPlatforms);
            updateLaserSegment();
            return;
        }

        if (state == State.TURNING) {
            updateTurning(delta, platforms, polygonPlatforms);
            updateLaserSegment();
            return;
        }

        if (state == State.SHOOTING) {
            updateShooting(delta, platforms, polygonPlatforms);
            updateLaserSegment();
            return;
        }

        if (state == State.EVADING) {
            updateEvading(delta, platforms, polygonPlatforms);
            updateLaserSegment();
            return;
        }

        updateTargetAwareness(knightBounds);

        if (hasSeenKnight && knightBounds != null) {
            updateCombatMovement(delta, knightBounds, platforms, polygonPlatforms);
        } else {
            updateIdlePatrol(delta, platforms, polygonPlatforms);
        }

        updateLaserSegment();
    }

    private void updateTargetAwareness(Rectangle knightBounds) {
        Vector2 knightCenter = getKnightCenter(knightBounds);

        if (knightCenter != null && isInsideDetectionRange(knightCenter)) {
            hasSeenKnight = true;
        }
    }

    private void updateCombatMovement(
        float delta,
        Rectangle knightBounds,
        Array<Rectangle> platforms,
        Array<Polygon> polygonPlatforms
    ) {
        Vector2 knightCenter = getKnightCenter(knightBounds);

        if (knightCenter == null) {
            state = State.IDLE;
            velocity.x = 0f;
            applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);
            return;
        }

        float centerX = getCenterX();
        float centerY = getCenterY();
        float dx = knightCenter.x - centerX;
        float dy = knightCenter.y - centerY;
        float distance2 = dx * dx + dy * dy;

        updateDirectionToward(knightCenter.x);

        if (grounded && Math.abs(dx) < MIN_EVADE_DISTANCE && Math.abs(dy) < 130f) {
            beginEvade(knightCenter);
            return;
        }

        if (attackCooldownTimer <= 0f && canStartShoot(dx, dy, distance2)) {
            beginShoot(knightCenter);
            return;
        }

        float desiredDistance = PREFERRED_SHOOT_DISTANCE;
        boolean tooFar = Math.abs(dx) > desiredDistance || Math.abs(dy) > 170f;

        if (tooFar) {
            int moveDirection = dx < -FACE_KNIGHT_DEAD_ZONE_X ? -1 : 1;
            velocity.x = moveDirection * speed;
            state = State.RUNNING;

            boolean hitWall = moveAxis(velocity.x * delta, 0f, platforms, polygonPlatforms);
            applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);

            if (hitWall || (grounded && isAtCliffEdge(moveDirection, platforms, polygonPlatforms))) {
                velocity.x = 0f;
                state = State.IDLE;
            }
        } else {
            velocity.x = 0f;
            state = State.IDLE;
            applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);
        }
    }

    private boolean canStartShoot(float dx, float dy, float distance2) {
        if (distance2 > shootRange * shootRange) {
            return false;
        }

        if (Math.abs(dy) > MAX_SHOOT_VERTICAL_DELTA) {
            return false;
        }

        if (Math.abs(dx) < 62f && Math.abs(dy) > 80f) {
            return false;
        }

        return true;
    }

    private void updateIdlePatrol(float delta, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        velocity.x = direction * speed * 0.55f;
        state = State.RUNNING;

        boolean hitWall = moveAxis(velocity.x * delta, 0f, platforms, polygonPlatforms);
        applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);

        if (hitWall || (grounded && isAtCliffEdge(direction, platforms, polygonPlatforms))) {
            velocity.x = 0f;
            beginTurnTo(-direction);
        }
    }

    private void beginShoot(Vector2 knightCenter) {
        lockedTarget.set(knightCenter);
        updateDirectionToward(knightCenter.x);
        velocity.set(0f, 0f);
        state = State.SHOOTING;
        stateTime = 0f;
        updateLaserSegment();
    }

    private void updateShooting(float delta, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        velocity.x = 0f;
        applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);

        if (stateTime >= SHOOT_DURATION) {
            state = State.IDLE;
            stateTime = 0f;
            attackCooldownTimer = ATTACK_COOLDOWN;
        }
    }

    private void beginEvade(Vector2 knightCenter) {
        updateDirectionToward(knightCenter.x);
        evadeMoveDirection = -direction;
        velocity.x = evadeMoveDirection * evadeSpeed;
        state = State.EVADING;
        stateTime = 0f;
    }

    private void updateEvading(float delta, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        velocity.x = evadeMoveDirection * evadeSpeed;

        boolean hitWall = moveAxis(velocity.x * delta, 0f, platforms, polygonPlatforms);
        applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);

        if (hitWall || (grounded && isAtCliffEdge(evadeMoveDirection, platforms, polygonPlatforms)) || stateTime >= EVADE_DURATION) {
            velocity.x = 0f;
            state = State.IDLE;
            stateTime = 0f;
            attackCooldownTimer = Math.max(attackCooldownTimer, 0.28f);
        }
    }

    private void updateTurning(float delta, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        velocity.x = 0f;
        applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);

        if (stateTime >= TURN_DURATION) {
            direction = turnTargetDirection < 0 ? -1 : 1;
            state = State.IDLE;
            stateTime = 0f;
        }
    }

    private void beginTurnTo(int newDirection) {
        int normalized = newDirection < 0 ? -1 : 1;

        if (direction == normalized) {
            return;
        }

        turnTargetDirection = normalized;
        state = State.TURNING;
        stateTime = 0f;
        velocity.x = 0f;
    }

    private void updateDying(float delta, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        if (!landedDeath) {
            velocity.y += GRAVITY * delta;

            if (Math.abs(velocity.x) > 0.01f) {
                moveAxis(velocity.x * delta, 0f, platforms, polygonPlatforms);
                velocity.x *= Math.max(0f, 1f - delta * 2.8f);
            }

            boolean hitGround = moveAxis(0f, velocity.y * delta, platforms, polygonPlatforms);
            grounded = isTouchingGround(platforms, polygonPlatforms);

            if ((hitGround && velocity.y <= 0f) || grounded) {
                velocity.set(0f, 0f);
                grounded = true;
                landedDeath = true;
                stateTime = 0f;
            }

            if (!landedDeath && stateTime >= DEATH_AIR_MIN_TIME && grounded) {
                landedDeath = true;
                stateTime = 0f;
            }

            return;
        }

        if (stateTime >= DEATH_LAND_DURATION) {
            state = State.DEAD;
            stateTime = DEATH_LAND_DURATION;
        }
    }

    private void updateHurtKnockback(float delta, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        hurtKnockbackTimer -= delta;

        moveAxis(velocity.x * delta, 0f, platforms, polygonPlatforms);
        applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);
        velocity.x *= Math.max(0f, 1f - delta * 4.2f);

        if (hurtKnockbackTimer <= 0f) {
            hurtKnockbackTimer = 0f;
            velocity.x = 0f;
            state = State.IDLE;
            stateTime = 0f;
            attackCooldownTimer = Math.max(attackCooldownTimer, 0.22f);
        }
    }

    private void applyGravityAndVerticalCollision(float delta, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        grounded = isTouchingGround(platforms, polygonPlatforms);

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

    private boolean isTouchingGround(Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        probeBounds.set(
            bounds.x + 4f,
            bounds.y - GROUND_CHECK_DISTANCE,
            Math.max(2f, bounds.width - 8f),
            GROUND_CHECK_DISTANCE + 1f
        );

        return overlapsAny(probeBounds, platforms, polygonPlatforms);
    }

    private boolean isAtCliffEdge(int moveDirection, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        float probeX = moveDirection > 0
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

    private void updateDirectionToward(float targetX) {
        float centerX = getCenterX();

        if (Math.abs(targetX - centerX) <= FACE_KNIGHT_DEAD_ZONE_X) {
            return;
        }

        int newDirection = targetX < centerX ? -1 : 1;

        if (state == State.IDLE || state == State.RUNNING) {
            direction = newDirection;
        } else {
            turnTargetDirection = newDirection;
        }
    }

    private boolean isInsideDetectionRange(Vector2 knightCenter) {
        float dx = knightCenter.x - getCenterX();
        float dy = knightCenter.y - getCenterY();
        return dx * dx + dy * dy <= detectionRange * detectionRange;
    }

    private Vector2 getKnightCenter(Rectangle knightBounds) {
        if (knightBounds == null) {
            return null;
        }

        return new Vector2(
            knightBounds.x + knightBounds.width / 2f,
            knightBounds.y + knightBounds.height / 2f
        );
    }

    private void updateLaserSegment() {
        float startX = direction > 0 ? bounds.x + bounds.width * 0.82f : bounds.x + bounds.width * 0.18f;
        float startY = bounds.y + bounds.height * 0.7f;
        laserStart.set(startX, startY);

        Vector2 directionVector = new Vector2(lockedTarget).sub(laserStart);

        if (directionVector.len2() < 1f) {
            directionVector.set(direction, 0f);
        }

        if (directionVector.x * direction < 0f && Math.abs(directionVector.x) > 24f) {
            directionVector.x = direction * Math.abs(directionVector.x);
        }

        if (Math.abs(directionVector.x) < 10f) {
            directionVector.x = direction * 10f;
        }

        directionVector.nor().scl(laserLength);
        laserEnd.set(laserStart).add(directionVector);
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
        return getBounds();
    }

    public boolean canDamageKnight(Rectangle knightBounds) {
        if (!isAlive() || knightBounds == null) {
            return false;
        }

        if (getDamageBounds().overlaps(knightBounds)) {
            return true;
        }

        return isLaserHitting(knightBounds);
    }

    public boolean isLaserHitting(Rectangle rectangle) {
        if (!isLaserActive() || rectangle == null) {
            return false;
        }

        float padding = LASER_WIDTH / 2f;
        Rectangle expanded = new Rectangle(
            rectangle.x - padding,
            rectangle.y - padding,
            rectangle.width + padding * 2f,
            rectangle.height + padding * 2f
        );

        return segmentIntersectsRect(laserStart.x, laserStart.y, laserEnd.x, laserEnd.y, expanded);
    }

    private boolean segmentIntersectsRect(float x1, float y1, float x2, float y2, Rectangle rect) {
        if (rect.contains(x1, y1) || rect.contains(x2, y2)) {
            return true;
        }

        float left = rect.x;
        float right = rect.x + rect.width;
        float bottom = rect.y;
        float top = rect.y + rect.height;

        return segmentsIntersect(x1, y1, x2, y2, left, bottom, right, bottom) ||
            segmentsIntersect(x1, y1, x2, y2, right, bottom, right, top) ||
            segmentsIntersect(x1, y1, x2, y2, right, top, left, top) ||
            segmentsIntersect(x1, y1, x2, y2, left, top, left, bottom);
    }

    public boolean isLaserActive() {
        return state == State.SHOOTING && stateTime >= LASER_START_TIME && stateTime <= LASER_END_TIME;
    }

    public float getLaserChargeProgress() {
        if (state != State.SHOOTING) {
            return 0f;
        }

        if (stateTime < LASER_START_TIME) {
            return Math.max(0f, Math.min(1f, stateTime / LASER_START_TIME));
        }

        if (stateTime <= LASER_END_TIME) {
            return 1f;
        }

        float fadeDuration = Math.max(0.01f, SHOOT_DURATION - LASER_END_TIME);
        return Math.max(0f, Math.min(1f, 1f - ((stateTime - LASER_END_TIME) / fadeDuration)));
    }

    public Vector2 getLaserStart() {
        updateLaserSegment();
        return laserStart;
    }

    public Vector2 getLaserEnd() {
        updateLaserSegment();
        return laserEnd;
    }

    public float getLaserWidth() {
        return LASER_WIDTH;
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
            velocity.x = knockbackX * 0.55f;
            velocity.y = Math.max(velocity.y, knockbackY * 0.35f);
            landedDeath = false;
            grounded = false;
        } else {
            state = State.IDLE;
            stateTime = 0f;
            hurtKnockbackTimer = HURT_KNOCKBACK_DURATION;
            velocity.x = knockbackX * 0.8f;
            velocity.y = Math.max(velocity.y, knockbackY * 0.45f);
            grounded = false;
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
        currentHealth = maxHealth;
        direction = spawnDirection;
        turnTargetDirection = direction;
        evadeMoveDirection = -direction;
        state = State.IDLE;
        stateTime = 0f;
        attackCooldownTimer = 0.45f;
        hurtKnockbackTimer = 0f;
        grounded = false;
        landedDeath = false;
        hasSeenKnight = false;
        updateBounds();
        lockedTarget.set(getCenterX() + direction * 64f, getCenterY());
        updateLaserSegment();
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

    public float getCenterX() {
        return bounds.x + bounds.width / 2f;
    }

    public float getCenterY() {
        return bounds.y + bounds.height / 2f;
    }
}
