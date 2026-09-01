package com.hollowKnight.view.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hollowKnight.config.ControlBindings;

public class GuideScreen extends ScreenAdapter {
    private final Game game;
    private Stage stage;
    private Skin skin;
    private BitmapFont fallbackFont;
    private LabelStyle fallbackLabelStyle;
    private TextButtonStyle fallbackButtonStyle;

    public GuideScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        GameSettings.load();
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/alternative/main.json"));

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    goBack();
                    return true;
                }
                return false;
            }
        });
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);

        Table root = new Table();
        root.setFillParent(true);
        root.pad(26f);

        Label title = makeLabel("GUIDE");
        title.setAlignment(Align.center);
        title.setFontScale(1.6f);
        root.add(title).growX().padBottom(14f).row();

        Table content = new Table();
        content.top().left();
        content.pad(18f, 34f, 28f, 34f);
        addControlsSection(content);
        addAbilitiesSection(content);
        addKnightFeaturesSection(content);
        addCharmsSection(content);
        addCheatsSection(content);

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        ScrollPane scrollPane = new ScrollPane(content, scrollStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setOverscroll(false, true);
        root.add(scrollPane).grow().row();

        TextButton backButton = makeButton("Back");
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                goBack();
            }
        });
        root.add(backButton).width(320f).height(52f).padTop(16f);

        stage.addActor(root);
    }

    private void addControlsSection(Table content) {
        addSectionTitle(content, "CONTROLS");
        addControlRow(content, "Move left", ControlBindings.Action.MOVE_LEFT);
        addControlRow(content, "Move right", ControlBindings.Action.MOVE_RIGHT);
        addControlRow(content, "Look / aim up", ControlBindings.Action.LOOK_UP);
        addControlRow(content, "Look / aim down", ControlBindings.Action.LOOK_DOWN);
        addControlRow(content, "Jump / double jump", ControlBindings.Action.JUMP);
        addControlRow(content, "Dash", ControlBindings.Action.DASH);
        addControlRow(content, "Nail attack", ControlBindings.Action.NAIL_ATTACK);
        addControlRow(content, "Focus heal", ControlBindings.Action.HEAL);
        addControlRow(content, "Vengeful Spirit", ControlBindings.Action.VENGEFUL_SPIRIT);
        addControlRow(content, "Howling Wraiths", ControlBindings.Action.HOWLING_WRAITHS);
        addControlRow(content, "Interact / dialogue", ControlBindings.Action.INTERACT);
        addControlRow(content, "Pause", ControlBindings.Action.PAUSE);
        addStaticControlRow(content, "Inventory / Charms", "I");
        content.add().height(20f).row();
    }

    private void addAbilitiesSection(Table content) {
        addSectionTitle(content, "ABILITIES");
        addParagraph(content,
            "Nail Attack - A close-range strike. Successful Nail hits fill Soul. " +
                "Hold up while attacking for an upward slash, or hold down in the air for a pogo strike.");
        addParagraph(content,
            "Dash - A fast horizontal burst used for movement and dodging. It has a short cooldown.");
        addParagraph(content,
            "Jump / Double Jump - Jump once from the ground and once again while airborne. " +
                "Releasing the jump key early produces a shorter jump.");
        addParagraph(content,
            "Focus Heal - Hold the heal key while standing still on the ground. After 1.5 seconds, " +
                "33 Soul is consumed and one health mask is restored.");
        addParagraph(content,
            "Vengeful Spirit - Costs 33 Soul. Fires a gravity-free projectile in the facing direction. " +
                "It disappears on terrain, passes through enemies, and damages every enemy it crosses once.");
        addParagraph(content,
            "Howling Wraiths - Costs 33 Soul. Creates a stationary magical blast above the Knight. " +
                "It remains briefly and deals three rapid damage ticks to enemies inside its hitbox.");
        content.add().height(20f).row();
    }

    private void addKnightFeaturesSection(Table content) {
        addSectionTitle(content, "KNIGHT, HEALTH AND SOUL");
        addParagraph(content,
            "The Knight starts with 5 health masks. Taking damage removes a mask and grants a short " +
                "damage-invulnerability window so repeated collisions do not remove all masks instantly.");
        addParagraph(content,
            "The Soul vessel holds 99 Soul. A successful normal Nail hit restores 11 Soul. " +
                "Healing and each spell cost 33 Soul, equal to one third of the vessel.");
        addParagraph(content,
            "Environmental hazards deal damage and return the Knight to the last safe grounded position. " +
                "Entering the False Knight arena seals the fight until the boss dies or the player dies.");
        content.add().height(20f).row();
    }

    private void addCharmsSection(Table content) {
        addSectionTitle(content, "CHARMS & INVENTORY");
        addParagraph(content,
            "Press I outside locked action animations to pause the game and open the Inventory. " +
                "Collected charms can be equipped by clicking them. You have 3 notches and every charm costs 1 notch.");
        addParagraph(content,
            "Soul Catcher: more Soul per Nail hit.  Dashmaster: shorter Dash cooldown.  " +
                "Unbreakable Strength: stronger Nail damage.  Quick Slash: faster Nail attacks.");
        addParagraph(content,
            "Quick Focus: faster healing.  Heavy Blow: stronger enemy knockback.  " +
                "Sharp Shadow: a longer damaging Dash through enemies.  Void Heart: 50% stronger spells with dark visuals.");
        content.add().height(20f).row();
    }

    private void addCheatsSection(Table content) {
        addSectionTitle(content, "CHEAT CODES");
        addParagraph(content, GameSettings.getCheatGuideText());
    }

    private void addSectionTitle(Table content, String text) {
        Label label = makeLabel(text);
        label.setFontScale(1.18f);
        label.setColor(Color.SKY);
        content.add(label).colspan(2).left().padTop(8f).padBottom(8f).row();
    }

    private void addStaticControlRow(Table content, String name, String key) {
        Label nameLabel = makeLabel(name);
        Label keyLabel = makeLabel(key);
        keyLabel.setColor(Color.LIGHT_GRAY);
        content.add(nameLabel).width(370f).left().pad(3f, 0f, 3f, 8f);
        content.add(keyLabel).width(360f).left().pad(3f, 18f, 3f, 0f).row();
    }

    private void addControlRow(Table content, String name, ControlBindings.Action action) {
        Label nameLabel = makeLabel(name);
        Label keyLabel = makeLabel(ControlBindings.getLabel(action));
        keyLabel.setColor(Color.LIGHT_GRAY);
        content.add(nameLabel).width(370f).left().pad(3f, 0f, 3f, 8f);
        content.add(keyLabel).width(360f).left().pad(3f, 18f, 3f, 0f).row();
    }

    private void addParagraph(Table content, String text) {
        Label label = makeLabel(text);
        label.setWrap(true);
        label.setAlignment(Align.left);
        content.add(label).colspan(2).width(820f).left().pad(5f, 8f, 7f, 8f).row();
    }

    private Label makeLabel(String text) {
        return new Label(text, getSafeLabelStyle());
    }

    private TextButton makeButton(String text) {
        return new TextButton(text, getSafeButtonStyle());
    }

    private LabelStyle getSafeLabelStyle() {
        String[] names = {"default", "hollow_style", "title", "subtitle", "label", "white", "small", "big"};
        for (String name : names) {
            if (skin.has(name, LabelStyle.class)) {
                return skin.get(name, LabelStyle.class);
            }
        }
        ObjectMap<String, LabelStyle> styles = skin.getAll(LabelStyle.class);
        if (styles != null && styles.size > 0) {
            return styles.entries().next().value;
        }
        if (fallbackLabelStyle == null) {
            fallbackLabelStyle = new LabelStyle(getFallbackFont(), Color.WHITE);
        }
        return fallbackLabelStyle;
    }

    private TextButtonStyle getSafeButtonStyle() {
        String[] names = {"hollow_style", "default", "button", "textbutton", "white", "small", "big"};
        for (String name : names) {
            if (skin.has(name, TextButtonStyle.class)) {
                return skin.get(name, TextButtonStyle.class);
            }
        }
        ObjectMap<String, TextButtonStyle> styles = skin.getAll(TextButtonStyle.class);
        if (styles != null && styles.size > 0) {
            return styles.entries().next().value;
        }
        if (fallbackButtonStyle == null) {
            fallbackButtonStyle = new TextButtonStyle();
            fallbackButtonStyle.font = getFallbackFont();
            fallbackButtonStyle.fontColor = Color.WHITE;
            fallbackButtonStyle.overFontColor = Color.LIGHT_GRAY;
        }
        return fallbackButtonStyle;
    }

    private BitmapFont getFallbackFont() {
        if (fallbackFont == null) {
            fallbackFont = new BitmapFont();
            fallbackFont.getData().setScale(1.05f);
        }
        return fallbackFont;
    }

    private void goBack() {
        Gdx.input.setInputProcessor(null);
        game.setScreen(new MainMenuScreen(game));
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.025f, 0.035f, 0.065f, 1f);
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
        if (stage != null) {
            stage.dispose();
        }
        if (skin != null) {
            skin.dispose();
        }
        if (fallbackFont != null && (fallbackLabelStyle == null || fallbackLabelStyle.font == fallbackFont)) {
            fallbackFont.dispose();
        }
    }
}
