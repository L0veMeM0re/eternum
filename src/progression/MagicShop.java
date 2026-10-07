package progression;

import player.Player;

public class MagicShop {
    public static final int ABDOLBOS_COST = 750;
    public static final int ASCENSION_KEY_COST = 67676;
    public static final int ASCENSION_START_FLOOR = 50;

    /** Макс. свитков каждого типа в запасе (как у лавки золота). */
    public static final int MAX_MAGIC_SCROLLS = 4;

    public static final int LUCK_SCROLL_COST = 480;
    /** +10% к шансу предмета с моба за забег. */
    public static final int LUCK_DROP_BONUS_PERCENT = 10;

    public static final int GREED_SCROLL_COST = 450;
    /** +25% золота за забег (не стакается с зельем жадности — отдельный множитель). */
    public static final double GREED_GOLD_BONUS = 0.25;

    public static final int FOG_SCROLL_COST = 620;
    /** Первые N этажей: слабее враги и меньше в пачке. */
    public static final int FOG_SCROLL_FLOORS = 15;
    /** ATK/DEF/HP врагов на туманных этажах (%). */
    public static final int FOG_ENEMY_POWER_PERCENT = 72;

    public static final int SECOND_BREATH_SCROLL_COST = 1380;
    /** HP при воскрешении (% от макс.). */
    public static final int SECOND_BREATH_HEAL_PERCENT = 25;

    public boolean buyAbdolbosPotion(Player player) {
        if (!player.spendGold(ABDOLBOS_COST)) {
            return false;
        }
        player.addAbdolbosPotion();
        return true;
    }

    public boolean buyAscensionKey(Player player) {
        if (player.hasAscensionUnlock()) {
            return false;
        }
        if (!player.spendGold(ASCENSION_KEY_COST)) {
            return false;
        }
        player.unlockAscension();
        return true;
    }

    public boolean buyLuckScroll(Player player) {
        if (player.getLuckScrolls() >= MAX_MAGIC_SCROLLS || !player.spendGold(LUCK_SCROLL_COST)) {
            return false;
        }
        player.addLuckScroll(1, MAX_MAGIC_SCROLLS);
        return true;
    }

    public boolean buyGreedScroll(Player player) {
        if (player.getGreedScrolls() >= MAX_MAGIC_SCROLLS || !player.spendGold(GREED_SCROLL_COST)) {
            return false;
        }
        player.addGreedScroll(1, MAX_MAGIC_SCROLLS);
        return true;
    }

    public boolean buyFogScroll(Player player) {
        if (player.getFogScrolls() >= MAX_MAGIC_SCROLLS || !player.spendGold(FOG_SCROLL_COST)) {
            return false;
        }
        player.addFogScroll(1, MAX_MAGIC_SCROLLS);
        return true;
    }

    public boolean buySecondBreathScroll(Player player) {
        if (player.getSecondBreathScrolls() >= MAX_MAGIC_SCROLLS || !player.spendGold(SECOND_BREATH_SCROLL_COST)) {
            return false;
        }
        player.addSecondBreathScroll(1, MAX_MAGIC_SCROLLS);
        return true;
    }
}
