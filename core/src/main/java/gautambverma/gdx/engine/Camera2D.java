package gautambverma.gdx.engine;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Camera that follows a target with smooth lerp and bounds clamping.
 * Use for platformers, scrolling shooters, etc.
 */
public class Camera2D {
    public OrthographicCamera camera;
    private float worldWidth, worldHeight;
    private float viewWidth, viewHeight;
    private float lerpSpeed = 5f;

    public Camera2D(float viewWidth, float viewHeight, float worldWidth, float worldHeight) {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        camera = new OrthographicCamera(viewWidth, viewHeight);
    }

    public void follow(Vector2 target, float delta) {
        float targetX = MathUtils.clamp(target.x, viewWidth / 2, worldWidth - viewWidth / 2);
        float targetY = MathUtils.clamp(target.y, viewHeight / 2, worldHeight - viewHeight / 2);

        camera.position.x += (targetX - camera.position.x) * lerpSpeed * delta;
        camera.position.y += (targetY - camera.position.y) * lerpSpeed * delta;
        camera.update();
    }

    public void setLerpSpeed(float speed) { this.lerpSpeed = speed; }
    
    public void shake(float intensity) {
        camera.position.x += (float)(Math.random() - 0.5) * intensity;
        camera.position.y += (float)(Math.random() - 0.5) * intensity;
    }
}
