package com.hollowKnight.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.hollowKnight.model.achievement.AchievementId;
import com.hollowKnight.model.achievement.AchievementListener;
import com.hollowKnight.model.achievement.AchievementManager;

public class AchievementNotification extends Table implements AchievementListener, Disposable {
    private final AchievementManager manager;
    private final Array<AchievementId> pending;
    private final Label unlockedLabel;
    private final Label achievementLabel;
    private final Texture backgroundTexture;
    private BitmapFont fallbackFont;
    private boolean displaying;
    private boolean listening;

    public AchievementNotification(Skin skin) {
        manager = AchievementManager.getInstance();
        pending = new Array<>();

        setSize(510f, 104f);
        pad(14f, 20f, 14f, 20f);

        backgroundTexture = createBackgroundTexture();
        setBackground(new TextureRegionDrawable(backgroundTexture));

        Label.LabelStyle labelStyle = findLabelStyle(skin);
        unlockedLabel = new Label("ACHIEVEMENT UNLOCKED", labelStyle);
        unlockedLabel.setAlignment(Align.center);
        unlockedLabel.setColor(Color.GOLD);
        unlockedLabel.setFontScale(0.92f);

        achievementLabel = new Label("", labelStyle);
        achievementLabel.setAlignment(Align.center);
        achievementLabel.setFontScale(1.18f);

        add(unlockedLabel).expandX().fillX().row();
        add(achievementLabel).expandX().fillX().padTop(5f);

        getColor().a = 0f;
        setVisible(false);
        displaying = false;
        setListening(true);
    }

    @Override
    public void onAchievementUnlocked(AchievementId achievement) {
        if (achievement == null) {
            return;
        }

        pending.add(achievement);
        if (!displaying) {
            showNext();
        }
    }

    public void updatePosition(float viewportWidth, float viewportHeight) {
        setPosition(
            Math.max(18f, viewportWidth - getWidth() - 26f),
            Math.max(18f, viewportHeight - getHeight() - 26f)
        );
    }

    public void setListening(boolean listen) {
        if (listen == listening) {
            return;
        }

        listening = listen;
        if (listening) {
            manager.addListener(this);
        } else {
            manager.removeListener(this);
        }
    }

    private void showNext() {
        if (pending.size == 0) {
            displaying = false;
            setVisible(false);
            return;
        }

        displaying = true;
        AchievementId achievement = pending.removeIndex(0);
        achievementLabel.setText(achievement.getTitle());
        clearActions();
        setVisible(true);
        getColor().a = 0f;

        addAction(Actions.sequence(
            Actions.fadeIn(0.22f),
            Actions.delay(2.8f),
            Actions.fadeOut(0.40f),
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    setVisible(false);
                    displaying = false;
                    showNext();
                }
            })
        ));
    }

    private Label.LabelStyle findLabelStyle(Skin skin) {
        String[] candidates = {"default", "hollow_style", "title", "subtitle", "label", "white"};
        for (String name : candidates) {
            if (skin != null && skin.has(name, Label.LabelStyle.class)) {
                return skin.get(name, Label.LabelStyle.class);
            }
        }

        if (skin != null) {
            ObjectMap<String, Label.LabelStyle> styles = skin.getAll(Label.LabelStyle.class);
            if (styles != null && styles.size > 0) {
                return styles.entries().next().value;
            }
        }

        fallbackFont = new BitmapFont();
        return new Label.LabelStyle(fallbackFont, Color.WHITE);
    }

    private Texture createBackgroundTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.025f, 0.035f, 0.07f, 0.94f);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void dispose() {
        setListening(false);
        backgroundTexture.dispose();
        if (fallbackFont != null) {
            fallbackFont.dispose();
            fallbackFont = null;
        }
    }
}
