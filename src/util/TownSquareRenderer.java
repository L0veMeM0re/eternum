package util;

public final class TownSquareRenderer {
    private static final int INNER = 46;

    private TownSquareRenderer() {
    }

    public static void print() {
        printCompact();
    }

    /** Компактная сцена под баннер ScreenFrame (без дублирования заголовка). */
    public static void printCompact() {
        System.out.println();
        titleRow("◇  Площадь Серебряного Брода  ◇");
        emptyRow();

        textRow("┌─────────┐ ┌─────────┐ ┌─────────┐", ConsoleColors.YELLOW);
        textRow("│ 1·2·3 ⚔ │ │    4    │ │    5    │", ConsoleColors.WHITE);
        textRow("│ Магазин │ │  Наём   │ │  Магия  │", ConsoleColors.BRIGHT_CYAN);
        textRow("└─────────┘ └─────────┘ └─────────┘", ConsoleColors.YELLOW);

        emptyRow();

        textRow("┌─────────┐ ┌─────────┐ ┌─────────┐", ConsoleColors.YELLOW);
        textRow("│    6    │ │    7    │ │    8    │", ConsoleColors.WHITE);
        textRow("│ Игорный │ │Стриптиз │ │ Кузница │", ConsoleColors.BRIGHT_MAGENTA);
        textRow("└─────────┘ └─────────┘ └─────────┘", ConsoleColors.YELLOW);

        emptyRow();
        printFountain();
        emptyRow();

        textRow("┌─────────┐   ~ ⛲ ~   ┌─────────┐", ConsoleColors.YELLOW);
        textRow("│    9    │  · · · ·  │   🛡    │", ConsoleColors.WHITE);
        textRow("│  Шахта │   камни   │  Страж  │", ConsoleColors.BRIGHT_CYAN);
        textRow("└─────────┘           └─────────┘", ConsoleColors.YELLOW);

        printTower();
        textRow("дым еле теплится · у подножия — башня", ConsoleColors.GRAY);
        System.out.println();
    }

    private static void printFountain() {
        textRow("· · · · · · · · · · · · · · · · · · ·", ConsoleColors.BRIGHT_BLUE);
        textRow("  ·   ╭───────────────╮   ·", ConsoleColors.BRIGHT_BLUE);
        textRow("    · │  ~   ⛲   ~  │ ·", ConsoleColors.BRIGHT_CYAN);
        textRow("  ·   ╰───────────────╯   ·", ConsoleColors.BRIGHT_BLUE);
        textRow("· · · · · · · · · · · · · · · · · · ·", ConsoleColors.BRIGHT_BLUE);
    }

    private static void printTower() {
        emptyRow();
        textRow("▲", ConsoleColors.GRAY);
        textRow("███", ConsoleColors.GRAY);
        textRow("▓▓▓▓▓", ConsoleColors.GRAY);
        textRow("███████", ConsoleColors.GRAY);
    }

    private static void border(char left, char right, String color) {
        System.out.println(ConsoleColors.wrap(color, "      " + left + "═".repeat(INNER) + right));
    }

    private static void emptyRow() {
        plainRow(" ".repeat(INNER));
    }

    private static void titleRow(String text) {
        plainRow(ConsoleColors.bold(ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW, centerPlain(text))));
    }

    private static void textRow(String text, String color) {
        plainRow(ConsoleColors.wrap(color, centerPlain(text)));
    }

    private static void plainRow(String inner) {
        String side = ConsoleColors.wrap(ConsoleColors.CYAN, "║");
        System.out.println("      " + side + inner + side);
    }

    private static String centerPlain(String text) {
        if (text.length() >= INNER) {
            return text.substring(0, INNER);
        }
        int pad = INNER - text.length();
        int left = pad / 2;
        return " ".repeat(left) + text + " ".repeat(pad - left);
    }
}
