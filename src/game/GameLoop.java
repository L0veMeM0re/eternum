package game;

import combat.EncounterCombatState;
import combat.Fighter;
import combat.HeroFighter;
import combat.SetAbilityService;
import combat.TeamCombatService;
import enemy.Boss;
import enemy.Enemy;
import enemy.EnemyFactory;
import enemy.EnemyScaling;
import enemy.EnemySquad;
import enemy.SquadFactory;
import enemy.EnemyType;
import item.DropService;
import item.Item;
import item.ItemFactory;
import item.ItemSetAbility;
import item.Rarity;
import party.Party;
import player.Player;
import player.Stats;
import progression.LevelService;
import progression.MagicShop;
import progression.MetaProgression;
import progression.RunEffects;
import quest.QuestService;
import save.SaveManager;
import util.ConsoleColors;
import util.ConsoleInput;
import util.HpBarRenderer;
import util.MobArt;
import util.RandomUtil;
import util.ScreenFrame;

import java.util.List;
import java.util.function.Supplier;

public class GameLoop {
    private static final long TURN_DELAY_MS = 1000;

    private final Player player;
    private final Party party;
    private final MetaProgression meta;
    private final LevelService levelService;
    private final TeamCombatService teamCombat = new TeamCombatService();
    private final SquadFactory squadFactory = new SquadFactory();
    private final EnemyFactory enemyFactory = new EnemyFactory();
    private final SaveManager saveManager;
    private final Runnable onRunEnd;
    private final ConsoleInput consoleInput;
    private final Supplier<Boolean> autoRestartSupplier;
    private final EncounterCombatState encounterState = new EncounterCombatState();

    private int currentFloor = 1;
    private int runPeakFloor = 1;
    private int runKills;
    private int runItems;
    private long runStartTime;
    private volatile boolean running;
    private volatile boolean paused;
    private volatile boolean abortedByUser;
    private boolean lastRunDeath;

    public GameLoop(Player player, MetaProgression meta, LevelService levelService,
                    SaveManager saveManager, ConsoleInput consoleInput, Runnable onRunEnd,
                    Supplier<Boolean> autoRestartSupplier) {
        this.player = player;
        this.party = player.getParty();
        this.meta = meta;
        this.levelService = levelService;
        this.saveManager = saveManager;
        this.consoleInput = consoleInput;
        this.onRunEnd = onRunEnd;
        this.autoRestartSupplier = autoRestartSupplier;
    }

    public boolean wasLastRunDeath() {
        return lastRunDeath;
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        running = false;
    }

    private void persist() {
        saveManager.save(meta, player, autoRestartSupplier.get());
    }

    public void startRun() {
        startRun(1);
    }

    public void startRun(int startFloor) {
        currentFloor = Math.max(1, Math.min(startFloor, EnemyFactory.MAX_FLOOR));
        runPeakFloor = currentFloor;
        runKills = 0;
        runItems = 0;
        runStartTime = System.currentTimeMillis();
        running = true;
        paused = false;
        abortedByUser = false;
        consoleInput.setCombatMode(true);
        player.applyScrollsForRun();
        player.recalculateStats(meta);
        player.resetAbdolbosRunCounter();
        encounterState.resetForRun();
        party.prepareForRun(player, meta);

        printTeamSummary();
        System.out.println("\n===== НОВЫЙ ЗАБЕГ =====");
        System.out.println("Цель: пройти " + EnemyFactory.MAX_FLOOR + " этажей");
        if (player.getRunDamageBonus() > 0) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.GREEN,
                    "Благословение: +" + (int) (player.getRunDamageBonus() * 100) + "% урона на этот забег"));
        }
        if (player.getRunXpBonus() > 0) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.GREEN,
                    "Свиток опыта: +" + (int) (player.getRunXpBonus() * 100) + "% XP на этот забег"));
        }
        if (player.getRunDropBonusPercent() > 0) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.GREEN,
                    "Свиток удачи: +" + player.getRunDropBonusPercent() + "% шанс дропа на этот забег"));
        }
        if (player.getRunGoldBonus() > 0) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.GREEN,
                    "Свиток жадности: +" + (int) (player.getRunGoldBonus() * 100) + "% золота на этот забег"));
        }
        if (player.isRunFogScrollActive()) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.GREEN,
                    "Свиток тумана: этажи 1–" + MagicShop.FOG_SCROLL_FLOORS
                            + " — враги слабее и меньше в пачке"));
        }
        if (player.isRunSecondBreathActive()) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.GREEN,
                    "Свиток второго дыхания: 1 воскрешение отряда с "
                            + MagicShop.SECOND_BREATH_HEAL_PERCENT + "% HP"));
        }
        if (player.getAbdolbosPotions() > 0) {
            System.out.println(ConsoleColors.dim("Зелья Абдолбос в запасе: " + player.getAbdolbosPotions()
                    + " | B — выпить (макс. " + RunEffects.MAX_POTIONS_PER_RUN + " за забег)"));
        }
        if (currentFloor > 1) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_MAGENTA,
                    "Старт с этажа " + currentFloor + " (ключ возвышения)"));
        }
        System.out.println("Этаж " + currentFloor + " | " + enemyFactory.getZoneName(currentFloor));
        System.out.println("Автобой... (Enter — пауза, B — зелье Абдолбос, Q — выход в меню)\n");

        while (running && party.isAnyMemberAlive(player) && currentFloor <= EnemyFactory.MAX_FLOOR) {
            if (processInput(consoleInput.pollLine())) {
                break;
            }
            runCombatTurn();
            if (!running || abortedByUser) {
                break;
            }
            if (!party.isAnyMemberAlive(player)) {
                break;
            }
            sleep(TURN_DELAY_MS);
        }

        if (abortedByUser) {
            finishAbortedRun();
            return;
        }

        if (currentFloor > EnemyFactory.MAX_FLOOR && party.isAnyMemberAlive(player)) {
            completeRun(true);
        } else if (!party.isAnyMemberAlive(player)) {
            completeRun(false);
        }
    }

    private void printTeamSummary() {
        System.out.println("Отряд:");
        System.out.println("  • " + player.getCharacterClass().getDisplayName()
                + " (Герой) | Ур." + player.getLevel());
        for (int i = 0; i < Party.MAX_MERC_SLOTS; i++) {
            var m = party.getMercenary(i);
            if (m != null) {
                System.out.println("  • " + m.getName() + " (" + m.getCharacterClass().getDisplayName()
                        + ") | Ур." + m.getLevel());
            }
        }
    }

    private void finishAbortedRun() {
        QuestService.onRunEnded(player, runPeakFloor);
        player.getRovingMerchant().refresh(player);
        player.finishRunAfterRun(meta);
        party.recalculateAll(player, meta);
        persist();
        System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                "\n===== ЗАБЕГ ПРЕРВАН (этаж " + currentFloor + ") ====="));
        System.out.println(ConsoleColors.dim("Бродячий торговец обновил товар на площади."));
        System.out.println("Прогресс сохранён. Вы в главном меню.\n");
        consoleInput.setCombatMode(false);
        onRunEnd.run();
        abortedByUser = false;
    }

    private void abortRunToMenu() {
        if (!running) {
            return;
        }
        abortedByUser = true;
        running = false;
        paused = false;
    }

    private void runCombatTurn() {
        runPeakFloor = Math.max(runPeakFloor, currentFloor);
        boolean fogActive = player.isFogScrollActiveForFloor(currentFloor);
        EnemySquad squad = squadFactory.createEncounter(currentFloor, fogActive);
        boolean isBoss = squad.isBossFight();

        printEncounterHeader(squad, isBoss, fogActive);
        party.prepareForRun(player, meta);
        encounterState.resetForEncounter();

        List<Fighter> allies = party.getCombatOrder(player);

        while (running && !abortedByUser && squad.isAlive() && party.isAnyMemberAlive(player)) {
            if (processInput(consoleInput.pollLine())) {
                return;
            }
            teamCombat.executeRound(allies, squad, player, encounterState, party);
            if (!squad.isAlive()) {
                handleVictory(squad, isBoss);
                return;
            }
            if (!party.isAnyMemberAlive(player)) {
                if (player.trySecondBreathRevive(meta)) {
                    System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_MAGENTA,
                            "\n  ✦ Свиток второго дыхания — отряд поднимается с "
                                    + MagicShop.SECOND_BREATH_HEAL_PERCENT + "% HP!\n"));
                    continue;
                }
                return;
            }
            sleep(TURN_DELAY_MS);
        }
    }

    private void printEncounterHeader(EnemySquad squad, boolean isBoss, boolean fogActive) {
        Enemy first = squad.getEnemies().get(0);
        if (isBoss) {
            ScreenFrame.combat(currentFloor, enemyFactory.getZoneName(currentFloor), true, first.getName());
        } else {
            ScreenFrame.combat(currentFloor, enemyFactory.getZoneName(currentFloor), false, null);
            if (currentFloor > 10) {
                System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_RED,
                        "  Сложность: " + EnemyScaling.getDifficultyLabel(currentFloor)));
            }
        }
        if (fogActive) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_CYAN,
                    "  ◌ Свиток тумана — ослабленные враги"));
        }
        System.out.println(ConsoleColors.bold("  Противники (" + squad.getEnemies().size() + "):"));
        System.out.println();
        MobArt.printSquadInRow(squad.getEnemies());
        System.out.println();
        for (Enemy e : squad.getEnemies()) {
            String eliteTag = e.isElite()
                    ? ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW, " ★ЭЛИТ ")
                    : " ";
            Stats es = e.getStats();
            System.out.println("    " + e.getName() + eliteTag + ConsoleColors.dim(" | ")
                    + HpBarRenderer.numbersAndBar(es.getCurrentHp(), es.getMaxHp(), HpBarRenderer.Side.ENEMY)
                    + ConsoleColors.dim(" ATK:" + es.getAttack() + " DEF:" + es.getDefense()
                    + " | CRIT:" + (int) es.getCritChance() + "%"));
        }
        System.out.println();
    }

    private void handleVictory(EnemySquad squad, boolean isBoss) {
        int totalXp = 0;
        int totalGold = 0;
        for (Enemy e : squad.getEnemies()) {
            totalXp += e.calculateXpReward();
            totalGold += e.calculateGoldReward();
            runKills++;
            QuestService.onEnemyKilled(player, e.getType());
        }
        meta.addKill();
        meta.addGoldEarned(totalGold);

        int xpWithSet = (int) (totalXp * SetAbilityService.xpMultiplier(new HeroFighter(player)));
        int xpShown = player.applyXpBonus(xpWithSet);
        if (player.getCombatStats().isAlive()) {
            player.addXp(xpWithSet, meta, levelService);
        }
        party.grantXpToMercenaries(xpWithSet, meta, levelService, player.getRunXpBonus());
        totalGold = player.applyGoldBonus(totalGold);
        player.addGold(totalGold);

        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN, "\nПобеда!"));
        System.out.println("  " + ConsoleColors.wrap(ConsoleColors.GREEN, "+" + xpShown + " XP (живым в отряде)"));
        System.out.println("  " + ConsoleColors.wrap(ConsoleColors.YELLOW, "+" + totalGold + " золота"));

        for (Enemy e : squad.getEnemies()) {
            handleDrop(e, isBoss && e instanceof Boss);
        }

        if (isBoss) {
            for (Enemy e : squad.getEnemies()) {
                if (e instanceof Boss boss) {
                    QuestService.onBossDefeated(player, boss.getBossType());
                }
            }
            persist();
            System.out.println("[Сохранение после босса]");
        }

        party.prepareForRun(player, meta);
        applyVitalityHeal();
        applySetFloorHeals();

        if (SetAbilityService.has(new HeroFighter(player), ItemSetAbility.CELESTIAL_AEGIS)
                && currentFloor % 5 == 0) {
            encounterState.grantCelestialAegisCharge();
            System.out.println(ConsoleColors.dim("  ✦ [Звёздный щит] Заряд защиты (+1)"));
        }

        currentFloor++;
        runPeakFloor = Math.max(runPeakFloor, currentFloor);
        if (party.isAnyMemberAlive(player) && currentFloor <= EnemyFactory.MAX_FLOOR) {
            System.out.println("\n>>> Следующий этаж: " + currentFloor
                    + " | " + enemyFactory.getZoneName(currentFloor));
        }
    }

    private void applySetFloorHeals() {
        for (var f : party.getCombatOrder(player)) {
            if (f.isAlive()) {
                SetAbilityService.applyPhoenixEmberHeal(f);
            }
        }
    }

    private void applyVitalityHeal() {
        double healPercent = player.getRunEffects().floorHealPercent();
        if (healPercent <= 0) {
            return;
        }
        int heroHeal = player.getCombatStats().heal(
                Math.max(1, (int) (player.getCombatStats().getMaxHp() * healPercent)));
        if (heroHeal > 0) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                    "  ♥ Живучесть: +" + heroHeal + " HP герою"));
        }
        for (var m : party.getLivingMercenaries()) {
            int heal = m.getStats().heal(
                    Math.max(1, (int) (m.getStats().getMaxHp() * healPercent)));
            if (heal > 0) {
                System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                        "  ♥ Живучесть: +" + heal + " HP (" + m.getName() + ")"));
            }
        }
    }

    private void handleDrop(Enemy enemy, boolean bossDrop) {
        double dropBonus = meta.getDropBonus() + player.getRunDropBonusPercent()
                - player.getRunDropPenalty() * 100;

        if (bossDrop) {
            Rarity minRarity = DropService.rollBossMinRarity(dropBonus);
            giveItem(ItemFactory.generateDrop(currentFloor, minRarity, true));
            return;
        }

        DropService.MobDropOutcome outcome = DropService.rollMobDrop(
                enemy.isElite(), meta.getDropLevel(), player.getRunDropPenalty() * 100,
                player.getRunDropBonusPercent());

        if (outcome == DropService.MobDropOutcome.ITEM) {
            Rarity min = DropService.minRarityForMob(enemy.isElite(), currentFloor);
            giveItem(ItemFactory.generateDrop(currentFloor, min, false));
        } else if (outcome == DropService.MobDropOutcome.BONUS_GOLD) {
            int bonusGold = RandomUtil.range(3, 10);
            if (enemy.isElite()) {
                bonusGold *= 2;
            }
            player.addGold(bonusGold);
            meta.addGoldEarned(bonusGold);
            System.out.println("  +" + bonusGold + " бонусного золота");
        }
    }

    private void giveItem(Item item) {
        Rarity rarity = item.getRarity();
        if (rarity == Rarity.MYTHIC) {
            ConsoleColors.printDropBanner(rarity, "★★★ МИФИЧЕСКИЙ ДРОП ★★★");
        } else if (rarity == Rarity.LEGENDARY) {
            ConsoleColors.printDropBanner(rarity, "★★ ЛЕГЕНДАРНЫЙ ДРОП ★★");
        }

        if (player.tryAutoSell(item)) {
            runItems++;
            meta.addItemFound();
            System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                    "  Автопродажа [" + rarity.getColorName() + "]: "
                            + item.getName() + " → +" + item.getSellPrice() + " G"));
            return;
        }

        if (player.getInventory().add(item)) {
            runItems++;
            meta.addItemFound();
            System.out.println("  Выпал предмет: " + item.getDisplayName()
                    + ConsoleColors.dim(" (" + item.getStatDescription() + ")"));
        } else {
            System.out.println("  " + ConsoleColors.wrap(ConsoleColors.YELLOW, "Инвентарь полон: ")
                    + item.getDisplayName());
        }
    }

    private void completeRun(boolean victory) {
        long runSeconds = (System.currentTimeMillis() - runStartTime) / 1000;
        meta.addPlayTime(runSeconds);

        int previousBest = meta.getBestFloor();
        int reachedFloor = victory ? EnemyFactory.MAX_FLOOR : currentFloor;
        QuestService.onRunEnded(player, runPeakFloor);
        player.getRovingMerchant().refresh(player);
        meta.updateBestFloor(reachedFloor);
        boolean newRecord = reachedFloor > previousBest;

        int soulCrystals = 8 + (reachedFloor * 3) / 4 + runKills / 20;
        if (victory) {
            soulCrystals += 20;
        }
        meta.addSoulCrystals(soulCrystals);

        if (!victory) {
            meta.addDeath();
        }

        lastRunDeath = !victory;
        player.finishRunAfterRun(meta);
        party.prepareForRun(player, meta);
        persist();

        if (victory) {
            System.out.println("\n===== ПОБЕДА! ВСЕ 100 ЭТАЖЕЙ ПРОЙДЕНЫ! =====");
        } else {
            System.out.println("\n===== ЗАБЕГ ОКОНЧЕН (поражение на этаже " + currentFloor + ") =====");
        }
        System.out.println("Достигнут этаж: " + reachedFloor);
        System.out.println("Убийств: " + runKills);
        System.out.println("Предметов: " + runItems);
        System.out.println("Кристаллы душ: +" + soulCrystals);
        System.out.println(ConsoleColors.dim("Бродячий торговец обновил товар на площади."));
        if (newRecord) {
            System.out.println("\nНовый рекорд!");
        }
        System.out.println("========================\n");

        onRunEnd.run();
        consoleInput.setCombatMode(false);

        if (lastRunDeath && autoRestartSupplier.get()) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_CYAN,
                    ">>> Автозапуск нового забега...\n"));
            sleep(1500);
            startRun();
        }
    }

    private boolean processInput(String line) {
        if (line == null) {
            waitWhilePaused();
            return abortedByUser;
        }

        if (ConsoleInput.isQuitCommand(line)) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                    "\n[Выход в меню — сохранение...]"));
            abortRunToMenu();
            return true;
        }

        if ("b".equalsIgnoreCase(line.trim())) {
            drinkAbdolbosPotion();
            waitWhilePaused();
            return abortedByUser;
        }

        if (line.trim().isEmpty()) {
            paused = !paused;
            System.out.println(paused ? "[ПАУЗА — Enter для продолжения, B — зелье, Q — в меню]"
                    : "[ПРОДОЛЖИТЬ]");
        }

        waitWhilePaused();
        return abortedByUser;
    }

    private void waitWhilePaused() {
        while (paused && running && !abortedByUser) {
            if (processInput(consoleInput.pollLine(100))) {
                return;
            }
            sleepQuiet(50);
        }
    }

    private void sleep(long ms) {
        long deadline = System.currentTimeMillis() + ms;
        while (running && !abortedByUser && System.currentTimeMillis() < deadline) {
            String line = consoleInput.pollLine(80);
            if (line != null && processInput(line)) {
                return;
            }
            sleepQuiet(50);
        }
    }

    private void drinkAbdolbosPotion() {
        if (player.getAbdolbosUsedThisRun() >= RunEffects.MAX_POTIONS_PER_RUN) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                    "На этот забег можно выпить не больше " + RunEffects.MAX_POTIONS_PER_RUN
                            + " зелий Абдолбос."));
            return;
        }
        if (player.getAbdolbosPotions() <= 0) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                    "Нет зелий Абдолбос. Купите на площади в магической лавке."));
            return;
        }
        if (!player.spendAbdolbosPotion()) {
            return;
        }
        player.incrementAbdolbosUsedThisRun();
        System.out.println("\n" + player.getRunEffects().usePotion());
        System.out.println(ConsoleColors.dim("Выпито за забег: " + player.getAbdolbosUsedThisRun()
                + "/" + RunEffects.MAX_POTIONS_PER_RUN
                + " | Осталось в запасе: " + player.getAbdolbosPotions() + "\n"));
        player.recalculateStats(meta);
        party.recalculateAll(player, meta);
        persist();
    }

    private void sleepQuiet(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            running = false;
        }
    }
}
