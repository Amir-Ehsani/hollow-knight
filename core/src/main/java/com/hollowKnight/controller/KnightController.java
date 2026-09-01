package com.hollowKnight.controller;

import com.badlogic.gdx.InputAdapter;
import com.hollowKnight.config.ControlBindings;
import com.hollowKnight.model.Knight;

public class KnightController extends InputAdapter {

    private final Knight model;

    private boolean leftPressed;
    private boolean rightPressed;
    private boolean upPressed;
    private boolean downPressed;
    private boolean enabled = true;

    public KnightController(Knight model) {
        this.model = model;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (!enabled) {
            clearMovementState();
            model.cancelHealing();
        }
    }

    public void clearMovementState() {
        leftPressed = false;
        rightPressed = false;
        upPressed = false;
        downPressed = false;
        model.getVelocity().x = 0f;
        model.setLookingUp(false);
    }

    public void refreshHeldMovement() {
        if (!enabled || model.isKnockbackActive() || model.isCasting()) {
            return;
        }

        leftPressed = ControlBindings.isPressed(ControlBindings.Action.MOVE_LEFT);
        rightPressed = ControlBindings.isPressed(ControlBindings.Action.MOVE_RIGHT);
        updateMovement();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (!enabled) {
            return true;
        }

        if (model.isKnockbackActive() || model.isCasting()) {
            return true;
        }

        if (model.isHealing() && isGameplayActionKey(keycode) &&
            !ControlBindings.matches(ControlBindings.Action.HEAL, keycode)) {
            model.cancelHealing();
            return true;
        }

        if (ControlBindings.matches(ControlBindings.Action.MOVE_LEFT, keycode)) {
            model.cancelHealing();
            leftPressed = true;
            model.setFacingLeft(true);
            model.setLookingUp(false);
            updateMovement();
            return true;
        }

        if (ControlBindings.matches(ControlBindings.Action.MOVE_RIGHT, keycode)) {
            model.cancelHealing();
            rightPressed = true;
            model.setFacingLeft(false);
            model.setLookingUp(false);
            updateMovement();
            return true;
        }

        if (ControlBindings.matches(ControlBindings.Action.LOOK_UP, keycode)) {
            if (model.isHealing()) {
                model.cancelHealing();
                return true;
            }

            upPressed = true;
            model.setLookingUp(true);
            return true;
        }

        if (ControlBindings.matches(ControlBindings.Action.LOOK_DOWN, keycode)) {
            if (model.isHealing()) {
                model.cancelHealing();
                return true;
            }

            downPressed = true;
            model.setLookingUp(false);
            return true;
        }

        if (ControlBindings.matches(ControlBindings.Action.JUMP, keycode)) {
            if (model.isHealing()) {
                model.cancelHealing();
                return true;
            }

            model.cancelHealing();
            model.setLookingUp(false);
            model.jump();
            return true;
        }

        if (ControlBindings.matches(ControlBindings.Action.DASH, keycode)) {
            if (model.isHealing()) {
                model.cancelHealing();
                return true;
            }

            model.cancelHealing();
            model.setLookingUp(false);
            model.dash();
            return true;
        }

        if (ControlBindings.matches(ControlBindings.Action.NAIL_ATTACK, keycode)) {
            if (model.isHealing()) {
                model.cancelHealing();
                return true;
            }

            model.cancelHealing();
            model.attack(upPressed, downPressed);
            return true;
        }

        if (ControlBindings.matches(ControlBindings.Action.HEAL, keycode)) {
            model.startHealing();
            return true;
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (!enabled) {
            return true;
        }

        if (ControlBindings.matches(ControlBindings.Action.MOVE_LEFT, keycode)) {
            leftPressed = false;
            if (!model.isKnockbackActive() && !model.isCasting()) {
                updateMovement();
            }
            return true;
        }

        if (ControlBindings.matches(ControlBindings.Action.MOVE_RIGHT, keycode)) {
            rightPressed = false;
            if (!model.isKnockbackActive() && !model.isCasting()) {
                updateMovement();
            }
            return true;
        }

        if (ControlBindings.matches(ControlBindings.Action.LOOK_UP, keycode)) {
            upPressed = false;
            model.setLookingUp(false);
            return true;
        }

        if (ControlBindings.matches(ControlBindings.Action.LOOK_DOWN, keycode)) {
            downPressed = false;
            return true;
        }

        if (ControlBindings.matches(ControlBindings.Action.JUMP, keycode)) {
            model.cutJump();
            return true;
        }

        if (ControlBindings.matches(ControlBindings.Action.HEAL, keycode)) {
            model.cancelHealing();
            return true;
        }

        return model.isKnockbackActive() || model.isCasting();
    }

    private boolean isGameplayActionKey(int keycode) {
        return ControlBindings.matches(ControlBindings.Action.MOVE_LEFT, keycode) ||
            ControlBindings.matches(ControlBindings.Action.MOVE_RIGHT, keycode) ||
            ControlBindings.matches(ControlBindings.Action.LOOK_UP, keycode) ||
            ControlBindings.matches(ControlBindings.Action.LOOK_DOWN, keycode) ||
            ControlBindings.matches(ControlBindings.Action.JUMP, keycode) ||
            ControlBindings.matches(ControlBindings.Action.DASH, keycode) ||
            ControlBindings.matches(ControlBindings.Action.NAIL_ATTACK, keycode) ||
            ControlBindings.matches(ControlBindings.Action.VENGEFUL_SPIRIT, keycode) ||
            ControlBindings.matches(ControlBindings.Action.HOWLING_WRAITHS, keycode);
    }

    private void updateMovement() {
        if (model.isKnockbackActive() || model.isCasting()) {
            return;
        }

        if (model.isDashing() || model.isHealing()) {
            model.getVelocity().x = 0f;
            return;
        }

        boolean grounded = model.isGrounded();

        if (leftPressed && !rightPressed) {
            model.setFacingLeft(true);
            model.getVelocity().x = -model.getMovementSpeed();

            if (grounded && model.getCurrentState() != Knight.State.RUNNING) {
                model.setCurrentState(Knight.State.RUNNING);
            }
        } else if (rightPressed && !leftPressed) {
            model.setFacingLeft(false);
            model.getVelocity().x = model.getMovementSpeed();

            if (grounded && model.getCurrentState() != Knight.State.RUNNING) {
                model.setCurrentState(Knight.State.RUNNING);
            }
        } else {
            model.getVelocity().x = 0f;

            if (grounded && !model.isAttacking() && !model.isLookingUp() &&
                !model.isHealing() && !model.isCasting()) {
                model.setCurrentState(Knight.State.IDLE);
            }
        }
    }
}
