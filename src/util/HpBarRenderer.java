package util;

/** Компактная полоска HP для боевого лога. */
public final class HpBarRenderer {
    private static final int WIDTH = 8;

    public enum Side {
        ALLY(ConsoleColors.BRIGHT_GREEN),
        ENEMY(ConsoleColors.BRIGHT_RED);

        private final String fillColor;

        Side(String fillColor) {
            this.fillColor = fillColor;
        }
    }

    private HpBarRenderer() {
    }

    /** «45/100 [████░░░░]» — цифры и полоска (без префикса «→ HP:»). */
    public static String numbersAndBar(int current, int max, Side side) {
        int safeMax = Math.max(1, max);
        int safeCur = Math.max(0, Math.min(current, safeMax));
        return safeCur + "/" + safeMax + " " + bar(safeCur, safeMax, side);
    }

    /** « → HP: 45/100 [████░░░░] » — полоска справа от цифр. */
    public static String combatHp(int current, int max, Side side) {
        return ConsoleColors.dim(" → HP: ") + numbersAndBar(current, max, side);
    }

    public static String bar(int current, int max, Side side) {
        int safeMax = Math.max(1, max);
        double ratio = Math.max(0, Math.min(1, current / (double) safeMax));
        int filled = (int) Math.round(ratio * WIDTH);
        filled = Math.max(0, Math.min(WIDTH, filled));

        String full = ColorSetup.isActive() ? "█" : "#";
        String empty = ColorSetup.isActive() ? "░" : ".";

        return ConsoleColors.dim("[")
                + ConsoleColors.wrap(side.fillColor, full.repeat(filled))
                + ConsoleColors.dim(empty.repeat(WIDTH - filled) + "]");
    }
}
