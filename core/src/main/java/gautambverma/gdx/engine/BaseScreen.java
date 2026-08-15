package gautambverma.gdx.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Base screen with camera and viewport management.
 * All game screens extend this.
 */
public abstract class BaseScreen implements Screen {
    protected BaseGame game;
    protected OrthographicCamera camera;
    protected Viewport viewport;

    public BaseScreen(BaseGame game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(BaseGame.WORLD_WIDTH, BaseGame.WORLD_HEIGHT, camera);
        camera.position.set(BaseGame.WORLD_WIDTH / 2, BaseGame.WORLD_HEIGHT / 2, 0);
    }

    @Override
    public void render(float delta) {
        // Clear screen with black
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        update(delta);

        game.batch.begin();
        draw(delta);
        game.batch.end();
    }

    /** Update game logic */
    public abstract void update(float delta);

    /** Draw game objects (called between batch.begin/end) */
    public abstract void draw(float delta);

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {}
}
