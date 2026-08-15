package gautambverma.gdx.engine;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

/**
 * Base game class that all games extend.
 * Provides shared SpriteBatch, font, and virtual viewport.
 */
public abstract class BaseGame extends Game {
    public SpriteBatch batch;
    public BitmapFont font;

    public static final float WORLD_WIDTH = 480;
    public static final float WORLD_HEIGHT = 800;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);
        init();
    }

    /** Override this instead of create() */
    public abstract void init();

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
