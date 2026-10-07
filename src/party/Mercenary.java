package party;

import combat.CombatService;
import combat.Fighter;
import combat.UnitStatCalculator;
import inventory.Equipment;
import inventory.Inventory;
import item.Item;
import player.CharacterClass;
import player.Player;
import player.Stats;
import progression.LevelService;
import progression.MetaProgression;
import progression.RunEffects;
import progression.skill.SkillEffectCalculator;
import progression.skill.SkillTreeProgress;
import progression.skill.SkillTreeService;
import util.ConsoleColors;
import util.RandomUtil;

public class Mercenary implements Fighter {
    private final String name;
    private final CharacterClass characterClass;
    private final Equipment equipment = new Equipment();
    private Stats baseStats;
    private Stats combatStats;
    private int level = 1;
    private int xp = 0;
    private double vampirismChance;
    private double vampirismHealPercent;
    private final SkillTreeProgress skillTree = new SkillTreeProgress();

    public Mercenary(String name, CharacterClass characterClass) {
        this.name = name;
        this.characterClass = characterClass;
        this.baseStats = characterClass.createBaseStats();
        recalculateStats(null, 0);
    }

    public Mercenary(String name, CharacterClass characterClass, int level, int xp, LevelService levelService) {
        this.name = name;
        this.characterClass = characterClass;
        this.level = level;
        this.xp = xp;
        this.baseStats = levelService.createStatsForLevel(characterClass, level);
        recalculateStats(null, 0);
    }

    public void recalculateStats(MetaProgression meta, double runDamageBonus) {
        recalculateStats(meta, runDamageBonus, false);
    }

    public void recalculateStats(MetaProgression meta, double runDamageBonus, boolean clericAura) {
        combatStats = UnitStatCalculator.compute(baseStats, equipment, meta, runDamageBonus, clericAura);
        SkillEffectCalculator.applyPassiveStats(characterClass, skillTree, combatStats, clericAura);
        SkillEffectCalculator.applyOverflowStats(skillTree, combatStats);
        vampirismChance = UnitStatCalculator.vampChance(equipment);
        vampirismHealPercent = UnitStatCalculator.vampHealPercent(equipment);
        if (combatStats.getCurrentHp() == 0 && baseStats.isAlive()) {
            combatStats.healFull();
        }
    }

    public void healFull(MetaProgression meta, double runDamageBonus) {
        healFull(meta, runDamageBonus, false);
    }

    public void healFull(MetaProgression meta, double runDamageBonus, boolean clericAura) {
        recalculateStats(meta, runDamageBonus, clericAura);
        combatStats.healFull();
        baseStats.setCurrentHp(combatStats.getMaxHp() - equipment.totalHpBonus());
    }

    public void addXp(int baseAmount, MetaProgression meta, LevelService levelService, double runXpBonus) {
        int amount = (int) (baseAmount * (1.0 + runXpBonus));
        xp += amount;
        while (levelService.canLevelUp(level, xp)) {
            xp -= levelService.xpRequiredForLevel(level);
            level++;
            levelService.applyLevelUp(baseStats);
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_CYAN,
                    ">>> " + name + ": уровень " + level));
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW,
                    ">>> +1 очко талантов («Команда» → наёмник → Дерево умений)"));
        }
        recalculateStats(meta, 0);
        combatStats.healFull();
        baseStats.setCurrentHp(combatStats.getMaxHp() - equipment.totalHpBonus());
    }

    public boolean equipFromInventory(Player player, int inventoryIndex, MetaProgression meta, double runDamageBonus) {
        Item item = player.getInventory().get(inventoryIndex);
        if (item == null) {
            return false;
        }
        Equipment.Slot slot = item.getSlot();
        Item previous = equipment.get(slot);
        if (previous != null && player.getInventory().isFull()) {
            return false;
        }
        player.getInventory().remove(inventoryIndex);
        equipment.equip(slot, item);
        if (previous != null) {
            player.getInventory().add(previous);
        }
        recalculateStats(meta, runDamageBonus);
        combatStats.healFull();
        baseStats.setCurrentHp(combatStats.getMaxHp() - equipment.totalHpBonus());
        return true;
    }

    public boolean unequipToInventory(Player player, Equipment.Slot slot) {
        Item item = equipment.get(slot);
        if (item == null || !player.getInventory().add(item)) {
            return false;
        }
        equipment.unequip(slot);
        recalculateStats(null, 0);
        return true;
    }

    public void unequipAllToInventory(Player player) {
        for (Equipment.Slot slot : Equipment.Slot.values()) {
            while (equipment.get(slot) != null) {
                if (!unequipToInventory(player, slot)) {
                    break;
                }
            }
        }
    }

    @Override
    public String getCombatName() {
        return name;
    }

    public String getName() {
        return name;
    }

    public CharacterClass getCharacterClass() {
        return characterClass;
    }

    @Override
    public Stats getStats() {
        return combatStats;
    }

    public Stats getBaseStats() {
        return baseStats;
    }

    @Override
    public Equipment getEquipment() {
        return equipment;
    }

    @Override
    public boolean isAlive() {
        return combatStats.isAlive();
    }

    @Override
    public boolean isHero() {
        return false;
    }

    @Override
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

    public int getLevel() {
        return level;
    }

    public int getXp() {
        return xp;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public double getVampirismChance() {
        return vampirismChance;
    }

    public double getVampirismHealPercent() {
        return vampirismHealPercent;
    }

    public SkillTreeProgress getSkillTree() {
        return skillTree;
    }

    public int getAvailableSkillPoints() {
        return SkillTreeService.availablePointsForMercenary(level, skillTree);
    }
}
