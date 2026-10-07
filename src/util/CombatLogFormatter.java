package util;

import combat.Fighter;
import combat.HeroFighter;
import enemy.Boss;
import enemy.Enemy;
import party.Mercenary;

/** Подсветка имён в логе боя. */
public final class CombatLogFormatter {
    private static final String ACTION_ATTACK = " атаковал ";
    private static final String ACTION_DODGE_FROM = " увернулся от атаки ";

    private CombatLogFormatter() {
    }

    public static String allyName(Fighter fighter) {
        String name = fighter.getCombatName();
        if (fighter.isHero() && fighter instanceof HeroFighter hero) {
            String color = hero.getPlayer().getCharacterClass().getPortraitPrimary();
            return ConsoleColors.bold(ConsoleColors.wrap(color, "[" + name + "]"));
        }
        if (fighter instanceof Mercenary) {
            return ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN, "«" + name + "»");
        }
        return ConsoleColors.wrap(ConsoleColors.BRIGHT_CYAN, "[" + name + "]");
    }

    public static String enemyName(Enemy enemy) {
        String plain = plainEnemyName(enemy);
        if (enemy instanceof Boss boss) {
            return ConsoleColors.bold(ConsoleColors.wrap(boss.getPortraitPrimaryColor(), plain));
        }
        if (enemy.isElite()) {
            return ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW, "★ " + plain);
        }
        return ConsoleColors.wrap(ConsoleColors.BRIGHT_RED, plain);
    }

    public static String attackVerb() {
        return ConsoleColors.dim(ACTION_ATTACK);
    }

    public static String dodgeFromPhrase() {
        return ConsoleColors.dim(ACTION_DODGE_FROM);
    }

    private static String plainEnemyName(Enemy enemy) {
        if (enemy.isElite()) {
            return enemy.getType().getDisplayName();
        }
        return enemy.getName();
    }
}
