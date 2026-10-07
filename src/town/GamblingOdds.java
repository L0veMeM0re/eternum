package town;

/**
 * Коэффициенты и лимиты ставок на гладиаторов.
 * <p>
 * Маржа зала: {@link #HOUSE_EDGE} — сумма (1/коэф) по обеим сторонам ≈ 1/0.875 ≈ 14% overround.
 */
public final class GamblingOdds {
    /** Доля «честной» выплаты (остальное — маржа клуба). */
    public static final double HOUSE_EDGE = 0.875;
    public static final double MIN_ODDS = 1.28;
    public static final double MAX_ODDS = 3.25;

    public static final int MIN_BET = 100;
    public static final int MAX_BET = 7500;

    private GamblingOdds() {
    }

    public record OddsPair(double redOdds, double blueOdds, int redWinPercent, int blueWinPercent) {
    }

    public static OddsPair calculate(GladiatorSide red, GladiatorSide blue) {
        double redPower = Math.max(1, red.powerRating());
        double bluePower = Math.max(1, blue.powerRating());
        double total = redPower + bluePower;
        double probRed = redPower / total;
        double probBlue = bluePower / total;
        double redOdds = clamp(HOUSE_EDGE / probRed);
        double blueOdds = clamp(HOUSE_EDGE / probBlue);
        return new OddsPair(redOdds, blueOdds, (int) Math.round(probRed * 100),
                (int) Math.round(probBlue * 100));
    }

    /** Чистая прибыль при выигрыше (ставка уже списана). */
    public static int netProfit(int bet, double odds) {
        return (int) Math.floor(bet * odds) - bet;
    }

    /** Сумма overround по двум исходам (>&nbsp;1 — маржа зала). */
    public static double impliedOverround(double redOdds, double blueOdds) {
        return 1.0 / redOdds + 1.0 / blueOdds;
    }

    private static double clamp(double odds) {
        return Math.max(MIN_ODDS, Math.min(MAX_ODDS, Math.round(odds * 100.0) / 100.0));
    }
}
