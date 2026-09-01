<p align="center">
  <img src="assets/Hollow%20Knight%20sprites/Menu/vheart_title.png" alt="Hollow Knight: Voidheart Edition" width="720">
</p>

<p align="center">
  <strong>A desktop Hollow Knight recreation in Java</strong><br>
  <em>Explore the Forgotten Crossroads, master Nail and Soul, and face the False Knight.</em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-11%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 11+">
  <img src="https://img.shields.io/badge/libGDX-1.14.2-60a917?style=for-the-badge&logo=libgdx&logoColor=white" alt="libGDX">
  <img src="https://img.shields.io/badge/Gradle-Wrapper-02303A?style=for-the-badge&logo=gradle&logoColor=white" alt="Gradle">
  <img src="https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-111111?style=for-the-badge" alt="Desktop">
  <img src="https://img.shields.io/badge/Release-v1.0.0-6e7b91?style=for-the-badge" alt="v1.0.0">
</p>

<p align="center">
  <a href="#play-the-game">Play</a> ·
  <a href="#features">Features</a> ·
  <a href="#controls">Controls</a> ·
  <a href="#build-from-source">Build</a> ·
  <a href="#project-structure">Structure</a>
</p>

---

A fan-made action-adventure built with **libGDX** and **LWJGL3**. The Knight moves through a hand-authored Tiled world, fights Hallownest creatures, equips Charms, and records progress across save slots.

> **Fan project.** This is an unofficial recreation. Hollow Knight, its characters, art, music, and names belong to [Team Cherry](https://www.teamcherry.com.au/). This repository is not affiliated with or endorsed by Team Cherry.

---

## Play the game

The runnable desktop build is a **fat JAR**: game code, natives, and every asset the game loads are packed inside a single file.

1. Install **Java 11 or newer** ([Adoptium Temurin](https://adoptium.net/) is a good choice).
2. Download `hollowKnight-1.0.0.jar` from [Releases](https://github.com/Amir-Ehsani/hollow-knight/releases).
3. Double-click the JAR, or run:

```bash
java -jar hollowKnight-1.0.0.jar
```

The window opens in fullscreen at 60 FPS. Press **Esc** in-game to pause.

A local copy of the same build is produced at `release/hollowKnight-1.0.0.jar` after `gradlew lwjgl3:jar`.

---

## Features

| | |
| :--- | :--- |
| **The Knight** | Walk, jump, double jump, dash, Nail slash, upward slash, and aerial pogo. |
| **Soul arts** | Fill the Soul vessel with Nail hits, then Focus to heal or cast **Vengeful Spirit** and **Howling Wraiths**. |
| **Charms** | Collect and equip up to three notches: Soul Catcher, Dashmaster, Unbreakable Strength, Quick Slash, Quick Focus, Heavy Blow, Sharp Shadow, Void Heart. |
| **World** | Forgotten Crossroads and Crystal Peak, built in Tiled, with hazards, breakable walls, benches, and a sealed False Knight arena. |
| **Enemies** | Crawlid, Husk Hornhead, Mosquito, Crystal Crawler, Crystallized husks, and the **False Knight** boss. |
| **Zote** | Meet Zote the Mighty, hear his boasts, and fight through the cavern that bears his name. |
| **Progression** | Three save slots, achievements, inventory, settings (music, SFX, brightness), and an in-game guide. |

<p align="center">
  <img src="assets/bg/Forgotten%20Crossroads.png" alt="Forgotten Crossroads" width="420">
  &nbsp;
  <img src="assets/bg/Crystal%20Peak.png" alt="Crystal Peak" width="420">
</p>

<p align="center">
  <sub>Forgotten Crossroads · Crystal Peak</sub>
</p>

---

## Controls

| Action | Keys |
| :--- | :--- |
| Move | `A` `D` or `←` `→` |
| Look / aim | `W` `S` or `↑` `↓` |
| Jump / double jump | `Z` / `Space` / `K` |
| Dash | `C` / `Left Shift` |
| Nail attack | `X` / `J` |
| Focus heal | `H` |
| Vengeful Spirit | `Q` |
| Howling Wraiths | `R` |
| Interact / dialogue | `E` / `Enter` |
| Inventory / Charms | `I` |
| Pause | `Esc` |

Hold **up** with the Nail for an upward slash. Hold **down** in the air for a pogo strike.

---

## Charms

Every charm costs **1 notch**. You have **3 notches**.

| Charm | Effect |
| :--- | :--- |
| Soul Catcher | Nail hits restore more Soul |
| Dashmaster | Shorter dash cooldown |
| Unbreakable Strength | Stronger Nail damage |
| Quick Slash | Faster Nail attacks |
| Quick Focus | Faster healing |
| Heavy Blow | Stronger knockback |
| Sharp Shadow | Longer dash that damages enemies and ignores contact |
| Void Heart | Spells deal 50% more damage with a void visual |

---

## Achievements

| Achievement | How to earn it |
| :--- | :--- |
| Completion | Defeat the final boss |
| Speedrun | Finish in 15 minutes or less |
| True Hunter | Defeat every enemy type |
| Defeat False Knight | Win the False Knight fight |
| Soul Master | Cast both Vengeful Spirit and Howling Wraiths |

---

## Build from source

**Requirements**

- JDK 11 or newer on `PATH`
- Git

```bash
git clone https://github.com/Amir-Ehsani/hollow-knight.git
cd hollow-knight

# Windows
gradlew.bat lwjgl3:run

# macOS / Linux
./gradlew lwjgl3:run
```

Create the release JAR (includes sprites, maps, audio, UI, and the menu video):

```bash
# Windows
gradlew.bat lwjgl3:jar

# macOS / Linux
./gradlew lwjgl3:jar
```

Output:

```
lwjgl3/build/libs/hollowKnight-1.0.0.jar
release/hollowKnight-1.0.0.jar
```

Useful tasks:

| Task | What it does |
| :--- | :--- |
| `lwjgl3:run` | Launch the desktop game |
| `lwjgl3:jar` | Fat JAR with code, natives, and in-game assets |
| `clean` | Delete build outputs |

---

## Project structure

```
hollowKnight-proj/
├── assets/          Game art, maps, audio, UI, particles
├── core/            Shared game logic (model, view, controller)
├── lwjgl3/          Desktop launcher (LWJGL3)
├── release/         Runnable JAR produced by lwjgl3:jar
└── README.md
```

The `core` module holds the Knight, enemies, spells, charms, HUD, and screens.  
The `lwjgl3` module starts the window and packages a cross-platform JAR.

---

## Tech stack

- **Java 11** language level
- **[libGDX](https://libgdx.com/) 1.14.2** for rendering, input, and audio
- **LWJGL3** desktop backend
- **Tiled** maps (`.tmx`)
- **gdx-video** for the Voidheart menu background
- **SQLite** for local save slots
- **Gradle Wrapper** so no global Gradle install is required

---

## Credits

- **Amir Ehsani** — design and programming · [Portfolio](https://amir-ehsani.xyz/)
- **Team Cherry** — Hollow Knight, the original world, characters, and audio-visual identity
- **libGDX** and **LWJGL** — the desktop framework this project is built on
- **Tiled** — map editor used for the Crossroads and Crystal Peak layouts

This project is a student / fan recreation for learning and demonstration. Please support the original game: [hollowknight.com](https://www.hollowknight.com/)
