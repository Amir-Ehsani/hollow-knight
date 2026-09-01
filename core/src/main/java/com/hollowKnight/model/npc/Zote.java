package com.hollowKnight.model.npc;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Zote {

    public enum State {
        IDLE, TALKING, TURNING, ROLLING, ATTACKING, FALLING, GETTING_UP
    }

    public static final float DEFAULT_HITBOX_WIDTH = 38f;
    public static final float DEFAULT_HITBOX_HEIGHT = 62f;
    public static final float DEFAULT_DRAW_SCALE = 1.0f;
    public static final float DEFAULT_INTERACTION_RANGE = 120f;

    private static final float GRAVITY = -1500f;
    private static final float MOVE_STEP = 3f;
    private static final float GROUND_CHECK_DISTANCE = 3f;
    private static final float CLIFF_CHECK_DISTANCE_X = 10f;
    private static final float CLIFF_CHECK_DISTANCE_Y = 10f;
    private static final float TURN_DURATION = 0.22f;
    private static final float FALL_DURATION = 0.40f;
    private static final float GET_UP_DURATION = 0.62f;
    private static final float ATTACK_ANIMATION_DURATION = 0.48f;
    private static final float ANGRY_DURATION = 4.0f;
    private static final float ANGRY_SPEED = 135f;
    private static final float ATTACK_RANGE = 78f;
    private static final float EPS = 0.0001f;

    private final Vector2 position;
    private final Vector2 velocity;
    private final Vector2 spawnPosition;
    private final Rectangle bounds;
    private final Rectangle interactionBounds;
    private final Rectangle probeBounds;

    private final float drawScale;
    private final float interactionRange;
    private int direction;
    private final int spawnDirection;
    private State state;
    private float stateTime;
    private float angryTimer;
    private boolean grounded;
    private boolean firstDialogueCompleted;
    private int preceptIndex;

    private final String[] firstDialogue;
    private final String[] precepts;

    public Zote(float x, float y) {
        this(x, y, DEFAULT_HITBOX_WIDTH, DEFAULT_HITBOX_HEIGHT, 1, DEFAULT_DRAW_SCALE, DEFAULT_INTERACTION_RANGE);
    }

    public Zote(float x, float y, float width, float height, int direction, float drawScale, float interactionRange) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0f, 0f);
        this.spawnPosition = new Vector2(x, y);
        this.bounds = new Rectangle(x, y, Math.max(8f, width), Math.max(8f, height));
        this.interactionBounds = new Rectangle();
        this.probeBounds = new Rectangle();
        this.direction = direction < 0 ? -1 : 1;
        this.spawnDirection = this.direction;
        this.drawScale = Math.max(0.1f, drawScale);
        this.interactionRange = Math.max(32f, interactionRange);
        this.state = State.IDLE;
        this.stateTime = 0f;
        this.angryTimer = 0f;
        this.grounded = false;
        this.firstDialogueCompleted = false;
        this.preceptIndex = 0;
        this.firstDialogue = new String[] {
            "Stand still and listen, little wanderer. You are in the presence of Zote the Mighty.",
            "I have crossed this ruined kingdom alone, guided only by my peerless instinct and unmatched courage.",
            "Remember this well: a true warrior never admits defeat, never apologizes, and never asks for directions."
        };
        this.precepts = new String[] {
            "Precept One: Always win your battles.",
            "Precept Two: Never let them laugh at you.",
            "Precept Three: Keep your weapon sharp and your name sharper.",
            "Precept Four: A strong voice is better than a strong argument.",
            "Precept Five: Do not trust maps. Trust Zote.",
            "Precept Six: If danger is near, make yourself look larger.",
            "Precept Seven: Never bow, except to dodge poorly.",
            "Precept Eight: The best plan is the one you claim was yours."
        };
        updateBounds();
        updateInteractionBounds();
    }

    public void update(float delta, Rectangle knightBounds, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        stateTime += delta;

        if (angryTimer > 0f && state != State.TALKING) {
            angryTimer = Math.max(0f, angryTimer - delta);
        }

        if (state == State.TALKING) {
            velocity.x = 0f;
            faceKnight(knightBounds);
            applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);
            updateInteractionBounds();
            return;
        }

        if (state == State.FALLING) {
            updateFalling(delta, platforms, polygonPlatforms);
            updateInteractionBounds();
            return;
        }

        if (state == State.GETTING_UP) {
            updateGettingUp(delta, platforms, polygonPlatforms);
            updateInteractionBounds();
            return;
        }

        if (state == State.TURNING) {
            updateTurning(delta, platforms, polygonPlatforms);
            updateInteractionBounds();
            return;
        }

        if (angryTimer > 0f) {
            updateAngry(delta, knightBounds, platforms, polygonPlatforms);
            updateInteractionBounds();
            return;
        }

        state = State.IDLE;
        velocity.x = 0f;
        faceKnightIfClose(knightBounds);
        applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);
        updateInteractionBounds();
    }

    private void updateFalling(float delta, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        moveAxis(velocity.x * delta, 0f, platforms, polygonPlatforms);
        velocity.x *= Math.max(0f, 1f - delta * 4.2f);
        applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);

        if (grounded && stateTime >= FALL_DURATION) {
            state = State.GETTING_UP;
            stateTime = 0f;
            velocity.x = 0f;
        }
    }

    private void updateGettingUp(float delta, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        velocity.x = 0f;
        applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);

        if (stateTime >= GET_UP_DURATION) {
            state = angryTimer > 0f ? State.ROLLING : State.IDLE;
            stateTime = 0f;
        }
    }

    private void updateTurning(float delta, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        velocity.x = 0f;
        applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);

        if (stateTime >= TURN_DURATION) {
            direction *= -1;
            state = angryTimer > 0f ? State.ROLLING : State.IDLE;
            stateTime = 0f;
        }
    }

    private void updateAngry(float delta, Rectangle knightBounds, Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        if (knightBounds != null) {
            float myCenterX = bounds.x + bounds.width / 2f;
            float knightCenterX = knightBounds.x + knightBounds.width / 2f;
            int targetDirection = knightCenterX < myCenterX ? -1 : 1;

            if (targetDirection != direction && state != State.ATTACKING) {
                state = State.TURNING;
                stateTime = 0f;
                return;
            }

            direction = targetDirection;

            if (Math.abs(knightCenterX - myCenterX) <= ATTACK_RANGE) {
                if (state != State.ATTACKING) {
                    state = State.ATTACKING;
                    stateTime = 0f;
                }
            } else if (state != State.ROLLING) {
                state = State.ROLLING;
                stateTime = 0f;
            }
        } else if (state != State.ROLLING) {
            state = State.ROLLING;
            stateTime = 0f;
        }

        if (state == State.ATTACKING) {
            velocity.x = 0f;
            applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);

            if (stateTime >= ATTACK_ANIMATION_DURATION) {
                state = State.ROLLING;
                stateTime = 0f;
            }

            return;
        }

        velocity.x = direction * ANGRY_SPEED;
        boolean hitWall = moveAxis(velocity.x * delta, 0f, platforms, polygonPlatforms);
        applyGravityAndVerticalCollision(delta, platforms, polygonPlatforms);
        boolean reachedCliff = grounded && isAtCliffEdge(platforms, polygonPlatforms);

        if (hitWall || reachedCliff) {
            state = State.TURNING;
            stateTime = 0f;
            velocity.x = 0f;
        }
    }

    public void startTalking(Rectangle knightBounds) {
        state = State.TALKING;
        stateTime = 0f;
        angryTimer = 0f;
        velocity.x = 0f;
        faceKnight(knightBounds);
    }

    public void stopTalking() {
        if (state == State.TALKING) {
            state = State.IDLE;
            stateTime = 0f;
        }
    }

    public void takeNailHit(Rectangle knightBounds, float knockbackX, float knockbackY) {
        firstDialogueCompleted = true;
        faceKnight(knightBounds);
        state = State.FALLING;
        stateTime = 0f;
        angryTimer = ANGRY_DURATION;
        velocity.x = knockbackX;
        velocity.y = Math.max(100f, knockbackY);
        grounded = false;
    }

    public String[] getNextDialogue() {
        if (!firstDialogueCompleted) {
            firstDialogueCompleted = true;
            return firstDialogue;
        }

        String line = precepts[preceptIndex % precepts.length];
        preceptIndex++;
        return new String[] { line };
    }

    public boolean isKnightInInteractionRange(Rectangle knightBounds) {
        return knightBounds != null && interactionBounds.overlaps(knightBounds) && state != State.FALLING && state != State.GETTING_UP;
    }

    private void faceKnightIfClose(Rectangle knightBounds) {
        if (knightBounds != null && isKnightInInteractionRange(knightBounds)) {
            faceKnight(knightBounds);
        }
    }

    private void faceKnight(Rectangle knightBounds) {
        if (knightBounds == null) {
            return;
        }

        float myCenterX = bounds.x + bounds.width / 2f;
        float knightCenterX = knightBounds.x + knightBounds.width / 2f;
        direction = knightCenterX < myCenterX ? -1 : 1;
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

    private boolean isAtCliffEdge(Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        float probeX = direction > 0 ? bounds.x + bounds.width + CLIFF_CHECK_DISTANCE_X : bounds.x - CLIFF_CHECK_DISTANCE_X;
        probeBounds.set(probeX, bounds.y - CLIFF_CHECK_DISTANCE_Y, 7f, CLIFF_CHECK_DISTANCE_Y + 5f);
        return !overlapsAny(probeBounds, platforms, polygonPlatforms);
    }

    private boolean isTouchingGround(Array<Rectangle> platforms, Array<Polygon> polygonPlatforms) {
        probeBounds.set(bounds.x + 4f, bounds.y - GROUND_CHECK_DISTANCE, Math.max(2f, bounds.width - 8f), GROUND_CHECK_DISTANCE + 1f);
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

    private void updateBounds() {
        bounds.set(position.x, position.y, bounds.width, bounds.height);
    }

    private void updateInteractionBounds() {
        interactionBounds.set(bounds.x - interactionRange, bounds.y - 18f, bounds.width + interactionRange * 2f, bounds.height + 44f);
    }

    public Rectangle getBounds() {
        updateBounds();
        return bounds;
    }

    public Rectangle getInteractionBounds() {
        updateInteractionBounds();
        return interactionBounds;
    }

    public Vector2 getPosition() {
        return position;
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

    public boolean isGrounded() {
        return grounded;
    }

    public float getDrawScale() {
        return drawScale;
    }

    public void reset() {
        position.set(spawnPosition);
        velocity.set(0f, 0f);
        direction = spawnDirection;
        state = State.IDLE;
        stateTime = 0f;
        angryTimer = 0f;
        grounded = false;
        updateBounds();
        updateInteractionBounds();
    }
}
