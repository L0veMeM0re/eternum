package party;

import player.CharacterClass;
import player.Player;
import util.RandomUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Генерация кандидатов в наёмники для выбора игроком. */
public final class MercenaryRecruiter {
    public static final int OFFER_COUNT = 3;

    private MercenaryRecruiter() {
    }

    public static List<Mercenary> generateOffers() {
        List<Mercenary> offers = new ArrayList<>();
        Set<String> usedNames = new HashSet<>();
        int attempts = 0;
        while (offers.size() < OFFER_COUNT && attempts < 50) {
            attempts++;
            String name = MercenaryNames.randomName();
            if (!usedNames.add(name)) {
                continue;
            }
            CharacterClass cls = randomClass();
            offers.add(new Mercenary(name, cls));
        }
        while (offers.size() < OFFER_COUNT) {
            offers.add(new Mercenary(MercenaryNames.randomName() + " II", randomClass()));
        }
        return offers;
    }

    public static Mercenary hireChosen(Player player, Party party, Mercenary candidate, int priceIndex) {
        if (party.getUnlockedMercSlots() == 0) {
            return null;
        }
        int empty = party.findEmptySlot();
        if (empty < 0) {
            return null;
        }
        int hired = party.getMercenaryCount();
        if (hired >= MercenaryShop.MAX_HIRE_TIERS) {
            return null;
        }
        int cost = MercenaryShop.hireCost(priceIndex);
        if (!player.spendGold(cost)) {
            return null;
        }
        party.setMercenary(empty, candidate);
        return candidate;
    }

    private static CharacterClass randomClass() {
        CharacterClass[] all = CharacterClass.values();
        return all[RandomUtil.range(0, all.length - 1)];
    }
}
