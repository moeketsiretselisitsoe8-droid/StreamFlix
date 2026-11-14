package com.streamflix.util;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Theme - every colour and font used by the app lives here, so the whole UI stays consistent
 * (rubric: "consistent theme"). Change a value here and the entire app updates.
 *
 * PORTION: App shell & UI theme  |  OWNER: Retselisitsoe Moeketsi (group leader)
 */
public final class Theme {

    // ---- colours (Color objects, for Java code) ----
    public static final Color BACKGROUND = Color.web("#141414");
    public static final Color SURFACE    = Color.web("#1F1F1F");
    public static final Color ACCENT     = Color.web("#E50914");
    public static final Color TEXT       = Color.WHITE;
    public static final Color TEXT_MUTED = Color.web("#B3B3B3");

    // ---- the same colours as CSS strings, for setStyle(...) ----
    public static final String BACKGROUND_HEX = "#141414";
    public static final String SURFACE_HEX    = "#1F1F1F";
    public static final String ACCENT_HEX     = "#E50914";
    public static final String ACCENT_HOVER   = "#F6121D";
    public static final String MUTED_HEX      = "#B3B3B3";

    public static final String FONT_FAMILY = "Segoe UI";

    private Theme() { }

    public static Font bold(double size) {
        return Font.font(FONT_FAMILY, FontWeight.BOLD, size);
    }

    public static Font regular(double size) {
        return Font.font(FONT_FAMILY, FontWeight.NORMAL, size);
    }
}
