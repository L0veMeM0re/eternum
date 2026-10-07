package enemy;

import progression.MagicShop;
import player.Stats;
import util.RandomUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SquadFactory {
    private final EnemyFactory enemyFactory = new EnemyFactory();

    public EnemySquad createEncounter(int floor) {
        return createEncounter(floor, false);
    }

    public EnemySquad createEncounter(int floor, boolean fogScroll) {
        if (floor % 10 == 0) {
            return createBossEncounter(floor, fogScroll);
        }
        int count = rollMobCount(floor);
        if (fogScroll) {
            count = Math.max(1, count - 1);
        }
        String zone = enemyFactory.getZoneName(floor);
        List<Enemy> enemies = new ArrayList<>();
        Set<Integer> eliteSlots = rollEliteSlots(count, floor);
        for (int i = 0; i < count; i++) {
            EnemyType type = pickTypeForZone(zone);
            boolean elite = eliteSlots.contains(i);
            enemies.add(createMob(type, zone, floor, elite, fogScroll));
        }
        return new EnemySquad(enemies, false);
    }

    /** Один или несколько слотов в пачке могут стать элитными (не только одиночные враги). */
    private Set<Integer> rollEliteSlots(int count, int floor) {
        Set<Integer> slots = new HashSet<>();
        if (floor < 20 || count <= 0) {
            return slots;
        }
        if (RandomUtil.chance(EliteModifier.eliteChancePercent(floor))) {
            slots.add(RandomUtil.range(0, count - 1));
        }
        if (floor >= 70 && count >= 2 && RandomUtil.chance(EliteModifier.extraEliteInPackPercent(floor))) {
            slots.add(RandomUtil.range(0, count - 1));
        }
        return slots;
    }

    private EnemySquad createBossEncounter(int floor, boolean fogScroll) {
        Boss boss = enemyFactory.createBoss(floor);
        if (fogScroll) {
            applyFogWeakeningInPlace(boss.getStats());
        }
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(boss);
        BossType bossType = boss.getBossType();
        List<EnemyType> minionTypes = BossMinions.typesFor(bossType);
        int minionCount = Math.min(rollMobCount(floor) - 1, minionTypes.size());
        minionCount = Math.max(1, minionCount);
        if (fogScroll) {
            minionCount = Math.max(1, minionCount - 1);
        }
        String zone = enemyFactory.getZoneName(floor);
        Set<Integer> eliteMinionSlots = floor >= 70 ? rollEliteSlots(minionCount, floor) : Set.of();
        for (int i = 0; i < minionCount; i++) {
            EnemyType type = minionTypes.get(i % minionTypes.size());
            String title = BossMinions.minionTitle(bossType, type);
            Stats stats = EnemyScaling.scaleMobStats(type, floor);
            if (fogScroll) {
                applyFogWeakeningInPlace(stats);
            }
            if (eliteMinionSlots.contains(i)) {
                enemies.add(new EliteEnemy(title, type, zone, EliteModifier.applyElite(stats)));
            } else {
                enemies.add(new Enemy(title, zone, stats, type));
            }
        }
        return new EnemySquad(enemies, true);
    }

    private Enemy createMob(EnemyType type, String zone, int floor, boolean elite, boolean fogScroll) {
        if (!elite) {
            Enemy mob = new Enemy(type, zone, floor);
            if (fogScroll) {
                applyFogWeakeningInPlace(mob.getStats());
            }
            return mob;
        }
        Stats scaled = EnemyScaling.scaleMobStats(type, floor);
        if (fogScroll) {
            applyFogWeakeningInPlace(scaled);
        }
        Stats eliteStats = EliteModifier.applyElite(scaled);
        return new EliteEnemy(type, zone, eliteStats);
    }

    static void applyFogWeakeningInPlace(Stats stats) {
        double mult = MagicShop.FOG_ENEMY_POWER_PERCENT / 100.0;
        stats.setMaxHp(Math.max(1, (int) Math.round(stats.getMaxHp() * mult)));
        stats.setAttack(Math.max(1, (int) Math.round(stats.getAttack() * mult)));
        stats.setDefense(Math.max(0, (int) Math.round(stats.getDefense() * mult)));
        stats.setCurrentHp(stats.getMaxHp());
    }

    private EnemyType pickTypeForZone(String zone) {
        return switch (zone) {
            case "Лес" -> pick(EnemyType.GOBLIN, EnemyType.WOLF, EnemyType.BAT, EnemyType.SLIME);
            case "Пещера" -> pick(EnemyType.SKELETON, EnemyType.SPIDER, EnemyType.ORC, EnemyType.GHOST);
            case "Болото" -> pick(EnemyType.WITCH, EnemyType.SLIME, EnemyType.BAT, EnemyType.GHOST);
            case "Руины" -> pick(EnemyType.GOLEM, EnemyType.SKELETON, EnemyType.ELEMENTAL, EnemyType.DEMON);
            default -> pick(EnemyType.DEMON, EnemyType.ELEMENTAL, EnemyType.GHOST, EnemyType.GOLEM);
        };
    }

    private EnemyType pick(EnemyType... types) {
        return types[RandomUtil.range(0, types.length - 1)];
    }

    public static int rollMobCount(int floor) {
        if (floor < 10) {
            return 1;
        }
        if (floor < 20) {
            int chanceSecond = 30 + (floor - 10) * 7;
            return RandomUtil.chance(chanceSecond) ? 2 : 1;
        }
        if (floor < 60) {
            if (RandomUtil.chance(35 + (floor - 20) / 2)) {
                return 3;
            }
            return 2;
        }
        if (floor < 80) {
            if (RandomUtil.chance(35 + (floor - 60))) {
                return 4;
            }
            return 3;
        }
        return 4;
    }
}
