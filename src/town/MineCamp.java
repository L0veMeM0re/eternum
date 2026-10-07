package town;

import player.Player;
import progression.MetaProgression;
import quest.QuestId;
import quest.QuestLog;
import quest.QuestService;
import util.ConsoleColors;
import util.ScreenFrame;

import java.util.function.Supplier;

/** Шахтёрская застава — цепочка «Огни под горой». */
public final class MineCamp {
    private MineCamp() {
    }

    public static void open(Player player, MetaProgression meta, Supplier<String> readLine, Runnable onSave) {
        if (!QuestService.isMineCampAvailable(player, meta)) {
            ScreenFrame.openPath(ScreenFrame.Screen.MINE_CAMP, "Застава");
            System.out.println(ConsoleColors.dim(
                    "  Дорога к заставе закрыта. Нужен 10-й уровень или рекорд этажа 5+."));
            pause(readLine);
            return;
        }

        boolean back = false;
        while (!back) {
            ScreenFrame.openPath(ScreenFrame.Screen.MINE_CAMP, "Главное", "Мир");
            printBanner();
            ScreenFrame.section("Жители");
            System.out.println("  1. Бранн");
            int journalChoice = 2;
            System.out.println("  " + journalChoice + ". Журнал поручений");
            ScreenFrame.back();
            ScreenFrame.prompt();
            String input = readLine.get().trim();
            if ("0".equals(input)) {
                back = true;
            } else if ("1".equals(input)) {
                talkForeman(player, readLine, onSave);
            } else if (String.valueOf(journalChoice).equals(input)) {
                showJournal(player, readLine);
            } else {
                System.out.println("  Неверный выбор.");
            }
        }
    }

    private static void printBanner() {
        System.out.println();
        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_CYAN,
                "  ⛏ Костры, руда, искры в породе. Воздух режет глаза."));
    }

    private static void showJournal(Player player, Supplier<String> readLine) {
        ScreenFrame.openPath(ScreenFrame.Screen.MINE_CAMP, "Журнал");
        System.out.println(QuestService.formatJournalLine(player.getQuestLog()));
        pause(readLine);
    }

    private static void talkForeman(Player player, Supplier<String> readLine, Runnable onSave) {
        QuestLog log = player.getQuestLog();
        ScreenFrame.openPath(ScreenFrame.Screen.MINE_CAMP, "Бранн");

        if (log.isCompleted(QuestId.GOLEM_GUARD)) {
            printLines(
                    "«Страж повержен — шахта дышит спокойнее.",
                    "Иди на площадь, к Орису. Он ждёт.»");
            pause(readLine);
            return;
        }

        if (log.isGolemGuardReady()) {
            printLines(
                    "«Каменный страж пал! Вот тебе плата и руда.",
                    "На площади найди Ориса — он научит работать с душой оружия.»");
            QuestService.completeGolemGuard(player);
            System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                    "  +" + QuestService.GOLEM_GUARD_REWARD_GOLD + " золота, +"
                            + QuestService.GOLEM_GUARD_REWARD_ORE + " руды"));
            onSave.run();
            pause(readLine);
            return;
        }

        if (log.isGolemGuardAccepted()) {
            printLines(
                    "«Страж в пещере на площади — каменная гора.",
                    "Победи его хотя бы раз, пока поручение открыто.",
                    log.isGolemDefeatedForQuest() ? "Готов сдавать отчёт." : "Ещё не побеждён.»");
            if (log.isGolemDefeatedForQuest()) {
                System.out.println();
                System.out.println("  1. Завершить поручение");
                System.out.println("  0. Назад");
                ScreenFrame.prompt();
                if ("1".equals(readLine.get().trim())) {
                    QuestService.completeGolemGuard(player);
                    System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                            "  +" + QuestService.GOLEM_GUARD_REWARD_GOLD + " золота, +"
                                    + QuestService.GOLEM_GUARD_REWARD_ORE + " руды"));
                    onSave.run();
                }
            }
            pause(readLine);
            return;
        }

        if (log.isCompleted(QuestId.MINE_SHIFT)) {
            printLines(
                    "«Руда на месте. Теперь страж.",
                    "В пещере на площади — каменный горняк. Разбей его один раз.",
                    "Без этого Петр не возьмёт тебя в ученики.»");
            System.out.println();
            System.out.println("  1. Принять поручение");
            System.out.println("  2. Не сейчас");
            ScreenFrame.prompt();
            if ("1".equals(readLine.get().trim())) {
                log.acceptGolemGuard();
                System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                        "  Поручение принято: " + QuestId.GOLEM_GUARD.getTitle()));
                onSave.run();
            }
            pause(readLine);
            return;
        }

        if (log.isMineShiftAccepted()) {
            boolean loop = true;
            while (loop) {
                ScreenFrame.openPath(ScreenFrame.Screen.MINE_CAMP, "Бранн");
                printLines(
                        "«Нужно " + QuestLog.MINE_ORE_TARGET + " кусков сырой руды.",
                        "Сейчас у тебя: " + player.getRawOre() + ".»");
                System.out.println();
                if (log.isMineShiftReady(player.getRawOre())) {
                    System.out.println("  1. Завершить поручение");
                } else {
                    System.out.println("  1. Сдать руду (недостаточно)");
                }
                System.out.println("  0. Назад");
                ScreenFrame.prompt();
                switch (readLine.get().trim()) {
                    case "1" -> {
                        if (log.isMineShiftReady(player.getRawOre())) {
                            QuestService.completeMineShift(player);
                            System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                                    "  +" + QuestService.MINE_SHIFT_REWARD_GOLD + " золота, -"
                                            + QuestLog.MINE_ORE_TARGET + " руды"));
                            onSave.run();
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
            return;
        }

        printLines(
                "«Эй, новичок. Первая смена — тридцать кусков руды.",
                "Не золото, не камни — сырая руда из шахты или с зачисток.",
                "Справишься — расскажу про каменного стража.»");
        System.out.println();
        System.out.println("  1. Принять поручение");
        System.out.println("  2. Не сейчас");
        ScreenFrame.prompt();
        if ("1".equals(readLine.get().trim())) {
            log.acceptMineShift();
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                    "  Поручение принято: " + QuestId.MINE_SHIFT.getTitle()));
            onSave.run();
        }
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
