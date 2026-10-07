package item;

import util.GemRenderer;

/** Сокетный камень для оружия. statMultiplier > 1 — бонус от рискованной ковки. */
public final class Gem {
    private final GemType type;
    private final GemTier tier;
    private final double statMultiplier;

    public Gem(GemType type, GemTier tier) {
        this(type, tier, 1.0);
    }

    public Gem(GemType type, GemTier tier, double statMultiplier) {
        this.type = type;
        this.tier = tier;
        this.statMultiplier = Math.max(1.0, statMultiplier);
    }

    public GemType getType() {
        return type;
    }

    public GemTier getTier() {
        return tier;
    }

    public double getStatMultiplier() {
        return statMultiplier;
    }

    public Gem withTier(GemTier newTier) {
        return new Gem(type, newTier, statMultiplier);
    }

    public Gem withBonusMultiplier(double bonus) {
        return new Gem(type, tier, statMultiplier * bonus);
    }

    public int atkBonus() {
        if (type != GemType.RUBY) {
            return 0;
        }
        return scale(baseAtk(tier));
    }

    public double critBonus() {
        if (type != GemType.SAPPHIRE) {
            return 0;
        }
        return scale(baseCrit(tier));
    }

    public int hpBonus() {
        if (type != GemType.EMERALD) {
            return 0;
        }
        return scale(baseHp(tier));
    }

    public double critDamageBonus() {
        if (type != GemType.OBSIDIAN) {
            return 0;
        }
        return scale(baseCritDmg(tier));
    }

    public double vampChance() {
        if (type != GemType.AMBER) {
            return 0;
        }
        return scale(baseVamp(tier));
    }

    public String procDescription() {
        if (type != GemType.SHARD) {
            return "";
        }
        return switch (tier) {
            case I -> "каждый 6-й удар: +10% урона";
            case II -> "при крите: лечишь 4% от урона удара";
            case III -> "каждый 5-й удар: +18% урона";
        };
    }

    private static int baseAtk(GemTier tier) {
        return switch (tier) {
            case I -> 3;
            case II -> 6;
            case III -> 10;
        };
    }

    private static double baseCrit(GemTier tier) {
        return switch (tier) {
            case I -> 2;
            case II -> 4;
            case III -> 7;
        };
    }

    private static int baseHp(GemTier tier) {
        return switch (tier) {
            case I -> 15;
            case II -> 30;
            case III -> 50;
        };
    }

    private static double baseCritDmg(GemTier tier) {
        return switch (tier) {
            case I -> 0.1;
            case II -> 0.2;
            case III -> 0.35;
        };
    }

    private static double baseVamp(GemTier tier) {
        return switch (tier) {
            case I -> 2;
            case II -> 4;
            case III -> 6;
        };
    }

    private int scale(int base) {
        return Math.max(1, (int) Math.round(base * statMultiplier));
    }

    private double scale(double base) {
        return base * statMultiplier;
    }

    public String describe() {
        return GemRenderer.formatDescribe(this);
    }

    public String getDisplayName() {
        return GemRenderer.formatName(this);
    }

    public static Gem randomTierOne() {
        GemType[] types = GemType.values();
        GemType type = types[util.RandomUtil.range(0, types.length - 1)];
        return new Gem(type, GemTier.I);
    }
}
