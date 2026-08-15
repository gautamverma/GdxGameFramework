# GdxGameFramework — Developer Guide

A reusable multi-game framework for Android (and Desktop) built on [libGDX](https://libgdx.com). Each game is a self-contained module that plugs into the shared engine.

---

## Table of Contents

1. [Quick Start](#quick-start)
2. [Architecture Overview](#architecture-overview)
3. [Core Engine Classes](#core-engine-classes)
4. [Creating a New Game](#creating-a-new-game)
5. [Game Loop & Lifecycle](#game-loop--lifecycle)
6. [Rendering](#rendering)
7. [Input Handling](#input-handling)
8. [Physics (Box2D)](#physics-box2d)
9. [Camera & Scrolling](#camera--scrolling)
10. [Animation](#animation)
11. [Particles & Effects](#particles--effects)
12. [Audio](#audio)
13. [Asset Management](#asset-management)
14. [Saving & Loading](#saving--loading)
15. [Building & Running](#building--running)
16. [Project Structure](#project-structure)
17. [FAQ](#faq)

---

## Quick Start

### Build and run the demo game

```bash
cd GdxGameFramework

# Build Android APK
./gradlew :android:assembleDebug

# Install on device/emulator
./gradlew :android:installDebug
```

### Create a minimal game (5 lines)

```java
public class MyGame extends BaseGame {
    @Override
    public void init() {
        setScreen(new MyScreen(this));
    }
}
```

---

## Architecture Overview

```
┌──────────────────────────────────────────────────────┐
│  :android module (APK launcher)                       │
│  AndroidLauncher → picks which Game class to run      │
├──────────────────────────────────────────────────────┤
│  :games:spaceinvaders        :games:platformer        │
│  (extends BaseGame)          (extends BaseGame)       │
├──────────────────────────────────────────────────────┤
│  :core module (the reusable engine)                   │
│  ┌────────────┐ ┌──────────────┐ ┌──────────────┐   │
│  │  BaseGame  │ │  BaseScreen  │ │ SpriteEntity │   │
│  └────────────┘ └──────────────┘ └──────────────┘   │
│  ┌────────────────┐ ┌────────────┐ ┌────────────┐   │
│  │AnimatedEntity  │ │ Camera2D   │ │ParticlePool│   │
│  └────────────────┘ └────────────┘ └────────────┘   │
├──────────────────────────────────────────────────────┤
│  libGDX (com.badlogicgames.gdx)                       │
│  SpriteBatch, Texture, OrthographicCamera, Box2D...   │
└──────────────────────────────────────────────────────┘
```

**Key principle**: Game logic lives in `:games:*` modules. The `:core` engine handles rendering, input, physics wrappers, and utilities. The `:android` module is just a thin launcher.

---

## Core Engine Classes

### BaseGame

The root game class. Manages the SpriteBatch (shared renderer) and screen navigation.

```java
public abstract class BaseGame extends Game {
    public SpriteBatch batch;      // Shared renderer — DO NOT create new ones
    public BitmapFont font;        // Default font for debug/HUD text

    public static final float WORLD_WIDTH = 480;   // Virtual world width
    public static final float WORLD_HEIGHT = 800;  // Virtual world height

    public abstract void init();   // Override instead of create()
}
```

**Usage:**
```java
public class MyGame extends BaseGame {
    @Override
    public void init() {
        setScreen(new MenuScreen(this));
    }
}
```

---

### BaseScreen

A game screen (menu, gameplay, game over, etc.). Handles camera setup and the update/draw split.

```java
public abstract class BaseScreen implements Screen {
    protected BaseGame game;
    protected OrthographicCamera camera;
    protected Viewport viewport;

    public abstract void update(float delta);  // Game logic (input, physics, AI)
    public abstract void draw(float delta);    // Rendering (between batch.begin/end)
}
```

**The framework calls `render()` every frame, which:**
1. Clears the screen (black)
2. Updates the camera
3. Sets the batch projection matrix
4. Calls your `update(delta)`
5. Calls `batch.begin()`
6. Calls your `draw(delta)`
7. Calls `batch.end()`

You never need to call `batch.begin()` / `batch.end()` yourself in `draw()`.

---

### SpriteEntity

Base class for anything visible and interactive (players, enemies, bullets, pickups).

```java
public class SpriteEntity {
    public Vector2 position;       // World coordinates
    public Vector2 velocity;       // Pixels per second
    public float width, height;    // Bounding box
    public TextureRegion texture;  // Visual appearance
    public boolean active;         // Set false to remove

    public void update(float delta);           // Moves by velocity
    public void draw(SpriteBatch batch);       // Draws texture at position
    public Rectangle getBounds();              // For collision detection
    public boolean overlaps(SpriteEntity o);   // AABB collision check
}
```

**Usage:**
```java
SpriteEntity bullet = new SpriteEntity(x, y, 8, 16);
bullet.setTexture(bulletTexture);
bullet.velocity.y = 500;  // Moves up at 500 px/sec
```

---

### AnimatedEntity

Extends SpriteEntity with frame-based animation (walk cycles, explosions, etc.).

```java
AnimatedEntity player = new AnimatedEntity(x, y, 48, 48);
player.setAnimation(
    walkSpriteSheet,  // Texture with all frames in a grid
    6,                // columns in sprite sheet
    1,                // rows in sprite sheet
    0.1f              // seconds per frame
);
player.setLooping(true);
```

The animation plays automatically during `update()`. No manual frame management.

---

### Camera2D

Smooth-follow camera for scrolling games (platformers, shooters, etc.).

```java
Camera2D cam = new Camera2D(
    480, 800,     // View size (what player sees)
    4800, 800     // World size (total level)
);
cam.setLerpSpeed(5f);  // How quickly camera catches up (higher = snappier)

// In your update():
cam.follow(player.position, delta);

// In your draw() setup:
game.batch.setProjectionMatrix(cam.camera.combined);
```

**Features:**
- Smooth interpolation (no jarring jumps)
- Bounds clamping (camera won't show outside the world)
- Screen shake: `cam.shake(5f)` on explosions/impacts

---

### ParticlePool

Lightweight particle system for visual effects (no texture files needed).

```java
ParticlePool particles = new ParticlePool();

// Spawn burst on enemy death:
particles.spawn(
    enemy.position.x, enemy.position.y,  // Origin
    12,                                   // Particle count
    150f,                                 // Max speed
    0.5f                                  // Lifetime (seconds)
);

// In update():
particles.update(delta);

// In draw():
particles.draw(batch, sparkTexture);  // Uses any small texture as particle
```

---

## Creating a New Game

### Step 1: Create the module

```
games/
└── mygame/
    ├── build.gradle
    └── src/main/java/gautambverma/gdx/games/mygame/
        ├── MyGame.java
        └── MyGameScreen.java
```

**build.gradle:**
```groovy
plugins { id 'java-library' }
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
dependencies {
    implementation project(':core')
}
```

### Step 2: Register in settings.gradle

```groovy
include ':games:mygame'
```

### Step 3: Add dependency in android/build.gradle

```groovy
dependencies {
    implementation project(':games:mygame')
    // ... other deps
}
```

### Step 4: Point the launcher

In `AndroidLauncher.java`:
```java
initialize(new MyGame(), config);
```

### Step 5: Build

```bash
./gradlew :android:assembleDebug
```

---

## Game Loop & Lifecycle

```
┌─────────────────────────────────────────┐
│            libGDX Game Loop              │
│                                          │
│  create() ──→ [runs once at start]       │
│       ↓                                  │
│  ┌─→ render(delta) ──→ [60fps loop]     │
│  │        ↓                              │
│  │   screen.update(delta)                │
│  │   screen.draw(delta)                  │
│  │        ↓                              │
│  └────────┘                              │
│                                          │
│  dispose() ──→ [runs on exit]            │
└─────────────────────────────────────────┘
```

**Delta time**: `delta` is seconds since last frame (~0.016 at 60fps). Always multiply movement by `delta` for frame-rate independence:
```java
player.x += speed * delta;  // ✅ Correct
player.x += speed;          // ❌ Speed depends on frame rate
```

---

## Rendering

### SpriteBatch (the renderer)

All drawing goes through the shared `game.batch`:

```java
@Override
public void draw(float delta) {
    // batch.begin() already called by BaseScreen
    
    game.batch.draw(backgroundTexture, 0, 0);
    player.draw(game.batch);
    
    for (Enemy e : enemies) {
        e.draw(game.batch);
    }
    
    game.font.draw(game.batch, "Score: " + score, 10, WORLD_HEIGHT - 20);
    
    // batch.end() called automatically after
}
```

### Loading textures

```java
Texture playerTex = new Texture(Gdx.files.internal("player.png"));
// Place PNG files in android/src/main/assets/
```

### Texture Regions (sprite sheets)

```java
Texture sheet = new Texture("enemies.png");
TextureRegion[][] grid = TextureRegion.split(sheet, 32, 32);  // 32x32 per cell
TextureRegion enemy1 = grid[0][0];  // Top-left cell
TextureRegion enemy2 = grid[0][1];  // Next cell
```

---

## Input Handling

### Touch (most common for mobile)

```java
// Single tap detection
if (Gdx.input.justTouched()) {
    float worldX = Gdx.input.getX() * WORLD_WIDTH / Gdx.graphics.getWidth();
    float worldY = WORLD_HEIGHT - Gdx.input.getY() * WORLD_HEIGHT / Gdx.graphics.getHeight();
    // Note: libGDX Y is flipped (0 at bottom)
}

// Continuous touch (held down)
if (Gdx.input.isTouched()) {
    // Move toward finger
}

// Multi-touch
if (Gdx.input.isTouched(0)) { /* first finger */ }
if (Gdx.input.isTouched(1)) { /* second finger */ }
```

### Accelerometer

```java
float tiltX = Gdx.input.getAccelerometerX();  // -10 to +10
if (tiltX > 2f) player.moveLeft(delta);
if (tiltX < -2f) player.moveRight(delta);
```

### Gesture Detection (swipes, flings)

```java
// Add to your game's init:
GestureDetector gd = new GestureDetector(new GestureDetector.GestureAdapter() {
    @Override
    public boolean fling(float vx, float vy, int button) {
        if (Math.abs(vx) > Math.abs(vy)) {
            // Horizontal swipe
            if (vx > 0) onSwipeRight();
            else onSwipeLeft();
        } else {
            // Vertical swipe
            if (vy > 0) onSwipeDown();
            else onSwipeUp();
        }
        return true;
    }
});
Gdx.input.setInputProcessor(gd);
```

---

## Physics (Box2D)

For platformers, racing games, or anything with realistic physics.

```java
// Create physics world
World world = new World(new Vector2(0, -9.8f), true);

// Create a dynamic body (player)
BodyDef bodyDef = new BodyDef();
bodyDef.type = BodyDef.BodyType.DynamicBody;
bodyDef.position.set(playerX, playerY);
Body playerBody = world.createBody(bodyDef);

// Add shape
PolygonShape box = new PolygonShape();
box.setAsBox(halfWidth, halfHeight);
playerBody.createFixture(box, 1f);
box.dispose();

// In update():
world.step(1/60f, 6, 2);  // Step physics simulation
player.position.set(playerBody.getPosition());  // Sync graphics to physics

// Jump:
playerBody.applyLinearImpulse(0, 10f, playerBody.getWorldCenter().x, playerBody.getWorldCenter().y, true);
```

---

## Camera & Scrolling

### Side-scroller (Mario)

```java
Camera2D cam = new Camera2D(480, 800, levelWidth, levelHeight);

@Override
public void update(float delta) {
    // Camera follows player horizontally
    cam.follow(player.position, delta);
}

@Override
public void draw(float delta) {
    game.batch.setProjectionMatrix(cam.camera.combined);
    // Everything drawn in world coordinates — camera handles the view
    tileMap.draw(game.batch, cam);
    player.draw(game.batch);
}
```

### Vertical scroller (Space Invaders, endless runner)

```java
// Auto-scroll background
backgroundY -= scrollSpeed * delta;
if (backgroundY < -WORLD_HEIGHT) backgroundY = 0;
game.batch.draw(bgTex, 0, backgroundY);
game.batch.draw(bgTex, 0, backgroundY + WORLD_HEIGHT);
```

---

## Animation

### Sprite sheet animation

```java
// Load a 6-frame walk cycle (6 columns × 1 row)
AnimatedEntity player = new AnimatedEntity(100, 100, 48, 64);
Texture walkSheet = new Texture("player_walk.png");
player.setAnimation(walkSheet, 6, 1, 0.12f);  // 12fps animation
player.setLooping(true);

// In update:
player.update(delta);  // Advances animation automatically

// In draw:
player.draw(game.batch);
```

### One-shot animation (explosion)

```java
AnimatedEntity explosion = new AnimatedEntity(x, y, 48, 48);
explosion.setAnimation(explosionSheet, 8, 1, 0.05f);
explosion.setLooping(false);

// Remove after animation completes:
if (explosion.isAnimationFinished()) explosion.active = false;
```

---

## Particles & Effects

```java
ParticlePool sparks = new ParticlePool();

// On enemy death:
sparks.spawn(enemy.position.x, enemy.position.y, 15, 200f, 0.4f);

// On player jump:
sparks.spawn(player.position.x, player.position.y - 5, 5, 50f, 0.2f);

// Every frame:
sparks.update(delta);
sparks.draw(game.batch, particleTexture);
```

---

## Audio

```java
// Sound effects (short, preloaded)
Sound shootSfx = Gdx.audio.newSound(Gdx.files.internal("shoot.wav"));
shootSfx.play(0.5f);  // Volume 0-1

// Music (streaming, for background)
Music bgMusic = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
bgMusic.setLooping(true);
bgMusic.setVolume(0.3f);
bgMusic.play();
```

---

## Asset Management

For larger games, use AssetManager for async loading with progress:

```java
AssetManager assets = new AssetManager();

// Queue assets
assets.load("player.png", Texture.class);
assets.load("enemies.png", Texture.class);
assets.load("shoot.wav", Sound.class);

// In loading screen update():
if (assets.update()) {
    // All loaded! Switch to game screen
    Texture playerTex = assets.get("player.png");
}
float progress = assets.getProgress();  // 0.0 to 1.0
```

---

## Saving & Loading

```java
// Save
Preferences prefs = Gdx.app.getPreferences("MyGame");
prefs.putInteger("highScore", score);
prefs.putBoolean("soundOn", true);
prefs.flush();

// Load
int highScore = prefs.getInteger("highScore", 0);
boolean soundOn = prefs.getBoolean("soundOn", true);
```

---

## Building & Running

```bash
# Build Android APK
./gradlew :android:assembleDebug

# Install on connected device
./gradlew :android:installDebug

# Build only the core engine (check for compile errors fast)
./gradlew :core:build

# Clean everything
./gradlew clean
```

### Adding a Desktop module (recommended for fast iteration)

Add to `settings.gradle`:
```groovy
include ':desktop'
```

Create `desktop/build.gradle`:
```groovy
plugins { id 'java' }
dependencies {
    implementation project(':core')
    implementation project(':games:spaceinvaders')
    implementation "com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion"
    implementation "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop"
    implementation "com.badlogicgames.gdx:gdx-box2d-platform:$box2dVersion:natives-desktop"
}
mainClassName = 'gautambverma.gdx.desktop.DesktopLauncher'
```

Create `desktop/src/main/java/gautambverma/gdx/desktop/DesktopLauncher.java`:
```java
public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(480, 800);
        config.setTitle("My Game");
        new Lwjgl3Application(new SpaceInvadersGdx(), config);
    }
}
```

Then run: `./gradlew :desktop:run` — instant preview, no emulator!

---

## Project Structure

```
GdxGameFramework/
├── core/                             # Shared engine (Java library)
│   └── src/main/java/gautambverma/gdx/
│       ├── engine/
│       │   ├── BaseGame.java         # Game lifecycle + SpriteBatch
│       │   ├── BaseScreen.java       # Screen with camera/viewport
│       │   ├── SpriteEntity.java     # Drawable entity with physics
│       │   ├── AnimatedEntity.java   # Animated sprite entity
│       │   ├── Camera2D.java         # Follow camera with shake
│       │   └── ParticlePool.java     # Particle effects
│       ├── screens/
│       │   └── MenuScreen.java       # Reusable menu template
│       └── utils/
│           ├── GameConfig.java       # Shared constants
│           └── CollisionHelper.java  # Collision utilities
├── games/
│   ├── spaceinvaders/                # Game module 1
│   │   └── src/main/java/.../
│   │       ├── SpaceInvadersGdx.java
│   │       └── SIGameScreen.java
│   └── platformer/                   # Game module 2 (template)
│       └── src/main/java/.../
│           └── PlatformerGame.java
├── android/                          # Android APK launcher
│   ├── src/main/
│   │   ├── java/.../AndroidLauncher.java
│   │   ├── AndroidManifest.xml
│   │   ├── assets/                   # Game assets (PNG, WAV, maps)
│   │   └── res/
│   └── build.gradle
├── build.gradle                      # Root (declares AGP + repos)
├── settings.gradle                   # Module registry
└── gradle.properties                 # JVM args, AndroidX flags
```

---

## FAQ

**Q: How do I switch which game the APK launches?**
A: Change the class in `AndroidLauncher.java`:
```java
initialize(new PlatformerGame(), config);  // ← change this
```

**Q: Where do I put image/sound files?**
A: In `android/src/main/assets/`. Access with `Gdx.files.internal("filename.png")`.

**Q: Why is Y=0 at the bottom?**
A: libGDX uses OpenGL coordinates (Y-up). If you prefer Y-down (like Android Canvas), flip the camera:
```java
camera.setToOrtho(true, width, height);  // true = y-down
```

**Q: How do I handle different screen sizes?**
A: The `FitViewport` in BaseScreen handles this automatically — it letterboxes to maintain aspect ratio. Your game always sees 480×800 virtual units.

**Q: Can I use this for iOS/Web?**
A: Yes! libGDX supports iOS (via RoboVM) and HTML5 (via GWT). Add `:ios` and `:html` modules. The `:core` and `:games:*` modules work unchanged.

**Q: What's the performance like?**
A: libGDX uses OpenGL ES — it can handle thousands of sprites at 60fps on modern Android. SpriteBatch automatically batches draw calls for efficiency.

---

## Next Steps

1. **Add a Desktop module** for fast testing (no emulator needed)
2. **Download free sprites** from [kenney.nl/assets](https://kenney.nl/assets) or [itch.io](https://itch.io/game-assets/free)
3. **Build the platformer** using Camera2D + Box2D + TileMap
4. **Add a game selector** screen that lets the user pick which game to play

---

*Built with libGDX 1.12.1 • Box2D included • Java 11 • Android API 21+*
