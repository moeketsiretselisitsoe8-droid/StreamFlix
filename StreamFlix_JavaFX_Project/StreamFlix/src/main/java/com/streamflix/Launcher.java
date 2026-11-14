package com.streamflix;

/**
 * Launcher - a plain class with main(), used by IDEs (IntelliJ / NetBeans / Eclipse) and by the
 * packaged jar. It avoids the "JavaFX runtime components are missing" error that appears when the
 * class passed to the JVM itself extends Application.
 *
 * PORTION: App shell & UI theme  |  OWNER: Retselisitsoe Moeketsi (group leader)
 */
public class Launcher {
    public static void main(String[] args) {
        StreamFlixApp.main(args);
    }
}
