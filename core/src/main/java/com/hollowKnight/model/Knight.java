package com.hollowKnight.model;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
public class Knight {

    public enum State {
        IDLE, RUNNING, JUMPING, DOUBLE_JUMPING, FALLING, ATTACKING, DASHING, LOOKING_UP, HEALING, CASTING
    }
    public enum AttackDirection {
        FORWARD, UP, DOWN
    }

    private Vector2 position;
    private Vector2 velocity;
    private State currentState;
    private boolean facingLeft;

    private boolean knockbackActive = false;
    private float knockbackTimer = 0f;
    private float knockbackDuration = 0.22f;
    private float damageInvulnerabilityTimer = 0f;

    private static final float DAMAGE_INVULNERABILITY_DURATION = 2.0f;
    private static final float DAMAGE_BLINKS_PER_SECOND = 12f;

    private int maxHealth;
    private int currentHealth;
    private int maxSoul;
    private int currentSoul;

    private static final int DEFAULT_MAX_SOUL = 99;
    private static final int HEAL_SOUL_COST = 33;
    public static final int DEFAULT_SOUL_GAIN_ON_HIT = 11;
    public static final int DEFAULT_ATTACK_DAMAGE = 1;
    public static final float DEFAULT_DASH_DURATION = 0.48f;
    public static final float DEFAULT_DASH_COOLDOWN = 0.60f;
    public static final float DEFAULT_ATTACK_DURATION = 0.34f;
    public static final float DEFAULT_ATTACK_COOLDOWN = 0.36f;
    public static final float DEFAULT_HEAL_DURATION = 1.50f;
    private final float movementSpeed = 250f;

    private float gravity = -1500f;
    private float jumpSpeed = 950f;
    private boolean isGrounded = false;
    private boolean doubleJumpAvailable = false;
    private boolean doubleJumpAnimationActive = false;

    private boolean isDashing = false;
    private float dashSpeed = 420f;
    private float dashDuration = DEFAULT_DASH_DURATION;
    private float dashTime = 0f;
    private float dashCooldown = DEFAULT_DASH_COOLDOWN;
    private float dashCooldownTimer = 0f;
    private boolean dashAvailable = true;
    private boolean attacking = false;
    private boolean attackHasHit = false;
    private boolean lookingUp = false;

    private float attackTimer = 0f;
    private float attackDuration = DEFAULT_ATTACK_DURATION;

    private float attackCooldown = DEFAULT_ATTACK_COOLDOWN;
    private float attackCooldownTimer = 0f;
    private boolean healing = false;
    private float healTimer = 0f;
    private float healDuration = DEFAULT_HEAL_DURATION;

    private boolean casting = false;
    private float castTimer = 0f;
    private float castDuration = 0f;

    private int attackDamage = DEFAULT_ATTACK_DAMAGE;
    private int soulGainOnHit = DEFAULT_SOUL_GAIN_ON_HIT;
    private AttackDirection currentAttackDirection = AttackDirection.FORWARD;
    private static final float HITBOX_WIDTH = 40f;
    private static final float HITBOX_HEIGHT = 70f;

    private static final float MOVE_STEP = 3f;
    private static final float GROUND_CHECK_DISTANCE = 2f;
    private static final float EPS = 0.0001f;
    private static final float ATTACK_RANGE = 135f;
    private static final float ATTACK_HEIGHT = 85f;

    private static final float UP_ATTACK_WIDTH = 85f;
    private static final float UP_ATTACK_HEIGHT = 135f;

    private static final float POGO_ATTACK_WIDTH = 36f;
    private static final float POGO_ATTACK_HEIGHT = 60f;
    private static final float POGO_BOUNCE_SPEED = 900f;
    private Rectangle knightBounds = new Rectangle(0, 0, HITBOX_WIDTH, HITBOX_HEIGHT);
    private Rectangle attackBounds = new Rectangle();

    public Knight(float startX, float startY) {
        this.position = new Vector2(startX, startY);
        this.velocity = new Vector2(0, 0);
        this.currentState = State.IDLE;
        this.facingLeft = false;

        this.maxHealth = 5;
        this.currentHealth = maxHealth;
        this.maxSoul = DEFAULT_MAX_SOUL;
        this.currentSoul = this.maxSoul;

        updateBounds();
    }
    public void update(float delta, Array<Rectangle> platforms) {
        update(delta, platforms, null);
    }
    public void update(float delta, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        updateAttackTimers(delta);
        updateCastingTimer(delta);

        if (damageInvulnerabilityTimer > 0f) {
            damageInvulnerabilityTimer = Math.max(0f, damageInvulnerabilityTimer - delta);
        }

        if (knockbackActive) {
            knockbackTimer -= delta;
            if (knockbackTimer <= 0f) {
                knockbackActive = false;
                knockbackTimer = 0f;
                velocity.x = 0f;
            }
        }

        if (dashCooldownTimer > 0f) {
            dashCooldownTimer -= delta;
        } else if (isGrounded) {
            dashAvailable = true;
        }

        if (knockbackActive && healing) {
            cancelHealing();
        }
        if (knockbackActive && casting) {
            cancelSpellCast();
        }

        if (casting) {
            velocity.x = 0f;
            lookingUp = false;
            currentState = State.CASTING;
        }

        if (healing) {
            velocity.x = 0f;
            lookingUp = false;
            if (!canContinueHealing()) {
                cancelHealing();
            } else {
                healTimer += delta;
                currentState = State.HEALING;
                if (healTimer >= healDuration) {
                    currentSoul = Math.max(0, currentSoul - HEAL_SOUL_COST);
                    currentHealth = Math.min(maxHealth, currentHealth + 1);
                    healing = false;
                    healTimer = 0f;
                    currentState = State.IDLE;
                }
            }
        }

        float intendedVelX = velocity.x;
        if (isDashing) {
            dashTime -= delta;
            velocity.y = 0;

            if (facingLeft) {
                velocity.x = -dashSpeed;
            } else {
                velocity.x = dashSpeed;
            }

            intendedVelX = velocity.x;
            if (dashTime <= 0f) {
                isDashing = false;
                velocity.x = 0;
                intendedVelX = 0;
                currentState = State.IDLE;
            }

        } else {
            if (!isGrounded) {
                velocity.y += gravity * delta;
            }
        }

        float moveX = velocity.x * delta;
        boolean hitHorizontal = moveAxis(moveX, 0, platforms, polygonPlatforms);
        if (hitHorizontal) {
            velocity.x = 0;

            if (isDashing) {
                isDashing = false;
                dashTime = 0;
                intendedVelX = 0;
                currentState = State.IDLE;
            }
        }
        if (!isDashing) {
            velocity.x = intendedVelX;
        }
        float moveY = velocity.y * delta;
        boolean hitVertical = moveAxis(0, moveY, platforms, polygonPlatforms);

        if (hitVertical) {
            if (velocity.y <= 0) {
                velocity.y = 0;
                doubleJumpAvailable = true;
                doubleJumpAnimationActive = false;

                if (dashCooldownTimer <= 0f) {
                    dashAvailable = true;
                }
            } else if (velocity.y > 0 && !isDashing) {
                velocity.y = 0;
                doubleJumpAnimationActive = false;
            }
        }

        isGrounded = isTouchingGround(platforms, polygonPlatforms);

        if (isGrounded) {
            doubleJumpAvailable = true;
            doubleJumpAnimationActive = false;
            if (dashCooldownTimer <= 0f) {
                dashAvailable = true;
            }

            if (velocity.y < 0) {
                velocity.y = 0;
            }
        }

        if (!attacking && !isDashing && !healing && !casting) {
            if (knockbackActive) {
                currentState = velocity.y > 0f ? State.JUMPING : State.FALLING;
            } else if (!isGrounded) {
                if (velocity.y > 0) {
                    if (doubleJumpAnimationActive) {
                        currentState = State.DOUBLE_JUMPING;
                    } else {
                        currentState = State.JUMPING;
                    }
                } else {
                    currentState = State.FALLING;
                    doubleJumpAnimationActive = false;
                }
            } else if (Math.abs(velocity.x) > 0.1f) {
                currentState = State.RUNNING;
            } else if (lookingUp) {
                currentState = State.LOOKING_UP;
            } else {
                currentState = State.IDLE;
            }
        }
        if (position.y < -100) {
            position.y = 27000f;
            velocity.y = 0;
            updateBounds();
        }
        if (attacking) {
            updateAttackBounds();
        }
    }

    private void updateCastingTimer(float delta) {
        if (!casting) {
            return;
        }
        castTimer -= delta;

        if (castTimer <= 0f) {
            casting = false;
            castTimer = 0f;
            castDuration = 0f;

            if (currentState == State.CASTING) {
                if (!isGrounded) {
                    currentState = velocity.y > 0f ? State.JUMPING : State.FALLING;
                } else {
                    currentState = State.IDLE;
                }
            }
        }
    }
    private void updateAttackTimers(float delta) {
        if (attackCooldownTimer > 0f) {
            attackCooldownTimer -= delta;
        }

        if (attacking) {
            attackTimer -= delta;

            if (attackTimer <= 0f) {
                attacking = false;
                attackHasHit = false;
                if (currentState == State.ATTACKING) {
                    if (!isGrounded) {
                        if (velocity.y > 0) {
                            if (doubleJumpAnimationActive) {
                                currentState = State.DOUBLE_JUMPING;
                            } else {
                                currentState = State.JUMPING;
                            }
                        } else {
                            currentState = State.FALLING;
                            doubleJumpAnimationActive = false;
                        }

                    } else if (Math.abs(velocity.x) > 0.1f) {
                        currentState = State.RUNNING;
                    } else if (lookingUp) {
                        currentState = State.LOOKING_UP;
                    } else {
                        currentState = State.IDLE;
                    }
                }
            }
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
            if (overlapsAny(platforms, polygonPlatforms)) {
                position.x = oldX;
                position.y = oldY;
                updateBounds();
                return true;
            }
        }

        return false;
    }

    private boolean isTouchingGround(Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        float oldY = position.y;
        position.y -= GROUND_CHECK_DISTANCE;
        updateBounds();
        boolean touching = overlapsAny(platforms, polygonPlatforms);

        position.y = oldY;
        updateBounds();
        return touching;
    }

    private void updateBounds() {
        knightBounds.set(position.x, position.y, HITBOX_WIDTH, HITBOX_HEIGHT);
    }

    private boolean overlapsAny(Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        if (platforms != null) {
            for (Rectangle platform : platforms) {
                if (knightBounds.overlaps(platform)) {
                    return true;
                }
            }
        }
        if (polygonPlatforms != null) {
            for (Polygon polygon : polygonPlatforms) {
                if (rectOverlapsPolygon(knightBounds, polygon)) {
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
    public void attack(boolean upPressed, boolean downPressed) {
        if (knockbackActive) return;
        if (isDashing) return;
        if (healing) return;
        if (casting) return;
        if (attackCooldownTimer > 0f) return;
        if (attacking) return;

        attacking = true;
        attackHasHit = false;
        lookingUp = false;

        attackTimer = attackDuration;
        attackCooldownTimer = attackCooldown;
        if (upPressed) {
            currentAttackDirection = AttackDirection.UP;
        } else if (downPressed && !isGrounded) {
            currentAttackDirection = AttackDirection.DOWN;
        } else {
            currentAttackDirection = AttackDirection.FORWARD;
        }

        currentState = State.ATTACKING;
        updateAttackBounds();
    }

    private void updateAttackBounds() {
        if (currentAttackDirection == AttackDirection.UP) {
            attackBounds.set(
                position.x + (HITBOX_WIDTH / 2f) - (UP_ATTACK_WIDTH / 2f),
                position.y + HITBOX_HEIGHT,
                UP_ATTACK_WIDTH,
                UP_ATTACK_HEIGHT
            );
        } else if (currentAttackDirection == AttackDirection.DOWN) {
            attackBounds.set(
                position.x + (HITBOX_WIDTH / 2f) - (POGO_ATTACK_WIDTH / 2f),
                position.y - POGO_ATTACK_HEIGHT,
                POGO_ATTACK_WIDTH,
                POGO_ATTACK_HEIGHT
            );

        } else {
            if (facingLeft) {
                attackBounds.set(
                    position.x - ATTACK_RANGE,
                    position.y + 15f,
                    ATTACK_RANGE,
                    ATTACK_HEIGHT
                );
            } else {
                attackBounds.set(
                    position.x + HITBOX_WIDTH,
                    position.y + 15f,
                    ATTACK_RANGE,
                    ATTACK_HEIGHT
                );
            }
        }
    }
    public void setLookingUp(boolean lookingUp) {
        if (attacking || isDashing || healing || casting || knockbackActive) {
            return;
        }

        this.lookingUp = lookingUp;

        if (lookingUp && isGrounded && Math.abs(velocity.x) < 0.1f) {
            currentState = State.LOOKING_UP;
        }
    }
    public boolean isLookingUp() {
        return lookingUp && !attacking && !isDashing && !healing;
    }

    public boolean isAttacking() {
        return attacking;
    }

    public boolean canAttackHit() {
        return attacking && !attackHasHit;
    }
    public void markAttackHit() {
        attackHasHit = true;
    }

    public boolean isPogoAttack() {
        return attacking && currentAttackDirection == AttackDirection.DOWN;
    }

    public Rectangle getAttackBounds() {
        updateAttackBounds();
        return attackBounds;
    }
    public int getAttackDamage() {
        return attackDamage;
    }
    public void pogoBounce() {
        cancelHealing();
        velocity.y = POGO_BOUNCE_SPEED;
        isGrounded = false;
        doubleJumpAvailable = true;
        doubleJumpAnimationActive = false;
        attackHasHit = true;
    }

    public AttackDirection getCurrentAttackDirection() {
        return currentAttackDirection;
    }
    public Rectangle getBounds() {
        updateBounds();
        return knightBounds;
    }

    public boolean isGrounded() {
        return isGrounded;
    }

    public void jump() {
        if (knockbackActive) return;
        if (isDashing) return;
        if (healing) return;
        if (casting) return;
        lookingUp = false;

        if (isGrounded) {
            velocity.y = jumpSpeed;
            isGrounded = false;
            doubleJumpAnimationActive = false;
            currentState = State.JUMPING;
        } else if (doubleJumpAvailable) {
            velocity.y = jumpSpeed;
            doubleJumpAvailable = false;
            doubleJumpAnimationActive = true;
            currentState = State.DOUBLE_JUMPING;
        }
    }

    public void cutJump() {
        if (!isGrounded && velocity.y > 0 && !isDashing) {
            velocity.y *= 0.4f;
        }
    }
    public void dash() {
        if (knockbackActive) return;
        if (healing) return;
        if (casting) return;

        if (dashAvailable && !isDashing) {
            lookingUp = false;
            isDashing = true;
            dashAvailable = false;
            dashTime = dashDuration;
            dashCooldownTimer = dashCooldown;
            currentState = State.DASHING;
        }
    }
    public boolean isDashing() {
        return isDashing;
    }
    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public State getCurrentState() {
        return currentState;
    }
    public void setCurrentState(State state) {
        if (healing && state != State.HEALING) {
            return;
        }

        if (casting && state != State.CASTING) {
            return;
        }

        if (!attacking || state == State.ATTACKING) {
            this.currentState = state;
        }
    }
    public boolean isFacingLeft() {
        return facingLeft;
    }

    public void setFacingLeft(boolean facingLeft) {
        this.facingLeft = facingLeft;
    }

    public float getMovementSpeed() {
        return movementSpeed;
    }
    public int getCurrentHealth() {
        return currentHealth;
    }
    public int getMaxHealth() {
        return maxHealth;
    }

    public int getCurrentSoul() {
        return currentSoul;
    }
    public int getMaxSoul() {
        return maxSoul;
    }

    public int getHealSoulCost() {
        return HEAL_SOUL_COST;
    }

    public float getSoulPercent() {
        if (maxSoul <= 0) {
            return 0f;
        }
        return Math.max(0f, Math.min(1f, currentSoul / (float) maxSoul));
    }

    public boolean canHealWithSoul() {
        return currentHealth > 0 &&
            currentHealth < maxHealth &&
            currentSoul >= HEAL_SOUL_COST &&
            isGrounded &&
            !isDashing &&
            !attacking &&
            !healing &&
            !casting &&
            Math.abs(velocity.x) < 0.1f;
    }

    private boolean canContinueHealing() {
        return currentHealth > 0 &&
            currentHealth < maxHealth &&
            currentSoul >= HEAL_SOUL_COST &&
            isGrounded &&
            !isDashing &&
            !attacking &&
            !casting &&
            Math.abs(velocity.x) < 0.1f;
    }
    public boolean startHealing() {
        if (!canHealWithSoul()) {
            return false;
        }

        healing = true;
        healTimer = 0f;
        lookingUp = false;
        velocity.x = 0f;
        currentState = State.HEALING;
        return true;
    }
    public void cancelHealing() {
        if (!healing) {
            return;
        }
        healing = false;
        healTimer = 0f;

        if (currentState == State.HEALING) {
            currentState = State.IDLE;
        }
    }

    public boolean healWithSoul() {
        return startHealing();
    }
    public boolean isHealing() {
        return healing;
    }

    public float getHealProgress() {
        if (!healing || healDuration <= 0f) {
            return 0f;
        }

        return Math.max(0f, Math.min(1f, healTimer / healDuration));
    }
    public float getHealDuration() {
        return healDuration;
    }

    public void configureCharmModifiers(
        int soulGainOnHit,
        int attackDamage,
        float dashDuration,
        float dashCooldown,
        float attackDuration,
        float attackCooldown,
        float healDuration
    ) {
        this.soulGainOnHit = Math.max(0, soulGainOnHit);
        this.attackDamage = Math.max(1, attackDamage);
        this.dashDuration = Math.max(0.08f, dashDuration);
        this.dashCooldown = Math.max(0.08f, dashCooldown);
        this.attackDuration = Math.max(0.06f, attackDuration);
        this.attackCooldown = Math.max(0.06f, attackCooldown);
        this.healDuration = Math.max(0.20f, healDuration);

        if (dashCooldownTimer > this.dashCooldown) {
            dashCooldownTimer = this.dashCooldown;
        }
        if (attackCooldownTimer > this.attackCooldown) {
            attackCooldownTimer = this.attackCooldown;
        }
    }
    public int getSoulGainOnHit() {
        return soulGainOnHit;
    }
    public float getDashDuration() {
        return dashDuration;
    }

    public float getDashCooldown() {
        return dashCooldown;
    }

    public float getAttackDuration() {
        return attackDuration;
    }

    public float getAttackCooldown() {
        return attackCooldown;
    }

    public void addSoulOnHit() {
        addSoul(soulGainOnHit);
    }

    public void addSoul(int amount) {
        if (amount <= 0 || currentHealth <= 0) {
            return;
        }

        currentSoul = Math.min(maxSoul, currentSoul + amount);
    }
    public void useSoul(int amount) {
        if (amount <= 0) {
            return;
        }

        currentSoul = Math.max(0, currentSoul - amount);
    }

    public boolean canCastSpell(int soulCost) {
        return currentHealth > 0 &&
            soulCost > 0 &&
            currentSoul >= soulCost &&
            !knockbackActive &&
            !isDashing &&
            !attacking &&
            !healing &&
            !casting;
    }

    public boolean startSpellCast(int soulCost, float duration) {
        if (!canCastSpell(soulCost)) {
            return false;
        }

        currentSoul = Math.max(0, currentSoul - soulCost);
        casting = true;
        castDuration = Math.max(0.05f, duration);
        castTimer = castDuration;
        lookingUp = false;
        velocity.x = 0f;
        currentState = State.CASTING;
        return true;
    }

    public void cancelSpellCast() {
        if (!casting) {
            return;
        }

        casting = false;
        castTimer = 0f;
        castDuration = 0f;
        if (currentState == State.CASTING) {
            currentState = isGrounded ? State.IDLE : State.FALLING;
        }
    }

    public boolean isCasting() {
        return casting;
    }

    public float getCastProgress() {
        if (!casting || castDuration <= 0f) {
            return 0f;
        }

        return Math.max(0f, Math.min(1f, 1f - castTimer / castDuration));
    }

    public void respawnAt(float x, float y) {
        position.set(x, y);
        velocity.set(0f, 0f);

        isGrounded = false;
        doubleJumpAvailable = true;
        doubleJumpAnimationActive = false;

        isDashing = false;
        dashTime = 0f;
        attacking = false;
        attackHasHit = false;
        attackTimer = 0f;

        healing = false;
        healTimer = 0f;

        casting = false;
        castTimer = 0f;
        castDuration = 0f;

        knockbackActive = false;
        knockbackTimer = 0f;

        lookingUp = false;

        if (currentHealth > 0) {
            currentState = State.IDLE;
        }

        updateBounds();
    }

    public void resetForNewRun(float x, float y) {
        currentHealth = maxHealth;
        currentSoul = maxSoul;
        damageInvulnerabilityTimer = 0f;
        respawnAt(x, y);
        currentState = State.IDLE;
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = Math.max(0, Math.min(maxHealth, currentHealth));
    }

    public void setCurrentSoul(int currentSoul) {
        this.currentSoul = Math.max(0, Math.min(maxSoul, currentSoul));
    }

    public void takeDamage(int amount) {
        if (amount <= 0 || currentHealth <= 0 || damageInvulnerabilityTimer > 0f) {
            return;
        }

        cancelHealing();
        cancelSpellCast();
        this.currentHealth = Math.max(0, this.currentHealth - amount);

        if (currentHealth > 0) {
            damageInvulnerabilityTimer = DAMAGE_INVULNERABILITY_DURATION;
        } else {
            damageInvulnerabilityTimer = 0f;
            velocity.set(0f, 0f);
            isDashing = false;
            attacking = false;
            lookingUp = false;
            healing = false;
            healTimer = 0f;
            knockbackActive = false;
            knockbackTimer = 0f;
        }
    }

    public boolean canTakeDamage() {
        return currentHealth > 0 && damageInvulnerabilityTimer <= 0f;
    }

    public boolean isDamageInvulnerable() {
        return currentHealth > 0 && damageInvulnerabilityTimer > 0f;
    }

    public float getDamageInvulnerabilityTimer() {
        return damageInvulnerabilityTimer;
    }

    public boolean shouldDrawDuringDamageBlink() {
        if (currentHealth <= 0 || damageInvulnerabilityTimer <= 0f) {
            return true;
        }

        return ((int) (damageInvulnerabilityTimer * DAMAGE_BLINKS_PER_SECOND)) % 2 == 0;
    }

    public void applyKnockback(float knockbackX, float knockbackY) {
        applyKnockback(knockbackX, knockbackY, 0.22f);
    }

    public void applyKnockback(float knockbackX, float knockbackY, float duration) {
        if (currentHealth <= 0) {
            return;
        }

        cancelHealing();
        cancelSpellCast();

        knockbackActive = true;
        knockbackDuration = Math.max(0.05f, duration);
        knockbackTimer = knockbackDuration;

        isDashing = false;
        dashTime = 0f;
        attacking = false;
        attackHasHit = false;
        attackTimer = 0f;
        lookingUp = false;

        velocity.x = knockbackX;
        velocity.y = knockbackY;
        isGrounded = false;
        currentState = knockbackY > 0f ? State.JUMPING : State.FALLING;
    }

    public boolean isKnockbackActive() {
        return knockbackActive;
    }
}
