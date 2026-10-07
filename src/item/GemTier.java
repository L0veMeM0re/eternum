package item;

public enum GemTier {
    I(1), II(2), III(3);

    private final int level;

    GemTier(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public GemTier next() {
        return switch (this) {
            case I -> II;
            case II -> III;
            case III -> III;
        };
    }

    public String roman() {
        return name();
    }
}
