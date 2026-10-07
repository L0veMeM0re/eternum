package quest;

import inventory.Equipment;
import inventory.Inventory;
import item.Item;
import item.Rarity;
import party.Mercenary;
import party.Party;
import player.Player;
import util.ConsoleColors;
import util.ScreenFrame;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class QuestItemTurnIn {
    private QuestItemTurnIn() {
    }

    public static boolean pickAndRemove(Player player, Equipment.Slot slot, Supplier<String> readLine) {
        List<Integer> indices = listEligible(player, slot);
        if (indices.isEmpty()) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.GRAY,
                    "  В инвентаре нет подходящих предметов (только не надетые)."));
            pause(readLine);
            return false;
        }

        ScreenFrame.open(ScreenFrame.Screen.VILLAGE, "Сдача предмета");
        String slotLabel = slot == Equipment.Slot.WEAPON ? "оружие" : "нагрудник (броня)";
        System.out.println("  Выберите " + slotLabel + " из инвентаря:");
        System.out.println();
        Inventory inv = player.getInventory();
        for (int i = 0; i < indices.size(); i++) {
            Item item = inv.get(indices.get(i));
            System.out.println("  " + (i + 1) + ". " + item.getDisplayName()
                    + ConsoleColors.dim(" — " + item.getStatDescription()));
        }
        ScreenFrame.cancel();
        ScreenFrame.prompt();
        String choice = readLine.get().trim();
        if ("0".equals(choice)) {
            return false;
        }
        try {
            int menuIndex = Integer.parseInt(choice) - 1;
            if (menuIndex < 0 || menuIndex >= indices.size()) {
                System.out.println("  Неверный выбор.");
                pause(readLine);
                return false;
            }
            int invIndex = indices.get(menuIndex);
            Item removed = inv.remove(invIndex);
            if (removed == null) {
                System.out.println("  Предмет уже недоступен.");
                pause(readLine);
                return false;
            }
            System.out.println(ConsoleColors.wrap(ConsoleColors.GREEN,
                    "  Сдано: " + removed.getDisplayName()));
            pause(readLine);
            return true;
        } catch (NumberFormatException e) {
            System.out.println("  Неверный выбор.");
            pause(readLine);
            return false;
        }
    }

    private static List<Integer> listEligible(Player player, Equipment.Slot slot) {
        List<Integer> indices = new ArrayList<>();
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            Item item = inv.get(i);
            if (item != null && item.getSlot() == slot && !isEquipped(player, item)) {
                indices.add(i);
            }
        }
        return indices;
    }

    /** Предмет слота с редкостью не ниже {@code minimum}, не надетый. */
    public static boolean pickMinRarityForSlot(Player player, Equipment.Slot slot, Rarity minimum,
                                               Supplier<String> readLine) {
        List<Integer> indices = new ArrayList<>();
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            Item item = inv.get(i);
            if (item != null && item.getSlot() == slot && item.getRarity().isAtLeast(minimum)
                    && !isEquipped(player, item)) {
                indices.add(i);
            }
        }
        if (indices.isEmpty()) {
            String slotLabel = slot == Equipment.Slot.WEAPON ? "оружие" : "предмет";
            System.out.println(ConsoleColors.wrap(ConsoleColors.GRAY,
                    "  Нет " + slotLabel + " [" + minimum.getColorName() + "]+ в сумке (не надетые)."));
            pause(readLine);
            return false;
        }
        ScreenFrame.open(ScreenFrame.Screen.VILLAGE, "Сдача предмета");
        System.out.println("  Выберите " + (slot == Equipment.Slot.WEAPON ? "оружие" : "предмет")
                + " [" + minimum.getColorName() + "]+ из сумки:");
        System.out.println();
        for (int i = 0; i < indices.size(); i++) {
            Item item = inv.get(indices.get(i));
            System.out.println("  " + (i + 1) + ". " + item.getDisplayName()
                    + ConsoleColors.dim(" — " + item.getStatDescription()));
        }
        ScreenFrame.cancel();
        ScreenFrame.prompt();
        String choice = readLine.get().trim();
        if ("0".equals(choice)) {
            return false;
        }
        try {
            int menuIndex = Integer.parseInt(choice) - 1;
            if (menuIndex < 0 || menuIndex >= indices.size()) {
                System.out.println("  Неверный выбор.");
                pause(readLine);
                return false;
            }
            Item removed = inv.remove(indices.get(menuIndex));
            if (removed == null) {
                System.out.println("  Предмет уже недоступен.");
                pause(readLine);
                return false;
            }
            System.out.println(ConsoleColors.wrap(ConsoleColors.GREEN,
                    "  Сдано: " + removed.getDisplayName()));
            pause(readLine);
            return true;
        } catch (NumberFormatException e) {
            System.out.println("  Неверный выбор.");
            pause(readLine);
            return false;
        }
    }

    private static boolean isEquipped(Player player, Item item) {
        for (Equipment.Slot s : Equipment.Slot.values()) {
            if (player.getEquipment().get(s) == item) {
                return true;
            }
        }
        for (int i = 0; i < Party.MAX_MERC_SLOTS; i++) {
            Mercenary m = player.getParty().getMercenary(i);
            if (m == null) {
                continue;
            }
            for (Equipment.Slot s : Equipment.Slot.values()) {
                if (m.getEquipment().get(s) == item) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Предмет любого слота с редкостью не ниже {@code minimum}, не надетый. */
    public static boolean pickMinRarityAnySlot(Player player, Rarity minimum, Supplier<String> readLine) {
        List<Integer> indices = listEligibleByRarity(player, minimum);
        if (indices.isEmpty()) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.GRAY,
                    "  В сумке нет подходящих вещей (нужна аура [" + minimum.getColorName() + "]+, не надетые)."));
            pause(readLine);
            return false;
        }

        ScreenFrame.open(ScreenFrame.Screen.VILLAGE, "Сдача предмета");
        System.out.println("  Выберите вещь с сильной аурой из сумки (не надетую):");
        System.out.println(ConsoleColors.dim("  [Подсказка: " + minimum.getColorName() + "+]"));
        System.out.println();
        Inventory inv = player.getInventory();
        for (int i = 0; i < indices.size(); i++) {
            Item item = inv.get(indices.get(i));
            System.out.println("  " + (i + 1) + ". " + item.getDisplayName()
                    + ConsoleColors.dim(" — " + item.getStatDescription()));
        }
        ScreenFrame.cancel();
        ScreenFrame.prompt();
        String choice = readLine.get().trim();
        if ("0".equals(choice)) {
            return false;
        }
        try {
            int menuIndex = Integer.parseInt(choice) - 1;
            if (menuIndex < 0 || menuIndex >= indices.size()) {
                System.out.println("  Неверный выбор.");
                pause(readLine);
                return false;
            }
            int invIndex = indices.get(menuIndex);
            Item removed = inv.remove(invIndex);
            if (removed == null) {
                System.out.println("  Предмет уже недоступен.");
                pause(readLine);
                return false;
            }
            System.out.println(ConsoleColors.wrap(ConsoleColors.GREEN,
                    "  Сдано: " + removed.getDisplayName()));
            pause(readLine);
            return true;
        } catch (NumberFormatException e) {
            System.out.println("  Неверный выбор.");
            pause(readLine);
            return false;
        }
    }

    private static List<Integer> listEligibleByRarity(Player player, Rarity minimum) {
        List<Integer> indices = new ArrayList<>();
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            Item item = inv.get(i);
            if (item != null && item.getRarity().isAtLeast(minimum) && !isEquipped(player, item)) {
                indices.add(i);
            }
        }
        return indices;
    }

    private static void pause(Supplier<String> readLine) {
        System.out.println("  Enter...");
        readLine.get();
    }
}
