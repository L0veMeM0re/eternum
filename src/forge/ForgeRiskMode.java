package forge;

/** Режим риска: выше риск — лучше результат при успехе, та же цена. */
public enum ForgeRiskMode {
    STABLE("Stable", "Стабильная"),
    NORMAL("Normal", "Обычная"),
    MAD("Mad", "Безумная");

    private final String id;
    private final String title;

    ForgeRiskMode(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
