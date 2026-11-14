# StreamFlix — JavaFX Movie Streaming Demo

Group assignment: *Movie/Video Streaming Demo App* (BSc Software Engineering & Multimedia, Semester 1, 40 %).
A Netflix-style demo built with **JavaFX**: three genre rows, hover effects, trailers streamed from YouTube inside
the app, custom buttons, a custom loading animation and sound effects.

**Team:** Retselisitsoe Moeketsi (leader) · Rorisang Konyana · Moeti Leleka · Pofane Matlali · Lethako Khabele
→ who owns which files: `docs/PORTIONS.md` · Git workflow: `docs/GIT_GUIDE.md`

## Requirements
- **JDK 17 or newer** (21 recommended) — `java -version`
- **Maven 3.8+** — `mvn -version` (or open the folder in IntelliJ / NetBeans / Eclipse as a Maven project)
- **Internet** (thumbnails and trailers come from YouTube; nothing video-related is stored in the project)
- JavaFX is downloaded automatically by Maven (no manual SDK needed)

## Run
```bash
mvn javafx:run          # or:  ./run.sh   /   run.bat
```
In an IDE: open `pom.xml` as a project, then run **`com.streamflix.Launcher`** (not `StreamFlixApp`) to avoid the
"JavaFX runtime components are missing" error.

## Using the app
| Action | Result |
|--------|--------|
| Hover a thumbnail | card grows + glows, play icon fades in, tick sound |
| Click a thumbnail / title / hero "Play trailer" | loading animation → trailer plays inside the app |
| `‹ ›` arrows, horizontal swipe, **Shift + wheel** on a row | slide to the other movies (5 visible at a time) |
| Top-bar links | smooth scroll to Home / Action / Sci-Fi / Animation |
| Player: Play/Pause · Restart · Mute · Backup trailer · Open on YouTube | custom controls |
| **SPACE** / **ESC** in the player | play-pause / go back |
| Speaker icon (top right) | sound effects on/off |

## Requirement checklist
| Requirement | Where |
|-------------|-------|
| ≥ 3 categories, ≥ 8 videos each, clearly labelled | `movies.csv` (Action, Sci-Fi, Animation × 8), `CategoryRow` header |
| Thumbnails + title | `MovieCard` |
| Hover effect | `MovieCard.animateHover` |
| Only 5 thumbnails visible, scroll for the rest | `CategoryRow` (clipped viewport + animated strip) |
| Click → YouTube trailer played in the app, nothing stored | `PlayerView` + `LocalPlayerServer` (WebView, YouTube embed) |
| Play / pause | `PlayerView.togglePlayPause` |
| Custom loading animation | `LoadingAnimation` |
| **No `Button` class** | `CustomButton` (StackPane + Label); nav = `Label`; arrows/toggle = `StackPane` |
| Sound effects | `SoundManager`, `resources/sounds` |
| OOP / modular / commented | packages `model`, `data`, `ui`, `player`, `audio`, `util` |

## Project layout
```
StreamFlix/
├─ pom.xml
├─ run.sh / run.bat
├─ docs/            PORTIONS.md, GIT_GUIDE.md
├─ tools/           generate_sounds.py
└─ src/main
   ├─ java/com/streamflix
   │   ├─ StreamFlixApp.java, Launcher.java
   │   ├─ model/   Movie, Category
   │   ├─ data/    MovieRepository
   │   ├─ ui/      TopBar, HomeView, HeroBanner, CategoryRow, MovieCard,
   │   │           CustomButton, Icons, SoundToggle, LoadingAnimation
   │   ├─ player/  PlayerView, LocalPlayerServer
   │   ├─ audio/   SoundManager
   │   └─ util/    Theme
   └─ resources/   data/movies.csv, css/style.css, sounds/*.wav
```

## Troubleshooting
- **A trailer says it cannot be played in the app** – the video owner disabled embedding or removed it. The app first
  tries the *backup* ID automatically. To fix permanently, open YouTube, copy the 11 characters after `watch?v=`
  and replace the IDs in `src/main/resources/data/movies.csv`. "Open on YouTube" always works as a fallback.
- **Black screen / "Error 153" in the player** – make sure you run via Maven or the Launcher (the local page
  server must start), and that antivirus/firewall is not blocking Java on `127.0.0.1`.
- **Thumbnails stay grey** – no internet, or a wrong ID in `movies.csv` (the grey placeholder shows the title).
- **No sound** – check the speaker icon is on and the PC volume is up. The app still works silently.
- **Window too big for a small screen** – the page scrolls vertically; maximise the window or lower the Windows display scaling.
- WebView uses the JavaFX WebKit engine, which is older than Chrome; if YouTube ever changes its embed behaviour,
  the "Open on YouTube" button keeps the demo usable.

## Credits
Movie titles/trailers/thumbnails © their owners, streamed from YouTube for educational demo purposes.
Sound effects are synthesised by `tools/generate_sounds.py` (no third-party audio).
