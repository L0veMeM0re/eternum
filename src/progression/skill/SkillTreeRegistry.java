package progression.skill;

import player.CharacterClass;
import player.ClassSkillKit;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class SkillTreeRegistry {
    public enum TreeMode {
        HERO(3, true),
        MERCENARY(2, false);

        private final int upgradeMaxRank;
        private final boolean hasCapstone;

        TreeMode(int upgradeMaxRank, boolean hasCapstone) {
            this.upgradeMaxRank = upgradeMaxRank;
            this.hasCapstone = hasCapstone;
        }
    }

    private static final Map<CharacterClass, List<SkillNodeDef>> HERO_TREES = new EnumMap<>(CharacterClass.class);
    private static final Map<CharacterClass, List<SkillNodeDef>> MERC_TREES = new EnumMap<>(CharacterClass.class);

    static {
        for (CharacterClass cls : CharacterClass.values()) {
            HERO_TREES.put(cls, buildTree(cls, TreeMode.HERO));
            MERC_TREES.put(cls, buildTree(cls, TreeMode.MERCENARY));
        }
    }

    private SkillTreeRegistry() {
    }

    public static List<SkillNodeDef> nodesFor(CharacterClass cls, TreeMode mode) {
        return mode == TreeMode.HERO ? HERO_TREES.get(cls) : MERC_TREES.get(cls);
    }

    public static SkillNodeDef findNode(CharacterClass cls, TreeMode mode, String nodeId) {
        for (SkillNodeDef node : nodesFor(cls, mode)) {
            if (node.getId().equals(nodeId)) {
                return node;
            }
        }
        return null;
    }

    private static List<SkillNodeDef> buildTree(CharacterClass cls, TreeMode mode) {
        ClassSkillKit kit = cls.getSkillKit();
        int up = mode.upgradeMaxRank;
        List<SkillNodeDef> nodes = new ArrayList<>();

        nodes.add(new SkillNodeDef(SkillNodeDef.ACTIVE1_UNLOCK, "Изучить: " + kit.getSkill1Name(),
                1, 1, 1, List.of(), kit.getSkill1Description()));
        nodes.add(new SkillNodeDef(SkillNodeDef.ACTIVE1_POWER, kit.getSkill1Name() + " — мощь",
                1, 2, up, List.of(SkillNodeDef.ACTIVE1_UNLOCK),
                "+5% урона скилла за ранг"));
        nodes.add(new SkillNodeDef(SkillNodeDef.ACTIVE1_MASTER, kit.getSkill1Name() + " — мастерство",
                1, 3, up, List.of(SkillNodeDef.ACTIVE1_POWER),
                classMasterDesc(cls, 1, mode)));

        nodes.add(new SkillNodeDef(SkillNodeDef.ACTIVE2_UNLOCK, "Изучить: " + kit.getSkill2Name(),
                2, 1, 1, List.of(), kit.getSkill2Description()));
        nodes.add(new SkillNodeDef(SkillNodeDef.ACTIVE2_POWER, kit.getSkill2Name() + " — мощь",
                2, 2, up, List.of(SkillNodeDef.ACTIVE2_UNLOCK),
                "+5% урона скилла за ранг"));
        nodes.add(new SkillNodeDef(SkillNodeDef.ACTIVE2_MASTER, kit.getSkill2Name() + " — мастерство",
                2, 3, up, List.of(SkillNodeDef.ACTIVE2_POWER),
                classMasterDesc(cls, 2, mode)));

        nodes.add(new SkillNodeDef(SkillNodeDef.PASSIVE_CORE, kit.getPassiveName(),
                3, 1, up, List.of(),
                kit.getPassiveDescription() + " (+ за ранг)"));
        nodes.add(new SkillNodeDef(SkillNodeDef.PASSIVE_STAT, "Усиление",
                3, 2, up, List.of(SkillNodeDef.PASSIVE_CORE),
                classStatDesc(cls)));

        if (mode.hasCapstone) {
            nodes.add(new SkillNodeDef(SkillNodeDef.CAPSTONE, capstoneName(cls),
                    2, 4, 1,
                    List.of(SkillNodeDef.ACTIVE1_MASTER, SkillNodeDef.ACTIVE2_MASTER),
                    capstoneDesc(cls)));
        }
        return nodes;
    }

    private static String classMasterDesc(CharacterClass cls, int skill, TreeMode mode) {
        return switch (cls) {
            case WARRIOR -> skill == 1 ? "+1 ход DEF-б баффа / ранг" : "+1 ход иммунитета к криту / ранг";
            case MAGE -> skill == 1 ? "+5% игнора DEF / ранг" : "+1 ход дебаффа ATK / ранг";
            case ROGUE -> skill == 1 ? "+5% crit на скилл / ранг" : "+10% урона яда / ранг";
            case CLERIC -> skill == 1 ? (mode == TreeMode.HERO ? "CD −1 на 3-м ранге" : "+5% heal / ранг")
                    : "+5% вампиризма / ранг";
            case BERSERKER -> skill == 1 ? "−2% цены HP / ранг" : "+1 ход безумия / ранг";
        };
    }

    private static String classStatDesc(CharacterClass cls) {
        return switch (cls) {
            case WARRIOR -> "+3.5% max HP / ранг";
            case MAGE -> "+3% crit / ранг";
            case ROGUE -> "+2% dodge / ранг";
            case CLERIC -> "+2% DEF отряду / ранг";
            case BERSERKER -> "+4% cap ярости / ранг";
        };
    }

    private static String capstoneName(CharacterClass cls) {
        return switch (cls) {
            case WARRIOR -> "Железная воля";
            case MAGE -> "Перегрузка";
            case ROGUE -> "Казнь";
            case CLERIC -> "Божественный свет";
            case BERSERKER -> "Последний рубеж";
        };
    }

    private static String capstoneDesc(CharacterClass cls) {
        return switch (cls) {
            case WARRIOR -> "При HP<30%: +20% DEF на 2 хода";
            case MAGE -> "10% шанс ударить оба скилла за ход";
            case ROGUE -> "Финт по цели <25% HP: ×1.5 урона";
            case CLERIC -> "+10% heal отряду";
            case BERSERKER -> "1 раз за бой пережить смертельный удар (1 HP)";
        };
    }
}
