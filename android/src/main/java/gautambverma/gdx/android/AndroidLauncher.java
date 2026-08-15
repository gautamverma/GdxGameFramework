package gautambverma.gdx.android;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import gautambverma.gdx.games.spaceinvaders.SpaceInvadersGdx;

/**
 * Android entry point. Change the game class to launch different games.
 */
public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = true;
        config.useCompass = false;
        initialize(new SpaceInvadersGdx(), config);
    }
}
