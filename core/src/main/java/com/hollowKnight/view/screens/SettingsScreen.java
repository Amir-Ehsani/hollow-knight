package com.hollowKnight.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;

public class SettingsScreen extends ScreenAdapter {
    private Game game;
    private Stage stage;
    private Skin skin;
    private BitmapFont font;
    private LabelStyle labelStyle;
    private TextButtonStyle buttonStyle;
    private Label musicLabel;
    private Label sfxLabel;
    private Label brightnessLabel;
    private TextButton musicMuteButton;
    private TextButton sfxMuteButton;
    private Texture blackPixel;
    private VideoPlayer videoPlayer;

    public SettingsScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        GameSettings.load();
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("ui/alternative/main.json"));
        font = findHollowFont();
        labelStyle = new LabelStyle(font, Color.WHITE);
        buttonStyle = findButtonStyle();
        blackPixel = createSolidTexture(Color.BLACK);
        createBackgroundVideo();
        buildUi();
    }

    private void createBackgroundVideo() {
        videoPlayer = VideoPlayerCreator.createVideoPlayer();
        try {
            videoPlayer.load(Gdx.files.internal("bg/Muted_Voidheart.webm"));
            videoPlayer.play();
            videoPlayer.setOnCompletionListener(file -> videoPlayer.play());
        } catch (Exception e) {
            if (videoPlayer != null) {
                videoPlayer.dispose();
                videoPlayer = null;
            }
        }
    }

    private void buildUi() {
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        Label title = makeLabel("SETTINGS");
        title.setAlignment(Align.center);
        title.setFontScale(1.8f);
        root.add(title).width(720f).padBottom(32f).row();

        musicLabel = makeLabel("");
        sfxLabel = makeLabel("");
        brightnessLabel = makeLabel("");

        root.add(createMusicRow()).width(760f).height(58f).pad(7f).row();
        root.add(createSfxRow()).width(760f).height(58f).pad(7f).row();
        root.add(createBrightnessRow()).width(760f).height(58f).pad(7f).row();

        TextButton resetButton = makeButton("RESET SETTINGS");
        resetButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameSettings.reset();
                refreshLabels();
            }
        });

        TextButton backButton = makeButton("BACK");
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });

        root.add(resetButton).width(420f).height(54f).padTop(20f).row();
        root.add(backButton).width(420f).height(54f).padTop(10f).row();
        stage.addActor(root);
        refreshLabels();
    }

    private Table createMusicRow() {
        Table row = new Table();
        Label name = makeLabel("music");
        TextButton minus = makeButton("-");
        TextButton plus = makeButton("+");
        musicMuteButton = makeButton("mute");

        minus.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameSettings.addMusicStep(-1);
                refreshLabels();
            }
        });

        plus.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameSettings.addMusicStep(1);
                refreshLabels();
            }
        });

        musicMuteButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameSettings.toggleMusic();
                refreshLabels();
            }
        });

        row.add(name).width(260f).left();
        row.add(minus).width(64f).height(46f).padLeft(8f);
        row.add(musicLabel).width(120f).center().padLeft(10f);
        row.add(plus).width(64f).height(46f).padLeft(10f);
        row.add(musicMuteButton).width(150f).height(46f).padLeft(28f);
        return row;
    }

    private Table createSfxRow() {
        Table row = new Table();
        Label name = makeLabel("sfx");
        TextButton minus = makeButton("-");
        TextButton plus = makeButton("+");
        sfxMuteButton = makeButton("mute");

        minus.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameSettings.addSfxStep(-1);
                refreshLabels();
            }
        });

        plus.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameSettings.addSfxStep(1);
                refreshLabels();
            }
        });

        sfxMuteButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameSettings.toggleSfx();
                refreshLabels();
            }
        });

        row.add(name).width(260f).left();
        row.add(minus).width(64f).height(46f).padLeft(8f);
        row.add(sfxLabel).width(120f).center().padLeft(10f);
        row.add(plus).width(64f).height(46f).padLeft(10f);
        row.add(sfxMuteButton).width(150f).height(46f).padLeft(28f);
        return row;
    }

    private Table createBrightnessRow() {
        Table row = new Table();
        Label name = makeLabel("brightness");
        TextButton minus = makeButton("-");
        TextButton plus = makeButton("+");

        minus.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameSettings.addBrightnessStep(-1);
                refreshLabels();
            }
        });

        plus.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameSettings.addBrightnessStep(1);
                refreshLabels();
            }
        });

        row.add(name).width(260f).left();
        row.add(minus).width(64f).height(46f).padLeft(8f);
        row.add(brightnessLabel).width(120f).center().padLeft(10f);
        row.add(plus).width(64f).height(46f).padLeft(10f);
        row.add().width(178f);
        return row;
    }

    private void refreshLabels() {
        if (musicLabel != null) {
            musicLabel.setText(GameSettings.getMusicLabel());
        }

        if (sfxLabel != null) {
            sfxLabel.setText(GameSettings.getSfxLabel());
        }

        if (brightnessLabel != null) {
            brightnessLabel.setText(GameSettings.getBrightnessLabel());
        }

        if (musicMuteButton != null) {
            musicMuteButton.setText(GameSettings.getMusicMuteLabel());
        }

        if (sfxMuteButton != null) {
            sfxMuteButton.setText(GameSettings.getSfxMuteLabel());
        }
    }

    private Label makeLabel(String text) {
        return new Label(text, labelStyle);
    }

    private TextButton makeButton(String text) {
        return new TextButton(text, buttonStyle);
    }

    private BitmapFont findHollowFont() {
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

    private TextButtonStyle findButtonStyle() {
        String[] styleNames = {"hollow_style", "default", "button", "textbutton", "white", "small", "big"};

        for (String styleName : styleNames) {
            if (skin.has(styleName, TextButtonStyle.class)) {
                return copyButtonStyle(skin.get(styleName, TextButtonStyle.class));
            }
        }

        ObjectMap<String, TextButtonStyle> styles = skin.getAll(TextButtonStyle.class);

        if (styles != null && styles.size > 0) {
            return copyButtonStyle(styles.entries().next().value);
        }

        TextButtonStyle style = new TextButtonStyle();
        style.font = font;
        style.fontColor = Color.WHITE;
        style.overFontColor = Color.LIGHT_GRAY;
        style.downFontColor = Color.GRAY;
        return style;
    }

    private TextButtonStyle copyButtonStyle(TextButtonStyle source) {
        TextButtonStyle style = new TextButtonStyle();

        if (source != null) {
            style.up = source.up;
            style.down = source.down;
            style.over = source.over;
            style.checked = source.checked;
            style.disabled = source.disabled;
            style.font = source.font;
            style.fontColor = source.fontColor;
            style.downFontColor = source.downFontColor;
            style.overFontColor = source.overFontColor;
            style.checkedFontColor = source.checkedFontColor;
            style.disabledFontColor = source.disabledFontColor;
        }

        if (style.font == null) {
            style.font = font;
        }

        if (style.fontColor == null) {
            style.fontColor = Color.WHITE;
        }

        if (style.overFontColor == null) {
            style.overFontColor = Color.LIGHT_GRAY;
        }

        if (style.downFontColor == null) {
            style.downFontColor = Color.GRAY;
        }

        return style;
    }

    private Texture createSolidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.02f, 0.02f, 0.035f, 1f);

        stage.getBatch().begin();
        if (videoPlayer != null) {
            videoPlayer.update();
            Texture frame = videoPlayer.getTexture();

            if (frame != null) {
                stage.getBatch().draw(frame, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            }
        }

        if (blackPixel != null) {
            stage.getBatch().setColor(0f, 0f, 0f, GameSettings.getWorldDarknessAlpha());
            stage.getBatch().draw(blackPixel, 0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            stage.getBatch().setColor(1f, 1f, 1f, 1f);
        }
        stage.getBatch().end();

        stage.act(delta);
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
        if (stage != null) {
            stage.dispose();
        }

        if (skin != null) {
            skin.dispose();
        }

        if (blackPixel != null) {
            blackPixel.dispose();
        }

        if (videoPlayer != null) {
            videoPlayer.dispose();
        }
    }
}
