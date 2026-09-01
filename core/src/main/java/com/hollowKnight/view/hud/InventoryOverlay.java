package com.hollowKnight.view.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Scaling;
import com.hollowKnight.model.charm.CharmId;
import com.hollowKnight.model.charm.CharmInventory;
import com.hollowKnight.view.charm.CharmIconLibrary;

public class InventoryOverlay extends Table implements Disposable {
    public interface Listener {
        void onCloseRequested();
        void onLoadoutChanged();
    }

    private static final float GRID_WIDTH = 1080f;
    private static final float CARD_WIDTH = 245f;
    private static final float CARD_HEIGHT = 190f;

    private final CharmInventory inventory;
    private final CharmIconLibrary icons;
    private final LabelStyle labelStyle;
    private final TextButtonStyle buttonStyle;
    private final Listener listener;

    private final Table cards = new Table();
    private final Label notchLabel;
    private final Label collectionLabel;
    private final Label detailTitleLabel;
    private final Label detailDescriptionLabel;
    private final Label statusLabel;

    private CharmId selectedCharm;

    private final Texture panelTexture;
    private final Texture cardTexture;
    private final Texture equippedTexture;
    private final Texture detailsTexture;

    public InventoryOverlay(
        CharmInventory inventory,
        CharmIconLibrary icons,
        LabelStyle labelStyle,
        TextButtonStyle buttonStyle,
        Listener listener
    ) {
        this.inventory = inventory;
        this.icons = icons;
        this.labelStyle = labelStyle;
        this.buttonStyle = buttonStyle;
        this.listener = listener;

        panelTexture = createTexture(0.008f, 0.012f, 0.022f, 0.97f);
        cardTexture = createTexture(0.028f, 0.035f, 0.058f, 0.90f);
        equippedTexture = createTexture(0.12f, 0.09f, 0.025f, 0.96f);
        detailsTexture = createTexture(0.022f, 0.027f, 0.044f, 0.94f);

        setFillParent(true);
        setVisible(false);
        setBackground(new TextureRegionDrawable(new TextureRegion(panelTexture)));
        pad(18f, 34f, 18f, 34f);
        top();

        Label title = makeLabel("INVENTORY & CHARMS", 1.42f, Color.WHITE);
        title.setAlignment(Align.center);
        add(title).growX().height(62f).padTop(4f).row();

        notchLabel = makeLabel("", 0.92f, Color.GOLD);
        notchLabel.setAlignment(Align.center);
        add(notchLabel).growX().height(38f).row();

        collectionLabel = makeLabel("", 0.72f, Color.SKY);
        collectionLabel.setAlignment(Align.center);
        add(collectionLabel).growX().height(34f).padBottom(8f).row();

        cards.defaults().pad(7f);
        add(cards).width(GRID_WIDTH).height(410f).top().row();

        selectedCharm = findInitialSelection();

        Table details = new Table();
        details.setBackground(new TextureRegionDrawable(new TextureRegion(detailsTexture)));
        details.pad(7f, 18f, 7f, 18f);

        detailTitleLabel = makeLabel("", 0.68f, Color.WHITE);
        detailTitleLabel.setAlignment(Align.center);
        detailTitleLabel.setWrap(false);
        details.add(detailTitleLabel).growX().height(30f).row();

        detailDescriptionLabel = makeLabel("", 0.50f, Color.LIGHT_GRAY);
        detailDescriptionLabel.setAlignment(Align.center);
        detailDescriptionLabel.setWrap(true);
        details.add(detailDescriptionLabel).width(1000f).height(52f).row();

        add(details).width(GRID_WIDTH).height(96f).padTop(5f).row();

        statusLabel = makeLabel(
            "Move the cursor over a charm to read its effect. Click a collected charm to equip or unequip it.",
            0.48f,
            Color.LIGHT_GRAY
        );
        statusLabel.setAlignment(Align.center);
        statusLabel.setWrap(false);
        add(statusLabel).width(GRID_WIDTH).height(26f).padTop(2f).row();

        TextButton close = new TextButton("CLOSE [I]", buttonStyle);
        close.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (InventoryOverlay.this.listener != null) {
                    InventoryOverlay.this.listener.onCloseRequested();
                }
            }
        });
        add(close).width(300f).height(48f).padTop(3f).padBottom(3f);

        refresh();
    }

    public void showInventory() {
        refresh();
        setVisible(true);
    }

    public void hideInventory() {
        setVisible(false);
    }

    public void refresh() {
        notchLabel.setText(buildNotchText());
        collectionLabel.setText(
            inventory.getCollectedCount() + " / " + CharmId.values().length + " CHARMS FOUND"
        );
        rebuildCards();
        updateDetails(selectedCharm);
    }

    private void rebuildCards() {
        cards.clearChildren();
        int column = 0;

        for (final CharmId id : CharmId.values()) {
            cards.add(createCard(id)).width(CARD_WIDTH).height(CARD_HEIGHT);
            column++;

            if (column == 4) {
                cards.row();
                column = 0;
            }
        }
    }

    private Table createCard(final CharmId id) {
        final boolean collected = inventory.isCollected(id);
        final boolean equipped = inventory.isEquipped(id);

        Table card = new Table();
        card.top();
        card.pad(7f);
        card.setBackground(
            new TextureRegionDrawable(new TextureRegion(equipped ? equippedTexture : cardTexture))
        );

        Image icon = new Image(new TextureRegionDrawable(icons.get(id)));
        icon.setScaling(Scaling.fit);
        if (!collected) {
            icon.setColor(0.16f, 0.18f, 0.23f, 0.30f);
        } else if (!equipped) {
            icon.setColor(0.82f, 0.84f, 0.90f, 0.96f);
        }
        card.add(icon).width(88f).height(88f).padTop(3f).padBottom(3f).row();

        Label name = makeLabel(
            id.getDisplayName(),
            0.58f,
            collected ? Color.WHITE : Color.DARK_GRAY
        );
        name.setAlignment(Align.center);
        name.setWrap(true);
        card.add(name).width(218f).height(42f).row();

        Label state = makeLabel(
            !collected ? "NOT FOUND" : (equipped ? "EQUIPPED" : "AVAILABLE"),
            0.46f,
            equipped ? Color.GOLD : (collected ? Color.SKY : Color.GRAY)
        );
        state.setAlignment(Align.center);
        state.setWrap(false);
        card.add(state).width(218f).height(24f).padTop(2f).row();

        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectedCharm = id;
                handleToggle(id);
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                updateDetails(id);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                updateDetails(selectedCharm);
            }
        });

        return card;
    }

    private CharmId findInitialSelection() {
        for (CharmId id : CharmId.values()) {
            if (inventory.isCollected(id)) {
                return id;
            }
        }
        return CharmId.values().length == 0 ? null : CharmId.values()[0];
    }

    private void updateDetails(CharmId id) {
        if (id == null) {
            detailTitleLabel.setText("SELECT A CHARM");
            detailDescriptionLabel.setText("Move the cursor over a charm to see its description.");
            return;
        }

        boolean collected = inventory.isCollected(id);
        boolean equipped = inventory.isEquipped(id);
        String state = !collected ? "NOT FOUND" : (equipped ? "EQUIPPED" : "AVAILABLE");
        String optional = id.isOptional() ? "  |  OPTIONAL" : "";

        detailTitleLabel.setText(id.getDisplayName().toUpperCase());
        detailTitleLabel.setColor(collected ? Color.WHITE : Color.GRAY);

        detailDescriptionLabel.setText(
            id.getDescription() + "   COST: 1 NOTCH   |   " + state + optional
        );
        detailDescriptionLabel.setColor(collected ? Color.LIGHT_GRAY : Color.DARK_GRAY);
    }

    private void handleToggle(CharmId id) {
        CharmInventory.ToggleResult result = inventory.toggle(id);

        switch (result) {
            case EQUIPPED:
                statusLabel.setText(id.getDisplayName() + " equipped.");
                break;
            case UNEQUIPPED:
                statusLabel.setText(id.getDisplayName() + " unequipped.");
                break;
            case NO_FREE_NOTCH:
                statusLabel.setText("All 3 notches are full. Unequip a charm first.");
                break;
            case NOT_COLLECTED:
            default:
                statusLabel.setText("Find this charm in the world before equipping it.");
                break;
        }

        if (listener != null &&
            (result == CharmInventory.ToggleResult.EQUIPPED ||
                result == CharmInventory.ToggleResult.UNEQUIPPED)) {
            listener.onLoadoutChanged();
        }

        refresh();
    }

    private String buildNotchText() {
        StringBuilder builder = new StringBuilder("NOTCHES  ");
        int used = inventory.getUsedNotches();

        for (int i = 0; i < CharmInventory.MAX_NOTCHES; i++) {
            builder.append(i < used ? "[X]" : "[ ]");
            if (i + 1 < CharmInventory.MAX_NOTCHES) {
                builder.append(' ');
            }
        }

        builder.append("   ")
            .append(used)
            .append('/')
            .append(CharmInventory.MAX_NOTCHES);

        return builder.toString();
    }

    private Label makeLabel(String text, float scale, Color color) {
        Label label = new Label(text, labelStyle);
        label.setFontScale(scale);
        label.setColor(color);
        return label;
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
        cardTexture.dispose();
        equippedTexture.dispose();
        detailsTexture.dispose();
    }
}
