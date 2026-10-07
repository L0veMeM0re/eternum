package test;

import item.DropService;
import item.Item;
import item.ItemFactory;
import item.Rarity;
import enemy.EnemyScaling;
import enemy.EnemyType;
import enemy.BossType;
import player.Stats;

import java.util.EnumMap;
import java.util.Map;

/**
 * Симуляция 1M дропов: проверка шанса предмета и распределения редкости.
 * Запуск: {@code java -cp out test.DropBalanceTest}
 */
public class DropBalanceTest {
    private static final int SAMPLES = 1_000_000;
    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        System.out.println("=== DropBalanceTest (" + SAMPLES + " симуляций) ===\n");

        simulateItemDropRate();
        simulateRarityDistribution("Обычный моб (этаж 15)", 15, Rarity.COMMON, false);
        simulateRarityDistribution("Элитный моб (этаж 25)", 25, Rarity.EPIC, false);
        simulateRarityDistribution("Босс (этаж 10)", 10, Rarity.COMMON, true);
        simulateFullGenerateDrop("generateDrop этаж 15", 15, Rarity.COMMON, false);
        printEarlyMobStats();

        System.out.println();
        System.out.println("Итого: " + passed + " OK, " + failed + " FAIL");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void simulateItemDropRate() {
        int items = 0;
        int gold = 0;
        int nothing = 0;
        for (int i = 0; i < SAMPLES; i++) {
            switch (DropService.rollMobDrop(false, 0, 0)) {
                case ITEM -> items++;
                case BONUS_GOLD -> gold++;
                case NOTHING -> nothing++;
            }
        }
        double itemPct = items * 100.0 / SAMPLES;
        double goldPct = gold * 100.0 / SAMPLES;
        System.out.println("--- Шанс дропа с моба (dropLevel=0) ---");
        System.out.printf("  Предмет:  %8d (%6.3f%%)  цель ~25%%%n", items, itemPct);
        System.out.printf("  Золото:   %8d (%6.3f%%)  ~28%% если нет предмета%n", gold, goldPct);
        System.out.printf("  Пусто:    %8d (%6.3f%%)%n%n", nothing, nothing * 100.0 / SAMPLES);

        assertNear("Item drop ~25%", 25.0, itemPct, 0.35);
        assertNear("Bonus gold ~21%", 21.0, goldPct, 0.35);
    }

    private static void simulateRarityDistribution(String label, int floor, Rarity minRarity, boolean boss) {
        Map<Rarity, Integer> counts = new EnumMap<>(Rarity.class);
        for (int i = 0; i < SAMPLES; i++) {
            Rarity r = DropService.rollRarity(floor, minRarity, boss);
            counts.merge(r, 1, Integer::sum);
        }
        System.out.println("--- " + label + " ---");
        for (Rarity r : Rarity.values()) {
            int n = counts.getOrDefault(r, 0);
            double pct = n * 100.0 / SAMPLES;
            double expected = expectedPercent(r, minRarity, boss, floor);
            System.out.printf("  %-12s %8d (%8.4f%%)  ожид. ~%.4f%%%n",
                    r.getColorName(), n, pct, expected);
        }
        if (minRarity == Rarity.COMMON) {
            assertRarityRates(counts, boss);
        }
        System.out.println();
    }

    private static void assertRarityRates(Map<Rarity, Integer> counts, boolean boss) {
        int mythic = counts.getOrDefault(Rarity.MYTHIC, 0);
        int legendary = counts.getOrDefault(Rarity.LEGENDARY, 0);
        int epic = counts.getOrDefault(Rarity.EPIC, 0);
        int rare = counts.getOrDefault(Rarity.RARE, 0);
        double mythicPct = mythic * 100.0 / SAMPLES;
        double legPct = legendary * 100.0 / SAMPLES;
        double epicPct = epic * 100.0 / SAMPLES;
        double rarePct = rare * 100.0 / SAMPLES;

        if (boss) {
            assertNear("Boss mythic ~0.01%", 0.01, mythicPct, 0.008);
            assertNear("Boss legendary ~0.5%", 0.5, legPct, 0.05);
            assertNear("Boss epic ~5%", 5.0, epicPct, 0.15);
            assertNear("Boss rare ~20%", 20.0, rarePct, 0.2);
        } else {
            assertNear("Mob mythic ~0.001%", 0.001, mythicPct, 0.003);
            assertNear("Mob legendary ~0.1%", 0.1, legPct, 0.025);
            assertNear("Mob epic ~3%", 3.0, epicPct, 0.08);
            assertNear("Mob rare ~15%", 15.0, rarePct, 0.12);
        }
    }

    private static void simulateFullGenerateDrop(String label, int floor, Rarity minRarity, boolean boss) {
        Map<Rarity, Integer> counts = new EnumMap<>(Rarity.class);
        for (int i = 0; i < SAMPLES; i++) {
            Item item = ItemFactory.generateDrop(floor, minRarity, boss);
            counts.merge(item.getRarity(), 1, Integer::sum);
        }
        System.out.println("--- " + label + " (полная генерация) ---");
        for (Rarity r : Rarity.values()) {
            int n = counts.getOrDefault(r, 0);
            System.out.printf("  %-12s %8d (%6.3f%%)%n", r.getColorName(), n, n * 100.0 / SAMPLES);
        }
        int mythic = counts.getOrDefault(Rarity.MYTHIC, 0);
        int legendary = counts.getOrDefault(Rarity.LEGENDARY, 0);
        assertNear("Full gen mythic ~0.001%", 0.001, mythic * 100.0 / SAMPLES, 0.003);
        assertNear("Full gen legendary ~0.1%", 0.1, legendary * 100.0 / SAMPLES, 0.025);
        System.out.println();
    }

    private static double expectedPercent(Rarity rarity, Rarity minRarity, boolean boss, int floor) {
        double base = DropService.expectedRarityPercent(rarity, boss);
        if (rarity.ordinal() <= minRarity.ordinal()) {
            double sum = 0;
            for (Rarity r : Rarity.values()) {
                if (r.ordinal() < minRarity.ordinal()) {
                    sum += DropService.expectedRarityPercent(r, boss);
                }
            }
            if (rarity == minRarity) {
                return base + sum;
            }
            return base;
        }
        return base;
    }

    private static void printEarlyMobStats() {
        System.out.println("--- Статы мобов (этаж 1 / 15 / 30) ---");
        for (int floor : new int[]{1, 15, 30}) {
            Stats goblin = EnemyScaling.scaleMobStats(EnemyType.GOBLIN, floor);
            Stats boss = EnemyScaling.scaleBossStats(BossType.FLOOR_10, floor);
            System.out.printf("  Этаж %2d: goblin HP=%d ATK=%d DEF=%d | boss HP=%d ATK=%d DEF=%d%n",
                    floor, goblin.getMaxHp(), goblin.getAttack(), goblin.getDefense(),
                    boss.getMaxHp(), boss.getAttack(), boss.getDefense());
        }
        System.out.println();
    }

    private static void assertNear(String name, double expected, double actual, double tolerance) {
        if (Math.abs(expected - actual) <= tolerance) {
            passed++;
            System.out.println("  OK  " + name);
        } else {
            failed++;
            System.out.printf("  FAIL %s — expected ~%.4f%%, got %.4f%% (±%.4f)%n",
                    name, expected, actual, tolerance);
        }
    }
}
