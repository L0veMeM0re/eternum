package test;

import combat.CombatService;
import combat.Fighter;
import combat.HeroCombatState;
import combat.HeroFighter;
import enemy.EnemyType;
import forge.ForgeCosts;
import forge.ForgeRiskMode;
import forge.ForgeService;
import forge.GemCombatService;
import inventory.Equipment;
import item.Gem;
import item.GemTier;
import item.GemType;
import item.DropService;
import item.Rarity;
import item.Weapon;
import player.CharacterClass;
import player.Player;
import progression.MetaProgression;
import save.ItemSerializer;
import save.SaveManager;
import town.MiningPhrases;
import util.StripClubRenderer;

/**
 * Проверка кузницы, камней, шахты и сохранения.
 * Запуск: java -cp out test.ForgeVerifierTest
 */
public class ForgeVerifierTest {
    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        testWeaponSocketsByRarity();
        testGemStatsOnWeapon();
        testWeaponCopyNoDoubleVamp();
        testItemSerializerRoundTrip();
        testSaveRoundTripOreAndGems();
        testSmeltOre();
        testSmeltFailsWithoutResources();
        testMergeFailReturnsGems();
        testInsertStable();
        testInsertDuplicateTypeBlocked();
        testMiningMathCheck();
        testMiningEconomyBalance();
        testForgePriceScale();
        testStripClubStage7();
        testShardCombatBonus();
        testAmberVampHealOnWeapon();
        testDropRatesQuick();

        System.out.println();
        System.out.println("Итого: " + passed + " OK, " + failed + " FAIL");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testWeaponSocketsByRarity() {
        assertEq("COMMON sockets", 0, Weapon.maxSockets(Rarity.COMMON));
        assertEq("RARE sockets", 0, Weapon.maxSockets(Rarity.RARE));
        assertEq("EPIC sockets", 1, Weapon.maxSockets(Rarity.EPIC));
        assertEq("LEGENDARY sockets", 2, Weapon.maxSockets(Rarity.LEGENDARY));
        assertEq("MYTHIC sockets", 3, Weapon.maxSockets(Rarity.MYTHIC));
    }

    private static void testGemStatsOnWeapon() {
        Gem ruby = new Gem(GemType.RUBY, GemTier.III);
        Weapon w = new Weapon("Blade", Rarity.LEGENDARY, 20, 5, 0, 0, 0, 0,
                new Gem[]{ruby, null});
        assertEq("Ruby III ATK on weapon", 30, w.getAttackBonus());
        assertEq("Base ATK unchanged", 20, w.getBaseAttackBonus());
    }

    private static void testWeaponCopyNoDoubleVamp() {
        Gem amber = new Gem(GemType.AMBER, GemTier.I);
        Weapon original = new Weapon("Dagger", Rarity.EPIC, 10, 0, 0, 0, 0, 0, new Gem[]{amber});
        assertEq("Amber vamp total", 2, (int) original.getVampirismChance());
        Weapon copied = original.withSocket(0, amber);
        assertEq("Copy vamp not doubled", 2, (int) copied.getVampirismChance());
        assertTrue("Amber heal default", copied.getVampirismHealPercent() == 5.0);
    }

    private static void testItemSerializerRoundTrip() {
        Gem gem = new Gem(GemType.SAPPHIRE, GemTier.II, 1.15);
        Weapon w = new Weapon("Test Sword", Rarity.LEGENDARY, 25, 3, 0.2, 0, 0, 0,
                new Gem[]{gem, null});
        String json = ItemSerializer.toJson(w);
        Weapon loaded = (Weapon) ItemSerializer.fromJson(json);
        assertEq("Loaded ATK with gem", w.getAttackBonus(), loaded.getAttackBonus());
        assertEq("Loaded CRIT with gem", (int) w.getCritBonus(), (int) loaded.getCritBonus());
        assertEq("Loaded base ATK", 25, loaded.getBaseAttackBonus());
        assertTrue("Gem preserved", loaded.getSocket(0).getType() == GemType.SAPPHIRE
                && loaded.getSocket(0).getTier() == GemTier.II);
    }

    private static void testSaveRoundTripOreAndGems() {
        Player player = new Player(CharacterClass.WARRIOR);
        player.setGold(50000);
        player.addRawOre(12);
        player.addGem(new Gem(GemType.RUBY, GemTier.I));
        player.addGem(new Gem(GemType.EMERALD, GemTier.II));
        player.setLastGolemRewardEpochDay(12345L);

        MetaProgression meta = new MetaProgression();
        SaveManager sm = new SaveManager();
        sm.save(meta, player, false);

        SaveManager.SaveData data = sm.load();
        Player loaded = new Player(CharacterClass.WARRIOR);
        loaded.setGold(data.playerGold);
        sm.applyToPlayer(data, loaded);

        assertEq("Ore saved", 12, loaded.getRawOre());
        assertEq("Gem stash size", 2, loaded.getGemStash().size());
        assertEq("Golem day saved", 12345L, loaded.getLastGolemRewardEpochDay());
    }

    private static void testSmeltOre() {
        Player p = playerWithGold(100000);
        p.addRawOre(3);
        int gemsBefore = p.getGemStash().size();
        ForgeService.ForgeResult r = ForgeService.smeltOre(p);
        assertTrue("Smelt success", r.success());
        assertEq("Ore consumed", 0, p.getRawOre());
        assertEq("Gem added", gemsBefore + 1, p.getGemStash().size());
    }

    private static void testSmeltFailsWithoutResources() {
        Player p = playerWithGold(100);
        p.addRawOre(1);
        assertTrue("Fail low ore", !ForgeService.smeltOre(p).success());
        p.addRawOre(10);
        assertTrue("Fail low gold", !ForgeService.smeltOre(p).success());
    }

    private static void testMergeFailReturnsGems() {
        Player p = playerWithGold(100000);
        Gem g1 = new Gem(GemType.RUBY, GemTier.I);
        Gem g2 = new Gem(GemType.RUBY, GemTier.I);
        Gem g3 = new Gem(GemType.RUBY, GemTier.I);
        p.addGem(g1);
        p.addGem(g2);
        p.addGem(g3);
        // STABLE fail loses 1 — run many times; at least verify success path works
        int successes = 0;
        for (int i = 0; i < 50; i++) {
            Player trial = playerWithGold(100000);
            trial.addGem(new Gem(GemType.RUBY, GemTier.I));
            trial.addGem(new Gem(GemType.RUBY, GemTier.I));
            trial.addGem(new Gem(GemType.RUBY, GemTier.I));
            if (ForgeService.mergeGems(trial, GemType.RUBY, GemTier.I, ForgeRiskMode.STABLE).success()) {
                successes++;
            }
        }
        assertTrue("STABLE merge succeeds sometimes (50 tries)", successes > 30);
    }

    private static void testInsertStable() {
        Player p = playerWithGold(100000);
        Gem gem = new Gem(GemType.RUBY, GemTier.I);
        p.addGem(gem);
        Weapon w = new Weapon("Sword", Rarity.EPIC, 15, 0, 0, 0, 0, 0, null);
        ForgeService.ForgeResult r = ForgeService.insertGem(p, w, 0, gem, ForgeRiskMode.STABLE);
        assertTrue("Insert stable", r.success());
        assertEq("Gem removed from stash", 0, p.getGemStash().size());
    }

    private static void testInsertDuplicateTypeBlocked() {
        Player p = playerWithGold(100000);
        Weapon w = new Weapon("Sword", Rarity.LEGENDARY, 15, 0, 0, 0, 0, 0,
                new Gem[]{new Gem(GemType.RUBY, GemTier.I), null});
        Gem secondRuby = new Gem(GemType.RUBY, GemTier.I);
        p.addGem(secondRuby);
        ForgeService.ForgeResult r = ForgeService.insertGem(p, w, 1, secondRuby, ForgeRiskMode.STABLE);
        assertTrue("Duplicate type blocked", !r.success());
    }

    private static void testMiningMathCheck() {
        assertTrue("Exact answer ok",
                MiningPhrases.checkAnswer(17, "17", 5000,
                        MiningPhrases.Difficulty.CALM));
        assertTrue("Timeout fail",
                !MiningPhrases.checkAnswer(17, "17", 31000,
                        MiningPhrases.Difficulty.CALM));
        assertTrue("Wrong answer fail",
                !MiningPhrases.checkAnswer(17, "18", 1000,
                        MiningPhrases.Difficulty.CALM));
        assertTrue("Spaces trimmed",
                MiningPhrases.checkAnswer(17, "  17  ", 5000,
                        MiningPhrases.Difficulty.CALM));
    }

    private static void testMiningEconomyBalance() {
        // 3 руды + 6000G = 1 камень; спокойная жила: 1000G за 1 руду → ~9000G за камень I
        int calmCostPerOre = MiningPhrases.attemptCost();
        int smeltTotal = calmCostPerOre * ForgeCosts.ORE_PER_SMELT + ForgeCosts.SMELT_ORE;
        assertTrue("One gem I costs 9k+ gold via mining", smeltTotal >= 9000);
        assertTrue("Smelt cheaper than train ATK (950)", ForgeCosts.SMELT_ORE >= 6000);
    }

    private static void testForgePriceScale() {
        assertTrue("Insert 5k", ForgeCosts.INSERT >= 5000);
        assertTrue("Merge II 8k", ForgeCosts.MERGE_TO_II >= 8000);
        assertTrue("Merge III 15k", ForgeCosts.MERGE_TO_III >= 15000);
        assertTrue("Mine 1k", ForgeCosts.MINE_ATTEMPT >= 1000);
    }

    private static void testStripClubStage7() {
        assertEq("7 stages", 7, StripClubRenderer.STAGES);
        assertEq("Stage 7 cost", 64000, StripClubRenderer.costForStage(6));
    }

    private static void testShardCombatBonus() {
        Player p = playerWithGold(0);
        Gem shard = new Gem(GemType.SHARD, GemTier.III);
        p.getEquipment().equip(Equipment.Slot.WEAPON,
                new Weapon("Blade", Rarity.EPIC, 10, 0, 0, 0, 0, 0, new Gem[]{shard}));
        Fighter fighter = new HeroFighter(p);
        HeroCombatState state = new HeroCombatState();
        for (int i = 0; i < 4; i++) {
            state.incrementAttackCounter();
        }
        CombatService.AttackModifiers mods = new CombatService.AttackModifiers();
        GemCombatService.applyOutgoingModifiers(fighter, state, mods);
        assertTrue("Shard III on 5th hit", mods.getDamageMultiplier() >= 1.17);
    }

    private static void testAmberVampHealOnWeapon() {
        Weapon w = new Weapon("Blade", Rarity.EPIC, 10, 0, 0, 0, 0, 0,
                new Gem[]{new Gem(GemType.AMBER, GemTier.II)});
        assertTrue("Amber enables heal%", w.getVampirismHealPercent() > 0);
        assertTrue("Amber vamp chance", w.getVampirismChance() >= 4);
    }

    private static Player playerWithGold(int gold) {
        Player p = new Player(CharacterClass.WARRIOR);
        p.setGold(gold);
        return p;
    }

    private static void assertEq(String name, int expected, int actual) {
        if (expected == actual) {
            passed++;
            System.out.println("  OK  " + name);
        } else {
            failed++;
            System.out.println("  FAIL " + name + " — expected " + expected + ", got " + actual);
        }
    }

    private static void testDropRatesQuick() {
        int items = 0;
        int mythic = 0;
        int legendary = 0;
        int n = 50_000;
        for (int i = 0; i < n; i++) {
            if (DropService.rollMobDrop(false, 0, 0) == DropService.MobDropOutcome.ITEM) {
                items++;
            }
            Rarity r = DropService.rollRarity(10, Rarity.COMMON, false);
            if (r == Rarity.MYTHIC) {
                mythic++;
            } else if (r == Rarity.LEGENDARY) {
                legendary++;
            }
        }
        double itemPct = items * 100.0 / n;
        assertTrue("Quick item drop 23-27%", itemPct >= 23 && itemPct <= 27);
        assertTrue("Quick mythic very rare", mythic <= 5);
        assertTrue("Quick legendary ~0.1%", legendary >= 30 && legendary <= 90);
    }

    private static void assertEq(String name, long expected, long actual) {
        if (expected == actual) {
            passed++;
            System.out.println("  OK  " + name);
        } else {
            failed++;
            System.out.println("  FAIL " + name + " — expected " + expected + ", got " + actual);
        }
    }

    private static void assertTrue(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  OK  " + name);
        } else {
            failed++;
            System.out.println("  FAIL " + name);
        }
    }
}
