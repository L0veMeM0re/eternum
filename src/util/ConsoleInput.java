package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Ввод с клавиатуры: фоновый поток читает stdin, строки уходят в очереди меню или боя.
 */
public final class ConsoleInput {
    private final BlockingQueue<String> menuLines = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> combatLines = new LinkedBlockingQueue<>();
    private final AtomicBoolean combatMode = new AtomicBoolean(false);
    private volatile boolean eof;

    public ConsoleInput() {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8));
        Thread t = new Thread(() -> readLoop(reader), "console-input");
        t.setDaemon(true);
        t.start();
    }

    private void readLoop(BufferedReader reader) {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (combatMode.get()) {
                    combatLines.offer(line);
                } else {
                    menuLines.offer(line);
                }
            }
        } catch (IOException ignored) {
            // ignore
        } finally {
            eof = true;
        }
    }

    public void setCombatMode(boolean enabled) {
        combatMode.set(enabled);
        if (enabled) {
            drainCombat();
        }
    }

    public boolean isCombatMode() {
        return combatMode.get();
    }

    public String readLineForMenu() {
        if (eof) {
            return "";
        }
        try {
            String line = menuLines.take();
            return line == null ? "" : normalize(line);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        }
    }

    /** Сброс пустых строк после выбора в меню. */
    public void discardExtraMenuLines() {
        String line;
        while ((line = menuLines.poll()) != null) {
            if (!line.trim().isEmpty()) {
                // неожиданный ввод — вернуть нельзя
            }
        }
    }

    public String pollLine() {
        return combatLines.poll();
    }

    public String pollLine(long timeoutMs) {
        if (eof) {
            return null;
        }
        try {
            return combatLines.poll(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    public void drainCombat() {
        while (combatLines.poll() != null) {
            // drain
        }
    }

    public void drainMenu() {
        while (menuLines.poll() != null) {
            // drain
        }
    }

    private static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim();
        if (s.startsWith("\ufeff")) {
            s = s.substring(1).trim();
        }
        return s;
    }

    public static boolean isQuitCommand(String raw) {
        if (raw == null) {
            return false;
        }
        String cmd = normalize(raw).toLowerCase(Locale.ROOT);
        return "q".equals(cmd) || "й".equals(cmd) || "quit".equals(cmd) || "exit".equals(cmd)
                || "выход".equals(cmd);
    }

    public boolean isEof() {
        return eof;
    }
}
