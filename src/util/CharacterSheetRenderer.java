package util;

import inventory.Equipment;
import inventory.SetBonusCalculator;
import item.Item;
import item.ItemSet;
import party.Mercenary;
import player.CharacterClass;
import player.ClassSkillKit;
import player.Player;
import player.StatCaps;
import player.Stats;
import progression.LevelService;
import progression.MetaProgression;

import java.util.ArrayList;
import java.util.List;

public final class CharacterSheetRenderer {
    private static final int PORTRAIT_WIDTH = 14;
    private static final int PANEL_WIDTH = 38;
    private static final int BAR_WIDTH = 10;

    private CharacterSheetRenderer() {
    }

    public static void print(Player player, LevelService levelService) {
        print(player, levelService, true);
    }

    public static void print(Player player, LevelService levelService, boolean showHeader) {
        CharacterClass cls = player.getCharacterClass();
        Equipment eq = player.getEquipment();
        var base = player.getBaseStats();
        var total = player.getCombatStats();

        if (showHeader) {
            System.out.println();
            System.out.println(ConsoleColors.bold("══════════ ЛИСТ ПЕРСОНАЖА ══════════"));
        }

        List<String> right = buildInfoPanel(player, levelService, cls, eq, base, total);
        String[] portrait = cls.getPortrait();
        int rows = Math.max(portrait.length, right.size());

        for (int i = 0; i < rows; i++) {
            String left = i < portrait.length
                    ? MobArt.colorize(portrait[i], cls.getPortraitPrimary(), cls.getPortraitAccent(), "@*[]<>^|")
                    : "";
            String rightLine = i < right.size() ? right.get(i) : "";
            System.out.println(padRight(left, PORTRAIT_WIDTH) + "  " + rightLine);
        }

        if (hasEquipment(eq)) {
            System.out.println();
            System.out.println(ConsoleColors.bold("  ── Экипировка ──"));
            for (Equipment.Slot slot : Equipment.Slot.values()) {
                Item item = eq.get(slot);
                if (item != null) {
                    System.out.println("  " + ConsoleColors.dim(slot.getDisplayName() + ": ")
                            + item.getDisplayName()
                            + ConsoleColors.dim(" (" + item.getStatDescription() + ")"));
                }
            }
        }
        System.out.println();
        System.out.println(ConsoleColors.bold("  ── Бонусы сетов ──"));
        printSetBonuses(player);
        System.out.println();
    }

    private static List<String> buildInfoPanel(Player player, LevelService levelService, CharacterClass cls,
                                               Equipment eq, player.Stats base, player.Stats total) {
        List<String> lines = new ArrayList<>();
        String className = ConsoleColors.wrap(cls.getPortraitPrimary(), cls.getDisplayName().toUpperCase());

        lines.add(boxTop());
        lines.add(boxRow(className + "   Ур. " + player.getLevel()));
        lines.add(boxRow("XP " + player.getXp() + " / " + levelService.xpRequiredForLevel(player.getLevel())));
        lines.add(boxRow(ConsoleColors.wrap(ConsoleColors.YELLOW, "Золото: " + player.getGold())));
        lines.add(boxMid());
        lines.add(boxRow(ConsoleColors.bold("ХАРАКТЕРИСТИКИ")));
        lines.add(boxRow(statBar("HP", total.getMaxHp(), 400,
                base.getMaxHp(), eq.totalHpBonus())));
        lines.add(boxRow(statBar("ATK", total.getAttack(), 80,
                base.getAttack(), eq.totalAttackBonus())));
        lines.add(boxRow(statBar("DEF", total.getDefense(), 50,
                base.getDefense(), eq.totalDefenseBonus())));
        lines.add(boxRow(statBar("CRIT", (int) total.getCritChance(), (int) StatCaps.MAX_CRIT_CHANCE,
                (int) base.getCritChance(), (int) eq.totalCritBonus(), "%")));
        lines.add(boxRow(statBarCritDmg(total.getCritDamage(), base.getCritDamage(), eq.totalCritDamageBonus())));
        lines.add(boxRow(statBar("DODGE", (int) total.getDodgeChance(), (int) StatCaps.MAX_DODGE_CHANCE,
                (int) base.getDodgeChance(), (int) eq.totalDodgeBonus(), "%")));
        if (player.getVampirismChance() > 0) {
            lines.add(boxRow(ConsoleColors.wrap(ConsoleColors.BRIGHT_RED,
                    String.format("VAMP  %d%% → %d%% урона",
                            (int) player.getVampirismChance(), (int) player.getVampirismHealPercent()))));
        }
        lines.add(boxMid());
        appendSkillLines(lines, cls);
        lines.add(boxBot());
        lines.add(boxRow(ConsoleColors.dim(cls.getDescription())));
        return lines;
    }

    /** Экипировка по слотам и бонусы сетов (экран инвентаря, просмотр экипировки). */
    public static void printMercenary(Mercenary mercenary, LevelService levelService,
                                      MetaProgression meta, double runDamageBonus) {
        printMercenary(mercenary, levelService, meta, runDamageBonus, true);
    }

    public static void printMercenary(Mercenary mercenary, LevelService levelService,
                                      MetaProgression meta, double runDamageBonus, boolean showHeader) {
        mercenary.recalculateStats(meta, runDamageBonus);
        if (showHeader) {
            System.out.println();
            System.out.println(ConsoleColors.bold("══════════ " + mercenary.getName().toUpperCase() + " ══════════"));
        }
        printUnitSheet(
                mercenary.getCharacterClass(),
                mercenary.getName(),
                mercenary.getLevel(),
                mercenary.getXp(),
                mercenary.getBaseStats(),
                mercenary.getStats(),
                mercenary.getEquipment(),
                levelService,
                mercenary.getCharacterClass().getDescription(),
                mercenary.getVampirismChance(),
                mercenary.getVampirismHealPercent());
        printEquipmentBlockForUnit(mercenary.getName(), mercenary.getEquipment());
        printSetBonusesForEquipment(mercenary.getEquipment());
        System.out.println();
    }

    public static void printHeroInTeam(Player player, LevelService levelService, MetaProgression meta) {
        printHeroInTeam(player, levelService, meta, true);
    }

    public static void printHeroInTeam(Player player, LevelService levelService, MetaProgression meta,
                                       boolean showHeader) {
        player.recalculateStats(meta);
        if (showHeader) {
            System.out.println();
            System.out.println(ConsoleColors.bold("══════════ ГЕРОЙ ══════════"));
        }
        printUnitSheet(
                player.getCharacterClass(),
                player.getCharacterClass().getDisplayName(),
                player.getLevel(),
                player.getXp(),
                player.getBaseStats(),
                player.getCombatStats(),
                player.getEquipment(),
                levelService,
                player.getCharacterClass().getDescription(),
                player.getVampirismChance(),
                player.getVampirismHealPercent());
        printEquipmentBlockForUnit(player.getCharacterClass().getDisplayName(), player.getEquipment());
        printSetBonuses(player);
        System.out.println();
    }

    private static void printUnitSheet(CharacterClass cls, String titleName, int level, int xp,
                                       Stats base, Stats total, Equipment eq, LevelService levelService,
                                       String description, double vampChance, double vampHeal) {
        List<String> right = buildUnitInfoPanel(cls, titleName, level, xp, base, total, eq, levelService,
                description, vampChance, vampHeal);
        String[] portrait = cls.getPortrait();
        int rows = Math.max(portrait.length, right.size());

        for (int i = 0; i < rows; i++) {
            String left = i < portrait.length
                    ? MobArt.colorize(portrait[i], cls.getPortraitPrimary(), cls.getPortraitAccent(), "@*[]<>^|")
                    : "";
            String rightLine = i < right.size() ? right.get(i) : "";
            System.out.println(padRight(left, PORTRAIT_WIDTH) + "  " + rightLine);
        }
    }

    private static List<String> buildUnitInfoPanel(CharacterClass cls, String titleName, int level, int xp,
                                                   Stats base, Stats total, Equipment eq,
                                                   LevelService levelService, String description,
                                                   double vampChance, double vampHeal) {
        List<String> lines = new ArrayList<>();
        String classLine = ConsoleColors.wrap(cls.getPortraitPrimary(), cls.getDisplayName().toUpperCase());

        lines.add(boxTop());
        lines.add(boxRow(classLine));
        lines.add(boxRow(ConsoleColors.wrap(ConsoleColors.WHITE, titleName) + "   Ур. " + level));
        lines.add(boxRow("XP " + xp + " / " + levelService.xpRequiredForLevel(level)));
        lines.add(boxMid());
        lines.add(boxRow(ConsoleColors.bold("ХАРАКТЕРИСТИКИ")));
        lines.add(boxRow(statBar("HP", total.getMaxHp(), 400, base.getMaxHp(), eq.totalHpBonus())));
        lines.add(boxRow(statBar("ATK", total.getAttack(), 80, base.getAttack(), eq.totalAttackBonus())));
        lines.add(boxRow(statBar("DEF", total.getDefense(), 50, base.getDefense(), eq.totalDefenseBonus())));
        lines.add(boxRow(statBar("CRIT", (int) total.getCritChance(), (int) StatCaps.MAX_CRIT_CHANCE,
                (int) base.getCritChance(), (int) eq.totalCritBonus(), "%")));
        lines.add(boxRow(statBarCritDmg(total.getCritDamage(), base.getCritDamage(), eq.totalCritDamageBonus())));
        lines.add(boxRow(statBar("DODGE", (int) total.getDodgeChance(), (int) StatCaps.MAX_DODGE_CHANCE,
                (int) base.getDodgeChance(), (int) eq.totalDodgeBonus(), "%")));
        if (vampChance > 0) {
            lines.add(boxRow(ConsoleColors.wrap(ConsoleColors.BRIGHT_RED,
                    String.format("VAMP  %d%% → %d%% урона", (int) vampChance, (int) vampHeal))));
        }
        lines.add(boxMid());
        appendSkillLines(lines, cls);
        lines.add(boxBot());
        lines.add(boxRow(ConsoleColors.dim(description)));
        return lines;
    }

    public static void printSetBonusesForEquipment(Equipment equipment) {
        SetBonusCalculator.BonusTotals totals = SetBonusCalculator.calculate(equipment);
        System.out.println(ConsoleColors.bold("  ── Бонусы сетов ──"));
        if (totals.activeSets.isEmpty()) {
            System.out.println(ConsoleColors.dim("  (нет активных сетов)"));
            return;
        }
        for (SetBonusCalculator.ActiveSetInfo info : totals.activeSets) {
            ItemSet set = info.set();
            System.out.println("  " + ConsoleColors.wrap(set.getColor(),
                    set.getDisplayName()) + ConsoleColors.dim(" — надето " + info.equipped() + "/"
                    + set.getMaxPieces()));
            for (ItemSet.SetTier tier : set.getTiers()) {
                String mark = info.equipped() >= tier.pieces
                        ? ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN, " [✓] ")
                        : ConsoleColors.dim(" [ ] ");
                System.out.println("  " + mark + tier.describe());
            }
        }
    }

    public static void printEquipmentBlockForUnit(String unitName, Equipment eq) {
        System.out.println(ConsoleColors.bold("── Экипировка (" + unitName + ") ──"));
        for (Equipment.Slot slot : Equipment.Slot.values()) {
            Item item = eq.get(slot);
            if (item != null) {
                System.out.println("  " + ConsoleColors.dim(slot.getDisplayName() + ": ")
                        + item.getDisplayName()
                        + ConsoleColors.dim(" (" + item.getStatDescription() + ")"));
            } else {
                System.out.println("  " + slot.getDisplayName() + ": " + ConsoleColors.dim("(пусто)"));
            }
        }
        System.out.println();
    }

    public static void printEquipmentBlock(Player player) {
        Equipment eq = player.getEquipment();
        System.out.println(ConsoleColors.bold("── Экипировка ──"));
        for (Equipment.Slot slot : Equipment.Slot.values()) {
            Item item = eq.get(slot);
            if (item != null) {
                System.out.println("  " + ConsoleColors.dim(slot.getDisplayName() + ": ")
                        + item.getDisplayName()
                        + ConsoleColors.dim(" (" + item.getStatDescription() + ")"));
            } else {
                System.out.println("  " + slot.getDisplayName() + ": " + ConsoleColors.dim("(пусто)"));
            }
        }
        System.out.println();
        System.out.println(ConsoleColors.bold("── Бонусы сетов ──"));
        printSetBonuses(player);
    }

    public static void printSetBonuses(Player player) {
        SetBonusCalculator.BonusTotals totals = SetBonusCalculator.calculate(player.getEquipment());
        if (totals.activeSets.isEmpty()) {
            System.out.println(ConsoleColors.dim("  (нет активных сетов)"));
            return;
        }
        for (SetBonusCalculator.ActiveSetInfo info : totals.activeSets) {
            ItemSet set = info.set();
            System.out.println("  " + ConsoleColors.wrap(set.getColor(),
                    set.getDisplayName()) + ConsoleColors.dim(" — надето " + info.equipped() + "/"
                    + set.getMaxPieces() + " (макс. 7 слотов)"));
            for (ItemSet.SetTier tier : set.getTiers()) {
                String mark = info.equipped() >= tier.pieces
                        ? ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN, " [✓] ")
                        : ConsoleColors.dim(" [ ] ");
                System.out.println(mark + tier.describe());
            }
        }
    }

    private static void appendSkillLines(List<String> lines, CharacterClass cls) {
        ClassSkillKit kit = cls.getSkillKit();
        lines.add(boxRow(ConsoleColors.bold("УМЕНИЯ")));
        lines.add(boxRow(ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW,
                "П: " + kit.getPassiveName()) + ConsoleColors.dim(" — " + kit.getPassiveDescription())));
        lines.add(boxRow(ConsoleColors.wrap(ConsoleColors.BRIGHT_MAGENTA,
                "1: " + kit.getSkill1Name() + " (CD " + kit.getSkill1Cooldown() + ")")
                + ConsoleColors.dim(" — " + kit.getSkill1Description())));
        lines.add(boxRow(ConsoleColors.wrap(ConsoleColors.BRIGHT_MAGENTA,
                "2: " + kit.getSkill2Name() + " (CD " + kit.getSkill2Cooldown() + ")")
                + ConsoleColors.dim(" — " + kit.getSkill2Description())));
    }

    private static String statBar(String label, int total, int scaleMax, int baseVal, int bonus) {
        return statBar(label, total, scaleMax, baseVal, bonus, "");
    }

    private static String statBar(String label, int total, int scaleMax, int baseVal, int bonus, String suffix) {
        String bar = renderBar(total, scaleMax);
        String bonusText = bonus > 0
                ? ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN, " +" + bonus)
                : "";
        return String.format("%-5s %s %3d%s%s", label, bar, total, suffix, bonusText);
    }

    private static String statBarCritDmg(double total, double baseVal, double bonus) {
        String bar = renderBar((int) (total * 10), 35);
        String bonusText = bonus > 0
                ? ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN, String.format(" +%.1f", bonus))
                : "";
        return String.format("%-5s %s %4.1fx%s", "CRDMG", bar, total, bonusText);
    }

    private static String renderBar(int value, int scaleMax) {
        int clamped = Math.min(value, scaleMax);
        int filled = Math.min(BAR_WIDTH, Math.max(0, (int) Math.round(clamped * (double) BAR_WIDTH / scaleMax)));
        String full = ColorSetup.isActive() ? "█" : "#";
        String empty = ColorSetup.isActive() ? "░" : ".";
        return ConsoleColors.wrap(ConsoleColors.BRIGHT_CYAN, full.repeat(filled))
                + ConsoleColors.dim(empty.repeat(BAR_WIDTH - filled));
    }

    private static String boxTop() {
        return "  +" + "-".repeat(PANEL_WIDTH - 4) + "+";
    }

    private static String boxMid() {
        return "  +" + "-".repeat(PANEL_WIDTH - 4) + "+";
    }

    private static String boxBot() {
        return "  +" + "-".repeat(PANEL_WIDTH - 4) + "+";
    }

    private static String boxRow(String content) {
        int plainLen = visibleLength(content);
        int pad = Math.max(0, PANEL_WIDTH - 4 - plainLen);
        return "  | " + content + " ".repeat(pad) + "|";
    }

    private static int visibleLength(String s) {
        int len = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\u001B') {
                while (i < s.length() && s.charAt(i) != 'm') {
                    i++;
                }
            } else {
                len++;
            }
        }
        return len;
    }

    private static String padRight(String s, int width) {
        int len = visibleLength(s);
        if (len >= width) {
            return s;
        }
        return s + " ".repeat(width - len);
    }

    private static boolean hasEquipment(Equipment eq) {
        for (Equipment.Slot slot : Equipment.Slot.values()) {
            if (eq.get(slot) != null) {
                return true;
            }
        }
        return false;
    }
}
