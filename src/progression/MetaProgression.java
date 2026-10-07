package progression;

import player.CharacterClass;

import java.util.EnumSet;
import java.util.Set;

public class MetaProgression {
    public static final int MAX_DAMAGE_LEVEL = 20;
    public static final int MAX_HEALTH_LEVEL = 20;
    public static final int MAX_DROP_LEVEL = 10;

    private int soulCrystals;
    private int damageLevel;
    private int healthLevel;
    private int dropLevel;
    private final Set<CharacterClass> unlockedClasses = EnumSet.allOf(CharacterClass.class);

    private int bestFloor;
    private int totalKills;
    private int totalDeaths;
    private long totalGoldEarned;
    private int totalItemsFound;
    private long totalPlayTimeSeconds;

    public int getSoulCrystals() {
        return soulCrystals;
    }

    public void addSoulCrystals(int amount) {
        soulCrystals += amount;
    }

    public boolean spendSoulCrystals(int amount) {
        if (soulCrystals < amount) {
            return false;
        }
        soulCrystals -= amount;
        return true;
    }

    public int getDamageLevel() {
        return damageLevel;
    }

    public int getHealthLevel() {
        return healthLevel;
    }

    public int getDropLevel() {
        return dropLevel;
    }

    public double getDamageMultiplier() {
        return 1.0 + damageLevel * 0.05;
    }

    public double getHealthMultiplier() {
        return 1.0 + healthLevel * 0.05;
    }

    public double getDropBonus() {
        return dropLevel * 2.0;
    }

    public int getUpgradeCost(int currentLevel) {
        return 5 + currentLevel * 3;
    }

    public boolean upgradeDamage() {
        if (damageLevel >= MAX_DAMAGE_LEVEL) {
            return false;
        }
        int cost = getUpgradeCost(damageLevel);
        if (!spendSoulCrystals(cost)) {
            return false;
        }
        damageLevel++;
        return true;
    }

    public boolean upgradeHealth() {
        if (healthLevel >= MAX_HEALTH_LEVEL) {
            return false;
        }
        int cost = getUpgradeCost(healthLevel);
        if (!spendSoulCrystals(cost)) {
            return false;
        }
        healthLevel++;
        return true;
    }

    public boolean upgradeDrop() {
        if (dropLevel >= MAX_DROP_LEVEL) {
            return false;
        }
        int cost = getUpgradeCost(dropLevel) + 2;
        if (!spendSoulCrystals(cost)) {
            return false;
        }
        dropLevel++;
        return true;
    }

    public Set<CharacterClass> getUnlockedClasses() {
        return unlockedClasses;
    }

    public int getBestFloor() {
        return bestFloor;
    }

    public void updateBestFloor(int floor) {
        if (floor > bestFloor) {
            bestFloor = floor;
        }
    }

    public int getTotalKills() {
        return totalKills;
    }

    public void addKill() {
        totalKills++;
    }

    public int getTotalDeaths() {
        return totalDeaths;
    }

    public void addDeath() {
        totalDeaths++;
    }

    public long getTotalGoldEarned() {
        return totalGoldEarned;
    }

    public void addGoldEarned(long amount) {
        totalGoldEarned += amount;
    }

    public int getTotalItemsFound() {
        return totalItemsFound;
    }

    public void addItemFound() {
        totalItemsFound++;
    }

    public long getTotalPlayTimeSeconds() {
        return totalPlayTimeSeconds;
    }

    public void addPlayTime(long seconds) {
        totalPlayTimeSeconds += seconds;
    }

    public void setSoulCrystals(int soulCrystals) {
        this.soulCrystals = soulCrystals;
    }

    public void setDamageLevel(int damageLevel) {
        this.damageLevel = damageLevel;
    }

    public void setHealthLevel(int healthLevel) {
        this.healthLevel = healthLevel;
    }

    public void setDropLevel(int dropLevel) {
        this.dropLevel = dropLevel;
    }

    public void setBestFloor(int bestFloor) {
        this.bestFloor = bestFloor;
    }

    public void setTotalKills(int totalKills) {
        this.totalKills = totalKills;
    }

    public void setTotalDeaths(int totalDeaths) {
        this.totalDeaths = totalDeaths;
    }

    public void setTotalGoldEarned(long totalGoldEarned) {
        this.totalGoldEarned = totalGoldEarned;
    }

    public void setTotalItemsFound(int totalItemsFound) {
        this.totalItemsFound = totalItemsFound;
    }

    public void setTotalPlayTimeSeconds(long totalPlayTimeSeconds) {
        this.totalPlayTimeSeconds = totalPlayTimeSeconds;
    }
}
