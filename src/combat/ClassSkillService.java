package combat;

import enemy.Enemy;
import enemy.EnemySquad;
import forge.GemCombatService;
import party.Mercenary;
import player.CharacterClass;
import player.ClassSkillKit;
import player.Player;
import player.Stats;
import progression.skill.SkillEffectCalculator;
import progression.skill.SkillTreeProgress;
import util.CombatLogFormatter;
import util.ConsoleColors;
import util.HpBarRenderer;

import java.util.List;

public final class ClassSkillService {
    private final CombatService combatService = new CombatService();

    private ClassSkillService() {
    }

    public static int berserkerMissingHpBonusPercent(Stats stats, SkillTreeProgress progress) {
        if (stats.getMaxHp() <= 0) {
            return 0;
        }
        double missing = (stats.getMaxHp() - stats.getCurrentHp()) * 100.0 / stats.getMaxHp();
        double perStep = SkillEffectCalculator.berserkerAtkPerMissing(progress);
        int cap = SkillEffectCalculator.berserkerAtkCap(progress);
        return Math.min(cap, (int) (missing / 5.0 * perStep));
    }

    public static Stats effectiveAttackStats(CharacterClass cls, SkillTreeProgress progress,
                                             Stats base, HeroCombatState state) {
        int atkBonus = 0;
        if (cls == CharacterClass.BERSERKER) {
            atkBonus = (int) (base.getAttack() * berserkerMissingHpBonusPercent(base, progress) / 100.0);
        }
        if (state != null && state.isInFrenzy()) {
            atkBonus += (int) (base.getAttack() * 0.25);
        }
        if (atkBonus == 0) {
            return base;
        }
        Stats copy = base.copy();
        copy.setAttack(base.getAttack() + atkBonus);
        return copy;
    }

    public static int effectiveDefense(CharacterClass cls, SkillTreeProgress progress,
                                       Stats stats, HeroCombatState state) {
        int def = stats.getDefense();
        if (state != null && state.getDefBuffRounds() > 0) {
            def = (int) (def * 1.18);
        }
        if (state != null && state.isInFrenzy()) {
            def = (int) (def * 0.83);
        }
        return def;
    }

    public static double effectiveEnemyAttack(Enemy enemy, HeroCombatState debuffStore) {
        double atk = enemy.getStats().getAttack();
        if (debuffStore != null) {
            HeroCombatState.EnemyDebuffState debuff = debuffStore.debuffFor(enemy);
            if (debuff.atkDebuffRounds > 0) {
                atk *= (1.0 - debuff.atkDebuffPercent);
            }
        }
        return atk;
    }

    public boolean tryUnitSkillTurn(Fighter unit, CharacterClass cls, SkillTreeProgress progress,
                                    List<Fighter> allies, EnemySquad squad, HeroCombatState state,
                                    HeroCombatState sharedDebuffs) {
        ClassSkillKit kit = cls.getSkillKit();
        if (SkillEffectCalculator.isSkill1Unlocked(progress) && state.isSkill1Ready()) {
            executeSkill1(unit, cls, progress, allies, squad, state, sharedDebuffs, kit);
            state.startSkill1Cooldown(SkillEffectCalculator.skill1Cooldown(cls, progress));
            return true;
        }
        if (SkillEffectCalculator.isSkill2Unlocked(progress) && state.isSkill2Ready()) {
            executeSkill2(unit, cls, progress, allies, squad, state, sharedDebuffs, kit);
            state.startSkill2Cooldown(SkillEffectCalculator.skill2Cooldown(cls, progress));
            // Защитные скиллы без урона не съедают ход — после них идёт обычная атака.
            return skill2ReplacesAttack(cls);
        }
        return false;
    }

    /** false = после скилла юнит ещё бьёт обычной атакой в этот же ход. */
    private static boolean skill2ReplacesAttack(CharacterClass cls) {
        return switch (cls) {
            case WARRIOR -> false;
            case BERSERKER -> true;
            default -> SkillEffectCalculator.skill2BaseMultiplier(cls) > 0;
        };
    }

    public void applyPoisonTicks(EnemySquad squad, EncounterCombatState encounter, List<Fighter> allies) {
        for (Enemy enemy : squad.getEnemies()) {
            if (!enemy.isAlive()) {
                continue;
            }
            int totalPoison = 0;
            for (HeroCombatState st : encounter.allUnitStates()) {
                HeroCombatState.EnemyDebuffState debuff = st.debuffFor(enemy);
                if (debuff.poisonRounds > 0 && debuff.poisonDamagePerRound > 0) {
                    totalPoison += debuff.poisonDamagePerRound;
                }
            }
            if (totalPoison <= 0) {
                continue;
            }
            enemy.getStats().takeDamage(totalPoison);
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                    "  ☠ Яд: " + CombatLogFormatter.enemyName(enemy)
                            + " −" + totalPoison + " HP"
                            + HpBarRenderer.combatHp(enemy.getStats().getCurrentHp(),
                            enemy.getStats().getMaxHp(), HpBarRenderer.Side.ENEMY)));
            for (Fighter ally : allies) {
                if (ally.isAlive()) {
                    SetAbilityService.healFromPoisonTick(ally, totalPoison);
                }
            }
        }
    }

    private void executeSkill1(Fighter unit, CharacterClass cls, SkillTreeProgress progress,
                               List<Fighter> allies, EnemySquad squad, HeroCombatState state,
                               HeroCombatState sharedDebuffs, ClassSkillKit kit) {
        printSkillHeader(unit, kit.getSkill1Name());
        switch (cls) {
            case WARRIOR -> {
                shieldBash(unit, cls, progress, squad, state);
                repeatIfFrenzy(state, () -> shieldBash(unit, cls, progress, squad, state));
            }
            case MAGE -> {
                fireWave(unit, cls, progress, squad, state);
                repeatIfFrenzy(state, () -> fireWave(unit, cls, progress, squad, state));
            }
            case ROGUE -> {
                feint(unit, cls, progress, squad, state);
                repeatIfFrenzy(state, () -> feint(unit, cls, progress, squad, state));
            }
            case CLERIC -> healLowestAlly(allies, progress);
            case BERSERKER -> {
                bloodRush(unit, cls, progress, squad, state);
                repeatIfFrenzy(state, () -> bloodRush(unit, cls, progress, squad, state));
            }
        }
    }

    private void executeSkill2(Fighter unit, CharacterClass cls, SkillTreeProgress progress,
                               List<Fighter> allies, EnemySquad squad, HeroCombatState state,
                               HeroCombatState sharedDebuffs, ClassSkillKit kit) {
        printSkillHeader(unit, kit.getSkill2Name());
        switch (cls) {
            case WARRIOR -> steadfast(unit, progress, state);
            case MAGE -> {
                iceBind(unit, cls, progress, squad, state, sharedDebuffs);
                repeatIfFrenzy(state, () -> iceBind(unit, cls, progress, squad, state, sharedDebuffs));
            }
            case ROGUE -> {
                poisonBlade(unit, cls, progress, squad, state);
                repeatIfFrenzy(state, () -> poisonBlade(unit, cls, progress, squad, state));
            }
            case CLERIC -> {
                holyStrike(unit, cls, progress, squad, state);
                repeatIfFrenzy(state, () -> holyStrike(unit, cls, progress, squad, state));
            }
            case BERSERKER -> frenzy(progress, state);
        }
    }

    private void repeatIfFrenzy(HeroCombatState state, Runnable action) {
        if (state.isInFrenzy()) {
            System.out.println(ConsoleColors.dim("  ↻ Двойной удар (безумие)"));
            action.run();
        }
    }

    private void shieldBash(Fighter unit, CharacterClass cls, SkillTreeProgress progress,
                            EnemySquad squad, HeroCombatState state) {
        Enemy target = squad.randomLivingTarget();
        if (target == null) {
            return;
        }
        double mult = SkillEffectCalculator.skill1BaseMultiplier(cls) * SkillEffectCalculator.skillDamageBonus(progress, true);
        CombatService.AttackResult hit = attackEnemyWithSetEffects(unit, cls, progress, target, state, squad,
                new CombatService.AttackModifiers().attackMultiplier(mult));
        System.out.println(formatSkillHit(unit, target, hit));
        applyPostHitEffects(unit, cls, progress, hit);
        state.addDefBuff(SkillEffectCalculator.defBuffRounds(progress));
        System.out.println(ConsoleColors.dim("  🛡 +18% DEF на " + SkillEffectCalculator.defBuffRounds(progress) + " хода"));
    }

    private void steadfast(Fighter unit, SkillTreeProgress progress, HeroCombatState state) {
        Stats stats = unit.getStats();
        if (stats.getCurrentHp() * 100 / Math.max(1, stats.getMaxHp()) < 40) {
            int healed = stats.heal(Math.max(1, (int) (stats.getMaxHp() * (0.18 + progress.getRank("a2_power") * 0.01))));
            System.out.println(combatService.formatSkillHeal(unit.getCombatName(), healed));
        }
        state.addCritImmune(SkillEffectCalculator.critImmuneRounds(progress));
        System.out.println(ConsoleColors.dim("  ✦ Иммунитет к криту на "
                + SkillEffectCalculator.critImmuneRounds(progress) + " ход"));
    }

    private void fireWave(Fighter unit, CharacterClass cls, SkillTreeProgress progress,
                          EnemySquad squad, HeroCombatState state) {
        double mult = SkillEffectCalculator.skill1BaseMultiplier(cls) * SkillEffectCalculator.skillDamageBonus(progress, true);
        for (Enemy enemy : squad.getLiving()) {
            CombatService.AttackModifiers mods = new CombatService.AttackModifiers()
                    .attackMultiplier(mult)
                    .defensePenetration(SkillEffectCalculator.fireDefPen(progress));
            CombatService.AttackResult hit = attackEnemyWithSetEffects(
                    unit, cls, progress, enemy, state, squad, mods);
            System.out.println(formatSkillHit(unit, enemy, hit));
            applyPostHitEffects(unit, cls, progress, hit);
        }
    }

    private void iceBind(Fighter unit, CharacterClass cls, SkillTreeProgress progress,
                         EnemySquad squad, HeroCombatState state, HeroCombatState sharedDebuffs) {
        Enemy target = squad.randomLivingTarget();
        if (target == null) {
            return;
        }
        double mult = SkillEffectCalculator.skill2BaseMultiplier(cls) * SkillEffectCalculator.skillDamageBonus(progress, false);
        CombatService.AttackResult hit = attackEnemyWithSetEffects(unit, cls, progress, target, state, squad,
                new CombatService.AttackModifiers().attackMultiplier(mult));
        System.out.println(formatSkillHit(unit, target, hit));
        applyPostHitEffects(unit, cls, progress, hit);
        HeroCombatState.EnemyDebuffState debuff = sharedDebuffs.debuffFor(target);
        debuff.atkDebuffRounds = SkillEffectCalculator.iceDebuffRounds(progress);
        debuff.atkDebuffPercent = SkillEffectCalculator.iceAtkDebuff(progress);
        System.out.println(ConsoleColors.dim("  ❄ −" + (int) (debuff.atkDebuffPercent * 100)
                + "% ATK врага на " + debuff.atkDebuffRounds + " хода"));
    }

    private void feint(Fighter unit, CharacterClass cls, SkillTreeProgress progress,
                       EnemySquad squad, HeroCombatState state) {
        Enemy target = squad.randomLivingTarget();
        if (target == null) {
            return;
        }
        double mult = SkillEffectCalculator.skill1BaseMultiplier(cls) * SkillEffectCalculator.skillDamageBonus(progress, true);
        CombatService.AttackModifiers mods = new CombatService.AttackModifiers()
                .attackMultiplier(mult)
                .ignoreDodge(true)
                .critBonus(SkillEffectCalculator.feintCritBonus(progress));
        CombatService.AttackResult hit = attackEnemyWithSetEffects(unit, cls, progress, target, state, squad, mods);
        System.out.println(formatSkillHit(unit, target, hit));
        applyPostHitEffects(unit, cls, progress, hit);
    }

    private void poisonBlade(Fighter unit, CharacterClass cls, SkillTreeProgress progress,
                             EnemySquad squad, HeroCombatState state) {
        Enemy target = squad.randomLivingTarget();
        if (target == null) {
            return;
        }
        double mult = SkillEffectCalculator.skill2BaseMultiplier(cls) * SkillEffectCalculator.skillDamageBonus(progress, false);
        CombatService.AttackResult hit = attackEnemyWithSetEffects(unit, cls, progress, target, state, squad,
                new CombatService.AttackModifiers().attackMultiplier(mult));
        System.out.println(formatSkillHit(unit, target, hit));
        applyPostHitEffects(unit, cls, progress, hit);
        int dot = Math.max(1, (int) (effectiveAttackStats(cls, progress, unit.getStats(), state).getAttack()
                * SkillEffectCalculator.poisonDotMultiplier(progress)));
        HeroCombatState.EnemyDebuffState debuff = state.debuffFor(target);
        debuff.poisonRounds = 3 + progress.getRank("a2_master");
        debuff.poisonDamagePerRound = dot;
        System.out.println(ConsoleColors.dim("  ☠ Яд: " + dot + " урона/ход, " + debuff.poisonRounds + " хода"));
    }

    private void healLowestAlly(List<Fighter> allies, SkillTreeProgress progress) {
        Fighter target = findLowestHpAlly(allies);
        if (target == null) {
            return;
        }
        int amount = Math.max(1, (int) (target.getStats().getMaxHp() * SkillEffectCalculator.healPercent(progress)));
        int healed = target.getStats().heal(amount);
        System.out.println(combatService.formatSkillHeal(CombatLogFormatter.allyName(target), healed));
    }

    private void holyStrike(Fighter unit, CharacterClass cls, SkillTreeProgress progress,
                            EnemySquad squad, HeroCombatState state) {
        Enemy target = squad.randomLivingTarget();
        if (target == null) {
            return;
        }
        double mult = SkillEffectCalculator.skill2BaseMultiplier(cls) * SkillEffectCalculator.skillDamageBonus(progress, false);
        CombatService.AttackResult hit = attackEnemyWithSetEffects(unit, cls, progress, target, state, squad,
                new CombatService.AttackModifiers().attackMultiplier(mult));
        System.out.println(formatSkillHit(unit, target, hit));
        if (!hit.isDodged() && hit.getDamage() > 0) {
            int healed = unit.getStats().heal(Math.max(1, (int) (hit.getDamage() * SkillEffectCalculator.holyLifesteal(progress))));
            if (healed > 0) {
                System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_RED,
                        "  ♥ Святой удар: +" + healed + " HP"));
            }
        }
        applyRogueCritHeal(unit, cls, progress, hit);
    }

    private void bloodRush(Fighter unit, CharacterClass cls, SkillTreeProgress progress,
                           EnemySquad squad, HeroCombatState state) {
        Stats stats = unit.getStats();
        int cost = Math.max(1, (int) (stats.getCurrentHp() * SkillEffectCalculator.bloodCostPercent(progress)));
        if (stats.getCurrentHp() - cost < 1) {
            cost = Math.max(0, stats.getCurrentHp() - 1);
        }
        if (cost > 0) {
            stats.takeDamage(cost);
            System.out.println(ConsoleColors.wrap(ConsoleColors.RED, "  −" + cost + " HP (цена рывка)"));
            tryBerserkerLastStand(unit, cls, progress, state);
        }
        Enemy target = squad.randomLivingTarget();
        if (target == null) {
            return;
        }
        double mult = SkillEffectCalculator.skill1BaseMultiplier(cls) * SkillEffectCalculator.skillDamageBonus(progress, true);
        CombatService.AttackResult hit = attackEnemyWithSetEffects(unit, cls, progress, target, state, squad,
                new CombatService.AttackModifiers().attackMultiplier(mult));
        System.out.println(formatSkillHit(unit, target, hit));
        applyPostHitEffects(unit, cls, progress, hit);
    }

    private void frenzy(SkillTreeProgress progress, HeroCombatState state) {
        state.startFrenzy(SkillEffectCalculator.frenzyRounds(progress));
        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_MAGENTA,
                "  ⚡ Безумие: +25% ATK, −17% DEF, двойной удар — "
                        + SkillEffectCalculator.frenzyRounds(progress) + " хода"));
    }

    private Fighter findLowestHpAlly(List<Fighter> allies) {
        Fighter lowest = null;
        double ratio = 2.0;
        for (Fighter ally : allies) {
            if (!ally.isAlive()) {
                continue;
            }
            Stats s = ally.getStats();
            double r = s.getCurrentHp() / (double) Math.max(1, s.getMaxHp());
            if (r < ratio) {
                ratio = r;
                lowest = ally;
            }
        }
        return lowest;
    }

    private void applyPostHitEffects(Fighter unit, CharacterClass cls, SkillTreeProgress progress,
                                     CombatService.AttackResult hit) {
        applyRogueCritHeal(unit, cls, progress, hit);
        int vamp = unit.applyVampirism(hit);
        String vampLine = combatService.formatVampirismHeal(vamp);
        if (!vampLine.isEmpty()) {
            System.out.println(vampLine);
        }
    }

    private void applyRogueCritHeal(Fighter unit, CharacterClass cls, SkillTreeProgress progress,
                                    CombatService.AttackResult hit) {
        if (cls != CharacterClass.ROGUE || !hit.isCritical() || hit.isDodged()) {
            return;
        }
        int healed = unit.getStats().heal(Math.max(1,
                (int) (unit.getStats().getMaxHp() * SkillEffectCalculator.rogueCritHealPercent(progress))));
        if (healed > 0) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                    "  ♥ Крит-лечение: +" + healed + " HP"));
        }
    }

    private void printSkillHeader(Fighter unit, String skillName) {
        System.out.println(CombatLogFormatter.allyName(unit)
                + ConsoleColors.wrap(ConsoleColors.BRIGHT_MAGENTA, " → «" + skillName + "»"));
    }

    private String formatSkillHit(Fighter unit, Enemy enemy, CombatService.AttackResult result) {
        Stats enemyStats = enemy.getStats();
        String hpInfo = HpBarRenderer.combatHp(enemyStats.getCurrentHp(), enemyStats.getMaxHp(),
                HpBarRenderer.Side.ENEMY);
        if (result.isDodged()) {
            return "  " + CombatLogFormatter.enemyName(enemy)
                    + CombatLogFormatter.dodgeFromPhrase()
                    + CombatLogFormatter.allyName(unit) + "!" + hpInfo;
        }
        String crit = result.isCritical()
                ? ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW, " [КРИТ!]") : "";
        String dmg = ConsoleColors.wrap(ConsoleColors.GREEN, String.valueOf(result.getDamage()));
        return "  " + CombatLogFormatter.enemyName(enemy) + " −" + dmg + crit + hpInfo;
    }

    public CombatService.AttackResult attackEnemyToFighter(Enemy enemy, Fighter target,
                                                             CharacterClass targetClass,
                                                             SkillTreeProgress targetProgress,
                                                             HeroCombatState targetState,
                                                             HeroCombatState sharedDebuffs,
                                                             List<Fighter> allies,
                                                             EncounterCombatState encounter) {
        Stats defenderStats = target.getStats();
        CombatService.AttackModifiers mods = new CombatService.AttackModifiers();

        int baseDef = defenderStats.getDefense();
        int effectiveDef = effectiveDefense(targetClass, targetProgress, defenderStats, targetState);
        if (baseDef > 0 && effectiveDef != baseDef) {
            mods.defenderDefBonus((double) effectiveDef / baseDef - 1.0);
        }
        if (targetState.getCritImmuneRounds() > 0) {
            mods.forceNoCrit(true);
        }

        SetAbilityService.applyIncomingModifiers(target, enemy, targetState, mods);

        Stats atkStats = enemy.getStats().copy();
        atkStats.setAttack((int) effectiveEnemyAttack(enemy, sharedDebuffs));

        CombatService.DamagePreview preview = combatService.previewDamage(atkStats, defenderStats, mods);
        if (preview.dodged) {
            SetAbilityService.onDefenderDodge(target, targetState);
            return new CombatService.AttackResult(0, false, true);
        }

        int damage = SetAbilityService.applyTitanBulwark(target, preview.damage, allies);
        target.getStats().takeDamage(damage);
        CombatService.AttackResult hit = new CombatService.AttackResult(damage, preview.critical, false);

        if (targetClass == CharacterClass.WARRIOR
                && !targetState.isWarriorFirstHitFrom(enemy) && hit.getDamage() > 0) {
            targetState.markWarriorFirstHitFrom(enemy);
            double reduction = SkillEffectCalculator.warriorFirstHitReduction(targetProgress);
            int refund = (int) (hit.getDamage() * reduction);
            if (refund > 0) {
                target.getStats().heal(refund);
                System.out.println(ConsoleColors.dim("  🛡 Стойкость: −" + (int) (reduction * 100) + "% урона"));
            }
        }
        if (!target.getStats().isAlive()) {
            SetAbilityService.LethalSave save = SetAbilityService.trySurviveLethal(
                    target, targetState, encounter.getCelestialAegisCharges());
            if (save == SetAbilityService.LethalSave.AEGIS) {
                encounter.consumeCelestialAegisCharge();
            }
        }
        tryBerserkerLastStand(target, targetClass, targetProgress, targetState);
        return hit;
    }

    /** Капstone берсерка: один раз за забег оставить 1 HP вместо смерти. */
    private void tryBerserkerLastStand(Fighter unit, CharacterClass cls, SkillTreeProgress progress,
                                       HeroCombatState state) {
        if (cls != CharacterClass.BERSERKER || state.isLastStandUsed() || unit.getStats().isAlive()) {
            return;
        }
        if (!SkillEffectCalculator.hasCapstone(progress)) {
            return;
        }
        state.markLastStandUsed();
        unit.getStats().setCurrentHp(1);
        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_MAGENTA,
                "  ⚡ Последний рубеж: " + CombatLogFormatter.allyName(unit) + " держится на 1 HP!"));
    }

    public CombatService.AttackResult unitNormalAttack(Fighter unit, CharacterClass cls,
                                                       SkillTreeProgress progress, Enemy target,
                                                       HeroCombatState state, EnemySquad squad) {
        CombatService.AttackResult hit = attackEnemyWithSetEffects(
                unit, cls, progress, target, state, squad, new CombatService.AttackModifiers());
        applyRogueCritHeal(unit, cls, progress, hit);
        return hit;
    }

    /** Атака по врагу с модификаторами сетов (исходящий урон + цепочки после удара). */
    public CombatService.AttackResult attackEnemyWithSetEffects(Fighter unit, CharacterClass cls,
                                                                 SkillTreeProgress progress,
                                                                 Enemy target, HeroCombatState state,
                                                                 EnemySquad squad,
                                                                 CombatService.AttackModifiers mods) {
        SetAbilityService.applyOutgoingModifiers(unit, target, state, mods);
        GemCombatService.applyOutgoingModifiers(unit, state, mods);
        CombatService.AttackResult hit = combatService.attack(
                effectiveAttackStats(cls, progress, unit.getStats(), state), target.getStats(), mods);
        SetAbilityService.afterAllyHit(unit, target, hit, squad, state, combatService);
        GemCombatService.afterHit(unit, hit);
        return hit;
    }

    public void repeatNormalAttackIfFrenzy(Fighter unit, CharacterClass cls, SkillTreeProgress progress,
                                           Enemy target, HeroCombatState state, EnemySquad squad) {
        if (!state.isInFrenzy() || target == null || !target.isAlive()) {
            return;
        }
        System.out.println(ConsoleColors.dim("  ↻ Двойной удар (безумие)"));
        CombatService.AttackResult hit = attackEnemyWithSetEffects(
                unit, cls, progress, target, state, squad, new CombatService.AttackModifiers());
        System.out.println(formatSkillHit(unit, target, hit));
        applyPostHitEffects(unit, cls, progress, hit);
    }

    private static final ClassSkillService INSTANCE = new ClassSkillService();

    public static ClassSkillService get() {
        return INSTANCE;
    }
}
