package combat;

import enemy.Enemy;
import enemy.EnemySquad;
import party.Mercenary;
import party.Party;
import player.CharacterClass;
import player.Player;
import player.Stats;
import progression.skill.SkillTreeProgress;
import util.CombatLogFormatter;
import util.ConsoleColors;
import util.HpBarRenderer;
import util.RandomUtil;

import java.util.List;

public class TeamCombatService {
    private final CombatService combatService = new CombatService();
    private final ClassSkillService skillService = ClassSkillService.get();

    public void executeRound(List<Fighter> allies, EnemySquad squad, Player player,
                             EncounterCombatState encounter, Party party) {
        skillService.applyPoisonTicks(squad, encounter, allies);

        for (Fighter ally : allies) {
            if (!ally.isAlive()) {
                continue;
            }
            if (ally.isHero() && ally instanceof HeroFighter hero) {
                executeHeroTurn(hero, allies, squad, player, encounter);
                continue;
            }
            if (ally instanceof Mercenary mercenary) {
                executeMercenaryTurn(mercenary, allies, squad, player, encounter);
            }
        }

        for (Enemy enemy : squad.getEnemies()) {
            if (!enemy.isAlive()) {
                continue;
            }
            Fighter target = randomLivingAlly(allies);
            if (target == null) {
                return;
            }
            CharacterClass tClass = classOf(target, player);
            SkillTreeProgress tProgress = progressOf(target, player);
            HeroCombatState tState = encounter.stateFor(target);
            CombatService.AttackResult hit = skillService.attackEnemyToFighter(
                    enemy, target, tClass, tProgress, tState, encounter.getHeroState(), allies, encounter);
            System.out.println(formatEnemyAttack(enemy, target, hit));
        }

        encounter.endOfRound();
    }

    private void executeHeroTurn(HeroFighter hero, List<Fighter> allies, EnemySquad squad,
                                 Player player, EncounterCombatState encounter) {
        HeroCombatState state = encounter.getHeroState();
        SkillTreeProgress progress = player.getSkillProgressForCurrentClass();
        CharacterClass cls = player.getCharacterClass();

        if (skillService.tryUnitSkillTurn(hero, cls, progress, allies, squad, state, encounter.getHeroState())) {
            return;
        }
        Enemy target = squad.randomLivingTarget();
        if (target == null) {
            return;
        }
        CombatService.AttackResult hit = skillService.unitNormalAttack(hero, cls, progress, target, state, squad);
        System.out.println(formatAllyAttack(hero, target, hit));
        int vamp = hero.applyVampirism(hit);
        printVamp(vamp);
        skillService.repeatNormalAttackIfFrenzy(hero, cls, progress, target, state, squad);
    }

    private void executeMercenaryTurn(Mercenary mercenary, List<Fighter> allies, EnemySquad squad,
                                      Player player, EncounterCombatState encounter) {
        HeroCombatState state = encounter.stateFor(mercenary);
        SkillTreeProgress progress = mercenary.getSkillTree();
        CharacterClass cls = mercenary.getCharacterClass();

        if (skillService.tryUnitSkillTurn(mercenary, cls, progress, allies, squad, state, encounter.getHeroState())) {
            return;
        }
        Enemy target = squad.randomLivingTarget();
        if (target == null) {
            return;
        }
        CombatService.AttackResult hit = skillService.unitNormalAttack(mercenary, cls, progress, target, state, squad);
        System.out.println(formatAllyAttack(mercenary, target, hit));
        int vamp = mercenary.applyVampirism(hit);
        printVamp(vamp);
        skillService.repeatNormalAttackIfFrenzy(mercenary, cls, progress, target, state, squad);
    }

    private CharacterClass classOf(Fighter fighter, Player player) {
        if (fighter instanceof HeroFighter) {
            return player.getCharacterClass();
        }
        if (fighter instanceof Mercenary mercenary) {
            return mercenary.getCharacterClass();
        }
        return player.getCharacterClass();
    }

    private SkillTreeProgress progressOf(Fighter fighter, Player player) {
        if (fighter instanceof HeroFighter) {
            return player.getSkillProgressForCurrentClass();
        }
        if (fighter instanceof Mercenary mercenary) {
            return mercenary.getSkillTree();
        }
        return player.getSkillProgressForCurrentClass();
    }

    private void printVamp(int vamp) {
        String vampLine = combatService.formatVampirismHeal(vamp);
        if (!vampLine.isEmpty()) {
            System.out.println(vampLine);
        }
    }

    private Fighter randomLivingAlly(List<Fighter> allies) {
        List<Fighter> living = allies.stream().filter(Fighter::isAlive).toList();
        if (living.isEmpty()) {
            return null;
        }
        return living.get(RandomUtil.range(0, living.size() - 1));
    }

    private String formatAllyAttack(Fighter ally, Enemy enemy, CombatService.AttackResult result) {
        Stats enemyStats = enemy.getStats();
        String hpInfo = HpBarRenderer.combatHp(enemyStats.getCurrentHp(), enemyStats.getMaxHp(),
                HpBarRenderer.Side.ENEMY);
        if (result.isDodged()) {
            return CombatLogFormatter.enemyName(enemy)
                    + CombatLogFormatter.dodgeFromPhrase()
                    + CombatLogFormatter.allyName(ally) + "!" + hpInfo;
        }
        String crit = result.isCritical()
                ? ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW, " [КРИТ!]") : "";
        String dmg = ConsoleColors.wrap(ConsoleColors.GREEN, String.valueOf(result.getDamage()));
        return CombatLogFormatter.allyName(ally)
                + CombatLogFormatter.attackVerb()
                + CombatLogFormatter.enemyName(enemy)
                + " на " + dmg + crit + hpInfo;
    }

    private String formatEnemyAttack(Enemy enemy, Fighter target, CombatService.AttackResult result) {
        Stats tStats = target.getStats();
        String hpInfo = ConsoleColors.dim(" → HP ")
                + CombatLogFormatter.allyName(target)
                + ConsoleColors.dim(": ")
                + HpBarRenderer.numbersAndBar(tStats.getCurrentHp(), tStats.getMaxHp(), HpBarRenderer.Side.ALLY);
        if (result.isDodged()) {
            return CombatLogFormatter.allyName(target)
                    + CombatLogFormatter.dodgeFromPhrase()
                    + CombatLogFormatter.enemyName(enemy) + "!" + hpInfo;
        }
        String crit = result.isCritical()
                ? ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW, " [КРИТ!]") : "";
        String dmg = ConsoleColors.wrap(ConsoleColors.RED, String.valueOf(result.getDamage()));
        return CombatLogFormatter.enemyName(enemy)
                + CombatLogFormatter.attackVerb()
                + CombatLogFormatter.allyName(target)
                + " на " + dmg + crit + hpInfo;
    }
}
