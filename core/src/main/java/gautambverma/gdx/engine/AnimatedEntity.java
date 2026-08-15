package gautambverma.gdx.engine;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Entity with frame-based animation (walk cycles, explosions, etc.)
 */
public class AnimatedEntity extends SpriteEntity {
    private Animation<TextureRegion> animation;
    private float stateTime = 0;
    private boolean looping = true;

    public AnimatedEntity(float x, float y, float width, float height) {
        super(x, y, width, height);
    }

    public void setAnimation(Texture spriteSheet, int cols, int rows, float frameDuration) {
        TextureRegion[][] tmp = TextureRegion.split(spriteSheet,
                spriteSheet.getWidth() / cols,
                spriteSheet.getHeight() / rows);

        TextureRegion[] frames = new TextureRegion[cols * rows];
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                frames[index++] = tmp[i][j];
            }
        }
        animation = new Animation<>(frameDuration, frames);
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public boolean isAnimationFinished() {
        return animation != null && animation.isAnimationFinished(stateTime);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        stateTime += delta;
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (!active) return;
        if (animation != null) {
            TextureRegion frame = animation.getKeyFrame(stateTime, looping);
            batch.draw(frame, position.x, position.y, width, height);
        } else {
            super.draw(batch);
        }
    }
}
