package util;

/**
 * Включает цвета: Jansi (обычный cmd Windows) или нативная VT (Windows Terminal).
 */
public final class ColorSetup {
    private static boolean active;

    private ColorSetup() {
    }

    public static void install() {
        ConsoleEncoding.setup();
        boolean jansiOk = false;
        if (tryJansi()) {
            jansiOk = verifyJansi();
            if (!jansiOk) {
                uninstallJansi();
            }
        }
        active = jansiOk || detectNativeSupport();
        ConsoleColors.applyActive(active);
    }

    public static boolean isActive() {
        return active;
    }

    private static boolean tryJansi() {
        try {
            Class<?> ansiConsole = Class.forName("org.fusesource.jansi.AnsiConsole");
            ansiConsole.getMethod("systemInstall").invoke(null);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean verifyJansi() {
        try {
            Class<?> ansi = Class.forName("org.fusesource.jansi.Ansi");
            Object enabled = ansi.getMethod("isEnabled").invoke(null);
            return Boolean.TRUE.equals(enabled);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void uninstallJansi() {
        try {
            Class<?> ansiConsole = Class.forName("org.fusesource.jansi.AnsiConsole");
            ansiConsole.getMethod("systemUninstall").invoke(null);
        } catch (Throwable ignored) {
            // ignore
        }
    }

    private static boolean detectNativeSupport() {
        if (System.getenv("NO_COLOR") != null) {
            return false;
        }
        String os = System.getProperty("os.name", "").toLowerCase();
        if (!os.contains("win")) {
            return System.console() != null;
        }
        return System.getenv("WT_SESSION") != null
                || System.getenv("TERM") != null
                || System.getenv("ANSICON") != null;
    }
}
