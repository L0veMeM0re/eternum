package game;

import inventory.Equipment;
import inventory.Inventory;
import inventory.InventoryHelper;
import item.Item;
import item.ItemSet;
import item.Rarity;
import player.CharacterClass;
import player.Player;
import party.Mercenary;
import party.MercenaryRecruiter;
import party.MercenaryShop;
import party.Party;
import progression.GoldShop;
import progression.LevelService;
import progression.MagicShop;
import progression.MetaProgression;
import progression.RunEffects;
import town.Forge;
import town.GamblingClub;
import town.MineCamp;
import town.OreCave;
import town.StripClub;
import town.SwampOutpost;
import town.RovingMerchantShop;
import town.TradeRoad;
import town.Village;
import util.TownSquareRenderer;
import quest.QuestService;
import save.SaveManager;
import util.CharacterSheetRenderer;
import util.ConsoleColors;
import util.ConsoleInput;
import util.MobArt;
import util.ScreenFrame;
import util.WelcomeBanner;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private final ConsoleInput consoleInput = new ConsoleInput();
    private final MetaProgression meta = new MetaProgression();
    private final LevelService levelService = new LevelService();
    private final SaveManager saveManager = new SaveManager();
    private Player player;
    private GameLoop gameLoop;
    private final GoldShop goldShop = new GoldShop();
    private final MagicShop magicShop = new MagicShop();
    private final MercenaryShop mercenaryShop = new MercenaryShop();
    private boolean exitRequested;
    private boolean autoRestartOnDeath;

    private boolean saveLoaded;
    private boolean saveLoadedWithoutClass;
    private final GameSettings settings = GameSettings.load();

    public void start() {
        if (!runIntroIfNeeded()) {
            return;
        }
        loadSave();
        showWelcome();
        ensureInitialClassSelection();

        while (!exitRequested) {
            showMainMenu();
            String choice = readLine();
            if (choice.isEmpty() && consoleInput.isEof()) {
                ensurePlayer();
                saveManager.save(meta, player, autoRestartOnDeath);
                System.out.println("До встречи, искатель приключений!");
                break;
            }
            switch (choice.toLowerCase()) {
                case "1" -> startNewRun();
                case "2" -> showCharacterMenu();
                case "3" -> showWorldMenu();
                case "4" -> showTownSquare();
                case "5" -> {
                    ensurePlayer();
                    if (player.getParty().hasRecruitedMercenary()) {
                        showTeamMenu();
                    } else {
                        showStatistics();
                    }
                }
                case "6" -> {
                    ensurePlayer();
                    if (player.getParty().hasRecruitedMercenary()) {
                        showStatistics();
                    } else {
                        ensurePlayer();
                        saveManager.save(meta, player, autoRestartOnDeath);
                        exitRequested = true;
                        System.out.println("До встречи, искатель приключений!");
                    }
                }
                case "7" -> {
                    ensurePlayer();
                    if (player.getParty().hasRecruitedMercenary()) {
                        saveManager.save(meta, player, autoRestartOnDeath);
                        exitRequested = true;
                        System.out.println("До встречи, искатель приключений!");
                    } else {
                        System.out.println("Неверный выбор.");
                    }
                }
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    /** Титул и пролог — только при первом входе. */
    private boolean runIntroIfNeeded() {
        if (settings.isPrologueSeen()) {
            return true;
        }
        TitleScreen.Action action = TitleScreen.show(this::readLine);
        if (action == TitleScreen.Action.EXIT) {
            return false;
        }
        ProloguePlayer.play(this::readLine);
        settings.setPrologueSeen(true);
        settings.save();
        return true;
    }

    private void loadSave() {
        SaveManager.SaveData data = saveManager.load();
        if (data != null) {
            saveManager.applyToMeta(data, meta);
            if (hasSavedClass(data)) {
                CharacterClass savedClass = CharacterClass.valueOf(data.selectedClass);
                player = new Player(savedClass);
                player.restoreProgress(savedClass, data.playerLevel, data.playerXp, levelService);
                player.setGold(data.playerGold);
                saveManager.applyToPlayer(data, player);
                player.applyGoldTraining(data.goldTrainingHp, data.goldTrainingAtk, data.inventoryExpansions);
                player.setAscensionUnlocked(data.ascensionUnlocked || data.ascensionKeys > 0);
                player.setAbdolbosPotions(data.abdolbosPotions);
                autoRestartOnDeath = saveManager.readAutoRestart(data);
                player.recalculateStats(meta);
                player.getParty().recalculateAll(player, meta);
                System.out.println("Сохранение загружено.");
            } else {
                System.out.println("Сохранение загружено (класс ещё не выбран).");
            }
        }
    }

    private static boolean hasSavedClass(SaveManager.SaveData data) {
        return data.selectedClass != null
                && !data.selectedClass.isBlank()
                && !"NONE".equalsIgnoreCase(data.selectedClass);
    }

    private void ensureInitialClassSelection() {
        if (player != null) {
            return;
        }
        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW,
                "Добро пожаловать! Сначала выберите класс героя."));
        while (player == null) {
            CharacterClass chosen = promptClassChoice(false);
            if (chosen != null) {
                player = new Player(chosen);
                player.recalculateStats(meta);
                saveManager.save(meta, player, autoRestartOnDeath);
                System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                        "Класс выбран: " + chosen.getDisplayName() + "."));
            }
        }
        System.out.println();
    }

    private void ensurePlayer() {
        if (player == null) {
            ensureInitialClassSelection();
        }
    }

    private void showWelcome() {
        WelcomeBanner.print();
        System.out.println("  Кристаллы душ: " + ConsoleColors.wrap(ConsoleColors.MAGENTA,
                String.valueOf(meta.getSoulCrystals())));
        if (meta.getBestFloor() > 0) {
            System.out.println("  Лучший этаж: " + ConsoleColors.wrap(ConsoleColors.YELLOW,
                    String.valueOf(meta.getBestFloor())));
        }
        System.out.println();
    }

    private void showMainMenu() {
        ScreenFrame.open(ScreenFrame.Screen.MAIN);
        System.out.println("  1. Новый забег");
        System.out.println("  2. Персонаж");
        System.out.println("  3. Мир");
        System.out.println("  4. Площадь (магазины)");
        ensurePlayer();
        if (player.getParty().hasRecruitedMercenary()) {
            System.out.println("  5. Команда");
            System.out.println("  6. Статистика");
            System.out.println("  7. Выход");
        } else {
            System.out.println("  5. Статистика");
            System.out.println("  6. Выход");
        }
        ensurePlayer();
        System.out.println();
        System.out.println("  Золото: " + ConsoleColors.wrap(ConsoleColors.YELLOW, String.valueOf(player.getGold())));
        int unspentHero = player.getAvailableSkillPoints();
        if (unspentHero > 0) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW,
                    "  ⚠ Непотраченных очков (герой): " + unspentHero));
        }
        int unspentMerc = player.countUnspentSkillPointsAllMercenaries();
        if (unspentMerc > 0) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW,
                    "  ⚠ Непотраченных очков (наёмники): " + unspentMerc));
        }
        ScreenFrame.prompt();
    }

    private void toggleAutoRestart() {
        autoRestartOnDeath = !autoRestartOnDeath;
        ensurePlayer();
        saveManager.save(meta, player, autoRestartOnDeath);
        System.out.println("Автозапуск при поражении: "
                + (autoRestartOnDeath ? ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN, "ВКЛ")
                : ConsoleColors.wrap(ConsoleColors.GRAY, "ВЫКЛ")));
    }

    private void startNewRun() {
        if (gameLoop != null && gameLoop.isRunning()) {
            System.out.println("Забег уже идёт!");
            return;
        }

        ensurePlayer();
        player.recalculateStats(meta);

        if (player.getBlessingScrolls() > 0) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.GREEN,
                    "В запасе благословение — будет использовано в забеге ("
                            + player.getBlessingScrolls() + "/" + GoldShop.MAX_SCROLLS + ")"));
        }
        if (player.getXpScrolls() > 0) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.GREEN,
                    "В запасе свиток опыта — будет использован в забеге ("
                            + player.getXpScrolls() + "/" + GoldShop.MAX_SCROLLS + ")"));
        }
        printMagicScrollStashHint();

        int startFloor = showRunStartMenu();
        if (startFloor < 0) {
            return;
        }

        gameLoop = new GameLoop(player, meta, levelService, saveManager, consoleInput,
                () -> gameLoop = null, () -> autoRestartOnDeath);
        gameLoop.startRun(startFloor);
        drainMenuInput();
    }

    private int showRunStartMenu() {
        while (true) {
            ScreenFrame.openPath(ScreenFrame.Screen.RUN, "Главное");
            String autoLabel = autoRestartOnDeath
                    ? ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN, "ВКЛ")
                    : ConsoleColors.wrap(ConsoleColors.GRAY, "ВЫКЛ");
            System.out.println("  A. Автозапуск при поражении: " + autoLabel);

            if (player.hasAscensionUnlock()) {
                System.out.println();
                System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                        "  ✓ Ключ возвышения активен"));
                System.out.println("  Можно начать забег с этажа " + MagicShop.ASCENSION_START_FLOOR + ".");
                ScreenFrame.section("Старт");
                System.out.println("  1. С этажа " + MagicShop.ASCENSION_START_FLOOR);
                System.out.println("  2. С первого этажа");
            } else {
                ScreenFrame.section("Старт");
                System.out.println("  1. Начать забег");
            }
            ScreenFrame.back();
            ScreenFrame.prompt();

            switch (readLine().toLowerCase()) {
                case "a" -> toggleAutoRestart();
                case "0" -> {
                    return -1;
                }
                case "1" -> {
                    if (player.hasAscensionUnlock()) {
                        return MagicShop.ASCENSION_START_FLOOR;
                    }
                    return 1;
                }
                case "2" -> {
                    if (player.hasAscensionUnlock()) {
                        return 1;
                    }
                    System.out.println("Неверный выбор.");
                }
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    private void showCharacterMenu() {
        ensurePlayer();

        boolean back = false;
        while (!back) {
            ScreenFrame.openPath(ScreenFrame.Screen.CHARACTER, "Главное");
            printCharacterSheet();
            ScreenFrame.section("Меню");
            System.out.println("  1. Инвентарь");
            System.out.println("  2. Экипировка");
            System.out.println("  3. Сменить класс");
            System.out.println("  4. Справочник сетов");
            System.out.println("  5. Дерево умений"
                    + (player.getAvailableSkillPoints() > 0
                    ? ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW, " [" + player.getAvailableSkillPoints() + "]")
                    : ""));
            ScreenFrame.back();
            ScreenFrame.prompt();

            switch (readLine()) {
                case "1" -> showInventoryMenu();
                case "2" -> showEquipmentMenu();
                case "3" -> changeClass();
                case "4" -> showSetCodex();
                case "5" -> SkillTreeMenu.showHeroMenu(player, meta, this::readLine,
                        () -> saveManager.save(meta, player, autoRestartOnDeath));
                case "0" -> back = true;
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    private void printCharacterSheet() {
        CharacterSheetRenderer.print(player, levelService, false);
    }

    private void changeClass() {
        ScreenFrame.openPath(ScreenFrame.Screen.CLASS_CHANGE, "Главное", "Персонаж");
        printClassOptions();
        ScreenFrame.cancel();
        ScreenFrame.prompt();

        CharacterClass chosen = parseClassChoice(readLine(), true);
        if (chosen == null) {
            return;
        }
        if (chosen == player.getCharacterClass()) {
            System.out.println("Этот класс уже выбран.");
            return;
        }
        player.changeClass(chosen, levelService);
        player.recalculateStats(meta);
        saveManager.save(meta, player, autoRestartOnDeath);
        System.out.println("Класс изменён на: " + chosen.getDisplayName()
                + " (ур. " + player.getLevel() + ", экипировка и инвентарь сохранены)");
    }

    private CharacterClass promptClassChoice(boolean allowCancel) {
        if (allowCancel) {
            ScreenFrame.openPath(ScreenFrame.Screen.CLASS_CHANGE, "Главное", "Персонаж");
        } else {
            ScreenFrame.open(ScreenFrame.Screen.CLASS_CHANGE);
            System.out.println("  Выберите класс — от него зависят умения и стиль боя.");
        }
        printClassOptions();
        if (allowCancel) {
            ScreenFrame.cancel();
        }
        ScreenFrame.prompt();
        return parseClassChoice(readLine(), allowCancel);
    }

    private void printClassOptions() {
        CharacterClass[] classes = CharacterClass.values();
        for (int i = 0; i < classes.length; i++) {
            CharacterClass cls = classes[i];
            System.out.println("  " + (i + 1) + ". " + ConsoleColors.wrap(cls.getPortraitPrimary(), cls.getDisplayName())
                    + " — " + cls.getDescription());
            for (String line : cls.getPortrait()) {
                System.out.println("      " + MobArt.colorize(line, cls.getPortraitPrimary(),
                        cls.getPortraitAccent(), "@*[]<>^|"));
            }
        }
    }

    private CharacterClass parseClassChoice(String input, boolean allowCancel) {
        if (allowCancel && "0".equals(input)) {
            return null;
        }
        try {
            int index = Integer.parseInt(input) - 1;
            CharacterClass[] classes = CharacterClass.values();
            if (index >= 0 && index < classes.length) {
                return classes[index];
            }
        } catch (NumberFormatException ignored) {
            // fall through
        }
        System.out.println("Неверный выбор.");
        return null;
    }

    private void showInventoryMenu() {
        Inventory inventory = player.getInventory();
        boolean back = false;

        while (!back) {
            ScreenFrame.openPath(ScreenFrame.Screen.INVENTORY, "Главное", "Персонаж");
            CharacterSheetRenderer.printEquipmentBlock(player);
            System.out.println();
            ScreenFrame.subsection("Рюкзак ("
                    + inventory.countItems() + "/" + inventory.getMaxSize() + ")");
            boolean hasItems = false;
            for (int i = 0; i < inventory.getSize(); i++) {
                Item item = inventory.get(i);
                if (item != null) {
                    hasItems = true;
                    System.out.println("    " + (i + 1) + ". " + item.getDisplayName()
                            + ConsoleColors.dim(" — " + item.getStatDescription()));
                }
            }
            if (!hasItems) {
                System.out.println(ConsoleColors.dim("    (пусто)"));
            }

            ScreenFrame.section("Действия");
            System.out.println("  1. Экипировать");
            System.out.println("  2. Одеть лучшее");
            System.out.println("  3. Продать предмет");
            System.out.println("  4. Продать по редкости");
            System.out.println("  5. Продать всё");
            System.out.println("  6. Продать хлам");
            System.out.println("  7. Удалить предмет");
            System.out.println("  8. Удалить по редкости");
            System.out.println("  9. Удалить всё");
            System.out.println("  10. Одеть лучшее — весь отряд");
            System.out.println("  11. Автопродажа при подборе");
            printAutoSellStatus();
            ScreenFrame.back();
            ScreenFrame.prompt();

            switch (readLine()) {
                case "1" -> equipFromInventory();
                case "2" -> equipBestFromInventory();
                case "3" -> sellFromInventory();
                case "4" -> sellByRarityMenu();
                case "5" -> sellAllFromInventory();
                case "6" -> sellJunkFromInventory();
                case "7" -> deleteFromInventory();
                case "8" -> deleteByRarityMenu();
                case "9" -> deleteAllFromInventory();
                case "10" -> equipBestForWholeParty();
                case "11" -> showAutoSellMenu();
                case "0" -> back = true;
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    private void equipBestFromInventory() {
        int count = InventoryHelper.equipBest(player, meta);
        if (count > 0) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                    "Экипировано предметов: " + count));
            saveManager.save(meta, player, autoRestartOnDeath);
        } else {
            System.out.println("Нет улучшений для экипировки.");
        }
    }

    private void equipBestForWholeParty() {
        int count = InventoryHelper.equipBestForParty(player, meta);
        if (count > 0) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                    "Экипировано предметов (весь отряд): " + count));
            saveManager.save(meta, player, autoRestartOnDeath);
        } else {
            System.out.println("Нет улучшений для экипировки.");
        }
    }

    private void sellJunkFromInventory() {
        int count = InventoryHelper.countJunk(player);
        if (count == 0) {
            System.out.println("Хлам не найден (common хуже экипировки).");
            return;
        }
        int gold = InventoryHelper.estimateSellJunkGold(player);
        if (!confirmAction("Продажа хлама",
                "Предметов: " + count + "\nБудет получено золота: " + gold, "")) {
            System.out.println("Отменено.");
            return;
        }
        count = InventoryHelper.sellJunk(player);
        System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                "Продано: " + count + " | Золото: " + player.getGold()));
        saveManager.save(meta, player, autoRestartOnDeath);
    }

    private void equipBestForMercenary(Mercenary mercenary) {
        int count = InventoryHelper.equipBestForMercenary(player, mercenary, meta, player.getRunDamageBonus());
        if (count > 0) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                    "Экипировано предметов: " + count));
            saveManager.save(meta, player, autoRestartOnDeath);
        } else {
            System.out.println("Нет улучшений для экипировки.");
        }
    }

    private void sellAllFromInventory() {
        int count = player.getInventory().countItems();
        if (count == 0) {
            System.out.println("Инвентарь пуст.");
            return;
        }
        int gold = estimateSellGoldAll();
        if (!confirmAction("Продажа всего инвентаря",
                "Предметов: " + count + "\nБудет получено золота: " + gold,
                listAllInventoryItems())) {
            System.out.println("Отменено.");
            return;
        }
        count = InventoryHelper.sellAll(player);
        if (count > 0) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                    "Продано предметов: " + count + " | Золото: " + player.getGold()));
            saveManager.save(meta, player, autoRestartOnDeath);
        } else {
            System.out.println("Нечего продавать.");
        }
    }

    private void deleteAllFromInventory() {
        if (player.getInventory().countItems() == 0) {
            System.out.println("Инвентарь пуст.");
            return;
        }
        int count = player.getInventory().countItems();
        if (!confirmAction("Удаление всего инвентаря",
                "Предметов: " + count + "\nЗолото не вернётся.",
                listAllInventoryItems())) {
            System.out.println("Отменено.");
            return;
        }
        count = InventoryHelper.deleteAll(player);
        if (count > 0) {
            player.recalculateStats(meta);
            System.out.println(ConsoleColors.wrap(ConsoleColors.RED,
                    "Удалено предметов: " + count));
            saveManager.save(meta, player, autoRestartOnDeath);
        }
    }

    private void deleteByRarityMenu() {
        ScreenFrame.open(ScreenFrame.Screen.INVENTORY, "Удалить по редкости", "Главное", "Персонаж", "Инвентарь");
        Rarity[] rarities = Rarity.values();
        for (int i = 0; i < rarities.length; i++) {
            Rarity r = rarities[i];
            int count = countByRarity(r);
            System.out.println("  " + (i + 1) + ". " + r.formatName(r.getColorName())
                    + ConsoleColors.dim(" (" + count + " шт.)"));
        }
        ScreenFrame.cancel();
        ScreenFrame.prompt();

        String input = readMenuChoice();
        if ("0".equals(input)) {
            return;
        }
        try {
            int index = Integer.parseInt(input) - 1;
            if (index >= 0 && index < rarities.length) {
                deleteRarity(rarities[index]);
            } else {
                System.out.println("Неверный выбор.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Неверный выбор.");
        }
    }

    private void deleteRarity(Rarity rarity) {
        int count = countByRarity(rarity);
        if (count == 0) {
            System.out.println("Нет предметов этой редкости.");
            return;
        }
        if (!confirmAction("Удаление по редкости",
                "Редкость: " + rarity.formatName(rarity.getColorName()) + "\nПредметов: " + count,
                listItemsByRarity(rarity))) {
            System.out.println("Отменено.");
            return;
        }
        int removed = InventoryHelper.deleteByRarity(player, rarity);
        if (removed > 0) {
            player.recalculateStats(meta);
            System.out.println(ConsoleColors.wrap(ConsoleColors.RED,
                    "Удалено: " + removed + " предмет(ов)"));
            saveManager.save(meta, player, autoRestartOnDeath);
        }
    }

    private void sellByRarityMenu() {
        ScreenFrame.open(ScreenFrame.Screen.INVENTORY, "Продать по редкости", "Главное", "Персонаж", "Инвентарь");
        Rarity[] rarities = Rarity.values();
        for (int i = 0; i < rarities.length; i++) {
            Rarity r = rarities[i];
            int count = countByRarity(r);
            System.out.println("  " + (i + 1) + ". " + r.formatName(r.getColorName())
                    + ConsoleColors.dim(" (" + count + " шт.)"));
        }
        ScreenFrame.cancel();
        ScreenFrame.prompt();

        String input = readMenuChoice();
        if ("0".equals(input)) {
            return;
        }
        try {
            int index = Integer.parseInt(input) - 1;
            if (index >= 0 && index < rarities.length) {
                sellRarity(rarities[index]);
            } else {
                System.out.println("Неверный выбор.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Неверный выбор.");
        }
    }

    private int countByRarity(Rarity rarity) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            Item item = player.getInventory().get(i);
            if (item != null && item.getRarity() == rarity) {
                count++;
            }
        }
        return count;
    }

    private void sellRarity(Rarity rarity) {
        int count = countByRarity(rarity);
        if (count == 0) {
            System.out.println("Нет предметов этой редкости.");
            return;
        }
        int gold = estimateSellGoldByRarity(rarity);
        if (!confirmAction("Продажа по редкости",
                "Редкость: " + rarity.formatName(rarity.getColorName())
                        + "\nПредметов: " + count + "\nБудет получено золота: " + gold,
                listItemsByRarity(rarity))) {
            System.out.println("Отменено.");
            return;
        }
        InventoryHelper.SellResult result = InventoryHelper.sellByRarity(player, rarity);
        if (result.count > 0) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                    "Продано: " + result.count + " | Получено золота: " + result.gold
                            + " | Всего золота: " + player.getGold()));
            saveManager.save(meta, player, autoRestartOnDeath);
        } else {
            System.out.println("Нечего продавать.");
        }
    }

    private void printAutoSellStatus() {
        var enabled = player.getAutoSellRarities();
        if (enabled.isEmpty()) {
            System.out.println(ConsoleColors.dim("  Автопродажа: выкл (предметы копятся в рюкзаке)"));
            return;
        }
        StringBuilder sb = new StringBuilder("  Автопродажа: ");
        for (Rarity r : Rarity.values()) {
            if (enabled.contains(r)) {
                sb.append(r.formatName(r.getColorName())).append(" ");
            }
        }
        System.out.println(sb.toString().trim());
    }

    private void showAutoSellMenu() {
        boolean back = false;
        while (!back) {
            ScreenFrame.open(ScreenFrame.Screen.INVENTORY, "Автопродажа", "Главное", "Персонаж", "Инвентарь");
            System.out.println("  При подборе лута предметы выбранных редкостей сразу продаются за золото.");
            System.out.println("  Остальные редкости попадают в рюкзак как обычно.\n");
            Rarity[] rarities = Rarity.values();
            for (int i = 0; i < rarities.length; i++) {
                Rarity r = rarities[i];
                String mark = player.isAutoSell(r)
                        ? ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN, "[✓]")
                        : ConsoleColors.dim("[ ]");
                System.out.println("  " + (i + 1) + ". " + mark + " " + r.formatName(r.getColorName()));
            }
            ScreenFrame.back();
            ScreenFrame.prompt();
            String input = readLine().trim();
            if ("0".equals(input)) {
                back = true;
            } else {
                try {
                    int idx = Integer.parseInt(input) - 1;
                    if (idx >= 0 && idx < rarities.length) {
                        Rarity r = rarities[idx];
                        boolean on = player.toggleAutoSell(r);
                        System.out.println(on
                                ? ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                                "  Автопродажа включена: " + r.getColorName())
                                : ConsoleColors.wrap(ConsoleColors.YELLOW,
                                "  Автопродажа выключена: " + r.getColorName()));
                        saveManager.save(meta, player, autoRestartOnDeath);
                    } else {
                        System.out.println("Неверный выбор.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Неверный выбор.");
                }
            }
        }
    }

    private void equipFromInventory() {
        System.out.print("Номер предмета: ");
        int index = readIndex() - 1;
        if (player.equipItem(index, meta)) {
            System.out.println("Предмет экипирован.");
            saveManager.save(meta, player, autoRestartOnDeath);
        } else {
            System.out.println("Не удалось экипировать (нет предмета или инвентарь полон).");
        }
    }

    private void sellFromInventory() {
        System.out.print("Номер предмета: ");
        int index = readIndex() - 1;
        Item item = player.getInventory().get(index);
        if (item != null && player.sellItem(index)) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                    "Продано за " + item.getSellPrice() + " золота."));
            saveManager.save(meta, player, autoRestartOnDeath);
        } else {
            System.out.println("Нет такого предмета.");
        }
    }

    private void deleteFromInventory() {
        System.out.print("Номер предмета для удаления: ");
        int index = readIndex() - 1;
        Item item = player.getInventory().get(index);
        if (item == null) {
            System.out.println("Нет такого предмета.");
            return;
        }
        if (!confirmAction("Удаление предмета",
                item.getDisplayName() + "\n" + item.getStatDescription(),
                null)) {
            System.out.println("Отменено.");
            return;
        }
        if (player.deleteItem(index)) {
            player.recalculateStats(meta);
            System.out.println("Предмет удалён.");
            saveManager.save(meta, player, autoRestartOnDeath);
        }
    }

    /** Сводка действия, затем 1 — да, 0 — нет (как в остальных меню). */
    private boolean confirmAction(String title, String summary, String itemList) {
        ScreenFrame.open(ScreenFrame.Screen.CONFIRM, title);
        System.out.println("  " + summary.replace("\n", "\n  "));
        if (itemList != null && !itemList.isEmpty()) {
            System.out.println();
            ScreenFrame.subsection("Список");
            System.out.println(itemList);
        }
        System.out.println();
        System.out.println("  1. Подтвердить");
        ScreenFrame.cancel();
        ScreenFrame.prompt();
        System.out.flush();
        String choice = readLine();
        consoleInput.discardExtraMenuLines();
        return "1".equals(choice);
    }

    private String readMenuChoice() {
        String choice = readLine();
        consoleInput.discardExtraMenuLines();
        return choice;
    }

    private String listAllInventoryItems() {
        StringBuilder sb = new StringBuilder();
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            Item item = inv.get(i);
            if (item != null) {
                sb.append("  • ").append(item.getDisplayName())
                        .append(ConsoleColors.dim(" — " + item.getStatDescription()))
                        .append('\n');
            }
        }
        return sb.toString().trim();
    }

    private String listItemsByRarity(Rarity rarity) {
        StringBuilder sb = new StringBuilder();
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            Item item = inv.get(i);
            if (item != null && item.getRarity() == rarity) {
                sb.append("  • ").append(item.getDisplayName())
                        .append(ConsoleColors.dim(" — " + item.getStatDescription()))
                        .append('\n');
            }
        }
        return sb.toString().trim();
    }

    private int estimateSellGoldAll() {
        int gold = 0;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            Item item = inv.get(i);
            if (item != null) {
                gold += item.getSellPrice();
            }
        }
        return gold;
    }

    private int estimateSellGoldByRarity(Rarity rarity) {
        int gold = 0;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            Item item = inv.get(i);
            if (item != null && item.getRarity() == rarity) {
                gold += item.getSellPrice();
            }
        }
        return gold;
    }

    private void showEquipmentMenu() {
        ScreenFrame.openPath(ScreenFrame.Screen.EQUIPMENT, "Главное", "Персонаж");
        CharacterSheetRenderer.printEquipmentBlock(player);
        System.out.println("\n  Нажмите Enter...");
        readLine();
    }

    private void showSetCodex() {
        ScreenFrame.openPath(ScreenFrame.Screen.SET_CODEX, "Главное", "Персонаж");
        System.out.println("  Предметы сетов определяются по названию (например «Кольцо дракона»).");
        System.out.println("  Сет «Созвездие» — по редкости: любые мифические предметы в экипировке.");
        System.out.println("  Бонусы суммируются за каждый достигнутый порог.\n");
        for (ItemSet set : ItemSet.values()) {
            if (set == ItemSet.NONE) {
                continue;
            }
            System.out.println(ConsoleColors.wrap(set.getColor(), "  ▸ Сет «" + set.getDisplayName() + "»"));
            if (set.isRaritySet()) {
                System.out.println(ConsoleColors.dim("    Условие: любые предметы редкости «"
                        + set.getCountRarity().getColorName() + "» (до 7 слотов)"));
            } else {
                System.out.println(ConsoleColors.dim("    Ключевые слова: "
                        + String.join(", ", set.getNameKeywords())));
            }
            for (ItemSet.SetTier tier : set.getTiers()) {
                System.out.println("      " + tier.describe());
            }
            System.out.println();
        }
        System.out.println("  Нажмите Enter...");
        readLine();
    }

    private void showWorldMenu() {
        ensurePlayer();
        boolean back = false;
        while (!back) {
            ScreenFrame.openPath(ScreenFrame.Screen.WORLD, "Главное");
            System.out.println("  За городской стеной — тихие места и люди, которым нужна помощь.");
            ScreenFrame.section("Локации");
            System.out.println("  1. Деревня");
            int next = 2;
            int mineChoice = -1;
            int swampChoice = -1;
            int roadChoice = -1;
            if (QuestService.isMineCampAvailable(player, meta)) {
                mineChoice = next++;
                System.out.println("  " + mineChoice + ". Шахтёрская застава");
            }
            if (player.getQuestLog().isHermitUnlocked()) {
                swampChoice = next++;
                System.out.println("  " + swampChoice + ". Застава болота");
            }
            if (player.getQuestLog().isStarostaUnlocked()) {
                roadChoice = next++;
                System.out.println("  " + roadChoice + ". Тёмный тракт");
            }
            ScreenFrame.back();
            ScreenFrame.prompt();
            String input = readLine();
            if ("0".equals(input)) {
                back = true;
            } else if ("1".equals(input)) {
                Village.open(player, meta, this::readMenuChoice,
                        () -> saveManager.save(meta, player, autoRestartOnDeath));
            } else if (mineChoice > 0 && String.valueOf(mineChoice).equals(input)) {
                MineCamp.open(player, meta, this::readMenuChoice,
                        () -> saveManager.save(meta, player, autoRestartOnDeath));
            } else if (swampChoice > 0 && String.valueOf(swampChoice).equals(input)) {
                SwampOutpost.open(player, meta, this::readMenuChoice,
                        () -> saveManager.save(meta, player, autoRestartOnDeath));
            } else if (roadChoice > 0 && String.valueOf(roadChoice).equals(input)) {
                TradeRoad.open(player, meta, this::readMenuChoice,
                        () -> saveManager.save(meta, player, autoRestartOnDeath));
            } else {
                System.out.println("Неверный выбор.");
            }
        }
    }

    private void showTownSquare() {
        ensurePlayer();
        boolean back = false;
        while (!back) {
            ScreenFrame.openPath(ScreenFrame.Screen.TOWN, "Главное");
            TownSquareRenderer.printCompact();
            System.out.println("  Золото: " + ConsoleColors.wrap(ConsoleColors.YELLOW, String.valueOf(player.getGold())));
            ScreenFrame.section("Лавки");
            System.out.println("  1. Магазин предметов");
            System.out.println("  2. Торговец (золото)");
            System.out.println("  3. Улучшения (кристаллы душ)");
            System.out.println("  4. Наёмники");
            System.out.println("  5. Магическая лавка");
            System.out.println("  6. Игорный клуб");
            System.out.println("  7. Стриптиз-клуб");
            System.out.println("  8. Кузница гемов");
            System.out.println("  9. Шахта руды");
            System.out.println("  10. Бродячий торговец");
            ScreenFrame.back();
            ScreenFrame.prompt();
            switch (readLine()) {
                case "1" -> showItemShop();
                case "2" -> showGoldShop();
                case "3" -> showUpgradeShop();
                case "4" -> showMercenaryShop();
                case "5" -> showMagicShop();
                case "6" -> GamblingClub.open(player, this::readLine,
                        () -> saveManager.save(meta, player, autoRestartOnDeath));
                case "7" -> StripClub.open(player, this::readLine,
                        () -> saveManager.save(meta, player, autoRestartOnDeath));
                case "8" -> Forge.open(player, meta, this::readLine,
                        () -> saveManager.save(meta, player, autoRestartOnDeath));
                case "9" -> OreCave.open(player, meta, this::readMenuChoice,
                        () -> saveManager.save(meta, player, autoRestartOnDeath));
                case "10" -> RovingMerchantShop.open(player, this::readLine,
                        () -> saveManager.save(meta, player, autoRestartOnDeath));
                case "0" -> back = true;
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    private void showItemShop() {
        ensurePlayer();
        boolean back = false;
        while (!back) {
            ScreenFrame.openPath(ScreenFrame.Screen.ITEM_SHOP, "Главное", "Площадь");
            System.out.println("  Золото: " + player.getGold());
            ScreenFrame.section("Товары");
            System.out.println("  1. Тайный сундук — случайный предмет (" + GoldShop.CHEST_COST + " G)");
            ScreenFrame.back();
            ScreenFrame.prompt();
            switch (readLine()) {
                case "1" -> buyChest();
                case "0" -> back = true;
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    private void showMercenaryShop() {
        ensurePlayer();
        Party party = player.getParty();
        boolean back = false;
        while (!back) {
            ScreenFrame.openPath(ScreenFrame.Screen.MERC_SHOP, "Главное", "Площадь");
            System.out.println("  Золото: " + player.getGold());
            System.out.println("  Открыто слотов: " + party.getUnlockedMercSlots() + "/" + Party.MAX_MERC_SLOTS);
            System.out.println("  В отряде: " + (1 + party.getMercenaryCount()) + "/4");
            System.out.println();
            int nextSlot = party.getUnlockedMercSlots() + 1;
            ScreenFrame.section("Услуги");
            if (nextSlot <= Party.MAX_MERC_SLOTS) {
                System.out.println("  1. Купить слот " + (nextSlot + 1) + " в отряде ("
                        + MercenaryShop.slotCost(nextSlot) + " G)");
            } else {
                System.out.println("  1. " + ConsoleColors.dim("(все слоты открыты)"));
            }
            System.out.println("  2. Нанять союзника (от " + MercenaryShop.hireCost(0) + " G)");
            System.out.println("  3. Продать союзника (50% цены, снять экипировку)");
            ScreenFrame.back();
            ScreenFrame.prompt();
            switch (readLine()) {
                case "1" -> buyMercenarySlot(party, nextSlot);
                case "2" -> hireMercenary(party);
                case "3" -> sellMercenaryMenu(party);
                case "0" -> back = true;
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    private void buyMercenarySlot(Party party, int nextSlot) {
        if (nextSlot > Party.MAX_MERC_SLOTS) {
            System.out.println("Все слоты уже открыты.");
            return;
        }
        if (mercenaryShop.buySlot(player, party)) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                    "Слот " + (party.getUnlockedMercSlots() + 1) + " в отряде открыт!"));
            saveManager.save(meta, player, autoRestartOnDeath);
        } else {
            System.out.println(ConsoleColors.wrap(ConsoleColors.RED, "Не хватает золота."));
        }
    }

    private void hireMercenary(Party party) {
        if (party.getUnlockedMercSlots() == 0) {
            System.out.println("Сначала купите слот в отряде.");
            return;
        }
        if (party.findEmptySlot() < 0) {
            System.out.println("Нет свободных слотов. Продайте союзника или откройте новый слот.");
            return;
        }
        int priceIndex = party.getMercenaryCount();
        int cost = MercenaryShop.hireCost(Math.min(priceIndex, MercenaryShop.MAX_HIRE_TIERS - 1));
        List<Mercenary> offers = MercenaryRecruiter.generateOffers();

        ScreenFrame.openPath(ScreenFrame.Screen.MERC_HIRE, "Главное", "Площадь", "Наёмники");
        System.out.println("  Стоимость найма: " + ConsoleColors.wrap(ConsoleColors.YELLOW, cost + " G"));
        System.out.println("  Ваше золото: " + player.getGold());
        System.out.println();

        for (int i = 0; i < offers.size(); i++) {
            ScreenFrame.subsection("Кандидат " + (i + 1));
            CharacterSheetRenderer.printMercenary(offers.get(i), levelService, meta, 0, false);
        }

        ScreenFrame.section("Найм");
        for (int i = 0; i < offers.size(); i++) {
            Mercenary o = offers.get(i);
            System.out.println("  " + (i + 1) + ". " + o.getName() + " (" + o.getCharacterClass().getDisplayName() + ")");
        }
        ScreenFrame.cancel();
        ScreenFrame.prompt();

        String choice = readLine();
        if ("0".equals(choice)) {
            return;
        }
        try {
            int pick = Integer.parseInt(choice);
            if (pick < 1 || pick > offers.size()) {
                System.out.println("Неверный выбор.");
                return;
            }
            if (player.getGold() < cost) {
                System.out.println(ConsoleColors.wrap(ConsoleColors.RED, "Не хватает золота."));
                return;
            }
            Mercenary chosen = offers.get(pick - 1);
            Mercenary hired = MercenaryRecruiter.hireChosen(player, party, chosen, priceIndex);
            if (hired != null) {
                hired.recalculateStats(meta, player.getRunDamageBonus());
                System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                        "\nВ отряд вступил: " + hired.getName() + " (" + hired.getCharacterClass().getDisplayName() + ")"));
                saveManager.save(meta, player, autoRestartOnDeath);
            } else {
                System.out.println(ConsoleColors.wrap(ConsoleColors.RED, "Не удалось нанять."));
            }
        } catch (NumberFormatException e) {
            System.out.println("Неверный выбор.");
        }
    }

    private void sellMercenaryMenu(Party party) {
        System.out.println("\nКого уволить (продать)?");
        for (int i = 0; i < Party.MAX_MERC_SLOTS; i++) {
            Mercenary m = party.getMercenary(i);
            if (m != null) {
                System.out.println((i + 1) + ". " + m.getName() + " (" + m.getCharacterClass().getDisplayName() + ")");
            }
        }
        System.out.println("0. Отмена");
        System.out.print("Выбор: ");
        try {
            int c = Integer.parseInt(readLine());
            if (c == 0) {
                return;
            }
            int slot = c - 1;
            if (mercenaryShop.sellMercenary(player, party, slot)) {
                System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW, "Союзник уволен, золото возвращено."));
                saveManager.save(meta, player, autoRestartOnDeath);
            } else {
                System.out.println("Неверный выбор.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Неверный выбор.");
        }
    }

    private void showTeamMenu() {
        ensurePlayer();
        Party party = player.getParty();
        if (!party.hasRecruitedMercenary()) {
            System.out.println("В отряде только герой. Наймите союзника на площади.");
            return;
        }
        boolean back = false;
        while (!back) {
            ScreenFrame.openPath(ScreenFrame.Screen.TEAM, "Главное");
            ScreenFrame.subsection("Герой");
            CharacterSheetRenderer.printHeroInTeam(player, levelService, meta, false);
            for (int i = 0; i < Party.MAX_MERC_SLOTS; i++) {
                Mercenary m = party.getMercenary(i);
                if (m != null) {
                    ScreenFrame.subsection(m.getName());
                    CharacterSheetRenderer.printMercenary(m, levelService, meta, player.getRunDamageBonus(), false);
                }
            }
            ScreenFrame.section("Действия");
            System.out.println("  1. Герой — экипировка (меню «Персонаж»)");
            List<Integer> mercSlotMenu = new ArrayList<>();
            int menuNum = 2;
            for (int i = 0; i < Party.MAX_MERC_SLOTS; i++) {
                if (party.getMercenary(i) != null) {
                    mercSlotMenu.add(i);
                    System.out.println("  " + menuNum + ". " + party.getMercenary(i).getName()
                            + " — экипировка и рюкзак");
                    menuNum++;
                }
            }
            ScreenFrame.back();
            ScreenFrame.prompt();
            String ch = readLine();
            if ("0".equals(ch)) {
                back = true;
            } else if ("1".equals(ch)) {
                showCharacterMenu();
            } else {
                try {
                    int pick = Integer.parseInt(ch);
                    if (pick >= 2 && pick < menuNum) {
                        int slot = mercSlotMenu.get(pick - 2);
                        showMercenaryCard(party.getMercenary(slot));
                    } else {
                        System.out.println("Неверный выбор.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Неверный выбор.");
                }
            }
        }
    }

    private void showMercenaryCard(Mercenary mercenary) {
        if (mercenary == null) {
            return;
        }
        boolean back = false;
        while (!back) {
            ScreenFrame.open(ScreenFrame.Screen.MERCENARY, mercenary.getName(), "Главное", "Команда");
            CharacterSheetRenderer.printMercenary(mercenary, levelService, meta, player.getRunDamageBonus(), false);
            ScreenFrame.subsection("Рюкзак (общий)");
            printInventoryBrief();
            ScreenFrame.section("Действия");
            System.out.println("  1. Надеть предмет (номер из рюкзака)");
            System.out.println("  2. Одеть лучшее");
            System.out.println("  3. Снять предмет (слот)");
            System.out.println("  4. Дерево умений"
                    + (mercenary.getAvailableSkillPoints() > 0
                    ? ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW,
                    " [" + mercenary.getAvailableSkillPoints() + "]") : ""));
            ScreenFrame.back();
            ScreenFrame.prompt();
            switch (readLine()) {
                case "1" -> {
                    System.out.print("Номер в рюкзаке: ");
                    int idx = readIndex() - 1;
                    if (mercenary.equipFromInventory(player, idx, meta, player.getRunDamageBonus())) {
                        System.out.println("Экипировано.");
                        saveManager.save(meta, player, autoRestartOnDeath);
                    } else {
                        System.out.println("Не удалось.");
                    }
                }
                case "2" -> equipBestForMercenary(mercenary);
                case "3" -> unequipMercenarySlot(mercenary);
                case "4" -> SkillTreeMenu.showMercenaryMenu(mercenary, player, meta, this::readLine,
                        () -> saveManager.save(meta, player, autoRestartOnDeath));
                case "0" -> back = true;
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    private void unequipMercenarySlot(Mercenary mercenary) {
        System.out.println("1.Оружие 2.Шлем 3.Броня 4.Перчатки 5.Сапоги 6.Кольцо 7.Амулет 0.Отмена");
        System.out.print("Слот: ");
        try {
            int s = Integer.parseInt(readLine());
            if (s == 0) {
                return;
            }
            Equipment.Slot[] slots = Equipment.Slot.values();
            if (s >= 1 && s <= slots.length) {
                if (mercenary.unequipToInventory(player, slots[s - 1])) {
                    mercenary.recalculateStats(meta, player.getRunDamageBonus());
                    System.out.println("Снято в рюкзак.");
                    saveManager.save(meta, player, autoRestartOnDeath);
                } else {
                    System.out.println("Слот пуст или рюкзак полон.");
                }
            }
        } catch (NumberFormatException ignored) {
            System.out.println("Неверный выбор.");
        }
    }

    private void printInventoryBrief() {
        Inventory inv = player.getInventory();
        boolean any = false;
        for (int i = 0; i < inv.getSize(); i++) {
            Item item = inv.get(i);
            if (item != null) {
                any = true;
                System.out.println("  " + (i + 1) + ". " + item.getDisplayName()
                        + ConsoleColors.dim(" — " + item.getStatDescription()));
            }
        }
        if (!any) {
            System.out.println(ConsoleColors.dim("  (пусто)"));
        }
    }

    private void showUpgradeShop() {
        boolean back = false;
        while (!back) {
            ScreenFrame.openPath(ScreenFrame.Screen.UPGRADE, "Главное", "Площадь");
            System.out.println("  Кристаллы душ: " + meta.getSoulCrystals());
            System.out.println();
            ScreenFrame.section("Улучшения");
            printUpgradeLine("  1. Урон", meta.getDamageLevel(), MetaProgression.MAX_DAMAGE_LEVEL, "+5% за уровень", false);
            printUpgradeLine("  2. Здоровье", meta.getHealthLevel(), MetaProgression.MAX_HEALTH_LEVEL, "+5% за уровень", false);
            printUpgradeLine("  3. Шанс дропа", meta.getDropLevel(), MetaProgression.MAX_DROP_LEVEL, "+1% за уровень", true);
            ScreenFrame.back();
            ScreenFrame.prompt();

            switch (readLine()) {
                case "1" -> purchaseUpgrade("урон", meta.upgradeDamage());
                case "2" -> purchaseUpgrade("здоровье", meta.upgradeHealth());
                case "3" -> purchaseUpgrade("шанс дропа", meta.upgradeDrop());
                case "0" -> back = true;
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    private void printUpgradeLine(String name, int level, int max, String effect, boolean dropUpgrade) {
        int cost = level >= max ? 0 : meta.getUpgradeCost(level) + (dropUpgrade ? 2 : 0);
        System.out.printf("%s: %d/%d (%s) — стоимость: %s%n",
                name, level, max, effect, level >= max ? "МАКС" : cost + " крист.");
    }

    private void purchaseUpgrade(String name, boolean success) {
        if (success) {
            ensurePlayer();
            saveManager.save(meta, player, autoRestartOnDeath);
            System.out.println("Улучшение «" + name + "» куплено!");
            player.recalculateStats(meta);
        } else {
            System.out.println("Не удалось купить (максимум или не хватает кристаллов).");
        }
    }

    private void showMagicShop() {
        ensurePlayer();
        boolean back = false;
        while (!back) {
            ScreenFrame.openPath(ScreenFrame.Screen.MAGIC_SHOP, "Главное", "Площадь");
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_MAGENTA,
                    "  ✦  Алхимик Абдолбос торгует странными зельями..."));
            System.out.println("  Золото: " + ConsoleColors.wrap(ConsoleColors.YELLOW, String.valueOf(player.getGold())));
            System.out.println("  Зелья Абдолбос в запасе: " + player.getAbdolbosPotions());
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_CYAN,
                    "  ⌨ Использование в забеге: клавиша B (макс. "
                            + RunEffects.MAX_POTIONS_PER_RUN + " за забег)"));
            if (player.hasAscensionUnlock()) {
                System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                        "  ✓ Ключ возвышения — старт с этажа " + MagicShop.ASCENSION_START_FLOOR));
            }
            ScreenFrame.section("Товары");
            System.out.println("  1. Зелье Абдолбос — в запас (" + MagicShop.ABDOLBOS_COST + " G)");
            System.out.println("     " + ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW,
                    "Выпить в бою: B") + ConsoleColors.dim(" · 50% баф или дебаф · до "
                    + RunEffects.MAX_POTIONS_PER_RUN + " шт. за забег"));
            System.out.println("  2. Ключ возвышения — старт забега с "
                    + MagicShop.ASCENSION_START_FLOOR + " этажа ("
                    + MagicShop.ASCENSION_KEY_COST + " G, навсегда)");
            System.out.println("  3. Свиток удачи — +" + MagicShop.LUCK_DROP_BONUS_PERCENT
                    + "% дроп на след. забег (" + MagicShop.LUCK_SCROLL_COST + " G) ["
                    + player.getLuckScrolls() + "/" + MagicShop.MAX_MAGIC_SCROLLS + "]");
            System.out.println("  4. Свиток жадности — +"
                    + (int) (MagicShop.GREED_GOLD_BONUS * 100) + "% золота на след. забег ("
                    + MagicShop.GREED_SCROLL_COST + " G) ["
                    + player.getGreedScrolls() + "/" + MagicShop.MAX_MAGIC_SCROLLS + "]");
            System.out.println("  5. Свиток тумана — легче этажи 1–" + MagicShop.FOG_SCROLL_FLOORS
                    + " (" + MagicShop.FOG_SCROLL_COST + " G) ["
                    + player.getFogScrolls() + "/" + MagicShop.MAX_MAGIC_SCROLLS + "]");
            System.out.println("  6. Свиток второго дыхания — 1 воскрешение с "
                    + MagicShop.SECOND_BREATH_HEAL_PERCENT + "% HP (" + MagicShop.SECOND_BREATH_SCROLL_COST
                    + " G) [" + player.getSecondBreathScrolls() + "/" + MagicShop.MAX_MAGIC_SCROLLS + "]");
            ScreenFrame.back();
            ScreenFrame.prompt();
            switch (readLine()) {
                case "1" -> buyAbdolbosPotion();
                case "2" -> buyAscensionKey();
                case "3" -> buyMagicScroll("Свиток удачи куплен!", magicShop.buyLuckScroll(player),
                        "Не хватает золота или запас свитков удачи полон.");
                case "4" -> buyMagicScroll("Свиток жадности куплен!", magicShop.buyGreedScroll(player),
                        "Не хватает золота или запас свитков жадности полон.");
                case "5" -> buyMagicScroll("Свиток тумана куплен!", magicShop.buyFogScroll(player),
                        "Не хватает золота или запас свитков тумана полон.");
                case "6" -> buyMagicScroll("Свиток второго дыхания куплен!",
                        magicShop.buySecondBreathScroll(player),
                        "Не хватает золота или запас свитков второго дыхания полон.");
                case "0" -> back = true;
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    private void printMagicScrollStashHint() {
        if (player.getLuckScrolls() > 0) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.GREEN,
                    "В запасе свиток удачи — будет использован в забеге ("
                            + player.getLuckScrolls() + "/" + MagicShop.MAX_MAGIC_SCROLLS + ")"));
        }
        if (player.getGreedScrolls() > 0) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.GREEN,
                    "В запасе свиток жадности — будет использован в забеге ("
                            + player.getGreedScrolls() + "/" + MagicShop.MAX_MAGIC_SCROLLS + ")"));
        }
        if (player.getFogScrolls() > 0) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.GREEN,
                    "В запасе свиток тумана — будет использован в забеге ("
                            + player.getFogScrolls() + "/" + MagicShop.MAX_MAGIC_SCROLLS + ")"));
        }
        if (player.getSecondBreathScrolls() > 0) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.GREEN,
                    "В запасе свиток второго дыхания — будет использован в забеге ("
                            + player.getSecondBreathScrolls() + "/" + MagicShop.MAX_MAGIC_SCROLLS + ")"));
        }
    }

    private void buyMagicScroll(String successMsg, boolean success, String failMsg) {
        if (success) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN, successMsg));
            saveManager.save(meta, player, autoRestartOnDeath);
        } else {
            System.out.println(ConsoleColors.wrap(ConsoleColors.RED, failMsg));
        }
    }

    private void buyAbdolbosPotion() {
        if (magicShop.buyAbdolbosPotion(player)) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                    "Зелье Абдолбос куплено! В запасе: " + player.getAbdolbosPotions()));
            System.out.println(ConsoleColors.dim("Выпейте в забеге клавишей B (до "
                    + RunEffects.MAX_POTIONS_PER_RUN + " раз за забег)."));
            saveManager.save(meta, player, autoRestartOnDeath);
        } else {
            System.out.println(ConsoleColors.wrap(ConsoleColors.RED, "Не хватает золота."));
        }
    }

    private void buyAscensionKey() {
        if (player.hasAscensionUnlock()) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                    "Ключ возвышения уже куплен."));
            return;
        }
        if (magicShop.buyAscensionKey(player)) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                    "Ключ возвышения получен! Теперь можно начинать забег с этажа "
                            + MagicShop.ASCENSION_START_FLOOR + "."));
            saveManager.save(meta, player, autoRestartOnDeath);
        } else {
            System.out.println(ConsoleColors.wrap(ConsoleColors.RED, "Не хватает золота."));
        }
    }

    private void showGoldShop() {
        ensurePlayer();
        boolean back = false;
        while (!back) {
            ScreenFrame.openPath(ScreenFrame.Screen.GOLD_SHOP, "Главное", "Площадь");
            System.out.println("  Ваше золото: " + ConsoleColors.wrap(ConsoleColors.YELLOW,
                    String.valueOf(player.getGold())));
            if (player.getBlessingScrolls() > 0) {
                System.out.println(ConsoleColors.wrap(ConsoleColors.GREEN,
                        "  ✓ Благословений в запасе: " + player.getBlessingScrolls()
                                + "/" + GoldShop.MAX_SCROLLS));
            }
            if (player.getXpScrolls() > 0) {
                System.out.println(ConsoleColors.wrap(ConsoleColors.GREEN,
                        "  ✓ Свитков опыта в запасе: " + player.getXpScrolls()
                                + "/" + GoldShop.MAX_SCROLLS));
            }
            ScreenFrame.section("Товары");
            System.out.println("  1. Благословение — +15% урона на след. забег (" + GoldShop.BLESSING_COST + " G) ["
                    + player.getBlessingScrolls() + "/" + GoldShop.MAX_SCROLLS + "]");
            System.out.println("  2. Свиток опыта — +50% XP на след. забег (" + GoldShop.XP_BOOST_COST + " G) ["
                    + player.getXpScrolls() + "/" + GoldShop.MAX_SCROLLS + "]");
            System.out.println("  4. Тренировка HP +10 навсегда (" + GoldShop.TRAIN_HP_COST + " G) ["
                    + GoldShop.hpTrainingPurchases(player) + "/" + GoldShop.MAX_TRAIN_HP + "]");
            System.out.println("  5. Тренировка ATK +1 навсегда (" + GoldShop.TRAIN_ATK_COST + " G) ["
                    + player.getGoldTrainingAtk() + "/" + GoldShop.MAX_TRAIN_ATK + "]");
            System.out.println("  6. Расширить инвентарь +2 слота (" + GoldShop.EXPAND_INV_COST + " G) ["
                    + player.getInventoryExpansions() + "/" + GoldShop.MAX_INV_EXPANSIONS + "]");
            ScreenFrame.back();
            ScreenFrame.prompt();

            switch (readLine()) {
                case "1" -> buyGoldItem("Благословение куплено!", goldShop.buyBlessing(player),
                        "Запас благословений полон (" + GoldShop.MAX_SCROLLS + ").");
                case "2" -> buyGoldItem("Свиток опыта куплен!", goldShop.buyXpBoost(player),
                        "Запас свитков опыта полон (" + GoldShop.MAX_SCROLLS + ").");
                case "4" -> buyGoldItem("Тренировка HP!", goldShop.buyHpTraining(player));
                case "5" -> buyGoldItem("Тренировка ATK!", goldShop.buyAtkTraining(player));
                case "6" -> buyGoldItem("Инвентарь расширен!", goldShop.buyInventoryExpansion(player));
                case "0" -> back = true;
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    private void buyGoldItem(String successMsg, boolean success) {
        buyGoldItem(successMsg, success, "Не удалось купить (мало золота или лимит).");
    }

    private void buyGoldItem(String successMsg, boolean success, String failMsg) {
        if (success) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN, successMsg));
            player.recalculateStats(meta);
            saveManager.save(meta, player, autoRestartOnDeath);
        } else {
            System.out.println(ConsoleColors.wrap(ConsoleColors.RED, failMsg));
        }
    }

    private void buyChest() {
        if (goldShop.buyMysteryChest(player)) {
            Item item = goldShop.getLastChestItem();
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN, "Сундук открыт!"));
            System.out.println("  Получено: " + item.getDisplayName());
            saveManager.save(meta, player, autoRestartOnDeath);
        } else {
            System.out.println(ConsoleColors.wrap(ConsoleColors.RED,
                    "Не удалось (мало золота или инвентарь полон)."));
        }
    }

    private void showStatistics() {
        ScreenFrame.openPath(ScreenFrame.Screen.STATS, "Главное");
        System.out.println("  Лучший этаж: " + meta.getBestFloor());
        System.out.println("  Всего убийств: " + meta.getTotalKills());
        System.out.println("  Всего смертей: " + meta.getTotalDeaths());
        System.out.println("  Получено золота: " + meta.getTotalGoldEarned());
        System.out.println("  Получено предметов: " + meta.getTotalItemsFound());
        System.out.println("  Время игры: " + formatTime(meta.getTotalPlayTimeSeconds()));
        System.out.println("  Кристаллы душ: " + meta.getSoulCrystals());
        System.out.println("\n  Нажмите Enter...");
        readLine();
    }

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%dч %02dм %02dс", hours, minutes, secs);
    }

    private String readLine() {
        return consoleInput.readLineForMenu();
    }

    /** Убирает команды, оставшиеся в очереди после забега (в т.ч. Q). */
    private void drainMenuInput() {
        consoleInput.drainCombat();
        consoleInput.discardExtraMenuLines();
    }

    private int readIndex() {
        try {
            return Integer.parseInt(readLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
