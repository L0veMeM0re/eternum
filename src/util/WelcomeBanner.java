package util;

/**
 * Стартовый баннер игры — ЭТЕРНУМ.
 */
public final class WelcomeBanner {
    private static final String[] TITLE = {
            "  ███████╗████████╗███████╗██████╗ ███╗   ██╗██╗   ██╗███╗   ███╗",
            "  ██╔════╝╚══██╔══╝██╔════╝██╔══██╗████╗  ██║██║   ██║████╗ ████║",
            "  █████╗     ██║   █████╗  ██████╔╝██╔██╗ ██║██║   ██║██╔████╔██║",
            "  ██╔══╝     ██║   ██╔══╝  ██╔══██╗██║╚██╗██║██║   ██║██║╚██╔╝██║",
            "  ███████╗   ██║   ███████╗██║  ██║██║ ╚████║╚██████╔╝██║ ╚═╝ ██║",
            "  ╚══════╝   ╚═╝   ╚══════╝╚═╝  ╚═╝╚═╝  ╚═══╝ ╚═════╝ ╚═╝     ╚═╝ ",
    };

    private WelcomeBanner() {
    }

    public static void print() {
        System.out.println();
        printFrameTop();
        System.out.println();
        printTitle();
        System.out.println();
        printSignature();
        System.out.println();
        printFrameBottom();
        System.out.println();
    }

    private static void printFrameTop() {
        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW,
                "  ✦╭───────────────────────────────────────────────────────────────╮✦"));
        System.out.println(ConsoleColors.wrap(ConsoleColors.CYAN,
                "   │  ～～ ✧ · · · · · · · · · · · · · · · · · · · · · ✧ ～～   │"));
        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW,
                "  ✦╰───────────────────────────────────────────────────────────────╯✦"));
    }

    private static void printFrameBottom() {
        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW,
                "  ✦╭───────────────────────────────────────────────────────────────╮✦"));
        System.out.println(ConsoleColors.wrap(ConsoleColors.CYAN,
                "   │  ～～ ✧ · · · · · · · · · · · · · · · · · · · · · ✧ ～～   │"));
        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW,
                "  ✦╰───────────────────────────────────────────────────────────────╯✦"));
    }

    private static final String[] TITLE_COMPACT = {
            "        ╔═══════════════════════════════╗",
            "        ║  Э  Т  Е  Р  Н  У  М          ║",
            "        ╚═══════════════════════════════╝",
    };

    private static void printTitle() {
        String[] art = isNarrowTerminal() ? TITLE_COMPACT : TITLE;
        for (String line : art) {
            System.out.println(ConsoleColors.bold(
                    ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW, line)));
        }
    }

    private static boolean isNarrowTerminal() {
        String cols = System.getenv("COLUMNS");
        if (cols != null) {
            try {
                return Integer.parseInt(cols.trim()) < 72;
            } catch (NumberFormatException ignored) {
                // default: full title
            }
        }
        return false;
    }

    private static void printSignature() {
        String bar = "  ─────────── ✦ ───────────";
        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_MAGENTA, bar));
        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW,
                ConsoleColors.bold(center("by LMM", 63))));
        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_MAGENTA, bar));
    }

    private static String center(String text, int width) {
        int pad = Math.max(0, (width - text.length()) / 2);
        return " ".repeat(pad) + text;
    }
}
