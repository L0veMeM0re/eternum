package util;

import enemy.Enemy;

import java.util.List;

/**
 * Печать ASCII-арта мобов с подсветкой глаз/деталей.
 */
public final class MobArt {
    private static final String DEFAULT_HIGHLIGHT = "oO0*@^●◉░▒▓█▲▼";

    private MobArt() {
    }

    public static void print(String[] lines, String primaryColor, String accentColor) {
        print(lines, primaryColor, accentColor, DEFAULT_HIGHLIGHT);
    }

    public static void print(String[] lines, String primaryColor, String accentColor, String highlightChars) {
        if (lines == null || lines.length == 0) {
            return;
        }
        for (String line : lines) {
            System.out.println(colorize(line, primaryColor, accentColor, highlightChars));
        }
    }

    public static String colorize(String line, String primaryColor, String accentColor, String highlightChars) {
        if (line == null) {
            return "";
        }
        if (!ColorSetup.isActive()) {
            return line;
        }
        StringBuilder sb = new StringBuilder(primaryColor);
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (highlightChars.indexOf(c) >= 0) {
                sb.append(ConsoleColors.RESET).append(accentColor).append(c);
                if (i + 1 < line.length()) {
                    sb.append(ConsoleColors.RESET).append(primaryColor);
                }
            } else {
                sb.append(c);
            }
        }
        sb.append(ConsoleColors.RESET);
        return sb.toString();
    }

    /** Портреты нескольких врагов в один ряд (для отряда). */
    public static void printSquadInRow(List<Enemy> enemies) {
        if (enemies == null || enemies.isEmpty()) {
            return;
        }
        int cols = enemies.size();
        int gap = cols > 2 ? 2 : 3;
        String[][] lines = new String[cols][];
        int maxRows = 0;
        int[] colWidth = new int[cols];

        for (int c = 0; c < cols; c++) {
            Enemy enemy = enemies.get(c);
            String[] raw = enemy.getPortraitLines();
            if (raw == null || raw.length == 0) {
                lines[c] = new String[0];
                continue;
            }
            String primary = enemy.getPortraitPrimaryColor();
            String accent = enemy.getPortraitAccentColor();
            String highlights = enemy.getPortraitHighlightChars();
            String[] colored = new String[raw.length];
            for (int r = 0; r < raw.length; r++) {
                colored[r] = colorize(raw[r], primary, accent, highlights);
                colWidth[c] = Math.max(colWidth[c], visibleLength(colored[r]));
            }
            lines[c] = colored;
            maxRows = Math.max(maxRows, colored.length);
        }

        for (int c = 0; c < cols; c++) {
            colWidth[c] = Math.max(colWidth[c], visibleLength(enemies.get(c).getName()));
        }

        for (int r = 0; r < maxRows; r++) {
            StringBuilder row = new StringBuilder("  ");
            for (int c = 0; c < cols; c++) {
                String part = (lines[c] != null && r < lines[c].length) ? lines[c][r] : "";
                row.append(padRight(part, colWidth[c]));
                if (c < cols - 1) {
                    row.append(" ".repeat(gap));
                }
            }
            System.out.println(row);
        }

        StringBuilder names = new StringBuilder("  ");
        for (int c = 0; c < cols; c++) {
            names.append(padRight(enemies.get(c).getName(), colWidth[c]));
            if (c < cols - 1) {
                names.append(" ".repeat(gap));
            }
        }
        System.out.println(names);
    }

    private static int visibleLength(String s) {
        if (s == null) {
            return 0;
        }
        int len = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\u001B') {
                while (i < s.length() && s.charAt(i) != 'm') {
                    i++;
                }
            } else {
                len++;
            }
        }
        return len;
    }

    private static String padRight(String s, int width) {
        if (s == null) {
            s = "";
        }
        int len = visibleLength(s);
        if (len >= width) {
            return s;
        }
        return s + " ".repeat(width - len);
    }
}
