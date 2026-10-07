package player;

import util.ConsoleColors;

public enum CharacterClass {
    WARRIOR("Воин", "Высокое HP, защита, средний урон",
            158, 16, 9, 5.0, 2.0, 5.0,
            ConsoleColors.BRIGHT_RED, ConsoleColors.BRIGHT_YELLOW,
            "    /=====\\",
            "   |[@_@]|",
            "   |  |  |",
            "  /|  |  |\\",
            " / |__|__| \\",
            "    |  |",
            "   /|  |\\",
            "  / |  | \\"),

    ROGUE("Разбойник", "Крит и уворот, быстрые убийства",
            90, 20, 3, 25.0, 2.5, 20.0,
            ConsoleColors.WHITE, ConsoleColors.BRIGHT_GREEN,
            "     ___",
            "    /   \\",
            "   | > < |",
            "    \\|@|/",
            "     / \\",
            "    /   \\",
            "   |     |",
            "   |__ __|"),

    MAGE("Маг", "Огромный урон и крит-урон, низкая живучесть",
            80, 30, 1, 15.0, 3.0, 8.0,
            ConsoleColors.BRIGHT_BLUE, ConsoleColors.BRIGHT_CYAN,
            "    / * \\",
            "   | @ @ |",
            "   |  ^  |",
            "   | /|\\ |",
            "    \\|@|/",
            "     | |",
            "    /   \\",
            "   *     *"),

    CLERIC("Клирик", "Поддержка отряда, устойчивость и исцеление",
            120, 12, 6, 8.0, 2.2, 8.0,
            ConsoleColors.WHITE, ConsoleColors.BRIGHT_YELLOW,
            "    /+++\\",
            "   | @ @ |",
            "   |  +  |",
            "   | /|\\ |",
            "    \\|+|/",
            "     | |",
            "    /   \\",
            "   +     +"),

    BERSERKER("Берсерк", "Растущий урон при низком HP, риск ради силы",
            125, 20, 4, 10.0, 2.4, 5.0,
            ConsoleColors.BRIGHT_RED, ConsoleColors.BRIGHT_MAGENTA,
            "   /\\  /\\",
            "  | @||@ |",
            "  |  \\/  |",
            "   | XX |",
            "  /|    |\\",
            " / |    | \\",
            "   |  |",
            "  /|  |\\");

    private final String displayName;
    private final String description;
    private final int baseHp;
    private final int baseAttack;
    private final int baseDefense;
    private final double baseCritChance;
    private final double baseCritDamage;
    private final double baseDodgeChance;
    private final String portraitPrimary;
    private final String portraitAccent;
    private final String[] portrait;

    CharacterClass(String displayName, String description,
                   int baseHp, int baseAttack, int baseDefense,
                   double baseCritChance, double baseCritDamage, double baseDodgeChance,
                   String portraitPrimary, String portraitAccent, String... portrait) {
        this.displayName = displayName;
        this.description = description;
        this.baseHp = baseHp;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.baseCritChance = baseCritChance;
        this.baseCritDamage = baseCritDamage;
        this.baseDodgeChance = baseDodgeChance;
        this.portraitPrimary = portraitPrimary;
        this.portraitAccent = portraitAccent;
        this.portrait = portrait;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getPortraitPrimary() {
        return portraitPrimary;
    }

    public String getPortraitAccent() {
        return portraitAccent;
    }

    public String[] getPortrait() {
        return portrait;
    }

    public Stats createBaseStats() {
        return new Stats(baseHp, baseAttack, baseDefense, baseCritChance, baseCritDamage, baseDodgeChance);
    }

    public ClassSkillKit getSkillKit() {
        return ClassSkillKit.forClass(this);
    }
}
