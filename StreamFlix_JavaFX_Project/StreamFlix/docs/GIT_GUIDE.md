# Git workflow for the group (rubric row 10 = 10 marks, row 11 = 15 marks)

The lecturers will look at the **commit history** to check that *every* member contributed: frequent, meaningful
commits, proper branching, a merge history and clear messages. So work on your own branch, commit small and often,
and merge through Pull Requests.

> Honest tip: marks go to real work. Commit as you actually read, test, fix and improve your portion
> (see "Your own improvements" in `PORTIONS.md`). Be ready to explain every commit with your name on it.

## 0. One-time set-up (leader)

```bash
cd StreamFlix
git init -b main
git add .
git commit -m "Initial project skeleton: Maven setup, README and docs"
# create an empty repo on GitHub/GitLab, then:
git remote add origin <URL>
git push -u origin main
```
Invite the four other members as collaborators (Settings → Collaborators). Optional but recommended:
protect `main` so changes arrive only through Pull Requests.

## 1. Everyone else: clone and make your branch

```bash
git clone <URL>
cd StreamFlix
git config user.name  "Your Real Name"          # must match your name – this is what the lecturer sees
git config user.email "your@email"              # the email linked to YOUR GitHub account
git checkout -b feature/<your-portion>
```

| Member | Branch name |
|--------|-------------|
| Retselisitsoe Moeketsi | `feature/app-shell` |
| Rorisang Konyana | `feature/data-and-rows` |
| Moeti Leleka | `feature/custom-ui` |
| Pofane Matlali | `feature/video-player` |
| Lethako Khabele | `feature/audio-and-loader` |

## 2. Daily loop

```bash
git pull origin main                 # get the others' merged work first
# ... edit / test your files ...
git add src/main/java/com/streamflix/ui/MovieCard.java
git commit -m "MovieCard: add red glow and lift effect on hover"
git push -u origin feature/custom-ui
```

**Commit rules**
- Small: one idea per commit. Several commits per week, not one on the last night.
- Only add *your own* files unless you are fixing something in someone else's (tell them).
- Messages in the present tense, starting with the area: `CategoryRow: stop scrolling past the last card`.
  Bad: `update`, `fix`, `final final 2`.

## 3. Pull Requests and merging

1. Push your branch, open a **Pull Request** → base `main`.
2. Title = what changed; description = how to test it. Ask **one other member** to review.
3. Leader merges with **"Create a merge commit"** (keeps the merge history the rubric asks for).
4. Everyone: `git checkout main && git pull`, then `git checkout feature/<yours> && git merge main`.

### If you get a merge conflict
```bash
git merge main                       # Git reports CONFLICT in some file
# open the file, find <<<<<<< ======= >>>>>>>, keep the right lines, delete the markers
git add <file>
git commit -m "Merge main into feature/custom-ui, resolve conflict in MovieCard"
```
Because each member owns different files, conflicts should be rare. The usual hot spots are `StreamFlixApp.java` and `movies.csv`.

## 4. Suggested commit ideas per member (use your own wording!)

**Retselisitsoe** – `Theme: adjust accent colours` · `TopBar: add hover underline on nav items` · `HomeView: smooth scroll to category` · `StreamFlixApp: ESC and SPACE shortcuts` · `docs: add user manual` · merge commits for everyone's PRs

**Rorisang** – `MovieRepository: skip comment lines and report bad rows` · `movies.csv: verify all Action trailers` · `movies.csv: verify Sci-Fi trailers` · `movies.csv: verify Animation trailers` · `CategoryRow: show "1 - 5 of 8" counter` · `CategoryRow: horizontal wheel support`

**Moeti** – `CustomButton: hover and press animation` · `Icons: add pause and speaker icons` · `MovieCard: thumbnail crop to 16:9` · `MovieCard: hover overlay with play icon` · `HeroBanner: Ken Burns zoom` · `CustomButton: disabled state`

**Pofane** – `LocalPlayerServer: serve embed page on localhost` · `PlayerView: play/pause through postMessage` · `PlayerView: bridge for state and errors` · `PlayerView: automatic backup trailer on error` · `PlayerView: mute and restart controls` · `PlayerView: open on YouTube button`

**Lethako** – `tools: script that synthesises sound effects` · `SoundManager: load clips and mute` · `SoundManager: throttle hover sound` · `LoadingAnimation: spinning arcs` · `LoadingAnimation: pulsing logo and dots` · `LoadingAnimation: error state` · `SoundToggle: speaker icon switch`

## 5. Before you submit

- [ ] `git log --graph --oneline --all` shows commits from **all five** names and merge commits
- [ ] `main` runs cleanly from a fresh clone: `mvn javafx:run`
- [ ] Tag the final version: `git tag v1.0 && git push --tags`
- [ ] Everyone can explain their files (see viva questions in `PORTIONS.md`)
