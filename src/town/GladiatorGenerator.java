package town;

import inventory.Equipment;
import inventory.SetBonusCalculator;
import item.Armor;
import item.ItemSet;
import item.Rarity;
import party.MercenaryNames;
import player.CharacterClass;
import player.Stats;
import util.RandomUtil;

public final class GladiatorGenerator {
    private GladiatorGenerator() {
    }

    public static GladiatorMatch generate() {
        boolean twoVsTwo = RandomUtil.chance(50);
        GladiatorMatch.PowerTier tier = pickTier();
        GladiatorSide red = generateSide("Красные", util.ConsoleColors.BRIGHT_RED, twoVsTwo ? 2 : 1, tier);
        GladiatorSide blue = generateSide("Синие", util.ConsoleColors.BRIGHT_CYAN, twoVsTwo ? 2 : 1, tier);
        balanceSides(red, blue, tier);
        return new GladiatorMatch(twoVsTwo, tier, red, blue);
    }

    private static GladiatorMatch.PowerTier pickTier() {
        int roll = RandomUtil.range(1, 100);
        if (roll <= 30) {
            return GladiatorMatch.PowerTier.WEAK;
        }
        if (roll <= 75) {
            return GladiatorMatch.PowerTier.MEDIUM;
        }
        return GladiatorMatch.PowerTier.STRONG;
    }

    private static GladiatorSide generateSide(String label, String color, int count,
                                              GladiatorMatch.PowerTier tier) {
        java.util.List<Gladiator> list = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(generateFighter(tier));
        }
        return new GladiatorSide(label, color, list);
    }

    private static void balanceSides(GladiatorSide red, GladiatorSide blue, GladiatorMatch.PowerTier tier) {
        double target = (red.powerRating() + blue.powerRating()) / 2.0;
        nudgeSide(red, target / Math.max(1, red.getFighters().size()), tier);
        nudgeSide(blue, target / Math.max(1, blue.getFighters().size()), tier);
    }

    private static void nudgeSide(GladiatorSide side, double targetPerFighter, GladiatorMatch.PowerTier tier) {
        for (Gladiator g : side.getFighters()) {
            double current = g.powerRating();
            double ratio = targetPerFighter / Math.max(1, current);
            ratio = Math.max(0.93, Math.min(1.07, ratio));
            if (Math.abs(ratio - 1.0) < 0.02) {
                continue;
            }
            Stats s = g.getStats();
            s.setMaxHp(Math.max(40, (int) (s.getMaxHp() * ratio)));
            s.setCurrentHp(s.getMaxHp());
            s.setAttack(Math.max(5, (int) (s.getAttack() * ratio)));
            s.setDefense(Math.max(1, (int) (s.getDefense() * ratio)));
        }
    }

    private static Gladiator generateFighter(GladiatorMatch.PowerTier tier) {
        CharacterClass[] classes = CharacterClass.values();
        CharacterClass cls = classes[RandomUtil.range(0, classes.length - 1)];
        Stats base = cls.createBaseStats();
        applyTier(base, tier);
        ItemSet set = randomSet();
        int pieces = RandomUtil.range(2, Math.min(6, set.getMaxPieces() > 0 ? set.getMaxPieces() : 6));
        applySetBonuses(base, set, pieces);
        String name = MercenaryNames.randomName();
        return new Gladiator(name, cls, base, set, pieces);
    }

    private static ItemSet randomSet() {
        ItemSet[] pool = java.util.Arrays.stream(ItemSet.values())
                .filter(s -> s != ItemSet.NONE)
                .toArray(ItemSet[]::new);
        return pool[RandomUtil.range(0, pool.length - 1)];
    }

    private static void applyTier(Stats stats, GladiatorMatch.PowerTier tier) {
        double mult = switch (tier) {
            case WEAK -> 0.72 + RandomUtil.range(0, 8) / 100.0;
            case MEDIUM -> 1.0 + RandomUtil.range(-5, 5) / 100.0;
            case STRONG -> 1.38 + RandomUtil.range(0, 10) / 100.0;
        };
        stats.setMaxHp(Math.max(50, (int) (stats.getMaxHp() * mult)));
        stats.setCurrentHp(stats.getMaxHp());
        stats.setAttack(Math.max(8, (int) (stats.getAttack() * mult)));
        stats.setDefense(Math.max(2, (int) (stats.getDefense() * mult)));
        stats.setCritChance(Math.min(35, stats.getCritChance() + tier.ordinal() * 2.0));
        stats.setDodgeChance(Math.min(25, stats.getDodgeChance() + tier.ordinal()));
    }

    private static void applySetBonuses(Stats stats, ItemSet set, int pieces) {
        Equipment eq = mockEquipment(set, pieces);
        SetBonusCalculator.BonusTotals totals = SetBonusCalculator.calculate(eq);
        stats.addMaxHp(totals.flatHp + (int) (stats.getMaxHp() * totals.percentHp));
        stats.setCurrentHp(stats.getMaxHp());
        stats.addAttack(totals.flatAtk + (int) (stats.getAttack() * totals.percentAtk));
        stats.addDefense(totals.flatDef + (int) (stats.getDefense() * totals.percentDef));
        stats.addCritChance(totals.flatCrit);
        stats.addCritDamage(totals.flatCritDmg);
        stats.addDodgeChance(totals.flatDodge);
    }

    private static Equipment mockEquipment(ItemSet set, int pieces) {
        Equipment eq = new Equipment();
        Equipment.Slot[] slots = Equipment.Slot.values();
        Rarity rarity = set.isRaritySet() ? set.getCountRarity() : Rarity.EPIC;
        String keyword = setKeyword(set);
        for (int i = 0; i < pieces && i < slots.length; i++) {
            eq.equip(slots[i], new Armor("Клинок " + keyword, rarity, slots[i], 0, 0, 0, 0, 0, 0));
        }
        return eq;
    }

    private static String setKeyword(ItemSet set) {
        return switch (set) {
            case DRAGON -> "дракона";
            case SHADOW -> "тени";
            case STORM -> "бури";
            case ABYSS -> "бездны";
            case CELESTIAL -> "звёзд";
            case PHOENIX -> "феникса";
            case TITAN -> "титана";
            case CHAOS -> "хаоса";
            default -> "героя";
        };
    }
}
