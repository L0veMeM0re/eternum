package item;

import inventory.Equipment;
import util.RandomUtil;

import java.util.ArrayList;
import java.util.List;

public final class ItemFactory {
    private static final String[][] WEAPON_NAMES = {
            {"Ржавый меч", "Деревянный посох", "Костяной кинжал", "Палка странника", "Тупой топор"},
            {"Стальной меч", "Клинок дракона", "Кинжал тени", "Топор бури", "Кинжал феникса"},
            {"Меч дракона", "Посох огня", "Клинок тени", "Секира бури", "Клинок феникса"},
            {"Клык Бездны", "Посох Архимага", "Клинок Судьбы", "Молот титанов", "Громовой клинок"},
            {"Клинок Вечности", "Посох Создателя", "Кинжал Богов", "Молот Титанов", "Коса душ"},
            {"Пламя Мироздания", "Звёздный Разлом", "Клык Хаоса", "Судьбоносный клинок", "Первозданный клинок"}
    };

    private static final String[][] HELMET_NAMES = {
            {"Кожаный шлем", "Потёртый капюшон", "Простая повязка", "Латный шлем", "Шапка ученика"},
            {"Железный шлем", "Шлем дракона", "Капюшон тени", "Шлем бури", "Шлем феникса"},
            {"Шлем дракона", "Корона тьмы", "Шлем паладина", "Корона шторма", "Маска фантома"},
            {"Корона Бездны", "Корона звёзд", "Маска вечности", "Шлем титанов", "Корона пламени"},
            {"Диадема богов", "Шлем Создателя", "Корона Судьбы", "Маска бездны", "Венец титанов"},
            {"Корона Мироздания", "Шлем Первого света", "Венец Хаоса", "Диадема вечности", "Корона абсолюта"}
    };

    private static final String[][] ARMOR_NAMES = {
            {"Кожаная броня", "Роба ученика", "Потёртый нагрудник", "Кольчуга новичка", "Охотничий жилет"},
            {"Кольчуга", "Латы дракона", "Мантия тени", "Доспех бури", "Нагрудник феникса"},
            {"Латы дракона", "Мантия тьмы", "Броня паладина", "Доспех шторма", "Одеяние фантома"},
            {"Доспех Бездны", "Мантия вечности", "Латы звёзд", "Доспех титанов", "Плащ пламени"},
            {"Доспех богов", "Мантия Создателя", "Латы Судьбы", "Облачение бездны", "Доспех титанов"},
            {"Доспех Мироздания", "Мантия Первого света", "Латы Хаоса", "Облачение вечности", "Броня абсолюта"}
    };

    private static final String[][] GLOVES_NAMES = {
            {"Кожаные перчатки", "Потрётые рукавицы", "Простые перчатки"},
            {"Железные перчатки", "Перчатки дракона", "Перчатки тени", "Перчатки бури"},
            {"Перчатки дракона", "Перчатки тени", "Перчатки бури", "Перчатки феникса", "Перчатки пламени"},
            {"Перчатки Бездны", "Перчатки звёзд", "Перчатки титанов", "Перчатки вечности"},
            {"Перчатки богов", "Перчатки Создателя", "Перчатки Судьбы"},
            {"Перчатки Мироздания", "Перчатки Хаоса", "Перчатки абсолюта"}
    };

    private static final String[][] BOOTS_NAMES = {
            {"Старые сапоги", "Потрётые ботинки", "Простые сандалии"},
            {"Сапоги охотника", "Сапоги дракона", "Сапоги тени", "Сапоги бури"},
            {"Сапоги дракона", "Сапоги тени", "Сапоги бури", "Сапоги феникса", "Сапоги пламени"},
            {"Сапоги Бездны", "Сапоги звёзд", "Сапоги титанов", "Сапоги вечности"},
            {"Сапоги богов", "Сапоги Создателя", "Сапоги Судьбы"},
            {"Сапоги Мироздания", "Сапоги Хаоса", "Сапоги абсолюта"}
    };

    private static final String[][] RING_NAMES = {
            {"Медное кольцо", "Кольцо ученика", "Простое кольцо"},
            {"Серебряное кольцо", "Кольцо дракона", "Кольцо тени", "Кольцо бури"},
            {"Кольцо дракона", "Кольцо тени", "Кольцо бури", "Кольцо феникса", "Кольцо пламени"},
            {"Кольцо Бездны", "Кольцо звёзд", "Кольцо титанов", "Кольцо вечности"},
            {"Кольцо богов", "Кольцо Создателя", "Кольцо Судьбы"},
            {"Кольцо Мироздания", "Кольцо Хаоса", "Кольцо абсолюта"}
    };

    private static final String[][] AMULET_NAMES = {
            {"Амулет удачи", "Амулет защиты", "Простой амулет"},
            {"Амулет силы", "Амулет дракона", "Амулет тени", "Амулет бури"},
            {"Амулет дракона", "Амулет тьмы", "Амулет бури", "Амулет феникса", "Амулет пламени"},
            {"Амулет Бездны", "Амулет звёзд", "Амулет титанов", "Амулет вечности"},
            {"Амулет богов", "Амулет Создателя", "Амулет Судьбы"},
            {"Амулет Мироздания", "Амулет Хаоса", "Амулет абсолюта"}
    };

    private ItemFactory() {
    }

    public static Item generateDrop(int floor, Rarity minRarity, boolean bossDrop) {
        Rarity rarity = DropService.rollRarity(floor, minRarity, bossDrop);
        if (rarity == Rarity.MYTHIC && RandomUtil.chance(12)) {
            return generateRandomItem(floor, rarity, true);
        }
        if (rarity == Rarity.LEGENDARY && RandomUtil.chance(18)) {
            return generateRandomItem(floor, rarity, true);
        }
        return generateRandomItem(floor, rarity, false);
    }

    public static Item generateRandomItem(int floor, Rarity rarity) {
        return generateRandomItem(floor, rarity, false);
    }

    private static Item generateRandomItem(int floor, Rarity rarity, boolean unique) {
        int tier = rarity.ordinal();
        double statScale = unique ? 1.22 : 1.0;
        int baseStat = computeBaseStat(floor, rarity, statScale);

        Equipment.Slot[] slots = Equipment.Slot.values();
        Equipment.Slot slot = slots[RandomUtil.range(0, slots.length - 1)];

        double[] vamp = rollVampirism(rarity);
        if (unique && vamp[0] == 0 && rarity.ordinal() >= Rarity.RARE.ordinal()) {
            vamp = new double[]{5 + rarity.ordinal() * 1.5, 15 + rarity.ordinal() * 5};
        }

        if (slot == Equipment.Slot.WEAPON) {
            int atk = baseStat + tier * 3;
            double crit = tier >= 1 ? tier * 1.7 : 0;
            double critDmg = tier >= 2 ? tier * 0.15 : 0;
            double dodge = tier >= 1 && RandomUtil.chance(40) ? tier * 1.3 : 0;
            String name = formatItemName(pickDropName(Equipment.Slot.WEAPON, tier, rarity), unique);
            return new Weapon(name, rarity, atk, crit, critDmg, dodge, vamp[0], vamp[1]);
        }

        String name = formatItemName(pickDropName(slot, tier, rarity), unique);
        int def = slot == Equipment.Slot.ARMOR || slot == Equipment.Slot.HELMET ? baseStat : baseStat / 2;
        int hp = slot == Equipment.Slot.ARMOR ? baseStat : baseStat / 3;
        int atk = tier >= 2 && slot == Equipment.Slot.RING ? baseStat / 2 : 0;
        double crit = slot == Equipment.Slot.AMULET ? tier * 2.0 : 0;
        double critDmg = slot == Equipment.Slot.RING ? tier * 0.12 : 0;
        double dodge = slot == Equipment.Slot.BOOTS || slot == Equipment.Slot.GLOVES ? tier * 1.7 : 0;
        return new Armor(name, rarity, slot, atk, def, hp, crit, critDmg, dodge, vamp[0], vamp[1]);
    }

    /** Эпик+ — всегда сетовое имя; редкий — 55% шанс. */
    private static String pickDropName(Equipment.Slot slot, int tier, Rarity rarity) {
        boolean forceSet = rarity.ordinal() >= Rarity.EPIC.ordinal()
                || (rarity.ordinal() >= Rarity.RARE.ordinal() && RandomUtil.chance(55));
        if (forceSet) {
            String setName = pickNameForRandomSet(slot, tier, rarity);
            if (setName != null) {
                return setName;
            }
        }
        if (slot == Equipment.Slot.WEAPON) {
            return pickName(WEAPON_NAMES, tier);
        }
        return pickArmorName(slot, tier);
    }

    private static String pickNameForRandomSet(Equipment.Slot slot, int tier, Rarity rarity) {
        List<ItemSet> eligible = eligibleSetsForRarity(rarity);
        for (int attempt = 0; attempt < eligible.size() * 2; attempt++) {
            ItemSet set = eligible.get(RandomUtil.range(0, eligible.size() - 1));
            String name = findNameForSet(slot, tier, set, rarity);
            if (name != null) {
                return name;
            }
        }
        return findAnySetName(slot, tier, rarity);
    }

    private static List<ItemSet> eligibleSetsForRarity(Rarity rarity) {
        List<ItemSet> eligible = new ArrayList<>();
        for (ItemSet set : ItemSet.values()) {
            if (set == ItemSet.NONE) {
                continue;
            }
            if (set == ItemSet.CELESTIAL && rarity != Rarity.MYTHIC) {
                continue;
            }
            if (set == ItemSet.CHAOS && rarity.ordinal() < Rarity.LEGENDARY.ordinal()) {
                continue;
            }
            eligible.add(set);
        }
        return eligible;
    }

    private static String findNameForSet(Equipment.Slot slot, int tier, ItemSet set, Rarity rarity) {
        if (set == ItemSet.CELESTIAL && rarity == Rarity.MYTHIC) {
            return slot == Equipment.Slot.WEAPON ? pickName(WEAPON_NAMES, tier) : pickArmorName(slot, tier);
        }
        String[][] names = namesForSlot(slot);
        for (int t = Math.min(tier, names.length - 1); t >= 1; t--) {
            for (String name : names[t]) {
                if (matchesSet(name, set)) {
                    return name;
                }
            }
        }
        return null;
    }

    private static String findAnySetName(Equipment.Slot slot, int tier, Rarity rarity) {
        String[][] names = namesForSlot(slot);
        List<String> setNames = new ArrayList<>();
        for (int t = Math.min(tier, names.length - 1); t >= 1; t--) {
            for (String name : names[t]) {
                if (ItemSet.fromItemName(name) != ItemSet.NONE) {
                    setNames.add(name);
                } else if (rarity == Rarity.MYTHIC) {
                    setNames.add(name);
                }
            }
        }
        if (setNames.isEmpty()) {
            return null;
        }
        return setNames.get(RandomUtil.range(0, setNames.size() - 1));
    }

    private static boolean matchesSet(String name, ItemSet set) {
        return ItemSet.fromItemName(name) == set;
    }

    private static String[][] namesForSlot(Equipment.Slot slot) {
        return switch (slot) {
            case WEAPON -> WEAPON_NAMES;
            case HELMET -> HELMET_NAMES;
            case ARMOR -> ARMOR_NAMES;
            case GLOVES -> GLOVES_NAMES;
            case BOOTS -> BOOTS_NAMES;
            case RING -> RING_NAMES;
            case AMULET -> AMULET_NAMES;
        };
    }

    /** Гарантирует разрыв статов между редкостями на одном этаже. */
    private static int computeBaseStat(int floor, Rarity rarity, double statScale) {
        int floorPart = 6 + floor / 4;
        int scaled = (int) (floorPart * rarity.getStatMultiplier() * statScale);
        int rarityFloor = switch (rarity) {
            case COMMON -> 4;
            case RARE -> 12;
            case EPIC -> 22;
            case LEGENDARY -> 36;
            case MYTHIC -> 52;
        };
        rarityFloor += floor / 12;
        return Math.max(scaled, rarityFloor);
    }

    private static String formatItemName(String baseName, boolean unique) {
        return unique ? "★ " + baseName : baseName;
    }

    private static double[] rollVampirism(Rarity rarity) {
        if (rarity.ordinal() < Rarity.RARE.ordinal()) {
            return new double[]{0, 0};
        }
        int rollChance = 8 + rarity.ordinal() * 6;
        if (!RandomUtil.chance(rollChance)) {
            return new double[]{0, 0};
        }
        double chance = 3 + rarity.ordinal() * 1.5;
        double heal = 12 + rarity.ordinal() * 5;
        return new double[]{chance, heal};
    }

    private static String pickName(String[][] names, int tier) {
        String[] pool = names[Math.min(tier, names.length - 1)];
        return pool[RandomUtil.range(0, pool.length - 1)];
    }

    private static String pickArmorName(Equipment.Slot slot, int tier) {
        return switch (slot) {
            case HELMET -> pickName(HELMET_NAMES, tier);
            case ARMOR -> pickName(ARMOR_NAMES, tier);
            case GLOVES -> pickName(GLOVES_NAMES, tier);
            case BOOTS -> pickName(BOOTS_NAMES, tier);
            case RING -> pickName(RING_NAMES, tier);
            case AMULET -> pickName(AMULET_NAMES, tier);
            default -> pickName(ARMOR_NAMES, tier);
        };
    }
}
