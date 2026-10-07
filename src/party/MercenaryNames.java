package party;

import util.RandomUtil;

public final class MercenaryNames {
    private static final String[] POOL = {
            "Артур", "Лира", "Торин", "Элара", "Горн", "Сильва",
            "Кай", "Мира", "Дрен", "Вера", "Оскар", "Нова",
            "Рюрик", "Астра", "Бран", "Ива", "Лорен", "Финн"
    };

    private MercenaryNames() {
    }

    public static String randomName() {
        return POOL[RandomUtil.range(0, POOL.length - 1)];
    }
}
