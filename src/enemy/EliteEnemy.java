package enemy;

import player.Stats;
import util.ConsoleColors;

public class EliteEnemy extends Enemy {
    public EliteEnemy(EnemyType type, String zone, Stats eliteStats) {
        super(type.getDisplayName(), zone, eliteStats, type);
    }

    public EliteEnemy(String name, EnemyType type, String zone, Stats eliteStats) {
        super(name, zone, eliteStats, type);
    }

    @Override
    public String getName() {
        return ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW, "★ ") + super.getName();
    }

    @Override
    public String[] getPortraitLines() {
        String[] base = super.getPortraitLines();
        if (base == null || base.length == 0) {
            return new String[]{"  ★"};
        }
        int width = 0;
        for (String line : base) {
            width = Math.max(width, line.length());
        }
        String starLine = " ".repeat(Math.max(0, width / 2 - 1)) + "★";
        String[] withStar = new String[base.length + 1];
        withStar[0] = starLine;
        System.arraycopy(base, 0, withStar, 1, base.length);
        return withStar;
    }

    @Override
    public String getPortraitHighlightChars() {
        return super.getPortraitHighlightChars() + "★";
    }

    @Override
    public boolean isElite() {
        return true;
    }

    @Override
    public int calculateXpReward() {
        return (int) (super.calculateXpReward() * EliteModifier.REWARD_MULT);
    }

    @Override
    public int calculateGoldReward() {
        return (int) (super.calculateGoldReward() * EliteModifier.REWARD_MULT);
    }
}
