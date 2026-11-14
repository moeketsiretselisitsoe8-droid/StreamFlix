package com.streamflix.ui;

import com.streamflix.audio.SoundManager;
import com.streamflix.audio.SoundManager.Sound;
import com.streamflix.util.Theme;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * CustomButton - OUR OWN button, built from a StackPane + Label.
 *
 * The assignment forbids javafx.scene.control.Button (and its subclasses), so this class extends
 * StackPane and adds the behaviour by hand: hover colour, grow-on-hover, press "squash",
 * hand cursor, click sound and an action callback.
 *
 * PORTION: Custom UI components  |  OWNER: Moeti Leleka
 */
public class CustomButton extends StackPane {

    /** Visual styles of a button. */
    public enum Variant { PRIMARY, SECONDARY, GHOST }

    private final Label label = new Label();
    private final Variant variant;
    private Runnable onAction;
    private boolean hovering = false;
    private boolean enabled = true;

    public CustomButton(String text, Variant variant) {
        this(text, variant, 15);
    }

    public CustomButton(String text, Variant variant, double fontSize) {
        this.variant = variant;

        label.setText(text);
        label.setFont(Theme.bold(fontSize));
        label.setTextFill(Color.WHITE);
        label.setGraphicTextGap(10);
        label.setContentDisplay(ContentDisplay.LEFT);
        label.setMouseTransparent(true);          // the StackPane receives the mouse events

        getChildren().add(label);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(10, 24, 10, 24));
        setCursor(Cursor.HAND);
        setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        refreshStyle();

        setOnMouseEntered(e -> {
            if (!enabled) {
                return;
            }
            hovering = true;
            refreshStyle();
            scaleTo(1.07);
            SoundManager.play(Sound.HOVER);
        });
        setOnMouseExited(e -> {
            hovering = false;
            refreshStyle();
            scaleTo(1.0);
        });
        setOnMousePressed(e -> {
            if (enabled) {
                scaleTo(0.94);
            }
        });
        setOnMouseReleased(e -> scaleTo(hovering ? 1.07 : 1.0));
        setOnMouseClicked(e -> {
            if (!enabled) {
                return;
            }
            SoundManager.play(Sound.CLICK);
            if (onAction != null) {
                onAction.run();
            }
        });
    }

    /** What happens when the button is clicked. */
    public void setOnAction(Runnable action) {
        this.onAction = action;
    }

    public void setText(String text) {
        label.setText(text);
    }

    /** Optional icon shown beside the text (see {@link Icons}). */
    public void setIcon(Node icon) {
        label.setGraphic(icon);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        setOpacity(enabled ? 1.0 : 0.4);
        setCursor(enabled ? Cursor.HAND : Cursor.DEFAULT);
    }

    // ------------------------------------------------------------------ internals

    private void scaleTo(double target) {
        ScaleTransition st = new ScaleTransition(Duration.millis(120), this);
        st.setToX(target);
        st.setToY(target);
        st.play();
    }

    private void refreshStyle() {
        String background;
        switch (variant) {
            case PRIMARY:
                background = hovering ? Theme.ACCENT_HOVER : Theme.ACCENT_HEX;
                break;
            case SECONDARY:
                background = hovering ? "rgba(255,255,255,0.35)" : "rgba(109,109,110,0.70)";
                break;
            default: // GHOST
                background = hovering ? "rgba(255,255,255,0.18)" : "rgba(0,0,0,0.35)";
                break;
        }
        String glow = hovering && variant == Variant.PRIMARY
                ? "-fx-effect: dropshadow(gaussian, rgba(229,9,20,0.75), 18, 0.3, 0, 0);"
                : "";
        setStyle("-fx-background-color: " + background + "; -fx-background-radius: 8;" + glow);
    }
}
