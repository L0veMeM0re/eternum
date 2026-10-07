package enemy;

import util.RandomUtil;

import java.util.Arrays;
import java.util.List;

public class EnemyFactory {
    public static final int MAX_FLOOR = 100;

    private static final EnemyType[] FOREST_ENEMIES = {
            EnemyType.GOBLIN, EnemyType.WOLF, EnemyType.BAT, EnemyType.SLIME
    };
    private static final EnemyType[] CAVE_ENEMIES = {
            EnemyType.SKELETON, EnemyType.SPIDER, EnemyType.ORC, EnemyType.GHOST
    };
    private static final EnemyType[] SWAMP_ENEMIES = {
            EnemyType.WITCH, EnemyType.SLIME, EnemyType.BAT, EnemyType.GHOST
    };
    private static final EnemyType[] RUINS_ENEMIES = {
            EnemyType.GOLEM, EnemyType.SKELETON, EnemyType.ELEMENTAL, EnemyType.DEMON
    };
    private static final EnemyType[] ABYSS_ENEMIES = {
            EnemyType.DEMON, EnemyType.ELEMENTAL, EnemyType.GHOST, EnemyType.GOLEM
    };

    public String getZoneName(int floor) {
        if (floor <= 20) {
            return "Лес";
        }
        if (floor <= 40) {
            return "Пещера";
        }
        if (floor <= 60) {
            return "Болото";
        }
        if (floor <= 80) {
            return "Руины";
        }
        return "Бездна";
    }

    public Enemy createEnemy(int floor) {
        if (floor % 10 == 0) {
            return createBoss(floor);
        }

        String zone = getZoneName(floor);
        EnemyType type = pickRandomType(zone);
        return new Enemy(type, zone, floor);
    }

    public Boss createBoss(int floor) {
        BossType bossType = BossType.forFloor(floor);
        return new Boss(bossType, getZoneName(floor), floor);
    }

    private EnemyType pickRandomType(String zone) {
        EnemyType[] pool = switch (zone) {
            case "Лес" -> FOREST_ENEMIES;
            case "Пещера" -> CAVE_ENEMIES;
            case "Болото" -> SWAMP_ENEMIES;
            case "Руины" -> RUINS_ENEMIES;
            default -> ABYSS_ENEMIES;
        };
        return pool[RandomUtil.range(0, pool.length - 1)];
    }

    public List<String> getZoneEnemies(String zone) {
        EnemyType[] pool = switch (zone) {
            case "Лес" -> FOREST_ENEMIES;
            case "Пещера" -> CAVE_ENEMIES;
            case "Болото" -> SWAMP_ENEMIES;
            case "Руины" -> RUINS_ENEMIES;
            default -> ABYSS_ENEMIES;
        };
        return Arrays.stream(pool)
                .map(t -> t.getDisplayName() + " (HP:" + t.getBaseHp() + " ATK:" + t.getBaseAttack() + ")")
                .toList();
    }
}
