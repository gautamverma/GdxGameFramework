package gautambverma.gdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import gautambverma.gdx.engine.BaseGame;
import gautambverma.gdx.engine.BaseScreen;

/**
 * Generic menu screen — override to customize per game.
 */
public abstract class MenuScreen extends BaseScreen {
    protected String title;

    public MenuScreen(BaseGame game, String title) {
        super(game);
        this.title = title;
    }

    @Override
    public void update(float delta) {
        if (Gdx.input.justTouched()) {
            onTap(Gdx.input.getX(), Gdx.input.getY());
        }
    }

    @Override
    public void draw(float delta) {
        game.font.draw(game.batch, title, 
            BaseGame.WORLD_WIDTH / 2 - title.length() * 10, 
            BaseGame.WORLD_HEIGHT * 0.7f);
    }

    /** Override to handle tap at screen coordinates */
    protected abstract void onTap(int screenX, int screenY);
}
