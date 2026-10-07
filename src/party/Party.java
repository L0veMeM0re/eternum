package party;

import combat.Fighter;
import combat.HeroFighter;
import player.CharacterClass;
import player.Player;
import progression.LevelService;
import progression.MetaProgression;

import java.util.ArrayList;
import java.util.List;

public class Party {
    public static final int MAX_MERC_SLOTS = 3;

    private int unlockedMercSlots;
    private final Mercenary[] mercenaries = new Mercenary[MAX_MERC_SLOTS];

    public int getUnlockedMercSlots() {
        return unlockedMercSlots;
    }

    public void setUnlockedMercSlots(int unlockedMercSlots) {
        this.unlockedMercSlots = Math.min(MAX_MERC_SLOTS, Math.max(0, unlockedMercSlots));
    }

    public Mercenary getMercenary(int slot) {
        if (slot < 0 || slot >= MAX_MERC_SLOTS) {
            return null;
        }
        return mercenaries[slot];
    }

    public void setMercenary(int slot, Mercenary mercenary) {
        if (slot >= 0 && slot < MAX_MERC_SLOTS) {
            mercenaries[slot] = mercenary;
        }
    }

    public int getMercenaryCount() {
        int count = 0;
        for (Mercenary m : mercenaries) {
            if (m != null) {
                count++;
            }
        }
        return count;
    }

    public boolean hasRecruitedMercenary() {
        return getMercenaryCount() > 0;
    }

    public int findEmptySlot() {
        for (int i = 0; i < unlockedMercSlots; i++) {
            if (mercenaries[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public boolean isAnyMemberAlive(Player player) {
        if (player.getCombatStats().isAlive()) {
            return true;
        }
        for (Mercenary m : mercenaries) {
            if (m != null && m.isAlive()) {
                return true;
            }
        }
        return false;
    }

    public void prepareForRun(Player player, MetaProgression meta) {
        player.recalculateStats(meta);
        player.healFull(meta);
        double runDmg = player.getRunDamageBonus();
        boolean clericAura = player.getCharacterClass() == CharacterClass.CLERIC;
        for (Mercenary m : mercenaries) {
            if (m != null) {
                m.healFull(meta, runDmg, clericAura);
            }
        }
    }

    public void recalculateAll(Player player, MetaProgression meta) {
        player.recalculateStats(meta);
        double runDmg = player.getRunDamageBonus();
        boolean clericAura = player.getCharacterClass() == CharacterClass.CLERIC;
        for (Mercenary m : mercenaries) {
            if (m != null) {
                m.recalculateStats(meta, runDmg, clericAura);
            }
        }
    }

    /** Герой, затем слоты 1–3 (наёмники). */
    public List<Fighter> getCombatOrder(Player player) {
        List<Fighter> order = new ArrayList<>();
        order.add(new HeroFighter(player));
        for (int i = 0; i < MAX_MERC_SLOTS; i++) {
            Mercenary m = mercenaries[i];
            if (m != null) {
                order.add(m);
            }
        }
        return order;
    }

    public List<Mercenary> getLivingMercenaries() {
        List<Mercenary> list = new ArrayList<>();
        for (Mercenary m : mercenaries) {
            if (m != null && m.isAlive()) {
                list.add(m);
            }
        }
        return list;
    }

    public void grantXpToMercenaries(int baseXp, MetaProgression meta, LevelService levelService,
                                     double runXpBonus) {
        for (Mercenary m : mercenaries) {
            if (m != null && m.isAlive()) {
                m.addXp(baseXp, meta, levelService, runXpBonus);
            }
        }
    }
}
