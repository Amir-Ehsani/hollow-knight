package com.hollowKnight.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;
import com.hollowKnight.save.SaveManager;

public class SaveSelectScreen extends ScreenAdapter {
    private Game game;
    private Stage stage;
    private Skin skin;
    private SaveManager saveManager;
    private BitmapFont font;
    private LabelStyle labelStyle;
    private TextButtonStyle baseButtonStyle;
    private Texture whitePixel;
    private Texture darkPixel;
    private Texture savedSlotTexture;
    private VideoPlayer videoPlayer;
    private Array<Texture> ownedTextures;
    private Array<HoverSlotText> hoverTexts;

    private class HoverSlotText {
        Label label;
        float baseScale;
        float currentScale;
        float currentAlpha;
        boolean hovered;

        HoverSlotText(Label label, float baseScale) {
            this.label = label;
            this.baseScale = baseScale;
            this.currentScale = baseScale;
            this.currentAlpha = 0.88f;
            this.hovered = false;
            this.label.setFontScale(baseScale);
            this.label.setColor(1f, 1f, 1f, currentAlpha);
        }

        void setHovered(boolean hovered) {
            this.hovered = hovered;
        }

        void update(float delta) {
            float targetScale = hovered ? baseScale + 0.18f : baseScale;
            float targetAlpha = hovered ? 1f : 0.88f;
            float speed = Math.min(1f, delta * 9f);
            currentScale += (targetScale - currentScale) * speed;
            currentAlpha += (targetAlpha - currentAlpha) * speed;
            label.setFontScale(currentScale);
            label.setColor(1f, 1f, 1f, currentAlpha);
        }
    }

    public SaveSelectScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        GameSettings.load();
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("ui/alternative/main.json"));
        saveManager = new SaveManager();
        ownedTextures = new Array<>();
        hoverTexts = new Array<>();
        whitePixel = createSolidTexture(Color.WHITE);
        darkPixel = createSolidTexture(new Color(0f, 0f, 0f, 0.62f));
        font = findHollowFont();
        labelStyle = new LabelStyle(font, Color.WHITE);
        baseButtonStyle = findButtonStyle();
        savedSlotTexture = new Texture("Area_Forgotten Crossroads.png");
        ownedTextures.add(savedSlotTexture);
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

        Label title = makeLabel("SELECT SAVE");
        title.setAlignment(Align.center);
        title.setFontScale(1.65f);
        root.add(title).width(680f).height(42f).padBottom(6f).row();
        root.add(makeLine(360f, 2f, 0.9f)).padBottom(20f).row();

        for (int i = 1; i <= 4; i++) {
            root.add(createSlotRow(i)).width(760f).height(88f).padTop(4f).padBottom(4f).row();
        }

        TextButton backButton = makeButton("BACK", false);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });

        root.add(backButton).width(180f).height(48f).padTop(18f).row();
        stage.addActor(root);
    }

    private Table createSlotRow(final int slot) {
        final boolean hasSave;
        boolean tempHasSave;

        try {
            tempHasSave = saveManager.hasSave(slot);
        } catch (Exception e) {
            tempHasSave = false;
        }

        hasSave = tempHasSave;

        Table row = new Table();
        row.left();

        if (hasSave && savedSlotTexture != null) {
            row.setBackground(new TextureRegionDrawable(new TextureRegion(savedSlotTexture)));
        }

        Table content = new Table();
        content.left();

        Label numberLabel = makeLabel(slot + ".");
        numberLabel.setFontScale(1.3f);
        numberLabel.setAlignment(Align.left);

        Label slotLabel = makeLabel(hasSave ? "SAVE SLOT " + slot : "NEW GAME");
        slotLabel.setFontScale(1.05f);
        slotLabel.setAlignment(Align.left);

        final HoverSlotText numberHover = new HoverSlotText(numberLabel, 1.3f);
        final HoverSlotText slotHover = new HoverSlotText(slotLabel, 1.05f);
        hoverTexts.add(numberHover);
        hoverTexts.add(slotHover);

        content.add(numberLabel).width(80f).left().padLeft(18f);
        content.add(slotLabel).width(520f).left().padLeft(10f);

        row.add(content).expand().fill().row();
        row.add(makeLine(730f, 2f, 0.82f)).padLeft(10f).padRight(10f).bottom().row();

        row.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new LoadingScreen(game, slot, hasSave));
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                numberHover.setHovered(true);
                slotHover.setHovered(true);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                numberHover.setHovered(false);
                slotHover.setHovered(false);
            }

        });

        return row;
    }

    private Label makeLabel(String text) {
        return new Label(text, labelStyle);
    }

    private TextButton makeButton(String text, boolean filled) {
        TextButtonStyle style = copyButtonStyle(baseButtonStyle);

        if (!filled) {
            style.up = null;
            style.down = null;
            style.over = null;
            style.checked = null;
        }

        return new TextButton(text, style);
    }

    private Image makeLine(float width, float height, float alpha) {
        Image image = new Image(new TextureRegionDrawable(new TextureRegion(whitePixel)));
        image.setColor(1f, 1f, 1f, alpha);
        image.setSize(width, height);
        return image;
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

    private Texture createSolidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        ownedTextures.add(texture);
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
        stage.getBatch().end();

        stage.act(delta);
        updateHoverTexts(delta);
        stage.draw();
    }

    private void updateHoverTexts(float delta) {
        if (hoverTexts == null) {
            return;
        }

        for (HoverSlotText hoverText : hoverTexts) {
            hoverText.update(delta);
        }
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

        if (videoPlayer != null) {
            videoPlayer.dispose();
        }

        if (ownedTextures != null) {
            for (Texture texture : ownedTextures) {
                if (texture != null) {
                    texture.dispose();
                }
            }
        }
    }
}
