package progression;

import util.ConsoleColors;
import util.RandomUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Эффекты зелий Абдолбос на текущий забег (каждое зелье — только баф или только дебаф). */
public final class RunEffects {
    public static final int MAX_POTIONS_PER_RUN = 10;

    public enum Positive {
        BERSERK("+17% урона", 0.17, 0, 0, 0, 0, 0, 0),
        THICK_SKIN("+12% HP", 0, 0.12, 0, 0, 0, 0, 0),
        SWIFT("+8% уворота", 0, 0, 0, 8, 0, 0, 0),
        GREED("+28% золота", 0, 0, 0, 0, 0.28, 0, 0),
        FOCUS("+6% крита", 0, 0, 6, 0, 0, 0, 0),
        VITALITY("лечение 6% HP после этажа", 0, 0, 0, 0, 0, 0, 0.06);

        public final String label;
        private final double damageBonus;
        private final double hpBonus;
        private final double defBonus;
        private final double dodgeBonus;
        private final double goldBonus;
        private final double critBonus;
        private final double floorHealPercent;

        Positive(String label, double damageBonus, double hpBonus, double defBonus,
                 double dodgeBonus, double goldBonus, double critBonus, double floorHealPercent) {
            this.label = label;
            this.damageBonus = damageBonus;
            this.hpBonus = hpBonus;
            this.defBonus = defBonus;
            this.dodgeBonus = dodgeBonus;
            this.goldBonus = goldBonus;
            this.critBonus = critBonus;
            this.floorHealPercent = floorHealPercent;
        }
    }

    public enum Negative {
        FRAGILE("-18% HP", 0, -0.18, 0, 0, 0, 0, 0),
        WEAK("-18% урона", -0.18, 0, 0, 0, 0, 0, 0),
        CLUMSY("-12% уворота", 0, 0, 0, -12, 0, 0, 0),
        CURSED("-22% защиты", 0, 0, -0.22, 0, 0, 0, 0),
        SLOW_MIND("-40% опыта", 0, 0, 0, 0, 0, -0.40, 0),
        BAD_LUCK("-18% шанс дропа", 0, 0, 0, 0, 0, 0, 0.18);

        public final String label;
        private final double damageBonus;
        private final double hpBonus;
        private final double defBonus;
        private final double dodgeBonus;
        private final double goldBonus;
        private final double xpBonus;
        private final double dropPenalty;

        Negative(String label, double damageBonus, double hpBonus, double defBonus,
                 double dodgeBonus, double goldBonus, double xpBonus, double dropPenalty) {
            this.label = label;
            this.damageBonus = damageBonus;
            this.hpBonus = hpBonus;
            this.defBonus = defBonus;
            this.dodgeBonus = dodgeBonus;
            this.goldBonus = goldBonus;
            this.xpBonus = xpBonus;
            this.dropPenalty = dropPenalty;
        }
    }

    private final List<Positive> positives = new ArrayList<>();
    private final List<Negative> negatives = new ArrayList<>();

    public boolean isActive() {
        return !positives.isEmpty() || !negatives.isEmpty();
    }

    public List<Positive> getPositives() {
        return Collections.unmodifiableList(positives);
    }

    public List<Negative> getNegatives() {
        return Collections.unmodifiableList(negatives);
    }

    /** Одно зелье: 50% — случайный баф, иначе — случайный дебаф. Каждый тип — не чаще одного раза. */
    public String usePotion() {
        if (RandomUtil.chance(50)) {
            Positive effect = pickUnusedPositive();
            if (effect != null) {
                positives.add(effect);
                return ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                        "★ Повезло! " + effect.label);
            }
            return ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                    "★ Повезло! (все бафы уже активны — без доп. эффекта)");
        }
        Negative effect = pickUnusedNegative();
        if (effect != null) {
            negatives.add(effect);
            return ConsoleColors.wrap(ConsoleColors.BRIGHT_RED,
                    "✧ Не повезло! " + effect.label);
        }
        return ConsoleColors.wrap(ConsoleColors.BRIGHT_RED,
                "✧ Не повезло! (все дебафы уже активны — без доп. эффекта)");
    }

    private Positive pickUnusedPositive() {
        List<Positive> pool = new ArrayList<>();
        for (Positive p : Positive.values()) {
            if (!positives.contains(p)) {
                pool.add(p);
            }
        }
        if (pool.isEmpty()) {
            return null;
        }
        return pool.get(RandomUtil.range(0, pool.size() - 1));
    }

    private Negative pickUnusedNegative() {
        List<Negative> pool = new ArrayList<>();
        for (Negative n : Negative.values()) {
            if (!negatives.contains(n)) {
                pool.add(n);
            }
        }
        if (pool.isEmpty()) {
            return null;
        }
        return pool.get(RandomUtil.range(0, pool.size() - 1));
    }

    public void clear() {
        positives.clear();
        negatives.clear();
    }

    private static final double MIN_MULT = 0.25;
    private static final double MAX_MULT = 2.5;

    public double damageMultiplier() {
        return clampMult(1.0 + sumPositive(p -> p.damageBonus) + sumNegative(n -> n.damageBonus));
    }

    public double hpMultiplier() {
        return clampMult(1.0 + sumPositive(p -> p.hpBonus) + sumNegative(n -> n.hpBonus));
    }

    public double defMultiplier() {
        return clampMult(1.0 + sumPositive(p -> p.defBonus) + sumNegative(n -> n.defBonus));
    }

    public double dodgeBonus() {
        return sumPositive(p -> p.dodgeBonus) + sumNegative(n -> n.dodgeBonus);
    }

    public double critBonus() {
        return sumPositive(p -> p.critBonus);
    }

    public double goldMultiplier() {
        return 1.0 + sumPositive(p -> p.goldBonus) + sumNegative(n -> n.goldBonus);
    }

    public double xpMultiplier() {
        return 1.0 + sumNegative(n -> n.xpBonus);
    }

    public double dropPenalty() {
        return Math.max(0, sumNegative(n -> n.dropPenalty));
    }

    public double floorHealPercent() {
        return sumPositive(p -> p.floorHealPercent);
    }

    public void printSummary() {
        if (!isActive()) {
            return;
        }
        System.out.println(ConsoleColors.bold("Активные эффекты Абдолбос:"));
        for (Positive p : positives) {
            System.out.println("  " + ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN, "✦ " + p.label));
        }
        for (Negative n : negatives) {
            System.out.println("  " + ConsoleColors.wrap(ConsoleColors.BRIGHT_RED, "✧ " + n.label));
        }
    }

    private double clampMult(double value) {
        return Math.max(MIN_MULT, Math.min(MAX_MULT, value));
    }

    private double sumPositive(java.util.function.Function<Positive, Double> fn) {
        double total = 0;
        for (Positive p : positives) {
            total += fn.apply(p);
        }
        return total;
    }

    private double sumNegative(java.util.function.Function<Negative, Double> fn) {
        double total = 0;
        for (Negative n : negatives) {
            total += fn.apply(n);
        }
        return total;
    }
}
