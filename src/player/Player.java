package player;

import combat.CombatService;
import inventory.Equipment;
import inventory.Inventory;
import inventory.SetBonusCalculator;
import item.Gem;
import item.Item;
import item.Rarity;
import party.Mercenary;
import party.Party;
import progression.LevelService;
import progression.MetaProgression;
import progression.RunEffects;
import progression.skill.SkillEffectCalculator;
import progression.skill.SkillTreeProgress;
import progression.skill.SkillTreeService;
import quest.QuestLog;
import util.ConsoleColors;
import util.RandomUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.time.LocalDate;

public class Player {
    private CharacterClass characterClass;
    private Stats baseStats;
    private Stats combatStats;
    private final Equipment equipment = new Equipment();
    private final Inventory inventory = new Inventory();
    private final Party party = new Party();
    private int level = 1;
    private int xp = 0;
    private int gold = 0;

    private double runDamageBonus;
    private double runXpBonus;
    private int goldTrainingHp;
    private int goldTrainingAtk;
    private int inventoryExpansions;
    private double vampirismChance;
    private double vampirismHealPercent;
    private final RunEffects runEffects = new RunEffects();
    private boolean ascensionUnlocked;
    private int abdolbosPotions;
    private int abdolbosUsedThisRun;
    private int blessingScrolls;
    private int xpScrolls;
    private int luckScrolls;
    private int greedScrolls;
    private int fogScrolls;
    private int secondBreathScrolls;

    private int runDropBonusPercent;
    private double runGoldBonus;
    private boolean runFogScrollActive;
    private boolean runSecondBreathActive;
    private boolean runSecondBreathUsed;
    private final SkillTreeProgress.PlayerTrees skillTrees = new SkillTreeProgress.PlayerTrees();
    private final EnumSet<Rarity> autoSellRarities = EnumSet.noneOf(Rarity.class);
    private int rawOre;
    private final List<Gem> gemStash = new ArrayList<>();
    private long lastGolemRewardEpochDay = -1;
    private final QuestLog questLog = new QuestLog();
    private int extraGemStashSlots;
    private int shopDiscountPercent;
    private final progression.RovingMerchant rovingMerchant = new progression.RovingMerchant();

    public Player(CharacterClass characterClass) {
        this.characterClass = characterClass;
        this.baseStats = characterClass.createBaseStats();
        recalculateStats();
    }

    public void recalculateStats(MetaProgression meta) {
        int prevCur = combatStats != null ? combatStats.getCurrentHp() : -1;
        int prevMax = combatStats != null ? Math.max(1, combatStats.getMaxHp()) : 0;
        applyEquipmentStats(meta);
        if (prevCur >= 0 && prevMax > 0) {
            int scaled = (int) Math.round((double) prevCur / prevMax * combatStats.getMaxHp());
            combatStats.setCurrentHp(Math.max(0, Math.min(scaled, combatStats.getMaxHp())));
        } else {
            combatStats.setCurrentHp(Math.min(baseStats.getCurrentHp(), combatStats.getMaxHp()));
            if (combatStats.getCurrentHp() == 0 && baseStats.isAlive()) {
                combatStats.healFull();
            }
        }
    }

    public void recalculateStats() {
        applyEquipmentStats(null);
        if (combatStats.getCurrentHp() == 0) {
            combatStats.healFull();
        } else {
            combatStats.setCurrentHp(Math.min(baseStats.getCurrentHp() + equipment.totalHpBonus(), combatStats.getMaxHp()));
        }
    }

    private void applyEquipmentStats(MetaProgression meta) {
        SetBonusCalculator.BonusTotals set = SetBonusCalculator.calculate(equipment);

        int hp = baseStats.getMaxHp() + equipment.totalHpBonus() + set.flatHp;
        int atk = baseStats.getAttack() + equipment.totalAttackBonus() + set.flatAtk;
        int def = baseStats.getDefense() + equipment.totalDefenseBonus() + set.flatDef;
        double crit = baseStats.getCritChance() + equipment.totalCritBonus() + set.flatCrit;
        double critDmg = baseStats.getCritDamage() + equipment.totalCritDamageBonus() + set.flatCritDmg;
        double dodge = baseStats.getDodgeChance() + equipment.totalDodgeBonus() + set.flatDodge;

        hp = (int) (hp * (1.0 + set.percentHp / 100.0));
        atk = (int) (atk * (1.0 + set.percentAtk / 100.0));
        def = (int) (def * (1.0 + set.percentDef / 100.0));

        if (meta != null) {
            hp = (int) (hp * meta.getHealthMultiplier());
            atk = (int) (atk * meta.getDamageMultiplier());
        }
        atk = (int) (atk * (1.0 + runDamageBonus) * runEffects.damageMultiplier());
        hp = (int) (hp * runEffects.hpMultiplier());
        def = (int) (def * runEffects.defMultiplier());
        crit += runEffects.critBonus();
        dodge += runEffects.dodgeBonus();

        combatStats = new Stats(hp, atk, def, StatCaps.capCrit(crit), critDmg, StatCaps.capDodge(dodge));
        SkillTreeProgress tree = skillTrees.forClass(characterClass);
        SkillEffectCalculator.applyPassiveStats(characterClass, tree, combatStats, false);
        SkillEffectCalculator.applyOverflowStats(tree, combatStats);

        double vampChance = equipment.totalVampirismChance() + set.vampirismChance;
        double vampHeal = Math.max(equipment.maxVampirismHealPercent(), set.vampirismHealPercent);
        vampirismChance = Math.min(50, vampChance);
        vampirismHealPercent = vampHeal;
    }

    public int applyVampirism(CombatService.AttackResult hit) {
        if (hit.isDodged() || hit.getDamage() <= 0 || vampirismChance <= 0 || vampirismHealPercent <= 0) {
            return 0;
        }
        if (!RandomUtil.chance(vampirismChance)) {
            return 0;
        }
        int healAmount = Math.max(1, (int) (hit.getDamage() * vampirismHealPercent / 100.0));
        return combatStats.heal(healAmount);
    }

    public double getVampirismChance() {
        return vampirismChance;
    }

    public double getVampirismHealPercent() {
        return vampirismHealPercent;
    }

    public int applyXpBonus(int amount) {
        return (int) (amount * (1.0 + runXpBonus) * runEffects.xpMultiplier());
    }

    public int applyGoldBonus(int amount) {
        return (int) (amount * (1.0 + runGoldBonus) * runEffects.goldMultiplier());
    }

    public int getRunDropBonusPercent() {
        return runDropBonusPercent;
    }

    public double getRunGoldBonus() {
        return runGoldBonus;
    }

    public boolean isRunFogScrollActive() {
        return runFogScrollActive;
    }

    public boolean isFogScrollActiveForFloor(int floor) {
        return runFogScrollActive && floor <= progression.MagicShop.FOG_SCROLL_FLOORS;
    }

    public boolean isRunSecondBreathActive() {
        return runSecondBreathActive && !runSecondBreathUsed;
    }

    public boolean trySecondBreathRevive(MetaProgression meta) {
        if (!isRunSecondBreathActive()) {
            return false;
        }
        runSecondBreathUsed = true;
        int healPct = progression.MagicShop.SECOND_BREATH_HEAL_PERCENT;
        reviveToPercentHp(combatStats, healPct);
        recalculateStats(meta);
        reviveToPercentHp(combatStats, healPct);
        baseStats.setCurrentHp(combatStats.getCurrentHp());
        for (int i = 0; i < Party.MAX_MERC_SLOTS; i++) {
            Mercenary m = party.getMercenary(i);
            if (m != null) {
                reviveToPercentHp(m.getStats(), healPct);
            }
        }
        return true;
    }

    private static void reviveToPercentHp(Stats stats, int percent) {
        int hp = Math.max(1, stats.getMaxHp() * percent / 100);
        stats.setCurrentHp(hp);
    }

    public double getRunDropPenalty() {
        return runEffects.dropPenalty();
    }

    public boolean equipItem(int inventoryIndex, MetaProgression meta) {
        Item item = inventory.get(inventoryIndex);
        if (item == null) {
            return false;
        }

        Equipment.Slot slot = item.getSlot();
        Item previous = equipment.get(slot);

        if (previous != null && inventory.isFull()) {
            return false;
        }

        inventory.remove(inventoryIndex);
        equipment.equip(slot, item);
        if (previous != null) {
            inventory.add(previous);
        }

        recalculateStats(meta);
        combatStats.healFull();
        baseStats.setCurrentHp(combatStats.getMaxHp() - equipment.totalHpBonus());
        return true;
    }

    public boolean sellItem(int inventoryIndex) {
        Item item = inventory.get(inventoryIndex);
        if (item == null) {
            return false;
        }
        gold += item.getSellPrice();
        inventory.remove(inventoryIndex);
        return true;
    }

    public boolean deleteItem(int inventoryIndex) {
        return inventory.remove(inventoryIndex) != null;
    }

    public void addXp(int baseAmount, MetaProgression meta, LevelService levelService) {
        int amount = applyXpBonus(baseAmount);
        xp += amount;
        while (levelService.canLevelUp(level, xp)) {
            xp -= levelService.xpRequiredForLevel(level);
            level++;
            levelService.applyLevelUp(baseStats);
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_CYAN,
                    ">>> Уровень повышен! Теперь уровень " + level));
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW,
                    ">>> +1 очко талантов (меню «Персонаж → Дерево умений»)"));
        }
        recalculateStats(meta);
        combatStats.healFull();
        baseStats.setCurrentHp(combatStats.getMaxHp() - equipment.totalHpBonus());
    }

    public void healFull(MetaProgression meta) {
        recalculateStats(meta);
        combatStats.healFull();
        baseStats.setCurrentHp(combatStats.getMaxHp() - equipment.totalHpBonus());
    }

    /** Лечит только героя. Для всего отряда вызывайте {@link party.Party#prepareForRun}. */
    public void prepareForRun(MetaProgression meta) {
        recalculateStats(meta);
        healFull(meta);
    }

    public Party getParty() {
        return party;
    }

    public void applyDeathPenalty(MetaProgression meta, LevelService levelService) {
        finishRunAfterRun(meta);
    }

    public void finishRunAfterRun(MetaProgression meta) {
        clearRunBuffs();
        runEffects.clear();
        abdolbosUsedThisRun = 0;
        resetRunScrollEffects();
        recalculateStats(meta);
        healFull(meta);
    }

    public void resetAbdolbosRunCounter() {
        abdolbosUsedThisRun = 0;
        runEffects.clear();
    }

    private void resetRunScrollEffects() {
        runDropBonusPercent = 0;
        runGoldBonus = 0;
        runFogScrollActive = false;
        runSecondBreathActive = false;
        runSecondBreathUsed = false;
    }

    public void clearRunBuffs() {
        runDamageBonus = 0;
        runXpBonus = 0;
    }

    /** Списывает по одному свитку из запаса и включает бонусы на текущий забег. */
    public void applyScrollsForRun() {
        if (blessingScrolls > 0) {
            blessingScrolls--;
            runDamageBonus = 0.15;
        }
        if (xpScrolls > 0) {
            xpScrolls--;
            runXpBonus = 0.50;
        }
        if (luckScrolls > 0) {
            luckScrolls--;
            runDropBonusPercent = progression.MagicShop.LUCK_DROP_BONUS_PERCENT;
        }
        if (greedScrolls > 0) {
            greedScrolls--;
            runGoldBonus = progression.MagicShop.GREED_GOLD_BONUS;
        }
        if (fogScrolls > 0) {
            fogScrolls--;
            runFogScrollActive = true;
        }
        if (secondBreathScrolls > 0) {
            secondBreathScrolls--;
            runSecondBreathActive = true;
        }
    }

    public int getBlessingScrolls() {
        return blessingScrolls;
    }

    public int getXpScrolls() {
        return xpScrolls;
    }

    public void setBlessingScrolls(int count) {
        blessingScrolls = Math.max(0, count);
    }

    public void setXpScrolls(int count) {
        xpScrolls = Math.max(0, count);
    }

    public boolean addBlessingScroll(int amount, int max) {
        if (blessingScrolls >= max) {
            return false;
        }
        blessingScrolls = Math.min(max, blessingScrolls + amount);
        return true;
    }

    public boolean addXpScroll(int amount, int max) {
        if (xpScrolls >= max) {
            return false;
        }
        xpScrolls = Math.min(max, xpScrolls + amount);
        return true;
    }

    public int getLuckScrolls() {
        return luckScrolls;
    }

    public int getGreedScrolls() {
        return greedScrolls;
    }

    public int getFogScrolls() {
        return fogScrolls;
    }

    public int getSecondBreathScrolls() {
        return secondBreathScrolls;
    }

    public void setLuckScrolls(int count) {
        luckScrolls = Math.max(0, count);
    }

    public void setGreedScrolls(int count) {
        greedScrolls = Math.max(0, count);
    }

    public void setFogScrolls(int count) {
        fogScrolls = Math.max(0, count);
    }

    public void setSecondBreathScrolls(int count) {
        secondBreathScrolls = Math.max(0, count);
    }

    public boolean addLuckScroll(int amount, int max) {
        if (luckScrolls >= max) {
            return false;
        }
        luckScrolls = Math.min(max, luckScrolls + amount);
        return true;
    }

    public boolean addGreedScroll(int amount, int max) {
        if (greedScrolls >= max) {
            return false;
        }
        greedScrolls = Math.min(max, greedScrolls + amount);
        return true;
    }

    public boolean addFogScroll(int amount, int max) {
        if (fogScrolls >= max) {
            return false;
        }
        fogScrolls = Math.min(max, fogScrolls + amount);
        return true;
    }

    public boolean addSecondBreathScroll(int amount, int max) {
        if (secondBreathScrolls >= max) {
            return false;
        }
        secondBreathScrolls = Math.min(max, secondBreathScrolls + amount);
        return true;
    }

    public void changeClass(CharacterClass newClass, LevelService levelService) {
        this.characterClass = newClass;
        this.baseStats = levelService.createStatsForLevel(newClass, level);
        baseStats.addMaxHp(goldTrainingHp);
        baseStats.addAttack(goldTrainingAtk);
        inventory.setMaxSize(Inventory.BASE_SIZE + inventoryExpansions * 2);
        recalculateStats();
        combatStats.healFull();
        baseStats.setCurrentHp(combatStats.getMaxHp() - equipment.totalHpBonus());
    }

    public void restoreProgress(CharacterClass characterClass, int level, int xp, LevelService levelService) {
        this.characterClass = characterClass;
        this.level = level;
        this.xp = xp;
        this.baseStats = levelService.createStatsForLevel(characterClass, level);
        recalculateStats();
    }

    public void applyGoldTraining(int totalHpBonus, int totalAtkBonus, int invExpansions) {
        this.goldTrainingHp = totalHpBonus;
        this.goldTrainingAtk = totalAtkBonus;
        this.inventoryExpansions = invExpansions;
        baseStats.addMaxHp(totalHpBonus);
        baseStats.addAttack(totalAtkBonus);
        inventory.setMaxSize(Inventory.BASE_SIZE + invExpansions * 2);
    }

    public void addGoldTrainingHp(int amount) {
        goldTrainingHp += amount;
    }

    public void addGoldTrainingAtk(int amount) {
        goldTrainingAtk += amount;
    }

    public void addInventoryExpansion() {
        inventoryExpansions++;
        inventory.expand(2);
    }

    public boolean spendGold(int amount) {
        if (gold < amount) {
            return false;
        }
        gold -= amount;
        return true;
    }

    public void setRunDamageBonus(double runDamageBonus) {
        this.runDamageBonus = runDamageBonus;
    }

    public void setRunXpBonus(double runXpBonus) {
        this.runXpBonus = runXpBonus;
    }

    public double getRunDamageBonus() {
        return runDamageBonus;
    }

    public double getRunXpBonus() {
        return runXpBonus;
    }

    public int getGoldTrainingHp() {
        return goldTrainingHp;
    }

    public int getGoldTrainingAtk() {
        return goldTrainingAtk;
    }

    public int getInventoryExpansions() {
        return inventoryExpansions;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public CharacterClass getCharacterClass() {
        return characterClass;
    }

    public Stats getCombatStats() {
        return combatStats;
    }

    public Stats getBaseStats() {
        return baseStats;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public int getLevel() {
        return level;
    }

    public int getXp() {
        return xp;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public void addGold(int amount) {
        gold += amount;
    }

    public Set<Rarity> getAutoSellRarities() {
        return EnumSet.copyOf(autoSellRarities);
    }

    public boolean isAutoSell(Rarity rarity) {
        return autoSellRarities.contains(rarity);
    }

    public boolean toggleAutoSell(Rarity rarity) {
        if (autoSellRarities.contains(rarity)) {
            autoSellRarities.remove(rarity);
            return false;
        }
        autoSellRarities.add(rarity);
        return true;
    }

    public void setAutoSellRarities(Set<Rarity> rarities) {
        autoSellRarities.clear();
        if (rarities != null) {
            autoSellRarities.addAll(rarities);
        }
    }

    /** Автопродажа при подборе: true — предмет продан, в инвентарь не кладём. */
    public boolean tryAutoSell(Item item) {
        if (item == null || !autoSellRarities.contains(item.getRarity())) {
            return false;
        }
        gold += item.getSellPrice();
        return true;
    }

    public RunEffects getRunEffects() {
        return runEffects;
    }

    public boolean hasAscensionUnlock() {
        return ascensionUnlocked;
    }

    public void unlockAscension() {
        ascensionUnlocked = true;
    }

    public void setAscensionUnlocked(boolean ascensionUnlocked) {
        this.ascensionUnlocked = ascensionUnlocked;
    }

    public int getAbdolbosPotions() {
        return abdolbosPotions;
    }

    public void addAbdolbosPotion() {
        abdolbosPotions++;
    }

    public void setAbdolbosPotions(int abdolbosPotions) {
        this.abdolbosPotions = Math.max(0, abdolbosPotions);
    }

    public boolean spendAbdolbosPotion() {
        if (abdolbosPotions <= 0) {
            return false;
        }
        abdolbosPotions--;
        return true;
    }

    public int getAbdolbosUsedThisRun() {
        return abdolbosUsedThisRun;
    }

    public void incrementAbdolbosUsedThisRun() {
        abdolbosUsedThisRun++;
    }

    public SkillTreeProgress.PlayerTrees getSkillTrees() {
        return skillTrees;
    }

    public SkillTreeProgress getSkillProgressForCurrentClass() {
        return skillTrees.forClass(characterClass);
    }

    public int getAvailableSkillPoints() {
        return SkillTreeService.availablePoints(level, skillTrees);
    }

    public int countUnspentSkillPointsAllMercenaries() {
        int total = 0;
        for (int i = 0; i < Party.MAX_MERC_SLOTS; i++) {
            Mercenary m = party.getMercenary(i);
            if (m != null) {
                total += m.getAvailableSkillPoints();
            }
        }
        return total;
    }

    public int getRawOre() {
        return rawOre;
    }

    public void setRawOre(int rawOre) {
        this.rawOre = Math.max(0, rawOre);
    }

    public void addRawOre(int amount) {
        rawOre = Math.max(0, rawOre + amount);
    }

    public List<Gem> getGemStash() {
        return gemStash;
    }

    public void addGem(Gem gem) {
        if (gem != null) {
            gemStash.add(gem);
        }
    }

    public boolean removeGem(Gem gem) {
        return gemStash.remove(gem);
    }

    public void clearGemStash() {
        gemStash.clear();
    }

    public boolean isGolemRewardClaimedToday() {
        return lastGolemRewardEpochDay == LocalDate.now().toEpochDay();
    }

    public void markGolemRewardClaimed() {
        lastGolemRewardEpochDay = LocalDate.now().toEpochDay();
    }

    public long getLastGolemRewardEpochDay() {
        return lastGolemRewardEpochDay;
    }

    public void setLastGolemRewardEpochDay(long day) {
        this.lastGolemRewardEpochDay = day;
    }

    public QuestLog getQuestLog() {
        return questLog;
    }

    public int getMaxGemStashSize() {
        return forge.ForgeService.MAX_GEM_STASH + extraGemStashSlots;
    }

    public int getExtraGemStashSlots() {
        return extraGemStashSlots;
    }

    public void setExtraGemStashSlots(int extraGemStashSlots) {
        this.extraGemStashSlots = Math.max(0, extraGemStashSlots);
    }

    public void addExtraGemStashSlots(int amount) {
        if (amount > 0) {
            extraGemStashSlots += amount;
        }
    }

    public int getShopDiscountPercent() {
        return shopDiscountPercent;
    }

    public void setShopDiscountPercent(int shopDiscountPercent) {
        this.shopDiscountPercent = Math.max(0, Math.min(50, shopDiscountPercent));
    }

    public int getShopPrice(int basePrice) {
        if (basePrice <= 0 || shopDiscountPercent <= 0) {
            return basePrice;
        }
        return Math.max(1, (int) Math.ceil(basePrice * (100 - shopDiscountPercent) / 100.0));
    }

    public boolean spendShopGold(int basePrice) {
        return spendGold(getShopPrice(basePrice));
    }

    public progression.RovingMerchant getRovingMerchant() {
        return rovingMerchant;
    }
}
