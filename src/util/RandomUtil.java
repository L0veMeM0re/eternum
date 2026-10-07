package util;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomUtil {
    private static final Random RANDOM = ThreadLocalRandom.current();

    private RandomUtil() {
    }

    public static boolean chance(double percent) {
        return RANDOM.nextDouble() * 100.0 < percent;
    }

    public static int range(int minInclusive, int maxInclusive) {
        if (minInclusive >= maxInclusive) {
            return minInclusive;
        }
        return RANDOM.nextInt(maxInclusive - minInclusive + 1) + minInclusive;
    }

    public static <T> T pick(List<T> items) {
        return items.get(RANDOM.nextInt(items.size()));
    }
}
