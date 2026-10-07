package inventory;

import item.Item;
import item.Rarity;
import party.Mercenary;
import party.Party;
import player.CharacterClass;
import player.Player;
import progression.MetaProgression;

public final class InventoryHelper {
    private InventoryHelper() {
    }

    public static int equipBest(Player player, MetaProgression meta) {
        int equipped = 0;
        boolean improved = true;

        while (improved) {
            improved = false;
            for (Equipment.Slot slot : Equipment.Slot.values()) {
                Item current = player.getEquipment().get(slot);
                int currentPower = ItemStats.power(current);

                int bestIndex = -1;
                int bestPower = currentPower;

                Inventory inv = player.getInventory();
                for (int i = 0; i < inv.getSize(); i++) {
                    Item item = inv.get(i);
                    if (item != null && item.getSlot() == slot) {
                        int power = ItemStats.power(item);
                        if (power > bestPower) {
                            bestPower = power;
                            bestIndex = i;
                        }
                    }
                }

                if (bestIndex >= 0 && player.equipItem(bestIndex, meta)) {
                    equipped++;
                    improved = true;
                }
            }
        }
        return equipped;
    }

    /** Экипирует наёмнику лучшие предметы из общего рюкзака игрока. */
    public static int equipBestForMercenary(Player player, Mercenary mercenary, MetaProgression meta,
                                            double runDamageBonus) {
        int equipped = 0;
        boolean improved = true;
        Inventory inv = player.getInventory();
        Equipment eq = mercenary.getEquipment();

        while (improved) {
            improved = false;
            for (Equipment.Slot slot : Equipment.Slot.values()) {
                Item current = eq.get(slot);
                int currentPower = ItemStats.power(current);

                int bestIndex = -1;
                int bestPower = currentPower;

                for (int i = 0; i < inv.getSize(); i++) {
                    Item item = inv.get(i);
                    if (item != null && item.getSlot() == slot) {
                        int power = ItemStats.power(item);
                        if (power > bestPower) {
                            bestPower = power;
                            bestIndex = i;
                        }
                    }
                }

                if (bestIndex >= 0 && mercenary.equipFromInventory(player, bestIndex, meta, runDamageBonus)) {
                    equipped++;
                    improved = true;
                }
            }
        }
        return equipped;
    }

    public static int sellAll(Player player) {
        int count = 0;
        Inventory inv = player.getInventory();
        for (int i = inv.getSize() - 1; i >= 0; i--) {
            if (inv.get(i) != null && player.sellItem(i)) {
                count++;
            }
        }
        return count;
    }

    public static SellResult sellByRarity(Player player, Rarity rarity) {
        int totalGold = 0;
        int count = 0;
        Inventory inv = player.getInventory();
        for (int i = inv.getSize() - 1; i >= 0; i--) {
            Item item = inv.get(i);
            if (item != null && item.getRarity() == rarity) {
                totalGold += item.getSellPrice();
                if (player.sellItem(i)) {
                    count++;
                }
            }
        }
        return new SellResult(count, totalGold);
    }

    public static int deleteAll(Player player) {
        int count = 0;
        Inventory inv = player.getInventory();
        for (int i = inv.getSize() - 1; i >= 0; i--) {
            if (inv.get(i) != null) {
                inv.remove(i);
                count++;
            }
        }
        return count;
    }

    public static int deleteByRarity(Player player, Rarity rarity) {
        int count = 0;
        Inventory inv = player.getInventory();
        for (int i = inv.getSize() - 1; i >= 0; i--) {
            Item item = inv.get(i);
            if (item != null && item.getRarity() == rarity) {
                inv.remove(i);
                count++;
            }
        }
        return count;
    }

    public static int sellJunk(Player player) {
        int count = 0;
        Inventory inv = player.getInventory();
        for (int i = inv.getSize() - 1; i >= 0; i--) {
            Item item = inv.get(i);
            if (item == null) {
                continue;
            }
            if (isJunk(player, item)) {
                if (player.sellItem(i)) {
                    count++;
                }
            }
        }
        return count;
    }

    public static int equipBestForParty(Player player, MetaProgression meta) {
        int total = equipBest(player, meta);
        boolean clericAura = player.getCharacterClass() == CharacterClass.CLERIC;
        double runDmg = player.getRunDamageBonus();
        for (int i = 0; i < Party.MAX_MERC_SLOTS; i++) {
            Mercenary m = player.getParty().getMercenary(i);
            if (m != null) {
                total += equipBestForMercenary(player, m, meta, runDmg);
                m.recalculateStats(meta, runDmg, clericAura);
            }
        }
        return total;
    }

    private static boolean isJunk(Player player, Item item) {
        if (item.getRarity() != Rarity.COMMON) {
            return false;
        }
        Item equipped = player.getEquipment().get(item.getSlot());
        if (equipped == null) {
            return true;
        }
        return ItemStats.power(item) <= ItemStats.power(equipped);
    }

    public static int estimateSellJunkGold(Player player) {
        int gold = 0;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            Item item = inv.get(i);
            if (item != null && isJunk(player, item)) {
                gold += item.getSellPrice();
            }
        }
        return gold;
    }

    public static int countJunk(Player player) {
        int count = 0;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            Item item = inv.get(i);
            if (item != null && isJunk(player, item)) {
                count++;
            }
        }
        return count;
    }

    public static class SellResult {
        public final int count;
        public final int gold;

        public SellResult(int count, int gold) {
            this.count = count;
            this.gold = gold;
        }
    }
}
