package item;

import util.ConsoleColors;

/**
 * Сеты по ключевым словам в названии предмета или по редкости (мифик).
 * Порог 2: небольшие статы. Пороги 4 и 6: статы + уникальная способность.
 */
public enum ItemSet {
    NONE("", new String[0], ConsoleColors.GRAY, null),

    DRAGON("Дракон", new String[]{"дракон"}, ConsoleColors.BRIGHT_RED, null,
            tier(2, 12, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, null),
            tier(4, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, ItemSetAbility.DRAGON_FIRST_STRIKE),
            tier(6, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, ItemSetAbility.DRAGON_FURY)),

    SHADOW("Тень", new String[]{"тен", "тьм", "фантом"}, ConsoleColors.BRIGHT_MAGENTA, null,
            tier(2, 0, 0, 0, 2, 0, 2, 0, 0, 0, 0, 0, null),
            tier(4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, ItemSetAbility.SHADOW_EMPOWER),
            tier(6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 12, ItemSetAbility.SHADOW_VAMP_DOT)),

    STORM("Буря", new String[]{"бур", "шторм"}, ConsoleColors.BRIGHT_CYAN, null,
            tier(2, 0, 5, 0, 2, 0, 0, 0, 0, 0, 0, 0, null),
            tier(4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, ItemSetAbility.STORM_CHAIN),
            tier(6, 0, 6, 0, 2, 0, 0, 0, 0, 0, 0, 0, ItemSetAbility.STORM_KILLSTRIKE)),

    ABYSS("Бездна", new String[]{"бездн"}, ConsoleColors.BRIGHT_BLUE, null,
            tier(2, 10, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, null),
            tier(4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 10, ItemSetAbility.ABYSS_WARD),
            tier(6, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, ItemSetAbility.ABYSS_DEEP)),

    CELESTIAL("Созвездие", new String[]{}, ConsoleColors.BRIGHT_YELLOW, Rarity.MYTHIC,
            tier(2, 8, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, null),
            tier(4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, ItemSetAbility.CELESTIAL_RESONANCE),
            tier(6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, ItemSetAbility.CELESTIAL_AEGIS),
            tier(7, 15, 6, 4, 4, 0, 0, 0, 0, 0, 0, 0, null)),

    PHOENIX("Феникс", new String[]{"феникс", "плам"}, ConsoleColors.ORANGE, null,
            tier(2, 8, 3, 0, 0, 0, 0, 0, 0, 0, 2, 10, null),
            tier(4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, ItemSetAbility.PHOENIX_EMBER),
            tier(6, 0, 5, 0, 3, 0, 0, 0, 0, 0, 3, 14, ItemSetAbility.PHOENIX_ASH)),

    TITAN("Титан", new String[]{"титан"}, ConsoleColors.YELLOW, null,
            tier(2, 12, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, null),
            tier(4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, ItemSetAbility.TITAN_STANCE),
            tier(6, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, ItemSetAbility.TITAN_BULWARK)),

    CHAOS("Хаос", new String[]{"хаос", "мироздан", "первоздан", "абсолют"}, ConsoleColors.BRIGHT_RED, null,
            tier(2, 0, 4, 0, 2, 0, 0, 0, 0, 0, 0, 0, null),
            tier(4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, ItemSetAbility.CHAOS_DOUBLE),
            tier(6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, ItemSetAbility.CHAOS_MOMENTUM),
            tier(7, 10, 8, 4, 4, 0.1, 0, 0, 0, 0, 0, 0, null));

    private final String displayName;
    private final String[] nameKeywords;
    private final String color;
    private final Rarity countRarity;
    private final SetTier[] tiers;

    ItemSet(String displayName, String[] nameKeywords, String color, Rarity countRarity) {
        this.displayName = displayName;
        this.nameKeywords = nameKeywords;
        this.color = color;
        this.countRarity = countRarity;
        this.tiers = new SetTier[0];
    }

    ItemSet(String displayName, String[] nameKeywords, String color, Rarity countRarity, SetTier... tiers) {
        this.displayName = displayName;
        this.nameKeywords = nameKeywords;
        this.color = color;
        this.countRarity = countRarity;
        this.tiers = tiers;
    }

    public static ItemSet fromItemName(String name) {
        if (name == null || name.isBlank()) {
            return NONE;
        }
        String lower = name.toLowerCase();
        for (ItemSet set : values()) {
            if (set == NONE) {
                continue;
            }
            for (String keyword : set.nameKeywords) {
                if (lower.contains(keyword)) {
                    return set;
                }
            }
        }
        return NONE;
    }

    private static SetTier tier(int pieces, int hp, int atk, int def,
                               double crit, double critDmg, double dodge,
                               double pctHp, double pctAtk, double pctDef,
                               double vampChance, double vampHeal,
                               ItemSetAbility ability) {
        return new SetTier(pieces, hp, atk, def, crit, critDmg, dodge, pctHp, pctAtk, pctDef,
                vampChance, vampHeal, ability);
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public Rarity getCountRarity() {
        return countRarity;
    }

    public boolean isRaritySet() {
        return countRarity != null;
    }

    public String[] getNameKeywords() {
        return nameKeywords;
    }

    public SetTier[] getTiers() {
        return tiers;
    }

    public int getMaxPieces() {
        if (tiers.length == 0) {
            return 0;
        }
        int max = 0;
        for (SetTier t : tiers) {
            max = Math.max(max, t.pieces);
        }
        return max;
    }

    public static final class SetTier {
        public final int pieces;
        public final int bonusHp;
        public final int bonusAtk;
        public final int bonusDef;
        public final double bonusCrit;
        public final double bonusCritDmg;
        public final double bonusDodge;
        public final double percentHp;
        public final double percentAtk;
        public final double percentDef;
        public final double vampirismChance;
        public final double vampirismHealPercent;
        public final ItemSetAbility ability;

        SetTier(int pieces, int bonusHp, int bonusAtk, int bonusDef,
                double bonusCrit, double bonusCritDmg, double bonusDodge,
                double percentHp, double percentAtk, double percentDef,
                double vampirismChance, double vampirismHealPercent,
                ItemSetAbility ability) {
            this.pieces = pieces;
            this.bonusHp = bonusHp;
            this.bonusAtk = bonusAtk;
            this.bonusDef = bonusDef;
            this.bonusCrit = bonusCrit;
            this.bonusCritDmg = bonusCritDmg;
            this.bonusDodge = bonusDodge;
            this.percentHp = percentHp;
            this.percentAtk = percentAtk;
            this.percentDef = percentDef;
            this.vampirismChance = vampirismChance;
            this.vampirismHealPercent = vampirismHealPercent;
            this.ability = ability;
        }

        public String describe() {
            StringBuilder sb = new StringBuilder(pieces + " шт: ");
            boolean first = true;
            first = appendStat(sb, first, bonusHp > 0, "+" + bonusHp + " HP");
            first = appendStat(sb, first, bonusAtk > 0, "+" + bonusAtk + " ATK");
            first = appendStat(sb, first, bonusDef > 0, "+" + bonusDef + " DEF");
            first = appendStat(sb, first, bonusCrit > 0, "+" + (int) bonusCrit + "% CRIT");
            first = appendStat(sb, first, bonusCritDmg > 0,
                    "+" + String.format("%.1f", bonusCritDmg) + "x CRIT DMG");
            first = appendStat(sb, first, bonusDodge > 0, "+" + (int) bonusDodge + "% DODGE");
            first = appendStat(sb, first, percentHp > 0 || percentAtk > 0 || percentDef > 0,
                    "+" + (int) percentHp + "/" + (int) percentAtk + "/" + (int) percentDef + "% HP/ATK/DEF");
            first = appendStat(sb, first, vampirismChance > 0,
                    "+" + (int) vampirismChance + "% вамп (" + (int) vampirismHealPercent + "% урона)");
            if (ability != null) {
                if (!first) {
                    sb.append(" | ");
                }
                sb.append("✦ ").append(ability.formatLine());
            }
            return sb.toString();
        }

        private boolean appendStat(StringBuilder sb, boolean first, boolean condition, String part) {
            if (!condition) {
                return first;
            }
            if (!first) {
                sb.append(", ");
            }
            sb.append(part);
            return false;
        }
    }
}
