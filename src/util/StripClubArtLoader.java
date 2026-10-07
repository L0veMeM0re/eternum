package util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Загрузка ASCII/Unicode-артов стриптиз-клуба из stage1.txt … stage7.txt. */
public final class StripClubArtLoader {
    private static final String[] SEARCH_DIRS = {"src", "."};

    private StripClubArtLoader() {
    }

    public static List<String> loadStage(int stageNumber) {
        if (stageNumber < 1 || stageNumber > 7) {
            return List.of();
        }
        String fileName = "stage" + stageNumber + ".txt";
        for (String dir : SEARCH_DIRS) {
            Path path = Path.of(dir, fileName);
            if (Files.isRegularFile(path)) {
                try {
                    return Files.readAllLines(path, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    System.out.println(ConsoleColors.dim("  (не удалось прочитать " + path + ")"));
                }
            }
        }
        return fallback(stageNumber);
    }

    private static List<String> fallback(int stageNumber) {
        List<String> lines = new ArrayList<>();
        lines.add("  [арт stage" + stageNumber + ".txt не найден — положите файл в папку src/]");
        return lines;
    }
}
