package com.hollowKnight.config;

import com.badlogic.gdx.Input;

public final class ControlBindings {
    public enum Action {
        MOVE_LEFT,
        MOVE_RIGHT,
        LOOK_UP,
        LOOK_DOWN,
        JUMP,
        DASH,
        NAIL_ATTACK,
        HEAL,
        VENGEFUL_SPIRIT,
        HOWLING_WRAITHS,
        INTERACT,
        PAUSE
    }

    private static final int[] MOVE_LEFT_KEYS = {Input.Keys.A, Input.Keys.LEFT};
    private static final int[] MOVE_RIGHT_KEYS = {Input.Keys.D, Input.Keys.RIGHT};
    private static final int[] LOOK_UP_KEYS = {Input.Keys.W, Input.Keys.UP};
    private static final int[] LOOK_DOWN_KEYS = {Input.Keys.S, Input.Keys.DOWN};
    private static final int[] JUMP_KEYS = {Input.Keys.Z, Input.Keys.SPACE, Input.Keys.K};
    private static final int[] DASH_KEYS = {Input.Keys.C, Input.Keys.SHIFT_LEFT};
    private static final int[] NAIL_ATTACK_KEYS = {Input.Keys.X, Input.Keys.J};
    private static final int[] HEAL_KEYS = {Input.Keys.H};
    private static final int[] VENGEFUL_SPIRIT_KEYS = {Input.Keys.Q};
    private static final int[] HOWLING_WRAITHS_KEYS = {Input.Keys.R};
    private static final int[] INTERACT_KEYS = {Input.Keys.E, Input.Keys.ENTER};
    private static final int[] PAUSE_KEYS = {Input.Keys.ESCAPE};
    private static final int[] NO_KEYS = new int[0];

    private ControlBindings() {
    }

    public static boolean matches(Action action, int keycode) {
        for (int key : getKeys(action)) {
            if (key == keycode) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPressed(Action action) {
        for (int key : getKeys(action)) {
            if (com.badlogic.gdx.Gdx.input.isKeyPressed(key)) {
                return true;
            }
        }
        return false;
    }
    public static int[] getKeys(Action action) {
        switch (action) {
            case MOVE_LEFT:
                return MOVE_LEFT_KEYS;
            case MOVE_RIGHT:
                return MOVE_RIGHT_KEYS;
            case LOOK_UP:
                return LOOK_UP_KEYS;
            case LOOK_DOWN:
                return LOOK_DOWN_KEYS;
            case JUMP:
                return JUMP_KEYS;
            case DASH:
                return DASH_KEYS;
            case NAIL_ATTACK:
                return NAIL_ATTACK_KEYS;
            case HEAL:
                return HEAL_KEYS;
            case VENGEFUL_SPIRIT:
                return VENGEFUL_SPIRIT_KEYS;
            case HOWLING_WRAITHS:
                return HOWLING_WRAITHS_KEYS;
            case INTERACT:
                return INTERACT_KEYS;
            case PAUSE:
                return PAUSE_KEYS;
            default:
                return NO_KEYS;
        }
    }

    public static String getLabel(Action action) {
        int[] keys = getKeys(action);
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < keys.length; i++) {
            if (i > 0) {
                builder.append(" / ");
            }
            builder.append(formatKeyName(keys[i]));
        }
        return builder.toString();
    }

    private static String formatKeyName(int keycode) {
        String name = Input.Keys.toString(keycode);
        if (name == null || name.trim().isEmpty()) {
            return String.valueOf(keycode);
        }
        return name.replace("L-Shift", "Left Shift")
            .replace("R-Shift", "Right Shift")
            .replace("Escape", "Esc");
    }
}
