package gautambverma.gdx.games.spaceinvaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import gautambverma.gdx.engine.BaseGame;
import gautambverma.gdx.engine.BaseScreen;
import gautambverma.gdx.engine.SpriteEntity;
import gautambverma.gdx.engine.ParticlePool;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Main gameplay screen for Space Invaders.
 */
public class SIGameScreen extends BaseScreen {
    // Entities
    SpriteEntity player;
    List<SpriteEntity> enemies = new ArrayList<>();
    List<SpriteEntity> playerBullets = new ArrayList<>();
    List<SpriteEntity> enemyBullets = new ArrayList<>();
    ParticlePool explosions = new ParticlePool();
    SpriteEntity explosionTemplate;
    
    // Textures
    Texture playerTex, enemyTex1, enemyTex2, bulletTex, enemyBulletTex, bgTex;
    
    // Game state
    int score = 0;
    int lives = 3;
    float enemyDir = 1;
    float enemySpeed = 60;
    float shootTimer = 0;
    boolean gameOver = false;

    public SIGameScreen(BaseGame game) {
        super(game);
        // For now use colored rectangles (textures loaded from assets in real version)
        createPlaceholderTextures();
        spawnEnemies();
        
        player = new SpriteEntity(BaseGame.WORLD_WIDTH / 2 - 20, 40, 40, 30);
        explosionTemplate = new SpriteEntity(0, 0, 4, 4);
    }

    void createPlaceholderTextures() {
        // In a real game, load from assets: new Texture(Gdx.files.internal("player.png"))
        // Using 1x1 white pixel textures tinted at draw time for the demo
        playerTex = createPixel(0, 200, 255);
        enemyTex1 = createPixel(50, 255, 50);
        enemyTex2 = createPixel(255, 100, 100);
        bulletTex = createPixel(255, 255, 100);
        enemyBulletTex = createPixel(255, 80, 80);
    }

    Texture createPixel(int r, int g, int b) {
        com.badlogic.gdx.graphics.Pixmap pm = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pm.setColor(r/255f, g/255f, b/255f, 1f);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    void spawnEnemies() {
        enemies.clear();
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 8; col++) {
                SpriteEntity e = new SpriteEntity(60 + col * 48, 550 + row * 40, 30, 24);
                e.setTexture(row < 2 ? enemyTex2 : enemyTex1);
                enemies.add(e);
            }
        }
    }

    @Override
    public void update(float delta) {
        if (gameOver) {
            if (Gdx.input.justTouched()) {
                game.setScreen(new SIGameScreen((BaseGame) game));
            }
            return;
        }

        // Player movement (touch)
        if (Gdx.input.isTouched()) {
            float touchX = Gdx.input.getX() * BaseGame.WORLD_WIDTH / Gdx.graphics.getWidth();
            if (touchX < player.position.x) player.position.x -= 300 * delta;
            if (touchX > player.position.x + player.width) player.position.x += 300 * delta;
        }
        player.position.x = MathUtils.clamp(player.position.x, 0, BaseGame.WORLD_WIDTH - player.width);

        // Shoot on tap
        if (Gdx.input.justTouched() && playerBullets.size() < 3) {
            SpriteEntity b = new SpriteEntity(player.position.x + 18, player.position.y + 30, 4, 12);
            b.velocity.y = 500;
            b.setTexture(bulletTex);
            playerBullets.add(b);
        }

        // Update enemies
        boolean hitEdge = false;
        for (SpriteEntity e : enemies) {
            e.position.x += enemyDir * enemySpeed * delta;
            if (e.position.x <= 0 || e.position.x + e.width >= BaseGame.WORLD_WIDTH) hitEdge = true;
        }
        if (hitEdge) {
            enemyDir *= -1;
            for (SpriteEntity e : enemies) e.position.y -= 15;
        }

        // Enemy shooting
        shootTimer += delta;
        if (shootTimer > 1.2f && !enemies.isEmpty()) {
            shootTimer = 0;
            SpriteEntity shooter = enemies.get(MathUtils.random(enemies.size() - 1));
            SpriteEntity b = new SpriteEntity(shooter.position.x + 12, shooter.position.y - 10, 4, 12);
            b.velocity.y = -200;
            b.setTexture(enemyBulletTex);
            enemyBullets.add(b);
        }

        // Update bullets
        for (SpriteEntity b : playerBullets) b.update(delta);
        for (SpriteEntity b : enemyBullets) b.update(delta);
        explosions.update(delta);

        // Collision: player bullets vs enemies
        Iterator<SpriteEntity> bit = playerBullets.iterator();
        while (bit.hasNext()) {
            SpriteEntity b = bit.next();
            if (b.position.y > BaseGame.WORLD_HEIGHT) { bit.remove(); continue; }
            Iterator<SpriteEntity> eit = enemies.iterator();
            while (eit.hasNext()) {
                SpriteEntity e = eit.next();
                if (b.overlaps(e)) {
                    bit.remove();
                    eit.remove();
                    score += 10;
                    enemySpeed += 1;
                    explosions.spawn(e.position.x + e.width/2, e.position.y + e.height/2, 8, 100, 0.4f);
                    break;
                }
            }
        }

        // Collision: enemy bullets vs player
        Iterator<SpriteEntity> ebit = enemyBullets.iterator();
        while (ebit.hasNext()) {
            SpriteEntity b = ebit.next();
            if (b.position.y < 0) { ebit.remove(); continue; }
            if (b.overlaps(player)) {
                ebit.remove();
                lives--;
                if (lives <= 0) gameOver = true;
                break;
            }
        }

        // Check enemies reached bottom
        for (SpriteEntity e : enemies) {
            if (e.position.y <= player.position.y + 30) { gameOver = true; break; }
        }

        // Next wave
        if (enemies.isEmpty()) {
            enemySpeed += 20;
            spawnEnemies();
        }
    }

    @Override
    public void draw(float delta) {
        // Draw player
        game.batch.draw(playerTex, player.position.x, player.position.y, player.width, player.height);

        // Draw enemies
        for (SpriteEntity e : enemies) e.draw(game.batch);
        
        // Draw bullets
        for (SpriteEntity b : playerBullets) b.draw(game.batch);
        for (SpriteEntity b : enemyBullets) b.draw(game.batch);
        
        // Draw particles
        if (explosionTemplate.texture == null) explosionTemplate.setTexture(bulletTex);
        explosions.draw(game.batch, explosionTemplate);

        // HUD
        game.font.draw(game.batch, "SCORE: " + score, 10, BaseGame.WORLD_HEIGHT - 10);
        game.font.draw(game.batch, "LIVES: " + lives, BaseGame.WORLD_WIDTH - 150, BaseGame.WORLD_HEIGHT - 10);

        if (gameOver) {
            game.font.draw(game.batch, "GAME OVER", BaseGame.WORLD_WIDTH / 2 - 80, BaseGame.WORLD_HEIGHT / 2);
            game.font.draw(game.batch, "Tap to restart", BaseGame.WORLD_WIDTH / 2 - 100, BaseGame.WORLD_HEIGHT / 2 - 40);
        }
    }

    @Override
    public void dispose() {
        playerTex.dispose();
        enemyTex1.dispose();
        enemyTex2.dispose();
        bulletTex.dispose();
        enemyBulletTex.dispose();
    }
}
