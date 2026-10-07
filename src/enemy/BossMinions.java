package enemy;

import java.util.List;

public final class BossMinions {
    private BossMinions() {
    }

    public static List<EnemyType> typesFor(BossType boss) {
        return switch (boss) {
            case FLOOR_10 -> List.of(EnemyType.GOBLIN, EnemyType.GOBLIN, EnemyType.WOLF);
            case FLOOR_20 -> List.of(EnemyType.SKELETON, EnemyType.GHOST, EnemyType.BAT);
            case FLOOR_30 -> List.of(EnemyType.ORC, EnemyType.ELEMENTAL, EnemyType.WOLF);
            case FLOOR_40 -> List.of(EnemyType.SKELETON, EnemyType.WITCH, EnemyType.GHOST);
            case FLOOR_50 -> List.of(EnemyType.DEMON, EnemyType.DEMON, EnemyType.ELEMENTAL);
            case FLOOR_60 -> List.of(EnemyType.SLIME, EnemyType.SLIME, EnemyType.SPIDER);
            case FLOOR_70 -> List.of(EnemyType.GOLEM, EnemyType.ORC, EnemyType.ELEMENTAL);
            case FLOOR_80 -> List.of(EnemyType.GHOST, EnemyType.WITCH, EnemyType.BAT);
            case FLOOR_90 -> List.of(EnemyType.DEMON, EnemyType.ELEMENTAL, EnemyType.GHOST);
            case FLOOR_100 -> List.of(EnemyType.DEMON, EnemyType.GOLEM, EnemyType.ELEMENTAL, EnemyType.WITCH);
        };
    }

    public static String minionTitle(BossType boss, EnemyType type) {
        return switch (boss) {
            case FLOOR_10 -> type == EnemyType.GOBLIN ? "Страж гоблинов" : "Волк короля";
            case FLOOR_20 -> type == EnemyType.SKELETON ? "Костяной слуга" : "Тень некроманта";
            case FLOOR_30 -> "Драконий прислужник";
            case FLOOR_40 -> "Слуга лича";
            case FLOOR_50 -> "Демон-страж";
            case FLOOR_60 -> "Головастик гидры";
            case FLOOR_70 -> "Каменный стражник";
            case FLOOR_80 -> "Вампирский слуга";
            case FLOOR_90 -> "Страж бездны";
            case FLOOR_100 -> "Приспешник тьмы";
        };
    }
}
