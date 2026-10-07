package item;

import util.ConsoleColors;

public enum Rarity {
    COMMON("Common", "Белый", 1.0),
    RARE("Rare", "Синий", 1.5),
    EPIC("Epic", "Фиолетовый", 2.0),
    LEGENDARY("Legendary", "Оранжевый", 3.6),
    MYTHIC("Mythic", "Мифический", 5.5);

    private final String englishName;
    private final String colorName;
    private final double statMultiplier;

    Rarity(String englishName, String colorName, double statMultiplier) {
        this.englishName = englishName;
        this.colorName = colorName;
        this.statMultiplier = statMultiplier;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getColorName() {
        return colorName;
    }

    public double getStatMultiplier() {
        return statMultiplier;
    }

    public String getAnsiColor() {
        return switch (this) {
            case COMMON -> ConsoleColors.WHITE;
            case RARE -> ConsoleColors.BRIGHT_BLUE;
            case EPIC -> ConsoleColors.BRIGHT_MAGENTA;
            case LEGENDARY -> ConsoleColors.ORANGE;
            case MYTHIC -> ConsoleColors.BRIGHT_RED;
        };
    }

    public String formatName(String itemName) {
        return ConsoleColors.wrap(getAnsiColor(), "[" + colorName + "] " + itemName);
    }

    public boolean isHighTier() {
        return this == LEGENDARY || this == MYTHIC;
    }

    public boolean isAtLeast(Rarity minimum) {
        return ordinal() >= minimum.ordinal();
    }
}
