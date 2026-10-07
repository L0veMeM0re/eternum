package item;

/** Уникальная механика сета (пороги 4 и 6). */
public enum ItemSetAbility {
    DRAGON_FIRST_STRIKE("Жар крови", "первый удар по каждому врагу: +25% урона"),
    DRAGON_FURY("Драконья ярость", "при HP ниже 40%: +20% ATK"),
    SHADOW_EMPOWER("Из тени", "после уворота следующая атака: +40% урона"),
    SHADOW_VAMP_DOT("Поглощение", "яд по врагам лечит носителя на 30% от урона яда"),
    STORM_CHAIN("Разряд", "каждая 3-я атака: удар по 2-му врагу за 50% урона"),
    STORM_KILLSTRIKE("Молния", "при убийстве: 20% шанс доп. удара по другому врагу"),
    ABYSS_WARD("Тёмная броня", "первый удар каждого врага по вам: −30% урона"),
    ABYSS_DEEP("Погружение", "+3% ATK за каждые 10% недостающего HP (макс. 27%)"),
    PHOENIX_EMBER("Искра", "после этажа: +5% HP"),
    PHOENIX_ASH("Пепел", "1 раз за забег: смертельный удар оставляет 1 HP (+15% ATK на 3 хода)"),
    TITAN_STANCE("Стойка", "−12% получаемого урона"),
    TITAN_BULWARK("Оплот", "15% урона перенаправляется на наёмника с наибольшим HP"),
    CHAOS_DOUBLE("Искажение", "12% шанс: атака попадает дважды за 60% урона"),
    CHAOS_MOMENTUM("Хаос растёт", "+2% ATK за каждого убитого на этаже (макс. 20%)"),
    CELESTIAL_RESONANCE("Созвездный резонанс", "+10% опыта за забег"),
    CELESTIAL_AEGIS("Звёздный щит", "раз в 5 этажей: блокирует один смертельный удар");

    public final String name;
    public final String description;

    ItemSetAbility(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String formatLine() {
        return name + " — " + description;
    }
}
