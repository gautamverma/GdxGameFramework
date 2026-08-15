package gautambverma.gdx.games.chainreaction;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import gautambverma.gdx.engine.BaseGame;
import gautambverma.gdx.engine.BaseScreen;
import java.util.ArrayList;
import java.util.List;

/**
 * Menu screen with floating orbs background animation.
 */
public class CRMenuScreen extends BaseScreen {
    private ShapeRenderer shapes;
    private List<Orb> bgOrbs;
    private float pulseTimer = 0;

    public CRMenuScreen(BaseGame game) {
        super(game);
        shapes = new ShapeRenderer();
        
        // Background orbs for visual interest
        bgOrbs = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            Orb orb = new Orb(
                MathUtils.random(50f, BaseGame.WORLD_WIDTH - 50f),
                MathUtils.random(50f, BaseGame.WORLD_HEIGHT - 50f),
                Orb.OrbSize.values()[MathUtils.random(2)]
            );
            bgOrbs.add(orb);
        }
    }

    @Override
    public void update(float delta) {
        pulseTimer += delta;
        
        for (Orb orb : bgOrbs) {
            orb.update(delta, BaseGame.WORLD_WIDTH, BaseGame.WORLD_HEIGHT);
        }
        
        if (Gdx.input.justTouched()) {
            game.setScreen(new CRGameScreen((BaseGame) game, 1));
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
        
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        
        // Background orbs (dimmed)
        for (Orb orb : bgOrbs) {
            shapes.setColor(orb.color.r * 0.4f, orb.color.g * 0.4f, orb.color.b * 0.4f, 0.3f);
            shapes.circle(orb.position.x, orb.position.y, orb.radius, 20);
        }
        shapes.end();
        
        // Title and instructions
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        
        game.font.setColor(0.2f, 1f, 0.8f, 1f);
        game.font.getData().setScale(3f);
        game.font.draw(game.batch, "CHAIN", BaseGame.WORLD_WIDTH / 2 - 80, BaseGame.WORLD_HEIGHT * 0.65f);
        game.font.draw(game.batch, "REACTION", BaseGame.WORLD_WIDTH / 2 - 110, BaseGame.WORLD_HEIGHT * 0.58f);
        
        game.font.getData().setScale(2f);
        float alpha = 0.5f + 0.5f * MathUtils.sin(pulseTimer * 3f);
        game.font.setColor(1, 1, 1, alpha);
        game.font.draw(game.batch, "Tap to Play", BaseGame.WORLD_WIDTH / 2 - 80, BaseGame.WORLD_HEIGHT * 0.35f);
        
        game.font.setColor(0.6f, 0.6f, 0.7f, 1f);
        game.font.getData().setScale(1.5f);
        game.font.draw(game.batch, "One tap. Maximum chaos.", 90, BaseGame.WORLD_HEIGHT * 0.25f);
        
        game.font.getData().setScale(2f);
        game.font.setColor(Color.WHITE);
        game.batch.end();
    }

    @Override
    public void dispose() {
        shapes.dispose();
    }
}
