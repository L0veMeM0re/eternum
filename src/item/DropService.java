package item;

import util.RandomUtil;

/**
 * Шансы дропа и распределение редкости.
 * Тесты: {@code test.DropBalanceTest} (1M симуляций).
 */
public final class DropService {
    public static final int BASE_ITEM_DROP_PERCENT = 25;
    /** +1% к шансу предмета за уровень «Шанс дропа» в мета-прогрессии. */
    public static final int META_DROP_LEVEL_BONUS_PERCENT = 1;
    public static final int MAX_ITEM_DROP_PERCENT = 35;
    public static final int BONUS_GOLD_PERCENT = 28;

    /** Точность ролла редкости: 1_000_000 = 100%. */
    public static final int RARITY_ROLL_SCALE = 1_000_000;

    /** Обычный/элитный моб после выпадения предмета. */
    public static final int MOB_MYTHIC_PPM = 10;           // 0.001%
    public static final int MOB_LEGENDARY_PPM = 1_000;     // 0.1%
    public static final int MOB_EPIC_PPM = 30_000;           // 3%
    public static final int MOB_RARE_PPM = 150_000;         // 15%
    // Common ≈ 81.899%

    /** Босс — чуть выше, но всё ещё редко. */
    public static final int BOSS_MYTHIC_PPM = 100;          // 0.01%
    public static final int BOSS_LEGENDARY_PPM = 5_000;      // 0.5%
    public static final int BOSS_EPIC_PPM = 50_000;          // 5%
    public static final int BOSS_RARE_PPM = 200_000;         // 20%

    public enum MobDropOutcome {
        ITEM,
        BONUS_GOLD,
        NOTHING
    }

    private DropService() {
    }

    /** Шанс выпадения предмета с обычного/элитного моба (не босс). */
    public static MobDropOutcome rollMobDrop(boolean elite, int dropLevel, double dropPenaltyPercent) {
        return rollMobDrop(elite, dropLevel, dropPenaltyPercent, 0);
    }

    public static MobDropOutcome rollMobDrop(boolean elite, int dropLevel, double dropPenaltyPercent,
                                             int runDropBonusPercent) {
        int itemChance = itemDropChancePercent(dropLevel, dropPenaltyPercent, runDropBonusPercent);
        if (RandomUtil.chance(itemChance)) {
            return MobDropOutcome.ITEM;
        }
        if (RandomUtil.chance(BONUS_GOLD_PERCENT)) {
            return MobDropOutcome.BONUS_GOLD;
        }
        return MobDropOutcome.NOTHING;
    }

    public static int itemDropChancePercent(int dropLevel, double dropPenaltyPercent) {
        return itemDropChancePercent(dropLevel, dropPenaltyPercent, 0);
    }

    public static int itemDropChancePercent(int dropLevel, double dropPenaltyPercent, int runDropBonusPercent) {
        int bonus = (int) Math.round(dropPenaltyPercent);
        int chance = BASE_ITEM_DROP_PERCENT + dropLevel * META_DROP_LEVEL_BONUS_PERCENT
                - bonus + runDropBonusPercent;
        return Math.max(5, Math.min(MAX_ITEM_DROP_PERCENT, chance));
    }

    /** Минимальная редкость с обычного моба (до rollRarity). */
    public static Rarity minRarityForMob(boolean elite, int floor) {
        if (elite) {
            return floor >= 25 ? Rarity.EPIC : Rarity.RARE;
        }
        return floor >= 40 ? Rarity.RARE : Rarity.COMMON;
    }

    /** Мягкий бонус мин. редкости у босса от мета-улучшения дропа. */
    public static Rarity rollBossMinRarity(double dropBonusPercent) {
        if (RandomUtil.chance(Math.min(8, 2 + dropBonusPercent / 10))) {
            return Rarity.LEGENDARY;
        }
        if (RandomUtil.chance(Math.min(18, 6 + dropBonusPercent / 5))) {
            return Rarity.EPIC;
        }
        if (RandomUtil.chance(Math.min(35, 15 + (int) dropBonusPercent))) {
            return Rarity.RARE;
        }
        return Rarity.COMMON;
    }

    public static Rarity rollRarity(int floor, Rarity minRarity, boolean bossDrop) {
        int roll = RandomUtil.range(1, RARITY_ROLL_SCALE);
        Rarity rarity = rarityFromRoll(roll, bossDrop);

        if (rarity.ordinal() < minRarity.ordinal()) {
            rarity = minRarity;
        }

        int floorBonus = floor / 25;
        for (int i = 0; i < floorBonus && rarity.ordinal() < Rarity.MYTHIC.ordinal(); i++) {
            if (RandomUtil.chance(8)) {
                rarity = Rarity.values()[rarity.ordinal() + 1];
            }
        }
        return rarity;
    }

    /** Для тестов: редкость по числу 1..RARITY_ROLL_SCALE без бонуса этажа. */
    public static Rarity rarityFromRoll(int roll, boolean bossDrop) {
        int mythic = bossDrop ? BOSS_MYTHIC_PPM : MOB_MYTHIC_PPM;
        int legendary = bossDrop ? BOSS_LEGENDARY_PPM : MOB_LEGENDARY_PPM;
        int epic = bossDrop ? BOSS_EPIC_PPM : MOB_EPIC_PPM;
        int rare = bossDrop ? BOSS_RARE_PPM : MOB_RARE_PPM;

        if (roll <= mythic) {
            return Rarity.MYTHIC;
        }
        if (roll <= mythic + legendary) {
            return Rarity.LEGENDARY;
        }
        if (roll <= mythic + legendary + epic) {
            return Rarity.EPIC;
        }
        if (roll <= mythic + legendary + epic + rare) {
            return Rarity.RARE;
        }
        return Rarity.COMMON;
    }

    public static double expectedRarityPercent(Rarity rarity, boolean bossDrop) {
        int mythic = bossDrop ? BOSS_MYTHIC_PPM : MOB_MYTHIC_PPM;
        int legendary = bossDrop ? BOSS_LEGENDARY_PPM : MOB_LEGENDARY_PPM;
        int epic = bossDrop ? BOSS_EPIC_PPM : MOB_EPIC_PPM;
        int rare = bossDrop ? BOSS_RARE_PPM : MOB_RARE_PPM;
        int ppm = switch (rarity) {
            case MYTHIC -> mythic;
            case LEGENDARY -> legendary;
            case EPIC -> epic;
            case RARE -> rare;
            case COMMON -> RARITY_ROLL_SCALE - mythic - legendary - epic - rare;
        };
        return ppm * 100.0 / RARITY_ROLL_SCALE;
    }
}
