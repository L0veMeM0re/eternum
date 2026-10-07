package combat;

import inventory.Equipment;
import inventory.SetBonusCalculator;
import player.StatCaps;
import player.Stats;
import progression.MetaProgression;

public final class UnitStatCalculator {
    private UnitStatCalculator() {
    }

    public static Stats compute(Stats base, Equipment equipment, MetaProgression meta, double runDamageBonus) {
        return compute(base, equipment, meta, runDamageBonus, false);
    }

    public static Stats compute(Stats base, Equipment equipment, MetaProgression meta,
                                double runDamageBonus, boolean clericAura) {
        SetBonusCalculator.BonusTotals set = SetBonusCalculator.calculate(equipment);

        int hp = base.getMaxHp() + equipment.totalHpBonus() + set.flatHp;
        int atk = base.getAttack() + equipment.totalAttackBonus() + set.flatAtk;
        int def = base.getDefense() + equipment.totalDefenseBonus() + set.flatDef;
        double crit = base.getCritChance() + equipment.totalCritBonus() + set.flatCrit;
        double critDmg = base.getCritDamage() + equipment.totalCritDamageBonus() + set.flatCritDmg;
        double dodge = base.getDodgeChance() + equipment.totalDodgeBonus() + set.flatDodge;

        hp = (int) (hp * (1.0 + set.percentHp / 100.0));
        atk = (int) (atk * (1.0 + set.percentAtk / 100.0));
        def = (int) (def * (1.0 + set.percentDef / 100.0));

        if (meta != null) {
            hp = (int) (hp * meta.getHealthMultiplier());
            atk = (int) (atk * meta.getDamageMultiplier());
        }
        atk = (int) (atk * (1.0 + runDamageBonus));

        if (clericAura) {
            hp = (int) (hp * 1.1);
        }

        crit = StatCaps.capCrit(crit);
        dodge = StatCaps.capDodge(dodge);
        return new Stats(hp, atk, def, crit, critDmg, dodge);
    }

    public static double vampChance(Equipment equipment) {
        SetBonusCalculator.BonusTotals set = SetBonusCalculator.calculate(equipment);
        return Math.min(50, equipment.totalVampirismChance() + set.vampirismChance);
    }

    public static double vampHealPercent(Equipment equipment) {
        SetBonusCalculator.BonusTotals set = SetBonusCalculator.calculate(equipment);
        return Math.max(equipment.maxVampirismHealPercent(), set.vampirismHealPercent);
    }
}
