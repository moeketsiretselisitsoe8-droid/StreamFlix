package com.streamflix.ui;

import com.streamflix.audio.SoundManager;
import com.streamflix.audio.SoundManager.Sound;
import com.streamflix.model.Movie;
import com.streamflix.util.Theme;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.function.Consumer;

/**
 * MovieCard - one clickable thumbnail + title in a category row.
 *
 * Hover effect (all at once):  card grows + lifts up, thumbnail gets a red glow,
 * a dark gradient fades in with a round play icon, and the title turns white.
 *
 * PORTION: Custom UI components  |  OWNER: Moeti Leleka
 */
public class MovieCard extends VBox {

    public static final double WIDTH = 208;
    public static final double IMAGE_HEIGHT = WIDTH * 9 / 16;   // 16:9 thumbnails

    private final Movie movie;
    private final StackPane imageBox = new StackPane();
    private final StackPane overlay = new StackPane();
    private final Label titleLabel = new Label();
    private final DropShadow glow = new DropShadow(0, Color.web(Theme.ACCENT_HEX));

    public MovieCard(Movie movie, Consumer<Movie> onSelect) {
        super(8);
        this.movie = movie;

        setPrefWidth(WIDTH);
        setMinWidth(WIDTH);
        setMaxWidth(WIDTH);
        setCursor(Cursor.HAND);

        buildImageBox();
        buildTexts();
        getChildren().addAll(imageBox, titleLabel, buildSubtitle());

        setOnMouseEntered(e -> animateHover(true));
        setOnMouseExited(e -> animateHover(false));
        setOnMouseClicked(e -> {
            SoundManager.play(Sound.OPEN);
            onSelect.accept(movie);
        });
    }

    public Movie getMovie() {
        return movie;
    }

    // ------------------------------------------------------------------ building the card

    private void buildImageBox() {
        imageBox.setPrefSize(WIDTH, IMAGE_HEIGHT);
        imageBox.setMinSize(WIDTH, IMAGE_HEIGHT);
        imageBox.setMaxSize(WIDTH, IMAGE_HEIGHT);

        // 1) placeholder, visible until the image arrives (or if the user is offline)
        Label placeholder = new Label(movie.getTitle());
        placeholder.setFont(Theme.bold(15));
        placeholder.setTextFill(Color.web("#8a8a8a"));
        placeholder.setWrapText(true);
        placeholder.setTextAlignment(TextAlignment.CENTER);
        placeholder.setMaxWidth(WIDTH - 24);
        StackPane placeholderBg = new StackPane(placeholder);
        placeholderBg.setStyle("-fx-background-color: linear-gradient(to bottom right, #2b2b2b, #1a1a1a);");

        // 2) the thumbnail, downloaded from YouTube in the background (UI never freezes)
        ImageView imageView = new ImageView();
        imageView.setFitWidth(WIDTH);
        imageView.setFitHeight(IMAGE_HEIGHT);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);
        Image image = new Image(movie.getThumbnailUrl(), true);   // true = load in background
        image.progressProperty().addListener((obs, old, progress) -> {
            if (progress.doubleValue() >= 1.0 && !image.isError()) {
                cropToWidescreen(imageView, image);
                imageView.setImage(image);
            }
        });
        if (image.getProgress() >= 1.0 && !image.isError()) {      // already cached
            cropToWidescreen(imageView, image);
            imageView.setImage(image);
        }

        // 3) hover overlay: dark gradient + round play icon (starts invisible)
        Region shade = new Region();
        shade.setStyle("-fx-background-color: linear-gradient(to top, rgba(0,0,0,0.85), rgba(0,0,0,0.15));");
        Circle ring = new Circle(22, Color.web(Theme.ACCENT_HEX, 0.92));
        overlay.getChildren().addAll(shade, ring, Icons.play(18, Color.WHITE));
        overlay.setAlignment(Pos.CENTER);
        overlay.setOpacity(0);
        overlay.setMouseTransparent(true);

        imageBox.getChildren().addAll(placeholderBg, imageView, overlay);

        // rounded corners
        Rectangle clip = new Rectangle(WIDTH, IMAGE_HEIGHT);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        imageBox.setClip(clip);

        // Note: a clipped node would cut its own shadow off, so the red glow is applied
        // to the whole card (not to imageBox) - see animateHover().
    }

    private void buildTexts() {
        titleLabel.setText(movie.getTitle());
        titleLabel.setFont(Theme.bold(14));
        titleLabel.setTextFill(Color.web("#d6d6d6"));
        titleLabel.setMaxWidth(WIDTH);
        titleLabel.setMinHeight(20);
    }

    private Label buildSubtitle() {
        Label sub = new Label(movie.getYear() + "  •  " + movie.getGenre());
        sub.setFont(Theme.regular(12));
        sub.setTextFill(Color.web(Theme.MUTED_HEX));
        return sub;
    }

    /** YouTube's hqdefault image is 4:3 with black bars; crop it to a clean centred 16:9 area. */
    private static void cropToWidescreen(ImageView view, Image image) {
        double w = image.getWidth();
        double h = image.getHeight();
        double wantedHeight = w * 9 / 16;
        if (h > wantedHeight + 1) {
            view.setViewport(new Rectangle2D(0, (h - wantedHeight) / 2, w, wantedHeight));
        }
    }

    // ------------------------------------------------------------------ hover animation

    private void animateHover(boolean entering) {
        Duration d = Duration.millis(180);

        ScaleTransition scale = new ScaleTransition(d, this);
        scale.setToX(entering ? 1.10 : 1.0);
        scale.setToY(entering ? 1.10 : 1.0);

        TranslateTransition lift = new TranslateTransition(d, this);
        lift.setToY(entering ? -6 : 0);

        FadeTransition fade = new FadeTransition(d, overlay);
        fade.setToValue(entering ? 1.0 : 0.0);

        new ParallelTransition(scale, lift, fade).play();

        if (entering) {
            glow.setRadius(26);
            glow.setSpread(0.15);
            setEffect(glow);                      // red glow around the card
            titleLabel.setTextFill(Color.WHITE);
            SoundManager.play(Sound.HOVER);
            setViewOrder(-1);                     // draw above neighbours WITHOUT reordering the HBox children
        } else {
            setViewOrder(0);
            setEffect(null);
            titleLabel.setTextFill(Color.web("#d6d6d6"));
        }
    }
}
