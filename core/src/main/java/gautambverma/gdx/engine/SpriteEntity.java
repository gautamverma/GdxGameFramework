package gautambverma.gdx.engine;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Base entity with position, size, velocity, and texture.
 * Games extend this for players, enemies, bullets, etc.
 */
public class SpriteEntity {
    public Vector2 position;
    public Vector2 velocity;
    public float width, height;
    public TextureRegion texture;
    public boolean active = true;

    public SpriteEntity(float x, float y, float width, float height) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0, 0);
        this.width = width;
        this.height = height;
    }

    public void setTexture(Texture tex) {
        this.texture = new TextureRegion(tex);
    }

    public void setTexture(TextureRegion region) {
        this.texture = region;
    }

    public void update(float delta) {
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
    }

    public void draw(SpriteBatch batch) {
        if (active && texture != null) {
            batch.draw(texture, position.x, position.y, width, height);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(position.x, position.y, width, height);
    }

    public boolean overlaps(SpriteEntity other) {
        return getBounds().overlaps(other.getBounds());
    }
}
