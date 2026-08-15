package gautambverma.gdx.engine;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Simple particle system for explosions, sparks, etc.
 */
public class ParticlePool {
    private List<Particle> particles = new ArrayList<>();

    public void spawn(float x, float y, int count, float speed, float lifetime) {
        for (int i = 0; i < count; i++) {
            float angle = (float)(Math.random() * Math.PI * 2);
            float vel = (float)(Math.random() * speed);
            Particle p = new Particle();
            p.x = x;
            p.y = y;
            p.vx = (float)Math.cos(angle) * vel;
            p.vy = (float)Math.sin(angle) * vel;
            p.lifetime = lifetime * (0.5f + (float)Math.random() * 0.5f);
            p.maxLifetime = p.lifetime;
            particles.add(p);
        }
    }

    public void update(float delta) {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.x += p.vx * delta;
            p.y += p.vy * delta;
            p.lifetime -= delta;
            if (p.lifetime <= 0) it.remove();
        }
    }

    public void draw(SpriteBatch batch, SpriteEntity template) {
        for (Particle p : particles) {
            float alpha = p.lifetime / p.maxLifetime;
            batch.setColor(1, 1, 1, alpha);
            batch.draw(template.texture, p.x - 2, p.y - 2, 4, 4);
        }
        batch.setColor(1, 1, 1, 1);
    }

    public boolean isEmpty() { return particles.isEmpty(); }

    private static class Particle {
        float x, y, vx, vy, lifetime, maxLifetime;
    }
}
