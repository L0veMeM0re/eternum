package progression;

import player.CharacterClass;
import player.Stats;

public class LevelService {
    public int xpRequiredForLevel(int currentLevel) {
        return 100 * currentLevel;
    }

    public boolean canLevelUp(int currentLevel, int currentXp) {
        return currentXp >= xpRequiredForLevel(currentLevel);
    }

    public void applyLevelUp(Stats stats) {
        stats.addMaxHp(5);
        stats.addAttack(1);
        stats.addDefense(1);
        stats.healFull();
    }

    public Stats createStatsForLevel(CharacterClass characterClass, int level) {
        Stats stats = characterClass.createBaseStats();
        for (int i = 1; i < level; i++) {
            applyLevelUp(stats);
        }
        return stats;
    }
}
