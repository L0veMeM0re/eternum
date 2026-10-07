package enemy;

import java.util.ArrayList;
import java.util.List;

public class EnemySquad {
    private final List<Enemy> enemies;
    private final boolean bossFight;

    public EnemySquad(List<Enemy> enemies, boolean bossFight) {
        this.enemies = new ArrayList<>(enemies);
        this.bossFight = bossFight;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public boolean isBossFight() {
        return bossFight;
    }

    public boolean isAlive() {
        return enemies.stream().anyMatch(Enemy::isAlive);
    }

    public List<Enemy> getLiving() {
        return enemies.stream().filter(Enemy::isAlive).toList();
    }

    public Enemy randomLivingTarget() {
        List<Enemy> living = getLiving();
        if (living.isEmpty()) {
            return null;
        }
        return living.get(util.RandomUtil.range(0, living.size() - 1));
    }

    public Enemy randomLivingTargetExcluding(Enemy exclude) {
        List<Enemy> living = getLiving().stream().filter(e -> e != exclude).toList();
        if (living.isEmpty()) {
            return null;
        }
        return living.get(util.RandomUtil.range(0, living.size() - 1));
    }
}
