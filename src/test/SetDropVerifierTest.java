package test;

import item.Item;
import item.ItemFactory;
import item.ItemSet;
import item.Rarity;

import java.util.EnumMap;
import java.util.Map;

/** Симуляция дропа: проверка доли сетовых предметов. */
public class SetDropVerifierTest {
    private static final int SAMPLES = 8000;

    public static void main(String[] args) {
        simulate("Обычный моб (этаж 10)", 10, Rarity.COMMON, false);
        simulate("Обычный моб (этаж 40)", 40, Rarity.RARE, false);
        simulate("Элитный моб (этаж 25)", 25, Rarity.EPIC, false);
        simulate("Босс (этаж 50)", 50, Rarity.COMMON, true);
    }

    private static void simulate(String label, int floor, Rarity minRarity, boolean boss) {
        Map<ItemSet, Integer> counts = new EnumMap<>(ItemSet.class);
        int setItems = 0;
        for (int i = 0; i < SAMPLES; i++) {
            Item item = ItemFactory.generateDrop(floor, minRarity, boss);
            ItemSet set = item.getItemSet();
            counts.merge(set, 1, Integer::sum);
            if (set != ItemSet.NONE) {
                setItems++;
            }
        }
        System.out.println("\n=== " + label + " (" + SAMPLES + " предметов) ===");
        System.out.printf("Сетовых: %d (%.1f%%)%n", setItems, setItems * 100.0 / SAMPLES);
        for (ItemSet set : ItemSet.values()) {
            if (set == ItemSet.NONE) {
                continue;
            }
            int n = counts.getOrDefault(set, 0);
            if (n > 0) {
                System.out.printf("  %-12s %4d (%.1f%%)%n", set.getDisplayName(), n, n * 100.0 / SAMPLES);
            }
        }
        long distinctSets = counts.entrySet().stream()
                .filter(e -> e.getKey() != ItemSet.NONE && e.getValue() > 0)
                .count();
        System.out.println("Разных сетов: " + distinctSets);
    }
}
