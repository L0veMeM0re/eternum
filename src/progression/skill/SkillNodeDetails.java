package progression.skill;

import player.CharacterClass;
import player.ClassSkillKit;
import util.ConsoleColors;

/** Человекочитаемые описания узлов дерева. */
public final class SkillNodeDetails {
    private SkillNodeDetails() {
    }

    public static String effectSummary(CharacterClass cls, String nodeId, int rank, int maxRank) {
        if (rank <= 0 && !SkillNodeDef.PASSIVE_CORE.equals(nodeId)) {
            return ConsoleColors.dim("     → ") + effectNext(cls, nodeId, 0);
        }
        if (rank >= maxRank) {
            return ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN, "     → ") + effectNow(cls, nodeId, rank) + " (макс.)";
        }
        return ConsoleColors.wrap(ConsoleColors.BRIGHT_CYAN, "     → ") + effectNow(cls, nodeId, rank)
                + ConsoleColors.dim(" | след.: ") + effectNext(cls, nodeId, rank);
    }

    public static String effectNow(CharacterClass cls, String nodeId, int rank) {
        ClassSkillKit kit = cls.getSkillKit();
        return switch (nodeId) {
            case SkillNodeDef.ACTIVE1_UNLOCK -> kit.getSkill1Description();
            case SkillNodeDef.ACTIVE2_UNLOCK -> kit.getSkill2Description();
            case SkillNodeDef.ACTIVE1_POWER, SkillNodeDef.ACTIVE2_POWER ->
                    "+" + (rank * 5) + "% урона скилла";
            case SkillNodeDef.ACTIVE1_MASTER -> active1MasterNow(cls, rank);
            case SkillNodeDef.ACTIVE2_MASTER -> active2MasterNow(cls, rank);
            case SkillNodeDef.PASSIVE_CORE -> passiveCoreNow(cls, rank, kit);
            case SkillNodeDef.PASSIVE_STAT -> passiveStatNow(cls, rank);
            case SkillNodeDef.CAPSTONE -> capstoneDesc(cls);
            default -> "";
        };
    }

    public static String effectNext(CharacterClass cls, String nodeId, int rank) {
        ClassSkillKit kit = cls.getSkillKit();
        return switch (nodeId) {
            case SkillNodeDef.ACTIVE1_UNLOCK -> "откроет «" + kit.getSkill1Name() + "» (CD " + kit.getSkill1Cooldown() + ")";
            case SkillNodeDef.ACTIVE2_UNLOCK -> "откроет «" + kit.getSkill2Name() + "» (CD " + kit.getSkill2Cooldown() + ")";
            case SkillNodeDef.ACTIVE1_POWER, SkillNodeDef.ACTIVE2_POWER ->
                    "+" + ((rank + 1) * 5) + "% урона скилла";
            case SkillNodeDef.ACTIVE1_MASTER -> active1MasterNext(cls, rank);
            case SkillNodeDef.ACTIVE2_MASTER -> active2MasterNext(cls, rank);
            case SkillNodeDef.PASSIVE_CORE -> passiveCoreNext(cls, rank);
            case SkillNodeDef.PASSIVE_STAT -> passiveStatNext(cls, rank);
            case SkillNodeDef.CAPSTONE -> capstoneDesc(cls);
            default -> "";
        };
    }

    public static String shortLabel(SkillNodeDef node) {
        return switch (node.getId()) {
            case SkillNodeDef.ACTIVE1_UNLOCK, SkillNodeDef.ACTIVE2_UNLOCK -> "Изучить";
            case SkillNodeDef.CAPSTONE -> "⚡ " + node.getDisplayName();
            case SkillNodeDef.PASSIVE_CORE -> node.getDisplayName();
            case SkillNodeDef.PASSIVE_STAT -> "Усиление";
            case SkillNodeDef.ACTIVE1_POWER, SkillNodeDef.ACTIVE2_POWER -> "Мощь";
            default -> "Мастерство";
        };
    }

    private static String active1MasterNow(CharacterClass cls, int rank) {
        return switch (cls) {
            case WARRIOR -> "DEF-бафф " + (2 + rank) + " хода";
            case MAGE -> "игнор " + (int) ((0.50 + rank * 0.05) * 100) + "% DEF (Огненная волна)";
            case ROGUE -> "+" + (int) (25 + rank * 5) + "% crit на Финт";
            case CLERIC -> rank >= 3 ? "CD Исцеления: 3" : "heal " + (int) ((0.20 + rank * 0.015 + 0.005) * 100) + "% HP";
            case BERSERKER -> "цена рывка " + Math.max(4, 9 - rank) + "% HP";
        };
    }

    private static String active1MasterNext(CharacterClass cls, int rank) {
        return switch (cls) {
            case WARRIOR -> "DEF-бафф " + (3 + rank) + " хода";
            case MAGE -> "игнор " + (int) ((0.55 + rank * 0.05) * 100) + "% DEF";
            case ROGUE -> "+" + (int) (30 + rank * 5) + "% crit";
            case CLERIC -> rank + 1 >= 3 ? "CD 4→3" : "heal +" + (int) (0.5 + rank * 0.5) + "%";
            case BERSERKER -> "цена " + Math.max(4, 8 - rank) + "% HP";
        };
    }

    private static String active2MasterNow(CharacterClass cls, int rank) {
        return switch (cls) {
            case WARRIOR -> "анти-крит " + (1 + rank) + " ход";
            case MAGE -> "−ATK врага " + (2 + rank) + " хода";
            case ROGUE -> "яд " + (30 + rank * 10) + "% ATK/ход";
            case CLERIC -> "вампиризм " + (50 + rank * 10) + "%";
            case BERSERKER -> "безумие " + (3 + rank) + " хода";
        };
    }

    private static String active2MasterNext(CharacterClass cls, int rank) {
        return switch (cls) {
            case WARRIOR -> "анти-крит " + (2 + rank) + " хода";
            case MAGE -> "−ATK " + (3 + rank) + " хода";
            case ROGUE -> "яд " + (40 + rank * 10) + "% ATK";
            case CLERIC -> "вампиризм " + (60 + rank * 10) + "%";
            case BERSERKER -> "безумие " + (4 + rank) + " хода";
        };
    }

    private static String passiveCoreNow(CharacterClass cls, int rank, ClassSkillKit kit) {
        if (rank == 0) {
            return kit.getPassiveDescription() + " (база, без очков)";
        }
        return switch (cls) {
            case WARRIOR -> "−" + (int) ((0.28 + rank * 0.015) * 100) + "% 1-го удара";
            case MAGE -> "crit dmg +" + String.format("%.1f", 0.4 + rank * 0.05) + "×";
            case ROGUE -> "+" + (8 + rank * 2) + "% crit, крит-хил " + (5 + rank) + "% HP";
            case CLERIC -> "отряд +" + (10 + rank * 2) + "% max HP";
            case BERSERKER -> "+" + (1.75 + rank * 0.25) + "% ATK / 5% missing HP";
        };
    }

    private static String passiveCoreNext(CharacterClass cls, int rank) {
        return switch (cls) {
            case WARRIOR -> "−" + (int) ((0.295 + rank * 0.015) * 100) + "% 1-го удара";
            case MAGE -> "crit dmg +" + String.format("%.1f", 0.45 + rank * 0.05) + "×";
            case ROGUE -> "+" + (10 + rank * 2) + "% crit";
            case CLERIC -> "отряд +" + (12 + rank * 2) + "% HP";
            case BERSERKER -> "сильнее ярость крови";
        };
    }

    private static String passiveStatNow(CharacterClass cls, int rank) {
        if (rank == 0) {
            return "нет бонуса";
        }
        return switch (cls) {
            case WARRIOR -> "+" + (int) (rank * 3.5) + "% max HP";
            case MAGE -> "+" + (rank * 3) + "% crit";
            case ROGUE -> "+" + (rank * 2) + "% dodge";
            case CLERIC -> "+" + (rank * 2) + "% DEF отряду";
            case BERSERKER -> "cap ярости " + (32 + rank * 4) + "%";
        };
    }

    private static String passiveStatNext(CharacterClass cls, int rank) {
        return switch (cls) {
            case WARRIOR -> "+" + (int) ((rank + 1) * 3.5) + "% max HP";
            case MAGE -> "+" + ((rank + 1) * 3) + "% crit";
            case ROGUE -> "+" + ((rank + 1) * 2) + "% dodge";
            case CLERIC -> "+" + ((rank + 1) * 2) + "% DEF";
            case BERSERKER -> "cap " + (32 + (rank + 1) * 4) + "%";
        };
    }

    private static String capstoneDesc(CharacterClass cls) {
        SkillNodeDef cap = SkillTreeRegistry.findNode(cls, SkillTreeRegistry.TreeMode.HERO, SkillNodeDef.CAPSTONE);
        return cap != null ? cap.getDescription() : "";
    }
}
