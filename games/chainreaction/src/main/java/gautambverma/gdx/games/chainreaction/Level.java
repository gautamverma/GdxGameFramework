package gautambverma.gdx.games.chainreaction;

import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Level definition — how many orbs, what sizes, what % needed to pass.
 */
public class Level {
    public int levelNumber;
    public int totalOrbs;
    public int requiredOrbs;     // How many must be triggered to pass
    public int starThreshold2;   // For 2 stars
    public int starThreshold3;   // For 3 stars (usually all)
    public float smallPct, mediumPct, largePct;

    public Level(int number) {
        this.levelNumber = number;
        // Progressive difficulty
        this.totalOrbs = 8 + number * 3;  // 11, 14, 17, 20, 23...
        if (totalOrbs > 50) totalOrbs = 50;
        
        float requiredPct = 0.5f + number * 0.03f;  // 53%, 56%, 59%... caps at 85%
        if (requiredPct > 0.85f) requiredPct = 0.85f;
        this.requiredOrbs = (int)(totalOrbs * requiredPct);
        
        this.starThreshold2 = (int)(totalOrbs * (requiredPct + 0.1f));
        this.starThreshold3 = totalOrbs;  // All orbs = 3 stars
        
        // Size distribution shifts to more small orbs at higher levels
        if (number <= 5) {
            smallPct = 0.3f; mediumPct = 0.4f; largePct = 0.3f;
        } else if (number <= 15) {
            smallPct = 0.4f; mediumPct = 0.4f; largePct = 0.2f;
        } else {
            smallPct = 0.5f; mediumPct = 0.35f; largePct = 0.15f;
        }
    }

    public List<Orb> generateOrbs(float worldWidth, float worldHeight) {
        List<Orb> orbs = new ArrayList<>();
        float margin = 40f;
        
        for (int i = 0; i < totalOrbs; i++) {
            float x = MathUtils.random(margin, worldWidth - margin);
            float y = MathUtils.random(margin, worldHeight - margin * 3); // Leave space for HUD
            
            Orb.OrbSize size;
            float roll = MathUtils.random();
            if (roll < smallPct) size = Orb.OrbSize.SMALL;
            else if (roll < smallPct + mediumPct) size = Orb.OrbSize.MEDIUM;
            else size = Orb.OrbSize.LARGE;
            
            orbs.add(new Orb(x, y, size));
        }
        return orbs;
    }

    public int getStars(int triggered) {
        if (triggered >= starThreshold3) return 3;
        if (triggered >= starThreshold2) return 2;
        if (triggered >= requiredOrbs) return 1;
        return 0;
    }
}
