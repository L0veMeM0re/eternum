package town;

import item.Item;
import player.Player;
import progression.RovingMerchant;
import util.ConsoleColors;
import util.ScreenFrame;

import java.util.List;
import java.util.function.Supplier;

/** Лавка бродячего торговца на площади. */
public final class RovingMerchantShop {
    private RovingMerchantShop() {
    }

    public static void open(Player player, Supplier<String> readLine, Runnable onSave) {
        player.getRovingMerchant().ensureStock(player);
        boolean back = false;
        while (!back) {
            ScreenFrame.openPath(ScreenFrame.Screen.ROVING_MERCHANT, "Главное", "Площадь");
            printBanner();
            System.out.println("  Золото: " + ConsoleColors.wrap(ConsoleColors.YELLOW,
                    String.valueOf(player.getGold())));
            System.out.println(ConsoleColors.dim(
                    "  Ассортимент обновляется после каждого забега в башню."));
            System.out.println();

            List<RovingMerchant.Offer> offers = player.getRovingMerchant().getOffers();
            for (int i = 0; i < offers.size(); i++) {
                printOfferLine(i + 1, offers.get(i), player);
            }

            ScreenFrame.back();
            ScreenFrame.prompt();
            String input = readLine.get().trim();
            if ("0".equals(input)) {
                back = true;
            } else {
                try {
                    int pick = Integer.parseInt(input);
                    if (pick < 1 || pick > offers.size()) {
                        System.out.println("  Неверный выбор.");
                        continue;
                    }
                    tryBuy(player, pick - 1, onSave);
                } catch (NumberFormatException e) {
                    System.out.println("  Неверный выбор.");
                }
            }
        }
    }

    private static void tryBuy(Player player, int index, Runnable onSave) {
        RovingMerchant merchant = player.getRovingMerchant();
        List<RovingMerchant.Offer> offers = merchant.getOffers();
        RovingMerchant.Offer offer = offers.get(index);
        if (offer.sold) {
            System.out.println("  Уже продано.");
            return;
        }
        int price = player.getShopPrice(offer.basePrice);
        if (player.getGold() < price) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.RED, "  Не хватает золота."));
            return;
        }
        Item item = offer.item;
        if (merchant.buy(player, index)) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                    "  Куплено: " + item.getDisplayName()
                            + ConsoleColors.dim(" (" + item.getStatDescription() + ")")
                            + " — " + price + " G"));
            onSave.run();
        } else {
            System.out.println(ConsoleColors.wrap(ConsoleColors.RED,
                    "  Инвентарь полон — освободите место."));
        }
    }

    private static void printOfferLine(int num, RovingMerchant.Offer offer, Player player) {
        if (offer.sold) {
            System.out.println("  " + num + ". " + ConsoleColors.dim("[Продано]"));
            return;
        }
        Item item = offer.item;
        int price = player.getShopPrice(offer.basePrice);
        System.out.println("  " + num + ". " + item.getDisplayName()
                + ConsoleColors.dim(" · " + item.getSlot().getDisplayName()
                + " · " + item.getStatDescription())
                + " — " + ConsoleColors.wrap(ConsoleColors.YELLOW, price + " G"));
    }

    private static void printBanner() {
        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_CYAN,
                "  🎒 Бродячий торговец — трофеи с тракта и башни."));
    }
}
