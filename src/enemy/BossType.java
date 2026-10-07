package enemy;

import util.ConsoleColors;

public enum BossType {
    FLOOR_10("Король Гоблинов", 300, 25, 8, 5, 3,
            ConsoleColors.BRIGHT_GREEN, ConsoleColors.BRIGHT_YELLOW,
            "      .-----------.",
            "     /   \\   /   \\",
            "    |  @     @     |",
            "    |      ^^      |",
            "    |    /||||\\    |",
            "     \\   -----   /",
            "      '-----------'",
            "         |     |",
            "        /|     |\\"),

    FLOOR_20("Некромант", 700, 45, 15, 10, 5,
            ConsoleColors.BRIGHT_MAGENTA, ConsoleColors.BRIGHT_CYAN,
            "         ***",
            "        /|||\\",
            "       | @ @ |",
            "       |  ^  |",
            "      /|     |\\",
            "     / |  |  | \\",
            "    |  |  |  |  |",
            "   /|__|  |  |__|\\",
            "  *         *"),

    FLOOR_30("Дракон", 1200, 55, 20, 8, 5,
            ConsoleColors.BRIGHT_RED, ConsoleColors.BRIGHT_YELLOW,
            "          /\\",
            "         /  \\",
            "        / @  @\\",
            "       /   ^^   \\",
            "      /  /----\\  \\",
            "     /__/      \\__\\",
            "        |    |",
            "       /|    |\\",
            "      / |    | \\"),

    FLOOR_40("Лич", 1600, 65, 25, 12, 8,
            ConsoleColors.WHITE, ConsoleColors.BRIGHT_CYAN,
            "       .--------.",
            "      /  @   @  \\",
            "     |    ^^^    |",
            "     |   /|||\\   |",
            "     |  |     |  |",
            "      \\ |     | /",
            "       \\|     |/",
            "        |     |",
            "       _|     |_"),

    FLOOR_50("Повелитель Демонов", 1950, 68, 25, 15, 10,
            ConsoleColors.BRIGHT_RED, ConsoleColors.ORANGE,
            "        /\\_/\\",
            "       / @   @\\",
            "      |   ^^^   |",
            "      |  /|||\\  |",
            "       \\  | |  /",
            "        \\ | /",
            "         \\|/",
            "        / | \\",
            "       /  |  \\"),

    FLOOR_60("Гидра", 2800, 85, 30, 10, 8,
            ConsoleColors.GREEN, ConsoleColors.BRIGHT_GREEN,
            "   @       @       @",
            "  /|\\     /|\\     /|\\",
            "   |       |       |",
            "    \\     o     /",
            "     \\    ^    /",
            "      \\  /|\\  /",
            "       \\/ | \\/",
            "        | | |",
            "       /| | |\\"),

    FLOOR_70("Титан", 3500, 95, 35, 5, 5,
            ConsoleColors.YELLOW, ConsoleColors.GRAY,
            "    .-----------------.",
            "   |    [=======]     |",
            "   |    | @ @ |       |",
            "   |    |  ^  |       |",
            "   |____|     |_______|",
            "        |     |",
            "       /|     |\\",
            "      / |     | \\",
            "     /__|     |__\\"),

    FLOOR_80("Вампир-Лорд", 4200, 105, 30, 18, 15,
            ConsoleColors.BRIGHT_RED, ConsoleColors.BRIGHT_MAGENTA,
            "         /\\",
            "        /  \\",
            "       | @  @|",
            "       |  ^^ |",
            "        \\ ff /",
            "         \\//",
            "        /|  |\\",
            "       / |  | \\",
            "      *  |  |  *"),

    FLOOR_90("Хранитель Бездны", 5000, 115, 38, 12, 12,
            ConsoleColors.BRIGHT_MAGENTA, ConsoleColors.BRIGHT_BLUE,
            "       .-'*'-.",
            "      /  * *  \\",
            "     | * @ @ * |",
            "     |    ^    |",
            "     |  *   *  |",
            "      \\  ***  /",
            "       '-._.-'",
            "      /       \\",
            "     *         *"),

    FLOOR_100("Повелитель Тьмы", 7000, 140, 45, 20, 10,
            ConsoleColors.BRIGHT_RED, ConsoleColors.BRIGHT_YELLOW,
            "      .-----------.",
            "     /   @   @   \\",
            "    |      ^^      |",
            "    |     /|||\\    |",
            "    |    /  |  \\   |",
            "     \\  |   |   /",
            "      \\ |   |  /",
            "       \\|   | /",
            "        |___|");

    private final String displayName;
    private final int baseHp;
    private final int baseAttack;
    private final int baseDefense;
    private final double critChance;
    private final double dodgeChance;
    private final String primaryColor;
    private final String accentColor;
    private final String[] art;

    BossType(String displayName, int baseHp, int baseAttack, int baseDefense,
             double critChance, double dodgeChance,
             String primaryColor, String accentColor, String... art) {
        this.displayName = displayName;
        this.baseHp = baseHp;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.critChance = critChance;
        this.dodgeChance = dodgeChance;
        this.primaryColor = primaryColor;
        this.accentColor = accentColor;
        this.art = art;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getBaseHp() {
        return baseHp;
    }

    public int getBaseAttack() {
        return baseAttack;
    }

    public int getBaseDefense() {
        return baseDefense;
    }

    public double getCritChance() {
        return critChance;
    }

    public double getDodgeChance() {
        return dodgeChance;
    }

    public String[] getArt() {
        return art;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public static BossType forFloor(int floor) {
        int index = (floor / 10) - 1;
        if (index < 0) {
            return FLOOR_10;
        }
        if (index >= values().length) {
            return FLOOR_100;
        }
        return values()[index];
    }
}
