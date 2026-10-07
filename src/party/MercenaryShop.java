package party;

import player.Player;

public class MercenaryShop {
    public static final int SLOT_2_COST = 5000;
    public static final int SLOT_3_COST = 15000;
    public static final int SLOT_4_COST = 50000;

    private static final int[] HIRE_COSTS = {1000, 2500, 5000, 10000};
    public static final int MAX_HIRE_TIERS = HIRE_COSTS.length;
    private static final int SELL_REFUND_PERCENT = 50;

    public boolean buySlot(Player player, Party party) {
        int next = party.getUnlockedMercSlots() + 1;
        if (next > Party.MAX_MERC_SLOTS) {
            return false;
        }
        int cost = slotCost(next);
        if (!player.spendGold(cost)) {
            return false;
        }
        party.setUnlockedMercSlots(next);
        return true;
    }

    public static int slotCost(int slotNumber) {
        return switch (slotNumber) {
            case 1 -> SLOT_2_COST;
            case 2 -> SLOT_3_COST;
            case 3 -> SLOT_4_COST;
            default -> 0;
        };
    }

    public boolean sellMercenary(Player player, Party party, int slot) {
        Mercenary m = party.getMercenary(slot);
        if (m == null) {
            return false;
        }
        int refund = HIRE_COSTS[Math.min(slot, HIRE_COSTS.length - 1)] * SELL_REFUND_PERCENT / 100;
        m.unequipAllToInventory(player);
        party.setMercenary(slot, null);
        player.addGold(refund);
        return true;
    }

    public static int hireCost(int offerIndex) {
        return HIRE_COSTS[Math.min(offerIndex, HIRE_COSTS.length - 1)];
    }

}
