package combat;

import enemy.Enemy;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Состояние героя в бою: кулдауны, баффы и дебаффы врагов. */
public class HeroCombatState {
    private int skill1Cooldown;
    private int skill2Cooldown;

    private int defBuffRounds;
    private int critImmuneRounds;
    private int frenzyRounds;

    /** Берсерк: «Последний рубеж» — 1 раз за забег. */
    private boolean lastStandUsed;

    private final Set<Enemy> warriorFirstHits = ConcurrentHashMap.newKeySet();
    private final Set<Enemy> firstStrikeTargets = ConcurrentHashMap.newKeySet();
    private final Set<Enemy> abyssWardFrom = ConcurrentHashMap.newKeySet();
    private final Map<Enemy, EnemyDebuffState> enemyDebuffs = new IdentityHashMap<>();

    private boolean shadowEmpowerReady;
    private int attackCounter;
    private int floorKillCount;
    private boolean phoenixAshUsed;
    private int phoenixAshBuffRounds;

    public void resetForEncounter() {
        skill1Cooldown = 0;
        skill2Cooldown = 0;
        defBuffRounds = 0;
        critImmuneRounds = 0;
        frenzyRounds = 0;
        warriorFirstHits.clear();
        firstStrikeTargets.clear();
        abyssWardFrom.clear();
        enemyDebuffs.clear();
        shadowEmpowerReady = false;
        attackCounter = 0;
        floorKillCount = 0;
    }

    public boolean hasFirstStrikeUsed(Enemy enemy) {
        return firstStrikeTargets.contains(enemy);
    }

    public void markFirstStrikeUsed(Enemy enemy) {
        firstStrikeTargets.add(enemy);
    }

    public boolean hasAbyssWardUsed(Enemy enemy) {
        return abyssWardFrom.contains(enemy);
    }

    public void markAbyssWardUsed(Enemy enemy) {
        abyssWardFrom.add(enemy);
    }

    public boolean isShadowEmpowerReady() {
        return shadowEmpowerReady;
    }

    public void enableShadowEmpower() {
        shadowEmpowerReady = true;
    }

    public void consumeShadowEmpower() {
        shadowEmpowerReady = false;
    }

    public int getAttackCounter() {
        return attackCounter;
    }

    public void incrementAttackCounter() {
        attackCounter++;
    }

    public int getFloorKillCount() {
        return floorKillCount;
    }

    public void incrementFloorKillCount() {
        floorKillCount++;
    }

    public boolean isPhoenixAshUsed() {
        return phoenixAshUsed;
    }

    public void markPhoenixAshUsed() {
        phoenixAshUsed = true;
    }

    public int getPhoenixAshBuffRounds() {
        return phoenixAshBuffRounds;
    }

    public void startPhoenixAshBuff(int rounds) {
        phoenixAshBuffRounds = Math.max(phoenixAshBuffRounds, rounds);
    }

    /** Новый забег — сброс одноразовых эффектов (капstone и т.д.). */
    public void resetForRun() {
        lastStandUsed = false;
        phoenixAshUsed = false;
        phoenixAshBuffRounds = 0;
        resetForEncounter();
    }

    public boolean isLastStandUsed() {
        return lastStandUsed;
    }

    public void markLastStandUsed() {
        lastStandUsed = true;
    }

    public int getSkill1Cooldown() {
        return skill1Cooldown;
    }

    public int getSkill2Cooldown() {
        return skill2Cooldown;
    }

    public boolean isSkill1Ready() {
        return skill1Cooldown <= 0;
    }

    public boolean isSkill2Ready() {
        return skill2Cooldown <= 0;
    }

    public void startSkill1Cooldown(int rounds) {
        skill1Cooldown = rounds;
    }

    public void startSkill2Cooldown(int rounds) {
        skill2Cooldown = rounds;
    }

    public int getDefBuffRounds() {
        return defBuffRounds;
    }

    public void addDefBuff(int rounds) {
        defBuffRounds = Math.max(defBuffRounds, rounds);
    }

    public int getCritImmuneRounds() {
        return critImmuneRounds;
    }

    public void addCritImmune(int rounds) {
        critImmuneRounds = Math.max(critImmuneRounds, rounds);
    }

    public int getFrenzyRounds() {
        return frenzyRounds;
    }

    public void startFrenzy(int rounds) {
        frenzyRounds = Math.max(frenzyRounds, rounds);
    }

    public boolean isInFrenzy() {
        return frenzyRounds > 0;
    }

    public boolean isWarriorFirstHitFrom(Enemy enemy) {
        return warriorFirstHits.contains(enemy);
    }

    public void markWarriorFirstHitFrom(Enemy enemy) {
        warriorFirstHits.add(enemy);
    }

    public EnemyDebuffState debuffFor(Enemy enemy) {
        return enemyDebuffs.computeIfAbsent(enemy, e -> new EnemyDebuffState());
    }

    public Map<Enemy, EnemyDebuffState> getEnemyDebuffs() {
        return enemyDebuffs;
    }

    public void endOfRound() {
        if (skill1Cooldown > 0) {
            skill1Cooldown--;
        }
        if (skill2Cooldown > 0) {
            skill2Cooldown--;
        }
        if (defBuffRounds > 0) {
            defBuffRounds--;
        }
        if (critImmuneRounds > 0) {
            critImmuneRounds--;
        }
        if (frenzyRounds > 0) {
            frenzyRounds--;
        }
        if (phoenixAshBuffRounds > 0) {
            phoenixAshBuffRounds--;
        }
        for (EnemyDebuffState debuff : enemyDebuffs.values()) {
            if (debuff.atkDebuffRounds > 0) {
                debuff.atkDebuffRounds--;
            }
            if (debuff.poisonRounds > 0) {
                debuff.poisonRounds--;
            }
        }
        enemyDebuffs.entrySet().removeIf(e -> !e.getKey().isAlive()
                || (e.getValue().atkDebuffRounds <= 0 && e.getValue().poisonRounds <= 0));
    }

    public static final class EnemyDebuffState {
        int atkDebuffRounds;
        double atkDebuffPercent;
        int poisonRounds;
        int poisonDamagePerRound;
    }
}
