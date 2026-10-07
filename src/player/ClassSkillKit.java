package player;

/** Пассивка и два активных умения класса героя. */
public final class ClassSkillKit {
    private final String passiveName;
    private final String passiveDescription;
    private final String skill1Name;
    private final int skill1Cooldown;
    private final String skill1Description;
    private final String skill2Name;
    private final int skill2Cooldown;
    private final String skill2Description;

    public ClassSkillKit(String passiveName, String passiveDescription,
                         String skill1Name, int skill1Cooldown, String skill1Description,
                         String skill2Name, int skill2Cooldown, String skill2Description) {
        this.passiveName = passiveName;
        this.passiveDescription = passiveDescription;
        this.skill1Name = skill1Name;
        this.skill1Cooldown = skill1Cooldown;
        this.skill1Description = skill1Description;
        this.skill2Name = skill2Name;
        this.skill2Cooldown = skill2Cooldown;
        this.skill2Description = skill2Description;
    }

    public String getPassiveName() {
        return passiveName;
    }

    public String getPassiveDescription() {
        return passiveDescription;
    }

    public String getSkill1Name() {
        return skill1Name;
    }

    public int getSkill1Cooldown() {
        return skill1Cooldown;
    }

    public String getSkill1Description() {
        return skill1Description;
    }

    public String getSkill2Name() {
        return skill2Name;
    }

    public int getSkill2Cooldown() {
        return skill2Cooldown;
    }

    public String getSkill2Description() {
        return skill2Description;
    }

    public static ClassSkillKit forClass(CharacterClass cls) {
        return switch (cls) {
            case WARRIOR -> new ClassSkillKit(
                    "Стойкость", "Первый удар каждого врага −28% урона",
                    "Щитовой удар", 3, "135% ATK, +18% DEF на 2 хода",
                    "Непоколебимость", 5, "При HP<40%: +18% max HP, иммунитет к криту 1 ход");
            case MAGE -> new ClassSkillKit(
                    "Арканный резонанс", "+0.4 к множителю крит-урона",
                    "Огненная волна", 4, "70% ATK по всем врагам, игнор 50% DEF",
                    "Ледяной оков", 3, "120% ATK, −20% ATK врага на 2 хода");
            case ROGUE -> new ClassSkillKit(
                    "В уязвимое место", "+8% крит; крит лечит на 5% max HP",
                    "Финт", 3, "140% ATK, не уклоняется, +25% крит",
                    "Ядовитый клинок", 5, "100% ATK + яд 3 хода (30% ATK/ход)");
            case CLERIC -> new ClassSkillKit(
                    "Благословение", "Отряд +10% max HP",
                    "Исцеление", 4, "Лечит союзника с наименьшим HP на 20% max HP",
                    "Святой удар", 3, "110% ATK, 50% вампиризм от урона");
            case BERSERKER -> new ClassSkillKit(
                    "Ярость крови", "+1.75% ATK за каждые 5% недостающего HP (макс. +32%)",
                    "Кровавый рывок", 4, "165% ATK, −9% текущего HP",
                    "Безумие", 6, "3 хода: +25% ATK, −17% DEF, двойной удар");
        };
    }
}
