package com.streamflix.ui;

import com.streamflix.audio.SoundManager;
import com.streamflix.audio.SoundManager.Sound;
import com.streamflix.model.Category;
import com.streamflix.model.Movie;
import com.streamflix.util.Theme;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.function.Consumer;

/**
 * CategoryRow - a labelled genre with a horizontally scrolling strip of MovieCards.
 *
 * Rules from the assignment: show ONLY 5 thumbnails at a time, with the ability to scroll to the rest.
 *  - The visible window ("viewport") is exactly 5 cards wide and is clipped, so card 6 is never seen.
 *  - The strip of ALL cards slides left/right with a smooth animation (Timeline + EASE_BOTH).
 *  - Scroll with the side arrows, a horizontal mouse-wheel / trackpad swipe, or Shift + mouse-wheel.
 *
 * PORTION: Data & Categories  |  OWNER: Rorisang Konyana
 */
public class CategoryRow extends VBox {

    public static final int VISIBLE = 5;                 // thumbnails shown at once
    private static final int ARROW_STEP = 3;             // cards moved per arrow click
    private static final double GAP = 18;                // space between cards
    private static final double SIDE_PAD = 14;           // room for the hover-grow effect
    private static final double TOP_PAD = 22;
    private static final double ARROW_WIDTH = 44;

    private static final double VIEWPORT_WIDTH = VISIBLE * MovieCard.WIDTH + (VISIBLE - 1) * GAP + 2 * SIDE_PAD;
    private static final double VIEWPORT_HEIGHT = 216;

    /** Total width of the row including both arrows - HomeView uses it for layout. */
    public static final double TOTAL_WIDTH = VIEWPORT_WIDTH + 2 * ARROW_WIDTH;

    private final Category category;
    private final HBox strip = new HBox(GAP);
    private final Label counter = new Label();
    private final StackPane leftArrow;
    private final StackPane rightArrow;
    private final int maxIndex;
    private int index = 0;                                // first visible card
    private long lastWheel = 0;

    public CategoryRow(Category category, Consumer<Movie> onSelect) {
        super(6);
        this.category = category;
        this.maxIndex = Math.max(0, category.size() - VISIBLE);

        setMaxWidth(USE_PREF_SIZE);
        getChildren().add(buildHeader());

        // ---- the strip with every card
        for (Movie m : category.getMovies()) {
            strip.getChildren().add(new MovieCard(m, onSelect));
        }
        strip.setLayoutX(SIDE_PAD);
        strip.setLayoutY(TOP_PAD);

        // ---- the clipped viewport that only shows 5 cards
        Pane viewport = new Pane(strip);
        viewport.setPrefSize(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        viewport.setMinSize(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        viewport.setMaxSize(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        viewport.setClip(new Rectangle(VIEWPORT_WIDTH, VIEWPORT_HEIGHT));
        viewport.setOnScroll(e -> {
            double d = (e.isShiftDown() && e.getDeltaX() == 0) ? e.getDeltaY()
                    : (Math.abs(e.getDeltaX()) > Math.abs(e.getDeltaY()) ? e.getDeltaX() : 0);
            if (d == 0) {
                return;                                   // normal vertical wheel: let the page scroll
            }
            e.consume();
            long now = System.currentTimeMillis();
            if (now - lastWheel < 250) {
                return;                                   // one step per swipe, not 30
            }
            lastWheel = now;
            scrollBy(d < 0 ? 1 : -1);
        });

        leftArrow = buildArrow(-1);
        rightArrow = buildArrow(+1);

        HBox line = new HBox(leftArrow, viewport, rightArrow);
        line.setAlignment(Pos.CENTER_LEFT);
        getChildren().add(line);

        updateIndicators();
    }

    public String getCategoryName() {
        return category.getName();
    }

    // ------------------------------------------------------------------ building

    private HBox buildHeader() {
        Rectangle accent = new Rectangle(5, 26);
        accent.setArcWidth(5);
        accent.setArcHeight(5);
        accent.setFill(Theme.ACCENT);

        Label name = new Label(category.getName());
        name.setFont(Theme.bold(26));
        name.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        counter.setFont(Theme.regular(13));
        counter.setTextFill(Color.web(Theme.MUTED_HEX));

        HBox header = new HBox(12, accent, name, spacer, counter);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, SIDE_PAD + 4, 0, ARROW_WIDTH + SIDE_PAD));
        return header;
    }

    private StackPane buildArrow(int direction) {
        StackPane arrow = new StackPane(Icons.chevron(22, Color.WHITE, direction));
        arrow.setPrefSize(ARROW_WIDTH, VIEWPORT_HEIGHT);
        arrow.setMinSize(ARROW_WIDTH, VIEWPORT_HEIGHT);
        arrow.setMaxSize(ARROW_WIDTH, VIEWPORT_HEIGHT);
        arrow.setCursor(Cursor.HAND);
        arrow.setOnMouseEntered(e -> {
            if (canMove(direction)) {
                arrow.setStyle("-fx-background-color: rgba(255,255,255,0.10); -fx-background-radius: 10;");
                SoundManager.play(Sound.HOVER);
            }
        });
        arrow.setOnMouseExited(e -> arrow.setStyle("-fx-background-color: transparent;"));
        arrow.setOnMouseClicked(e -> scrollBy(direction * ARROW_STEP));
        return arrow;
    }

    // ------------------------------------------------------------------ scrolling

    private boolean canMove(int direction) {
        return direction < 0 ? index > 0 : index < maxIndex;
    }

    /** Moves the strip by a number of cards (negative = left) with a smooth slide. */
    private void scrollBy(int cards) {
        int target = Math.max(0, Math.min(maxIndex, index + cards));
        if (target == index) {
            return;                                       // already at the start / end
        }
        index = target;
        SoundManager.play(Sound.SCROLL);

        double x = -index * (MovieCard.WIDTH + GAP);
        Timeline slide = new Timeline(new KeyFrame(Duration.millis(450),
                new KeyValue(strip.translateXProperty(), x, Interpolator.EASE_BOTH)));
        slide.play();
        updateIndicators();
    }

    private void updateIndicators() {
        int last = Math.min(index + VISIBLE, category.size());
        counter.setText((index + 1) + " - " + last + " of " + category.size());
        leftArrow.setOpacity(canMove(-1) ? 1.0 : 0.15);
        rightArrow.setOpacity(canMove(+1) ? 1.0 : 0.15);
    }
}
