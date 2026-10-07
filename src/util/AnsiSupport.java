package util;

@Deprecated
public final class AnsiSupport {
    private AnsiSupport() {
    }

    public static boolean isEnabled() {
        return ColorSetup.isActive();
    }
}
