package combat;

import inventory.Equipment;
import player.Player;
import player.Stats;

public final class HeroFighter implements Fighter {
    private final Player player;

    public HeroFighter(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public String getCombatName() {
        return player.getCharacterClass().getDisplayName();
    }

    @Override
    public Stats getStats() {
        return player.getCombatStats();
    }

    @Override
    public Equipment getEquipment() {
        return player.getEquipment();
    }

    @Override
    public boolean isAlive() {
        return player.getCombatStats().isAlive();
    }

    @Override
    public boolean isHero() {
        return true;
    }

    @Override
    public int applyVampirism(CombatService.AttackResult hit) {
        return player.applyVampirism(hit);
    }
}
