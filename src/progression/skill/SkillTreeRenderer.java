package progression.skill;

import player.CharacterClass;
import player.ClassSkillKit;
import util.ConsoleColors;
import util.ScreenFrame;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SkillTreeRenderer {
    /** Ширина одной ветки (символы). */
    private static final int COL_WIDTH = 42;
    /** Зазор между ветками. */
    private static final int COL_GAP = 8;
    /** Ширина строки описания внутри колонки. */
    private static final int TEXT_WRAP = COL_WIDTH - 6;

    private SkillTreeRenderer() {
    }

    /** Узлы в порядке отображения и нумерации меню (1..N). */
    public static List<SkillNodeDef> orderedNodes(CharacterClass cls, SkillTreeRegistry.TreeMode mode) {
        List<SkillNodeDef> all = SkillTreeRegistry.nodesFor(cls, mode);
        List<SkillNodeDef> ordered = new ArrayList<>();
        for (int branch = 1; branch <= 3; branch++) {
            int b = branch;
            all.stream()
                    .filter(n -> n.getBranch() == b && !SkillNodeDef.CAPSTONE.equals(n.getId()))
                    .sorted(Comparator.comparingInt(SkillNodeDef::getRow))
                    .forEach(ordered::add);
        }
        SkillNodeDef cap = SkillTreeRegistry.findNode(cls, mode, SkillNodeDef.CAPSTONE);
        if (cap != null) {
            ordered.add(cap);
        }
        return ordered;
    }

    public static void print(CharacterClass cls, SkillTreeRegistry.TreeMode mode,
                             SkillTreeProgress progress, int level, int availablePoints) {
        print(cls, mode, progress, level, availablePoints, "Главное", "Персонаж");
    }

    public static void print(CharacterClass cls, SkillTreeRegistry.TreeMode mode,
                             SkillTreeProgress progress, int level, int availablePoints,
                             String... breadcrumb) {
        ClassSkillKit kit = cls.getSkillKit();
        String role = mode == SkillTreeRegistry.TreeMode.HERO ? "Герой" : "Наёмник";
        String subtitle = cls.getDisplayName() + " · " + role;
        ScreenFrame.open(ScreenFrame.Screen.SKILL_TREE, subtitle, breadcrumb);
        List<SkillNodeDef> ordered = orderedNodes(cls, mode);
        int totalWidth = COL_WIDTH * 3 + COL_GAP * 2;

        System.out.println("  Ур." + level + "  │  "
                + ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW, "Очков: " + availablePoints)
                + "  │  Потрачено: " + progress.totalSpent());
        System.out.println();
        System.out.println(ConsoleColors.dim("  Легенда: ● взято  ○ доступно  ◌ закрыто  [N] — номер для покупки"));
        System.out.println();

        printAsciiTree(cls, mode, kit, progress, ordered, availablePoints, totalWidth);
        printOverflowSection(progress, cls, mode, availablePoints);
        System.out.println();
    }

    private static void printOverflowSection(SkillTreeProgress progress, CharacterClass cls,
                                             SkillTreeRegistry.TreeMode mode, int availablePoints) {
        int rank = SkillTreeService.overflowRank(progress);
        boolean maxed = SkillTreeService.isTreeFullyUpgraded(progress, cls, mode);
        int treeSpent = SkillTreeService.treeRankSpent(progress, cls, mode);
        int treeCap = SkillTreeService.treeRankCapTotal(cls, mode);

        System.out.println();
        System.out.println(ConsoleColors.dim("       " + "─".repeat(52)));
        System.out.println(ConsoleColors.bold("  [∞] Бесконечное усиление")
                + ConsoleColors.dim("  · 1 очко = +"
                + SkillTreeService.OVERFLOW_HP_PER_RANK + " HP, +"
                + SkillTreeService.OVERFLOW_ATK_PER_RANK + " ATK (без лимита)"));
        System.out.println("  Ранг: " + ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW, String.valueOf(rank))
                + ConsoleColors.dim("  │  Итого бонус: +"
                + (SkillTreeService.OVERFLOW_HP_PER_RANK * rank) + " HP, +"
                + (SkillTreeService.OVERFLOW_ATK_PER_RANK * rank) + " ATK"));

        if (maxed && availablePoints > 0) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                    "  ● Доступно — нажмите S или A (авто)"));
        } else if (maxed) {
            System.out.println(ConsoleColors.dim("  ● Дерево на макс. — копите очки (уровень / другой класс)"));
        } else {
            System.out.println(ConsoleColors.dim("  ◌ Откроется после полного дерева: "
                    + treeSpent + "/" + treeCap + " рангов узлов"));
        }
    }

    private static void printAsciiTree(CharacterClass cls, SkillTreeRegistry.TreeMode mode,
                                       ClassSkillKit kit, SkillTreeProgress progress,
                                       List<SkillNodeDef> ordered, int availablePoints, int totalWidth) {
        String h1 = branchHeader(kit.getSkill1Name(), 1);
        String h2 = branchHeader(kit.getSkill2Name(), 2);
        String h3 = branchHeader(kit.getPassiveName(), 3);
        System.out.println(padCenter(h1, COL_WIDTH) + gap() + padCenter(h2, COL_WIDTH)
                + gap() + padCenter(h3, COL_WIDTH));

        String l1 = treeColumn(cls, mode, progress, ordered, 1);
        String l2 = treeColumn(cls, mode, progress, ordered, 2);
        String l3 = treeColumn(cls, mode, progress, ordered, 3);
        String[] c1 = l1.split("\n", -1);
        String[] c2 = l2.split("\n", -1);
        String[] c3 = l3.split("\n", -1);
        int rows = Math.max(c1.length, Math.max(c2.length, c3.length));
        for (int r = 0; r < rows; r++) {
            System.out.println(col(c1, r) + gap() + col(c2, r) + gap() + col(c3, r));
        }

        SkillNodeDef cap = SkillTreeRegistry.findNode(cls, mode, SkillNodeDef.CAPSTONE);
        if (cap != null) {
            int capIdx = ordered.indexOf(cap) + 1;
            System.out.println();
            int arm = (COL_WIDTH - 4) / 2;
            System.out.println(ConsoleColors.dim("       " + "─".repeat(arm) + "┬" + "─".repeat(arm)));
            printNodeLine(capIdx, cap, cls, mode, progress, availablePoints);
        }
    }

    private static String treeColumn(CharacterClass cls, SkillTreeRegistry.TreeMode mode,
                                     SkillTreeProgress progress, List<SkillNodeDef> ordered, int branch) {
        StringBuilder sb = new StringBuilder();
        List<SkillNodeDef> branchNodes = ordered.stream()
                .filter(n -> n.getBranch() == branch && !SkillNodeDef.CAPSTONE.equals(n.getId()))
                .toList();
        for (SkillNodeDef node : branchNodes) {
            int menuIdx = ordered.indexOf(node) + 1;
            int rank = progress.getRank(node.getId());
            int max = node.getMaxRank();
            sb.append("       │\n");
            sb.append("    ").append(nodeGlyph(progress, node, cls, mode))
                    .append(" [").append(menuIdx).append("] ")
                    .append(SkillNodeDetails.shortLabel(node))
                    .append(ConsoleColors.dim(" " + rank + "/" + max))
                    .append("\n");
            appendWrapped(sb, ConsoleColors.dim("       → "),
                    SkillNodeDetails.effectNow(cls, node.getId(), Math.max(0, rank)));
            if (rank < max) {
                appendWrapped(sb, ConsoleColors.dim("       + "),
                        SkillNodeDetails.effectNext(cls, node.getId(), rank));
            }
        }
        return sb.toString();
    }

    private static void appendWrapped(StringBuilder sb, String prefix, String text) {
        String plain = stripColors(text);
        if (plain.isEmpty()) {
            return;
        }
        int start = 0;
        boolean first = true;
        while (start < plain.length()) {
            int end = Math.min(start + TEXT_WRAP, plain.length());
            if (end < plain.length()) {
                int breakAt = plain.lastIndexOf(' ', end);
                if (breakAt > start + TEXT_WRAP / 2) {
                    end = breakAt;
                }
            }
            sb.append(first ? prefix : ConsoleColors.dim("         "));
            sb.append(plain, start, end).append("\n");
            start = end;
            while (start < plain.length() && plain.charAt(start) == ' ') {
                start++;
            }
            first = false;
        }
    }

    private static void printNodeLine(int idx, SkillNodeDef node, CharacterClass cls,
                                      SkillTreeRegistry.TreeMode mode, SkillTreeProgress progress,
                                      int availablePoints) {
        int rank = progress.getRank(node.getId());
        int max = node.getMaxRank();
        boolean can = SkillTreeService.canUpgrade(progress, cls, mode, node.getId()) && availablePoints > 0;
        String status = nodeGlyph(progress, node, cls, mode);
        String avail = can ? ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN, " ○") : "";
        System.out.println("  [" + idx + "] " + status + " "
                + ConsoleColors.bold(node.getDisplayName())
                + ConsoleColors.dim("  " + rank + "/" + max) + avail);
        System.out.println(SkillNodeDetails.effectSummary(cls, node.getId(), rank, max));
    }

    private static String nodeGlyph(SkillTreeProgress progress, SkillNodeDef node,
                                    CharacterClass cls, SkillTreeRegistry.TreeMode mode) {
        int rank = progress.getRank(node.getId());
        int max = node.getMaxRank();
        if (rank >= max) {
            return ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN, "●");
        }
        if (SkillTreeService.canUpgrade(progress, cls, mode, node.getId())) {
            return ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW, "○");
        }
        return ConsoleColors.dim("◌");
    }

    private static String branchHeader(String name, int num) {
        return ConsoleColors.bold("[" + num + "] " + name);
    }

    private static String gap() {
        return " ".repeat(COL_GAP);
    }

    private static String padCenter(String text, int width) {
        int len = visibleLen(text);
        if (len >= width) {
            return text;
        }
        int pad = width - len;
        int left = pad / 2;
        return " ".repeat(left) + text + " ".repeat(pad - left);
    }

    private static String col(String[] lines, int r) {
        if (r >= lines.length) {
            return " ".repeat(COL_WIDTH);
        }
        String s = lines[r];
        int len = visibleLen(s);
        if (len > COL_WIDTH) {
            return s;
        }
        return s + " ".repeat(COL_WIDTH - len);
    }

    private static int visibleLen(String s) {
        return stripColors(s).length();
    }

    private static String stripColors(String s) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\u001B') {
                while (i < s.length() && s.charAt(i) != 'm') {
                    i++;
                }
            } else {
                out.append(s.charAt(i));
            }
        }
        return out.toString();
    }
}
