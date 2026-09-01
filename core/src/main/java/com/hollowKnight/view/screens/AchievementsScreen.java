package com.hollowKnight.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hollowKnight.model.achievement.AchievementId;
import com.hollowKnight.model.achievement.AchievementManager;

public class AchievementsScreen extends ScreenAdapter {
    private final Game game;
    private Stage stage;
    private Skin skin;
    private Array<Texture> ownedTextures;
    private BitmapFont fallbackFont;
    private Label.LabelStyle labelStyle;
    private TextButton.TextButtonStyle buttonStyle;

    public AchievementsScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("ui/alternative/main.json"));
        ownedTextures = new Array<>();
        labelStyle = findLabelStyle();
        buttonStyle = findButtonStyle();
        buildUi();
    }

    private void buildUi() {
        AchievementManager manager = AchievementManager.getInstance();

        Table root = new Table();
        root.setFillParent(true);
        root.top().padTop(30f);

        Label title = new Label("ACHIEVEMENTS", labelStyle);
        title.setAlignment(Align.center);
        title.setFontScale(1.75f);

        Label progress = new Label(
            manager.getUnlockedCount() + " / " + AchievementId.values().length + " UNLOCKED",
            labelStyle
        );
        progress.setAlignment(Align.center);
        progress.setColor(Color.SKY);

        root.add(title).width(900f).height(52f).row();
        root.add(progress).width(900f).height(34f).padBottom(14f).row();

        Table grid = new Table();
        int column = 0;
        for (AchievementId achievement : AchievementId.values()) {
            grid.add(createAchievementCard(achievement, manager.isUnlocked(achievement)))
                .width(310f)
                .height(340f)
                .pad(10f);
            column++;
            if (column == 3) {
                grid.row();
                column = 0;
            }
        }

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        ScrollPane scrollPane = new ScrollPane(grid, scrollStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        root.add(scrollPane).expand().fill().padLeft(35f).padRight(35f).row();

        TextButton back = new TextButton("BACK", buttonStyle);
        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });
        root.add(back).width(240f).height(52f).padTop(12f).padBottom(24f);

        stage.addActor(root);
    }

    private Table createAchievementCard(AchievementId achievement, boolean unlocked) {

        Table card = new Table();
        card.pad(14f);

        Texture iconTexture = loadIcon(achievement);
        Image icon = new Image(iconTexture);
        icon.setScaling(Scaling.fit);
        if (!unlocked) {
            icon.setColor(0.23f, 0.23f, 0.25f, 0.32f);
        }

        Stack iconStack = new Stack();
        iconStack.add(icon);

        if (!unlocked) {
            Table lockOverlay = new Table();
            Label lock = new Label("LOCKED", labelStyle);
            lock.setAlignment(Align.center);
            lock.setColor(Color.LIGHT_GRAY);
            lock.setFontScale(1.25f);
            lockOverlay.add(lock).expand().center();
            iconStack.add(lockOverlay);
        }

        Label name = new Label(achievement.getTitle(), labelStyle);
        name.setAlignment(Align.center);
        name.setWrap(true);
        name.setFontScale(1.12f);
        name.setColor(unlocked ? Color.WHITE : Color.GRAY);

        Label description = new Label(achievement.getDescription(), labelStyle);
        description.setAlignment(Align.center);
        description.setWrap(true);
        description.setColor(unlocked ? Color.LIGHT_GRAY : new Color(0.45f, 0.45f, 0.48f, 1f));

        Label state = new Label(unlocked ? "UNLOCKED" : "LOCKED", labelStyle);
        state.setAlignment(Align.center);
        state.setColor(unlocked ? Color.GOLD : Color.GRAY);

        card.add(iconStack).width(190f).height(190f).padBottom(10f).row();
        card.add(name).width(270f).height(46f).row();
        card.add(description).width(270f).height(60f).padTop(5f).row();
        card.add(state).width(270f).height(30f).padTop(4f);
        return card;
    }

    private Texture loadIcon(AchievementId achievement) {
        return ownTexture(Gdx.files.internal(achievement.getIconPath()));
    }

    private Texture ownTexture(FileHandle file) {
        Texture texture = new Texture(file);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        ownedTextures.add(texture);
        return texture;
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

        ObjectMap<String, TextButton.TextButtonStyle> styles =
            skin.getAll(TextButton.TextButtonStyle.class);
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

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.008f, 0.012f, 0.028f, 1f);
        stage.act(Math.min(delta, 1f / 20f));
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
        if (ownedTextures != null) {
            for (Texture texture : ownedTextures) {
                if (texture != null) {
                    texture.dispose();
                }
            }
            ownedTextures.clear();
        }
        if (fallbackFont != null) {
            fallbackFont.dispose();
        }
        if (skin != null) {
            skin.dispose();
        }
    }
}
