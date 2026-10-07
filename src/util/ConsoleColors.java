package util;

import item.Rarity;

public final class ConsoleColors {
    private static final String C_RESET = "\u001B[0m";
    private static final String C_BOLD = "\u001B[1m";
    private static final String C_WHITE = "\u001B[37m";
    private static final String C_GRAY = "\u001B[90m";
    private static final String C_RED = "\u001B[31m";
    private static final String C_GREEN = "\u001B[32m";
    private static final String C_YELLOW = "\u001B[33m";
    private static final String C_BLUE = "\u001B[34m";
    private static final String C_MAGENTA = "\u001B[35m";
    private static final String C_CYAN = "\u001B[36m";
    private static final String C_ORANGE = "\u001B[33m";
    private static final String C_BRIGHT_RED = "\u001B[91m";
    private static final String C_BRIGHT_GREEN = "\u001B[92m";
    private static final String C_BRIGHT_YELLOW = "\u001B[93m";
    private static final String C_BRIGHT_BLUE = "\u001B[94m";
    private static final String C_BRIGHT_MAGENTA = "\u001B[95m";
    private static final String C_BRIGHT_CYAN = "\u001B[96m";

    public static String RESET = "";
    public static String BOLD = "";
    public static String WHITE = "";
    public static String GRAY = "";
    public static String RED = "";
    public static String GREEN = "";
    public static String YELLOW = "";
    public static String BLUE = "";
    public static String MAGENTA = "";
    public static String CYAN = "";
    public static String ORANGE = "";
    public static String BRIGHT_RED = "";
    public static String BRIGHT_GREEN = "";
    public static String BRIGHT_YELLOW = "";
    public static String BRIGHT_BLUE = "";
    public static String BRIGHT_MAGENTA = "";
    public static String BRIGHT_CYAN = "";

    private ConsoleColors() {
    }

    public static void applyActive(boolean active) {
        RESET = active ? C_RESET : "";
        BOLD = active ? C_BOLD : "";
        WHITE = active ? C_WHITE : "";
        GRAY = active ? C_GRAY : "";
        RED = active ? C_RED : "";
        GREEN = active ? C_GREEN : "";
        YELLOW = active ? C_YELLOW : "";
        BLUE = active ? C_BLUE : "";
        MAGENTA = active ? C_MAGENTA : "";
        CYAN = active ? C_CYAN : "";
        ORANGE = active ? C_ORANGE : "";
        BRIGHT_RED = active ? C_BRIGHT_RED : "";
        BRIGHT_GREEN = active ? C_BRIGHT_GREEN : "";
        BRIGHT_YELLOW = active ? C_BRIGHT_YELLOW : "";
        BRIGHT_BLUE = active ? C_BRIGHT_BLUE : "";
        BRIGHT_MAGENTA = active ? C_BRIGHT_MAGENTA : "";
        BRIGHT_CYAN = active ? C_BRIGHT_CYAN : "";
    }

    public static boolean colorsActive() {
        return ColorSetup.isActive();
    }

    public static String wrap(String color, String text) {
        if (!ColorSetup.isActive() || color.isEmpty()) {
            return text;
        }
        return color + text + RESET;
    }

    public static String dim(String text) {
        return wrap(GRAY, text);
    }

    public static String bold(String text) {
        if (!ColorSetup.isActive()) {
            return text;
        }
        return BOLD + text + RESET;
    }

    public static void println(String text) {
        System.out.println(text);
    }

    public static void printDropBanner(Rarity rarity, String message) {
        String line = "==================================================";
        if (!ColorSetup.isActive()) {
            System.out.println();
            System.out.println("*** " + message + " ***");
            System.out.println(line);
            System.out.println();
            return;
        }
        String color = rarity.getAnsiColor();
        System.out.println();
        System.out.println(color + line + RESET);
        System.out.println(color + BOLD + "  *** " + message + " ***" + RESET);
        System.out.println(color + line + RESET);
        System.out.println();
    }
}
