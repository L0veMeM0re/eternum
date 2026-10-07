package game;

import game.prologue.PrologueArt;
import util.ConsoleColors;

import java.util.function.Supplier;

/** Титульный экран при первом запуске. */
public final class TitleScreen {
    public enum Action {
        START,
        EXIT
    }

    private TitleScreen() {
    }

    public static Action show(Supplier<String> readLine) {
        while (true) {
            clearScreen();
            printTitleArt();
            System.out.println();
            printTagline();
            System.out.println();
            System.out.println("  1. Начать игру");
            System.out.println("  2. Выйти");
            System.out.println();
            System.out.print("  > ");
            String choice = readLine.get().trim();
            switch (choice) {
                case "1" -> {
                    return Action.START;
                }
                case "2" -> {
                    return Action.EXIT;
                }
                default -> System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW, "  Неверный выбор."));
            }
        }
    }

    private static void clearScreen() {
        System.out.print("\u001B[2J\u001B[H");
        System.out.flush();
    }

    private static void printTitleArt() {
        String[] art = useCompactTitle() ? PrologueArt.TITLE_COMPACT : PrologueArt.TITLE_BLOCK;
        for (String line : art) {
            System.out.println(ConsoleColors.bold(
                    ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW, "  " + line)));
        }
    }

    private static boolean useCompactTitle() {
        String cols = System.getenv("COLUMNS");
        if (cols != null) {
            try {
                return Integer.parseInt(cols.trim()) < 80;
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return false;
    }

    private static void printTagline() {
        for (String line : PrologueArt.TITLE_TAGLINE.split("\n")) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.GRAY, "  " + line));
        }
    }
}
