package town;

import item.ItemSet;
import player.CharacterClass;
import player.Stats;

/** NPC-боец арены (не участник забега). */
public final class Gladiator {
    private final String name;
    private final CharacterClass characterClass;
    private final Stats stats;
    private final ItemSet itemSet;
    private final int setPieces;

    public Gladiator(String name, CharacterClass characterClass, Stats stats,
                     ItemSet itemSet, int setPieces) {
        this.name = name;
        this.characterClass = characterClass;
        this.stats = stats;
        this.itemSet = itemSet;
        this.setPieces = setPieces;
    }

    public String getName() {
        return name;
    }

    public CharacterClass getCharacterClass() {
        return characterClass;
    }

    public Stats getStats() {
        return stats;
    }

    public ItemSet getItemSet() {
        return itemSet;
    }

    public int getSetPieces() {
        return setPieces;
    }

    public boolean isAlive() {
        return stats.isAlive();
    }

    public double powerRating() {
        return stats.getMaxHp()
                + stats.getAttack() * 4.5
                + stats.getDefense() * 7.0
                + stats.getCritChance() * 14.0
                + stats.getDodgeChance() * 11.0
                + stats.getCritDamage() * 5.0;
    }

    public Gladiator copyForFight() {
        return new Gladiator(name, characterClass, stats.copy(), itemSet, setPieces);
    }

    public String setLine() {
        if (itemSet == ItemSet.NONE || setPieces <= 0) {
            return "без сета";
        }
        return itemSet.getDisplayName() + " (" + setPieces + ")";
    }
}
