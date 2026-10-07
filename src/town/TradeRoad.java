package town;

import inventory.Equipment;
import item.Rarity;
import player.Player;
import progression.MetaProgression;
import quest.QuestId;
import quest.QuestItemTurnIn;
import quest.QuestLog;
import quest.QuestService;
import util.ConsoleColors;
import util.ScreenFrame;

import java.util.function.Supplier;

/** Тёмный тракт между городом и деревней — Тален. */
public final class TradeRoad {
    private TradeRoad() {
    }

    public static void open(Player player, MetaProgression meta, Supplier<String> readLine, Runnable onSave) {
        if (!player.getQuestLog().isStarostaUnlocked()) {
            ScreenFrame.openPath(ScreenFrame.Screen.TRADE_ROAD, "Тракт");
            System.out.println(ConsoleColors.dim("  Тракт молчит. Сначала — печать у Корвина на болоте."));
            pause(readLine);
            return;
        }

        boolean back = false;
        while (!back) {
            ScreenFrame.openPath(ScreenFrame.Screen.TRADE_ROAD, "Главное", "Мир");
            printBanner();
            ScreenFrame.section("Жители");
            System.out.println("  1. Тален");
            int journalChoice = 2;
            System.out.println("  " + journalChoice + ". Журнал поручений");
            ScreenFrame.back();
            ScreenFrame.prompt();
            String input = readLine.get().trim();
            if ("0".equals(input)) {
                back = true;
            } else if ("1".equals(input)) {
                talkStarosta(player, meta, readLine, onSave);
            } else if (String.valueOf(journalChoice).equals(input)) {
                showJournal(player, readLine);
            } else {
                System.out.println("  Неверный выбор.");
            }
        }
    }

    public static void talkStarosta(Player player, MetaProgression meta, Supplier<String> readLine, Runnable onSave) {
        QuestLog log = player.getQuestLog();
        ScreenFrame.openPath(ScreenFrame.Screen.TRADE_ROAD, "Тален");

        if (log.isCompleted(QuestId.BOSS_FLOOR_20)) {
            printLines(
                    "«Тракт открыт. Купцы едут.»",
                    "«Про Лиру… в столице шепчут про Башню Молчания у ворот.",
                    "Когда будешь готов — приходи. Дальше путь не только мечом.»");
            pause(readLine);
            return;
        }

        if (log.isBossFloor20Ready()) {
            printLines(
                    "«Пал! Туман с дороги сходит.»",
                    "«Кристалл душ — и скидка в столичных лавках. Ты свой — пока Храм не заинтересуется.»");
            QuestService.completeBossFloor20(meta, player);
            System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                    "  +" + QuestService.BOSS_FLOOR_20_SOUL_CRYSTALS + " кристалл душ, -"
                            + QuestService.ROAD_SHOP_DISCOUNT_PERCENT + "% в лавках золота"));
            onSave.run();
            pause(readLine);
            return;
        }

        if (log.isStolenCargoReady()) {
            printLines(
                    "«Оружие на месте — груз можно вернуть торговцам.",
                    "Но главная беда — некромант на двадцатом этаже.",
                    "Он гонит орков на тракт. Убей его в забеге — и сдай мне весть.»");
            QuestService.completeStolenCargo(player);
            System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                    "  +" + QuestService.STOLEN_CARGO_REWARD_GOLD + " золота"));
            onSave.run();
            pause(readLine);
            return;
        }

        if (log.isStolenCargoAccepted()) {
            boolean loop = true;
            while (loop) {
                ScreenFrame.openPath(ScreenFrame.Screen.TRADE_ROAD, "Тален");
                printLines(
                        "«Три синих клинка или лучше — из сумки, не с пояса.",
                        "Сдано: " + log.getRoadWeaponsDelivered() + " / "
                                + QuestLog.ROAD_WEAPON_TARGET + ".»");
                System.out.println();
                System.out.println("  1. Сдать оружие [Синий]+");
                if (log.isStolenCargoReady()) {
                    System.out.println("  2. Завершить поручение");
                }
                System.out.println("  0. Назад");
                ScreenFrame.prompt();
                switch (readLine.get().trim()) {
                    case "1" -> {
                        if (QuestItemTurnIn.pickMinRarityForSlot(
                                player, Equipment.Slot.WEAPON, Rarity.RARE, readLine)) {
                            log.deliverRoadWeapon();
                            onSave.run();
                        }
                    }
                    case "2" -> {
                        if (log.isStolenCargoReady()) {
                            QuestService.completeStolenCargo(player);
                            onSave.run();
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
            return;
        }

        if (log.isRoadRaidersReady()) {
            printLines(
                    "«Двенадцать орков — счёт закрыт. Держи плату и добычу с тракта.",
                    "Но повозку так и не вернули. Говорят, оружие спрятали в лесу.",
                    "Принеси три клинка — синих или лучше.»");
            QuestService.completeRoadRaiders(player);
            System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                    "  +" + QuestService.ROAD_RAIDERS_REWARD_GOLD + " золота"));
            onSave.run();
            pause(readLine);
            return;
        }

        if (log.isRoadRaidersAccepted() && !log.isCompleted(QuestId.ROAD_RAIDERS)) {
            printLines(
                    "«Орки на тракте — как грибы после дождя.",
                    "Считай только в забегах, лес и пещеры.",
                    "Прогресс: " + log.getRoadOrcKills() + " / " + QuestLog.ROAD_ORC_TARGET + ".»");
            pause(readLine);
            return;
        }

        if (log.isCompleted(QuestId.BROKEN_WAGON) && !log.isCompleted(QuestId.ROAD_RAIDERS)) {
            printLines(
                    "«Завал убрали — повозка проехала.",
                    "Но орки снова напали. Двенадцать голов — и поговорим о краденом грузе.»");
            System.out.println();
            System.out.println("  1. Принять поручение");
            System.out.println("  2. Не сейчас");
            ScreenFrame.prompt();
            if ("1".equals(readLine.get().trim())) {
                log.acceptRoadRaiders();
                System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                        "  Поручение принято: " + QuestId.ROAD_RAIDERS.getTitle()));
                onSave.run();
            }
            pause(readLine);
            return;
        }

        if (log.isBrokenWagonAccepted() && !log.isCompleted(QuestId.BROKEN_WAGON)) {
            boolean loop = true;
            while (loop) {
                ScreenFrame.openPath(ScreenFrame.Screen.TRADE_ROAD, "Тален");
                printLines(
                        "«Нужно " + QuestLog.ROAD_WAGON_ORE + " кусков руды — засыпать колею.",
                        "Сейчас у тебя: " + player.getRawOre() + ".»");
                System.out.println();
                if (log.isBrokenWagonReady(player.getRawOre())) {
                    System.out.println("  1. Завершить поручение");
                } else {
                    System.out.println("  1. Сдать руду (недостаточно)");
                }
                System.out.println("  0. Назад");
                ScreenFrame.prompt();
                if ("1".equals(readLine.get().trim())) {
                    if (log.isBrokenWagonReady(player.getRawOre())) {
                        QuestService.completeBrokenWagon(player);
                        System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                                "  +" + QuestService.BROKEN_WAGON_REWARD_GOLD + " золота, -"
                                        + QuestLog.ROAD_WAGON_ORE + " руды"));
                        onSave.run();
                        loop = false;
                    } else {
                        System.out.println("  Руды пока недостаточно.");
                        pause(readLine);
                    }
                } else if ("0".equals(readLine.get().trim())) {
                    loop = false;
                }
            }
            return;
        }

        if (log.isCompleted(QuestId.ROAD_RAIDERS)) {
            printLines(
                    "«Орки отступили, но груз всё ещё у воров.",
                    "Три оружия — синих или лучше — из сумки. Не с себя снимай.»");
            System.out.println();
            System.out.println("  1. Принять поручение");
            System.out.println("  2. Не сейчас");
            ScreenFrame.prompt();
            if ("1".equals(readLine.get().trim())) {
                log.acceptStolenCargo();
                System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                        "  Поручение принято: " + QuestId.STOLEN_CARGO.getTitle()));
                onSave.run();
            }
            pause(readLine);
            return;
        }

        if (log.isCompleted(QuestId.STOLEN_CARGO)) {
            printLines(
                    "«Груз нашли — молодец.",
                    "Последнее: на двадцатом этаже некромант гонит орков на наш тракт.",
                    "Убей его в забеге и вернись.»");
            System.out.println();
            System.out.println("  1. Принять поручение");
            System.out.println("  2. Не сейчас");
            ScreenFrame.prompt();
            if ("1".equals(readLine.get().trim())) {
                log.acceptBossFloor20();
                System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                        "  Поручение принято: " + QuestId.BOSS_FLOOR_20.getTitle()));
                onSave.run();
            }
            pause(readLine);
            return;
        }

        if (log.isBossFloor20Accepted()) {
            printLines(
                    "«Некромант ждёт на двадцатом этаже башни.",
                    "Победи его в забеге — только тогда тракт будет наш.",
                    log.isBossFloor20Defeated()
                            ? "Ты уже победил его — сдай отчёт."
                            : "Ещё не побеждён.»");
            if (log.isBossFloor20Defeated()) {
                System.out.println();
                System.out.println("  1. Сдать отчёт");
                System.out.println("  0. Назад");
                ScreenFrame.prompt();
                if ("1".equals(readLine.get().trim())) {
                    QuestService.completeBossFloor20(meta, player);
                    onSave.run();
                }
            }
            pause(readLine);
            return;
        }

        printLines(
                "«Ты тот, кто запечатал болото? Слава небесам.",
                "Пока ты лез в башню, тракт между городом и деревней умирает.",
                "Повозка с рудой разбилась — нужно десять кусков, чтоб засыпать колею.",
                "Поможешь — заплачу и открою дело дальше.»");
        System.out.println();
        System.out.println("  1. Принять поручение");
        System.out.println("  2. Не сейчас");
        ScreenFrame.prompt();
        if ("1".equals(readLine.get().trim())) {
            log.acceptBrokenWagon();
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                    "  Поручение принято: " + QuestId.BROKEN_WAGON.getTitle()));
            onSave.run();
        }
        pause(readLine);
    }

    private static void printBanner() {
        System.out.println();
        System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                "  🛤 Колея на столицу. Следы копыт, обломки, запах крови."));
    }

    private static void showJournal(Player player, Supplier<String> readLine) {
        ScreenFrame.openPath(ScreenFrame.Screen.TRADE_ROAD, "Журнал");
        System.out.println(QuestService.formatJournalLine(player.getQuestLog()));
        pause(readLine);
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
