package town;

import combat.CombatService;
import player.Stats;
import util.ConsoleColors;
import util.HpBarRenderer;
import util.RandomUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GladiatorArena {
    public enum Winner { RED, BLUE }

    private final CombatService combatService = new CombatService();

    private GladiatorArena() {
    }

    public static Winner fight(GladiatorSide red, GladiatorSide blue, boolean verbose) {
        return new GladiatorArena().run(red.copyForFight(), blue.copyForFight(), verbose);
    }

    private Winner run(GladiatorSide red, GladiatorSide blue, boolean verbose) {
        if (verbose) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW, "\n  ⚔ Бой начался!"));
        }
        int round = 0;
        while (red.isAlive() && blue.isAlive()) {
            round++;
            if (verbose) {
                System.out.println(ConsoleColors.dim("\n  —— Раунд " + round + " ——"));
            }
            List<Gladiator> order = buildTurnOrder(red, blue);
            for (Gladiator attacker : order) {
                if (!attacker.isAlive()) {
                    continue;
                }
                GladiatorSide atkSide = sideOf(red, blue, attacker);
                GladiatorSide enemySide = atkSide == red ? blue : red;
                if (!enemySide.isAlive()) {
                    break;
                }
                Gladiator target = pickTarget(enemySide);
                if (target == null) {
                    break;
                }
                CombatService.AttackResult hit = combatService.attack(
                        attacker.getStats(), target.getStats());
                if (verbose) {
                    printHit(attacker, target, hit, atkSide);
                }
            }
        }
        Winner winner = red.isAlive() ? Winner.RED : Winner.BLUE;
        if (verbose) {
            String name = winner == Winner.RED ? red.getLabel() : blue.getLabel();
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                    "\n  ★ Победа: " + name + "!"));
        }
        return winner;
    }

    private GladiatorSide sideOf(GladiatorSide red, GladiatorSide blue, Gladiator fighter) {
        if (red.getFighters().contains(fighter)) {
            return red;
        }
        return blue;
    }

    private List<Gladiator> buildTurnOrder(GladiatorSide red, GladiatorSide blue) {
        List<Gladiator> order = new ArrayList<>();
        order.addAll(red.getFighters());
        order.addAll(blue.getFighters());
        Collections.shuffle(order);
        return order;
    }

    private Gladiator pickTarget(GladiatorSide enemySide) {
        List<Gladiator> living = enemySide.getFighters().stream().filter(Gladiator::isAlive).toList();
        if (living.isEmpty()) {
            return null;
        }
        return living.get(RandomUtil.range(0, living.size() - 1));
    }

    private void printHit(Gladiator attacker, Gladiator target,
                          CombatService.AttackResult hit, GladiatorSide attackerSide) {
        String atkColor = attackerSide != null ? attackerSide.getColor() : ConsoleColors.WHITE;
        System.out.print("  " + ConsoleColors.wrap(atkColor, attacker.getName()));
        if (hit.isDodged()) {
            System.out.println(ConsoleColors.dim(" промах → " + target.getName()));
            return;
        }
        String crit = hit.isCritical() ? ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW, " КРИТ!") : "";
        System.out.println(" → " + target.getName() + " −" + hit.getDamage() + crit
                + HpBarRenderer.combatHp(target.getStats().getCurrentHp(),
                target.getStats().getMaxHp(), HpBarRenderer.Side.ENEMY));
    }
}
