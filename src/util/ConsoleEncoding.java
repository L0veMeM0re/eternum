package util;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/** Настройка UTF-8 для Windows cmd и прямой вывод Unicode-арта. */
public final class ConsoleEncoding {
    private static boolean utf8Mode;
    private static boolean windowsTerminal;
    private static boolean useAsciiArtFallback;

    private ConsoleEncoding() {
    }

    public static void setup() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        windowsTerminal = System.getenv("WT_SESSION") != null
                || System.getenv("TERM") != null;
        if (os.contains("win")) {
            try {
                new ProcessBuilder("cmd", "/c", "chcp", "65001")
                        .redirectErrorStream(true)
                        .start()
                        .waitFor();
            } catch (Exception ignored) {
                // ignore
            }
        }
        useAsciiArtFallback = os.contains("win") && !windowsTerminal;
        utf8Mode = !useAsciiArtFallback;
    }

    public static boolean useAsciiArtFallback() {
        return useAsciiArtFallback;
    }

    public static boolean isWindowsTerminal() {
        return windowsTerminal;
    }

    /** Печать строки арта: UTF-8 напрямую в консоль или ASCII-fallback. */
    public static void printArtLine(String line) {
        if (line == null || line.isEmpty()) {
            System.out.println();
            return;
        }
        String output = useAsciiArtFallback ? toAsciiArt(line) : line;
        if (useAsciiArtFallback) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_MAGENTA, output));
            return;
        }
        writeRawUtf8(output + System.lineSeparator());
    }

    private static void writeRawUtf8(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        try {
            FileOutputStream out = new FileOutputStream(FileDescriptor.out);
            out.write(bytes);
            out.flush();
        } catch (IOException e) {
            System.out.print(text);
        }
    }

    /** Braille / block Unicode → ASCII (# . пробел) для cmd без UTF-8. */
    static String toAsciiArt(String line) {
        StringBuilder sb = new StringBuilder(line.length());
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\r') {
                continue;
            }
            if (c <= 127 && c != '\uFFFD') {
                sb.append(c);
                continue;
            }
            if (c >= '\u2800' && c <= '\u28FF') {
                int pattern = c - '\u2800';
                if (pattern == 0) {
                    sb.append(' ');
                } else {
                    int dots = Integer.bitCount(pattern);
                    sb.append(dots >= 4 ? '#' : dots >= 2 ? ':' : '.');
                }
                continue;
            }
            if (isBlockGraphic(c)) {
                sb.append('#');
                continue;
            }
            if (Character.isWhitespace(c)) {
                sb.append(' ');
            } else if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            } else {
                sb.append('#');
            }
        }
        return sb.toString();
    }

    private static boolean isBlockGraphic(char c) {
        return (c >= '\u2580' && c <= '\u259F')
                || (c >= '\u25A0' && c <= '\u25FF')
                || (c >= '\u2500' && c <= '\u257F')
                || c == '\uFFFD';
    }
}
