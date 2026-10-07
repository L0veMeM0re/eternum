package forge;

import combat.CombatService;
import combat.Fighter;
import combat.HeroCombatState;
import inventory.Equipment;
import item.Gem;
import item.GemTier;
import item.GemType;
import item.Item;
import item.Weapon;

/** Боевые эффекты сокетных камней (Shard и др.). */
public final class GemCombatService {
    private GemCombatService() {
    }

    public static void applyOutgoingModifiers(Fighter unit, HeroCombatState state,
                                              CombatService.AttackModifiers mods) {
        Gem shard = findShard(unit);
        if (shard == null) {
            return;
        }
        int nextHit = state.getAttackCounter() + 1;
        double bonus = switch (shard.getTier()) {
            case I -> nextHit % 6 == 0 ? 1.10 : 1.0;
            case II -> 1.0;
            case III -> nextHit % 5 == 0 ? 1.18 : 1.0;
        };
        if (bonus > 1.0) {
            mods.damageMultiplier(mods.getDamageMultiplier() * bonus);
        }
    }

    public static void afterHit(Fighter unit, CombatService.AttackResult hit) {
        if (hit.isDodged() || hit.getDamage() <= 0) {
            return;
        }
        Gem shard = findShard(unit);
        if (shard != null && shard.getTier() == GemTier.II && hit.isCritical()) {
            int heal = Math.max(1, (int) (hit.getDamage() * 0.04));
            unit.getStats().heal(heal);
        }
    }

    private static Gem findShard(Fighter unit) {
        if (unit == null || unit.getEquipment() == null) {
            return null;
        }
        Item item = unit.getEquipment().get(Equipment.Slot.WEAPON);
        if (!(item instanceof Weapon weapon)) {
            return null;
        }
        for (Gem gem : weapon.getSocketedGems()) {
            if (gem != null && gem.getType() == GemType.SHARD) {
                return gem;
            }
        }
        return null;
    }
}
