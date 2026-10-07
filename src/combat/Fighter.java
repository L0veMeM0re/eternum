package combat;

import inventory.Equipment;
import player.Stats;

/** Участник боя (герой или наёмник). */
public interface Fighter {
    String getCombatName();

    Stats getStats();

    Equipment getEquipment();

    boolean isAlive();

    boolean isHero();

    default int applyVampirism(CombatService.AttackResult hit) {
        return 0;
    }
}
