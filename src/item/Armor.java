package item;

import inventory.Equipment;

public class Armor extends Item {
    private final Equipment.Slot slot;
    private final int defenseBonus;
    private final int hpBonus;
    private final int attackBonus;
    private final double critBonus;
    private final double critDamageBonus;
    private final double dodgeBonus;

    public Armor(String name, Rarity rarity, Equipment.Slot slot, int defenseBonus, int hpBonus) {
        this(name, rarity, slot, 0, defenseBonus, hpBonus, 0, 0, 0, 0, 0);
    }

    public Armor(String name, Rarity rarity, Equipment.Slot slot, int attackBonus, int defenseBonus,
                 int hpBonus, double critBonus, double critDamageBonus, double dodgeBonus) {
        this(name, rarity, slot, attackBonus, defenseBonus, hpBonus, critBonus, critDamageBonus, dodgeBonus, 0, 0);
    }

    public Armor(String name, Rarity rarity, Equipment.Slot slot, int attackBonus, int defenseBonus,
                 int hpBonus, double critBonus, double critDamageBonus, double dodgeBonus,
                 double vampirismChance, double vampirismHealPercent) {
        super(name, rarity, calculateSellPrice(rarity, attackBonus, defenseBonus, hpBonus),
                ItemSet.fromItemName(name), vampirismChance, vampirismHealPercent);
        this.slot = slot;
        this.attackBonus = attackBonus;
        this.defenseBonus = defenseBonus;
        this.hpBonus = hpBonus;
        this.critBonus = critBonus;
        this.critDamageBonus = critDamageBonus;
        this.dodgeBonus = dodgeBonus;
    }

    private static int calculateSellPrice(Rarity rarity, int attack, int defense, int hp) {
        return (int) ((attack * 1.5 + defense * 1.0 + hp * 0.5) * rarity.getStatMultiplier());
    }

    @Override
    public Equipment.Slot getSlot() {
        return slot;
    }

    @Override
    public String getStatDescription() {
        StringBuilder sb = new StringBuilder();
        appendInt(sb, "ATK", attackBonus);
        appendInt(sb, "DEF", defenseBonus);
        appendInt(sb, "HP", hpBonus);
        appendDouble(sb, "CRIT", critBonus, "%");
        appendDouble(sb, "CRIT DMG", critDamageBonus, "x");
        appendDouble(sb, "DODGE", dodgeBonus, "%");
        if (hasVampirism()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("ВАМП +").append((int) getVampirismChance()).append("% (")
                    .append((int) getVampirismHealPercent()).append("% урона)");
        }
        return sb.toString();
    }

    private void appendInt(StringBuilder sb, String label, int value) {
        if (value > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(label).append(" +").append(value);
        }
    }

    private void appendDouble(StringBuilder sb, String label, double value, String suffix) {
        if (value > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(label).append(" +");
            if ("x".equals(suffix)) {
                sb.append(String.format("%.1f", value)).append(suffix);
            } else {
                sb.append((int) value).append(suffix);
            }
        }
    }

    @Override
    public int getAttackBonus() {
        return attackBonus;
    }

    @Override
    public int getDefenseBonus() {
        return defenseBonus;
    }

    @Override
    public double getCritBonus() {
        return critBonus;
    }

    @Override
    public double getCritDamageBonus() {
        return critDamageBonus;
    }

    @Override
    public double getDodgeBonus() {
        return dodgeBonus;
    }

    @Override
    public int getHpBonus() {
        return hpBonus;
    }
}
