package item;

public enum GemType {
    RUBY("Ruby", "ATK"),
    SAPPHIRE("Sapphire", "CRIT"),
    EMERALD("Emerald", "HP"),
    OBSIDIAN("Obsidian", "CRIT DMG"),
    AMBER("Amber", "VAMP"),
    SHARD("Осколок", "эффект");

    private final String displayName;
    private final String statLabel;

    GemType(String displayName, String statLabel) {
        this.displayName = displayName;
        this.statLabel = statLabel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getStatLabel() {
        return statLabel;
    }
}
