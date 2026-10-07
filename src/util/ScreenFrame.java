package util;

/**
 * Единое оформление экранов: хлебные крошки, цветной баннер, блок действий.
 * Помогает игроку понимать, где он находится в меню.
 */
public final class ScreenFrame {
    private static final int WIDTH = 44;
    private static final String TOP_RULE = "═".repeat(52);

    public enum Screen {
        MAIN("◆", "ГЛАВНОЕ МЕНЮ", ConsoleColors.BRIGHT_CYAN),
        RUN("⚔", "НОВЫЙ ЗАБЕГ", ConsoleColors.BRIGHT_RED),
        CHARACTER("♦", "ПЕРСОНАЖ", ConsoleColors.BRIGHT_GREEN),
        INVENTORY("▣", "ИНВЕНТАРЬ", ConsoleColors.BRIGHT_YELLOW),
        EQUIPMENT("▤", "ЭКИПИРОВКА", ConsoleColors.BRIGHT_BLUE),
        SET_CODEX("▥", "СПРАВОЧНИК СЕТОВ", ConsoleColors.CYAN),
        CLASS_CHANGE("✧", "СМЕНА КЛАССА", ConsoleColors.BRIGHT_MAGENTA),
        SKILL_TREE("▲", "ДЕРЕВО УМЕНИЙ", ConsoleColors.BRIGHT_MAGENTA),
        TEAM("◈", "КОМАНДА", ConsoleColors.BRIGHT_GREEN),
        MERCENARY("◉", "НАЁМНИК", ConsoleColors.GREEN),
        TOWN("◆", "ГОРОДСКАЯ ПЛОЩАДЬ", ConsoleColors.BRIGHT_CYAN),
        ITEM_SHOP("▣", "МАГАЗИН ПРЕДМЕТОВ", ConsoleColors.YELLOW),
        GOLD_SHOP("◈", "ТОРГОВЕЦ ЗОЛОТА", ConsoleColors.BRIGHT_YELLOW),
        UPGRADE("◉", "УЛУЧШЕНИЯ", ConsoleColors.MAGENTA),
        MERC_SHOP("◈", "НАЁМНИКИ", ConsoleColors.CYAN),
        MERC_HIRE("◉", "ВЫБОР НАЁМНИКА", ConsoleColors.CYAN),
        MAGIC_SHOP("✦", "МАГИЧЕСКАЯ ЛАВКА", ConsoleColors.BRIGHT_MAGENTA),
        GAMBLING_CLUB("🎲", "ИГОРНЫЙ КЛУБ", ConsoleColors.BRIGHT_YELLOW),
        STRIP_CLUB("♥", "СТРИПТИЗ-КЛУБ", ConsoleColors.BRIGHT_MAGENTA),
        FORGE("🔨", "КУЗНИЦА", ConsoleColors.YELLOW),
        ORE_CAVE("⛏", "ШАХТА РУДЫ", ConsoleColors.BRIGHT_CYAN),
        WORLD("🌍", "МИР", ConsoleColors.GREEN),
        VILLAGE("🏡", "ДЕРЕВНЯ", ConsoleColors.BRIGHT_GREEN),
        SWAMP_OUTPOST("🌫", "ЗАСТАВА БОЛОТА", ConsoleColors.GREEN),
        MINE_CAMP("⛏", "ШАХТЁРСКАЯ ЗАСТАВА", ConsoleColors.BRIGHT_CYAN),
        TRADE_ROAD("🛤", "ТЁМНЫЙ ТРАКТ", ConsoleColors.YELLOW),
        ROVING_MERCHANT("🎒", "БРОДЯЧИЙ ТОРГОВЕЦ", ConsoleColors.BRIGHT_CYAN),
        STATS("▤", "СТАТИСТИКА", ConsoleColors.WHITE),
        CONFIRM("?", "ПОДТВЕРЖДЕНИЕ", ConsoleColors.YELLOW),
        COMBAT("⚔", "БОЙ", ConsoleColors.CYAN),
        COMBAT_BOSS("☠", "БОСС-БОЙ", ConsoleColors.BRIGHT_RED);

        private final String icon;
        private final String title;
        private final String color;

        Screen(String icon, String title, String color) {
            this.icon = icon;
            this.title = title;
            this.color = color;
        }

        public String icon() {
            return icon;
        }

        public String title() {
            return title;
        }

        public String color() {
            return color;
        }
    }

    private ScreenFrame() {
    }

    /** Открывает экран без хлебных крошек. */
    public static void open(Screen screen) {
        open(screen, null);
    }

    /** Открывает экран с подзаголовком (без крошек). */
    public static void open(Screen screen, String subtitle) {
        open(screen, subtitle, new String[0]);
    }

    /** Открывает экран с хлебными крошками. */
    public static void openPath(Screen screen, String... path) {
        open(screen, null, path);
    }

    /** Полный вариант: крошки + подзаголовок. */
    public static void open(Screen screen, String subtitle, String... path) {
        System.out.println();
        System.out.println(ConsoleColors.dim("  " + TOP_RULE));
        printBreadcrumb(path, screen, subtitle);
        printBanner(screen, subtitle);
        System.out.println(ConsoleColors.dim("  " + "─".repeat(WIDTH)));
    }

    /** Заголовок боя в забеге. */
    public static void combat(int floor, String zone, boolean boss, String bossName) {
        System.out.println();
        System.out.println(ConsoleColors.dim("  " + TOP_RULE));
        System.out.println(ConsoleColors.dim("  Забег") + ConsoleColors.dim(" › ")
                + ConsoleColors.wrap(ConsoleColors.CYAN, "Этаж " + floor));
        if (boss) {
            printBanner(Screen.COMBAT_BOSS, bossName);
        } else {
            printBanner(Screen.COMBAT, zone);
        }
        System.out.println(ConsoleColors.dim("  " + "─".repeat(WIDTH)));
    }

    public static void section(String label) {
        System.out.println();
        System.out.println(ConsoleColors.wrap(ConsoleColors.CYAN, "  ▸ " + label));
    }

    public static void subsection(String label) {
        System.out.println(ConsoleColors.bold("  ── " + label + " ──"));
    }

    public static void back() {
        System.out.println(ConsoleColors.dim("  0 — Назад"));
    }

    public static void cancel() {
        System.out.println(ConsoleColors.dim("  0 — Отмена"));
    }

    public static void prompt() {
        System.out.print(ConsoleColors.wrap(ConsoleColors.WHITE, "  ▶ Выбор: "));
    }

    public static void hint(String text) {
        System.out.println(ConsoleColors.dim("  " + text));
    }

    private static void printBreadcrumb(String[] path, Screen screen, String subtitle) {
        StringBuilder sb = new StringBuilder("  ");
        for (int i = 0; i < path.length; i++) {
            if (i > 0) {
                sb.append(ConsoleColors.dim(" › "));
            }
            sb.append(ConsoleColors.dim(path[i]));
        }
        if (path.length > 0) {
            sb.append(ConsoleColors.dim(" › "));
        }
        String current = screen.title();
        if (subtitle != null && !subtitle.isBlank()) {
            current = current + " · " + subtitle;
        }
        sb.append(ConsoleColors.wrap(screen.color(), ConsoleColors.bold(current)));
        System.out.println(sb);
    }

    private static void printBanner(Screen screen, String subtitle) {
        String line = screen.icon() + "  " + screen.title();
        if (subtitle != null && !subtitle.isBlank()) {
            line = line + " — " + subtitle;
        }
        String inner = center(line, WIDTH);
        String borderColor = screen.color();
        String side = ConsoleColors.wrap(borderColor, "║");
        String horiz = ConsoleColors.wrap(borderColor, "═".repeat(WIDTH));
        System.out.println(ConsoleColors.wrap(borderColor, "  ╔" + horiz + "╗"));
        System.out.println("  " + side + ConsoleColors.wrap(borderColor, ConsoleColors.bold(inner)) + side);
        System.out.println(ConsoleColors.wrap(borderColor, "  ╚" + horiz + "╝"));
    }

    private static String center(String text, int width) {
        String plain = stripAnsi(text);
        if (plain.length() >= width) {
            return text.substring(0, Math.min(text.length(), width));
        }
        int pad = width - plain.length();
        int left = pad / 2;
        return " ".repeat(left) + text + " ".repeat(pad - left);
    }

    private static String stripAnsi(String text) {
        return text.replaceAll("\u001B\\[[0-9;]*m", "");
    }
}
