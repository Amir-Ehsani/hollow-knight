package com.hollowKnight.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.ScreenAdapter;
import com.hollowKnight.controller.game.GameController;

public class GameScreen extends ScreenAdapter {
    private final GameController controller;

    public GameScreen(Game game) {
        controller = new GameController(game);
    }

    public GameScreen(Game game, int saveSlot, boolean loadSavedGame) {
        controller = new GameController(game, saveSlot, loadSavedGame);
    }

    @Override
    public void show() {
        controller.show();
    }

    @Override
    public void render(float delta) {
        controller.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        controller.resize(width, height);
    }

    @Override
    public void pause() {
        controller.pause();
    }

    @Override
    public void resume() {
        controller.resume();
    }

    @Override
    public void hide() {
        controller.hide();
    }

    @Override
    public void dispose() {
        controller.dispose();
    }
}
