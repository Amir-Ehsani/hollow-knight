package com.hollowKnight.model.charm;

import com.badlogic.gdx.math.Rectangle;

public class CharmPickup {
    private final CharmId charmId;
    private final Rectangle bounds;
    private final float drawScale;
    private float stateTime;
    private boolean collected;

    public CharmPickup(CharmId charmId, float x, float y, float width, float height, float drawScale) {
        this.charmId = charmId;
        this.bounds = new Rectangle(x, y, Math.max(24f, width), Math.max(24f, height));
        this.drawScale = Math.max(0.25f, drawScale);
    }

    public void update(float delta) {
        if (!collected && delta > 0f) {
            stateTime += delta;
        }
    }

    public CharmId getCharmId() {
        return charmId;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public float getDrawScale() {
        return drawScale;
    }

    public float getStateTime() {
        return stateTime;
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        collected = true;
    }
}
