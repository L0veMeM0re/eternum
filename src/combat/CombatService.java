package combat;

import player.Stats;
import util.ConsoleColors;
import util.RandomUtil;

public class CombatService {

    public static class AttackModifiers {
        private double attackMultiplier = 1.0;
        private double defensePenetration;
        private double critBonus;
        private boolean ignoreDodge;
        private boolean forceNoCrit;
        private double damageMultiplier = 1.0;
        private double incomingDamageMultiplier = 1.0;
        private double defenderDefBonus;

        public double getDamageMultiplier() {
            return damageMultiplier;
        }

        public double getIncomingDamageMultiplier() {
            return incomingDamageMultiplier;
        }

        public double getAttackMultiplier() {
            return attackMultiplier;
        }

        public AttackModifiers attackMultiplier(double attackMultiplier) {
            this.attackMultiplier = attackMultiplier;
            return this;
        }

        public AttackModifiers defensePenetration(double defensePenetration) {
            this.defensePenetration = defensePenetration;
            return this;
        }

        public AttackModifiers critBonus(double critBonus) {
            this.critBonus = critBonus;
            return this;
        }

        public AttackModifiers ignoreDodge(boolean ignoreDodge) {
            this.ignoreDodge = ignoreDodge;
            return this;
        }

        public AttackModifiers forceNoCrit(boolean forceNoCrit) {
            this.forceNoCrit = forceNoCrit;
            return this;
        }

        public AttackModifiers damageMultiplier(double damageMultiplier) {
            this.damageMultiplier = damageMultiplier;
            return this;
        }

        public AttackModifiers defenderDefBonus(double defenderDefBonus) {
            this.defenderDefBonus = defenderDefBonus;
            return this;
        }

        public AttackModifiers incomingDamageMultiplier(double incomingDamageMultiplier) {
            this.incomingDamageMultiplier = incomingDamageMultiplier;
            return this;
        }
    }

    public static class AttackResult {
        private final int damage;
        private final boolean critical;
        private final boolean dodged;

        public AttackResult(int damage, boolean critical, boolean dodged) {
            this.damage = damage;
            this.critical = critical;
            this.dodged = dodged;
        }

        public int getDamage() {
            return damage;
        }

        public boolean isCritical() {
            return critical;
        }

        public boolean isDodged() {
            return dodged;
        }
    }

    public AttackResult attack(Stats attacker, Stats defender) {
        return attack(attacker, defender, new AttackModifiers());
    }

    public AttackResult attack(Stats attacker, Stats defender, AttackModifiers mods) {
        if (!mods.ignoreDodge && RandomUtil.chance(defender.getDodgeChance())) {
            return new AttackResult(0, false, true);
        }

        int effectiveAttack = Math.max(1, (int) (attacker.getAttack() * mods.attackMultiplier));
        int effectiveDefense = Math.max(0, (int) (defender.getDefense() * (1.0 - mods.defensePenetration)
                + defender.getDefense() * mods.defenderDefBonus));
        int baseDamage = Math.max(1, effectiveAttack - effectiveDefense);

        boolean critical = !mods.forceNoCrit && RandomUtil.chance(attacker.getCritChance() + mods.critBonus);
        int damage = critical ? (int) (baseDamage * attacker.getCritDamage()) : baseDamage;
        damage = Math.max(1, (int) (damage * mods.damageMultiplier));
        damage = Math.max(1, (int) (damage * mods.incomingDamageMultiplier));
        defender.takeDamage(damage);
        return new AttackResult(damage, critical, false);
    }

    /** Урон без применения — для перенаправления (сет Титан). */
    public DamagePreview previewDamage(Stats attacker, Stats defender, AttackModifiers mods) {
        if (!mods.ignoreDodge && RandomUtil.chance(defender.getDodgeChance())) {
            return new DamagePreview(0, false, true);
        }
        int effectiveAttack = Math.max(1, (int) (attacker.getAttack() * mods.attackMultiplier));
        int effectiveDefense = Math.max(0, (int) (defender.getDefense() * (1.0 - mods.defensePenetration)
                + defender.getDefense() * mods.defenderDefBonus));
        int baseDamage = Math.max(1, effectiveAttack - effectiveDefense);
        boolean critical = !mods.forceNoCrit && RandomUtil.chance(attacker.getCritChance() + mods.critBonus);
        int damage = critical ? (int) (baseDamage * attacker.getCritDamage()) : baseDamage;
        damage = Math.max(1, (int) (damage * mods.damageMultiplier));
        damage = Math.max(1, (int) (damage * mods.incomingDamageMultiplier));
        return new DamagePreview(damage, critical, false);
    }

    public static final class DamagePreview {
        public final int damage;
        public final boolean critical;
        public final boolean dodged;

        public DamagePreview(int damage, boolean critical, boolean dodged) {
            this.damage = damage;
            this.critical = critical;
            this.dodged = dodged;
        }
    }

    public String formatVampirismHeal(int healed) {
        if (healed <= 0) {
            return "";
        }
        return ConsoleColors.wrap(ConsoleColors.BRIGHT_RED,
                "  ♥ Вампиризм: +" + healed + " HP");
    }

    public String formatSkillHeal(String targetName, int healed) {
        if (healed <= 0) {
            return "";
        }
        return ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                "  ✚ " + targetName + ": +" + healed + " HP");
    }
}
