package progression.skill;

import player.CharacterClass;
import player.StatCaps;
import player.Stats;

/** Бонусы дерева умений для боя и статов. */
public final class SkillEffectCalculator {
    private SkillEffectCalculator() {
    }

    public static boolean isSkill1Unlocked(SkillTreeProgress progress) {
        return progress.getRank(SkillNodeDef.ACTIVE1_UNLOCK) >= 1;
    }

    public static boolean isSkill2Unlocked(SkillTreeProgress progress) {
        return progress.getRank(SkillNodeDef.ACTIVE2_UNLOCK) >= 1;
    }

    public static boolean hasCapstone(SkillTreeProgress progress) {
        return progress.getRank(SkillNodeDef.CAPSTONE) >= 1;
    }

    /** +5% урона скилла за ранг power+master. */
    public static double skillDamageBonus(SkillTreeProgress progress, boolean skill1) {
        String power = skill1 ? SkillNodeDef.ACTIVE1_POWER : SkillNodeDef.ACTIVE2_POWER;
        String master = skill1 ? SkillNodeDef.ACTIVE1_MASTER : SkillNodeDef.ACTIVE2_MASTER;
        return 1.0 + (progress.getRank(power) + progress.getRank(master)) * 0.05;
    }

    public static int skill1Cooldown(CharacterClass cls, SkillTreeProgress progress) {
        int base = cls.getSkillKit().getSkill1Cooldown();
        if (cls == CharacterClass.CLERIC && progress.getRank(SkillNodeDef.ACTIVE1_MASTER) >= 3) {
            return Math.max(1, base - 1);
        }
        return base;
    }

    public static int skill2Cooldown(CharacterClass cls, SkillTreeProgress progress) {
        return cls.getSkillKit().getSkill2Cooldown();
    }

    public static void applyPassiveStats(CharacterClass cls, SkillTreeProgress progress, Stats stats,
                                         boolean clericAuraFromHero) {
        int core = progress.getRank(SkillNodeDef.PASSIVE_CORE);
        int stat = progress.getRank(SkillNodeDef.PASSIVE_STAT);

        switch (cls) {
            case WARRIOR -> stats.addMaxHp((int) (stats.getMaxHp() * 0.035 * stat));
            case MAGE -> {
                stats.addCritDamage(0.4 + core * 0.05);
                stats.addCritChance(stat * 3.0);
            }
            case ROGUE -> {
                stats.addCritChance(8 + core * 2.0);
                stats.addDodgeChance(stat * 2.0);
            }
            case CLERIC -> {
                double hpBonus = 0.10 + core * 0.02;
                stats.setMaxHp((int) (stats.getMaxHp() * (1.0 + hpBonus)));
                if (clericAuraFromHero) {
                    stats.addDefense((int) (stats.getDefense() * 0.02 * stat));
                }
            }
            case BERSERKER -> { /* scaling in combat */ }
            default -> { }
        }
        stats.setCritChance(StatCaps.capCrit(stats.getCritChance()));
        stats.setDodgeChance(StatCaps.capDodge(stats.getDodgeChance()));
    }

    /** Бесконечная прокачка после полного дерева. */
    public static void applyOverflowStats(SkillTreeProgress progress, Stats stats) {
        int rank = progress.getRank(SkillNodeDef.OVERFLOW_STATS);
        if (rank <= 0) {
            return;
        }
        stats.addMaxHp(SkillTreeService.OVERFLOW_HP_PER_RANK * rank);
        stats.addAttack(SkillTreeService.OVERFLOW_ATK_PER_RANK * rank);
    }

    public static double warriorFirstHitReduction(SkillTreeProgress progress) {
        int core = progress.getRank(SkillNodeDef.PASSIVE_CORE);
        return 0.28 + core * 0.015;
    }

    public static double rogueCritHealPercent(SkillTreeProgress progress) {
        return 0.05 + progress.getRank(SkillNodeDef.ACTIVE1_MASTER) * 0.005;
    }

    public static int defBuffRounds(SkillTreeProgress progress) {
        return 2 + progress.getRank(SkillNodeDef.ACTIVE1_MASTER);
    }

    public static int critImmuneRounds(SkillTreeProgress progress) {
        return 1 + progress.getRank(SkillNodeDef.ACTIVE2_MASTER);
    }

    public static double fireDefPen(SkillTreeProgress progress) {
        return 0.50 + progress.getRank(SkillNodeDef.ACTIVE1_MASTER) * 0.05;
    }

    public static double iceAtkDebuff(SkillTreeProgress progress) {
        return 0.20 + progress.getRank(SkillNodeDef.ACTIVE2_POWER) * 0.015
                + progress.getRank(SkillNodeDef.ACTIVE2_MASTER) * 0.005;
    }

    public static int iceDebuffRounds(SkillTreeProgress progress) {
        return 2 + progress.getRank(SkillNodeDef.ACTIVE2_MASTER);
    }

    public static double feintCritBonus(SkillTreeProgress progress) {
        return 25 + progress.getRank(SkillNodeDef.ACTIVE1_MASTER) * 5.0;
    }

    public static double poisonDotMultiplier(SkillTreeProgress progress) {
        return 0.30 + progress.getRank(SkillNodeDef.ACTIVE2_MASTER) * 0.10;
    }

    public static double healPercent(SkillTreeProgress progress) {
        return 0.20 + progress.getRank(SkillNodeDef.ACTIVE1_POWER) * 0.015
                + progress.getRank(SkillNodeDef.ACTIVE1_MASTER) * 0.005;
    }

    public static double holyLifesteal(SkillTreeProgress progress) {
        return 0.50 + progress.getRank(SkillNodeDef.ACTIVE2_MASTER) * 0.10;
    }

    public static double bloodCostPercent(SkillTreeProgress progress) {
        return Math.max(0.04, 0.09 - progress.getRank(SkillNodeDef.ACTIVE1_MASTER) * 0.01);
    }

    public static int frenzyRounds(SkillTreeProgress progress) {
        return 3 + progress.getRank(SkillNodeDef.ACTIVE2_MASTER);
    }

    public static int berserkerAtkCap(SkillTreeProgress progress) {
        return 32 + progress.getRank(SkillNodeDef.PASSIVE_STAT) * 4;
    }

    public static double berserkerAtkPerMissing(SkillTreeProgress progress) {
        return 1.75 + progress.getRank(SkillNodeDef.PASSIVE_CORE) * 0.25;
    }

    public static double skill1BaseMultiplier(CharacterClass cls) {
        return switch (cls) {
            case WARRIOR -> 1.35;
            case MAGE -> 0.70;
            case ROGUE -> 1.40;
            case CLERIC -> 0;
            case BERSERKER -> 1.65;
        };
    }

    public static double skill2BaseMultiplier(CharacterClass cls) {
        return switch (cls) {
            case WARRIOR -> 0;
            case MAGE -> 1.20;
            case ROGUE -> 1.00;
            case CLERIC -> 1.10;
            case BERSERKER -> 0;
        };
    }
}
