package player;

/** Потолки боевых процентных статов игрока и наёмников. */
public final class StatCaps {
    public static final double MAX_CRIT_CHANCE = 50.0;
    public static final double MAX_DODGE_CHANCE = 66.0;

    private StatCaps() {
    }

    public static double capCrit(double value) {
        return Math.min(MAX_CRIT_CHANCE, Math.max(0, value));
    }

    public static double capDodge(double value) {
        return Math.min(MAX_DODGE_CHANCE, Math.max(0, value));
    }
}
