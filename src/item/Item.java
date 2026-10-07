package item;

import inventory.Equipment;
import util.ConsoleColors;

public abstract class Item {
    private final String name;
    private final Rarity rarity;
    private final int sellPrice;
    private final ItemSet itemSet;
    private final double vampirismChance;
    private final double vampirismHealPercent;

    protected Item(String name, Rarity rarity, int sellPrice) {
        this(name, rarity, sellPrice, ItemSet.fromItemName(name), 0, 0);
    }

    protected Item(String name, Rarity rarity, int sellPrice, ItemSet itemSet,
                   double vampirismChance, double vampirismHealPercent) {
        this.name = name;
        this.rarity = rarity;
        this.sellPrice = sellPrice;
        this.itemSet = itemSet != null ? itemSet : ItemSet.NONE;
        this.vampirismChance = Math.max(0, vampirismChance);
        this.vampirismHealPercent = Math.max(0, vampirismHealPercent);
    }

    public String getName() {
        return name;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public int getSellPrice() {
        return sellPrice;
    }

    public ItemSet getItemSet() {
        return itemSet;
    }

    public double getVampirismChance() {
        return vampirismChance;
    }

    public double getVampirismHealPercent() {
        return vampirismHealPercent;
    }

    public boolean hasVampirism() {
        return vampirismChance > 0 && vampirismHealPercent > 0;
    }

    public abstract Equipment.Slot getSlot();

    public abstract String getStatDescription();

    public abstract int getAttackBonus();

    public abstract int getDefenseBonus();

    public abstract double getCritBonus();

    public abstract double getCritDamageBonus();

    public abstract double getDodgeBonus();

    public abstract int getHpBonus();

    public String getDisplayName() {
        String itemName = rarity.formatName(name);
        if (itemSet == ItemSet.NONE) {
            return itemName;
        }
        return ConsoleColors.wrap(itemSet.getColor(), "[" + itemSet.getDisplayName() + "] ") + itemName;
    }
}
