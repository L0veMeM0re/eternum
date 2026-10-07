package progression.skill;

import player.CharacterClass;

import java.util.ArrayList;
import java.util.List;

public final class SkillTreeService {
    public static final int RESPEC_BASE_COST = 5;
    /** За каждое «лишнее» очко после полного дерева (как за уровень). */
    public static final int OVERFLOW_HP_PER_RANK = 5;
    public static final int OVERFLOW_ATK_PER_RANK = 1;

    private SkillTreeService() {
    }

    public static int pointsEarnedFromLevel(int level) {
        return Math.max(0, level - 1);
    }

    public static int availablePoints(int level, SkillTreeProgress.PlayerTrees allTrees) {
        return pointsEarnedFromLevel(level) - allTrees.totalSpentAllClasses();
    }

    public static int availablePointsForMercenary(int level, SkillTreeProgress progress) {
        return pointsEarnedFromLevel(level) - progress.totalSpent();
    }

    public static boolean canUpgrade(SkillTreeProgress progress, CharacterClass cls,
                                     SkillTreeRegistry.TreeMode mode, String nodeId) {
        SkillNodeDef node = SkillTreeRegistry.findNode(cls, mode, nodeId);
        if (node == null) {
            return false;
        }
        if (progress.getRank(nodeId) >= node.getMaxRank()) {
            return false;
        }
        for (String prereq : node.getPrerequisites()) {
            int required = SkillNodeDef.CAPSTONE.equals(nodeId)
                    && (SkillNodeDef.ACTIVE1_MASTER.equals(prereq) || SkillNodeDef.ACTIVE2_MASTER.equals(prereq))
                    ? 2 : 1;
            if (progress.getRank(prereq) < required) {
                return false;
            }
        }
        return true;
    }

    public static String upgrade(SkillTreeProgress progress, CharacterClass cls,
                                 SkillTreeRegistry.TreeMode mode, String nodeId, int available) {
        if (available <= 0) {
            return "Нет свободных очков.";
        }
        if (!canUpgrade(progress, cls, mode, nodeId)) {
            return "Узел недоступен или уже максимален.";
        }
        SkillNodeDef node = SkillTreeRegistry.findNode(cls, mode, nodeId);
        progress.setRank(nodeId, progress.getRank(nodeId) + 1);
        return "Улучшено: " + node.getDisplayName()
                + " → ранг " + progress.getRank(nodeId) + "/" + node.getMaxRank();
    }

    public static List<String> listAvailableNodeIds(SkillTreeProgress progress, CharacterClass cls,
                                                    SkillTreeRegistry.TreeMode mode) {
        List<String> ids = new ArrayList<>();
        for (SkillNodeDef node : SkillTreeRegistry.nodesFor(cls, mode)) {
            if (canUpgrade(progress, cls, mode, node.getId())) {
                ids.add(node.getId());
            }
        }
        return ids;
    }

    public static int autoUpgradeAll(SkillTreeProgress progress, CharacterClass cls,
                                     SkillTreeRegistry.TreeMode mode, int available) {
        int spent = 0;
        boolean progressMade;
        do {
            progressMade = false;
            for (SkillNodeDef node : recommendedOrder(cls, mode)) {
                while (available - spent > 0 && canUpgrade(progress, cls, mode, node.getId())) {
                    upgrade(progress, cls, mode, node.getId(), available - spent);
                    spent++;
                    progressMade = true;
                }
            }
            while (available - spent > 0 && canUpgradeOverflow(progress, cls, mode)) {
                upgradeOverflow(progress, cls, mode, available - spent);
                spent++;
                progressMade = true;
            }
        } while (progressMade && spent < available);
        return spent;
    }

    public static boolean isTreeFullyUpgraded(SkillTreeProgress progress, CharacterClass cls,
                                                SkillTreeRegistry.TreeMode mode) {
        for (SkillNodeDef node : SkillTreeRegistry.nodesFor(cls, mode)) {
            if (progress.getRank(node.getId()) < node.getMaxRank()) {
                return false;
            }
        }
        return true;
    }

    public static boolean canUpgradeOverflow(SkillTreeProgress progress, CharacterClass cls,
                                             SkillTreeRegistry.TreeMode mode) {
        return isTreeFullyUpgraded(progress, cls, mode);
    }

    public static String upgradeOverflow(SkillTreeProgress progress, CharacterClass cls,
                                         SkillTreeRegistry.TreeMode mode, int available) {
        if (available <= 0) {
            return "Нет свободных очков.";
        }
        if (!canUpgradeOverflow(progress, cls, mode)) {
            return "Сначала прокачайте все узлы дерева.";
        }
        int cur = progress.getRank(SkillNodeDef.OVERFLOW_STATS);
        progress.setRank(SkillNodeDef.OVERFLOW_STATS, cur + 1);
        return "Усиление: +" + OVERFLOW_HP_PER_RANK + " HP, +" + OVERFLOW_ATK_PER_RANK + " ATK"
                + " → ранг " + (cur + 1);
    }

    public static int overflowRank(SkillTreeProgress progress) {
        return progress.getRank(SkillNodeDef.OVERFLOW_STATS);
    }

    public static int treeRankCapTotal(CharacterClass cls, SkillTreeRegistry.TreeMode mode) {
        int sum = 0;
        for (SkillNodeDef node : SkillTreeRegistry.nodesFor(cls, mode)) {
            sum += node.getMaxRank();
        }
        return sum;
    }

    public static int treeRankSpent(SkillTreeProgress progress, CharacterClass cls,
                                    SkillTreeRegistry.TreeMode mode) {
        int spent = 0;
        for (SkillNodeDef node : SkillTreeRegistry.nodesFor(cls, mode)) {
            spent += progress.getRank(node.getId());
        }
        return spent;
    }

    private static List<SkillNodeDef> recommendedOrder(CharacterClass cls, SkillTreeRegistry.TreeMode mode) {
        List<SkillNodeDef> order = new ArrayList<>();
        for (SkillNodeDef node : SkillTreeRegistry.nodesFor(cls, mode)) {
            order.add(node);
        }
        order.sort((a, b) -> {
            int pa = priority(a.getId());
            int pb = priority(b.getId());
            if (pa != pb) {
                return Integer.compare(pa, pb);
            }
            return Integer.compare(a.getBranch(), b.getBranch());
        });
        return order;
    }

    private static int priority(String id) {
        return switch (id) {
            case SkillNodeDef.ACTIVE1_UNLOCK, SkillNodeDef.ACTIVE2_UNLOCK -> 0;
            case SkillNodeDef.PASSIVE_CORE -> 1;
            case SkillNodeDef.ACTIVE1_POWER, SkillNodeDef.ACTIVE2_POWER -> 2;
            case SkillNodeDef.ACTIVE1_MASTER, SkillNodeDef.ACTIVE2_MASTER -> 3;
            case SkillNodeDef.PASSIVE_STAT -> 4;
            case SkillNodeDef.CAPSTONE -> 5;
            default -> 9;
        };
    }

    public static int respecCost(SkillTreeProgress progress) {
        return RESPEC_BASE_COST + progress.totalSpent() / 2;
    }

    public static void respec(SkillTreeProgress progress) {
        progress.clear();
    }

    public static String preview(SkillTreeProgress progress, CharacterClass cls,
                                 SkillTreeRegistry.TreeMode mode, String nodeId) {
        SkillNodeDef node = SkillTreeRegistry.findNode(cls, mode, nodeId);
        if (node == null) {
            return "Неизвестный узел.";
        }
        int cur = progress.getRank(nodeId);
        int next = Math.min(node.getMaxRank(), cur + 1);
        if (cur >= node.getMaxRank()) {
            return node.getDisplayName() + ": максимальный ранг (" + cur + "/" + node.getMaxRank() + ")";
        }
        return node.getDisplayName() + ": " + cur + " → " + next + "/" + node.getMaxRank()
                + "\n  " + node.getDescription();
    }
}
