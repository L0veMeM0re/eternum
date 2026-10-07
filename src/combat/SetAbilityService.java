package combat;

import enemy.Enemy;
import enemy.EnemySquad;
import inventory.SetBonusCalculator;
import item.ItemSetAbility;
import party.Mercenary;
import player.Stats;
import util.CombatLogFormatter;
import util.ConsoleColors;
import util.RandomUtil;

import java.util.EnumSet;
import java.util.List;

/** Уникальные механики сетов (пороги 4/6) в бою. */
public final class SetAbilityService {
    private SetAbilityService() {
    }

    public static boolean has(Fighter fighter, ItemSetAbility ability) {
        return active(fighter).contains(ability);
    }

    public static EnumSet<ItemSetAbility> active(Fighter fighter) {
        if (fighter == null || fighter.getEquipment() == null) {
            return EnumSet.noneOf(ItemSetAbility.class);
        }
        return SetBonusCalculator.calculate(fighter.getEquipment()).activeAbilities;
    }

    public static double outgoingAttackBonus(Fighter unit, HeroCombatState state) {
        EnumSet<ItemSetAbility> ab = active(unit);
        double bonus = 0;
        Stats stats = unit.getStats();
        if (ab.contains(ItemSetAbility.DRAGON_FURY) && stats.getMaxHp() > 0) {
            if (stats.getCurrentHp() * 100 / stats.getMaxHp() < 40) {
                bonus += 0.20;
            }
        }
        if (ab.contains(ItemSetAbility.ABYSS_DEEP) && stats.getMaxHp() > 0) {
            int missingPct = (stats.getMaxHp() - stats.getCurrentHp()) * 100 / stats.getMaxHp();
            bonus += Math.min(0.27, (missingPct / 10) * 0.03);
        }
        if (ab.contains(ItemSetAbility.CHAOS_MOMENTUM)) {
            bonus += Math.min(0.20, state.getFloorKillCount() * 0.02);
        }
        if (state.getPhoenixAshBuffRounds() > 0) {
            bonus += 0.15;
        }
        return bonus;
    }

    public static void applyOutgoingModifiers(Fighter unit, Enemy target, HeroCombatState state,
                                              CombatService.AttackModifiers mods) {
        EnumSet<ItemSetAbility> ab = active(unit);
        if (ab.contains(ItemSetAbility.DRAGON_FIRST_STRIKE) && !state.hasFirstStrikeUsed(target)) {
            mods.damageMultiplier(mods.getDamageMultiplier() * 1.25);
            state.markFirstStrikeUsed(target);
            logSet("Жар крови", "+25% первый удар");
        }
        if (ab.contains(ItemSetAbility.SHADOW_EMPOWER) && state.isShadowEmpowerReady()) {
            mods.damageMultiplier(mods.getDamageMultiplier() * 1.40);
            state.consumeShadowEmpower();
            logSet("Из тени", "+40% после уворота");
        }
        double atkBonus = outgoingAttackBonus(unit, state);
        if (atkBonus > 0) {
            mods.attackMultiplier(mods.getAttackMultiplier() * (1.0 + atkBonus));
        }
    }

    public static void applyIncomingModifiers(Fighter defender, Enemy attacker, HeroCombatState state,
                                              CombatService.AttackModifiers mods) {
        EnumSet<ItemSetAbility> ab = active(defender);
        if (ab.contains(ItemSetAbility.TITAN_STANCE)) {
            mods.incomingDamageMultiplier(mods.getIncomingDamageMultiplier() * 0.88);
        }
        if (ab.contains(ItemSetAbility.ABYSS_WARD)
                && !state.hasAbyssWardUsed(attacker) && attacker != null) {
            mods.incomingDamageMultiplier(mods.getIncomingDamageMultiplier() * 0.70);
            state.markAbyssWardUsed(attacker);
            logSet("Тёмная броня", "−30% первый удар");
        }
    }

    public static void onDefenderDodge(Fighter defender, HeroCombatState state) {
        if (has(defender, ItemSetAbility.SHADOW_EMPOWER)) {
            state.enableShadowEmpower();
        }
    }

    public static void afterAllyHit(Fighter attacker, Enemy primary, CombatService.AttackResult hit,
                                    EnemySquad squad, HeroCombatState state, CombatService combatService) {
        if (hit.isDodged() || hit.getDamage() <= 0) {
            return;
        }
        EnumSet<ItemSetAbility> ab = active(attacker);
        state.incrementAttackCounter();

        if (ab.contains(ItemSetAbility.CHAOS_DOUBLE) && RandomUtil.chance(12)) {
            int extra = Math.max(1, (int) (hit.getDamage() * 0.60));
            primary.getStats().takeDamage(extra);
            logSet("Искажение", "повтор −" + extra);
        }

        if (ab.contains(ItemSetAbility.STORM_CHAIN) && state.getAttackCounter() % 3 == 0) {
            Enemy secondary = squad.randomLivingTargetExcluding(primary);
            if (secondary != null) {
                int chain = Math.max(1, hit.getDamage() / 2);
                secondary.getStats().takeDamage(chain);
                System.out.println(ConsoleColors.dim("  ⚡ Разряд: "
                        + CombatLogFormatter.enemyName(secondary) + " −" + chain));
            }
        }

        if (!primary.isAlive()) {
            state.incrementFloorKillCount();
            if (ab.contains(ItemSetAbility.STORM_KILLSTRIKE) && RandomUtil.chance(20)) {
                Enemy extra = squad.randomLivingTarget();
                if (extra != null) {
                    int bolt = Math.max(1, attacker.getStats().getAttack() - extra.getStats().getDefense());
                    extra.getStats().takeDamage(bolt);
                    System.out.println(ConsoleColors.dim("  ⚡ Молния: "
                            + CombatLogFormatter.enemyName(extra) + " −" + bolt));
                }
            }
        }
    }

    public static int applyTitanBulwark(Fighter target, int damage, List<Fighter> allies) {
        if (damage <= 0 || !has(target, ItemSetAbility.TITAN_BULWARK)) {
            return damage;
        }
        Mercenary tank = highestHpMercenary(allies);
        if (tank == null || !tank.isAlive() || tank == target) {
            return damage;
        }
        int redirect = Math.max(1, damage * 15 / 100);
        tank.getStats().takeDamage(redirect);
        logSet("Оплот", redirect + " урона → " + tank.getName());
        return Math.max(1, damage - redirect);
    }

    public enum LethalSave { NONE, AEGIS, PHOENIX }

    public static LethalSave trySurviveLethal(Fighter unit, HeroCombatState state, int aegisCharges) {
        if (unit.getStats().isAlive()) {
            return LethalSave.NONE;
        }
        EnumSet<ItemSetAbility> ab = active(unit);
        if (ab.contains(ItemSetAbility.CELESTIAL_AEGIS) && aegisCharges > 0) {
            unit.getStats().setCurrentHp(1);
            logSet("Звёздный щит", CombatLogFormatter.allyName(unit) + " держится на 1 HP");
            return LethalSave.AEGIS;
        }
        if (ab.contains(ItemSetAbility.PHOENIX_ASH) && !state.isPhoenixAshUsed()) {
            state.markPhoenixAshUsed();
            state.startPhoenixAshBuff(3);
            unit.getStats().setCurrentHp(1);
            logSet("Пепел", CombatLogFormatter.allyName(unit) + " возрождается на 1 HP");
            return LethalSave.PHOENIX;
        }
        return LethalSave.NONE;
    }

    public static int healFromPoisonTick(Fighter wearer, int totalPoisonDamage) {
        if (totalPoisonDamage <= 0 || !has(wearer, ItemSetAbility.SHADOW_VAMP_DOT)) {
            return 0;
        }
        int heal = Math.max(1, totalPoisonDamage * 30 / 100);
        int actual = wearer.getStats().heal(heal);
        if (actual > 0) {
            logSet("Поглощение", "+" + actual + " HP от яда");
        }
        return actual;
    }

    public static double xpMultiplier(Fighter hero) {
        return has(hero, ItemSetAbility.CELESTIAL_RESONANCE) ? 1.10 : 1.0;
    }

    public static void applyPhoenixEmberHeal(Fighter unit) {
        if (!has(unit, ItemSetAbility.PHOENIX_EMBER)) {
            return;
        }
        int heal = unit.getStats().heal(Math.max(1, unit.getStats().getMaxHp() * 5 / 100));
        if (heal > 0) {
            logSet("Искра", CombatLogFormatter.allyName(unit) + " +" + heal + " HP");
        }
    }

    private static Mercenary highestHpMercenary(List<Fighter> allies) {
        Mercenary best = null;
        int bestHp = -1;
        for (Fighter f : allies) {
            if (f instanceof Mercenary m && m.isAlive()) {
                if (m.getStats().getMaxHp() > bestHp) {
                    bestHp = m.getStats().getMaxHp();
                    best = m;
                }
            }
        }
        return best;
    }

    private static void logSet(String name, String msg) {
        System.out.println(ConsoleColors.dim("  ✦ [" + name + "] " + msg));
    }
}
