package com.hollowKnight;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.hollowKnight.view.screens.GameScreen;
import com.hollowKnight.view.screens.MainMenuScreen;

public class Main extends Game {
    private Cursor customCursor;

    @Override
    public void create() {
        createCursors();
        setScreen(new MainMenuScreen(this));
    }

    private void createCursors() {
        Pixmap menuPixmap =
            new Pixmap(Gdx.files.internal("Hollow Knight sprites/Backend/Cursor-main.png"));
        customCursor = Gdx.graphics.newCursor(menuPixmap, 0, 0);
        menuPixmap.dispose();

    }

    @Override
    public void setScreen(Screen screen) {
        super.setScreen(screen);

        if (screen instanceof GameScreen) {
            hideGameplayCursor();
        } else {
            showMenuCursor();
        }
    }

    public void showMenuCursor() {
        Gdx.input.setCursorCatched(false);

        if (customCursor != null) {
            Gdx.graphics.setCursor(customCursor);
        }
    }

    public void hideGameplayCursor() {

        Gdx.input.setCursorCatched(true);
    }

    @Override
    public void render() {

        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();

        if (customCursor != null) {
            customCursor.dispose();
            customCursor = null;
        }
    }
}
