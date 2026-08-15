package gautambverma.gdx.utils;

import gautambverma.gdx.engine.SpriteEntity;
import java.util.List;
import java.util.Iterator;

/**
 * Collision detection utilities.
 */
public class CollisionHelper {
    
    /** Check one entity against a list, remove hit entries */
    public static <T extends SpriteEntity> T checkCollision(SpriteEntity source, List<T> targets) {
        for (T target : targets) {
            if (target.active && source.overlaps(target)) {
                return target;
            }
        }
        return null;
    }

    /** Remove inactive entities from a list */
    public static <T extends SpriteEntity> void removeInactive(List<T> list) {
        Iterator<T> it = list.iterator();
        while (it.hasNext()) {
            if (!it.next().active) it.remove();
        }
    }
}
