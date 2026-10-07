package inventory;

import item.Item;

public class Equipment {
    public enum Slot {
        WEAPON("Оружие"),
        HELMET("Шлем"),
        ARMOR("Броня"),
        GLOVES("Перчатки"),
        BOOTS("Сапоги"),
        RING("Кольцо"),
        AMULET("Амулет");

        private final String displayName;

        Slot(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final Item[] slots = new Item[Slot.values().length];

    public Item get(Slot slot) {
        return slots[slot.ordinal()];
    }

    public void equip(Slot slot, Item item) {
        slots[slot.ordinal()] = item;
    }

    public Item unequip(Slot slot) {
        Item previous = slots[slot.ordinal()];
        slots[slot.ordinal()] = null;
        return previous;
    }

    public int totalAttackBonus() {
        int total = 0;
        for (Item item : slots) {
            if (item != null) {
                total += item.getAttackBonus();
            }
        }
        return total;
    }

    public int totalDefenseBonus() {
        int total = 0;
        for (Item item : slots) {
            if (item != null) {
                total += item.getDefenseBonus();
            }
        }
        return total;
    }

    public int totalHpBonus() {
        int total = 0;
        for (Item item : slots) {
            if (item != null) {
                total += item.getHpBonus();
            }
        }
        return total;
    }

    public double totalCritBonus() {
        double total = 0;
        for (Item item : slots) {
            if (item != null) {
                total += item.getCritBonus();
            }
        }
        return total;
    }

    public double totalCritDamageBonus() {
        double total = 0;
        for (Item item : slots) {
            if (item != null) {
                total += item.getCritDamageBonus();
            }
        }
        return total;
    }

    public double totalDodgeBonus() {
        double total = 0;
        for (Item item : slots) {
            if (item != null) {
                total += item.getDodgeBonus();
            }
        }
        return total;
    }

    public double totalVampirismChance() {
        double total = 0;
        for (Item item : slots) {
            if (item != null) {
                total += item.getVampirismChance();
            }
        }
        return total;
    }

    public double maxVampirismHealPercent() {
        double max = 0;
        for (Item item : slots) {
            if (item != null && item.hasVampirism()) {
                max = Math.max(max, item.getVampirismHealPercent());
            }
        }
        return max;
    }

    public void clear() {
        for (int i = 0; i < slots.length; i++) {
            slots[i] = null;
        }
    }
}
