package enemy;

import player.Stats;

public final class EliteModifier {
    public static final double HP_MULT = 1.5;
    public static final double ATK_MULT = 1.25;
    public static final double DEF_MULT = 1.25;
    public static final double REWARD_MULT = 2.0;

    private EliteModifier() {
    }

    /** Шанс элиты в пачке: 5% на 20 этаже → ~14% к 69, затем 18%→40% с 70 по 100. */
    public static double eliteChancePercent(int floor) {
        if (floor < 20) {
            return 0;
        }
        if (floor < 70) {
            return 5.0 + (floor - 20) * (9.0 / 50.0);
        }
        if (floor >= EnemyFactory.MAX_FLOOR) {
            return 40;
        }
        return 18.0 + (floor - 70) * (22.0 / (EnemyFactory.MAX_FLOOR - 70));
    }

    /** Доп. шанс второй элиты в большой пачке (с 70 этажа). */
    public static double extraEliteInPackPercent(int floor) {
        if (floor < 70) {
            return 0;
        }
        if (floor >= EnemyFactory.MAX_FLOOR) {
            return 15;
        }
        return 8.0 + (floor - 70) * (7.0 / (EnemyFactory.MAX_FLOOR - 70));
    }

    public static Stats applyElite(Stats stats) {
        return new Stats(
                (int) (stats.getMaxHp() * HP_MULT),
                (int) (stats.getAttack() * ATK_MULT),
                (int) (stats.getDefense() * DEF_MULT),
                stats.getCritChance(),
                stats.getCritDamage(),
                stats.getDodgeChance());
    }
}
