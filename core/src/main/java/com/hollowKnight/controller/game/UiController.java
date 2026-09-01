package com.hollowKnight.controller.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hollowKnight.Main;
import com.hollowKnight.config.ControlBindings;
import com.hollowKnight.controller.KnightController;
import com.hollowKnight.model.Damageable;
import com.hollowKnight.model.Knight;
import com.hollowKnight.model.achievement.AchievementManager;
import com.hollowKnight.model.charm.CharmId;
import com.hollowKnight.model.charm.CharmInventory;
import com.hollowKnight.model.charm.CharmPickup;
import com.hollowKnight.model.enemy.CrystalCrawler;
import com.hollowKnight.model.enemy.Crystallized;
import com.hollowKnight.model.enemy.FalseKnight;
import com.hollowKnight.model.enemy.Crawlid;
import com.hollowKnight.model.enemy.HuskHornhead;
import com.hollowKnight.model.enemy.Mosquito;
import com.hollowKnight.model.npc.Zote;
import com.hollowKnight.model.spell.HowlingWraithsCast;
import com.hollowKnight.model.spell.VengefulSpiritProjectile;
import com.hollowKnight.save.SaveManager;
import com.hollowKnight.view.hud.GameHud;
import com.hollowKnight.view.hud.AchievementNotification;
import com.hollowKnight.view.hud.InventoryOverlay;
import com.hollowKnight.view.hud.ZoteDialogueBox;
import com.hollowKnight.view.enemies.CrystalCrawlerRenderer;
import com.hollowKnight.view.enemies.CrystallizedRenderer;
import com.hollowKnight.view.enemies.FalseKnightRenderer;
import com.hollowKnight.view.enemies.CrawlidRenderer;
import com.hollowKnight.view.enemies.HuskHornheadRenderer;
import com.hollowKnight.view.enemies.MosquitoRenderer;
import com.hollowKnight.view.npc.ZoteRenderer;
import com.hollowKnight.view.effects.TiledParticleManager;
import com.hollowKnight.view.effects.CombatEffectManager;
import com.hollowKnight.view.charm.CharmIconLibrary;

import com.hollowKnight.view.screens.EndGameScreen;
import com.hollowKnight.view.screens.GameSettings;
import com.hollowKnight.view.screens.MainMenuScreen;

import java.util.HashMap;
import java.util.Map;

abstract class UiController extends GameControllerState {
    protected UiController(Game game, int saveSlot, boolean loadSavedGame) {
        super(game, saveSlot, loadSavedGame);
    }

    protected Texture createSolidTexture(Color color) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(color);
            pixmap.fill();
            Texture texture = new Texture(pixmap);
            pixmap.dispose();
            return texture;
        }

    protected Label makeLabel(String text) {
            return new Label(text, getSafeLabelStyle());
        }

    protected TextButton makeTextButton(String text) {
            return new TextButton(text, getSafeTextButtonStyle());
        }

    protected LabelStyle getSafeLabelStyle() {
            String[] styleNames = {"default", "hollow_style", "title", "subtitle", "label", "white", "small", "big"};

            for (String styleName : styleNames) {
                if (uiSkin.has(styleName, LabelStyle.class)) {
                    return uiSkin.get(styleName, LabelStyle.class);
                }
            }

            ObjectMap<String, LabelStyle> styles = uiSkin.getAll(LabelStyle.class);

            if (styles != null && styles.size > 0) {
                return styles.entries().next().value;
            }

            if (fallbackLabelStyle == null) {
                fallbackLabelStyle = new LabelStyle(getFallbackFont(), Color.WHITE);
            }

            return fallbackLabelStyle;
        }

    protected TextButtonStyle getSafeTextButtonStyle() {
            String[] styleNames = {"hollow_style", "default", "button", "textbutton", "white", "small", "big"};

            for (String styleName : styleNames) {
                if (uiSkin.has(styleName, TextButtonStyle.class)) {
                    return uiSkin.get(styleName, TextButtonStyle.class);
                }
            }

            ObjectMap<String, TextButtonStyle> styles = uiSkin.getAll(TextButtonStyle.class);

            if (styles != null && styles.size > 0) {
                return styles.entries().next().value;
            }

            if (fallbackTextButtonStyle == null) {
                fallbackTextButtonStyle = new TextButtonStyle();
                fallbackTextButtonStyle.font = getFallbackFont();
                fallbackTextButtonStyle.fontColor = Color.WHITE;
                fallbackTextButtonStyle.overFontColor = Color.LIGHT_GRAY;
                fallbackTextButtonStyle.downFontColor = Color.GRAY;
                fallbackTextButtonStyle.checkedFontColor = Color.WHITE;
            }

            return fallbackTextButtonStyle;
        }

    protected BitmapFont getFallbackFont() {
            if (fallbackFont == null) {
                String[] buttonStyleNames = {"hollow_style", "default", "button", "textbutton", "white", "small", "big"};

                if (uiSkin != null) {
                    for (String styleName : buttonStyleNames) {
                        if (uiSkin.has(styleName, TextButtonStyle.class)) {
                            TextButtonStyle style = uiSkin.get(styleName, TextButtonStyle.class);

                            if (style != null && style.font != null) {
                                fallbackFont = style.font;
                                return fallbackFont;
                            }
                        }
                    }

                    ObjectMap<String, TextButtonStyle> buttonStyles = uiSkin.getAll(TextButtonStyle.class);

                    if (buttonStyles != null && buttonStyles.size > 0) {
                        for (ObjectMap.Entry<String, TextButtonStyle> entry : buttonStyles.entries()) {
                            if (entry.value != null && entry.value.font != null) {
                                fallbackFont = entry.value.font;
                                return fallbackFont;
                            }
                        }
                    }
                }

                fallbackFont = new BitmapFont();
                fallbackFont.getData().setScale(1.15f);
            }

            return fallbackFont;
        }

    protected void createUiActors() {
            hazardLabel = makeLabel("be carefull its dangerous");
            hazardLabel.setAlignment(Align.center);
            hazardLabel.setFontScale(1.35f);
            hazardLabel.setVisible(false);

            Table hazardTable = new Table();
            hazardTable.setFillParent(true);
            hazardTable.add(hazardLabel).center();
            hudStage.addActor(hazardTable);

            deathLabel = makeLabel("You DIED");
            deathLabel.setAlignment(Align.center);
            deathLabel.setFontScale(2.3f);
            deathLabel.setVisible(false);

            Table deathTable = new Table();
            deathTable.setFillParent(true);
            deathTable.add(deathLabel).center();
            hudStage.addActor(deathTable);

            cheatStatusLabel = makeLabel("");
            cheatStatusLabel.setAlignment(Align.center);
            cheatStatusLabel.setFontScale(1.08f);
            cheatStatusLabel.setColor(Color.SKY);
            cheatStatusLabel.setVisible(false);

            Table cheatStatusTable = new Table();
            cheatStatusTable.setFillParent(true);
            cheatStatusTable.top().padTop(32f);
            cheatStatusTable.add(cheatStatusLabel).width(760f).center();
            hudStage.addActor(cheatStatusTable);

            pauseMenu = new Table();
            pauseMenu.setFillParent(true);
            pauseMenu.center();
            pauseMenu.setVisible(false);

            pauseMainPanel = new Table();
            pauseMainPanel.center();

            TextButton continueButton = makeTextButton("Continue");
            TextButton cheatsButton = makeTextButton("Cheat Codes");
            TextButton settingsButton = makeTextButton("Settings");
            TextButton saveButton = makeTextButton("Save");
            TextButton saveExitButton = makeTextButton("Save And Exit");
            TextButton mainMenuButton = makeTextButton("Main Menu");

            pauseStatusLabel = makeLabel("");
            pauseStatusLabel.setAlignment(Align.center);

            cheatPanel = new Table();
            cheatPanel.setVisible(false);
            Label cheatLabel = makeLabel("Cheat Codes\n" + GameSettings.getCheatGuideText());
            cheatLabel.setAlignment(Align.center);
            cheatPanel.add(cheatLabel).width(720f).pad(12f);

            settingsPanel = createSettingsPanel();
            settingsPanel.setFillParent(true);
            settingsPanel.center();
            settingsPanel.setVisible(false);
            refreshSettingsPanelLabels();

            continueButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    setPaused(false);
                }
            });

            cheatsButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    cheatPanel.setVisible(!cheatPanel.isVisible());
                    if (settingsPanel != null) {
                        settingsPanel.setVisible(false);
                    }
                }
            });

            settingsButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    showPauseSettings();
                }
            });

            saveButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    saveCurrentGame();
                    pauseStatusLabel.setText("Game saved in slot " + saveSlot);
                }
            });

            saveExitButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    saveCurrentGame();
                    game.setScreen(new MainMenuScreen(game));
                }
            });

            mainMenuButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.setScreen(new MainMenuScreen(game));
                }
            });

            pauseMainPanel.add(continueButton).width(360f).height(52f).pad(7f).row();
            pauseMainPanel.add(cheatsButton).width(360f).height(52f).pad(7f).row();
            pauseMainPanel.add(settingsButton).width(360f).height(52f).pad(7f).row();
            pauseMainPanel.add(saveButton).width(360f).height(52f).pad(7f).row();
            pauseMainPanel.add(saveExitButton).width(360f).height(52f).pad(7f).row();
            pauseMainPanel.add(mainMenuButton).width(360f).height(52f).pad(7f).row();
            pauseMainPanel.add(cheatPanel).padTop(10f).row();
            pauseMainPanel.add(pauseStatusLabel).width(560f).padTop(8f).row();

            pauseMenu.add(pauseMainPanel).center();

            zoteDialogueBox = new ZoteDialogueBox(getFallbackFont());
            hudStage.addActor(zoteDialogueBox);
            hudStage.addActor(pauseMenu);
            hudStage.addActor(settingsPanel);

            inventoryOverlay = new InventoryOverlay(
                charmInventory,
                charmIconLibrary,
                getSafeLabelStyle(),
                getSafeTextButtonStyle(),
                new InventoryOverlay.Listener() {
                    @Override
                    public void onCloseRequested() {
                        closeInventory();
                    }

                    @Override
                    public void onLoadoutChanged() {
                        applyCharmLoadout();
                        saveCurrentGame();
                    }
                }
            );
            hudStage.addActor(inventoryOverlay);
        }

    protected Table createSettingsPanel() {
            Table panel = new Table();

            Label title = makeLabel("SETTINGS");
            title.setAlignment(Align.center);
            title.setFontScale(1.15f);

            musicSettingLabel = makeLabel("");
            sfxSettingLabel = makeLabel("");
            brightnessSettingLabel = makeLabel("");

            panel.add(title).width(640f).padBottom(10f).row();
            panel.add(createPauseMusicRow()).width(640f).height(46f).pad(3f).row();
            panel.add(createPauseSfxRow()).width(640f).height(46f).pad(3f).row();
            panel.add(createPauseBrightnessRow()).width(640f).height(46f).pad(3f).row();

            TextButton resetButton = makeTextButton("Reset Settings");
            resetButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    GameSettings.reset();
                    refreshSettingsPanelLabels();
                    refreshCurrentMusicVolume();
                }
            });

            TextButton backButton = makeTextButton("Back");
            backButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    showPauseMain();
                }
            });

            panel.add(resetButton).width(330f).height(44f).padTop(8f).row();
            panel.add(backButton).width(330f).height(44f).padTop(6f).row();
            return panel;
        }

    protected Table createPauseMusicRow() {
            Table row = new Table();
            Label name = makeLabel("music");
            TextButton minus = makeTextButton("-");
            TextButton plus = makeTextButton("+");
            musicMuteSettingButton = makeTextButton("mute");

            minus.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    GameSettings.addMusicStep(-1);
                    refreshSettingsPanelLabels();
                    refreshCurrentMusicVolume();
                }
            });

            plus.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    GameSettings.addMusicStep(1);
                    refreshSettingsPanelLabels();
                    refreshCurrentMusicVolume();
                }
            });

            musicMuteSettingButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    GameSettings.toggleMusic();
                    refreshSettingsPanelLabels();
                    refreshCurrentMusicVolume();
                }
            });

            row.add(name).width(180f).left();
            row.add(minus).width(54f).height(40f).padLeft(6f);
            row.add(musicSettingLabel).width(92f).center().padLeft(8f);
            row.add(plus).width(54f).height(40f).padLeft(8f);
            row.add(musicMuteSettingButton).width(130f).height(40f).padLeft(24f);
            return row;
        }

    protected Table createPauseSfxRow() {
            Table row = new Table();
            Label name = makeLabel("sfx");
            TextButton minus = makeTextButton("-");
            TextButton plus = makeTextButton("+");
            sfxMuteSettingButton = makeTextButton("mute");

            minus.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    GameSettings.addSfxStep(-1);
                    refreshSettingsPanelLabels();
                }
            });

            plus.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    GameSettings.addSfxStep(1);
                    refreshSettingsPanelLabels();
                }
            });

            sfxMuteSettingButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    GameSettings.toggleSfx();
                    refreshSettingsPanelLabels();
                }
            });

            row.add(name).width(180f).left();
            row.add(minus).width(54f).height(40f).padLeft(6f);
            row.add(sfxSettingLabel).width(92f).center().padLeft(8f);
            row.add(plus).width(54f).height(40f).padLeft(8f);
            row.add(sfxMuteSettingButton).width(130f).height(40f).padLeft(24f);
            return row;
        }

    protected Table createPauseBrightnessRow() {
            Table row = new Table();
            Label name = makeLabel("brightness");
            TextButton minus = makeTextButton("-");
            TextButton plus = makeTextButton("+");

            minus.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    GameSettings.addBrightnessStep(-1);
                    refreshSettingsPanelLabels();
                }
            });

            plus.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    GameSettings.addBrightnessStep(1);
                    refreshSettingsPanelLabels();
                }
            });

            row.add(name).width(180f).left();
            row.add(minus).width(54f).height(40f).padLeft(6f);
            row.add(brightnessSettingLabel).width(92f).center().padLeft(8f);
            row.add(plus).width(54f).height(40f).padLeft(8f);
            row.add().width(154f);
            return row;
        }

    protected void refreshSettingsPanelLabels() {
            if (musicSettingLabel != null) {
                musicSettingLabel.setText(GameSettings.getMusicLabel());
            }

            if (sfxSettingLabel != null) {
                sfxSettingLabel.setText(GameSettings.getSfxLabel());
            }

            if (brightnessSettingLabel != null) {
                brightnessSettingLabel.setText(GameSettings.getBrightnessLabel());
            }

            if (musicMuteSettingButton != null) {
                musicMuteSettingButton.setText(GameSettings.getMusicMuteLabel());
            }

            if (sfxMuteSettingButton != null) {
                sfxMuteSettingButton.setText(GameSettings.getSfxMuteLabel());
            }
        }

    protected void refreshCurrentMusicVolume() {
            float volume = GameSettings.getMusicOutputVolume(MUSIC_VOLUME);

            if (currentMusic != null) {
                currentMusic.setVolume(volume);
            }

            if (fadingOutMusic != null) {
                fadingOutMusic.setVolume(volume);
            }
        }

    protected void showPauseMain() {
            if (pauseMenu != null) {
                pauseMenu.setVisible(true);
            }

            if (pauseMainPanel != null) {
                pauseMainPanel.setVisible(true);
            }

            if (settingsPanel != null) {
                settingsPanel.setVisible(false);
            }

            if (pauseStatusLabel != null) {
                pauseStatusLabel.setText("");
            }
        }

    protected void showPauseSettings() {
            if (pauseMenu != null) {
                pauseMenu.setVisible(false);
            }

            if (pauseMainPanel != null) {
                pauseMainPanel.setVisible(false);
            }

            if (cheatPanel != null) {
                cheatPanel.setVisible(false);
            }

            if (settingsPanel != null) {
                settingsPanel.setVisible(true);
                refreshSettingsPanelLabels();
            }

            if (pauseStatusLabel != null) {
                pauseStatusLabel.setText("");
            }
        }

    protected boolean canOpenInventory() {
            return !paused &&
                !deathRespawnPending &&
                !victoryPending &&
                !isZoteDialogueActive() &&
                knightModel != null &&
                knightModel.getCurrentHealth() > 0 &&
                !knightModel.isAttacking() &&
                !knightModel.isDashing() &&
                !knightModel.isHealing() &&
                !knightModel.isCasting() &&
                !knightModel.isKnockbackActive();
        }

    protected void openInventory() {
            if (!canOpenInventory() || inventoryOverlay == null) {
                return;
            }

            inventoryOpen = true;
            paused = true;
            pauseMenu.setVisible(false);
            if (settingsPanel != null) settingsPanel.setVisible(false);
            if (cheatPanel != null) cheatPanel.setVisible(false);
            inventoryOverlay.showInventory();
            setGameplayCursorVisible(true);
            refreshKnightControllerEnabled();
            stopPlayerLoopingSfx();
        }

    protected void closeInventory() {
            if (!inventoryOpen) {
                return;
            }

            inventoryOpen = false;
            paused = false;
            if (inventoryOverlay != null) {
                inventoryOverlay.hideInventory();
            }
            setGameplayCursorVisible(false);
            refreshKnightControllerEnabled();
        }

    protected void refreshKnightControllerEnabled() {
            if (knightController == null) {
                return;
            }

            boolean enabled = !paused &&
                !deathRespawnPending &&
                !victoryPending &&
                !isZoteDialogueActive() &&
                !noclipMode;
            knightController.setEnabled(enabled);
        }

    protected void setPaused(boolean paused) {
            if (inventoryOpen) {
                inventoryOpen = false;
                if (inventoryOverlay != null) {
                    inventoryOverlay.hideInventory();
                }
            }

            this.paused = paused;
            setGameplayCursorVisible(paused);

            if (paused) {
                stopPlayerLoopingSfx();
            }

            if (pauseMenu != null) {
                pauseMenu.setVisible(paused);
            }

            if (cheatPanel != null && !paused) {
                cheatPanel.setVisible(false);
            }

            if (settingsPanel != null && !paused) {
                settingsPanel.setVisible(false);
            }

            if (paused) {
                showPauseMain();
                refreshSettingsPanelLabels();
            }

            if (pauseStatusLabel != null && !paused) {
                pauseStatusLabel.setText("");
            }

            refreshKnightControllerEnabled();

            if (paused) {
                stopFootsteps();
                stopFallingSfx();
            }
        }

    protected void saveCurrentGame() {
            if (saveManager == null || knightModel == null || lastSafeRespawnPosition == null) {
                return;
            }

            SaveManager.SaveData data = new SaveManager.SaveData(
                saveSlot,
                activeMapPath == null ? DEFAULT_MAP_PATH : activeMapPath,
                knightModel.getPosition().x,
                knightModel.getPosition().y,
                lastSafeRespawnPosition.x,
                lastSafeRespawnPosition.y,
                knightModel.getCurrentHealth(),
                knightModel.getCurrentSoul(),
                charmInventory == null ? "" : charmInventory.serializeCollected(),
                charmInventory == null ? "" : charmInventory.serializeEquipped()
            );

            saveManager.saveGame(data);
        }
}
