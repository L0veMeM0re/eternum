package game;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Глобальные настройки игры (отдельно от savegame.json). */
public final class GameSettings {
    private static final Path SETTINGS_PATH = Path.of("settings.json");

    private boolean prologueSeen;

    public static GameSettings load() {
        GameSettings settings = new GameSettings();
        if (!Files.exists(SETTINGS_PATH)) {
            return settings;
        }
        try {
            String json = Files.readString(SETTINGS_PATH);
            settings.prologueSeen = json.contains("\"prologueSeen\": true");
        } catch (IOException e) {
            System.out.println("Не удалось прочитать settings.json: " + e.getMessage());
        }
        return settings;
    }

    public void save() {
        String json = "{\n  \"prologueSeen\": " + prologueSeen + "\n}\n";
        try {
            Files.writeString(SETTINGS_PATH, json);
        } catch (IOException e) {
            System.out.println("Не удалось сохранить settings.json: " + e.getMessage());
        }
    }

    public boolean isPrologueSeen() {
        return prologueSeen;
    }

    public void setPrologueSeen(boolean prologueSeen) {
        this.prologueSeen = prologueSeen;
    }

    /** Только для тестов. */
    public static void resetForTest() {
        try {
            Files.deleteIfExists(SETTINGS_PATH);
        } catch (IOException ignored) {
            // ignore
        }
    }

    public static boolean readPrologueSeenFromDisk() {
        if (!Files.exists(SETTINGS_PATH)) {
            return false;
        }
        try {
            String json = Files.readString(SETTINGS_PATH);
            Matcher m = Pattern.compile("\"prologueSeen\"\\s*:\\s*(true|false)").matcher(json);
            return m.find() && "true".equals(m.group(1));
        } catch (IOException e) {
            return false;
        }
    }
}
