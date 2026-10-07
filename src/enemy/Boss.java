package enemy;

import player.Stats;
import util.MobArt;

public class Boss extends Enemy {
    private final BossType bossType;
    private final int floor;

    public Boss(BossType bossType, String zone, int floor) {
        super(bossType.getDisplayName(), zone, EnemyScaling.scaleBossStats(bossType, floor), EnemyType.GOBLIN);
        this.bossType = bossType;
        this.floor = floor;
    }

    public BossType getBossType() {
        return bossType;
    }

    public int getFloor() {
        return floor;
    }

    @Override
    public String[] getPortraitLines() {
        return bossType.getArt();
    }

    @Override
    public String getPortraitPrimaryColor() {
        return bossType.getPrimaryColor();
    }

    @Override
    public String getPortraitAccentColor() {
        return bossType.getAccentColor();
    }

    @Override
    public String getPortraitHighlightChars() {
        return "oO@*^●#█▲";
    }

    @Override
    public void printArt() {
        MobArt.print(getPortraitLines(), getPortraitPrimaryColor(), getPortraitAccentColor(),
                getPortraitHighlightChars());
    }

    @Override
    public int calculateXpReward() {
        return Math.max(50, stats.getMaxHp() / 2);
    }

    @Override
    public int calculateGoldReward() {
        return Math.max(30, stats.getAttack() * 2);
    }
}
