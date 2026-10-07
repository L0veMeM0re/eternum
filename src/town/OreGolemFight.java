package town;

import combat.EncounterCombatState;
import combat.Fighter;
import combat.HeroFighter;
import combat.TeamCombatService;
import enemy.BossType;
import enemy.Enemy;
import enemy.EnemyScaling;
import enemy.EnemySquad;
import enemy.EnemyType;
import forge.ForgeService;
import item.Gem;
import player.Player;
import player.Stats;
import progression.MetaProgression;
import quest.QuestId;
import quest.QuestLog;
import quest.QuestService;
import util.ConsoleColors;
import util.HpBarRenderer;
import util.RandomUtil;
import util.ScreenFrame;

import java.util.List;
import java.util.function.Supplier;

/** Бой отряда со Стражем жилы в пещере. */
public final class OreGolemFight {
    public enum GolemRisk {
        CAREFUL("Careful", 0.90, 0.90, 0.70),
        NORMAL("Normal", 1.0, 1.0, 1.0),
        PROVOKE("Provoke", 1.12, 1.15, 1.50);

        private final String title;
        private final double statMult;
        private final double atkMult;
        private final double rewardMult;

        GolemRisk(String title, double statMult, double atkMult, double rewardMult) {
            this.title = title;
            this.statMult = statMult;
            this.atkMult = atkMult;
            this.rewardMult = rewardMult;
        }

        public String getTitle() {
            return title;
        }
    }

    private static final int BASE_ORE_MIN = 6;
    private static final int BASE_ORE_MAX = 12;

    private final TeamCombatService teamCombat = new TeamCombatService();

    private OreGolemFight() {
    }

    public static void fight(Player player, MetaProgression meta, GolemRisk risk,
                             Supplier<String> readLine, Runnable onSave) {
        new OreGolemFight().run(player, meta, risk, readLine, onSave);
    }

    private void run(Player player, MetaProgression meta, GolemRisk risk,
                     Supplier<String> readLine, Runnable onSave) {
        int virtualFloor = Math.max(25, player.getLevel() * 2);
        Stats base = EnemyScaling.scaleBossStats(BossType.FLOOR_50, virtualFloor);
        int hp = (int) (base.getMaxHp() * 1.35 * risk.statMult);
        int atk = (int) (base.getAttack() * 1.20 * risk.statMult * risk.atkMult);
        int def = (int) (base.getDefense() * 1.10 * risk.statMult);
        Stats golemStats = new Stats(hp, atk, def, base.getCritChance(), base.getCritDamage(), base.getDodgeChance());

        Enemy golem = new OreGolem(golemStats);
        EnemySquad squad = new EnemySquad(List.of(golem), true);

        player.recalculateStats(meta);
        player.getParty().prepareForRun(player, meta);
        EncounterCombatState encounter = new EncounterCombatState();
        encounter.resetForEncounter();

        ScreenFrame.open(ScreenFrame.Screen.ORE_CAVE, "Golem fight");
        System.out.println("  " + ConsoleColors.bold("Ore Golem") + " — " + risk.getTitle() + " approach");
        golem.printArt();
        System.out.println("  Golem" + HpBarRenderer.combatHp(
                golemStats.getCurrentHp(), golemStats.getMaxHp(), HpBarRenderer.Side.ENEMY));

        List<Fighter> allies = player.getParty().getCombatOrder(player);
        printAllies(allies, player);

        System.out.println("\n  [Enter] — next round");
        readLine.get();

        int round = 0;
        while (squad.isAlive() && player.getParty().isAnyMemberAlive(player)) {
            round++;
            System.out.println(ConsoleColors.dim("\n  —— Round " + round + " ——"));
            teamCombat.executeRound(allies, squad, player, encounter, player.getParty());
            if (!squad.isAlive()) {
                break;
            }
            if (!player.getParty().isAnyMemberAlive(player)) {
                System.out.println(ConsoleColors.wrap(ConsoleColors.RED,
                        "\n  ✗ Party defeated — no ore."));
                player.getParty().prepareForRun(player, meta);
                System.out.println("\n  [Enter]");
                readLine.get();
                return;
            }
            readLine.get();
        }

        if (squad.isAlive()) {
            return;
        }

        boolean dailyAvailable = !player.isGolemRewardClaimedToday();
        int ore = computeOre(risk, dailyAvailable);
        player.addRawOre(ore);

        Gem bonusGem = null;
        if (dailyAvailable && RandomUtil.chance(20)) {
            bonusGem = Gem.randomTierOne();
            if (player.getGemStash().size() < player.getMaxGemStashSize()) {
                player.addGem(bonusGem);
            } else {
                bonusGem = null;
            }
        }

        if (dailyAvailable) {
            player.markGolemRewardClaimed();
        }

        QuestService.onGolemDefeated(player);

        onSave.run();
        player.getParty().prepareForRun(player, meta);

        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                "\n  ★ Golem shattered! +" + ore + " ore"));
        if (!dailyAvailable) {
            System.out.println(ConsoleColors.dim("  (repeat win today — reduced reward)"));
        }
        if (bonusGem != null) {
            System.out.println("  + Bonus gem: " + bonusGem.describe());
        }
        System.out.println("\n  [Enter]");
        readLine.get();
    }

    private static int computeOre(GolemRisk risk, boolean dailyAvailable) {
        int base = RandomUtil.range(BASE_ORE_MIN, BASE_ORE_MAX);
        int scaled = (int) Math.round(base * risk.rewardMult);
        if (!dailyAvailable) {
            scaled = Math.max(1, scaled / 3);
        }
        return scaled;
    }

    private void printAllies(List<Fighter> allies, Player player) {
        System.out.println(ConsoleColors.bold("\n  Your party:"));
        for (Fighter ally : allies) {
            Stats s = ally.getStats();
            String name = ally instanceof HeroFighter ? "Hero" : ally.getCombatName();
            System.out.println("  • " + name + HpBarRenderer.combatHp(
                    s.getCurrentHp(), s.getMaxHp(), HpBarRenderer.Side.ALLY));
        }
    }

    /** Каменный страж пещеры. */
    private static final class OreGolem extends Enemy {
        OreGolem(Stats stats) {
            super("Ore Golem", "Deep Mine", stats, EnemyType.GOLEM);
        }

        @Override
        public void printArt() {
            String[] art = {
                    "     .-=====-.",
                    "    /  O     O  \\",
                    "   |    ====    |",
                    "    \\  .----.  /",
                    "     '==|  |=='",
                    "        |  |",
                    "       /    \\"
            };
            for (String line : art) {
                System.out.println(ConsoleColors.wrap(ConsoleColors.GRAY, "  " + line));
            }
        }
    }
}
