package town;

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

/** Застава у болота — Корвин и цепочка «Глубже этажей». */
public final class SwampOutpost {
    private SwampOutpost() {
    }

    public static void open(Player player, MetaProgression meta, Supplier<String> readLine, Runnable onSave) {
        QuestLog log = player.getQuestLog();
        if (!log.isHermitUnlocked()) {
            ScreenFrame.openPath(ScreenFrame.Screen.SWAMP_OUTPOST, "Застава");
            System.out.println(ConsoleColors.dim("  Тропа в туман. Сюда пускают, когда дружина доверяет."));
            pause(readLine);
            return;
        }

        boolean back = false;
        while (!back) {
            ScreenFrame.openPath(ScreenFrame.Screen.SWAMP_OUTPOST, "Главное", "Мир");
            printBanner();
            ScreenFrame.section("Жители");
            System.out.println("  1. Корвин");
            int journalChoice = 2;
            System.out.println("  " + journalChoice + ". Журнал поручений");
            ScreenFrame.back();
            ScreenFrame.prompt();
            String input = readLine.get().trim();
            if ("0".equals(input)) {
                back = true;
            } else if ("1".equals(input)) {
                talkHermit(player, meta, readLine, onSave);
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
                "  🌫 Камыш, болотный огонь и чужой шёпот — не ветер."));
    }

    private static void showJournal(Player player, Supplier<String> readLine) {
        ScreenFrame.openPath(ScreenFrame.Screen.SWAMP_OUTPOST, "Журнал");
        System.out.println(QuestService.formatJournalLine(player.getQuestLog()));
        pause(readLine);
    }

    private static void talkHermit(Player player, MetaProgression meta, Supplier<String> readLine, Runnable onSave) {
        QuestLog log = player.getQuestLog();
        ScreenFrame.openPath(ScreenFrame.Screen.SWAMP_OUTPOST, "Корвин");

        if (log.isCompleted(QuestId.MIST_SEAL)) {
            printLines(
                    "«Печать держится. Пока.»",
                    "«Иди к Талену — тракт на столицу откроет. Скажи: Корвин отправил.»",
                    "«И не светись цифрами в броде. Очистители любят аномалий.»");
            pause(readLine);
            return;
        }

        if (log.isCompleted(QuestId.SWAMP_WHISPER) && log.isMistSealAccepted()) {
            printLines(
                    "«Ведьмы замолкли, но туман ещё помнит.",
                    "Принеси вещь с сильной аурой — эпическую или крепче, из сумки, не надетую.",
                    "Ею запечатаем то, что шепчет по ночам.»");
            System.out.println();
            System.out.println("  1. Сдать предмет");
            System.out.println("  0. Назад");
            ScreenFrame.prompt();
            if ("1".equals(readLine.get().trim())) {
                if (QuestItemTurnIn.pickMinRarityAnySlot(player, Rarity.EPIC, readLine)) {
                    QuestService.completeMistSeal(meta, player);
                    System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                            "  +" + QuestService.MIST_SEAL_SOUL_CRYSTALS + " кристалл душ, +1 свиток благословения"));
                    onSave.run();
                }
            } else {
                pause(readLine);
            }
            return;
        }

        if (log.isSwampWhisperReady()) {
            printLines(
                    "«Шесть ведьм — и болото снова дышит ровно.",
                    "Держи плату и камень. Осталось одно: печать.",
                    "Принеси вещь с сильной аурой — эпическую или лучше, из сумки.»");
            QuestService.completeSwampWhisper(player);
            System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                    "  +" + QuestService.SWAMP_WHISPER_REWARD_GOLD + " золота"));
            onSave.run();
            pause(readLine);
            return;
        }

        if (log.isSwampWhisperAccepted()) {
            printLines(
                    "«Слышишь? Цифры… ворота… не слушай долго.»",
                    "«Ведьмы: " + log.getWitchKills() + " / " + QuestLog.WITCH_TARGET + ".»");
            pause(readLine);
            return;
        }

        if (log.isCompleted(QuestId.SWAMP_WHISPER)) {
            printLines(
                    "«Ведьмы замолкли. Осталась печать — принеси вещь с сильной аурой.",
                    "Эпическая или крепче, из сумки, не надетую.»");
            System.out.println();
            System.out.println("  1. Принять поручение");
            System.out.println("  2. Не сейчас");
            ScreenFrame.prompt();
            if ("1".equals(readLine.get().trim())) {
                log.acceptMistSeal();
                System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                        "  Поручение принято: " + QuestId.MIST_SEAL.getTitle()));
                onSave.run();
            }
            pause(readLine);
            return;
        }

        printLines(
                "«Гаррик прислал? Значит, кости собраны.»",
                "«Болото повторяет то, что Храм стёр у людей — Завесу.",
                "Ведьмы — эхо Смирения Памяти. Шесть в забегах.»",
                "«Спросишь потом, что это значит. Согласен?»");
        System.out.println();
        System.out.println("  1. Принять поручение");
        System.out.println("  2. Не сейчас");
        ScreenFrame.prompt();
        if ("1".equals(readLine.get().trim())) {
            log.acceptSwampWhisper();
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                    "  Поручение принято: " + QuestId.SWAMP_WHISPER.getTitle()));
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
