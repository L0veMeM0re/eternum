package combat;

import party.Mercenary;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

/** Состояние боя всего отряда за одну схватку. */
public class EncounterCombatState {
    private final HeroCombatState heroState = new HeroCombatState();
    private final Map<Mercenary, HeroCombatState> mercenaryStates = new IdentityHashMap<>();

    private int celestialAegisCharges;

    public HeroCombatState getHeroState() {
        return heroState;
    }

    public HeroCombatState stateFor(Mercenary mercenary) {
        return mercenaryStates.computeIfAbsent(mercenary, m -> new HeroCombatState());
    }

    public HeroCombatState stateFor(Fighter fighter) {
        if (fighter instanceof HeroFighter) {
            return heroState;
        }
        if (fighter instanceof Mercenary mercenary) {
            return stateFor(mercenary);
        }
        return heroState;
    }

    public Collection<HeroCombatState> allUnitStates() {
        java.util.List<HeroCombatState> all = new java.util.ArrayList<>();
        all.add(heroState);
        all.addAll(mercenaryStates.values());
        return all;
    }

    public void resetForEncounter() {
        heroState.resetForEncounter();
        mercenaryStates.clear();
    }

    public void resetForRun() {
        heroState.resetForRun();
        mercenaryStates.clear();
        celestialAegisCharges = 0;
    }

    public void endOfRound() {
        heroState.endOfRound();
        for (HeroCombatState state : mercenaryStates.values()) {
            state.endOfRound();
        }
    }

    public int getCelestialAegisCharges() {
        return celestialAegisCharges;
    }

    public void grantCelestialAegisCharge() {
        celestialAegisCharges++;
    }

    public void consumeCelestialAegisCharge() {
        if (celestialAegisCharges > 0) {
            celestialAegisCharges--;
        }
    }
}
