package town;

import inventory.Equipment;
import player.Player;
import progression.MetaProgression;
import quest.QuestId;
import quest.QuestItemTurnIn;
import quest.QuestLog;
import quest.QuestService;
import util.ConsoleColors;
import util.ScreenFrame;

import java.util.function.Supplier;

/** Деревня у городских ворот — NPC и сюжетные квесты. */
public final class Village {
    private Village() {
    }

    public static void open(Player player, MetaProgression meta, Supplier<String> readLine, Runnable onSave) {
        boolean back = false;
        while (!back) {
            ScreenFrame.openPath(ScreenFrame.Screen.VILLAGE, "Главное", "Мир");
            printBanner();
            QuestLog log = player.getQuestLog();
            ScreenFrame.section("Жители");
            System.out.println("  1. Эльвина");
            int garrickChoice = -1;
            int elaraChoice = -1;
            int next = 2;
            if (log.isMilitiaUnlocked()) {
                garrickChoice = next++;
                System.out.println("  " + garrickChoice + ". Гаррик");
            }
            if (log.isHealerUnlocked()) {
                elaraChoice = next++;
                System.out.println("  " + elaraChoice + ". Элара");
            }
            int starostaChoice = -1;
            if (log.isStarostaUnlocked()) {
                starostaChoice = next++;
                System.out.println("  " + starostaChoice + ". Тален");
            }
            int journalChoice = next;
            System.out.println("  " + journalChoice + ". Журнал поручений");
            ScreenFrame.back();
            ScreenFrame.prompt();
            String input = readLine.get().trim();
            if ("0".equals(input)) {
                back = true;
            } else if ("1".equals(input)) {
                talkGrandmother(player, readLine, onSave);
            } else if (garrickChoice > 0 && String.valueOf(garrickChoice).equals(input)) {
                talkMilitiaman(player, readLine, onSave);
            } else if (elaraChoice > 0 && String.valueOf(elaraChoice).equals(input)) {
                talkHealer(player, readLine, onSave);
            } else if (starostaChoice > 0 && String.valueOf(starostaChoice).equals(input)) {
                TradeRoad.talkStarosta(player, meta, readLine, onSave);
            } else if (String.valueOf(journalChoice).equals(input)) {
                showJournal(player, readLine);
            } else {
                System.out.println("  Неверный выбор.");
            }
        }
    }

    private static void printBanner() {
        System.out.println();
        System.out.println(ConsoleColors.wrap(ConsoleColors.GREEN,
                "  🏡 Серебряный Брод. Дым еле теплится — семь ночей без огня на окраине. У подножия — чёрная башня."));
    }

    private static void showJournal(Player player, Supplier<String> readLine) {
        ScreenFrame.openPath(ScreenFrame.Screen.VILLAGE, "Журнал");
        System.out.println(QuestService.formatJournalLine(player.getQuestLog()));
        System.out.println("  Enter...");
        readLine.get();
    }

    private static void talkGrandmother(Player player, Supplier<String> readLine, Runnable onSave) {
        QuestLog log = player.getQuestLog();
        ScreenFrame.openPath(ScreenFrame.Screen.VILLAGE, "Эльвина");

        if (log.isCompleted(QuestId.GOBLIN_HUNT)) {
            printLines(
                    "«Спасибо. Заходи — каша будет.",
                    "Только… про Лиру при людях не начинай. Серые ещё вернутся.»");
            pause(readLine);
            return;
        }

        if (log.isGoblinHuntReady()) {
            printLines(
                    "«Слава небесам… хоть куры перестали прятаться.»",
                    "«Держи золото. И сходи к Гаррику у ворот — дружина без тебя не справится.»");
            QuestService.completeGoblinHunt(player);
            System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                    "  +" + QuestService.GOBLIN_REWARD_GOLD + " золота"));
            onSave.run();
            pause(readLine);
            return;
        }

        if (log.isGoblinHuntAccepted()) {
            printLines(
                    "«Ну? Тварей стало меньше?»",
                    "«Считай только в забегах, у башни.»",
                    "«Прогресс: " + log.getGoblinKills() + " / " + QuestLog.GOBLIN_TARGET + ".»");
            pause(readLine);
            return;
        }

        printLines(
                "«Тише, милок… не громи. После… после тех в плащах и так нервы на взводе.»",
                "«Из башни лезут твари — к броду, к дворам. Десять гоблинов в забегах,",
                "там, где ты зачищаешь этажи.»",
                "«Плату дам. Только не спрашивай у дружины, откуда у тебя сила. Согласен?»");
        System.out.println();
        System.out.println("  1. Принять поручение");
        System.out.println("  2. Не сейчас");
        ScreenFrame.prompt();
        if ("1".equals(readLine.get().trim())) {
            log.acceptGoblinHunt();
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                    "  Поручение принято: " + QuestId.GOBLIN_HUNT.getTitle()));
            onSave.run();
        }
        pause(readLine);
    }

    private static void talkMilitiaman(Player player, Supplier<String> readLine, Runnable onSave) {
        QuestLog log = player.getQuestLog();
        ScreenFrame.openPath(ScreenFrame.Screen.VILLAGE, "Гаррик");

        if (log.isCompleted(QuestId.MILITIA_GEAR)) {
            talkIlyaAfterMilitia(player, readLine, onSave);
            return;
        }

        if (!log.isMilitiaQuestAccepted()) {
            printLines(
                    "«Ты тот, кого Эльвина звала? Гоблинов убавил — вижу.»",
                    "«Корона велела: дружина должна выглядеть… укреплённой.",
                    "Пять клинков и пять нагрудников — из рюкзака, не с плеч.»",
                    "«В книге запишем как дар короны. Тебе — золото и камень. Берёшься?»");
            System.out.println();
            System.out.println("  1. Принять поручение");
            System.out.println("  2. Не сейчас");
            ScreenFrame.prompt();
            if ("1".equals(readLine.get().trim())) {
                log.acceptMilitiaQuest();
                System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                        "  Поручение принято: " + QuestId.MILITIA_GEAR.getTitle()));
                onSave.run();
            }
            pause(readLine);
            return;
        }

        boolean loop = true;
        while (loop) {
            ScreenFrame.openPath(ScreenFrame.Screen.VILLAGE, "Гаррик");
            printLines(
                    "«По одному — так в журнале чище.»",
                    "«Оружие: " + log.getWeaponsDelivered() + "/" + QuestLog.WEAPONS_TARGET
                            + " | Броня: " + log.getArmorDelivered() + "/" + QuestLog.ARMOR_TARGET + ".»");
            System.out.println();
            System.out.println("  1. Сдать оружие");
            System.out.println("  2. Сдать нагрудник (броня)");
            if (log.isMilitiaGearReady()) {
                System.out.println("  3. Завершить поручение");
            }
            System.out.println("  0. Назад");
            ScreenFrame.prompt();
            switch (readLine.get().trim()) {
                case "1" -> {
                    if (log.getWeaponsDelivered() >= QuestLog.WEAPONS_TARGET) {
                        System.out.println("  Оружия уже достаточно.");
                        pause(readLine);
                    } else if (QuestItemTurnIn.pickAndRemove(player, Equipment.Slot.WEAPON, readLine)) {
                        log.deliverWeapon();
                        onSave.run();
                    }
                }
                case "2" -> {
                    if (log.getArmorDelivered() >= QuestLog.ARMOR_TARGET) {
                        System.out.println("  Брони уже достаточно.");
                        pause(readLine);
                    } else if (QuestItemTurnIn.pickAndRemove(player, Equipment.Slot.ARMOR, readLine)) {
                        log.deliverArmor();
                        onSave.run();
                    }
                }
                case "3" -> {
                    if (log.isMilitiaGearReady()) {
                        printLines(
                                "«Сойдёт. Деревня увидит — мы не одни.»",
                                "«Держи плату, камень и случайный клинок из трофеев.",
                                "И слушай: ночью лес снова не спит.»");
                        QuestService.completeMilitiaGear(player);
                        System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                                "  +" + QuestService.MILITIA_REWARD_GOLD + " золота"));
                        onSave.run();
                        pause(readLine);
                        loop = false;
                    } else {
                        System.out.println("  Ещё не всё сдано.");
                        pause(readLine);
                    }
                }
                case "0" -> loop = false;
                default -> System.out.println("  Неверный выбор.");
            }
        }
    }

    private static void talkIlyaAfterMilitia(Player player, Supplier<String> readLine, Runnable onSave) {
        QuestLog log = player.getQuestLog();
        if (!log.isCompleted(QuestId.NIGHT_PACK)) {
            talkNightPack(player, readLine, onSave);
            return;
        }
        if (!log.isCompleted(QuestId.BITTER_POTION)) {
            printLines(
                    "«Сначала помоги Эларе у колодца — без её отвара в башню самоубийство.",
                    "Она ждёт тебя.»");
            pause(readLine);
            return;
        }
        talkIlyaTowerQuests(player, readLine, onSave);
    }

    private static void talkIlyaTowerQuests(Player player, Supplier<String> readLine, Runnable onSave) {
        QuestLog log = player.getQuestLog();

        if (log.isCompleted(QuestId.CAVE_BONES)) {
            printLines(
                    "«Кости улеглись. Но шёпот на болоте — другая песня. Берегись.»");
            pause(readLine);
            return;
        }

        if (log.isFloor20ReportReady()) {
            printLines(
                    "«Живой с рубежа-20 — редкость. Держи золото.»",
                    "«Теперь — кости в пещерах. Десять скелетов. Потом открою путь к болоту и Корвину.»");
            QuestService.completeFloor20Report(player);
            System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                    "  +" + QuestService.FLOOR_20_REWARD_GOLD + " золота"));
            onSave.run();
            pause(readLine);
            return;
        }

        if (log.isCaveBonesReady()) {
            printLines(
                    "«Сделано. Держи плату и руду — и трофей охотника с костей.»",
                    "«Иди к болоту — Корвин. Он… многое слышит, чего Храм стёр у других.»");
            QuestService.completeCaveBones(player);
            System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                    "  +" + QuestService.CAVE_BONES_REWARD_GOLD + " золота, +"
                            + QuestService.CAVE_BONES_REWARD_ORE + " руды"));
            onSave.run();
            pause(readLine);
            return;
        }

        if (log.isCaveBonesAccepted()) {
            printLines(
                    "«Это не просто мертвецы.",
                    "Прогресс: " + log.getSkeletonKills() + " / " + QuestLog.SKELETON_TARGET + ".»");
            pause(readLine);
            return;
        }

        if (log.isCompleted(QuestId.FLOOR_20_REPORT)) {
            printLines(
                    "«Доклад ушёл в город. Ниже рубежа — кости. Старые охотники, поднятые туманом.»",
                    "«Десять в забегах — и пусть тебя примет Корвин на болотной заставе.»");
            System.out.println();
            System.out.println("  1. Принять поручение");
            System.out.println("  2. Не сейчас");
            ScreenFrame.prompt();
            if ("1".equals(readLine.get().trim())) {
                log.acceptCaveBones();
                System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                        "  Поручение принято: " + QuestId.CAVE_BONES.getTitle()));
                onSave.run();
            }
            pause(readLine);
            return;
        }

        if (log.isFloor20ReportAccepted()) {
            printLines(
                    "«Доклад пишут на рубеже-20, в этом походе. Не рекорд в меню — именно сейчас.»",
                    log.isFloor20ReachedInRun()
                            ? "«Был там? Оформлю доклад.»"
                            : "«Ещё не дотянул — возвращайся.»");
            if (log.isFloor20ReachedInRun()) {
                System.out.println();
                System.out.println("  1. Сдать доклад");
                System.out.println("  0. Назад");
                ScreenFrame.prompt();
                if ("1".equals(readLine.get().trim())) {
                    QuestService.completeFloor20Report(player);
                    System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                            "  +" + QuestService.FLOOR_20_REWARD_GOLD + " золота"));
                    onSave.run();
                }
            }
            pause(readLine);
            return;
        }

        printLines(
                "«Отвар сработал — вижу по лицам в броде.»",
                "«Корона требует доклад: дойти до двадцатого этажа за один забег и вернуться живым.",
                "Ниже рубежа — пещеры, там хуже.»",
                "«Не спрашиваю, кто тебя направляет. Берёшься?»");
        System.out.println();
        System.out.println("  1. Принять поручение");
        System.out.println("  2. Не сейчас");
        ScreenFrame.prompt();
        if ("1".equals(readLine.get().trim())) {
            log.acceptFloor20Report();
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                    "  Поручение принято: " + QuestId.FLOOR_20_REPORT.getTitle()));
            onSave.run();
        }
        pause(readLine);
    }

    private static void talkNightPack(Player player, Supplier<String> readLine, Runnable onSave) {
        QuestLog log = player.getQuestLog();

        if (log.isCompleted(QuestId.NIGHT_PACK)) {
            printLines(
                    "«Пока тихо. Если снова пойдёт волна — найду тебя. Элара тоже благодарна.»");
            pause(readLine);
            return;
        }

        if (log.isNightPackReady()) {
            printLines(
                    "«Дышать легче.»",
                    "«Держи плату и руду. Иди к колодцу — к Эларе. Скажи, что Гаррик послал.»");
            QuestService.completeNightPack(player);
            System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                    "  +" + QuestService.NIGHT_PACK_REWARD_GOLD + " золота, +"
                            + QuestService.NIGHT_PACK_REWARD_ORE + " руды"));
            onSave.run();
            pause(readLine);
            return;
        }

        if (log.isNightPackAccepted()) {
            printLines(
                    "«Как ночь?»",
                    "«Только в забегах, нижние этажи башни.»",
                    "«Волки: " + log.getWolfKills() + " / " + QuestLog.WOLF_TARGET
                            + " | Нетопыри: " + log.getBatKills() + " / " + QuestLog.BAT_TARGET + ".»");
            pause(readLine);
            return;
        }

        printLines(
                "«Семь ночей — ни огня, ни песни. Указ Серых. А зверь не читает указов.»",
                "«Волки, нетопыри… будто их тянет к тому, что осталось после искры.»",
                "«Восемь волков и восемь нетопырей в забегах.",
                "Потом — к Эларе, она лечит тех, кого искра задела.»");
        System.out.println();
        System.out.println("  1. Принять поручение");
        System.out.println("  2. Не сейчас");
        ScreenFrame.prompt();
        if ("1".equals(readLine.get().trim())) {
            log.acceptNightPack();
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                    "  Поручение принято: " + QuestId.NIGHT_PACK.getTitle()));
            onSave.run();
        }
        pause(readLine);
    }

    private static void talkHealer(Player player, Supplier<String> readLine, Runnable onSave) {
        QuestLog log = player.getQuestLog();
        ScreenFrame.openPath(ScreenFrame.Screen.VILLAGE, "Элара");

        if (log.isCompleted(QuestId.BITTER_POTION)) {
            printLines(
                    "«Отвар на полке. Поранишься — приду. И не говори в трактире про искру — там уши Храма.»",
                    "«Если Гаррик снова пошлёт тебя глубже в башню — не отказывайся.»");
            pause(readLine);
            return;
        }

        if (!log.isBitterPotionAccepted()) {
            printLines(
                    "«От Гаррика? Хорошо. Раненых после ночи — вон сколько.»",
                    "«Официально это порча после искры. Мне нужна руда — двадцать кусков.",
                    "Жжёт горло, зато держит тело.»",
                    "«Принесёшь — заплачу. И… ты брат Лиры? Её увезли в Башню Молчания.",
                    "Я не верю Храму.»");
            System.out.println();
            System.out.println("  1. Принять поручение");
            System.out.println("  2. Не сейчас");
            ScreenFrame.prompt();
            if ("1".equals(readLine.get().trim())) {
                log.acceptBitterPotion();
                System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                        "  Поручение принято: " + QuestId.BITTER_POTION.getTitle()));
                onSave.run();
            }
            pause(readLine);
            return;
        }

        boolean loop = true;
        while (loop) {
            ScreenFrame.openPath(ScreenFrame.Screen.VILLAGE, "Элара");
            printLines(
                    "«Нужно 20. Сейчас: " + player.getRawOre() + ".»",
                    "«Шахта на площади — там руда, но опасно.»");
            System.out.println();
            if (log.isBitterPotionReady(player.getRawOre())) {
                System.out.println("  1. Завершить поручение");
            } else {
                System.out.println("  1. Сдать руду (нужно ещё "
                        + Math.max(0, QuestLog.ORE_TARGET - player.getRawOre()) + ")");
            }
            System.out.println("  0. Назад");
            ScreenFrame.prompt();
            switch (readLine.get().trim()) {
                case "1" -> {
                    if (log.isBitterPotionReady(player.getRawOre())) {
                        printLines(
                                "«Запах — как ад. Зато держит.»",
                                "«Возьми золото и камень. Гаррик пошлёт тебя в башню — скажи, что отвар готов.»",
                                "«Лира… если узнаешь правду — не мсти вслух. Серые слышат.»");
                        QuestService.completeBitterPotion(player);
                        System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                                "  +" + QuestService.BITTER_POTION_REWARD_GOLD + " золота, -"
                                        + QuestLog.ORE_TARGET + " руды"));
                        onSave.run();
                        pause(readLine);
                        loop = false;
                    } else {
                        System.out.println("  Руды пока недостаточно.");
                        pause(readLine);
                    }
                }
                case "0" -> loop = false;
                default -> System.out.println("  Неверный выбор.");
            }
        }
    }

    private static void printLines(String... lines) {
        System.out.println();
        for (String line : lines) {
            System.out.println("  " + line);
        }
    }

    private static void pause(Supplier<String> readLine) {
        System.out.println();
        System.out.println("  Enter...");
        readLine.get();
    }
}
