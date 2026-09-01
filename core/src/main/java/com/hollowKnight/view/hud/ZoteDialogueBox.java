package com.hollowKnight.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

public class ZoteDialogueBox extends Table implements Disposable {

    private static final float CHAR_INTERVAL = 0.018f;

    private final Table panel;
    private final Label nameLabel;
    private final Label textLabel;
    private final Label nextLabel;
    private final Label promptLabel;
    private final Texture panelTexture;
    private final Texture promptTexture;

    private String[] lines;
    private String currentLine;
    private int lineIndex;
    private int visibleCharacters;
    private float charTimer;
    private boolean active;

    public ZoteDialogueBox(BitmapFont font) {
        setFillParent(true);
        top();
        padTop(24f);

        panelTexture = createTexture(0f, 0f, 0f, 0.82f);
        promptTexture = createTexture(0f, 0f, 0f, 0.62f);

        Label.LabelStyle textStyle = new Label.LabelStyle(font, Color.WHITE);
        Label.LabelStyle nameStyle = new Label.LabelStyle(font, Color.valueOf("f5e6b8ff"));
        Label.LabelStyle nextStyle = new Label.LabelStyle(font, Color.valueOf("d5d5d5ff"));
        Label.LabelStyle promptStyle = new Label.LabelStyle(font, Color.valueOf("d5d5d5ff"));

        promptLabel = new Label("Press E to talk", promptStyle);
        promptLabel.setAlignment(Align.center);
        promptLabel.setFontScale(0.92f);
        promptLabel.setVisible(false);
        promptLabel.getStyle().background = new TextureRegionDrawable(new TextureRegion(promptTexture));

        panel = new Table();
        panel.setBackground(new TextureRegionDrawable(new TextureRegion(panelTexture)));
        panel.setVisible(false);

        nameLabel = new Label("Zote the Mighty", nameStyle);
        nameLabel.setAlignment(Align.left);
        nameLabel.setFontScale(1.05f);

        textLabel = new Label("", textStyle);
        textLabel.setWrap(true);
        textLabel.setAlignment(Align.topLeft);
        textLabel.setFontScale(0.96f);

        nextLabel = new Label("Enter", nextStyle);
        nextLabel.setAlignment(Align.right);
        nextLabel.setFontScale(0.82f);

        panel.pad(18f, 24f, 14f, 24f);
        panel.add(nameLabel).left().width(840f).height(30f).row();
        panel.add(textLabel).left().top().width(840f).height(82f).row();
        panel.add(nextLabel).right().width(840f).height(24f).row();

        add(panel).width(900f).height(170f).padTop(4f).row();
        add(promptLabel).width(230f).height(34f).padTop(10f).row();

        lines = new String[0];
        currentLine = "";
        active = false;
    }

    public void update(float delta) {
        if (!active || currentLine == null || visibleCharacters >= currentLine.length()) {
            return;
        }

        charTimer += delta;

        while (charTimer >= CHAR_INTERVAL && visibleCharacters < currentLine.length()) {
            charTimer -= CHAR_INTERVAL;
            visibleCharacters++;
        }

        refreshText();
    }

    public void begin(String[] dialogueLines) {
        if (dialogueLines == null || dialogueLines.length == 0) {
            return;
        }

        lines = dialogueLines;
        lineIndex = 0;
        active = true;
        panel.setVisible(true);
        promptLabel.setVisible(false);
        setLine(lines[lineIndex]);
    }

    public boolean advance() {
        if (!active) {
            return false;
        }

        if (visibleCharacters < currentLine.length()) {
            visibleCharacters = currentLine.length();
            refreshText();
            return true;
        }

        lineIndex++;

        if (lineIndex >= lines.length) {
            close();
            return false;
        }

        setLine(lines[lineIndex]);
        return true;
    }

    public void close() {
        active = false;
        panel.setVisible(false);
        textLabel.setText("");
        currentLine = "";
    }

    public void setPromptVisible(boolean visible) {
        promptLabel.setVisible(visible && !active);
    }

    public boolean isActive() {
        return active;
    }

    private void setLine(String line) {
        currentLine = line == null ? "" : line;
        visibleCharacters = 0;
        charTimer = 0f;
        refreshText();
    }

    private void refreshText() {
        int end = Math.max(0, Math.min(visibleCharacters, currentLine.length()));
        textLabel.setText(currentLine.substring(0, end));
    }

    private Texture createTexture(float r, float g, float b, float a) {
        Pixmap pixmap = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
        pixmap.setColor(r, g, b, a);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void dispose() {
        panelTexture.dispose();
        promptTexture.dispose();
    }
}
