package util;

import java.util.List;

/** ASCII-сцена стриптиз-клуба (7 стадий из файлов stage1.txt … stage7.txt). */
public final class StripClubRenderer {
    public static final int STAGES = 7;
    public static final String DANCER_NAME = "Нина";

    private static final int[] STAGE_COSTS = {1000, 2000, 4000, 8000, 16000, 32000, 64000};

    private StripClubRenderer() {
    }

    public static int costForStage(int stageIndex) {
        if (stageIndex < 0 || stageIndex >= STAGES) {
            return 0;
        }
        return STAGE_COSTS[stageIndex];
    }

    public static String stageTitle(int stageIndex) {
        return "Стадия " + (stageIndex + 1) + "/" + STAGES;
    }

    public static void printClubBanner() {
        System.out.println();
        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_MAGENTA,
                "  ╔══════════════════════════════════════════════════════════╗"));
        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_MAGENTA,
                "  ║        ♥  С Т Р И П Т И З  -  К Л У Б  ♥                 ║"));
        System.out.println(ConsoleColors.wrap(ConsoleColors.MAGENTA,
                "  ║              неон · сцена · " + DANCER_NAME + "                        ║"));
        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_MAGENTA,
                "  ╚══════════════════════════════════════════════════════════╝"));
        System.out.println();
    }

    /**
     * @param tipsPaid сколько чаевых уже дано (0 = показать stage1.txt)
     */
    public static void printDancer(int tipsPaid) {
        int fileNum = Math.min(Math.max(1, tipsPaid + 1), STAGES);
        List<String> art = StripClubArtLoader.loadStage(fileNum);
        System.out.println();
        for (String line : art) {
            printArtLine(line);
        }
        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW,
                "  " + DANCER_NAME + " — " + stageTitle(fileNum - 1)));
        System.out.println();
    }

    private static void printArtLine(String line) {
        ConsoleEncoding.printArtLine(line);
    }
}
