package com.streamflix.player;

import com.streamflix.audio.SoundManager;
import com.streamflix.audio.SoundManager.Sound;
import com.streamflix.model.Movie;
import com.streamflix.ui.CustomButton;
import com.streamflix.ui.CustomButton.Variant;
import com.streamflix.ui.Icons;
import com.streamflix.ui.LoadingAnimation;
import com.streamflix.util.Theme;
import javafx.animation.PauseTransition;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import netscape.javascript.JSObject;

/**
 * PlayerView - the full-screen trailer player.
 *
 *   top    : custom Back button + movie title
 *   centre : WebView showing the YouTube trailer  (+ our LoadingAnimation on top while it loads)
 *   bottom : custom controls - Play/Pause, Restart, Mute, Backup trailer, Open on YouTube
 *
 * How play/pause works: the YouTube player lives inside an iframe (see LocalPlayerServer). We control it by
 * running a tiny JavaScript line with WebEngine.executeScript that posts a message such as {"func":"pauseVideo"}
 * to the iframe. YouTube's own controls are switched off (controls=0), so ONLY our custom controls are visible.
 *
 * PORTION: Video playback  |  OWNER: Pofane Matlali
 */
public class PlayerView extends BorderPane {

    private final WebView webView = new WebView();
    private final WebEngine engine = webView.getEngine();
    private final LoadingAnimation loading = new LoadingAnimation();
    private final LocalPlayerServer server;
    private final HostServices hostServices;

    // IMPORTANT: keep a strong reference, otherwise Java may garbage-collect the bridge and JavaScript calls fail.
    private final Bridge bridge = new Bridge();
    private final PauseTransition loadingTimeout = new PauseTransition(Duration.seconds(4));

    private final Label titleLabel = new Label();
    private final Label metaLabel = new Label();
    private CustomButton playPauseButton;
    private CustomButton muteButton;
    private CustomButton backupButton;

    private Movie current;
    private boolean usingBackup = false;
    private boolean playing = false;
    private boolean muted = false;
    private Runnable onClose = () -> { };

    public PlayerView(HostServices hostServices, LocalPlayerServer server) {
        this.hostServices = hostServices;
        this.server = server;

        setStyle("-fx-background-color: #000000;");
        setTop(buildTopBar());
        setCenter(buildCenter());
        setBottom(buildControls());

        // Some YouTube features check the browser name, so identify as a modern browser.
        engine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                + "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        webView.setContextMenuEnabled(false);

        engine.getLoadWorker().stateProperty().addListener((obs, old, state) -> {
            if (state == Worker.State.SUCCEEDED && current != null) {
                attachBridge();
                loadingTimeout.playFromStart();           // hide the loading screen even if no "playing" event arrives
            } else if (state == Worker.State.FAILED && current != null) {
                loading.showError("Could not reach YouTube. Check your internet connection, then press Restart.");
            }
        });
        loadingTimeout.setOnFinished(e -> loading.hide());
    }

    // ------------------------------------------------------------------ public API

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    /** Loads and starts the trailer for this movie. */
    public void open(Movie movie) {
        this.current = movie;
        this.usingBackup = false;
        this.playing = true;
        this.muted = false;

        titleLabel.setText(movie.getTitle());
        metaLabel.setText(movie.getGenre() + "  •  " + movie.getYear() + "  •  Official trailer");
        backupButton.setEnabled(movie.hasBackup());
        backupButton.setText("Backup trailer");
        refreshControls();

        loadCurrentTrailer();
    }

    /** Stops the video (by leaving the page) and tells the app to hide the player. */
    public void close() {
        loadingTimeout.stop();
        engine.load("about:blank");                       // stops all sound immediately
        current = null;
        loading.hide();
        onClose.run();
    }

    public void togglePlayPause() {
        if (current == null) {
            return;
        }
        command(playing ? "pauseVideo" : "playVideo");
        playing = !playing;
        refreshControls();
    }

    // ------------------------------------------------------------------ building the screen

    private HBox buildTopBar() {
        CustomButton back = new CustomButton("Back", Variant.GHOST, 15);
        back.setIcon(Icons.chevron(14, Color.WHITE, -1));
        back.setOnAction(() -> {
            SoundManager.play(Sound.BACK);
            close();
        });

        titleLabel.setFont(Theme.bold(22));
        titleLabel.setTextFill(Color.WHITE);
        metaLabel.setFont(Theme.regular(13));
        metaLabel.setTextFill(Color.web(Theme.MUTED_HEX));
        VBox texts = new VBox(2, titleLabel, metaLabel);
        texts.setAlignment(Pos.CENTER_LEFT);

        HBox bar = new HBox(20, back, texts);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 24, 12, 24));
        bar.setStyle("-fx-background-color: linear-gradient(to bottom, #000000, #0b0b0b);");
        return bar;
    }

    private StackPane buildCenter() {
        StackPane center = new StackPane(webView, loading);
        center.setStyle("-fx-background-color: #000000;");
        return center;
    }

    private HBox buildControls() {
        playPauseButton = new CustomButton("Pause", Variant.PRIMARY, 16);
        playPauseButton.setOnAction(this::togglePlayPause);

        CustomButton restartButton = new CustomButton("Restart", Variant.SECONDARY, 15);
        restartButton.setOnAction(this::restart);

        muteButton = new CustomButton("Mute", Variant.SECONDARY, 15);
        muteButton.setOnAction(this::toggleMute);

        backupButton = new CustomButton("Backup trailer", Variant.SECONDARY, 15);
        backupButton.setOnAction(this::toggleBackup);

        CustomButton youtubeButton = new CustomButton("Open on YouTube", Variant.GHOST, 15);
        youtubeButton.setOnAction(() -> {
            if (current != null) {
                command("pauseVideo");
                playing = false;
                refreshControls();
                hostServices.showDocument(current.getWatchUrl(usingBackup));
            }
        });

        HBox bar = new HBox(14, playPauseButton, restartButton, muteButton, backupButton, youtubeButton);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(14, 24, 16, 24));
        bar.setStyle("-fx-background-color: linear-gradient(to top, #000000, #0b0b0b);");
        return bar;
    }

    // ------------------------------------------------------------------ control actions

    private void restart() {
        if (current == null) {
            return;
        }
        if (loading.isShowing()) {
            loadCurrentTrailer();                         // loading screen is stuck / failed -> reload everything
        } else {
            command("seekTo", "0, true");
            command("playVideo");
            playing = true;
            refreshControls();
        }
    }

    private void toggleMute() {
        command(muted ? "unMute" : "mute");
        muted = !muted;
        refreshControls();
    }

    private void toggleBackup() {
        if (current == null || !current.hasBackup()) {
            return;
        }
        usingBackup = !usingBackup;
        backupButton.setText(usingBackup ? "Main trailer" : "Backup trailer");
        loadCurrentTrailer();
    }

    private void refreshControls() {
        playPauseButton.setText(playing ? "Pause" : "Play");
        playPauseButton.setIcon(playing ? Icons.pause(15, Color.WHITE) : Icons.play(15, Color.WHITE));
        muteButton.setText(muted ? "Unmute" : "Mute");
        muteButton.setIcon(Icons.speaker(18, Color.WHITE, !muted));
    }

    // ------------------------------------------------------------------ talking to YouTube

    private void loadCurrentTrailer() {
        String id = (usingBackup && current.hasBackup()) ? current.getBackupId() : current.getTrailerId();
        playing = true;
        muted = false;
        refreshControls();
        loading.show(current.getTitle());
        loadingTimeout.stop();
        engine.load(server.playerUrl(id));
    }

    /** Gives the web page a Java object called "javaBridge" so JavaScript can report video events back to us. */
    private void attachBridge() {
        try {
            JSObject window = (JSObject) engine.executeScript("window");
            window.setMember("javaBridge", bridge);
        } catch (Exception e) {
            System.err.println("Could not attach bridge: " + e.getMessage());
        }
    }

    private void command(String function) {
        command(function, "");
    }

    /** Sends a command (playVideo, pauseVideo, mute, seekTo ...) to the YouTube iframe. */
    private void command(String function, String args) {
        String js = "(function(){var f=document.getElementById('yt');"
                + "if(f&&f.contentWindow){f.contentWindow.postMessage(JSON.stringify("
                + "{event:'command',func:'" + function + "',args:[" + args + "]}),'*');}})();";
        try {
            engine.executeScript(js);
        } catch (Exception ignored) {
            // page not ready yet - nothing to control
        }
    }

    /** Called (via JavaScript) when the video changes state: 1 = playing, 2 = paused, 0 = ended, 3 = buffering. */
    private void handleState(int state) {
        if (current == null) {
            return;
        }
        if (state == 1) {
            playing = true;
            loading.hide();                               // the video really started -> remove loading animation
        } else if (state == 2 || state == 0) {
            playing = false;
        }
        refreshControls();
    }

    /** Called when YouTube cannot play this video inside our app (owner blocked embedding, video removed ...). */
    private void handleError(int code) {
        if (current == null) {
            return;
        }
        if (!usingBackup && current.hasBackup()) {        // automatically try the second trailer once
            usingBackup = true;
            backupButton.setText("Main trailer");
            loadCurrentTrailer();
        } else {
            loading.showError("This trailer cannot be played inside the app (YouTube error " + code
                    + "). Press \"Open on YouTube\" to watch it in your browser.");
        }
    }

    /**
     * Receives calls from the web page's JavaScript. It MUST be public (JavaScript can only call public members).
     * Each call is moved onto the JavaFX thread with Platform.runLater, because UI may only change there.
     */
    public class Bridge {
        public void onState(int state) {
            Platform.runLater(() -> handleState(state));
        }

        public void onError(int code) {
            Platform.runLater(() -> handleError(code));
        }
    }
}
