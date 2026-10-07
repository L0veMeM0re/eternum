package test;

import enemy.BossType;
import enemy.EnemyType;
import inventory.Equipment;
import item.Armor;
import item.DropService;
import item.Rarity;
import item.Weapon;
import player.CharacterClass;
import player.Player;
import progression.MagicShop;
import progression.MetaProgression;
import progression.RovingMerchant;
import quest.QuestId;
import quest.QuestLog;
import quest.QuestService;
import game.GameSettings;
import save.SaveManager;
import town.GamblingOdds;
import town.GladiatorArena;
import town.GladiatorGenerator;
import town.GladiatorMatch;

/**
 * Полное прохождение цепочки квестов деревни.
 * Запуск: {@code java -cp out test.QuestVerifierTest}
 */
public class QuestVerifierTest {
    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        testQuest1GoblinHuntFull();
        testQuest1IgnoresWrongKills();
        testQuest2MilitiaGearFull();
        testQuest2RequiresUnlock();
        testQuest3NightPackFull();
        testQuest3IgnoresKillsBeforeAccept();
        testQuest4BitterPotionFull();
        testQuest4RequiresHealerUnlock();
        testFullChainInOrder();
        testChainSaveAndContinue();
        testAllCompletedPersistAfterSave();

        testTowerArcFloor20Full();
        testTowerArcCaveBonesFull();
        testTowerArcSwampWhisperFull();
        testTowerArcMistSealFull();
        testTowerArcSaveRoundTrip();

        testMineCampUnlock();
        testMineShiftFull();
        testGolemGuardFull();
        testThreeSparksFull();
        testSoulSocketFull();
        testMineArcSaveRoundTrip();

        testRoadArcBrokenWagonFull();
        testRoadArcRaidersFull();
        testRoadArcStolenCargoFull();
        testRoadArcBossFloor20Full();
        testRoadArcSaveRoundTrip();

        testPrologueSettingsPersist();

        testMagicScrollsApplyAndSave();

        testGamblingOddsBalance();

        testRovingMerchantRefreshAndBuy();

        System.out.println();
        System.out.println("Итого: " + passed + " OK, " + failed + " FAIL");
        if (failed > 0) {
            System.exit(1);
        }
    }

    /** Квест 1: принять → 10 гоблинов → сдать → золото + Илья. */
    private static void testQuest1GoblinHuntFull() {
        Player p = newPlayer();
        QuestLog log = p.getQuestLog();

        assertTrue("Q1 not accepted initially", !log.isGoblinHuntAccepted());
        log.acceptGoblinHunt();
        assertTrue("Q1 accepted", log.isGoblinHuntAccepted());

        for (int i = 0; i < QuestLog.GOBLIN_TARGET; i++) {
            QuestService.onEnemyKilled(p, EnemyType.GOBLIN);
        }
        assertTrue("Q1 ready", log.isGoblinHuntReady());
        assertTrue("Militia locked before turn-in", !log.isMilitiaUnlocked());

        QuestService.completeGoblinHunt(p);
        assertTrue("Q1 completed", log.isCompleted(QuestId.GOBLIN_HUNT));
        assertTrue("Militia unlocked", log.isMilitiaUnlocked());
        assertEq("Q1 gold", QuestService.GOBLIN_REWARD_GOLD, p.getGold());
    }

    /** Другие мобы не считаются для Q1. */
    private static void testQuest1IgnoresWrongKills() {
        Player p = newPlayer();
        p.getQuestLog().acceptGoblinHunt();
        QuestService.onEnemyKilled(p, EnemyType.WOLF);
        QuestService.onEnemyKilled(p, EnemyType.BAT);
        QuestService.onEnemyKilled(p, EnemyType.SKELETON);
        assertEq("Only goblins count", 0, p.getQuestLog().getGoblinKills());
    }

    /** Квест 2: 5 оружия + 5 брони → золото + камень I. */
    private static void testQuest2MilitiaGearFull() {
        Player p = newPlayer();
        QuestLog log = p.getQuestLog();
        log.unlockMilitia();
        log.acceptMilitiaQuest();

        fillInventoryWithGear(p, QuestLog.WEAPONS_TARGET, QuestLog.ARMOR_TARGET);

        for (int i = 0; i < QuestLog.WEAPONS_TARGET; i++) {
            assertTrue("Weapon removed", p.getInventory().remove(findWeaponSlot(p)) != null);
            log.deliverWeapon();
        }
        for (int i = 0; i < QuestLog.ARMOR_TARGET; i++) {
            assertTrue("Armor removed", p.getInventory().remove(findArmorSlot(p)) != null);
            log.deliverArmor();
        }

        assertTrue("Q2 ready", log.isMilitiaGearReady());
        int gemsBefore = p.getGemStash().size();
        QuestService.completeMilitiaGear(p);

        assertTrue("Q2 completed", log.isCompleted(QuestId.MILITIA_GEAR));
        assertEq("Q2 gold", QuestService.MILITIA_REWARD_GOLD, p.getGold());
        assertEq("Q2 gem", gemsBefore + 1, p.getGemStash().size());
        assertTrue("Q2 bonus [Синий] gear", hasGearWithRarity(p, Rarity.RARE));
    }

    /** Квест 2: без принятия сдача не идёт. */
    private static void testQuest2RequiresUnlock() {
        Player p = newPlayer();
        p.getQuestLog().unlockMilitia();
        p.getQuestLog().deliverWeapon();
        assertEq("No delivery without accept", 0, p.getQuestLog().getWeaponsDelivered());
    }

    /** Квест 3: 8 волков + 8 мышей → золото + руда + Агата. */
    private static void testQuest3NightPackFull() {
        Player p = newPlayer();
        QuestLog log = p.getQuestLog();
        log.markCompleted(QuestId.MILITIA_GEAR);
        log.acceptNightPack();

        for (int i = 0; i < QuestLog.WOLF_TARGET; i++) {
            QuestService.onEnemyKilled(p, EnemyType.WOLF);
        }
        for (int i = 0; i < QuestLog.BAT_TARGET; i++) {
            QuestService.onEnemyKilled(p, EnemyType.BAT);
        }

        assertTrue("Q3 ready", log.isNightPackReady());
        assertTrue("Healer locked before turn-in", !log.isHealerUnlocked());

        QuestService.completeNightPack(p);
        assertTrue("Q3 completed", log.isCompleted(QuestId.NIGHT_PACK));
        assertTrue("Healer unlocked", log.isHealerUnlocked());
        assertEq("Q3 gold", QuestService.NIGHT_PACK_REWARD_GOLD, p.getGold());
        assertEq("Q3 ore", QuestService.NIGHT_PACK_REWARD_ORE, p.getRawOre());
    }

    /** Волки/мыши не считаются до принятия Q3. */
    private static void testQuest3IgnoresKillsBeforeAccept() {
        Player p = newPlayer();
        QuestService.onEnemyKilled(p, EnemyType.WOLF);
        QuestService.onEnemyKilled(p, EnemyType.BAT);
        assertEq("Wolves before accept", 0, p.getQuestLog().getWolfKills());
        assertEq("Bats before accept", 0, p.getQuestLog().getBatKills());
    }

    /** Квест 4: 20 руды → золото + камень II. */
    private static void testQuest4BitterPotionFull() {
        Player p = newPlayer();
        QuestLog log = p.getQuestLog();
        log.unlockHealer();
        log.acceptBitterPotion();
        p.setRawOre(QuestLog.ORE_TARGET - 1);
        assertTrue("Not ready with 19 ore", !log.isBitterPotionReady(p.getRawOre()));

        p.setRawOre(QuestLog.ORE_TARGET);
        assertTrue("Q4 ready", log.isBitterPotionReady(p.getRawOre()));

        int gemsBefore = p.getGemStash().size();
        QuestService.completeBitterPotion(p);

        assertTrue("Q4 completed", log.isCompleted(QuestId.BITTER_POTION));
        assertEq("Ore spent", 0, p.getRawOre());
        assertEq("Q4 gold", QuestService.BITTER_POTION_REWARD_GOLD, p.getGold());
        assertEq("Q4 gem II", gemsBefore + 1, p.getGemStash().size());
    }

    /** Квест 4: без Агаты (healerUnlocked) не принимается осмысленно — руда не списывается без accept. */
    private static void testQuest4RequiresHealerUnlock() {
        Player p = newPlayer();
        p.setRawOre(100);
        assertTrue("Not accepted", !p.getQuestLog().isBitterPotionAccepted());
        QuestService.completeBitterPotion(p);
        assertEq("Ore unchanged without quest", 100, p.getRawOre());
        assertTrue("Q4 not done", !p.getQuestLog().isCompleted(QuestId.BITTER_POTION));
    }

    /** Вся цепочка 1→2→3→4 подряд на одном персонаже. */
    private static void testFullChainInOrder() {
        Player p = newPlayer();
        QuestLog log = p.getQuestLog();

        // 1
        log.acceptGoblinHunt();
        for (int i = 0; i < QuestLog.GOBLIN_TARGET; i++) {
            QuestService.onEnemyKilled(p, EnemyType.GOBLIN);
        }
        QuestService.completeGoblinHunt(p);
        assertTrue("Chain: militia open", log.isMilitiaUnlocked());

        // 2
        log.acceptMilitiaQuest();
        fillInventoryWithGear(p, QuestLog.WEAPONS_TARGET, QuestLog.ARMOR_TARGET);
        for (int i = 0; i < QuestLog.WEAPONS_TARGET; i++) {
            p.getInventory().remove(findWeaponSlot(p));
            log.deliverWeapon();
        }
        for (int i = 0; i < QuestLog.ARMOR_TARGET; i++) {
            p.getInventory().remove(findArmorSlot(p));
            log.deliverArmor();
        }
        QuestService.completeMilitiaGear(p);
        assertTrue("Chain: Q2 done", log.isCompleted(QuestId.MILITIA_GEAR));

        // 3
        log.acceptNightPack();
        for (int i = 0; i < QuestLog.WOLF_TARGET; i++) {
            QuestService.onEnemyKilled(p, EnemyType.WOLF);
        }
        for (int i = 0; i < QuestLog.BAT_TARGET; i++) {
            QuestService.onEnemyKilled(p, EnemyType.BAT);
        }
        QuestService.completeNightPack(p);
        assertTrue("Chain: healer open", log.isHealerUnlocked());

        // 4
        log.acceptBitterPotion();
        p.setRawOre(QuestLog.ORE_TARGET);
        QuestService.completeBitterPotion(p);

        assertTrue("Chain: all 4 done",
                log.isCompleted(QuestId.GOBLIN_HUNT)
                        && log.isCompleted(QuestId.MILITIA_GEAR)
                        && log.isCompleted(QuestId.NIGHT_PACK)
                        && log.isCompleted(QuestId.BITTER_POTION));
        assertTrue("Chain: journal shows completed",
                QuestService.formatJournalLine(log).contains("✓"));
    }

    /** Сохранение посередине цепочки и продолжение после загрузки. */
    private static void testChainSaveAndContinue() {
        Player player = newPlayer();
        player.getQuestLog().acceptGoblinHunt();
        for (int i = 0; i < QuestLog.GOBLIN_TARGET; i++) {
            QuestService.onEnemyKilled(player, EnemyType.GOBLIN);
        }
        QuestService.completeGoblinHunt(player);
        player.getQuestLog().acceptMilitiaQuest();
        player.getQuestLog().setWeaponsDelivered(3);
        player.getQuestLog().setArmorDelivered(2);

        SaveManager sm = new SaveManager();
        sm.save(new MetaProgression(), player, false);
        SaveManager.SaveData data = sm.load();
        Player loaded = new Player(CharacterClass.WARRIOR);
        sm.applyToPlayer(data, loaded);
        QuestLog log = loaded.getQuestLog();

        assertTrue("Reload: militia unlocked", log.isMilitiaUnlocked());
        assertEq("Reload: weapons progress", 3, log.getWeaponsDelivered());
        assertEq("Reload: armor progress", 2, log.getArmorDelivered());

        fillInventoryWithGear(loaded, 2, 3);
        for (int i = log.getWeaponsDelivered(); i < QuestLog.WEAPONS_TARGET; i++) {
            loaded.getInventory().remove(findWeaponSlot(loaded));
            log.deliverWeapon();
        }
        for (int i = log.getArmorDelivered(); i < QuestLog.ARMOR_TARGET; i++) {
            loaded.getInventory().remove(findArmorSlot(loaded));
            log.deliverArmor();
        }
        QuestService.completeMilitiaGear(loaded);
        assertTrue("Reload: Q2 finished", log.isCompleted(QuestId.MILITIA_GEAR));
    }

    /** Все 4 квеста выполнены — сохраняются после перезагрузки. */
    private static void testAllCompletedPersistAfterSave() {
        Player player = newPlayer();
        QuestLog log = player.getQuestLog();
        for (QuestId id : QuestId.values()) {
            log.markCompleted(id);
        }
        log.unlockMilitia();
        log.unlockHealer();
        log.unlockHermit();
        log.unlockForgeMaster();
        player.setExtraGemStashSlots(QuestService.SOUL_SOCKET_EXTRA_STASH);

        SaveManager sm = new SaveManager();
        sm.save(new MetaProgression(), player, false);
        Player loaded = new Player(CharacterClass.WARRIOR);
        sm.applyToPlayer(sm.load(), loaded);
        QuestLog reloaded = loaded.getQuestLog();

        for (QuestId id : QuestId.values()) {
            assertTrue("Persist " + id.name(), reloaded.isCompleted(id));
        }
        assertTrue("Persist militia flag", reloaded.isMilitiaUnlocked());
        assertTrue("Persist healer flag", reloaded.isHealerUnlocked());
        assertTrue("Persist hermit flag", reloaded.isHermitUnlocked());
        assertTrue("Persist forge master flag", reloaded.isForgeMasterUnlocked());
        assertEq("Persist extra gem stash", QuestService.SOUL_SOCKET_EXTRA_STASH, loaded.getExtraGemStashSlots());
    }

    private static void completeVillageChain(Player p) {
        QuestLog log = p.getQuestLog();
        log.markCompleted(QuestId.GOBLIN_HUNT);
        log.markCompleted(QuestId.MILITIA_GEAR);
        log.markCompleted(QuestId.NIGHT_PACK);
        log.markCompleted(QuestId.BITTER_POTION);
        log.unlockMilitia();
        log.unlockHealer();
    }

    /** Q5: этаж 20 за один забег. */
    private static void testTowerArcFloor20Full() {
        Player p = newPlayer();
        completeVillageChain(p);
        QuestLog log = p.getQuestLog();
        log.acceptFloor20Report();
        assertTrue("Not ready before run", !log.isFloor20ReportReady());

        QuestService.onRunEnded(p, 19);
        assertTrue("Floor 19 not enough", !log.isFloor20ReachedInRun());

        QuestService.onRunEnded(p, 20);
        assertTrue("Floor 20 reached flag", log.isFloor20ReachedInRun());
        assertTrue("Q5 ready", log.isFloor20ReportReady());

        QuestService.completeFloor20Report(p);
        assertTrue("Q5 completed", log.isCompleted(QuestId.FLOOR_20_REPORT));
        assertEq("Q5 gold", QuestService.FLOOR_20_REWARD_GOLD, p.getGold());
    }

    /** Q6: 10 скелетов → отшельник. */
    private static void testTowerArcCaveBonesFull() {
        Player p = newPlayer();
        completeVillageChain(p);
        QuestLog log = p.getQuestLog();
        log.markCompleted(QuestId.FLOOR_20_REPORT);
        log.acceptCaveBones();

        for (int i = 0; i < QuestLog.SKELETON_TARGET; i++) {
            QuestService.onEnemyKilled(p, EnemyType.SKELETON);
        }
        assertTrue("Q6 ready", log.isCaveBonesReady());
        assertTrue("Hermit locked", !log.isHermitUnlocked());

        QuestService.completeCaveBones(p);
        assertTrue("Q6 completed", log.isCompleted(QuestId.CAVE_BONES));
        assertTrue("Hermit unlocked", log.isHermitUnlocked());
        assertEq("Q6 gold", QuestService.CAVE_BONES_REWARD_GOLD, p.getGold());
        assertEq("Q6 ore", QuestService.CAVE_BONES_REWARD_ORE, p.getRawOre());
        assertTrue("Q6 bonus [Синий] gear", hasGearWithRarity(p, Rarity.RARE));
    }

    /** Q7: 6 ведьм. */
    private static void testTowerArcSwampWhisperFull() {
        Player p = newPlayer();
        completeVillageChain(p);
        QuestLog log = p.getQuestLog();
        log.unlockHermit();
        log.acceptSwampWhisper();

        for (int i = 0; i < QuestLog.WITCH_TARGET; i++) {
            QuestService.onEnemyKilled(p, EnemyType.WITCH);
        }
        assertTrue("Q7 ready", log.isSwampWhisperReady());

        int gemsBefore = p.getGemStash().size();
        QuestService.completeSwampWhisper(p);
        assertTrue("Q7 completed", log.isCompleted(QuestId.SWAMP_WHISPER));
        assertEq("Q7 gold", QuestService.SWAMP_WHISPER_REWARD_GOLD, p.getGold());
        assertEq("Q7 gem III", gemsBefore + 1, p.getGemStash().size());
    }

    /** Q8: печать тумана — кристалл душ + свиток. */
    private static void testTowerArcMistSealFull() {
        Player p = newPlayer();
        MetaProgression meta = new MetaProgression();
        QuestLog log = p.getQuestLog();
        log.markCompleted(QuestId.SWAMP_WHISPER);
        log.acceptMistSeal();

        p.getInventory().add(new Weapon("Epic Blade", Rarity.EPIC, 20, 0));
        assertTrue("Epic in bag", findWeaponSlot(p) >= 0);
        p.getInventory().remove(findWeaponSlot(p));

        int crystalsBefore = meta.getSoulCrystals();
        int scrollsBefore = p.getBlessingScrolls();
        QuestService.completeMistSeal(meta, p);

        assertTrue("Q8 completed", log.isCompleted(QuestId.MIST_SEAL));
        assertEq("Q8 soul crystals", crystalsBefore + QuestService.MIST_SEAL_SOUL_CRYSTALS,
                meta.getSoulCrystals());
        assertEq("Q8 blessing scroll", scrollsBefore + 1, p.getBlessingScrolls());
    }

    /** Сохранение прогресса башенной цепочки. */
    private static void testTowerArcSaveRoundTrip() {
        Player player = newPlayer();
        completeVillageChain(player);
        QuestLog log = player.getQuestLog();
        log.acceptFloor20Report();
        log.setFloor20ReachedInRun(true);
        log.acceptCaveBones();
        log.setSkeletonKills(4);

        SaveManager sm = new SaveManager();
        sm.save(new MetaProgression(), player, false);
        Player loaded = new Player(CharacterClass.WARRIOR);
        sm.applyToPlayer(sm.load(), loaded);
        QuestLog reloaded = loaded.getQuestLog();

        assertTrue("Reload floor20 accepted", reloaded.isFloor20ReportAccepted());
        assertTrue("Reload floor20 reached", reloaded.isFloor20ReachedInRun());
        assertTrue("Reload cave bones accepted", reloaded.isCaveBonesAccepted());
        assertEq("Reload skeleton kills", 4, reloaded.getSkeletonKills());
    }

    /** Шахтёрская застава: уровень 10 или рекорд этажа 5+. */
    private static void testMineCampUnlock() {
        Player p = newPlayer();
        MetaProgression meta = new MetaProgression();
        assertTrue("Locked at start", !QuestService.isMineCampAvailable(p, meta));

        meta.setBestFloor(5);
        assertTrue("Unlocked by floor 5", QuestService.isMineCampAvailable(p, meta));

        meta.setBestFloor(0);
        p.setLevel(10);
        assertTrue("Unlocked by level 10", QuestService.isMineCampAvailable(p, meta));
    }

    /** Q9: 30 руды. */
    private static void testMineShiftFull() {
        Player p = newPlayer();
        QuestLog log = p.getQuestLog();
        log.acceptMineShift();
        p.setRawOre(QuestLog.MINE_ORE_TARGET);
        assertTrue("Q9 ready", log.isMineShiftReady(p.getRawOre()));

        QuestService.completeMineShift(p);
        assertTrue("Q9 completed", log.isCompleted(QuestId.MINE_SHIFT));
        assertEq("Ore spent", 0, p.getRawOre());
        assertEq("Q9 gold", QuestService.MINE_SHIFT_REWARD_GOLD, p.getGold());
    }

    /** Q10: победа над големом. */
    private static void testGolemGuardFull() {
        Player p = newPlayer();
        QuestLog log = p.getQuestLog();
        log.markCompleted(QuestId.MINE_SHIFT);
        log.acceptGolemGuard();

        QuestService.onGolemDefeated(p);
        assertTrue("Q10 ready", log.isGolemGuardReady());
        assertTrue("Forge master locked", !log.isForgeMasterUnlocked());

        QuestService.completeGolemGuard(p);
        assertTrue("Q10 completed", log.isCompleted(QuestId.GOLEM_GUARD));
        assertTrue("Forge master unlocked", log.isForgeMasterUnlocked());
        assertEq("Q10 gold", QuestService.GOLEM_GUARD_REWARD_GOLD, p.getGold());
        assertEq("Q10 ore", QuestService.GOLEM_GUARD_REWARD_ORE, p.getRawOre());
    }

    /** Q11: 3 переплавки. */
    private static void testThreeSparksFull() {
        Player p = newPlayer();
        QuestLog log = p.getQuestLog();
        log.unlockForgeMaster();
        log.acceptThreeSparks();

        for (int i = 0; i < QuestLog.SMELT_TARGET; i++) {
            QuestService.onSuccessfulSmelt(p);
        }
        assertTrue("Q11 ready", log.isThreeSparksReady());

        QuestService.completeThreeSparks(p);
        assertTrue("Q11 completed", log.isCompleted(QuestId.THREE_SPARKS));
        assertEq("Q11 gold", QuestService.THREE_SPARKS_REWARD_GOLD, p.getGold());
    }

    /** Q12: вставка гема + бонус к сундуку. */
    private static void testSoulSocketFull() {
        Player p = newPlayer();
        QuestLog log = p.getQuestLog();
        log.markCompleted(QuestId.THREE_SPARKS);
        log.acceptSoulSocket();

        QuestService.onSuccessfulGemInsert(p);
        assertTrue("Q12 ready", log.isSoulSocketReady());

        int stashBefore = p.getMaxGemStashSize();
        int gemsBefore = p.getGemStash().size();
        QuestService.completeSoulSocket(p);

        assertTrue("Q12 completed", log.isCompleted(QuestId.SOUL_SOCKET));
        assertEq("Extra stash slots", stashBefore + QuestService.SOUL_SOCKET_EXTRA_STASH, p.getMaxGemStashSize());
        assertEq("Q12 gem II", gemsBefore + 1, p.getGemStash().size());
    }

    /** Сохранение шахтёрской цепочки. */
    private static void testMineArcSaveRoundTrip() {
        Player player = newPlayer();
        QuestLog log = player.getQuestLog();
        log.acceptMineShift();
        log.setGolemDefeatedForQuest(true);
        log.setSuccessfulSmelts(2);
        player.setExtraGemStashSlots(1);

        SaveManager sm = new SaveManager();
        sm.save(new MetaProgression(), player, false);
        Player loaded = new Player(CharacterClass.WARRIOR);
        sm.applyToPlayer(sm.load(), loaded);
        QuestLog reloaded = loaded.getQuestLog();

        assertTrue("Reload mine shift", reloaded.isMineShiftAccepted());
        assertTrue("Reload golem defeated", reloaded.isGolemDefeatedForQuest());
        assertEq("Reload smelts", 2, reloaded.getSuccessfulSmelts());
        assertEq("Reload extra stash", 1, loaded.getExtraGemStashSlots());
    }

    private static void completeTowerChain(Player p) {
        QuestLog log = p.getQuestLog();
        for (QuestId id : new QuestId[] {
                QuestId.FLOOR_20_REPORT, QuestId.CAVE_BONES,
                QuestId.SWAMP_WHISPER, QuestId.MIST_SEAL }) {
            log.markCompleted(id);
        }
        log.unlockHermit();
        log.unlockStarosta();
    }

    private static void testRoadArcBrokenWagonFull() {
        Player p = newPlayer();
        completeTowerChain(p);
        QuestLog log = p.getQuestLog();
        log.acceptBrokenWagon();
        p.setRawOre(QuestLog.ROAD_WAGON_ORE);
        QuestService.completeBrokenWagon(p);
        assertTrue("Q13 done", log.isCompleted(QuestId.BROKEN_WAGON));
        assertEq("Q13 gold", QuestService.BROKEN_WAGON_REWARD_GOLD, p.getGold());
    }

    private static void testRoadArcRaidersFull() {
        Player p = newPlayer();
        completeTowerChain(p);
        QuestLog log = p.getQuestLog();
        log.markCompleted(QuestId.BROKEN_WAGON);
        log.acceptRoadRaiders();
        for (int i = 0; i < QuestLog.ROAD_ORC_TARGET; i++) {
            QuestService.onEnemyKilled(p, EnemyType.ORC);
        }
        QuestService.completeRoadRaiders(p);
        assertTrue("Q14 done", log.isCompleted(QuestId.ROAD_RAIDERS));
        assertEq("Q14 gold", QuestService.ROAD_RAIDERS_REWARD_GOLD, p.getGold());
        assertTrue("Q14 bonus [Фиолетовый] gear", hasGearWithRarity(p, Rarity.EPIC));
    }

    private static void testRoadArcStolenCargoFull() {
        Player p = newPlayer();
        completeTowerChain(p);
        QuestLog log = p.getQuestLog();
        log.markCompleted(QuestId.ROAD_RAIDERS);
        log.acceptStolenCargo();
        for (int i = 0; i < QuestLog.ROAD_WEAPON_TARGET; i++) {
            log.deliverRoadWeapon();
        }
        int gemsBefore = p.getGemStash().size();
        QuestService.completeStolenCargo(p);
        assertTrue("Q15 done", log.isCompleted(QuestId.STOLEN_CARGO));
        assertEq("Q15 gem", gemsBefore + 1, p.getGemStash().size());
    }

    private static void testRoadArcBossFloor20Full() {
        Player p = newPlayer();
        MetaProgression meta = new MetaProgression();
        QuestLog log = p.getQuestLog();
        log.markCompleted(QuestId.STOLEN_CARGO);
        log.acceptBossFloor20();
        QuestService.onBossDefeated(p, BossType.FLOOR_20);
        assertTrue("Q16 ready", log.isBossFloor20Ready());
        int crystalsBefore = meta.getSoulCrystals();
        QuestService.completeBossFloor20(meta, p);
        assertTrue("Q16 done", log.isCompleted(QuestId.BOSS_FLOOR_20));
        assertEq("Q16 crystals", crystalsBefore + QuestService.BOSS_FLOOR_20_SOUL_CRYSTALS,
                meta.getSoulCrystals());
        assertEq("Shop discount", QuestService.ROAD_SHOP_DISCOUNT_PERCENT, p.getShopDiscountPercent());
    }

    private static void testRoadArcSaveRoundTrip() {
        Player player = newPlayer();
        completeTowerChain(player);
        QuestLog log = player.getQuestLog();
        log.acceptRoadRaiders();
        log.setRoadOrcKills(7);
        player.setShopDiscountPercent(10);

        SaveManager sm = new SaveManager();
        sm.save(new MetaProgression(), player, false);
        Player loaded = new Player(CharacterClass.WARRIOR);
        sm.applyToPlayer(sm.load(), loaded);

        assertTrue("Reload starosta", loaded.getQuestLog().isStarostaUnlocked());
        assertEq("Reload orc kills", 7, loaded.getQuestLog().getRoadOrcKills());
        assertEq("Reload discount", 10, loaded.getShopDiscountPercent());
    }

    private static Player newPlayer() {
        return new Player(CharacterClass.WARRIOR);
    }

    private static void fillInventoryWithGear(Player p, int weapons, int armors) {
        for (int i = 0; i < weapons; i++) {
            p.getInventory().add(new Weapon("Test Sword " + i, Rarity.COMMON, 5, 0));
        }
        for (int i = 0; i < armors; i++) {
            p.getInventory().add(new Armor("Test Chest " + i, Rarity.COMMON,
                    Equipment.Slot.ARMOR, 3, 5));
        }
    }

    private static int findWeaponSlot(Player p) {
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            if (p.getInventory().get(i) instanceof Weapon) {
                return i;
            }
        }
        return -1;
    }

    private static int findArmorSlot(Player p) {
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            var item = p.getInventory().get(i);
            if (item instanceof Armor a && a.getSlot() == Equipment.Slot.ARMOR) {
                return i;
            }
        }
        return -1;
    }

    /** Случайная экипировка из награды квеста (оружие или броня). */
    private static boolean hasGearWithRarity(Player p, Rarity rarity) {
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            var item = p.getInventory().get(i);
            if (item instanceof Weapon w && w.getRarity() == rarity) {
                return true;
            }
            if (item instanceof Armor a && a.getRarity() == rarity) {
                return true;
            }
        }
        return false;
    }

    private static void testGamblingOddsBalance() {
        System.out.println("\n--- Gambling odds ---");
        assertEq("Max bet", 7500, GamblingOdds.MAX_BET);
        assertEq("Min bet", 100, GamblingOdds.MIN_BET);

        int redWins = 0;
        int samples = 1500;
        for (int i = 0; i < samples; i++) {
            GladiatorMatch match = GladiatorGenerator.generate();
            GamblingOdds.OddsPair odds = GamblingOdds.calculate(match.getRed(), match.getBlue());
            assertTrue("Odds in range red", odds.redOdds() >= GamblingOdds.MIN_ODDS
                    && odds.redOdds() <= GamblingOdds.MAX_ODDS);
            assertTrue("Odds in range blue", odds.blueOdds() >= GamblingOdds.MIN_ODDS
                    && odds.blueOdds() <= GamblingOdds.MAX_ODDS);
            assertTrue("House overround", GamblingOdds.impliedOverround(odds.redOdds(), odds.blueOdds()) >= 1.10);
            assertEq("Win % sum", 100, odds.redWinPercent() + odds.blueWinPercent());

            GladiatorArena.Winner winner = GladiatorArena.fight(match.getRed(), match.getBlue(), false);
            if (winner == GladiatorArena.Winner.RED) {
                redWins++;
            }
        }
        int redPct = redWins * 100 / samples;
        assertTrue("Red win ~50% over many fights", redPct >= 42 && redPct <= 58);
        System.out.println("  (sim " + samples + ": red wins " + redPct + "%)");
    }

    private static void testMagicScrollsApplyAndSave() {
        System.out.println("\n--- Magic scrolls ---");
        Player p = newPlayer();
        MetaProgression meta = new MetaProgression();
        p.setGold(50_000);
        MagicShop shop = new MagicShop();

        assertTrue("Buy luck", shop.buyLuckScroll(p));
        assertTrue("Buy greed", shop.buyGreedScroll(p));
        assertTrue("Buy fog", shop.buyFogScroll(p));
        assertTrue("Buy second breath", shop.buySecondBreathScroll(p));
        assertEq("Luck stash", 1, p.getLuckScrolls());

        p.applyScrollsForRun();
        assertEq("Luck applied", MagicShop.LUCK_DROP_BONUS_PERCENT, p.getRunDropBonusPercent());
        assertTrue("Greed applied", p.getRunGoldBonus() > 0.24);
        assertTrue("Fog active", p.isRunFogScrollActive());
        assertTrue("Second breath ready", p.isRunSecondBreathActive());
        assertEq("Luck consumed", 0, p.getLuckScrolls());

        assertEq("Drop chance with luck", 35,
                DropService.itemDropChancePercent(0, 0, MagicShop.LUCK_DROP_BONUS_PERCENT));
        assertEq("Gold bonus 100", 125, p.applyGoldBonus(100));

        p.getCombatStats().setCurrentHp(0);
        assertTrue("Second breath revive", p.trySecondBreathRevive(meta));
        assertTrue("Hero alive after revive", p.getCombatStats().isAlive());
        assertTrue("Second breath spent", !p.isRunSecondBreathActive());

        p.setLuckScrolls(2);
        p.setGreedScrolls(1);
        p.setFogScrolls(3);
        p.setSecondBreathScrolls(1);
        SaveManager sm = new SaveManager();
        sm.save(meta, p, false);
        Player loaded = new Player(CharacterClass.WARRIOR);
        sm.applyToPlayer(sm.load(), loaded);
        assertEq("Save luck", 2, loaded.getLuckScrolls());
        assertEq("Save greed", 1, loaded.getGreedScrolls());
        assertEq("Save fog", 3, loaded.getFogScrolls());
        assertEq("Save second breath", 1, loaded.getSecondBreathScrolls());
    }

    private static void testRovingMerchantRefreshAndBuy() {
        System.out.println("\n--- Roving merchant ---");
        Player p = newPlayer();
        p.setGold(50_000);
        RovingMerchant merchant = p.getRovingMerchant();
        merchant.refresh(p);
        assertEq("10 offers", RovingMerchant.OFFER_COUNT, merchant.getOffers().size());

        int idx = -1;
        for (int i = 0; i < merchant.getOffers().size(); i++) {
            if (!merchant.getOffers().get(i).sold && merchant.getOffers().get(i).item != null) {
                idx = i;
                break;
            }
        }
        assertTrue("Has buyable offer", idx >= 0);
        int goldBefore = p.getGold();
        int price = p.getShopPrice(merchant.getOffers().get(idx).basePrice);
        assertTrue("Merchant buy", merchant.buy(p, idx));
        assertEq("Gold spent", goldBefore - price, p.getGold());
        assertTrue("Offer marked sold", merchant.getOffers().get(idx).sold);

        merchant.refresh(p);
        assertEq("Restock after run", RovingMerchant.OFFER_COUNT, merchant.getOffers().size());
        assertTrue("Fresh stock not all sold", !merchant.getOffers().get(0).sold);
    }

    private static void testPrologueSettingsPersist() {
        System.out.println("\n--- Prologue settings ---");
        GameSettings.resetForTest();
        assertTrue("prologueSeen default false", !GameSettings.readPrologueSeenFromDisk());
        GameSettings s = new GameSettings();
        s.setPrologueSeen(true);
        s.save();
        assertTrue("prologueSeen saved", GameSettings.readPrologueSeenFromDisk());
        GameSettings loaded = GameSettings.load();
        assertTrue("prologueSeen loaded", loaded.isPrologueSeen());
        GameSettings.resetForTest();
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
