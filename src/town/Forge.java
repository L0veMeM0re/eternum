package town;

import forge.ForgeCosts;
import forge.ForgeRiskMode;
import forge.ForgeService;
import item.Gem;
import item.GemTier;
import item.GemType;
import item.Item;
import item.Weapon;
import party.Mercenary;
import party.Party;
import player.Player;
import progression.MetaProgression;
import quest.QuestId;
import quest.QuestLog;
import quest.QuestService;
import util.ConsoleColors;
import util.ScreenFrame;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Кузница на площади — гемы, слияние, сокеты. */
public final class Forge {
    private Forge() {
    }

    public static void open(Player player, MetaProgression meta, Supplier<String> readLine, Runnable onSave) {
        boolean back = false;
        while (!back) {
            ScreenFrame.openPath(ScreenFrame.Screen.FORGE, "Главное", "Площадь");
            printBanner();
            ForgeRenderer.printResources(player, player.getGemStash().size(), player.getMaxGemStashSize());
            ForgeRenderer.printQuickGuide();
            ForgeRenderer.printMainMenu();
            int forgeMasterChoice = -1;
            if (player.getQuestLog().isForgeMasterUnlocked()) {
                forgeMasterChoice = 7;
                System.out.println("  7. Орис (поручения)");
            }
            ScreenFrame.back();
            ScreenFrame.prompt();
            String choice = readLine.get().trim();
            if ("0".equals(choice)) {
                back = true;
            } else if ("1".equals(choice)) {
                smelt(player, readLine, onSave);
            } else if ("2".equals(choice)) {
                merge(player, readLine, onSave);
            } else if ("3".equals(choice)) {
                socket(player, meta, readLine, onSave);
            } else if ("4".equals(choice)) {
                extract(player, meta, readLine, onSave);
            } else if ("5".equals(choice)) {
                viewStash(player, readLine);
            } else if ("6".equals(choice)) {
                viewCodex(readLine);
            } else if (forgeMasterChoice > 0 && String.valueOf(forgeMasterChoice).equals(choice)) {
                talkForgeMaster(player, readLine, onSave);
            } else {
                System.out.println("  Неверный выбор.");
            }
        }
    }

    private static void smelt(Player player, Supplier<String> readLine, Runnable onSave) {
        System.out.println();
        ForgeRenderer.printSmeltConfirm(player);
        System.out.println();
        System.out.println("  1. Переплавить");
        ScreenFrame.cancel();
        System.out.print("  ▶ ");
        if (!"1".equals(readLine.get().trim())) {
            return;
        }
        ForgeService.ForgeResult result = ForgeService.smeltOre(player);
        printResult(result.success(), result.message());
        if (result.success()) {
            QuestService.onSuccessfulSmelt(player);
            onSave.run();
        }
        pause(readLine);
    }

    private static void merge(Player player, Supplier<String> readLine, Runnable onSave) {
        System.out.println();
        ForgeRenderer.printMergeHeader();
        if (player.getGemStash().isEmpty()) {
            ForgeRenderer.printStash(player);
            pause(readLine);
            return;
        }
        ForgeRenderer.printStash(player);
        GemType type = pickGemType(readLine);
        if (type == null) {
            return;
        }
        GemTier tier = pickFromTier(readLine);
        if (tier == null || tier == GemTier.III) {
            System.out.println(ConsoleColors.dim("  Отмена."));
            pause(readLine);
            return;
        }
        int cost = tier == GemTier.I ? ForgeCosts.MERGE_TO_II : ForgeCosts.MERGE_TO_III;
        ForgeRiskMode mode = pickMergeRisk(readLine, cost);
        if (mode == null) {
            return;
        }
        System.out.println(ConsoleColors.dim("\n  ── Результат ──"));
        ForgeService.MergeResult result = ForgeService.mergeGems(player, type, tier, mode);
        printResult(result.success(), result.message());
        onSave.run();
        pause(readLine);
    }

    private static void socket(Player player, MetaProgression meta, Supplier<String> readLine, Runnable onSave) {
        System.out.println();
        ForgeRenderer.printSocketHeader();
        List<WeaponRef> weapons = collectWeapons(player);
        if (weapons.isEmpty()) {
            System.out.println(ConsoleColors.dim("      Нет оружия EPIC+ с гнёздами."));
            pause(readLine);
            return;
        }
        if (player.getGemStash().isEmpty()) {
            System.out.println(ConsoleColors.dim("      Сундук пуст — сначала переплави руду."));
            pause(readLine);
            return;
        }
        WeaponRef ref = pickWeapon(weapons, readLine);
        if (ref == null) {
            return;
        }
        Weapon weapon = ref.weapon();
        if (!weapon.hasEmptySocket()) {
            System.out.println(ConsoleColors.dim("      Все гнёзда заняты."));
            pause(readLine);
            return;
        }
        ForgeRenderer.printStash(player);
        Gem gem = pickGem(player, readLine, weapon);
        if (gem == null) {
            return;
        }
        ForgeRiskMode mode = pickInsertRisk(readLine);
        if (mode == null) {
            return;
        }
        int socket = weapon.firstEmptySocketIndex();
        ForgeService.ForgeResult result = ForgeService.insertGem(player, weapon, socket, gem, mode);
        System.out.println(ConsoleColors.dim("\n  ── Результат ──"));
        if (!result.success()) {
            printResult(false, result.message());
            onSave.run();
            pause(readLine);
            return;
        }
        Weapon updated = weapon.withSocket(socket, result.resultGem());
        ref.apply(updated);
        player.recalculateStats(meta);
        player.getParty().recalculateAll(player, meta);
        QuestService.onSuccessfulGemInsert(player);
        onSave.run();
        printResult(true, result.message());
        pause(readLine);
    }

    private static void extract(Player player, MetaProgression meta, Supplier<String> readLine, Runnable onSave) {
        System.out.println();
        ForgeRenderer.printExtractHeader();
        List<WeaponRef> weapons = collectSocketedWeapons(player);
        if (weapons.isEmpty()) {
            System.out.println(ConsoleColors.dim("      Нет оружия с вставленными камнями."));
            pause(readLine);
            return;
        }
        WeaponRef ref = pickWeapon(weapons, readLine);
        if (ref == null) {
            return;
        }
        Weapon weapon = ref.weapon();
        ForgeRenderer.printSocketPickerHeader();
        int socket = pickSocketIndex(weapon, readLine);
        if (socket < 0) {
            return;
        }
        Gem gem = weapon.getSocket(socket);
        if (gem == null) {
            return;
        }
        System.out.println(ConsoleColors.dim("\n  ── Результат ──"));
        ForgeService.ForgeResult result = ForgeService.extractGem(player, gem);
        if (result.success()) {
            ref.apply(weapon.withSocketCleared(socket));
            player.recalculateStats(meta);
            player.getParty().recalculateAll(player, meta);
        }
        printResult(result.success(), result.message());
        onSave.run();
        pause(readLine);
    }

    private static void viewStash(Player player, Supplier<String> readLine) {
        System.out.println();
        ForgeRenderer.printStash(player);
        pause(readLine);
    }

    private static void viewCodex(Supplier<String> readLine) {
        System.out.println();
        ForgeRenderer.printGemCodex();
        pause(readLine);
    }

    private static void printBanner() {
        System.out.println();
        System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                "  ╔══════════════════════════════════════╗"));
        System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                "  ║   🔨  К У З Н И Ц А  Г Е М О В  🔨   ║"));
        System.out.println(ConsoleColors.wrap(ConsoleColors.GRAY,
                "  ║     крафт · сокеты · риск            ║"));
        System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                "  ╚══════════════════════════════════════╝"));
    }

    private static ForgeRiskMode pickMergeRisk(Supplier<String> readLine, int cost) {
        ForgeRenderer.printMergeRiskPicker(cost);
        System.out.print("  ▶ ");
        return parseRisk(readLine.get().trim());
    }

    private static ForgeRiskMode pickInsertRisk(Supplier<String> readLine) {
        ForgeRenderer.printInsertRiskPicker();
        System.out.print("  ▶ ");
        return parseRisk(readLine.get().trim());
    }

    private static ForgeRiskMode parseRisk(String choice) {
        return switch (choice) {
            case "1" -> ForgeRiskMode.STABLE;
            case "2" -> ForgeRiskMode.NORMAL;
            case "3" -> ForgeRiskMode.MAD;
            default -> null;
        };
    }

    private static GemType pickGemType(Supplier<String> readLine) {
        ForgeRenderer.printGemTypesPicker();
        System.out.print("  ▶ ");
        int idx = parseInt(readLine.get()) - 1;
        GemType[] types = GemType.values();
        if (idx < 0 || idx >= types.length) {
            return null;
        }
        return types[idx];
    }

    private static GemTier pickFromTier(Supplier<String> readLine) {
        ForgeRenderer.printTierPicker();
        System.out.print("  ▶ ");
        return switch (readLine.get().trim()) {
            case "1" -> GemTier.I;
            case "2" -> GemTier.II;
            default -> null;
        };
    }

    private static Gem pickGem(Player player, Supplier<String> readLine, Weapon weapon) {
        List<Gem> eligible = new ArrayList<>();
        for (Gem gem : player.getGemStash()) {
            if (!weapon.hasGemType(gem.getType())) {
                eligible.add(gem);
            }
        }
        if (eligible.isEmpty()) {
            System.out.println(ConsoleColors.dim("      Нет подходящих камней (тип уже стоит в оружии)."));
            return null;
        }
        ForgeRenderer.printGemPickerHeader();
        for (int i = 0; i < eligible.size(); i++) {
            Gem g = eligible.get(i);
            System.out.println("  " + (i + 1) + ". " + g.describe());
        }
        ScreenFrame.cancel();
        System.out.print("  ▶ ");
        int idx = parseInt(readLine.get()) - 1;
        if (idx < 0 || idx >= eligible.size()) {
            return null;
        }
        return eligible.get(idx);
    }

    private static WeaponRef pickWeapon(List<WeaponRef> weapons, Supplier<String> readLine) {
        ForgeRenderer.printWeaponPickerHeader();
        for (int i = 0; i < weapons.size(); i++) {
            WeaponRef ref = weapons.get(i);
            ForgeRenderer.printWeaponLine(i + 1, ref.label(), ref.weapon());
        }
        ScreenFrame.cancel();
        System.out.print("  ▶ ");
        int idx = parseInt(readLine.get()) - 1;
        if (idx < 0 || idx >= weapons.size()) {
            return null;
        }
        return weapons.get(idx);
    }

    private static int pickSocketIndex(Weapon weapon, Supplier<String> readLine) {
        for (int i = 0; i < weapon.socketCount(); i++) {
            Gem gem = weapon.getSocket(i);
            if (gem != null) {
                System.out.println("  " + (i + 1) + ". " + gem.describe());
            }
        }
        ScreenFrame.cancel();
        System.out.print("  ▶ ");
        int idx = parseInt(readLine.get()) - 1;
        if (idx < 0 || idx >= weapon.socketCount() || weapon.getSocket(idx) == null) {
            return -1;
        }
        return idx;
    }

    private static List<WeaponRef> collectWeapons(Player player) {
        List<WeaponRef> list = new ArrayList<>();
        Item heroW = player.getEquipment().get(inventory.Equipment.Slot.WEAPON);
        if (heroW instanceof Weapon w && w.socketCount() > 0) {
            list.add(new WeaponRef("Герой", w, nw -> player.getEquipment().equip(inventory.Equipment.Slot.WEAPON, nw)));
        }
        Party party = player.getParty();
        for (int i = 0; i < Party.MAX_MERC_SLOTS; i++) {
            Mercenary m = party.getMercenary(i);
            if (m == null) {
                continue;
            }
            Item mw = m.getEquipment().get(inventory.Equipment.Slot.WEAPON);
            if (mw instanceof Weapon w && w.socketCount() > 0) {
                list.add(new WeaponRef(m.getName(), w,
                        nw -> m.getEquipment().equip(inventory.Equipment.Slot.WEAPON, nw)));
            }
        }
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            Item item = player.getInventory().get(i);
            if (item instanceof Weapon w && w.socketCount() > 0) {
                int invIdx = i;
                list.add(new WeaponRef("Инв. слот " + (i + 1), w,
                        nw -> player.getInventory().set(invIdx, nw)));
            }
        }
        return list;
    }

    private static List<WeaponRef> collectSocketedWeapons(Player player) {
        return collectWeapons(player).stream().filter(r -> r.filledSockets() > 0).toList();
    }

    private static void printResult(boolean success, String message) {
        System.out.println("  " + (success
                ? ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN, "✓ " + message)
                : ConsoleColors.wrap(ConsoleColors.RED, "✗ " + message)));
    }

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void pause(Supplier<String> readLine) {
        System.out.println(ConsoleColors.dim("\n  [Enter]"));
        readLine.get();
    }

    private static void talkForgeMaster(Player player, Supplier<String> readLine, Runnable onSave) {
        QuestLog log = player.getQuestLog();
        ScreenFrame.openPath(ScreenFrame.Screen.FORGE, "Орис");

        if (log.isCompleted(QuestId.SOUL_SOCKET)) {
            System.out.println();
            System.out.println("  «Гнездо приняло душу. Больше от меня не требуется — куй дальше сам.»");
            pause(readLine);
            return;
        }

        if (log.isSoulSocketReady()) {
            System.out.println();
            System.out.println("  «Вставил гем — молодец. Держи камень и расширение сундука.");
            QuestService.completeSoulSocket(player);
            onSave.run();
            pause(readLine);
            return;
        }

        if (log.isThreeSparksReady()) {
            System.out.println();
            System.out.println("  «Три искры — огонь слушается. Теперь вставь гем в оружие через меню кузницы.");
            QuestService.completeThreeSparks(player);
            System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                    "  +" + QuestService.THREE_SPARKS_REWARD_GOLD + " золота"));
            onSave.run();
            pause(readLine);
            return;
        }

        if (log.isSoulSocketAccepted()) {
            System.out.println();
            System.out.println("  «Вставь гем в оружие — пункт «Вставить гем» в кузнице.");
            if (log.isGemInsertedForQuest()) {
                System.out.println("  Готово к сдаче.");
                System.out.println();
                System.out.println("  1. Завершить поручение");
                System.out.println("  0. Назад");
                ScreenFrame.prompt();
                if ("1".equals(readLine.get().trim())) {
                    QuestService.completeSoulSocket(player);
                    onSave.run();
                }
            }
            pause(readLine);
            return;
        }

        if (log.isThreeSparksAccepted()) {
            System.out.println();
            System.out.println("  «Переплавок: " + log.getSuccessfulSmelts() + " / "
                    + QuestLog.SMELT_TARGET + ". Успешные — через «Переплавить руду».»");
            pause(readLine);
            return;
        }

        if (log.isCompleted(QuestId.THREE_SPARKS)) {
            System.out.println();
            System.out.println("  «Искры есть. Осталось вдохнуть душу — вставь гем в оружие.»");
            System.out.println();
            System.out.println("  1. Принять поручение");
            System.out.println("  2. Не сейчас");
            ScreenFrame.prompt();
            if ("1".equals(readLine.get().trim())) {
                log.acceptSoulSocket();
                System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                        "  Поручение принято: " + QuestId.SOUL_SOCKET.getTitle()));
                onSave.run();
            }
            pause(readLine);
            return;
        }

        System.out.println();
        System.out.println("  «Бранн прислал? Значит, страж пал.");
        System.out.println("  Три удачные переплавки руды — и научу работать с гнёздами.");
        System.out.println();
        System.out.println("  1. Принять поручение");
        System.out.println("  2. Не сейчас");
        ScreenFrame.prompt();
        if ("1".equals(readLine.get().trim())) {
            log.acceptThreeSparks();
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                    "  Поручение принято: " + QuestId.THREE_SPARKS.getTitle()));
            onSave.run();
        }
        pause(readLine);
    }

    private record WeaponRef(String label, Weapon weapon, java.util.function.Consumer<Weapon> setter) {
        void apply(Weapon updated) {
            setter.accept(updated);
        }

        int filledSockets() {
            int n = 0;
            for (Gem g : weapon.getSocketedGems()) {
                if (g != null) {
                    n++;
                }
            }
            return n;
        }
    }
}
