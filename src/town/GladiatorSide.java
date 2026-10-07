package town;

import java.util.ArrayList;
import java.util.List;

public final class GladiatorSide {
    private final String label;
    private final String color;
    private final List<Gladiator> fighters;

    public GladiatorSide(String label, String color, List<Gladiator> fighters) {
        this.label = label;
        this.color = color;
        this.fighters = new ArrayList<>(fighters);
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }

    public List<Gladiator> getFighters() {
        return fighters;
    }

    public boolean isAlive() {
        return fighters.stream().anyMatch(Gladiator::isAlive);
    }

    public double powerRating() {
        return fighters.stream().mapToDouble(Gladiator::powerRating).sum();
    }

    public GladiatorSide copyForFight() {
        List<Gladiator> copies = new ArrayList<>();
        for (Gladiator g : fighters) {
            copies.add(g.copyForFight());
        }
        return new GladiatorSide(label, color, copies);
    }
}
