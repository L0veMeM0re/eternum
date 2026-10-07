package inventory;

import item.Item;

public final class ItemStats {
    private ItemStats() {
    }

    public static int power(Item item) {
        if (item == null) {
            return 0;
        }
        return item.getAttackBonus() * 3
                + item.getDefenseBonus() * 2
                + item.getHpBonus()
                + (int) (item.getCritBonus() * 2)
                + (int) (item.getCritDamageBonus() * 25)
                + (int) (item.getDodgeBonus() * 2)
                + (int) (item.getVampirismChance() * 3);
    }
}
