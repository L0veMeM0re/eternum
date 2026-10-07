package progression;

import item.Item;
import item.ItemFactory;
import item.Rarity;
import player.Player;
import util.RandomUtil;

public class GoldShop {
    public static final int MAX_SCROLLS = 4;
    public static final int BLESSING_COST = 350;
    public static final int XP_BOOST_COST = 400;
    public static final int CHEST_COST = 550;
    public static final int TRAIN_HP_COST = 800;
    public static final int TRAIN_ATK_COST = 950;
    public static final int EXPAND_INV_COST = 600;
    /** +10 HP за одну покупку */
    public static final int TRAIN_HP_PER_PURCHASE = 10;
    /** Макс. число покупок тренировки HP (не сумма HP!) */
    public static final int MAX_TRAIN_HP = 15;
    public static final int MAX_TRAIN_ATK = 15;
    public static final int MAX_INV_EXPANSIONS = 10;

    private Item lastChestItem;

    public Item getLastChestItem() {
        return lastChestItem;
    }

    public boolean buyBlessing(Player player) {
        if (player.getBlessingScrolls() >= MAX_SCROLLS || !player.spendShopGold(BLESSING_COST)) {
            return false;
        }
        player.addBlessingScroll(1, MAX_SCROLLS);
        return true;
    }

    public boolean buyXpBoost(Player player) {
        if (player.getXpScrolls() >= MAX_SCROLLS || !player.spendShopGold(XP_BOOST_COST)) {
            return false;
        }
        player.addXpScroll(1, MAX_SCROLLS);
        return true;
    }

    public boolean buyMysteryChest(Player player) {
        if (!player.spendShopGold(CHEST_COST)) {
            return false;
        }
        Rarity rarity = RandomUtil.chance(3) ? Rarity.LEGENDARY
                : RandomUtil.chance(15) ? Rarity.EPIC
                : RandomUtil.chance(45) ? Rarity.RARE : Rarity.COMMON;
        lastChestItem = ItemFactory.generateRandomItem(Math.max(10, player.getLevel() * 2), rarity);
        if (!player.getInventory().add(lastChestItem)) {
            player.addGold(player.getShopPrice(CHEST_COST));
            lastChestItem = null;
            return false;
        }
        return true;
    }

    public static int hpTrainingPurchases(Player player) {
        return player.getGoldTrainingHp() / TRAIN_HP_PER_PURCHASE;
    }

    public boolean buyHpTraining(Player player) {
        if (hpTrainingPurchases(player) >= MAX_TRAIN_HP || !player.spendShopGold(TRAIN_HP_COST)) {
            return false;
        }
        player.addGoldTrainingHp(TRAIN_HP_PER_PURCHASE);
        player.getBaseStats().addMaxHp(TRAIN_HP_PER_PURCHASE);
        return true;
    }

    public boolean buyAtkTraining(Player player) {
        if (player.getGoldTrainingAtk() >= MAX_TRAIN_ATK || !player.spendShopGold(TRAIN_ATK_COST)) {
            return false;
        }
        player.addGoldTrainingAtk(1);
        player.getBaseStats().addAttack(1);
        return true;
    }

    public boolean buyInventoryExpansion(Player player) {
        if (player.getInventoryExpansions() >= MAX_INV_EXPANSIONS || !player.spendShopGold(EXPAND_INV_COST)) {
            return false;
        }
        player.addInventoryExpansion();
        return true;
    }
}
