package util;

import item.Gem;
import item.GemTier;
import item.GemType;

/** Цвета и иконки самоцветов в консоли. */
public final class GemRenderer {
    private GemRenderer() {
    }

    public static String icon(GemType type) {
        return switch (type) {
            case RUBY -> "◆";
            case SAPPHIRE -> "◆";
            case EMERALD -> "◆";
            case OBSIDIAN -> "◆";
            case AMBER -> "◆";
            case SHARD -> "✦";
        };
    }

    public static String color(GemType type) {
        return switch (type) {
            case RUBY -> ConsoleColors.BRIGHT_RED;
            case SAPPHIRE -> ConsoleColors.BRIGHT_BLUE;
            case EMERALD -> ConsoleColors.BRIGHT_GREEN;
            case OBSIDIAN -> ConsoleColors.WHITE;
            case AMBER -> ConsoleColors.BRIGHT_YELLOW;
            case SHARD -> ConsoleColors.BRIGHT_MAGENTA;
        };
    }

    public static String tierColor(GemTier tier) {
        return switch (tier) {
            case I -> ConsoleColors.WHITE;
            case II -> ConsoleColors.YELLOW;
            case III -> ConsoleColors.BRIGHT_YELLOW;
        };
    }

    /** Имя типа: «Ruby», «Осколок» — цветом камня. */
    public static String formatTypeName(GemType type) {
        return ConsoleColors.wrap(color(type), type.getDisplayName());
    }

    /** Имя + tier: «Ruby II». */
    public static String formatName(Gem gem) {
        String name = ConsoleColors.wrap(color(gem.getType()), gem.getType().getDisplayName());
        String tier = ConsoleColors.wrap(tierColor(gem.getTier()), gem.getTier().roman());
        return name + " " + ConsoleColors.bold(tier);
    }

    /** Иконка + имя + tier. */
    public static String formatBadge(Gem gem) {
        return coloredIcon(gem.getType()) + " " + formatName(gem);
    }

    /** Иконка в цвете камня. */
    public static String coloredIcon(GemType type) {
        return ConsoleColors.wrap(color(type), icon(type));
    }

    /** Полное описание с цветами (как describe(), но ярче). */
    public static String formatDescribe(Gem gem) {
        StringBuilder sb = new StringBuilder();
        sb.append(formatName(gem));
        if (gem.getStatMultiplier() > 1.001) {
            sb.append(ConsoleColors.dim(" (+"))
                    .append(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                            String.valueOf((int) Math.round((gem.getStatMultiplier() - 1) * 100))))
                    .append(ConsoleColors.dim("%)"));
        }
        sb.append(ConsoleColors.dim(": "));
        sb.append(ConsoleColors.wrap(color(gem.getType()), statText(gem)));
        return sb.toString();
    }

    /** Короткая строка для гнезда: [◆ Ruby II]. */
    public static String formatSocket(Gem gem) {
        return ConsoleColors.dim("[")
                + coloredIcon(gem.getType()) + " "
                + formatName(gem)
                + ConsoleColors.dim("]");
    }

    private static String statText(Gem gem) {
        return switch (gem.getType()) {
            case RUBY -> "+" + gem.atkBonus() + " ATK";
            case SAPPHIRE -> "+" + (int) gem.critBonus() + "% CRIT";
            case EMERALD -> "+" + gem.hpBonus() + " HP";
            case OBSIDIAN -> "+" + String.format("%.1f", gem.critDamageBonus()) + " CRIT DMG";
            case AMBER -> "+" + (int) gem.vampChance() + "% VAMP";
            case SHARD -> gem.procDescription();
        };
    }
}
