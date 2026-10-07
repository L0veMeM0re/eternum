package player;

public class Stats {
    private int maxHp;
    private int currentHp;
    private int attack;
    private int defense;
    private double critChance;
    private double critDamage;
    private double dodgeChance;

    public Stats(int maxHp, int attack, int defense, double critChance, double critDamage, double dodgeChance) {
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.critChance = critChance;
        this.critDamage = critDamage;
        this.dodgeChance = dodgeChance;
    }

    public Stats copy() {
        Stats copy = new Stats(maxHp, attack, defense, critChance, critDamage, dodgeChance);
        copy.currentHp = currentHp;
        return copy;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
        if (currentHp > maxHp) {
            currentHp = maxHp;
        }
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public void setCurrentHp(int currentHp) {
        this.currentHp = Math.max(0, Math.min(currentHp, maxHp));
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public double getCritChance() {
        return critChance;
    }

    public void setCritChance(double critChance) {
        this.critChance = critChance;
    }

    public double getCritDamage() {
        return critDamage;
    }

    public void setCritDamage(double critDamage) {
        this.critDamage = critDamage;
    }

    public double getDodgeChance() {
        return dodgeChance;
    }

    public void setDodgeChance(double dodgeChance) {
        this.dodgeChance = dodgeChance;
    }

    public void addMaxHp(int amount) {
        maxHp += amount;
    }

    public void addAttack(int amount) {
        attack += amount;
    }

    public void addDefense(int amount) {
        defense += amount;
    }

    public void addCritChance(double amount) {
        critChance += amount;
    }

    public void addCritDamage(double amount) {
        critDamage += amount;
    }

    public void addDodgeChance(double amount) {
        dodgeChance += amount;
    }

    public void healFull() {
        currentHp = maxHp;
    }

    public boolean isAlive() {
        return currentHp > 0;
    }

    public void takeDamage(int damage) {
        currentHp = Math.max(0, currentHp - damage);
    }

    public int heal(int amount) {
        if (amount <= 0) {
            return 0;
        }
        int before = currentHp;
        currentHp = Math.min(maxHp, currentHp + amount);
        return currentHp - before;
    }
}
