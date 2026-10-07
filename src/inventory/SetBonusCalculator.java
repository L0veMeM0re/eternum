package inventory;

import item.Item;
import item.ItemSet;
import item.ItemSetAbility;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public final class SetBonusCalculator {
    private SetBonusCalculator() {
    }

    public static BonusTotals calculate(Equipment equipment) {
        BonusTotals totals = new BonusTotals();
        for (ItemSet set : ItemSet.values()) {
            if (set == ItemSet.NONE) {
                continue;
            }
            int equipped = countEquippedForSet(equipment, set);
            if (equipped <= 0) {
                continue;
            }
            totals.activeSets.add(new ActiveSetInfo(set, equipped));
            applyTiers(totals, set, equipped);
        }
        return totals;
    }

    private static int countEquippedForSet(Equipment equipment, ItemSet set) {
        int count = 0;
        for (Equipment.Slot slot : Equipment.Slot.values()) {
            Item item = equipment.get(slot);
            if (item == null) {
                continue;
            }
            if (set.isRaritySet()) {
                if (item.getRarity() == set.getCountRarity()) {
                    count++;
                }
            } else if (item.getItemSet() == set) {
                count++;
            }
        }
        return count;
    }

    private static void applyTiers(BonusTotals totals, ItemSet set, int equipped) {
        for (ItemSet.SetTier tier : set.getTiers()) {
            if (equipped >= tier.pieces) {
                totals.flatHp += tier.bonusHp;
                totals.flatAtk += tier.bonusAtk;
                totals.flatDef += tier.bonusDef;
                totals.flatCrit += tier.bonusCrit;
                totals.flatCritDmg += tier.bonusCritDmg;
                totals.flatDodge += tier.bonusDodge;
                totals.percentHp += tier.percentHp;
                totals.percentAtk += tier.percentAtk;
                totals.percentDef += tier.percentDef;
                totals.vampirismChance += tier.vampirismChance;
                totals.vampirismHealPercent = Math.max(totals.vampirismHealPercent, tier.vampirismHealPercent);
                if (tier.ability != null) {
                    totals.activeAbilities.add(tier.ability);
                }
            }
        }
    }

    public static final class BonusTotals {
        public int flatHp;
        public int flatAtk;
        public int flatDef;
        public double flatCrit;
        public double flatCritDmg;
        public double flatDodge;
        public double percentHp;
        public double percentAtk;
        public double percentDef;
        public double vampirismChance;
        public double vampirismHealPercent;
        public final EnumSet<ItemSetAbility> activeAbilities = EnumSet.noneOf(ItemSetAbility.class);
        public final List<ActiveSetInfo> activeSets = new ArrayList<>();
    }

    public record ActiveSetInfo(ItemSet set, int equipped) {
    }
}
