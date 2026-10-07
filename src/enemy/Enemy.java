package enemy;

import player.Stats;
import util.MobArt;

public class Enemy {
    protected final EnemyType type;
    protected final String name;
    protected final String zone;
    protected final Stats stats;

    public Enemy(EnemyType type, String zone, int floor) {
        this.type = type;
        this.name = type.getDisplayName();
        this.zone = zone;
        this.stats = EnemyScaling.scaleMobStats(type, floor);
    }

    protected Enemy(String name, String zone, Stats stats, EnemyType type) {
        this.type = type;
        this.name = name;
        this.zone = zone;
        this.stats = stats;
    }

    public EnemyType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getZone() {
        return zone;
    }

    public Stats getStats() {
        return stats;
    }

    public boolean isAlive() {
        return stats.isAlive();
    }

    public void printArt() {
        MobArt.print(getPortraitLines(), getPortraitPrimaryColor(), getPortraitAccentColor(),
                getPortraitHighlightChars());
    }

    public String[] getPortraitLines() {
        return type.getArt();
    }

    public String getPortraitPrimaryColor() {
        return type.getPrimaryColor();
    }

    public String getPortraitAccentColor() {
        return type.getAccentColor();
    }

    public String getPortraitHighlightChars() {
        return "oO0*@^●◉░▒▓█▲▼";
    }

    public int calculateXpReward() {
        return Math.max(5, stats.getMaxHp() / 3);
    }

    public int calculateGoldReward() {
        return Math.max(3, stats.getAttack() + stats.getMaxHp() / 10);
    }

    public boolean isElite() {
        return false;
    }
}
