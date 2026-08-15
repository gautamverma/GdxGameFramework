package gautambverma.gdx.games.chainreaction;

import gautambverma.gdx.engine.BaseGame;

public class ChainReactionGame extends BaseGame {
    @Override
    public void init() {
        setScreen(new CRMenuScreen(this));
    }
}
