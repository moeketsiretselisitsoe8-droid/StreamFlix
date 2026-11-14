package com.streamflix.ui;

import com.streamflix.model.Category;
import com.streamflix.model.Movie;
import com.streamflix.util.Theme;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * HomeView - the scrolling home page: hero banner, then one CategoryRow per genre, then a footer.
 *
 * PORTION: App shell & UI theme  |  OWNER: Retselisitsoe Moeketsi (group leader)
 * (It glues together everybody's parts: HeroBanner + CategoryRow + MovieCard.)
 */
public class HomeView extends ScrollPane {

    private final VBox content = new VBox(30);
    private final Map<String, CategoryRow> rows = new LinkedHashMap<>();
    private Timeline scrollAnimation;

    public HomeView(List<Category> categories, Movie featured, Consumer<Movie> onPlay) {
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(24, 0, 40, 0));
        content.setStyle("-fx-background-color: " + Theme.BACKGROUND_HEX + ";");

        HeroBanner hero = new HeroBanner(featured,
                () -> onPlay.accept(featured),
                () -> {
                    if (!categories.isEmpty()) {
                        scrollToCategory(categories.get(0).getName());
                    }
                });
        content.getChildren().add(hero);

        for (Category category : categories) {
            CategoryRow row = new CategoryRow(category, onPlay);
            rows.put(category.getName(), row);
            content.getChildren().add(row);
        }

        Label footer = new Label("StreamFlix demo  •  Group project  •  BSc Software Engineering & Multimedia");
        footer.setFont(Theme.regular(12));
        footer.setTextFill(Color.web("#6b6b6b"));
        footer.setPadding(new Insets(20, 0, 0, 0));
        content.getChildren().add(footer);

        setContent(content);
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setPannable(false);
    }

    /** Smoothly scrolls back to the hero banner. */
    public void scrollToTop() {
        animateTo(0);
    }

    /** Smoothly scrolls so the row with this genre name is at the top. */
    public void scrollToCategory(String name) {
        CategoryRow row = rows.get(name);
        if (row == null) {
            return;
        }
        content.applyCss();
        content.layout();
        double scrollable = content.getBoundsInLocal().getHeight() - getViewportBounds().getHeight();
        if (scrollable <= 0) {
            return;
        }
        double y = row.getBoundsInParent().getMinY() - 16;
        animateTo(Math.max(0, Math.min(1, y / scrollable)));
    }

    private void animateTo(double vvalue) {
        if (scrollAnimation != null) {
            scrollAnimation.stop();
        }
        scrollAnimation = new Timeline(new KeyFrame(Duration.millis(550),
                new KeyValue(vvalueProperty(), vvalue, Interpolator.EASE_BOTH)));
        scrollAnimation.play();
    }
}
