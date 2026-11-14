package com.streamflix.ui;

import com.streamflix.util.Theme;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * LoadingAnimation - OUR OWN loading screen, shown while a trailer loads from YouTube.
 * (Assignment: "create your own custom animations displayed when the video is being loaded".)
 *
 * What you see: three glowing arcs spinning at different speeds/directions around a pulsing
 * red "S" logo, the movie title, and "Loading trailer..." with animated dots.
 * It is only a StackPane + shapes + JavaFX animations (RotateTransition, ScaleTransition, Timeline).
 *
 * PORTION: Audio & Loading animation  |  OWNER: Lethako Khabele
 */
public class LoadingAnimation extends StackPane {

    private final List<Animation> animations = new ArrayList<>();
    private final Label titleLabel = new Label();
    private final Label statusLabel = new Label();
    private Timeline dotsTimeline;
    private FadeTransition fade;
    private int dots = 0;

    public LoadingAnimation() {
        setStyle("-fx-background-color: rgba(10,10,10,0.97);");
        setAlignment(Pos.CENTER);

        // ---- spinner: 3 rings + logo
        StackPane spinner = new StackPane(
                ring(88, 20, 250, Color.web("#E50914"), 5, 2.4, true),
                ring(66, 100, 200, Color.web("#ff6b6b"), 4, 1.6, false),
                ring(44, 200, 150, Color.WHITE, 3, 1.1, true),
                logo());
        spinner.setPrefSize(220, 220);
        spinner.setMaxSize(220, 220);

        titleLabel.setFont(Theme.bold(26));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(640);
        titleLabel.setAlignment(Pos.CENTER);

        statusLabel.setFont(Theme.regular(16));
        statusLabel.setTextFill(Color.web(Theme.MUTED_HEX));
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(640);
        statusLabel.setAlignment(Pos.CENTER);

        VBox box = new VBox(22, spinner, titleLabel, statusLabel);
        box.setAlignment(Pos.CENTER);
        getChildren().add(box);

        // animated "..." after the status text
        dotsTimeline = new Timeline(new KeyFrame(Duration.millis(380), e -> {
            dots = (dots + 1) % 4;
            statusLabel.setText("Loading trailer" + ".".repeat(dots));
        }));
        dotsTimeline.setCycleCount(Animation.INDEFINITE);
        animations.add(dotsTimeline);

        setVisible(false);
    }

    // ------------------------------------------------------------------ public API

    /** Shows the loading screen for the given movie title and starts all animations. */
    public void show(String movieTitle) {
        if (fade != null) {
            fade.stop();
        }
        titleLabel.setText(movieTitle);
        statusLabel.setTextFill(Color.web(Theme.MUTED_HEX));
        statusLabel.setText("Loading trailer");
        dots = 0;
        setOpacity(1.0);
        setVisible(true);
        animations.forEach(Animation::play);
    }

    /** Fades the loading screen away, then stops the animations to save CPU. */
    public void hide() {
        if (!isVisible()) {
            return;
        }
        fade = new FadeTransition(Duration.millis(500), this);
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            setVisible(false);
            animations.forEach(Animation::stop);
        });
        fade.play();
    }

    /** Keeps the screen up but replaces the loading text with an error message. */
    public void showError(String message) {
        if (fade != null) {
            fade.stop();
        }
        setOpacity(1.0);
        setVisible(true);
        dotsTimeline.stop();
        statusLabel.setTextFill(Color.web("#ff6b6b"));
        statusLabel.setText(message);
    }

    public boolean isShowing() {
        return isVisible() && getOpacity() > 0.01;
    }

    // ------------------------------------------------------------------ building blocks

    /** One spinning arc. A transparent full circle sits in the same Group so the Group's centre
     *  is the circle's centre - that makes the rotation spin around the middle, not the arc's tip. */
    private Group ring(double radius, double startAngle, double length, Color color,
                       double width, double secondsPerTurn, boolean clockwise) {
        Circle bounds = new Circle(radius + width);
        bounds.setFill(Color.TRANSPARENT);

        Arc arc = new Arc(0, 0, radius, radius, startAngle, length);
        arc.setType(ArcType.OPEN);
        arc.setFill(Color.TRANSPARENT);
        arc.setStroke(color);
        arc.setStrokeWidth(width);
        arc.setStrokeLineCap(StrokeLineCap.ROUND);
        arc.setEffect(new DropShadow(14, color));

        Group group = new Group(bounds, arc);

        RotateTransition spin = new RotateTransition(Duration.seconds(secondsPerTurn), group);
        spin.setByAngle(clockwise ? 360 : -360);
        spin.setCycleCount(Animation.INDEFINITE);
        spin.setInterpolator(Interpolator.LINEAR);
        animations.add(spin);
        return group;
    }

    /** The pulsing red "S" in the middle. */
    private Text logo() {
        Text s = new Text("S");
        s.setFont(Theme.bold(54));
        s.setFill(Color.web(Theme.ACCENT_HEX));
        s.setEffect(new DropShadow(20, Color.web(Theme.ACCENT_HEX)));

        ScaleTransition pulse = new ScaleTransition(Duration.millis(750), s);
        pulse.setFromX(0.85);
        pulse.setFromY(0.85);
        pulse.setToX(1.15);
        pulse.setToY(1.15);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setInterpolator(Interpolator.EASE_BOTH);
        animations.add(pulse);
        return s;
    }
}
