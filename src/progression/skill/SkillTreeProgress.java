package progression.skill;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/** Прогресс узлов дерева: id узла → текущий ранг (0 = не куплено). */
public class SkillTreeProgress {
    private final Map<String, Integer> ranks = new HashMap<>();

    public int getRank(String nodeId) {
        return ranks.getOrDefault(nodeId, 0);
    }

    public void setRank(String nodeId, int rank) {
        if (rank <= 0) {
            ranks.remove(nodeId);
        } else {
            ranks.put(nodeId, rank);
        }
    }

    public int totalSpent() {
        return ranks.values().stream().mapToInt(Integer::intValue).sum();
    }

    public Map<String, Integer> snapshot() {
        return new HashMap<>(ranks);
    }

    public void loadSnapshot(Map<String, Integer> data) {
        ranks.clear();
        if (data != null) {
            ranks.putAll(data);
        }
    }

    public void clear() {
        ranks.clear();
    }

    /** Прогресс героя по каждому классу. */
    public static class PlayerTrees {
        private final EnumMap<player.CharacterClass, SkillTreeProgress> byClass =
                new EnumMap<>(player.CharacterClass.class);

        public SkillTreeProgress forClass(player.CharacterClass cls) {
            return byClass.computeIfAbsent(cls, c -> new SkillTreeProgress());
        }

        public int totalSpentAllClasses() {
            return byClass.values().stream().mapToInt(SkillTreeProgress::totalSpent).sum();
        }

        public Map<String, Map<String, Integer>> snapshot() {
            Map<String, Map<String, Integer>> out = new HashMap<>();
            for (var e : byClass.entrySet()) {
                out.put(e.getKey().name(), e.getValue().snapshot());
            }
            return out;
        }

        public void loadSnapshot(Map<String, Map<String, Integer>> data) {
            byClass.clear();
            if (data == null) {
                return;
            }
            for (var e : data.entrySet()) {
                try {
                    player.CharacterClass cls = player.CharacterClass.valueOf(e.getKey());
                    SkillTreeProgress p = new SkillTreeProgress();
                    p.loadSnapshot(e.getValue());
                    byClass.put(cls, p);
                } catch (IllegalArgumentException ignored) {
                    // skip unknown class
                }
            }
        }
    }
}
