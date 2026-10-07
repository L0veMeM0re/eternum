package test;

import combat.CombatService;
import combat.Fighter;
import combat.HeroCombatState;
import combat.SetAbilityService;
import enemy.Enemy;
import enemy.EnemySquad;
import enemy.EnemyType;
import inventory.Equipment;
import inventory.SetBonusCalculator;
import item.Armor;
import item.ItemSet;
import item.ItemSetAbility;
import item.Rarity;
import party.Mercenary;
import player.CharacterClass;
import player.Stats;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/** Проверка регистрации и логики всех 16 способностей сетов. Запуск: java -cp out test.SetAbilityVerifierTest */
public class SetAbilityVerifierTest {
    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        testSetBonusRegistration();
        testDragonFirstStrike();
        testDragonFury();
        testShadowEmpower();
        testShadowVampDot();
        testStormChainCounter();
        testAbyssWard();
        testAbyssDeep();
        testPhoenixEmber();
        testPhoenixAsh();
        testTitanStance();
        testTitanBulwark();
        testChaosMomentum();
        testCelestialResonance();
        testCelestialAegis();
        testSkillMultiplierStacking();

        System.out.println();
        System.out.println("Итого: " + passed + " OK, " + failed + " FAIL");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testSetBonusRegistration() {
        for (ItemSet set : ItemSet.values()) {
            if (set == ItemSet.NONE) {
                continue;
            }
            int max = set.getMaxPieces();
            var totals = SetBonusCalculator.calculate(equipSet(set, max));
            EnumSet<ItemSetAbility> expected = EnumSet.noneOf(ItemSetAbility.class);
            for (ItemSet.SetTier tier : set.getTiers()) {
                if (max >= tier.pieces && tier.ability != null) {
                    expected.add(tier.ability);
                }
            }
            assertTrue("Сет " + set.name() + " (" + max + " шт): способности",
                    totals.activeAbilities.equals(expected));
        }
    }

    private static void testDragonFirstStrike() {
        Fighter f = fighter(equipSet(ItemSet.DRAGON, 4), stats(100, 50, 10));
        HeroCombatState state = new HeroCombatState();
        Enemy enemy = enemy("Гоблин", 50, 0, 0);
        CombatService.AttackModifiers mods = new CombatService.AttackModifiers()
                .forceNoCrit(true).ignoreDodge(true);
        SetAbilityService.applyOutgoingModifiers(f, enemy, state, mods);
        assertTrue("DRAGON_FIRST_STRIKE: +25% урона", mods.getDamageMultiplier() == 1.25);
        assertTrue("DRAGON_FIRST_STRIKE: одноразово на врага", state.hasFirstStrikeUsed(enemy));
        SetAbilityService.applyOutgoingModifiers(f, enemy, state, mods);
        assertTrue("DRAGON_FIRST_STRIKE: повторно без бонуса", mods.getDamageMultiplier() == 1.25);
    }

    private static void testDragonFury() {
        Stats s = stats(100, 50, 10);
        s.setCurrentHp(30);
        Fighter f = fighter(equipSet(ItemSet.DRAGON, 6), s);
        HeroCombatState state = new HeroCombatState();
        assertTrue("DRAGON_FURY: +20% ATK при HP<40%",
                SetAbilityService.outgoingAttackBonus(f, state) >= 0.20 - 0.001);
        s.setCurrentHp(80);
        assertTrue("DRAGON_FURY: нет бонуса при HP>=40%",
                SetAbilityService.outgoingAttackBonus(f, state) < 0.001);
    }

    private static void testShadowEmpower() {
        Fighter f = fighter(equipSet(ItemSet.SHADOW, 4), stats(100, 50, 10));
        HeroCombatState state = new HeroCombatState();
        SetAbilityService.onDefenderDodge(f, state);
        assertTrue("SHADOW_EMPOWER: готов после уворота", state.isShadowEmpowerReady());
        Enemy enemy = enemy("Гоблин", 50, 0, 0);
        CombatService.AttackModifiers mods = new CombatService.AttackModifiers();
        SetAbilityService.applyOutgoingModifiers(f, enemy, state, mods);
        assertTrue("SHADOW_EMPOWER: +40% урона", mods.getDamageMultiplier() == 1.40);
        assertTrue("SHADOW_EMPOWER: расходуется", !state.isShadowEmpowerReady());
    }

    private static void testShadowVampDot() {
        Fighter f = fighter(equipSet(ItemSet.SHADOW, 6), stats(100, 50, 10));
        f.getStats().setCurrentHp(50);
        int before = f.getStats().getCurrentHp();
        int healed = SetAbilityService.healFromPoisonTick(f, 100);
        assertTrue("SHADOW_VAMP_DOT: лечит 30% от яда", healed == 30);
        assertTrue("SHADOW_VAMP_DOT: HP вырос", f.getStats().getCurrentHp() == before + 30);
        Fighter noSet = fighter(new Equipment(), stats(100, 50, 10));
        assertTrue("SHADOW_VAMP_DOT: без сета 0", SetAbilityService.healFromPoisonTick(noSet, 100) == 0);
    }

    private static void testStormChainCounter() {
        Fighter f = fighter(equipSet(ItemSet.STORM, 4), stats(100, 100, 10));
        HeroCombatState state = new HeroCombatState();
        Enemy primary = enemy("Гоблин", 200, 0, 0);
        Enemy secondary = enemy("Волк", 200, 0, 0);
        EnemySquad squad = new EnemySquad(List.of(primary, secondary), false);
        CombatService combat = new CombatService();
        int hpBefore = secondary.getStats().getCurrentHp();
        for (int i = 0; i < 2; i++) {
            SetAbilityService.afterAllyHit(f, primary,
                    new CombatService.AttackResult(40, false, false), squad, state, combat);
        }
        assertTrue("STORM_CHAIN: до 3-й атаки без цепи", secondary.getStats().getCurrentHp() == hpBefore);
        SetAbilityService.afterAllyHit(f, primary,
                new CombatService.AttackResult(40, false, false), squad, state, combat);
        assertTrue("STORM_CHAIN: 3-я атака бьёт второго", secondary.getStats().getCurrentHp() < hpBefore);
    }

    private static void testAbyssWard() {
        Fighter f = fighter(equipSet(ItemSet.ABYSS, 4), stats(100, 50, 10));
        HeroCombatState state = new HeroCombatState();
        Enemy attacker = enemy("Орк", 50, 20, 0);
        CombatService.AttackModifiers mods = new CombatService.AttackModifiers();
        SetAbilityService.applyIncomingModifiers(f, attacker, state, mods);
        assertTrue("ABYSS_WARD: −30% первого удара", mods.getIncomingDamageMultiplier() == 0.70);
        SetAbilityService.applyIncomingModifiers(f, attacker, state, mods);
        assertTrue("ABYSS_WARD: одноразово", mods.getIncomingDamageMultiplier() == 0.70);
    }

    private static void testAbyssDeep() {
        Stats s = stats(100, 50, 10);
        s.setCurrentHp(50);
        Fighter f = fighter(equipSet(ItemSet.ABYSS, 6), s);
        HeroCombatState state = new HeroCombatState();
        double bonus = SetAbilityService.outgoingAttackBonus(f, state);
        assertTrue("ABYSS_DEEP: +15% ATK при 50% HP", bonus >= 0.15 - 0.001 && bonus <= 0.15 + 0.001);
    }

    private static void testPhoenixEmber() {
        Fighter f = fighter(equipSet(ItemSet.PHOENIX, 4), stats(100, 50, 10));
        f.getStats().setCurrentHp(50);
        int before = f.getStats().getCurrentHp();
        SetAbilityService.applyPhoenixEmberHeal(f);
        assertTrue("PHOENIX_EMBER: +5% max HP", f.getStats().getCurrentHp() == before + 5);
    }

    private static void testPhoenixAsh() {
        Fighter f = fighter(equipSet(ItemSet.PHOENIX, 6), stats(100, 50, 10));
        HeroCombatState state = new HeroCombatState();
        f.getStats().setCurrentHp(0);
        var save = SetAbilityService.trySurviveLethal(f, state, 0);
        assertTrue("PHOENIX_ASH: спасает", save == SetAbilityService.LethalSave.PHOENIX);
        assertTrue("PHOENIX_ASH: 1 HP", f.getStats().getCurrentHp() == 1);
        assertTrue("PHOENIX_ASH: бафф 3 хода", state.getPhoenixAshBuffRounds() == 3);
        f.getStats().setCurrentHp(0);
        assertTrue("PHOENIX_ASH: одноразово", SetAbilityService.trySurviveLethal(f, state, 0)
                == SetAbilityService.LethalSave.NONE);
    }

    private static void testTitanStance() {
        Fighter f = fighter(equipSet(ItemSet.TITAN, 4), stats(100, 50, 10));
        HeroCombatState state = new HeroCombatState();
        CombatService.AttackModifiers mods = new CombatService.AttackModifiers();
        SetAbilityService.applyIncomingModifiers(f, null, state, mods);
        assertTrue("TITAN_STANCE: −12% урона", mods.getIncomingDamageMultiplier() == 0.88);
    }

    private static void testTitanBulwark() {
        Fighter hero = fighter(equipSet(ItemSet.TITAN, 6), stats(100, 50, 10));
        Mercenary tank = new Mercenary("Танк", CharacterClass.WARRIOR);
        tank.getStats().setMaxHp(200);
        tank.getStats().setCurrentHp(200);
        List<Fighter> allies = List.of(hero, tank);
        int tankHp = tank.getStats().getCurrentHp();
        int dmg = SetAbilityService.applyTitanBulwark(hero, 100, allies);
        assertTrue("TITAN_BULWARK: герой получает 85", dmg == 85);
        assertTrue("TITAN_BULWARK: танк получает 15", tank.getStats().getCurrentHp() == tankHp - 15);
    }

    private static void testChaosMomentum() {
        Fighter f = fighter(equipSet(ItemSet.CHAOS, 6), stats(100, 50, 10));
        HeroCombatState state = new HeroCombatState();
        state.incrementFloorKillCount();
        state.incrementFloorKillCount();
        state.incrementFloorKillCount();
        double bonus = SetAbilityService.outgoingAttackBonus(f, state);
        assertTrue("CHAOS_MOMENTUM: +6% за 3 убийства", bonus >= 0.06 - 0.001);
    }

    private static void testCelestialResonance() {
        Fighter f = fighter(equipSet(ItemSet.CELESTIAL, 4), stats(100, 50, 10));
        assertTrue("CELESTIAL_RESONANCE: x1.1 XP", SetAbilityService.xpMultiplier(f) == 1.10);
    }

    private static void testCelestialAegis() {
        Fighter f = fighter(equipSet(ItemSet.CELESTIAL, 6), stats(100, 50, 10));
        HeroCombatState state = new HeroCombatState();
        f.getStats().setCurrentHp(0);
        var save = SetAbilityService.trySurviveLethal(f, state, 1);
        assertTrue("CELESTIAL_AEGIS: спасает с зарядом", save == SetAbilityService.LethalSave.AEGIS);
        assertTrue("CELESTIAL_AEGIS: 1 HP", f.getStats().getCurrentHp() == 1);
        f.getStats().setCurrentHp(0);
        assertTrue("CELESTIAL_AEGIS: без заряда не спасает",
                SetAbilityService.trySurviveLethal(f, state, 0) == SetAbilityService.LethalSave.NONE);
    }

    /** Множитель скилла и сетовый ATK-бонус должны перемножаться, а не затирать друг друга. */
    private static void testSkillMultiplierStacking() {
        Stats s = stats(100, 30, 10);
        s.setCurrentHp(20);
        Fighter f = fighter(equipSet(ItemSet.DRAGON, 6), s);
        HeroCombatState state = new HeroCombatState();
        Enemy enemy = enemy("Гоблин", 50, 0, 0);
        CombatService.AttackModifiers mods = new CombatService.AttackModifiers()
                .attackMultiplier(1.5).forceNoCrit(true).ignoreDodge(true);
        SetAbilityService.applyOutgoingModifiers(f, enemy, state, mods);
        assertTrue("Скилл x1.5 + DRAGON_FURY x1.2 = x1.8 ATK",
                Math.abs(mods.getAttackMultiplier() - 1.8) < 0.01);
    }

    private static Equipment equipSet(ItemSet set, int count) {
        Equipment eq = new Equipment();
        Equipment.Slot[] slots = Equipment.Slot.values();
        Rarity rarity = set.isRaritySet() ? set.getCountRarity() : Rarity.EPIC;
        String keyword = switch (set) {
            case DRAGON -> "дракон";
            case SHADOW -> "тень";
            case STORM -> "буря";
            case ABYSS -> "бездна";
            case CELESTIAL -> "звезда";
            case PHOENIX -> "феникс";
            case TITAN -> "титан";
            case CHAOS -> "хаос";
            default -> "предмет";
        };
        for (int i = 0; i < count && i < slots.length; i++) {
            eq.equip(slots[i], new Armor("Клинок " + keyword, rarity, slots[i], 0, 0, 0, 0, 0, 0));
        }
        return eq;
    }

    private static Stats stats(int hp, int atk, int def) {
        return new Stats(hp, atk, def, 0, 1.5, 0);
    }

    private static Fighter fighter(Equipment eq, Stats stats) {
        return new Fighter() {
            @Override
            public String getCombatName() {
                return "Тест";
            }

            @Override
            public Stats getStats() {
                return stats;
            }

            @Override
            public Equipment getEquipment() {
                return eq;
            }

            @Override
            public boolean isAlive() {
                return stats.isAlive();
            }

            @Override
            public boolean isHero() {
                return true;
            }
        };
    }

    private static Enemy enemy(String name, int hp, int atk, int def) {
        Enemy e = new Enemy(EnemyType.GOBLIN, "test", 1);
        Stats s = e.getStats();
        s.setMaxHp(hp);
        s.setCurrentHp(hp);
        s.setAttack(atk);
        s.setDefense(def);
        s.setDodgeChance(0);
        s.setCritChance(0);
        return e;
    }

    private static void assertTrue(String label, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  OK  " + label);
        } else {
            failed++;
            System.out.println("  FAIL " + label);
        }
    }
}
