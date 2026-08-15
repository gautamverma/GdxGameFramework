package gautambverma.gdx.games.spaceinvaders;

import gautambverma.gdx.engine.BaseGame;

/**
 * Space Invaders — demo game built on the GdxGameFramework.
 */
public class SpaceInvadersGdx extends BaseGame {
    @Override
    public void init() {
        setScreen(new SIGameScreen(this));
    }
}
