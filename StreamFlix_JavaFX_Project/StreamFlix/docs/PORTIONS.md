# Who builds what — StreamFlix portions

Five members, five portions. Each portion is a set of files that one person **owns**, reads, runs, explains and
improves. The `PORTION / OWNER` comment at the top of every Java file shows who it belongs to.

| # | Member | Portion | Files | Rubric areas it mainly earns |
|---|--------|---------|-------|------------------------------|
| 1 | **Retselisitsoe Moeketsi** (leader) | App shell, theme & integration | `StreamFlixApp`, `Launcher`, `Theme`, `TopBar`, `HomeView`, `style.css`, `pom.xml`, docs | UI design (10), Code quality (10), Git/merging (10) |
| 2 | **Rorisang Konyana** | Data & categories + 5-thumbnail scrolling row | `Movie`, `Category`, `MovieRepository`, `movies.csv`, `CategoryRow` | Categories (10), Thumbnail display (10) |
| 3 | **Moeti Leleka** | Custom UI components & hover effects | `CustomButton`, `Icons`, `MovieCard`, `HeroBanner` | Hover effects (10), Custom controls (5), UI design |
| 4 | **Pofane Matlali** | Video playback from YouTube | `PlayerView`, `LocalPlayerServer` | Video playback (15), Custom controls (5) |
| 5 | **Lethako Khabele** | Sound effects & custom loading animation | `SoundManager`, `SoundToggle`, `LoadingAnimation`, `tools/generate_sounds.py`, `sounds/*.wav` | Sound (5), Custom animations (part of 10) |

Rubric rows 1 (Application, 20) and 11 (Evidence of students, 15) are shared by everyone.
Row 11 is the viva: **each of you must be able to explain your own files line by line, and have a rough idea how the others fit in.**

---

## How the parts connect (draw this on the board in the presentation)

```
StreamFlixApp (Retselisitsoe)
 ├─ MovieRepository ──reads── movies.csv ──> Category / Movie            (Rorisang)
 ├─ TopBar ── SoundToggle (Lethako)                                     (Retselisitsoe)
 ├─ HomeView                                                             (Retselisitsoe)
 │    ├─ HeroBanner ── CustomButton, Icons                               (Moeti)
 │    └─ CategoryRow ── MovieCard ── Icons                               (Rorisang, Moeti)
 └─ PlayerView ── LocalPlayerServer, LoadingAnimation, CustomButton      (Pofane, Lethako)
SoundManager is used by almost everything (Lethako)
```

The contract between parts is small and stable: `Movie` (getters), `SoundManager.play(Sound.X)`,
`new CustomButton(text, Variant)`, `loading.show(title)/hide()`. Nobody should rename these without telling the group.

---

## 1. Retselisitsoe Moeketsi — App shell, theme & integration (leader)

**Owns:** `StreamFlixApp.java`, `Launcher.java`, `Theme.java`, `TopBar.java`, `HomeView.java`, `css/style.css`, `pom.xml`, `README.md`, `docs/`

**What it does**
- `StreamFlixApp` starts JavaFX, loads the catalogue, builds the home screen and the (hidden) player, fades between them, handles ESC / SPACE shortcuts and shuts the local server down on exit.
- `Theme` keeps every colour/font in one place → consistent look (rubric: "consistent theme").
- `TopBar` has the logo, nav links (Home + one per genre, scroll smoothly to the row) and the sound switch.
- `HomeView` is the page: hero banner → rows → footer, with an animated scroll to a chosen row.
- Leader duties: repo set-up, branches, reviewing/merging, resolving conflicts, final integration test.

**Be ready to explain (viva)**
1. Why is the player a second layer in a `StackPane` instead of a new window/scene? *(simple, instant fade, keeps the home page's scroll position)*
2. What is the difference between `StreamFlixApp` and `Launcher`? *(Launcher is a plain class, avoids the "JavaFX runtime components missing" error)*
3. How does clicking "Sci-Fi" in the top bar scroll the page? *(`scrollToCategory` computes the row's Y ÷ scrollable height and animates `vvalue` with a `Timeline`)*
4. Where would you change the theme colour? *(`Theme.java` + `style.css`)*
5. What does `stop()` do and why? *(closes the local web server)*

**Your own improvements (each can be a real commit)**
- Add a "My List" nav item, or a window icon.
- Add a fade-in splash screen before the home screen.
- Write the group's short user manual / screenshots for submission.

## 2. Rorisang Konyana — Data & categories + scrolling row

**Owns:** `model/Movie.java`, `model/Category.java`, `data/MovieRepository.java`, `resources/data/movies.csv`, `ui/CategoryRow.java`

**What it does**
- `Movie` / `Category` are plain data classes (encapsulation: private fields, getters, immutable).
- `MovieRepository` reads `movies.csv` and groups lines by genre → 3 categories × 8 movies. Adding a movie = adding one text line.
- `CategoryRow` shows **only 5 thumbnails**: a viewport exactly 5 cards wide with a clip; the full strip of cards slides inside it (`translateX` animated with a `Timeline`). Scroll by the arrow controls, a horizontal swipe, or Shift + wheel. The "1 – 5 of 8" counter and faded arrows show the position.

**Be ready to explain (viva)**
1. How do you guarantee only 5 are visible? *(viewport width = 5×card + 4×gap + padding, with `setClip`)*
2. How is the smooth scroll made? *(`Timeline` + `KeyValue(translateXProperty, x, Interpolator.EASE_BOTH)`)*
3. Why a CSV and not hard-coded lists? *(separation of data and code)*
4. What does `maxIndex = size − 5` do? *(stops the strip at the last 5 movies)*
5. How would you add a 4th category? *(add 8 lines with a new genre name – no Java change)*

**Your own improvements**
- Validate the CSV (warn if a genre has fewer than 8 movies).
- Add a "Top 10" row, or keyboard arrow-key scrolling.
- Double-check every trailer ID still plays (see README troubleshooting) and replace dead ones.

## 3. Moeti Leleka — Custom UI components & hover effects

**Owns:** `ui/CustomButton.java`, `ui/Icons.java`, `ui/MovieCard.java`, `ui/HeroBanner.java`

**What it does**
- `CustomButton` is our own button (a `StackPane` + `Label`) — the assignment forbids the `Button` class. Hover colour, grow on hover, press "squash", hand cursor, click sound, action callback.
- `Icons` draws play, pause, chevron and speaker icons from JavaFX shapes (no image files).
- `MovieCard` = thumbnail + title. Hover effect: card scales to 110 %, lifts, red glow, dark gradient with round play icon fades in, title turns white, hover sound.
- `HeroBanner` = featured movie with a slow "Ken Burns" zoom, readable gradients and two custom buttons.

**Be ready to explain (viva)**
1. How do you make a button without `Button`? *(extend `StackPane`, add mouse handlers)*
2. How is the hover effect built? *(`ParallelTransition` of `ScaleTransition` + `TranslateTransition` + `FadeTransition`, plus a `DropShadow`)*
3. Why is the thumbnail cropped? *(YouTube's image is 4:3 with black bars → `Rectangle2D` viewport gives a clean 16:9)*
4. Why `setViewOrder(-1)` instead of `toFront()`? *(toFront would reorder the HBox children and break the layout)*
5. Why does the image load with `new Image(url, true)`? *(background loading → UI never freezes)*

**Your own improvements**
- Add a 4th button variant (e.g. outline), or a "pressed" ripple effect.
- Add a rating/“NEW” badge on the card.
- Tune the hover animation timing and colours until you like it.

## 4. Pofane Matlali — Video playback from YouTube

**Owns:** `player/PlayerView.java`, `player/LocalPlayerServer.java`

**What it does**
- `PlayerView` is the player screen: Back button, title, `WebView`, and custom controls **Play/Pause, Restart, Mute, Backup trailer, Open on YouTube**. YouTube's own controls are off (`controls=0`), so only ours show.
- Play/pause works by running JavaScript (`executeScript`) that posts `{func:'pauseVideo'}` to the YouTube iframe.
- A Java object `javaBridge` lets the page report **state changes** (playing / paused / ended) and **errors** back to Java, so the Play/Pause label and the loading animation stay correct. If a trailer can't be embedded it switches to the backup automatically.
- `LocalPlayerServer` is a tiny JDK web server on `127.0.0.1` that serves the page containing the iframe. YouTube blocks embeds from pages with no real web address (Error 153); this fixes that. **No video file is ever stored** – streaming is straight from YouTube.

**Be ready to explain (viva)**
1. Why not just `engine.load("https://youtube.com/embed/...")`? *(works only sometimes; the local page gives a valid origin/referrer and a place for our JS)*
2. How does the Pause button reach the video? *(Java → `executeScript` → `postMessage` → YouTube iframe)*
3. Why `Platform.runLater` in `Bridge`? *(JS calls arrive on a different thread; UI changes must run on the JavaFX thread)*
4. Why keep `bridge` as a field? *(garbage collection would otherwise delete it and JS calls would silently fail)*
5. What happens on error 101/150/153? *(auto-switch to backup ID, else show a message and offer "Open on YouTube")*

**Your own improvements**
- Add a "skip 10 seconds" control (needs `getCurrentTime` → use a `javaBridge` callback).
- Add volume levels instead of just mute.
- Test on a slow connection and a no-internet run; improve the error messages.

## 5. Lethako Khabele — Sound effects & custom loading animation

**Owns:** `audio/SoundManager.java`, `ui/SoundToggle.java`, `ui/LoadingAnimation.java`, `tools/generate_sounds.py`, `resources/sounds/*.wav`

**What it does**
- `SoundManager` loads 7 short sounds once and exposes `SoundManager.play(Sound.X)`; supports mute; limits hover ticks so they never "machine-gun"; keeps working silently if audio is missing.
- Sounds: hover, click, open (trailer), back, scroll, toggle, startup. They are **synthesised by `generate_sounds.py`** (maths, no copyright issues).
- `LoadingAnimation` is our own loading screen: 3 glowing arcs spinning at different speeds/directions, a pulsing logo, title, animated dots, fade-out, and an error mode.
- `SoundToggle` is the speaker icon (top-right) that mutes everything.

**Be ready to explain (viva)**
1. Why `AudioClip` and not `MediaPlayer`? *(AudioClip is for short effects – preloaded, low latency, can overlap)*
2. How does the animation spin around its centre? *(each arc is in a `Group` with an invisible full circle so the pivot is the circle's centre)*
3. Which JavaFX animation classes are used? *(`RotateTransition`, `ScaleTransition`, `FadeTransition`, `Timeline`)*
4. Why stop animations when hidden? *(saves CPU)*
5. How would you add a new sound? *(add to `generate_sounds.py`, run it, add an enum entry)*

**Your own improvements**
- Change the loader design (colours, number of rings, logo) so it is clearly *your* design.
- Generate a different startup sound / tune volumes in the `Sound` enum.
- Add a sound when the trailer starts playing.

---

## Weekly plan (Weeks 12 → 15)

| Week | Goal |
|------|------|
| **12** | Leader creates the repo + branches (see `GIT_GUIDE.md`). Everyone clones, runs the app, reads their files. First commits: small fixes/comments/own tweaks. |
| **13** | Each member improves & tests their portion on their own branch (several commits). Open Pull Requests; leader reviews and merges. |
| **14** | Integration testing together on one machine, bug fixes, check all 24 trailers, polish UI. Prepare viva answers. |
| **15** | Final merge to `main`, tag `v1.0`, rehearse the presentation (each member demos *their* part), submit. |
