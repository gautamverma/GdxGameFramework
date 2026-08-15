package gautambverma.gdx.games.chainreaction;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import gautambverma.gdx.engine.BaseGame;
import gautambverma.gdx.engine.BaseScreen;

/**
 * Results screen — shows stars earned and options to retry/continue.
 */
public class CRResultScreen extends BaseScreen {
    private ShapeRenderer shapes;
    private int levelNumber;
    private int triggered;
    private int total;
    private int stars;
    private int score;
    private float timer = 0;

    public CRResultScreen(BaseGame game, int levelNumber, int triggered, int total, int stars, int score) {
        super(game);
        this.shapes = new ShapeRenderer();
        this.levelNumber = levelNumber;
        this.triggered = triggered;
        this.total = total;
        this.stars = stars;
        this.score = score;
    }

    @Override
    public void update(float delta) {
        timer += delta;
        
        if (timer > 1.0f && Gdx.input.justTouched()) {
            float touchY = BaseGame.WORLD_HEIGHT - Gdx.input.getY() * BaseGame.WORLD_HEIGHT / Gdx.graphics.getHeight();
            
            if (touchY > BaseGame.WORLD_HEIGHT / 2) {
                // Top half — next level or retry
                if (stars > 0) {
                    game.setScreen(new CRGameScreen((BaseGame) game, levelNumber + 1));
                } else {
                    game.setScreen(new CRGameScreen((BaseGame) game, levelNumber));
                }
            } else {
                // Bottom half — menu
                game.setScreen(new CRMenuScreen((BaseGame) game));
            }
        }
    }

    @Override
    public void draw(float delta) {}

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.08f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        camera.update();
        update(delta);
        
        // Draw stars
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < 3; i++) {
            float cx = BaseGame.WORLD_WIDTH / 2 - 60 + i * 60;
            float cy = BaseGame.WORLD_HEIGHT * 0.6f;
            if (i < stars) {
                shapes.setColor(1f, 0.85f, 0f, 1f); // Gold
            } else {
                shapes.setColor(0.3f, 0.3f, 0.3f, 1f); // Grey
            }
            shapes.circle(cx, cy, 20, 5); // Pentagon approximation for star
        }
        shapes.end();
        
        // Text
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        
        game.font.getData().setScale(2.5f);
        game.font.setColor(stars > 0 ? Color.GREEN : Color.RED);
        String title = stars > 0 ? "SUCCESS!" : "TRY AGAIN";
        game.font.draw(game.batch, title, BaseGame.WORLD_WIDTH / 2 - 90, BaseGame.WORLD_HEIGHT * 0.78f);
        
        game.font.getData().setScale(2f);
        game.font.setColor(Color.WHITE);
        game.font.draw(game.batch, "Level " + levelNumber, BaseGame.WORLD_WIDTH / 2 - 50, BaseGame.WORLD_HEIGHT * 0.7f);
        game.font.draw(game.batch, "Triggered: " + triggered + " / " + total, 100, BaseGame.WORLD_HEIGHT * 0.48f);
        game.font.draw(game.batch, "Score: " + score, 100, BaseGame.WORLD_HEIGHT * 0.42f);
        
        if (timer > 1.0f) {
            game.font.setColor(0.7f, 0.7f, 1f, 1f);
            if (stars > 0) {
                game.font.draw(game.batch, "Tap top: Next Level", 100, BaseGame.WORLD_HEIGHT * 0.3f);
            } else {
                game.font.draw(game.batch, "Tap top: Retry", 130, BaseGame.WORLD_HEIGHT * 0.3f);
            }
            game.font.draw(game.batch, "Tap bottom: Menu", 120, BaseGame.WORLD_HEIGHT * 0.22f);
        }
        
        game.font.setColor(Color.WHITE);
        game.font.getData().setScale(2f);
        game.batch.end();
    }

    @Override
    public void dispose() {
        shapes.dispose();
    }
}
