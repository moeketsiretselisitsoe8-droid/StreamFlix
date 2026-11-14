package com.streamflix.ui;

import com.streamflix.audio.SoundManager;
import com.streamflix.audio.SoundManager.Sound;
import javafx.animation.ScaleTransition;
import javafx.scene.Cursor;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

/**
 * SoundToggle - small speaker icon (top-right) that mutes / un-mutes every sound effect.
 * Built from a StackPane + vector icon, so it is a custom control, not a Button.
 *
 * PORTION: Audio & Loading animation  |  OWNER: Lethako Khabele
 */
public class SoundToggle extends StackPane {

    private static final double ICON_SIZE = 22;

    public SoundToggle() {
        setPrefSize(40, 40);
        setMinSize(40, 40);
        setMaxSize(40, 40);
        setCursor(Cursor.HAND);
        Tooltip.install(this, new Tooltip("Sound effects on/off"));
        refresh(false);

        setOnMouseEntered(e -> {
            setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 20;");
            scaleTo(1.12);
        });
        setOnMouseExited(e -> {
            setStyle("-fx-background-color: transparent; -fx-background-radius: 20;");
            scaleTo(1.0);
        });
        setOnMouseClicked(e -> {
            boolean nowMuted = !SoundManager.isMuted();
            if (nowMuted) {
                SoundManager.play(Sound.TOGGLE);      // play the "off" blip BEFORE muting
            }
            SoundManager.setMuted(nowMuted);
            if (!nowMuted) {
                SoundManager.play(Sound.TOGGLE);      // play the "on" blip AFTER un-muting
            }
            refresh(true);
        });
    }

    private void refresh(boolean animate) {
        getChildren().setAll(Icons.speaker(ICON_SIZE, Color.WHITE, !SoundManager.isMuted()));
    }

    private void scaleTo(double target) {
        ScaleTransition st = new ScaleTransition(Duration.millis(120), this);
        st.setToX(target);
        st.setToY(target);
        st.play();
    }
}
