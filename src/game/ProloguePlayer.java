package game;

import game.prologue.PrologueArt;
import util.ConsoleColors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Воспроизведение вступительной кастсцены (8 сцен, сц.05 — четыре части). */
public final class ProloguePlayer {
    private static final int WRAP_WIDTH = 70;

    private ProloguePlayer() {
    }

    public static void play(Supplier<String> readLine) {
        playScene(readLine, PrologueArt.ART_01_OFFICE, PrologueArt.TEXT_01);
        playScene(readLine, PrologueArt.ART_02_RAIN, PrologueArt.TEXT_02);
        playScene(readLine, PrologueArt.ART_03_TRUCK, PrologueArt.TEXT_03);
        playScene(readLine, PrologueArt.ART_04_VOID, PrologueArt.TEXT_04);
        playScene05(readLine);
        playScene06(readLine);
        playScene(readLine, PrologueArt.ART_07_WELL, PrologueArt.TEXT_07);
        playFinalScene(readLine);
    }

    /** Сцена 05: пробуждение — четыре остановки с Enter. */
    private static void playScene05(Supplier<String> readLine) {
        playBeat(readLine, PrologueArt.ART_05_WAKE, PrologueArt.TEXT_05_A);
        playBeat(readLine, PrologueArt.ART_05_MIRROR, PrologueArt.TEXT_05_B);
        playBeat(readLine, PrologueArt.ART_05_UI, PrologueArt.TEXT_05_C);
        playBeat(readLine, PrologueArt.ART_05_REBIRTH, PrologueArt.TEXT_05_D);
    }

    /** Сцена 06: перебивка + Лира. */
    private static void playScene06(Supplier<String> readLine) {
        clearScreen();
        printArt(PrologueArt.ART_06_TITLE_CARD);
        System.out.println();
        printArt(PrologueArt.ART_06_LIRA);
        System.out.println();
        printBody(PrologueArt.TEXT_06);
        pause(readLine);
    }

    private static void playFinalScene(Supplier<String> readLine) {
        clearScreen();
        printArt(PrologueArt.ART_08_GREY_CLOAKS);
        System.out.println();
        printBody(PrologueArt.TEXT_08);
        System.out.println();
        printBody(PrologueArt.TEXT_08_AFTER);
        System.out.println();
        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_CYAN, "  Enter — начать игру"));
        readLine.get();
    }

    private static void playScene(Supplier<String> readLine, String[] art, String text) {
        playBeat(readLine, art, text);
    }

    private static void playBeat(Supplier<String> readLine, String[] art, String text) {
        clearScreen();
        printArt(art);
        System.out.println();
        printBody(text);
        pause(readLine);
    }

    private static void pause(Supplier<String> readLine) {
        System.out.println();
        System.out.println(ConsoleColors.dim("  Enter…"));
        readLine.get();
    }

    private static void clearScreen() {
        System.out.print("\u001B[2J\u001B[H");
        System.out.flush();
    }

    private static void printArt(String[] lines) {
        for (String line : lines) {
            System.out.println("  " + ConsoleColors.wrap(ConsoleColors.GRAY, line));
        }
    }

    private static void printBody(String text) {
        for (String paragraph : text.split("\n")) {
            if (paragraph.isBlank()) {
                System.out.println();
                continue;
            }
            for (String line : wrapParagraph(paragraph)) {
                System.out.println("  " + formatLine(line));
            }
            System.out.println();
        }
    }

    private static String formatLine(String line) {
        if (line.startsWith(">>>")) {
            return ConsoleColors.wrap(ConsoleColors.BRIGHT_CYAN, line);
        }
        if (line.contains("**")) {
            return applyBoldMarkers(line);
        }
        if (line.startsWith("«") || line.contains(" — «")) {
            return ConsoleColors.wrap(ConsoleColors.YELLOW, line);
        }
        return line;
    }

    private static String applyBoldMarkers(String line) {
        StringBuilder out = new StringBuilder();
        int i = 0;
        boolean bold = false;
        while (i < line.length()) {
            if (i + 1 < line.length() && line.charAt(i) == '*' && line.charAt(i + 1) == '*') {
                if (bold) {
                    out.append(ConsoleColors.RESET);
                } else {
                    out.append(ConsoleColors.BOLD);
                }
                bold = !bold;
                i += 2;
                continue;
            }
            out.append(line.charAt(i));
            i++;
        }
        if (bold) {
            out.append(ConsoleColors.RESET);
        }
        return out.toString();
    }

    private static List<String> wrapParagraph(String paragraph) {
        List<String> lines = new ArrayList<>();
        String[] words = paragraph.split("\\s+");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            if (current.isEmpty()) {
                current.append(word);
            } else if (current.length() + 1 + word.length() <= WRAP_WIDTH) {
                current.append(' ').append(word);
            } else {
                lines.add(current.toString());
                current = new StringBuilder(word);
            }
        }
        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        return lines;
    }
}
