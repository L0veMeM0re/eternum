package progression;

import item.Item;
import item.ItemFactory;
import item.Rarity;
import player.Player;
import util.RandomUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Бродячий торговец на площади: 10 случайных предметов, обновление после забега. */
public final class RovingMerchant {
    public static final int OFFER_COUNT = 10;

    private static final int PRICE_COMMON = 22;
    private static final int PRICE_RARE = 48;
    private static final int PRICE_EPIC = 98;

    private final List<Offer> offers = new ArrayList<>();

    public List<Offer> getOffers() {
        return Collections.unmodifiableList(offers);
    }

    public boolean hasStock() {
        return !offers.isEmpty();
    }

    /** Новый ассортимент после забега в подземелье. */
    public void refresh(Player player) {
        offers.clear();
        int itemLevel = itemLevelFor(player);
        for (int i = 0; i < OFFER_COUNT; i++) {
            Rarity rarity = rollRarity();
            Item item = ItemFactory.generateRandomItem(itemLevel, rarity);
            offers.add(new Offer(item, basePrice(rarity, itemLevel), false));
        }
    }

    /** Первый визит до забега — сразу наполнить витрину. */
    public void ensureStock(Player player) {
        if (offers.isEmpty()) {
            refresh(player);
        }
    }

    public boolean buy(Player player, int index) {
        if (index < 0 || index >= offers.size()) {
            return false;
        }
        Offer offer = offers.get(index);
        if (offer.sold || offer.item == null) {
            return false;
        }
        if (!player.spendShopGold(offer.basePrice)) {
            return false;
        }
        if (!player.getInventory().add(offer.item)) {
            player.addGold(player.getShopPrice(offer.basePrice));
            return false;
        }
        offer.sold = true;
        return true;
    }

    public void loadOffers(List<Offer> loaded) {
        offers.clear();
        if (loaded != null) {
            offers.addAll(loaded);
        }
    }

    public static int basePrice(Rarity rarity, int itemLevel) {
        int mult = switch (rarity) {
            case RARE -> PRICE_RARE;
            case EPIC -> PRICE_EPIC;
            default -> PRICE_COMMON;
        };
        return Math.max(100, itemLevel * mult);
    }

    private static int itemLevelFor(Player player) {
        return Math.max(8, player.getLevel() * 2);
    }

    /** Обычно белый/синий; ~5% — фиолетовый. */
    private static Rarity rollRarity() {
        int roll = RandomUtil.range(1, 100);
        if (roll <= 5) {
            return Rarity.EPIC;
        }
        if (roll <= 52) {
            return Rarity.RARE;
        }
        return Rarity.COMMON;
    }

    public static final class Offer {
        public Item item;
        public int basePrice;
        public boolean sold;

        public Offer(Item item, int basePrice, boolean sold) {
            this.item = item;
            this.basePrice = basePrice;
            this.sold = sold;
        }
    }
}
