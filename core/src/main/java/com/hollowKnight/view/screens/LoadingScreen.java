package com.hollowKnight.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class LoadingScreen extends ScreenAdapter {
    private Game game;
    private SpriteBatch batch;
    private Array<Texture> frameTextures;
    private Animation<TextureRegion> loadingAnimation;
    private float stateTime;
    private Skin skin;
    private BitmapFont font;
    private boolean loadStarted = false;
    private int saveSlot;
    private boolean loadSavedGame;

    public LoadingScreen(Game game) {
        this(game, 1, true);
    }

    public LoadingScreen(Game game, int saveSlot, boolean loadSavedGame) {
        this.game = game;
        this.saveSlot = saveSlot;
        this.loadSavedGame = loadSavedGame;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        skin = new Skin(Gdx.files.internal("ui/alternative/main.json"));

        font = getSafeFont();

        frameTextures = new Array<>();
        Array<TextureRegion> frames = new Array<>();

        for (int i = 0; i <= 7; i++) {
            Texture tex = new Texture(Gdx.files.internal("Hollow Knight sprites/Inventory & UI/Loading icons/loading_icon_new000" + i + ".png"));
            tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            frameTextures.add(tex);
            frames.add(new TextureRegion(tex));
        }

        loadingAnimation = new Animation<>(0.1f, frames, Animation.PlayMode.LOOP);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.05f, 0.05f, 0.08f, 1);
        stateTime += delta;

        TextureRegion currentFrame = loadingAnimation.getKeyFrame(stateTime);

        batch.begin();

        float spriteWidth = currentFrame.getRegionWidth();
        float x = Gdx.graphics.getWidth() - spriteWidth - 50f;
        float y = 40f;

        batch.draw(currentFrame, x, y);
        font.draw(batch, "Loading...", x - 180f, y + 45f);

        batch.end();

        if (stateTime > 1.5f && !loadStarted) {
            loadStarted = true;
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    game.setScreen(new GameScreen(game, saveSlot, loadSavedGame));
                }
            });
        }
    }

    private BitmapFont getSafeFont() {
        String[] styleNames = {"hollow_style", "default", "button", "textbutton", "white", "small", "big"};

        for (String styleName : styleNames) {
            if (skin.has(styleName, TextButtonStyle.class)) {
                TextButtonStyle style = skin.get(styleName, TextButtonStyle.class);

                if (style != null && style.font != null) {
                    return style.font;
                }
            }
        }

        ObjectMap<String, TextButtonStyle> styles = skin.getAll(TextButtonStyle.class);

        if (styles != null && styles.size > 0) {
            for (ObjectMap.Entry<String, TextButtonStyle> entry : styles.entries()) {
                if (entry.value != null && entry.value.font != null) {
                    return entry.value.font;
                }
            }
        }

        return new BitmapFont();
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (skin != null) skin.dispose();
        if (frameTextures != null) {
            for (Texture tex : frameTextures) {
                tex.dispose();
            }
        }
    }
}
