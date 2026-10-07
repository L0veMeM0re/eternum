package game;

import party.Mercenary;
import player.CharacterClass;
import player.Player;
import progression.MetaProgression;
import progression.skill.SkillNodeDef;
import progression.skill.SkillNodeDetails;
import progression.skill.SkillTreeProgress;
import progression.skill.SkillTreeRegistry;
import progression.skill.SkillTreeRenderer;
import progression.skill.SkillTreeService;
import util.ConsoleColors;
import util.ScreenFrame;

import java.util.List;
import java.util.function.Supplier;

public final class SkillTreeMenu {
    private SkillTreeMenu() {
    }

    public static void showHeroMenu(Player player, MetaProgression meta, Supplier<String> readLine, Runnable onSave) {
        runMenu(player.getCharacterClass(), player.getSkillProgressForCurrentClass(),
                SkillTreeRegistry.TreeMode.HERO, player.getLevel(), player::getAvailableSkillPoints,
                meta, readLine, onSave,
                () -> player.recalculateStats(meta),
                player.getCharacterClass().getDisplayName(),
                "Главное", "Персонаж");
    }

    public static void showMercenaryMenu(Mercenary mercenary, Player player, MetaProgression meta,
                                         Supplier<String> readLine, Runnable onSave) {
        runMenu(mercenary.getCharacterClass(), mercenary.getSkillTree(),
                SkillTreeRegistry.TreeMode.MERCENARY, mercenary.getLevel(), mercenary::getAvailableSkillPoints,
                meta, readLine, onSave,
                () -> refreshMercenary(mercenary, player, meta),
                mercenary.getName(),
                "Главное", "Команда", mercenary.getName());
    }

    private static void runMenu(CharacterClass cls, SkillTreeProgress progress, SkillTreeRegistry.TreeMode mode,
                                int level, Supplier<Integer> availablePoints, MetaProgression meta,
                                Supplier<String> readLine, Runnable onSave, Runnable onUpgrade,
                                String respecLabel, String... breadcrumb) {
        boolean back = false;
        while (!back) {
            int available = availablePoints.get();
            List<SkillNodeDef> ordered = SkillTreeRenderer.orderedNodes(cls, mode);
            SkillTreeRenderer.print(cls, mode, progress, level, available, breadcrumb);
            printMenuOptions(ordered.size(), available, progress, cls, mode);
            String input = readLine.get().trim();
            if ("0".equals(input)) {
                back = true;
            } else if ("A".equalsIgnoreCase(input)) {
                handleAuto(progress, cls, mode, available, () -> {
                    onUpgrade.run();
                    onSave.run();
                });
            } else if ("S".equalsIgnoreCase(input)) {
                handleOverflow(progress, cls, mode, available, () -> {
                    onUpgrade.run();
                    onSave.run();
                });
            } else if ("R".equalsIgnoreCase(input)) {
                handleRespec(progress, meta, readLine, () -> {
                    onUpgrade.run();
                    onSave.run();
                }, respecLabel);
            } else if ("P".equalsIgnoreCase(input)) {
                handlePreview(progress, cls, mode, ordered, readLine);
            } else {
                tryUpgradeByMenuIndex(input, progress, cls, mode, available, ordered, msg -> {
                    System.out.println(msg.startsWith("Улучшено")
                            ? ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN, msg)
                            : ConsoleColors.wrap(ConsoleColors.YELLOW, msg));
                    if (msg.startsWith("Улучшено")) {
                        onUpgrade.run();
                        onSave.run();
                    }
                });
            }
        }
    }

    private static void refreshMercenary(Mercenary mercenary, Player player, MetaProgression meta) {
        boolean clericAura = player.getCharacterClass() == CharacterClass.CLERIC;
        mercenary.recalculateStats(meta, player.getRunDamageBonus(), clericAura);
    }

    private static void printMenuOptions(int nodeCount, int availablePoints, SkillTreeProgress progress,
                                         CharacterClass cls, SkillTreeRegistry.TreeMode mode) {
        ScreenFrame.section("Управление");
        if (availablePoints > 0) {
            System.out.println("  1-" + nodeCount + " — улучшить узел (номер из списка выше, 1 очко = 1 ранг)");
        } else {
            System.out.println(ConsoleColors.dim("  (нет свободных очков — качайтесь или смените класс с непотраченными)"));
        }
        boolean maxed = SkillTreeService.isTreeFullyUpgraded(progress, cls, mode);
        if (maxed && availablePoints > 0) {
            System.out.println("  S — бесконечное усиление [∞]: +"
                    + SkillTreeService.OVERFLOW_HP_PER_RANK + " HP, +"
                    + SkillTreeService.OVERFLOW_ATK_PER_RANK + " ATK за очко");
        } else {
            System.out.println(ConsoleColors.dim("  S — бесконечное усиление [∞] (после полного дерева, см. блок ниже)"));
        }
        System.out.println("  A — авто-прокачка (сначала дерево, затем [∞])");
        System.out.println("  P — подробное превью узла");
        System.out.println("  R — сброс дерева за кристаллы");
        ScreenFrame.back();
        ScreenFrame.prompt();
    }

    private static void handleOverflow(SkillTreeProgress progress, CharacterClass cls,
                                       SkillTreeRegistry.TreeMode mode, int available, Runnable onSpent) {
        String msg = SkillTreeService.upgradeOverflow(progress, cls, mode, available);
        if (msg.startsWith("Усиление")) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN, "  " + msg));
            onSpent.run();
        } else {
            System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW, "  " + msg));
        }
    }

    private static void handleAuto(SkillTreeProgress progress, CharacterClass cls,
                                   SkillTreeRegistry.TreeMode mode, int available, Runnable onSpent) {
        int spent = SkillTreeService.autoUpgradeAll(progress, cls, mode, available);
        if (spent > 0) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN, "  Вложено очков: " + spent));
            onSpent.run();
        } else {
            System.out.println("  Нечего улучшать или нет очков.");
        }
    }

    private static void tryUpgradeByMenuIndex(String input, SkillTreeProgress progress, CharacterClass cls,
                                              SkillTreeRegistry.TreeMode mode, int available,
                                              List<SkillNodeDef> ordered,
                                              java.util.function.Consumer<String> onResult) {
        try {
            int idx = Integer.parseInt(input) - 1;
            if (idx < 0 || idx >= ordered.size()) {
                System.out.println("  Неверный номер узла.");
                return;
            }
            SkillNodeDef node = ordered.get(idx);
            onResult.accept(SkillTreeService.upgrade(progress, cls, mode, node.getId(), available));
        } catch (NumberFormatException e) {
            System.out.println("  Неверный выбор.");
        }
    }

    private static void handlePreview(SkillTreeProgress progress, CharacterClass cls,
                                      SkillTreeRegistry.TreeMode mode, List<SkillNodeDef> ordered,
                                      Supplier<String> readLine) {
        System.out.print("  Номер узла для превью: ");
        try {
            int idx = Integer.parseInt(readLine.get().trim()) - 1;
            if (idx < 0 || idx >= ordered.size()) {
                System.out.println("  Неверный номер.");
                return;
            }
            SkillNodeDef node = ordered.get(idx);
            int rank = progress.getRank(node.getId());
            System.out.println();
            System.out.println(ConsoleColors.bold("  " + node.getDisplayName() + " (" + rank + "/" + node.getMaxRank() + ")"));
            System.out.println("  Сейчас: " + SkillNodeDetails.effectNow(cls, node.getId(), rank));
            if (rank < node.getMaxRank()) {
                System.out.println("  После покупки: " + SkillNodeDetails.effectNext(cls, node.getId(), rank));
            }
            System.out.println("  " + ConsoleColors.dim(node.getDescription()));
        } catch (NumberFormatException e) {
            System.out.println("  Неверный номер.");
        }
    }

    private static void handleRespec(SkillTreeProgress progress, MetaProgression meta,
                                     Supplier<String> readLine, Runnable onDone, String label) {
        int cost = SkillTreeService.respecCost(progress);
        if (progress.totalSpent() == 0) {
            System.out.println("  Дерево пустое.");
            return;
        }
        System.out.println("  Сброс «" + label + "» за " + cost + " кристаллов. 1 — да, 0 — нет");
        if (!"1".equals(readLine.get().trim())) {
            return;
        }
        if (!meta.spendSoulCrystals(cost)) {
            System.out.println("  Недостаточно кристаллов.");
            return;
        }
        SkillTreeService.respec(progress);
        onDone.run();
        System.out.println("  Дерево сброшено.");
    }
}
