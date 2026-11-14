package com.streamflix.ui;

import com.streamflix.model.Movie;
import com.streamflix.ui.CustomButton.Variant;
import com.streamflix.util.Theme;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * HeroBanner - the big "featured movie" panel at the top of the home page (like Netflix).
 * The background image slowly zooms (Ken-Burns effect) to keep the page feeling alive.
 *
 * PORTION: Custom UI components  |  OWNER: Moeti Leleka
 */
public class HeroBanner extends StackPane {

    public static final double WIDTH = 1140;
    public static final double HEIGHT = 330;

    public HeroBanner(Movie movie, Runnable onPlay, Runnable onBrowse) {
        setPrefSize(WIDTH, HEIGHT);
        setMinSize(WIDTH, HEIGHT);
        setMaxSize(WIDTH, HEIGHT);
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-background-color: #1a1a1a; -fx-background-radius: 16;");

        // ---- background image (tries the large one first, then falls back to the normal one)
        ImageView backdrop = new ImageView();
        backdrop.setFitWidth(WIDTH);
        backdrop.setFitHeight(HEIGHT);
        backdrop.setPreserveRatio(false);
        loadBackdrop(backdrop, movie.getBackdropUrl(), movie.getThumbnailUrl());

        ScaleTransition kenBurns = new ScaleTransition(Duration.seconds(14), backdrop);
        kenBurns.setFromX(1.0);
        kenBurns.setFromY(1.0);
        kenBurns.setToX(1.08);
        kenBurns.setToY(1.08);
        kenBurns.setAutoReverse(true);
        kenBurns.setCycleCount(Animation.INDEFINITE);
        kenBurns.setInterpolator(Interpolator.EASE_BOTH);
        kenBurns.play();

        // ---- gradients so the text is always readable
        Region leftShade = new Region();
        leftShade.setStyle("-fx-background-color: linear-gradient(to right, rgba(20,20,20,0.96) 0%, "
                + "rgba(20,20,20,0.65) 45%, rgba(20,20,20,0.0) 100%);");
        Region bottomShade = new Region();
        bottomShade.setStyle("-fx-background-color: linear-gradient(to top, rgba(20,20,20,0.9) 0%, "
                + "rgba(20,20,20,0.0) 40%);");

        // ---- text + buttons
        Label tag = new Label("FEATURED");
        tag.setFont(Theme.bold(13));
        tag.setTextFill(Color.web(Theme.ACCENT_HEX));

        Label title = new Label(movie.getTitle());
        title.setFont(Theme.bold(46));
        title.setTextFill(Color.WHITE);
        title.setEffect(new DropShadow(12, Color.BLACK));
        title.setWrapText(true);
        title.setMaxWidth(560);

        Label meta = new Label(movie.getGenre() + "   •   " + movie.getYear() + "   •   Official trailer");
        meta.setFont(Theme.regular(15));
        meta.setTextFill(Color.web("#d0d0d0"));

        CustomButton play = new CustomButton("Play trailer", Variant.PRIMARY, 17);
        play.setIcon(Icons.play(16, Color.WHITE));
        play.setOnAction(onPlay);

        CustomButton browse = new CustomButton("Browse movies", Variant.SECONDARY, 17);
        browse.setOnAction(onBrowse);

        HBox buttons = new HBox(14, play, browse);
        buttons.setAlignment(Pos.CENTER_LEFT);
        buttons.setPadding(new Insets(10, 0, 0, 0));

        VBox texts = new VBox(8, tag, title, meta, buttons);
        texts.setAlignment(Pos.CENTER_LEFT);
        texts.setPadding(new Insets(0, 0, 0, 44));
        texts.setMaxWidth(640);
        StackPane.setAlignment(texts, Pos.CENTER_LEFT);

        getChildren().addAll(backdrop, leftShade, bottomShade, texts);

        Rectangle clip = new Rectangle(WIDTH, HEIGHT);
        clip.setArcWidth(32);
        clip.setArcHeight(32);
        setClip(clip);
    }

    private void loadBackdrop(ImageView view, String primaryUrl, String fallbackUrl) {
        Image image = new Image(primaryUrl, true);
        image.errorProperty().addListener((obs, was, isError) -> {
            if (isError) {
                useImage(view, new Image(fallbackUrl, true));     // maxres missing -> use normal thumbnail
            }
        });
        useImage(view, image);
    }

    private void useImage(ImageView view, Image image) {
        image.progressProperty().addListener((obs, old, progress) -> {
            if (progress.doubleValue() >= 1.0 && !image.isError()) {
                coverCrop(view, image);
                view.setImage(image);
            }
        });
        if (image.getProgress() >= 1.0 && !image.isError()) {
            coverCrop(view, image);
            view.setImage(image);
        }
    }

    /** Crops the image so it fills the banner without stretching ("cover" behaviour). */
    private void coverCrop(ImageView view, Image image) {
        double wantedHeight = image.getWidth() * HEIGHT / WIDTH;
        if (image.getHeight() > wantedHeight) {
            view.setViewport(new Rectangle2D(0, (image.getHeight() - wantedHeight) / 2,
                    image.getWidth(), wantedHeight));
        }
    }
}
