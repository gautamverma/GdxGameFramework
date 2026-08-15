package gautambverma.gdx.games.chainreaction;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import gautambverma.gdx.engine.BaseGame;
import gautambverma.gdx.engine.BaseScreen;
import java.util.List;

/**
 * Main gameplay — tap once to start a chain reaction.
 */
public class CRGameScreen extends BaseScreen {
    private Level level;
    private List<Orb> orbs;
    private ShapeRenderer shapes;
    
    private boolean tapped = false;      // Has player used their tap?
    private boolean chainComplete = false;
    private float completeTimer = 0;
    private int triggeredCount = 0;
    private int comboMultiplier = 1;
    private float lastTriggerTime = 0;
    
    // Score
    private int score = 0;

    public CRGameScreen(BaseGame game, int levelNumber) {
        super(game);
        this.level = new Level(levelNumber);
        this.orbs = level.generateOrbs(BaseGame.WORLD_WIDTH, BaseGame.WORLD_HEIGHT - 80);
        this.shapes = new ShapeRenderer();
    }

    @Override
    public void update(float delta) {
        // Handle tap (one tap per level)
        if (!tapped && Gdx.input.justTouched()) {
            float touchX = Gdx.input.getX() * BaseGame.WORLD_WIDTH / Gdx.graphics.getWidth();
            float touchY = BaseGame.WORLD_HEIGHT - Gdx.input.getY() * BaseGame.WORLD_HEIGHT / Gdx.graphics.getHeight();
            
            // Find closest orb to tap, or create explosion at tap point
            Orb closest = null;
            float closestDist = Float.MAX_VALUE;
            for (Orb orb : orbs) {
                float dist = orb.position.dst(touchX, touchY);
                if (dist < orb.radius + 20f && dist < closestDist) { // 20px grace area
                    closest = orb;
                    closestDist = dist;
                }
            }
            
            if (closest != null) {
                closest.trigger();
                tapped = true;
                triggeredCount = 1;
                lastTriggerTime = 0;
            }
        }
        
        // Update orbs
        boolean anyActive = false;
        for (Orb orb : orbs) {
            orb.update(delta, BaseGame.WORLD_WIDTH, BaseGame.WORLD_HEIGHT - 80);
            
            if (orb.state == Orb.OrbState.EXPLODING || orb.state == Orb.OrbState.EXPANDED) {
                anyActive = true;
            }
        }
        
        // Chain reaction — exploding/expanded orbs trigger floating ones
        for (Orb source : orbs) {
            if (source.state == Orb.OrbState.EXPLODING || source.state == Orb.OrbState.EXPANDED) {
                for (Orb target : orbs) {
                    if (source.canTrigger(target)) {
                        target.trigger();
                        triggeredCount++;
                        lastTriggerTime = 0;
                        
                        // Combo: rapid triggers increase multiplier
                        comboMultiplier = Math.min(comboMultiplier + 1, 8);
                        score += 10 * comboMultiplier;
                    }
                }
            }
        }
        
        // Combo decay
        lastTriggerTime += delta;
        if (lastTriggerTime > 0.5f) {
            comboMultiplier = 1;
        }
        
        // Check if chain is complete (all explosions done)
        if (tapped && !anyActive && !chainComplete) {
            boolean allSettled = true;
            for (Orb orb : orbs) {
                if (orb.state == Orb.OrbState.EXPLODING || orb.state == Orb.OrbState.EXPANDED || orb.state == Orb.OrbState.FADING) {
                    allSettled = false;
                    break;
                }
            }
            if (allSettled) {
                chainComplete = true;
                completeTimer = 0;
            }
        }
        
        // Show results after delay
        if (chainComplete) {
            completeTimer += delta;
            if (completeTimer > 2.0f && Gdx.input.justTouched()) {
                int stars = level.getStars(triggeredCount);
                if (stars > 0) {
                    // Advance to next level
                    game.setScreen(new CRResultScreen((BaseGame) game, level.levelNumber, triggeredCount, level.totalOrbs, stars, score));
                } else {
                    // Retry
                    game.setScreen(new CRResultScreen((BaseGame) game, level.levelNumber, triggeredCount, level.totalOrbs, 0, score));
                }
            }
        }
    }

    @Override
    public void draw(float delta) {
        // We need ShapeRenderer for circles (outside of SpriteBatch)
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.08f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        camera.update();
        update(delta);
        
        // Enable blending for transparency
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        shapes.setProjectionMatrix(camera.combined);
        
        // Draw orbs
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Orb orb : orbs) {
            if (orb.state == Orb.OrbState.DEAD) continue;
            
            float alpha = orb.getAlpha();
            
            if (orb.state == Orb.OrbState.FLOATING) {
                // Floating orb — solid with glow
                shapes.setColor(orb.color.r * 0.3f, orb.color.g * 0.3f, orb.color.b * 0.3f, alpha * 0.4f);
                shapes.circle(orb.position.x, orb.position.y, orb.radius + 4, 24);
                shapes.setColor(orb.color.r, orb.color.g, orb.color.b, alpha);
                shapes.circle(orb.position.x, orb.position.y, orb.radius, 20);
                // Highlight
                shapes.setColor(1, 1, 1, alpha * 0.4f);
                shapes.circle(orb.position.x - orb.radius * 0.3f, orb.position.y + orb.radius * 0.3f, orb.radius * 0.3f, 12);
            } else {
                // Exploding/expanded — large ring
                float r = orb.currentExplosionRadius;
                shapes.setColor(orb.color.r, orb.color.g, orb.color.b, alpha * 0.2f);
                shapes.circle(orb.position.x, orb.position.y, r, 32);
                shapes.setColor(orb.color.r, orb.color.g, orb.color.b, alpha * 0.6f);
                shapes.circle(orb.position.x, orb.position.y, r * 0.7f, 28);
                shapes.setColor(1, 1, 1, alpha * 0.8f);
                shapes.circle(orb.position.x, orb.position.y, r * 0.2f, 16);
            }
        }
        shapes.end();
        
        // HUD (using SpriteBatch for text)
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        
        // Level info
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Level " + level.levelNumber, 10, BaseGame.WORLD_HEIGHT - 10);
        
        // Progress
        String progress = triggeredCount + " / " + level.requiredOrbs + " needed";
        game.font.draw(game.batch, progress, BaseGame.WORLD_WIDTH - 220, BaseGame.WORLD_HEIGHT - 10);
        
        // Combo
        if (comboMultiplier > 1) {
            game.font.setColor(Color.YELLOW);
            game.font.draw(game.batch, "x" + comboMultiplier, BaseGame.WORLD_WIDTH / 2 - 20, BaseGame.WORLD_HEIGHT / 2);
        }
        
        // Tap instruction
        if (!tapped) {
            game.font.setColor(1, 1, 1, 0.6f);
            game.font.draw(game.batch, "Tap an orb to start the chain!", 80, 50);
        }
        
        // Result overlay
        if (chainComplete && completeTimer > 0.5f) {
            int stars = level.getStars(triggeredCount);
            game.font.setColor(stars > 0 ? Color.GREEN : Color.RED);
            String result = stars > 0 ? 
                "CHAIN COMPLETE! " + triggeredCount + "/" + level.totalOrbs :
                "NOT ENOUGH... " + triggeredCount + "/" + level.requiredOrbs + " needed";
            game.font.draw(game.batch, result, 40, BaseGame.WORLD_HEIGHT / 2 + 60);
            
            if (completeTimer > 2.0f) {
                game.font.setColor(Color.WHITE);
                game.font.draw(game.batch, "Tap to continue", 140, BaseGame.WORLD_HEIGHT / 2 - 20);
            }
        }
        
        game.font.setColor(Color.WHITE);
        game.batch.end();
    }

    @Override
    public void dispose() {
        shapes.dispose();
    }
}
