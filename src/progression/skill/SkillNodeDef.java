package progression.skill;

import java.util.List;

public final class SkillNodeDef {
    public static final String ACTIVE1_UNLOCK = "a1_unlock";
    public static final String ACTIVE1_POWER = "a1_power";
    public static final String ACTIVE1_MASTER = "a1_master";
    public static final String ACTIVE2_UNLOCK = "a2_unlock";
    public static final String ACTIVE2_POWER = "a2_power";
    public static final String ACTIVE2_MASTER = "a2_master";
    public static final String PASSIVE_CORE = "p_core";
    public static final String PASSIVE_STAT = "p_stat";
    public static final String CAPSTONE = "capstone";
    /** Бесконечная прокачка после полного дерева: +HP и +ATK за очко. */
    public static final String OVERFLOW_STATS = "overflow_stats";

    private final String id;
    private final String displayName;
    private final int branch;
    private final int row;
    private final int maxRank;
    private final List<String> prerequisites;
    private final String description;

    public SkillNodeDef(String id, String displayName, int branch, int row,
                        int maxRank, List<String> prerequisites, String description) {
        this.id = id;
        this.displayName = displayName;
        this.branch = branch;
        this.row = row;
        this.maxRank = maxRank;
        this.prerequisites = prerequisites;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getBranch() {
        return branch;
    }

    public int getRow() {
        return row;
    }

    public int getMaxRank() {
        return maxRank;
    }

    public List<String> getPrerequisites() {
        return prerequisites;
    }

    public String getDescription() {
        return description;
    }
}
