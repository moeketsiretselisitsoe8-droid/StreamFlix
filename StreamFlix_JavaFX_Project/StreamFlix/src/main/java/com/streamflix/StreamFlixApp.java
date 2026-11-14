package com.streamflix;

import com.streamflix.audio.SoundManager;
import com.streamflix.audio.SoundManager.Sound;
import com.streamflix.data.MovieRepository;
import com.streamflix.model.Category;
import com.streamflix.model.Movie;
import com.streamflix.player.LocalPlayerServer;
import com.streamflix.player.PlayerView;
import com.streamflix.ui.HomeView;
import com.streamflix.ui.TopBar;
import com.streamflix.util.Theme;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * StreamFlixApp - the application entry point and "shell".
 *
 * It builds the main screens and decides which one is visible:
 *     root (StackPane)
 *       |- shell  : TopBar + HomeView   (the browsing screen)
 *       |- player : PlayerView          (hidden until a movie is clicked, then fades in on top)
 *
 * PORTION: App shell & UI theme  |  OWNER: Retselisitsoe Moeketsi (group leader)
 */
public class StreamFlixApp extends Application {

    private final LocalPlayerServer playerServer = new LocalPlayerServer();
    private PlayerView player;

    @Override
    public void start(Stage stage) {
        SoundManager.load();

        List<Category> categories = MovieRepository.loadCategories();
        if (categories.isEmpty()) {
            throw new IllegalStateException("movies.csv contains no movies");
        }
        Movie featured = chooseFeatured(categories);

        player = new PlayerView(getHostServices(), playerServer);
        player.setOnClose(this::hidePlayer);
        player.setVisible(false);

        HomeView home = new HomeView(categories, featured, this::showPlayer);
        List<String> names = categories.stream().map(Category::getName).collect(Collectors.toList());
        TopBar topBar = new TopBar(names, home::scrollToTop, home::scrollToCategory);

        BorderPane shell = new BorderPane();
        shell.setTop(topBar);
        shell.setCenter(home);

        StackPane root = new StackPane(shell, player);

        Scene scene = new Scene(root, 1320, 760, Theme.BACKGROUND);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        // Keyboard shortcuts while the player is open: ESC = back, SPACE = play/pause
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (!player.isVisible()) {
                return;
            }
            if (e.getCode() == KeyCode.ESCAPE) {
                SoundManager.play(Sound.BACK);
                player.close();
                e.consume();
            } else if (e.getCode() == KeyCode.SPACE) {
                player.togglePlayPause();
                e.consume();
            }
        });

        stage.setTitle("StreamFlix - Movie Streaming Demo");
        stage.setMinWidth(1280);
        stage.setMinHeight(640);
        stage.setScene(scene);
        stage.show();

        SoundManager.play(Sound.STARTUP);
    }

    @Override
    public void stop() {
        playerServer.stop();
    }

    // ------------------------------------------------------------------ navigation

    private void showPlayer(Movie movie) {
        player.setOpacity(0);
        player.setVisible(true);
        player.open(movie);
        FadeTransition in = new FadeTransition(Duration.millis(350), player);
        in.setToValue(1);
        in.play();
    }

    private void hidePlayer() {
        FadeTransition out = new FadeTransition(Duration.millis(250), player);
        out.setToValue(0);
        out.setOnFinished(e -> player.setVisible(false));
        out.play();
    }

    /** Featured movie on the hero banner: Inception if present, otherwise the first movie in the file. */
    private Movie chooseFeatured(List<Category> categories) {
        for (Category c : categories) {
            for (Movie m : c.getMovies()) {
                if (m.getTitle().equals("Inception")) {
                    return m;
                }
            }
        }
        return categories.get(0).getMovies().get(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
