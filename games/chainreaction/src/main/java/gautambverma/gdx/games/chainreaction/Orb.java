package gautambverma.gdx.games.chainreaction;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * A floating orb that can be triggered to explode.
 * Orbs drift slowly and expand when exploding, triggering nearby orbs.
 */
public class Orb {
    public Vector2 position;
    public Vector2 velocity;
    public float radius;
    public Color color;
    public OrbState state = OrbState.FLOATING;
    
    // Explosion properties
    public float explosionRadius;      // Max radius when fully exploded
    public float currentExplosionRadius = 0;
    public float explosionSpeed;       // How fast it expands
    public float explosionTimer = 0;
    public float explosionDuration;    // How long it stays expanded
    public float fadeTimer = 0;
    public float fadeDuration = 0.3f;
    
    public enum OrbState {
        FLOATING,    // Drifting around
        EXPLODING,   // Expanding
        EXPANDED,    // Holding at max radius (can trigger others)
        FADING,      // Shrinking and disappearing
        DEAD         // Remove from game
    }

    public Orb(float x, float y, OrbSize size) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(
            MathUtils.random(-20f, 20f),
            MathUtils.random(-20f, 20f)
        );
        
        switch (size) {
            case SMALL:
                this.radius = 12f;
                this.explosionRadius = 40f;
                this.explosionSpeed = 200f;
                this.explosionDuration = 0.5f;
                break;
            case MEDIUM:
                this.radius = 18f;
                this.explosionRadius = 65f;
                this.explosionSpeed = 150f;
                this.explosionDuration = 0.7f;
                break;
            case LARGE:
                this.radius = 25f;
                this.explosionRadius = 100f;
                this.explosionSpeed = 100f;
                this.explosionDuration = 1.0f;
                break;
        }
        
        // Random warm/cool color
        this.color = new Color(
            MathUtils.random(0.3f, 1f),
            MathUtils.random(0.1f, 0.8f),
            MathUtils.random(0.3f, 1f),
            1f
        );
    }

    public void update(float delta, float worldWidth, float worldHeight) {
        switch (state) {
            case FLOATING:
                position.x += velocity.x * delta;
                position.y += velocity.y * delta;
                // Bounce off walls
                if (position.x - radius < 0 || position.x + radius > worldWidth) {
                    velocity.x *= -1;
                    position.x = MathUtils.clamp(position.x, radius, worldWidth - radius);
                }
                if (position.y - radius < 0 || position.y + radius > worldHeight) {
                    velocity.y *= -1;
                    position.y = MathUtils.clamp(position.y, radius, worldHeight - radius);
                }
                break;
                
            case EXPLODING:
                currentExplosionRadius += explosionSpeed * delta;
                if (currentExplosionRadius >= explosionRadius) {
                    currentExplosionRadius = explosionRadius;
                    state = OrbState.EXPANDED;
                    explosionTimer = 0;
                }
                break;
                
            case EXPANDED:
                explosionTimer += delta;
                if (explosionTimer >= explosionDuration) {
                    state = OrbState.FADING;
                    fadeTimer = 0;
                }
                break;
                
            case FADING:
                fadeTimer += delta;
                currentExplosionRadius = explosionRadius * (1f - fadeTimer / fadeDuration);
                if (fadeTimer >= fadeDuration) {
                    state = OrbState.DEAD;
                }
                break;
                
            case DEAD:
                break;
        }
    }

    public void trigger() {
        if (state == OrbState.FLOATING) {
            state = OrbState.EXPLODING;
            currentExplosionRadius = radius;
            velocity.set(0, 0);
        }
    }

    /** Check if this exploding orb can trigger another floating orb */
    public boolean canTrigger(Orb other) {
        if (other.state != OrbState.FLOATING) return false;
        if (state != OrbState.EXPLODING && state != OrbState.EXPANDED) return false;
        float dist = position.dst(other.position);
        return dist <= currentExplosionRadius + other.radius;
    }

    public float getAlpha() {
        if (state == OrbState.FADING) {
            return 1f - (fadeTimer / fadeDuration);
        }
        return 1f;
    }

    public enum OrbSize {
        SMALL, MEDIUM, LARGE
    }
}
