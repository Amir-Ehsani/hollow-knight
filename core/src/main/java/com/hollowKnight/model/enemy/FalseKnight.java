package com.hollowKnight.model.enemy;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.hollowKnight.model.Damageable;
public class FalseKnight implements Damageable {
    public enum State {
        IDLE, TURNING, ATTACK_ANTIC, ATTACKING, ATTACK_RECOVER, RUN_ANTIC, RUNNING, JUMP_ANTIC, JUMPING, JUMP_ATTACK, LANDING, STUN_FALL, STUNNED, STUN_RECOVER, DEATH_FALL, DEATH_HIT, DEATH_LAND, DEAD
    }
    public enum Move {
        NONE, MACE_SLAM, CHARGE_RUN, OFFENSIVE_LEAP, DEFENSIVE_LEAP, POWER_SLAM
    }
    public enum Event {
        SWING, SLAM_IMPACT, POWER_SLAM_IMPACT, JUMP, LAND, CHARGE, ARMOR_HIT, ARMOR_FINAL_HIT, HEAD_HIT, ENTER_STUN, RAGE, DEATH, FALL, STUN_RECOVER, CEILING_BREAK
    }
    public static class Shockwave {
        private final Rectangle bounds;
        private final int direction;
        private float speed;
        private float age;
        private boolean alive;
        private Shockwave(float x, float y, int direction, float startSpeed) {
            this.bounds = new Rectangle(x, y, 104f, 36f);
            this.direction = direction < 0 ? -1 : 1;
            this.speed = startSpeed;
            this.age = 0f;
            this.alive = true;
        }
        private void update(float delta) {
            if (!alive) {
                return;
            }
            age += delta;
            speed += 185f * delta;
            bounds.x += direction * speed * delta;
            if (age >= 2f) {
                alive = false;
            }
        }
        public Rectangle getBounds() {
            return bounds;
        }
        public int getDirection() {
            return direction;
        }
        public float getAge() {
            return age;
        }
        public boolean isAlive() {
            return alive;
        }
    }
    public static final float DEFAULT_HITBOX_WIDTH = 128f;
    public static final float DEFAULT_HITBOX_HEIGHT = 145f;
    public static final int DEFAULT_HEALTH = 28;
    public static final float DEFAULT_DRAW_SCALE = 1.0f;
    public static final float DEFAULT_SPRITE_Y_OFFSET = 52f;
    public static final float DEFAULT_DETECTION_RANGE = 980f;
    public static final float DEFAULT_ARENA_PADDING = 520f;
    private static final float GRAVITY = -1750f;
    private static final float MOVE_STEP = 2f;
    private static final float GROUND_CHECK_DISTANCE = 5f;
    private static final float TURN_DURATION = 0.18f;
    private static final float IDLE_DECISION_TIME = 0.52f;
    private static final float PHASE_TWO_IDLE_DECISION_TIME = 0.36f;
    private static final float ATTACK_ANTIC_DURATION = 0.48f;
    private static final float ATTACK_DURATION = 0.62f;
    private static final float ATTACK_RECOVER_DURATION = 0.42f;
    private static final float ATTACK_IMPACT_TIME = 0.31f;
    private static final float ATTACK_ACTIVE_TIME = 0.24f;
    private static final float RUN_ANTIC_DURATION = 0.32f;
    private static final float RUN_DURATION = 1.18f;
    private static final float JUMP_ANTIC_DURATION = 0.34f;
    private static final float LANDING_DURATION = 0.32f;
    private static final float STUN_FALL_DURATION = 0.72f;
    private static final float STUN_DURATION = 4.2f;
    private static final float STUN_RECOVER_DURATION = 1.05f;
    private static final float DEATH_HIT_DURATION = 0.54f;
    private static final float DEATH_LAND_DURATION = 1.55f;
    private static final float CLOSE_DISTANCE = 210f;
    private static final float FAR_DISTANCE = 520f;
    private static final float BASE_RUN_SPEED = 330f;
    private static final float PHASE_TWO_SPEED_MULTIPLIER = 1.28f;
    private static final float OFFENSIVE_JUMP_SPEED_X = 255f;
    private static final float DEFENSIVE_JUMP_SPEED_X = 330f;
    private static final float JUMP_SPEED_Y = 820f;
    private static final float POWER_JUMP_SPEED_Y = 920f;
    private static final float SHOCKWAVE_START_SPEED = 245f;
    private static final float EPS = 0.0001f;
    private final Vector2 position;
    private final Vector2 velocity;
    private final Vector2 spawnPosition;
    private final Rectangle bounds;
    private final Rectangle armorDamageBounds;
    private final Rectangle maceDamageBounds;
    private final Rectangle vulnerableBounds;
    private final Rectangle probeBounds;
    private final Array<Event> eventQueue;
    private final Array<Shockwave> shockwaves;
    private Rectangle arenaBounds;
    private final int maxHealth;
    private int currentHealth;
    private int direction;
    private final int spawnDirection;
    private final float drawScale;
    private final float spriteYOffset;
    private final float detectionRange;
    private State state;
    private Move currentMove;
    private Move lastMove;
    private float stateTime;
    private float decisionTimer;
    private float runSpeed;
    private float attackActiveTimer;
    private boolean impactDone;
    private boolean grounded;
    private boolean hasSeenKnight;
    private boolean phaseTwo;
    private boolean stunTriggered;
    private int repeatedMoveCount;
    private int recentHitCount;
    private float recentHitTimer;
    private float lastKnightCenterX;
    private float lastKnightCenterY;
    public FalseKnight(float x, float y) {
        this(x, y, DEFAULT_HITBOX_WIDTH, DEFAULT_HITBOX_HEIGHT, DEFAULT_HEALTH, 1, DEFAULT_DRAW_SCALE, DEFAULT_DETECTION_RANGE, null, DEFAULT_SPRITE_Y_OFFSET);
    }
    public FalseKnight(float x, float y, float width, float height, int health, int direction, float drawScale, float detectionRange, Rectangle arenaBounds) {
        this(x, y, width, height, health, direction, drawScale, detectionRange, arenaBounds, DEFAULT_SPRITE_Y_OFFSET);
    }
    public FalseKnight(float x, float y, float width, float height, int health, int direction, float drawScale, float detectionRange, Rectangle arenaBounds, float spriteYOffset) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2();
        this.spawnPosition = new Vector2(x, y);
        this.bounds = new Rectangle(x, y, Math.max(50f, width), Math.max(70f, height));
        this.armorDamageBounds = new Rectangle();
        this.maceDamageBounds = new Rectangle();
        this.vulnerableBounds = new Rectangle();
        this.probeBounds = new Rectangle();
        this.eventQueue = new Array<>();
        this.shockwaves = new Array<>();
        this.maxHealth = Math.max(6, health);
        this.currentHealth = this.maxHealth;
        this.direction = direction < 0 ? -1 : 1;
        this.spawnDirection = this.direction;
        this.drawScale = Math.max(0.1f, drawScale);
        this.spriteYOffset = Math.max(0f, spriteYOffset);
        this.detectionRange = Math.max(260f, detectionRange);
        this.arenaBounds = arenaBounds == null ? null : new Rectangle(arenaBounds);
        this.state = State.IDLE;
        this.currentMove = Move.NONE;
        this.lastMove = Move.NONE;
        this.stateTime = 0f;
        this.decisionTimer = IDLE_DECISION_TIME;
        this.runSpeed = BASE_RUN_SPEED;
        this.attackActiveTimer = 0f;
        this.impactDone = false;
        this.grounded = false;
        this.hasSeenKnight = false;
        this.phaseTwo = false;
        this.stunTriggered = false;
        this.repeatedMoveCount = 0;
        this.recentHitCount = 0;
        this.recentHitTimer = 0f;
        this.lastKnightCenterX = x;
        this.lastKnightCenterY = y;
        updateBounds();
        updateDerivedBounds();
    }
    public void update(float delta, Rectangle knightBounds, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        if (delta <= 0f) {
            return;
        }
        updateShockwaves(delta);
        updateRecentHits(delta);
        updateTarget(knightBounds);
        stateTime += delta;
        if (state == State.DEAD) {
            return;
        }
        if (state == State.DEATH_HIT) {
            velocity.x *= Math.max(0f, 1f - delta * 3.2f);
            applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);
            if (stateTime >= DEATH_HIT_DURATION) {
                changeState(State.DEATH_FALL);
                queueEvent(Event.FALL);
            }
            updateDerivedBounds();
            return;
        }
        if (state == State.DEATH_FALL) {
            applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);
            if (grounded) {
                velocity.set(0f, 0f);
                changeState(State.DEATH_LAND);
                queueEvent(Event.LAND);
            }
            updateDerivedBounds();
            return;
        }
        if (state == State.DEATH_LAND) {
            if (stateTime >= DEATH_LAND_DURATION) {
                changeState(State.DEAD);
            }
            updateDerivedBounds();
            return;
        }
        if (state == State.STUN_FALL) {
            velocity.x *= Math.max(0f, 1f - delta * 4.5f);
            applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);
            if (grounded || stateTime >= STUN_FALL_DURATION) {
                velocity.set(0f, 0f);
                changeState(State.STUNNED);
                queueEvent(Event.ENTER_STUN);
            }
            updateDerivedBounds();
            return;
        }
        if (state == State.STUNNED) {
            velocity.set(0f, 0f);
            grounded = true;
            if (stateTime >= STUN_DURATION) {
                beginStunRecover();
            }
            updateDerivedBounds();
            return;
        }
        if (state == State.STUN_RECOVER) {
            velocity.set(0f, 0f);
            if (stateTime >= STUN_RECOVER_DURATION) {
                changeState(State.IDLE);
                currentMove = Move.NONE;
                decisionTimer = getDecisionDelay();
            }
            updateDerivedBounds();
            return;
        }
        if (!hasSeenKnight) {
            velocity.x = 0f;
            applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);
            updateDerivedBounds();
            return;
        }
        switch (state) {
            case IDLE:
                updateIdle(delta, knightBounds, platforms, polygonPlatforms);
                break;
            case TURNING:
                velocity.x = 0f;
                applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);
                if (stateTime >= TURN_DURATION) {
                    changeState(State.IDLE);
                    decisionTimer = 0.05f;
                }
                break;
            case ATTACK_ANTIC:
                velocity.x = 0f;
                applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);
                if (stateTime >= ATTACK_ANTIC_DURATION) {
                    beginAttackSwing();
                }
                break;
            case ATTACKING:
                updateAttack(delta, platforms, polygonPlatforms);
                break;
            case ATTACK_RECOVER:
                velocity.x = 0f;
                applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);
                if (stateTime >= getScaledDuration(ATTACK_RECOVER_DURATION)) {
                    finishMove();
                }
                break;
            case RUN_ANTIC:
                velocity.x = 0f;
                applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);
                if (stateTime >= getScaledDuration(RUN_ANTIC_DURATION)) {
                    beginRun();
                }
                break;
            case RUNNING:
                updateRun(delta, knightBounds, platforms, polygonPlatforms);
                break;
            case JUMP_ANTIC:
                velocity.x = 0f;
                applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);
                if (stateTime >= getScaledDuration(JUMP_ANTIC_DURATION)) {
                    beginLeapVelocity(knightBounds);
                }
                break;
            case JUMPING:
                updateLeap(delta, platforms, polygonPlatforms, false);
                break;
            case JUMP_ATTACK:
                updateLeap(delta, platforms, polygonPlatforms, true);
                break;
            case LANDING:
                velocity.x = 0f;
                applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);
                if (stateTime >= getScaledDuration(LANDING_DURATION)) {
                    finishMove();
                }
                break;
            default:
                break;
        }
        updateDerivedBounds();
    }
    private void updateTarget(Rectangle knightBounds) {
        if (knightBounds == null) {
            return;
        }
        lastKnightCenterX = knightBounds.x + knightBounds.width / 2f;
        lastKnightCenterY = knightBounds.y + knightBounds.height / 2f;
        float dx = lastKnightCenterX - getCenterX();
        float dy = lastKnightCenterY - getCenterY();
        float range = detectionRange;
        if (dx * dx + dy * dy <= range * range) {
            hasSeenKnight = true;
        }
    }
    private void updateIdle(float delta, Rectangle knightBounds, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        velocity.x = 0f;
        applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);
        updateDirectionTowardLastTarget();
        if (state == State.TURNING) {
            return;
        }
        decisionTimer -= delta;
        if (decisionTimer <= 0f) {
            beginMove(chooseMove(knightBounds));
        }
    }
    private Move chooseMove(Rectangle knightBounds) {
        float dx = knightBounds == null ? lastKnightCenterX - getCenterX() : knightBounds.x + knightBounds.width / 2f - getCenterX();
        float absDx = Math.abs(dx);
        float roll = MathUtils.random();
        Move chosen;
        if (phaseTwo && roll < 0.22f) {
            chosen = Move.POWER_SLAM;
        } else if (absDx <= CLOSE_DISTANCE) {
            if (roll < 0.68f) {
                chosen = Move.MACE_SLAM;
            } else if (roll < 0.84f) {
                chosen = Move.DEFENSIVE_LEAP;
            } else {
                chosen = phaseTwo ? Move.POWER_SLAM : Move.OFFENSIVE_LEAP;
            }
        } else if (absDx >= FAR_DISTANCE) {
            if (roll < 0.48f) {
                chosen = Move.CHARGE_RUN;
            } else if (roll < 0.82f) {
                chosen = Move.OFFENSIVE_LEAP;
            } else {
                chosen = phaseTwo ? Move.POWER_SLAM : Move.CHARGE_RUN;
            }
        } else {
            if (roll < 0.36f) {
                chosen = Move.OFFENSIVE_LEAP;
            } else if (roll < 0.64f) {
                chosen = Move.MACE_SLAM;
            } else if (roll < 0.84f) {
                chosen = Move.CHARGE_RUN;
            } else {
                chosen = phaseTwo ? Move.POWER_SLAM : Move.DEFENSIVE_LEAP;
            }
        }
        if (chosen == lastMove && repeatedMoveCount >= 1) {
            if (chosen == Move.MACE_SLAM) {
                chosen = absDx > CLOSE_DISTANCE ? Move.CHARGE_RUN : Move.DEFENSIVE_LEAP;
            } else if (chosen == Move.CHARGE_RUN) {
                chosen = Move.OFFENSIVE_LEAP;
            } else if (chosen == Move.OFFENSIVE_LEAP) {
                chosen = Move.MACE_SLAM;
            } else if (chosen == Move.DEFENSIVE_LEAP) {
                chosen = Move.MACE_SLAM;
            } else {
                chosen = Move.CHARGE_RUN;
            }
        }
        return chosen;
    }
    private void beginMove(Move move) {
        currentMove = move == null ? Move.MACE_SLAM : move;
        if (currentMove == lastMove) {
            repeatedMoveCount++;
        } else {
            repeatedMoveCount = 0;
        }
        lastMove = currentMove;
        impactDone = false;
        attackActiveTimer = 0f;
        updateDirectionTowardLastTarget();
        if (currentMove == Move.MACE_SLAM) {
            changeState(State.ATTACK_ANTIC);
        } else if (currentMove == Move.CHARGE_RUN) {
            changeState(State.RUN_ANTIC);
        } else {
            changeState(State.JUMP_ANTIC);
        }
    }
    private void beginAttackSwing() {
        changeState(State.ATTACKING);
        queueEvent(Event.SWING);
    }
    private void updateAttack(float delta, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        velocity.x = 0f;
        applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);
        if (!impactDone && stateTime >= getScaledDuration(ATTACK_IMPACT_TIME)) {
            impactDone = true;
            attackActiveTimer = ATTACK_ACTIVE_TIME;
            queueEvent(Event.SLAM_IMPACT);
        }
        if (attackActiveTimer > 0f) {
            attackActiveTimer = Math.max(0f, attackActiveTimer - delta);
        }
        if (stateTime >= getScaledDuration(ATTACK_DURATION)) {
            attackActiveTimer = 0f;
            changeState(State.ATTACK_RECOVER);
        }
    }
    private void beginRun() {
        changeState(State.RUNNING);
        runSpeed = BASE_RUN_SPEED * getSpeedMultiplier();
        velocity.x = direction * runSpeed;
        queueEvent(Event.CHARGE);
    }
    private void updateRun(float delta, Rectangle knightBounds, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        velocity.x = direction * runSpeed;
        boolean hitWall = moveAxis(velocity.x * delta, 0f, platforms, polygonPlatforms);
        applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);
        float knightCenterX = knightBounds == null ? lastKnightCenterX : knightBounds.x + knightBounds.width / 2f;
        float dx = knightCenterX - getCenterX();
        if (hitWall || stateTime >= getScaledDuration(RUN_DURATION) || Math.signum(dx) != Math.signum(direction) || Math.abs(dx) < 95f) {
            velocity.x = 0f;
            finishMove();
        }
    }
    private void beginLeapVelocity(Rectangle knightBounds) {
        float targetCenterX = knightBounds == null ? lastKnightCenterX : knightBounds.x + knightBounds.width / 2f;
        float dx = targetCenterX - getCenterX();
        updateDirectionToward(targetCenterX);
        boolean defensive = currentMove == Move.DEFENSIVE_LEAP;
        float speedX;
        if (defensive) {
            int awayDirection = dx < 0f ? 1 : -1;
            direction = awayDirection;
            speedX = awayDirection * DEFENSIVE_JUMP_SPEED_X * getSpeedMultiplier();
        } else {
            speedX = direction * OFFENSIVE_JUMP_SPEED_X * getSpeedMultiplier();
        }
        velocity.x = speedX;
        velocity.y = currentMove == Move.POWER_SLAM ? POWER_JUMP_SPEED_Y : JUMP_SPEED_Y;
        grounded = false;
        changeState(State.JUMPING);
        queueEvent(Event.JUMP);
    }
    private void updateLeap(float delta, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms, boolean attacking) {
        boolean hitHorizontal = moveAxis(velocity.x * delta, 0f, platforms, polygonPlatforms);
        if (hitHorizontal) {
            velocity.x = 0f;
        }
        if (!grounded) {
            velocity.y += GRAVITY * delta;
        }
        boolean hitVertical = moveAxis(0f, velocity.y * delta, platforms, polygonPlatforms);
        if (hitVertical) {
            if (velocity.y <= 0f) {
                velocity.y = 0f;
                grounded = true;
                landFromLeap();
                return;
            }
            velocity.y = 0f;
        }
        grounded = isTouchingGround(platforms, polygonPlatforms);
        if (grounded && velocity.y <= 0f && stateTime > 0.12f) {
            landFromLeap();
            return;
        }
        if (!attacking && velocity.y <= 50f && stateTime >= 0.18f) {
            changeState(State.JUMP_ATTACK);
        }
    }
    private void landFromLeap() {
        velocity.set(0f, 0f);
        if (currentMove == Move.POWER_SLAM) {
            spawnShockwaves();
            queueEvent(Event.POWER_SLAM_IMPACT);
            queueEvent(Event.CEILING_BREAK);
        } else {
            queueEvent(Event.LAND);
        }
        changeState(State.LANDING);
    }
    private void spawnShockwaves() {
        float y = bounds.y - 15f;
        float leftX = bounds.x - 108f;
        float rightX = bounds.x + bounds.width + 4f;
        shockwaves.add(new Shockwave(leftX, y, -1, SHOCKWAVE_START_SPEED));
        shockwaves.add(new Shockwave(rightX, y, 1, SHOCKWAVE_START_SPEED));
    }
    private void finishMove() {
        currentMove = Move.NONE;
        decisionTimer = getDecisionDelay();
        changeState(State.IDLE);
    }
    private void beginDefensiveLeapFromDamage() {
        if (state == State.STUNNED || state == State.STUN_FALL || state == State.STUN_RECOVER || isDeathState()) {
            return;
        }
        currentMove = Move.DEFENSIVE_LEAP;
        repeatedMoveCount = 0;
        lastMove = Move.DEFENSIVE_LEAP;
        changeState(State.JUMP_ANTIC);
    }
    private void beginStun() {
        if (stunTriggered || isDeathState()) {
            return;
        }
        stunTriggered = true;
        phaseTwo = false;
        currentMove = Move.NONE;
        velocity.x = 0f;
        velocity.y = Math.max(velocity.y, 260f);
        changeState(State.STUN_FALL);
        queueEvent(Event.FALL);
    }
    private void beginStunRecover() {
        phaseTwo = true;
        currentMove = Move.NONE;
        changeState(State.STUN_RECOVER);
        queueEvent(Event.STUN_RECOVER);
        queueEvent(Event.RAGE);
    }
    private void beginDeath() {
        currentHealth = 0;
        currentMove = Move.NONE;
        velocity.x *= 0.25f;
        velocity.y = Math.max(velocity.y, 260f);
        shockwaves.clear();
        changeState(State.DEATH_HIT);
        queueEvent(Event.DEATH);
    }
    private void changeState(State newState) {
        if (state == newState) {
            return;
        }
        state = newState;
        stateTime = 0f;
        impactDone = false;
        if (newState != State.ATTACKING) {
            attackActiveTimer = 0f;
        }
    }
    private float getDecisionDelay() {
        return phaseTwo ? PHASE_TWO_IDLE_DECISION_TIME : IDLE_DECISION_TIME;
    }
    private float getSpeedMultiplier() {
        return phaseTwo ? PHASE_TWO_SPEED_MULTIPLIER : 1f;
    }
    private float getScaledDuration(float duration) {
        return phaseTwo ? duration / PHASE_TWO_SPEED_MULTIPLIER : duration;
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
    private boolean moveAxis(float amountX, float amountY, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        float distance = Math.max(Math.abs(amountX), Math.abs(amountY));
        if (distance <= 0f) {
            return false;
        }
        int steps = Math.max(1, (int) Math.ceil(distance / MOVE_STEP));
        float stepX = amountX / steps;
        float stepY = amountY / steps;
        boolean blocked = false;
        for (int i = 0; i < steps; i++) {
            float oldX = position.x;
            float oldY = position.y;
            position.x += stepX;
            position.y += stepY;
            updateBounds();
            clampToArena();
            updateBounds();
            if (overlapsAny(bounds, platforms, polygonPlatforms)) {
                position.x = oldX;
                position.y = oldY;
                updateBounds();
                blocked = true;
                break;
            }
        }
        return blocked;
    }
    private void clampToArena() {
        if (arenaBounds == null) {
            return;
        }
        if (position.x < arenaBounds.x) {
            position.x = arenaBounds.x;
        }
        float maxX = arenaBounds.x + arenaBounds.width - bounds.width;
        if (position.x > maxX) {
            position.x = maxX;
        }
    }
    private boolean isTouchingGround(Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        probeBounds.set(bounds.x + 8f, bounds.y - GROUND_CHECK_DISTANCE, Math.max(2f, bounds.width - 16f), GROUND_CHECK_DISTANCE + 1f);
        if (overlapsAny(probeBounds, platforms, polygonPlatforms)) {
            return true;
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
        if (polygon == null) {
            return false;
        }
        Rectangle polygonBounds = polygon.getBoundingRectangle();
        if (!rect.overlaps(polygonBounds)) {
            return false;
        }
        float[] vertices = polygon.getTransformedVertices();
        float rx1 = rect.x;
        float ry1 = rect.y;
        float rx2 = rect.x + rect.width;
        float ry2 = rect.y + rect.height;
        if (polygon.contains(rx1, ry1) || polygon.contains(rx2, ry1) || polygon.contains(rx2, ry2) || polygon.contains(rx1, ry2)) {
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
    private boolean segmentsIntersect(float ax, float ay, float bx, float by, float cx, float cy, float dx, float dy) {
        float d1 = cross(ax, ay, bx, by, cx, cy);
        float d2 = cross(ax, ay, bx, by, dx, dy);
        float d3 = cross(cx, cy, dx, dy, ax, ay);
        float d4 = cross(cx, cy, dx, dy, bx, by);
        if (((d1 > EPS && d2 < -EPS) || (d1 < -EPS && d2 > EPS)) && ((d3 > EPS && d4 < -EPS) || (d3 < -EPS && d4 > EPS))) {
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
        return px >= Math.min(ax, bx) - EPS && px <= Math.max(ax, bx) + EPS && py >= Math.min(ay, by) - EPS && py <= Math.max(ay, by) + EPS;
    }
    private void updateShockwaves(float delta) {
        for (int i = shockwaves.size - 1; i >= 0; i--) {
            Shockwave wave = shockwaves.get(i);
            if (wave == null) {
                shockwaves.removeIndex(i);
                continue;
            }
            wave.update(delta);
            if (!wave.isAlive()) {
                shockwaves.removeIndex(i);
            }
        }
    }
    private void updateRecentHits(float delta) {
        if (recentHitTimer > 0f) {
            recentHitTimer = Math.max(0f, recentHitTimer - delta);
            if (recentHitTimer <= 0f) {
                recentHitCount = 0;
            }
        }
    }

    private void updateDirectionTowardLastTarget() {
        updateDirectionToward(lastKnightCenterX);
    }

    private void updateDirectionToward(float x) {
        float dx = x - getCenterX();
        if (Math.abs(dx) > 4f) {
            int desired = dx < 0f ? -1 : 1;

            if (desired != direction && state == State.IDLE) {
                direction = desired;
                changeState(State.TURNING);
            } else {
                direction = desired;
            }
        }
    }

    private boolean isDeathState() {
        return state == State.DEATH_FALL || state == State.DEATH_HIT || state == State.DEATH_LAND || state == State.DEAD;
    }
    private void updateBounds() {
        bounds.set(position.x, position.y, bounds.width, bounds.height);
    }

    private void updateDerivedBounds() {
        armorDamageBounds.set(bounds.x + 12f, bounds.y + 6f, Math.max(12f, bounds.width - 24f), Math.max(12f, bounds.height - 16f));

        float vulnerableWidth = Math.max(18f, bounds.width * 0.34f);
        float vulnerableHeight = Math.max(16f, bounds.height * 0.22f);
        float vulnerableX = direction < 0 ? bounds.x + bounds.width * 0.13f : bounds.x + bounds.width * 0.53f;
        float vulnerableY = bounds.y + bounds.height * 0.24f;
        vulnerableBounds.set(vulnerableX, vulnerableY, vulnerableWidth, vulnerableHeight);
        if (direction < 0) {
            maceDamageBounds.set(bounds.x - 126f, bounds.y + 2f, 154f, 92f);
        } else {
            maceDamageBounds.set(bounds.x + bounds.width - 28f, bounds.y + 2f, 154f, 92f);
        }
    }

    private void queueEvent(Event event) {
        eventQueue.add(event);
    }

    public Event pollEvent() {
        if (eventQueue.size == 0) {
            return null;
        }
        return eventQueue.removeIndex(0);
    }

    public boolean canDamageKnight(Rectangle knightBounds) {
        if (knightBounds == null || !isAlive() || isVulnerableState()) {
            return false;
        }

        if (state == State.RUNNING && armorDamageBounds.overlaps(knightBounds)) {
            return true;
        }
        if (attackActiveTimer > 0f && maceDamageBounds.overlaps(knightBounds)) {
            return true;
        }

        if ((state == State.JUMP_ATTACK || state == State.LANDING) && armorDamageBounds.overlaps(knightBounds)) {
            return true;
        }

        for (Shockwave wave : shockwaves) {
            if (wave != null && wave.isAlive() && wave.getBounds().overlaps(knightBounds)) {
                return true;
            }
        }
        return armorDamageBounds.overlaps(knightBounds);
    }

    private boolean isVulnerableState() {
        return state == State.STUN_FALL || state == State.STUNNED || state == State.STUN_RECOVER || isDeathState();
    }

    @Override
    public Rectangle getBounds() {
        updateBounds();
        updateDerivedBounds();
        if (state == State.STUNNED) {
            return vulnerableBounds;
        }

        return bounds;
    }

    public Rectangle getBodyBounds() {
        updateBounds();
        return bounds;
    }
    public Rectangle getDamageBounds() {
        updateDerivedBounds();
        return armorDamageBounds;
    }

    public Rectangle getMaceDamageBounds() {
        updateDerivedBounds();
        return maceDamageBounds;
    }

    public Rectangle getVulnerableBounds() {
        updateDerivedBounds();
        return vulnerableBounds;
    }
    public Array<Shockwave> getShockwaves() {
        return shockwaves;
    }

    @Override
    public void takeDamage(int damage) {
        takeDamage(damage, 0f, 0f);
    }

    public void takeDamage(int damage, float knockbackX, float knockbackY) {
        if (damage <= 0 || state == State.DEAD || isDeathState()) {
            return;
        }
        boolean vulnerableHit = state == State.STUNNED;
        currentHealth = Math.max(0, currentHealth - damage);

        if (vulnerableHit) {
            queueEvent(Event.HEAD_HIT);
        } else if (!stunTriggered && currentHealth <= maxHealth / 2) {
            queueEvent(Event.ARMOR_FINAL_HIT);
        } else {
            queueEvent(Event.ARMOR_HIT);
        }

        recentHitTimer = 1.1f;
        recentHitCount++;
        if (currentHealth <= 0) {
            beginDeath();
            return;
        }

        if (!stunTriggered && currentHealth <= maxHealth / 2) {
            beginStun();
            return;
        }

        if (!vulnerableHit && recentHitCount >= 3) {
            recentHitCount = 0;
            beginDefensiveLeapFromDamage();
        }
    }
    @Override
    public boolean isAlive() {
        return currentHealth > 0 && state != State.DEAD && state != State.DEATH_LAND && state != State.DEATH_HIT && state != State.DEATH_FALL;
    }

    public boolean isReadyToRemove() {
        return false;
    }

    public void respawnIfDeadAndFar(Rectangle knightBounds, float respawnDistance) {
    }
    public boolean isDefeated() {
        return currentHealth <= 0 || state == State.DEAD || state == State.DEATH_LAND || state == State.DEATH_FALL || state == State.DEATH_HIT;
    }

    public void resetToSpawn() {
        if (isDefeated()) {
            shockwaves.clear();
            eventQueue.clear();
            return;
        }

        position.set(spawnPosition);
        velocity.set(0f, 0f);
        currentHealth = maxHealth;
        direction = spawnDirection;
        state = State.IDLE;
        currentMove = Move.NONE;
        lastMove = Move.NONE;
        stateTime = 0f;
        decisionTimer = IDLE_DECISION_TIME;
        runSpeed = BASE_RUN_SPEED;
        attackActiveTimer = 0f;
        impactDone = false;
        grounded = false;
        hasSeenKnight = false;
        phaseTwo = false;
        stunTriggered = false;
        repeatedMoveCount = 0;
        recentHitCount = 0;
        recentHitTimer = 0f;
        shockwaves.clear();
        eventQueue.clear();
        updateBounds();
        updateDerivedBounds();
    }
    public void setArenaBounds(Rectangle arenaBounds) {
        this.arenaBounds = arenaBounds == null ? null : new Rectangle(arenaBounds);
    }

    public Rectangle getArenaBounds() {
        return arenaBounds;
    }

    public State getState() {
        return state;
    }
    public Move getCurrentMove() {
        return currentMove;
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

    public float getSpriteYOffset() {
        return spriteYOffset;
    }
    public boolean isFacingRight() {
        return direction > 0;
    }

    public boolean hasActiveMaceDamage() {
        return attackActiveTimer > 0f;
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

    public boolean isPhaseTwo() {
        return phaseTwo;
    }
    public boolean hasSeenKnight() {
        return hasSeenKnight;
    }

    public float getCenterX() {
        return bounds.x + bounds.width / 2f;
    }

    public float getCenterY() {
        return bounds.y + bounds.height / 2f;
    }
}
