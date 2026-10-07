package enemy;

import player.Stats;

/**
 * Масштаб врагов по этажу + множитель сложности по диапазонам этажей.
 * Этажи 1–30 смягчены для ранней игры.
 */
public final class EnemyScaling {
    private static final int LATE_GAME_START = 50;
    private static final int EARLY_GAME_END = 30;

    private EnemyScaling() {
    }

    public static Stats scaleMobStats(EnemyType type, int floor) {
        double mult = getDifficultyMultiplier(floor) * endgameMultiplier(floor) * earlyEaseMultiplier(floor);
        int hpPerFloor = floor <= EARLY_GAME_END ? 9 : 14;
        int atkPerFloor = floor <= EARLY_GAME_END ? 2 : 3;
        int defPerFloor = floor <= EARLY_GAME_END ? 1 : 2;
        int hp = (int) (scaleStat(type.getBaseHp(), floor, hpPerFloor, 22) * mult);
        int atk = (int) (scaleStat(type.getBaseAttack(), floor, atkPerFloor, 5) * mult);
        int def = (int) (scaleStat(type.getBaseDefense(), floor, defPerFloor, 2) * mult);
        double crit = type.getCritChance() + floor * critPerFloor(floor) + lateBonus(floor, 0.4);
        double dodge = type.getDodgeChance() + floor * dodgePerFloor(floor) + lateBonus(floor, 0.25);
        double critDmg = 1.8 + floor * 0.008 + lateBonus(floor, 0.08);
        return new Stats(hp, atk, def, crit, critDmg, dodge);
    }

    private static double critPerFloor(int floor) {
        return floor <= EARLY_GAME_END ? 0.10 : 0.15;
    }

    private static double dodgePerFloor(int floor) {
        return floor <= EARLY_GAME_END ? 0.06 : 0.10;
    }

    /** Снижение статов на ранних этажах. */
    private static double earlyEaseMultiplier(int floor) {
        if (floor <= 10) {
            return 0.80;
        }
        if (floor <= 20) {
            return 0.88;
        }
        if (floor <= EARLY_GAME_END) {
            return 0.94;
        }
        return 1.0;
    }

    public static Stats scaleBossStats(BossType bossType, int floor) {
        double mult = getDifficultyMultiplier(floor) * 1.05 * endgameMultiplier(floor) * bossFloorBonus(floor)
                * earlyEaseMultiplier(floor);
        int tier = floor / 10;
        int hpPerFloor = floor <= EARLY_GAME_END ? 12 : 18;
        int hp = (int) (scaleStat(bossType.getBaseHp() + tier * 35, floor, hpPerFloor, 35) * mult);
        int atk = (int) (scaleStat(bossType.getBaseAttack() + tier * 3, floor, 3, 7) * mult);
        int def = (int) (scaleStat(bossType.getBaseDefense() + tier * 2, floor, 2, 3) * mult);
        double crit = bossType.getCritChance() + tier * 0.8 + lateBonus(floor, 0.5);
        double dodge = bossType.getDodgeChance() + tier * 0.4 + lateBonus(floor, 0.3);
        double critDmg = 2.5 + tier * 0.08 + lateBonus(floor, 0.12);
        return new Stats(hp, atk, def, crit, critDmg, dodge);
    }

    /** Этажи 80–100: +40% к HP/ATK/DEF мобов и боссов. */
    private static double endgameMultiplier(int floor) {
        return floor >= 80 ? 1.40 : 1.0;
    }

    /** Предпоследний босс (90) +20%; финальный (100) +30% и ещё +30%. */
    private static double bossFloorBonus(int floor) {
        if (floor >= 100) {
            return 1.30 * 1.30;
        }
        if (floor >= 90) {
            return 1.20;
        }
        return 1.0;
    }

    /**
     * 1–10: без бонуса, 11–20: +10%, 21–29: до +18%, 30+: прежняя кривая смягчена.
     */
    public static double getDifficultyMultiplier(int floor) {
        if (floor <= 10) {
            return 1.0;
        }
        if (floor <= 20) {
            return 1.10;
        }
        if (floor < 30) {
            return 1.10 + (floor - 20) * 0.008;
        }
        if (floor <= 40) {
            return 1.22;
        }
        if (floor <= 50) {
            return 1.28;
        }
        return 1.28 + (floor - 50) * 0.008;
    }

    private static int scaleStat(int base, int floor, int earlyPerFloor, int latePerFloor) {
        int earlyFloors = Math.min(floor, LATE_GAME_START);
        int lateFloors = Math.max(0, floor - LATE_GAME_START);
        return base + earlyFloors * earlyPerFloor + lateFloors * latePerFloor;
    }

    private static double lateBonus(int floor, double perFloorAfter50) {
        return Math.max(0, floor - LATE_GAME_START) * perFloorAfter50;
    }

    public static String getDifficultyLabel(int floor) {
        double mult = getDifficultyMultiplier(floor);
        int pct = (int) Math.round((mult - 1.0) * 100);
        if (floor <= 20) {
            return "норма (+" + pct + "%)";
        }
        if (floor <= 40) {
            return "сложно (+" + pct + "%)";
        }
        if (floor <= 60) {
            return "экстрим (+" + pct + "%)";
        }
        return "кошмар (+" + pct + "%)";
    }
}
