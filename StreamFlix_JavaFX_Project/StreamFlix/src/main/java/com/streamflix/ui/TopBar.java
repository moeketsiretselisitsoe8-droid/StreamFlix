package com.streamflix.ui;

import com.streamflix.audio.SoundManager;
import com.streamflix.audio.SoundManager.Sound;
import com.streamflix.util.Theme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.function.Consumer;

/**
 * TopBar - logo on the left, "Home" + one link per genre in the middle, sound switch on the right.
 * The links are Labels (not Buttons) with hover + click behaviour added by hand.
 *
 * PORTION: App shell & UI theme  |  OWNER: Retselisitsoe Moeketsi (group leader)
 */
public class TopBar extends HBox {

    public TopBar(List<String> categoryNames, Runnable onHome, Consumer<String> onCategory) {
        super(28);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(14, 36, 14, 36));
        setStyle("-fx-background-color: linear-gradient(to bottom, #000000, #101010);"
                + "-fx-border-color: transparent transparent #2a2a2a transparent;");

        Label logo = new Label("STREAMFLIX");
        logo.setFont(Theme.bold(30));
        logo.setTextFill(Theme.ACCENT);
        logo.setEffect(new DropShadow(10, Color.web("#E5091466")));
        logo.setCursor(Cursor.HAND);
        logo.setOnMouseClicked(e -> {
            SoundManager.play(Sound.CLICK);
            onHome.run();
        });

        getChildren().add(logo);
        getChildren().add(navItem("Home", onHome));
        for (String name : categoryNames) {
            getChildren().add(navItem(name, () -> onCategory.accept(name)));
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        getChildren().addAll(spacer, new SoundToggle());
    }

    private Label navItem(String text, Runnable action) {
        Label item = new Label(text);
        item.setFont(Theme.bold(15));
        item.setTextFill(Color.web("#d2d2d2"));
        item.setCursor(Cursor.HAND);
        item.setOnMouseEntered(e -> {
            item.setTextFill(Color.WHITE);
            item.setUnderline(true);
            SoundManager.play(Sound.HOVER);
        });
        item.setOnMouseExited(e -> {
            item.setTextFill(Color.web("#d2d2d2"));
            item.setUnderline(false);
        });
        item.setOnMouseClicked(e -> {
            SoundManager.play(Sound.CLICK);
            action.run();
        });
        return item;
    }
}
