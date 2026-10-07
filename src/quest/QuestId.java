package quest;



public enum QuestId {

    GOBLIN_HUNT("Твари у брода"),

    MILITIA_GEAR("Дар короны"),

    NIGHT_PACK("Ночь без огня"),

    BITTER_POTION("Отвар от искры"),

    FLOOR_20_REPORT("Доклад с рубежа-20"),

    CAVE_BONES("Кости охотников"),

    SWAMP_WHISPER("Шёпот Завесы"),

    MIST_SEAL("Печать тумана"),

    MINE_SHIFT("Первая смена"),

    GOLEM_GUARD("Каменный страж"),

    THREE_SPARKS("Три искры"),

    SOUL_SOCKET("Гнездо клинка"),
    BROKEN_WAGON("Разбитая повозка"),
    ROAD_RAIDERS("Орки на тракте"),
    STOLEN_CARGO("Клинки воров"),
    BOSS_FLOOR_20("Некромант рубежа");



    private final String title;



    QuestId(String title) {

        this.title = title;

    }



    public String getTitle() {

        return title;

    }

}

