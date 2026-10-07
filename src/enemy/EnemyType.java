package enemy;

import util.ConsoleColors;

public enum EnemyType {
    GOBLIN("Гоблин", 40, 8, 2, 2, 3,
            ConsoleColors.GREEN, ConsoleColors.BRIGHT_YELLOW,
            "       ___",
            "      /   \\",
            "     | o   o|",
            "     |  \\_/ |",
            "    /|  ^^  |\\",
            "   / |  |   | \\",
            "      | | | |",
            "     _| |_| |_"),

    WOLF("Волк", 35, 10, 1, 5, 8,
            ConsoleColors.WHITE, ConsoleColors.RED,
            "        /\\___/\\",
            "       /       \\",
            "      /  @   @   \\",
            "     |      ^      |",
            "      \\  \\___/  /",
            "       \\  \\_/  /",
            "        |     |",
            "       /|     |\\"),

    SKELETON("Скелет", 80, 15, 4, 3, 5,
            ConsoleColors.WHITE, ConsoleColors.BRIGHT_CYAN,
            "       .---.",
            "      / o o \\",
            "     |   ^   |",
            "     |  [|]  |",
            "     |   |   |",
            "     |  / \\  |",
            "     | |   | |",
            "     |_|   |_|"),

    SPIDER("Паук", 70, 18, 2, 8, 12,
            ConsoleColors.BRIGHT_MAGENTA, ConsoleColors.RED,
            "    \\       /",
            "     \\  @  /",
            "      \\| |/",
            "    /\\ |o| /\\",
            "   /  \\|_|/  \\",
            "  / /\\     /\\ \\",
            " | |  \\___/  | |",
            "  \\_/       \\_/"),

    ORC("Орк", 100, 20, 5, 5, 4,
            ConsoleColors.GREEN, ConsoleColors.BRIGHT_RED,
            "      .-------.",
            "     /  o   o  \\",
            "    |    ^^^    |",
            "    |   /|||\\   |",
            "    |  |     |  |",
            "     \\ |     | /",
            "      \\|     |/",
            "       |_____|"),

    BAT("Летучая мышь", 30, 12, 0, 10, 20,
            ConsoleColors.GRAY, ConsoleColors.BRIGHT_MAGENTA,
            "    /\\       /\\",
            "   /  \\     /  \\",
            "  /    \\___/    \\",
            " |      @ @      |",
            "  \\      ^      /",
            "   \\    / \\    /",
            "    \\  /   \\  /",
            "     \\/     \\/"),

    SLIME("Слизь", 60, 9, 6, 1, 2,
            ConsoleColors.BRIGHT_GREEN, ConsoleColors.GREEN,
            "       .---.",
            "      /     \\",
            "     |  @ @  |",
            "     |   ~   |",
            "      \\  ~  /",
            "       '---'",
            "      /     \\",
            "     '-------'"),

    GHOST("Призрак", 55, 14, 1, 5, 15,
            ConsoleColors.BRIGHT_CYAN, ConsoleColors.WHITE,
            "       .~~~.",
            "      /  ~  \\",
            "     |  o o  |",
            "     |   ^   |",
            "      \\  ~  /",
            "       |   |",
            "      /|   |\\",
            "     / |   | \\"),

    GOLEM("Голем", 120, 16, 10, 2, 1,
            ConsoleColors.YELLOW, ConsoleColors.GRAY,
            "    .-----------.",
            "   |  [=====]   |",
            "   |  | @ @ |   |",
            "   |  |  ^  |   |",
            "   |__|     |__|",
            "      |     |",
            "     /|     |\\",
            "    / |     | \\"),

    WITCH("Ведьма", 65, 22, 3, 12, 10,
            ConsoleColors.BRIGHT_MAGENTA, ConsoleColors.BRIGHT_GREEN,
            "        /\\",
            "       /  \\",
            "      / @  \\",
            "     |   ^  |",
            "     |  /|\\ |",
            "      | / \\ |",
            "     /|     |\\",
            "    * |     | *"),

    DEMON("Бес", 75, 19, 4, 10, 8,
            ConsoleColors.BRIGHT_RED, ConsoleColors.YELLOW,
            "       /\\",
            "      /  \\",
            "     / @  @\\",
            "    |   ^   |",
            "    |  /|\\  |",
            "     \\  |  /",
            "      \\ | /",
            "       \\|/"),

    ELEMENTAL("Элементаль", 90, 24, 3, 8, 6,
            ConsoleColors.BRIGHT_CYAN, ConsoleColors.BRIGHT_BLUE,
            "      .~~~~~.",
            "     /  * *  \\",
            "    |  * @ *  |",
            "    |    ^    |",
            "     \\  * *  /",
            "      '~~~~~'",
            "     /       \\",
            "    ~         ~");

    private final String displayName;
    private final int baseHp;
    private final int baseAttack;
    private final int baseDefense;
    private final double critChance;
    private final double dodgeChance;
    private final String primaryColor;
    private final String accentColor;
    private final String[] art;

    EnemyType(String displayName, int baseHp, int baseAttack, int baseDefense,
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
}
