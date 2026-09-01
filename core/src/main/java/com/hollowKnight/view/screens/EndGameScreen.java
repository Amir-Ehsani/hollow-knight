package com.hollowKnight.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class EndGameScreen extends ScreenAdapter {
    private final Game game;
    private final int saveSlot;
    private final float elapsedSeconds;
    private final int deathCount;
    private final int enemyKillCount;

    private Stage stage;
    private Skin skin;
    private BitmapFont fallbackFont;
    private Label.LabelStyle labelStyle;
    private TextButton.TextButtonStyle buttonStyle;
    private Texture panelTexture;
    private Texture lineTexture;
    private Texture victoryImage;
    private Music victoryMusic;

    public EndGameScreen(
        Game game,
        int saveSlot,
        float elapsedSeconds,
        int deathCount,
        int enemyKillCount
    ) {
        this.game = game;
        this.saveSlot = saveSlot;
        this.elapsedSeconds = Math.max(0f, elapsedSeconds);
        this.deathCount = Math.max(0, deathCount);
        this.enemyKillCount = Math.max(0, enemyKillCount);
    }

    @Override
    public void show() {
        GameSettings.load();
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("ui/alternative/main.json"));
        labelStyle = findLabelStyle();
        buttonStyle = findButtonStyle();
        panelTexture = createSolidTexture(new Color(0.015f, 0.018f, 0.028f, 0.86f));
        lineTexture = createSolidTexture(new Color(0.75f, 0.86f, 1f, 0.72f));
        victoryImage = loadVictoryImage();
        startVictoryMusic();
        buildUi();
    }

    private void buildUi() {
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        Table panel = new Table();
        panel.setBackground(new TextureRegionDrawable(panelTexture));
        panel.pad(34f, 58f, 34f, 58f);

        Label title = makeLabel("VICTORY");
        title.setAlignment(Align.center);
        title.setFontScale(2.35f);
        title.setColor(new Color(0.88f, 0.94f, 1f, 1f));
        panel.add(title).width(700f).height(70f).row();

        Label subtitle = makeLabel("FALSE KNIGHT DEFEATED");
        subtitle.setAlignment(Align.center);
        subtitle.setFontScale(1.15f);
        subtitle.setColor(new Color(0.56f, 0.77f, 0.95f, 1f));
        panel.add(subtitle).width(700f).height(36f).padBottom(12f).row();

        if (victoryImage != null) {
            Image image = new Image(victoryImage);
            image.setScaling(Scaling.fit);
            panel.add(image).width(180f).height(180f).padBottom(12f).row();
        }

        Image line = new Image(lineTexture);
        panel.add(line).width(540f).height(2f).padBottom(22f).row();

        Table stats = new Table();
        addStat(stats, "DEATHS", Integer.toString(deathCount));
        addStat(stats, "ENEMIES DEFEATED", Integer.toString(enemyKillCount));
        addStat(stats, "TOTAL TIME", formatTime(elapsedSeconds));
        panel.add(stats).width(610f).padBottom(26f).row();

        Table buttons = new Table();
        TextButton restart = new TextButton("RESTART", buttonStyle);
        TextButton mainMenu = new TextButton("MAIN MENU", buttonStyle);

        restart.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stopVictoryMusic();
                game.setScreen(new GameScreen(game, saveSlot, false));
            }
        });

        mainMenu.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stopVictoryMusic();
                game.setScreen(new MainMenuScreen(game));
            }
        });

        buttons.add(restart).width(240f).height(56f).padRight(16f);
        buttons.add(mainMenu).width(240f).height(56f).padLeft(16f);
        panel.add(buttons).width(560f).height(62f);

        root.add(panel).width(820f);
        stage.addActor(root);
    }

    private void addStat(Table stats, String name, String value) {
        Label nameLabel = makeLabel(name);
        nameLabel.setColor(Color.LIGHT_GRAY);
        nameLabel.setFontScale(1.05f);
        nameLabel.setAlignment(Align.left);

        Label valueLabel = makeLabel(value);
        valueLabel.setColor(Color.WHITE);
        valueLabel.setFontScale(1.18f);
        valueLabel.setAlignment(Align.right);

        stats.add(nameLabel).width(350f).height(44f).left();
        stats.add(valueLabel).width(220f).height(44f).right().row();
    }

    private String formatTime(float seconds) {
        int total = Math.max(0, Math.round(seconds));
        int hours = total / 3600;
        int minutes = (total % 3600) / 60;
        int secs = total % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        }
        return String.format("%02d:%02d", minutes, secs);
    }

    private Texture loadVictoryImage() {
        Texture texture = new Texture(
            "Hollow Knight sprites/Achievements/achievement_false_knight.png"
        );
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    private void startVictoryMusic() {
        victoryMusic = Gdx.audio.newMusic(Gdx.files.internal("sfx/victory-music.wav"));
        victoryMusic.setLooping(true);
        victoryMusic.setVolume(GameSettings.getMusicOutputVolume(0.62f));
        victoryMusic.play();
    }

    private Label makeLabel(String text) {
        return new Label(text, labelStyle);
    }

    private Label.LabelStyle findLabelStyle() {
        String[] names = {"default", "hollow_style", "title", "subtitle", "label", "white"};
        for (String name : names) {
            if (skin.has(name, Label.LabelStyle.class)) {
                return skin.get(name, Label.LabelStyle.class);
            }
        }

        ObjectMap<String, Label.LabelStyle> styles = skin.getAll(Label.LabelStyle.class);
        if (styles != null && styles.size > 0) {
            return styles.entries().next().value;
        }

        fallbackFont = new BitmapFont();
        return new Label.LabelStyle(fallbackFont, Color.WHITE);
    }

    private TextButton.TextButtonStyle findButtonStyle() {
        String[] names = {"hollow_style", "default", "button", "textbutton"};
        for (String name : names) {
            if (skin.has(name, TextButton.TextButtonStyle.class)) {
                return skin.get(name, TextButton.TextButtonStyle.class);
            }
        }

        ObjectMap<String, TextButton.TextButtonStyle> styles = skin.getAll(TextButton.TextButtonStyle.class);
        if (styles != null && styles.size > 0) {
            return styles.entries().next().value;
        }

        return new TextButton.TextButtonStyle(null, null, null, labelStyle.font);
    }

    private Texture createSolidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void stopVictoryMusic() {
        if (victoryMusic != null) {
            victoryMusic.stop();
            victoryMusic.dispose();
            victoryMusic = null;
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.004f, 0.006f, 0.012f, 1f);
        if (victoryMusic != null) {
            victoryMusic.setVolume(GameSettings.getMusicOutputVolume(0.62f));
        }
        stage.act(Math.min(delta, 1f / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stopVictoryMusic();
        if (stage != null) {
            stage.dispose();
        }
        if (skin != null) {
            skin.dispose();
        }
        if (fallbackFont != null) {
            fallbackFont.dispose();
        }
        if (panelTexture != null) {
            panelTexture.dispose();
        }
        if (lineTexture != null) {
            lineTexture.dispose();
        }
        if (victoryImage != null) {
            victoryImage.dispose();
        }
    }
}
