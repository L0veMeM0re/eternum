package town;

public final class GladiatorMatch {
    public enum PowerTier {
        WEAK("Слабая лига"),
        MEDIUM("Средняя лига"),
        STRONG("Сильная лига");

        public final String title;

        PowerTier(String title) {
            this.title = title;
        }
    }

    private final boolean twoVsTwo;
    private final PowerTier tier;
    private final GladiatorSide red;
    private final GladiatorSide blue;

    public GladiatorMatch(boolean twoVsTwo, PowerTier tier, GladiatorSide red, GladiatorSide blue) {
        this.twoVsTwo = twoVsTwo;
        this.tier = tier;
        this.red = red;
        this.blue = blue;
    }

    public boolean isTwoVsTwo() {
        return twoVsTwo;
    }

    public PowerTier getTier() {
        return tier;
    }

    public GladiatorSide getRed() {
        return red;
    }

    public GladiatorSide getBlue() {
        return blue;
    }

    public String formatMode() {
        return twoVsTwo ? "2 на 2" : "1 на 1";
    }
}
