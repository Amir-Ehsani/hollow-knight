package com.hollowKnight.view.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

public class MainMenuScreen extends ScreenAdapter {
    private Stage stage;
    private Skin skin;
    private Game game;

    private Texture logo;
    private ParticleEffect particleEffect;

    private Array<Texture> pointerTextures;
    private AnimatedImage leftArrow;
    private AnimatedImage rightArrow;

    private VideoPlayer videoPlayer;
    private Music menuMusic;

    private class AnimatedImage extends Image {
        private Animation<TextureRegion> animation;
        private float stateTime = 0;
        private boolean reversing = false;

        public AnimatedImage(Animation<TextureRegion> animation) {
            super(new TextureRegionDrawable(animation.getKeyFrame(0)));
            this.animation = animation;
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            if (isVisible()) {
                if (reversing) {
                    stateTime -= delta;
                    if (stateTime <= 0) {
                        stateTime = 0;
                        setVisible(false);
                        reversing = false;
                    }
                } else {
                    stateTime += delta;
                    if (stateTime > animation.getAnimationDuration()) {
                        stateTime = animation.getAnimationDuration();
                    }
                }
                ((TextureRegionDrawable) getDrawable()).setRegion(animation.getKeyFrame(stateTime, false));
            }
        }

        public void playForward() {
            reversing = false;
            stateTime = 0;
            setVisible(true);
        }

        public void playReverse() {
            reversing = true;
        }
    }

    public MainMenuScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);

        skin = new Skin(Gdx.files.internal("ui/alternative/main.json"));
        GameSettings.load();
        startMenuMusic();

        Table rootTable = new Table();
        rootTable.setFillParent(true);

        logo = new Texture(Gdx.files.internal("Hollow Knight sprites/Menu/vheart_title.png"));

        videoPlayer = VideoPlayerCreator.createVideoPlayer();
        try {

            videoPlayer.load(Gdx.files.internal("bg/Muted_Voidheart.webm"));
            videoPlayer.play();

            videoPlayer.setOnCompletionListener(file -> videoPlayer.play());
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextButton startBtn = new TextButton("Start Game", skin, "hollow_style");
        TextButton settingsBtn = new TextButton("Settings", skin, "hollow_style");
        TextButton guideBtn = new TextButton("Guide", skin, "hollow_style");
        TextButton achievementsBtn = new TextButton("Achievements", skin, "hollow_style");
        TextButton quitBtn = new TextButton("Quit", skin, "hollow_style");

        Image logoImage = new Image(logo);
        logoImage.setScaling(Scaling.fit);

        startBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.input.setInputProcessor(null);
                stage.addAction(sequence(
                    fadeOut(0.6f),
                    run(new Runnable() {
                        @Override
                        public void run() {
                            game.setScreen(new SaveSelectScreen(game));
                        }
                    })
                ));
            }
        });

        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.input.setInputProcessor(null);
                stage.addAction(sequence(
                    fadeOut(0.4f),
                    run(new Runnable() {
                        @Override
                        public void run() {
                            game.setScreen(new SettingsScreen(game));
                        }
                    })
                ));
            }
        });

        guideBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.input.setInputProcessor(null);
                stage.addAction(sequence(
                    fadeOut(0.4f),
                    run(new Runnable() {
                        @Override
                        public void run() {
                            game.setScreen(new GuideScreen(game));
                        }
                    })
                ));
            }
        });

        achievementsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.input.setInputProcessor(null);
                stage.addAction(sequence(
                    fadeOut(0.4f),
                    run(new Runnable() {
                        @Override
                        public void run() {
                            game.setScreen(new AchievementsScreen(game));
                        }
                    })
                ));
            }
        });

        quitBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        rootTable.add(logoImage).maxWidth(500).maxHeight(200).row();
        rootTable.add(startBtn).pad(10).fillX().row();
        rootTable.add(settingsBtn).pad(10).fillX().row();
        rootTable.add(guideBtn).pad(10).fillX().row();
        rootTable.add(achievementsBtn).pad(10).fillX().row();
        rootTable.add(quitBtn).pad(10).fillX().row();

        pointerTextures = new Array<>();
        Array<TextureRegion> leftFrames = new Array<>();
        Array<TextureRegion> rightFrames = new Array<>();

        for (int i = 0; i <= 9; i++) {
            Texture tex = new Texture(Gdx.files.internal("Hollow Knight sprites/Inventory & UI/Pointers/main_menu_pointer_anim000" + i + ".png"));
            pointerTextures.add(tex);

            TextureRegion leftReg = new TextureRegion(tex);
            leftFrames.add(leftReg);

            TextureRegion rightReg = new TextureRegion(tex);
            rightReg.flip(true, false);
            rightFrames.add(rightReg);
        }

        Animation<TextureRegion> leftAnim = new Animation<>(0.02f, leftFrames, Animation.PlayMode.NORMAL);
        Animation<TextureRegion> rightAnim = new Animation<>(0.02f, rightFrames, Animation.PlayMode.NORMAL);

        leftArrow = new AnimatedImage(leftAnim);
        rightArrow = new AnimatedImage(rightAnim);

        leftArrow.setVisible(false);
        rightArrow.setVisible(false);

        stage.addActor(rootTable);
        stage.addActor(leftArrow);
        stage.addActor(rightArrow);

        ClickListener hoverListener = new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                Actor target = event.getListenerActor();

                leftArrow.playForward();
                rightArrow.playForward();

                Vector2 pos = target.localToStageCoordinates(new Vector2(0, 0));
                float offset = -100f;

                leftArrow.setPosition(
                    pos.x - leftArrow.getWidth() - offset,
                    pos.y + (target.getHeight() - leftArrow.getHeight()) / 2f
                );

                rightArrow.setPosition(
                    pos.x + target.getWidth() + offset,
                    pos.y + (target.getHeight() - rightArrow.getHeight()) / 2f
                );
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                leftArrow.playReverse();
                rightArrow.playReverse();
            }
        };

        startBtn.addListener(hoverListener);
        settingsBtn.addListener(hoverListener);
        guideBtn.addListener(hoverListener);
        achievementsBtn.addListener(hoverListener);
        quitBtn.addListener(hoverListener);

        particleEffect = new ParticleEffect();
        particleEffect.load(Gdx.files.internal("particle/m-particle.p"), Gdx.files.internal("particle/"));
        particleEffect.setPosition(stage.getViewport().getWorldWidth() / 2f, stage.getViewport().getWorldHeight() / 2f);
        particleEffect.start();
    }

    private void startMenuMusic() {
        stopMenuMusic();
        GameSettings.load();

        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("music/Crossroads Main.wav"));

        float volume = GameSettings.getMusicOutputVolume(0.55f);
        menuMusic.setLooping(true);
        menuMusic.setVolume(volume);
        menuMusic.play();
    }

    private void stopMenuMusic() {
        if (menuMusic != null) {
            menuMusic.stop();
            menuMusic.dispose();
            menuMusic = null;
        }
    }

    private void updateMenuMusicVolume() {
        if (menuMusic != null) {
            menuMusic.setVolume(GameSettings.getMusicOutputVolume(0.55f));
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1);
        updateMenuMusicVolume();

        stage.getBatch().begin();
        if (videoPlayer != null) {
            videoPlayer.update();
            Texture frame = videoPlayer.getTexture();

            if (frame != null) {

                stage.getBatch().draw(frame, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            }
        }
        stage.getBatch().end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1/30f));
        stage.draw();

        stage.getBatch().begin();
        particleEffect.update(delta);
        particleEffect.draw(stage.getBatch());
        stage.getBatch().end();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        if (particleEffect != null) {
            particleEffect.setPosition(stage.getViewport().getWorldWidth() / 2f, stage.getViewport().getWorldHeight() / 2f);
        }
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        stopMenuMusic();
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();

        if (logo != null) logo.dispose();
        if (particleEffect != null) particleEffect.dispose();
        stopMenuMusic();

        if (videoPlayer != null) videoPlayer.dispose();

        if (pointerTextures != null) {
            for (Texture tex : pointerTextures) {
                tex.dispose();
            }
        }
    }
}
