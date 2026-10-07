package forge;

/** Цены кузницы (×10–15 от первоначального черновика). */
public final class ForgeCosts {
    public static final int INSERT = 5_000;
    public static final int EXTRACT = 4_000;
    public static final int SMELT_ORE = 6_000;
    public static final int MERGE_TO_II = 8_000;
    public static final int MERGE_TO_III = 15_000;
    public static final int ORE_PER_SMELT = 3;

    public static final int MINE_ATTEMPT = 1_000;

    private ForgeCosts() {
    }
}
