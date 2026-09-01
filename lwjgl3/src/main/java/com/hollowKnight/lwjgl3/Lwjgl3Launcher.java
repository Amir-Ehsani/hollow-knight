package com.hollowKnight.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.hollowKnight.Main;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    private static final int TARGET_FPS = 60;

    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) {
            return;
        }

        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new Main(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration =
            new Lwjgl3ApplicationConfiguration();

        configuration.setTitle("Hollow Knight");
        configuration.useVsync(true);
        configuration.setForegroundFPS(TARGET_FPS);
        configuration.setIdleFPS(30);

        configuration.setFullscreenMode(
            Lwjgl3ApplicationConfiguration.getDisplayMode()
        );
        configuration.setAutoIconify(true);
        configuration.setResizable(false);

        configuration.setWindowIcon(
            "libgdx128.png",
            "libgdx64.png",
            "libgdx32.png",
            "libgdx16.png"
        );

        return configuration;
    }
}
