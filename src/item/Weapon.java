package item;

import inventory.Equipment;
import util.GemRenderer;

import java.util.Arrays;

public class Weapon extends Item {
    private final int attackBonus;
    private final double critBonus;
    private final double critDamageBonus;
    private final double dodgeBonus;
    private final Gem[] socketedGems;

    public Weapon(String name, Rarity rarity, int attackBonus, double critBonus) {
        this(name, rarity, attackBonus, critBonus, 0, 0, 0, 0, null);
    }

    public Weapon(String name, Rarity rarity, int attackBonus, double critBonus,
                  double critDamageBonus, double dodgeBonus) {
        this(name, rarity, attackBonus, critBonus, critDamageBonus, dodgeBonus, 0, 0, null);
    }

    public Weapon(String name, Rarity rarity, int attackBonus, double critBonus,
                  double critDamageBonus, double dodgeBonus,
                  double vampirismChance, double vampirismHealPercent) {
        this(name, rarity, attackBonus, critBonus, critDamageBonus, dodgeBonus,
                vampirismChance, vampirismHealPercent, null);
    }

    public Weapon(String name, Rarity rarity, int attackBonus, double critBonus,
                  double critDamageBonus, double dodgeBonus,
                  double vampirismChance, double vampirismHealPercent,
                  Gem[] socketedGems) {
        super(name, rarity, calculateSellPrice(rarity, attackBonus),
                ItemSet.fromItemName(name), vampirismChance, vampirismHealPercent);
        this.attackBonus = attackBonus;
        this.critBonus = critBonus;
        this.critDamageBonus = critDamageBonus;
        this.dodgeBonus = dodgeBonus;
        int slots = maxSockets(rarity);
        this.socketedGems = new Gem[slots];
        if (socketedGems != null) {
            for (int i = 0; i < Math.min(slots, socketedGems.length); i++) {
                this.socketedGems[i] = socketedGems[i];
            }
        }
    }

    public static int maxSockets(Rarity rarity) {
        return switch (rarity) {
            case EPIC -> 1;
            case LEGENDARY -> 2;
            case MYTHIC -> 3;
            default -> 0;
        };
    }

    private static int calculateSellPrice(Rarity rarity, int attackBonus) {
        return (int) (attackBonus * 1.5 * rarity.getStatMultiplier());
    }

    public Gem[] getSocketedGems() {
        return socketedGems.clone();
    }

    public int socketCount() {
        return socketedGems.length;
    }

    public Gem getSocket(int index) {
        if (index < 0 || index >= socketedGems.length) {
            return null;
        }
        return socketedGems[index];
    }

    public boolean hasEmptySocket() {
        for (Gem gem : socketedGems) {
            if (gem == null) {
                return true;
            }
        }
        return false;
    }

    public int firstEmptySocketIndex() {
        for (int i = 0; i < socketedGems.length; i++) {
            if (socketedGems[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public boolean hasGemType(GemType type) {
        for (Gem gem : socketedGems) {
            if (gem != null && gem.getType() == type) {
                return true;
            }
        }
        return false;
    }

    public Weapon withSocket(int index, Gem gem) {
        Gem[] copy = socketedGems.clone();
        if (index >= 0 && index < copy.length) {
            copy[index] = gem;
        }
        return copyWeapon(copy);
    }

    public Weapon withSocketCleared(int index) {
        Gem[] copy = socketedGems.clone();
        if (index >= 0 && index < copy.length) {
            copy[index] = null;
        }
        return copyWeapon(copy);
    }

    private Weapon copyWeapon(Gem[] gems) {
        return new Weapon(getName(), getRarity(), attackBonus, critBonus, critDamageBonus, dodgeBonus,
                super.getVampirismChance(), super.getVampirismHealPercent(), gems);
    }

    /** Базовые статы предмета без камней (для сохранения). */
    public int getBaseAttackBonus() {
        return attackBonus;
    }

    public double getBaseCritBonus() {
        return critBonus;
    }

    public double getBaseCritDamageBonus() {
        return critDamageBonus;
    }

    public double getBaseDodgeBonus() {
        return dodgeBonus;
    }

    public double getBaseVampirismChance() {
        return super.getVampirismChance();
    }

    public double getBaseVampirismHealPercent() {
        return super.getVampirismHealPercent();
    }

    private int gemAtk() {
        int total = 0;
        for (Gem gem : socketedGems) {
            if (gem != null) {
                total += gem.atkBonus();
            }
        }
        return total;
    }

    private double gemCrit() {
        double total = 0;
        for (Gem gem : socketedGems) {
            if (gem != null) {
                total += gem.critBonus();
            }
        }
        return total;
    }

    private double gemCritDmg() {
        double total = 0;
        for (Gem gem : socketedGems) {
            if (gem != null) {
                total += gem.critDamageBonus();
            }
        }
        return total;
    }

    private int gemHp() {
        int total = 0;
        for (Gem gem : socketedGems) {
            if (gem != null) {
                total += gem.hpBonus();
            }
        }
        return total;
    }

    private double gemVamp() {
        double total = 0;
        for (Gem gem : socketedGems) {
            if (gem != null) {
                total += gem.vampChance();
            }
        }
        return total;
    }

    @Override
    public Equipment.Slot getSlot() {
        return Equipment.Slot.WEAPON;
    }

    @Override
    public String getStatDescription() {
        StringBuilder sb = new StringBuilder("ATK +" + getAttackBonus());
        appendStat(sb, "CRIT", getCritBonus(), "%");
        appendStat(sb, "CRIT DMG", getCritDamageBonus(), "x");
        appendStat(sb, "DODGE", dodgeBonus, "%");
        appendVamp(sb);
        appendSockets(sb);
        return sb.toString();
    }

    private void appendSockets(StringBuilder sb) {
        if (socketedGems.length == 0) {
            return;
        }
        sb.append(" | Gnёzda ").append(filledSockets()).append("/").append(socketedGems.length);
        for (Gem gem : socketedGems) {
            if (gem != null) {
                sb.append(" ").append(GemRenderer.formatSocket(gem));
            }
        }
    }

    private int filledSockets() {
        int n = 0;
        for (Gem gem : socketedGems) {
            if (gem != null) {
                n++;
            }
        }
        return n;
    }

    private void appendStat(StringBuilder sb, String label, double value, String suffix) {
        if (value > 0) {
            sb.append(", ").append(label).append(" +");
            if ("x".equals(suffix)) {
                sb.append(String.format("%.1f", value)).append(suffix);
            } else {
                sb.append((int) value).append(suffix);
            }
        }
    }

    private void appendVamp(StringBuilder sb) {
        double vamp = getVampirismChance();
        if (vamp > 0 && getVampirismHealPercent() > 0) {
            sb.append(", VAMP ").append((int) vamp).append("% (")
                    .append((int) getVampirismHealPercent()).append("% dmg)");
        } else if (gemVamp() > 0) {
            sb.append(", VAMP +").append((int) gemVamp()).append("%");
        }
    }

    @Override
    public int getAttackBonus() {
        return attackBonus + gemAtk();
    }

    @Override
    public int getDefenseBonus() {
        return 0;
    }

    @Override
    public double getCritBonus() {
        return critBonus + gemCrit();
    }

    @Override
    public double getCritDamageBonus() {
        return critDamageBonus + gemCritDmg();
    }

    @Override
    public double getDodgeBonus() {
        return dodgeBonus;
    }

    @Override
    public int getHpBonus() {
        return gemHp();
    }

    @Override
    public double getVampirismChance() {
        return super.getVampirismChance() + gemVamp();
    }

    @Override
    public double getVampirismHealPercent() {
        double base = super.getVampirismHealPercent();
        if (base > 0) {
            return base;
        }
        if (gemVamp() > 0) {
            return 5.0;
        }
        return 0;
    }

    public boolean socketArraysEqual(Gem[] other) {
        return Arrays.equals(socketedGems, other);
    }
}
