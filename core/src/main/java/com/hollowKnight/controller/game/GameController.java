package com.hollowKnight.controller.game;

import com.badlogic.gdx.Game;

public final class GameController extends LifecycleController {
    public GameController(Game game) {
        this(game, 1, true);
    }

    public GameController(Game game, int saveSlot, boolean loadSavedGame) {
        super(game, saveSlot, loadSavedGame);
    }
}
