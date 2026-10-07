package quest;



import java.util.EnumSet;

import java.util.Set;



public class QuestLog {

    public static final int GOBLIN_TARGET = 10;

    public static final int WEAPONS_TARGET = 5;

    public static final int ARMOR_TARGET = 5;

    public static final int WOLF_TARGET = 8;

    public static final int BAT_TARGET = 8;

    public static final int ORE_TARGET = 20;

    public static final int FLOOR_20_TARGET = 20;

    public static final int SKELETON_TARGET = 10;

    public static final int WITCH_TARGET = 6;

    public static final int MINE_ORE_TARGET = 30;

    public static final int SMELT_TARGET = 3;
    public static final int ROAD_WAGON_ORE = 10;
    public static final int ROAD_ORC_TARGET = 12;
    public static final int ROAD_WEAPON_TARGET = 3;



    private final Set<QuestId> completed = EnumSet.noneOf(QuestId.class);

    private boolean goblinHuntAccepted;

    private int goblinKills;

    private boolean militiaUnlocked;

    private boolean militiaQuestAccepted;

    private int weaponsDelivered;

    private int armorDelivered;

    private boolean nightPackAccepted;

    private int wolfKills;

    private int batKills;

    private boolean healerUnlocked;

    private boolean bitterPotionAccepted;

    private boolean floor20ReportAccepted;

    private boolean floor20ReachedInRun;

    private boolean caveBonesAccepted;

    private int skeletonKills;

    private boolean hermitUnlocked;

    private boolean swampWhisperAccepted;

    private int witchKills;

    private boolean mistSealAccepted;

    private boolean mineShiftAccepted;

    private boolean golemGuardAccepted;

    private boolean golemDefeatedForQuest;

    private boolean forgeMasterUnlocked;

    private boolean threeSparksAccepted;

    private int successfulSmelts;

    private boolean soulSocketAccepted;

    private boolean gemInsertedForQuest;
    private boolean starostaUnlocked;
    private boolean brokenWagonAccepted;
    private boolean roadRaidersAccepted;
    private int roadOrcKills;
    private boolean stolenCargoAccepted;
    private int roadWeaponsDelivered;
    private boolean bossFloor20Accepted;
    private boolean bossFloor20Defeated;

    public boolean isCompleted(QuestId id) {

        return completed.contains(id);

    }



    public void markCompleted(QuestId id) {

        completed.add(id);

    }



    public Set<QuestId> getCompleted() {

        return EnumSet.copyOf(completed);

    }



    public boolean isGoblinHuntAccepted() {

        return goblinHuntAccepted;

    }



    public void acceptGoblinHunt() {

        goblinHuntAccepted = true;

    }



    public int getGoblinKills() {

        return goblinKills;

    }



    public void addGoblinKill() {

        if (goblinHuntAccepted && !isCompleted(QuestId.GOBLIN_HUNT)) {

            goblinKills = Math.min(GOBLIN_TARGET, goblinKills + 1);

        }

    }



    public boolean isGoblinHuntReady() {

        return goblinHuntAccepted && !isCompleted(QuestId.GOBLIN_HUNT) && goblinKills >= GOBLIN_TARGET;

    }



    public boolean isMilitiaUnlocked() {

        return militiaUnlocked;

    }



    public void unlockMilitia() {

        militiaUnlocked = true;

    }



    public boolean isMilitiaQuestAccepted() {

        return militiaQuestAccepted;

    }



    public void acceptMilitiaQuest() {

        militiaQuestAccepted = true;

    }



    public int getWeaponsDelivered() {

        return weaponsDelivered;

    }



    public int getArmorDelivered() {

        return armorDelivered;

    }



    public void deliverWeapon() {

        if (militiaQuestAccepted && !isCompleted(QuestId.MILITIA_GEAR)) {

            weaponsDelivered = Math.min(WEAPONS_TARGET, weaponsDelivered + 1);

        }

    }



    public void deliverArmor() {

        if (militiaQuestAccepted && !isCompleted(QuestId.MILITIA_GEAR)) {

            armorDelivered = Math.min(ARMOR_TARGET, armorDelivered + 1);

        }

    }



    public boolean isMilitiaGearReady() {

        return militiaQuestAccepted

                && !isCompleted(QuestId.MILITIA_GEAR)

                && weaponsDelivered >= WEAPONS_TARGET

                && armorDelivered >= ARMOR_TARGET;

    }



    public boolean isNightPackAccepted() {

        return nightPackAccepted;

    }



    public void acceptNightPack() {

        nightPackAccepted = true;

    }



    public int getWolfKills() {

        return wolfKills;

    }



    public int getBatKills() {

        return batKills;

    }



    public void addWolfKill() {

        if (nightPackAccepted && !isCompleted(QuestId.NIGHT_PACK)) {

            wolfKills = Math.min(WOLF_TARGET, wolfKills + 1);

        }

    }



    public void addBatKill() {

        if (nightPackAccepted && !isCompleted(QuestId.NIGHT_PACK)) {

            batKills = Math.min(BAT_TARGET, batKills + 1);

        }

    }



    public boolean isNightPackReady() {

        return nightPackAccepted

                && !isCompleted(QuestId.NIGHT_PACK)

                && wolfKills >= WOLF_TARGET

                && batKills >= BAT_TARGET;

    }



    public boolean isHealerUnlocked() {

        return healerUnlocked;

    }



    public void unlockHealer() {

        healerUnlocked = true;

    }



    public boolean isBitterPotionAccepted() {

        return bitterPotionAccepted;

    }



    public void acceptBitterPotion() {

        bitterPotionAccepted = true;

    }



    public boolean isBitterPotionReady(int rawOre) {

        return bitterPotionAccepted

                && !isCompleted(QuestId.BITTER_POTION)

                && rawOre >= ORE_TARGET;

    }



    public boolean isFloor20ReportAccepted() {

        return floor20ReportAccepted;

    }



    public void acceptFloor20Report() {

        floor20ReportAccepted = true;

        floor20ReachedInRun = false;

    }



    public boolean isFloor20ReachedInRun() {

        return floor20ReachedInRun;

    }



    public void markFloor20ReachedInRun() {

        if (floor20ReportAccepted && !isCompleted(QuestId.FLOOR_20_REPORT)) {

            floor20ReachedInRun = true;

        }

    }



    public boolean isFloor20ReportReady() {

        return floor20ReportAccepted

                && !isCompleted(QuestId.FLOOR_20_REPORT)

                && floor20ReachedInRun;

    }



    public boolean isCaveBonesAccepted() {

        return caveBonesAccepted;

    }



    public void acceptCaveBones() {

        caveBonesAccepted = true;

    }



    public int getSkeletonKills() {

        return skeletonKills;

    }



    public void addSkeletonKill() {

        if (caveBonesAccepted && !isCompleted(QuestId.CAVE_BONES)) {

            skeletonKills = Math.min(SKELETON_TARGET, skeletonKills + 1);

        }

    }



    public boolean isCaveBonesReady() {

        return caveBonesAccepted

                && !isCompleted(QuestId.CAVE_BONES)

                && skeletonKills >= SKELETON_TARGET;

    }



    public boolean isHermitUnlocked() {

        return hermitUnlocked;

    }



    public void unlockHermit() {

        hermitUnlocked = true;

    }



    public boolean isSwampWhisperAccepted() {

        return swampWhisperAccepted;

    }



    public void acceptSwampWhisper() {

        swampWhisperAccepted = true;

    }



    public int getWitchKills() {

        return witchKills;

    }



    public void addWitchKill() {

        if (swampWhisperAccepted && !isCompleted(QuestId.SWAMP_WHISPER)) {

            witchKills = Math.min(WITCH_TARGET, witchKills + 1);

        }

    }



    public boolean isSwampWhisperReady() {

        return swampWhisperAccepted

                && !isCompleted(QuestId.SWAMP_WHISPER)

                && witchKills >= WITCH_TARGET;

    }



    public boolean isMistSealAccepted() {

        return mistSealAccepted;

    }



    public void acceptMistSeal() {

        mistSealAccepted = true;

    }



    public boolean isMineShiftAccepted() {

        return mineShiftAccepted;

    }



    public void acceptMineShift() {

        mineShiftAccepted = true;

    }



    public boolean isMineShiftReady(int rawOre) {

        return mineShiftAccepted

                && !isCompleted(QuestId.MINE_SHIFT)

                && rawOre >= MINE_ORE_TARGET;

    }



    public boolean isGolemGuardAccepted() {

        return golemGuardAccepted;

    }



    public void acceptGolemGuard() {

        golemGuardAccepted = true;

    }



    public boolean isGolemDefeatedForQuest() {

        return golemDefeatedForQuest;

    }



    public void markGolemDefeatedForQuest() {

        if (golemGuardAccepted && !isCompleted(QuestId.GOLEM_GUARD)) {

            golemDefeatedForQuest = true;

        }

    }



    public boolean isGolemGuardReady() {

        return golemGuardAccepted

                && !isCompleted(QuestId.GOLEM_GUARD)

                && golemDefeatedForQuest;

    }



    public boolean isForgeMasterUnlocked() {

        return forgeMasterUnlocked;

    }



    public void unlockForgeMaster() {

        forgeMasterUnlocked = true;

    }



    public boolean isThreeSparksAccepted() {

        return threeSparksAccepted;

    }



    public void acceptThreeSparks() {

        threeSparksAccepted = true;

    }



    public int getSuccessfulSmelts() {

        return successfulSmelts;

    }



    public void addSuccessfulSmelt() {

        if (threeSparksAccepted && !isCompleted(QuestId.THREE_SPARKS)) {

            successfulSmelts = Math.min(SMELT_TARGET, successfulSmelts + 1);

        }

    }



    public boolean isThreeSparksReady() {

        return threeSparksAccepted

                && !isCompleted(QuestId.THREE_SPARKS)

                && successfulSmelts >= SMELT_TARGET;

    }



    public boolean isSoulSocketAccepted() {

        return soulSocketAccepted;

    }



    public void acceptSoulSocket() {

        soulSocketAccepted = true;

    }



    public boolean isGemInsertedForQuest() {

        return gemInsertedForQuest;

    }



    public void markGemInsertedForQuest() {

        if (soulSocketAccepted && !isCompleted(QuestId.SOUL_SOCKET)) {

            gemInsertedForQuest = true;

        }

    }



    public boolean isSoulSocketReady() {

        return soulSocketAccepted

                && !isCompleted(QuestId.SOUL_SOCKET)

                && gemInsertedForQuest;

    }



    public void loadCompleted(Set<QuestId> ids) {

        completed.clear();

        if (ids != null) {

            completed.addAll(ids);

        }

    }



    public void setGoblinHuntAccepted(boolean goblinHuntAccepted) {

        this.goblinHuntAccepted = goblinHuntAccepted;

    }



    public void setGoblinKills(int goblinKills) {

        this.goblinKills = Math.max(0, goblinKills);

    }



    public void setMilitiaUnlocked(boolean militiaUnlocked) {

        this.militiaUnlocked = militiaUnlocked;

    }



    public void setMilitiaQuestAccepted(boolean militiaQuestAccepted) {

        this.militiaQuestAccepted = militiaQuestAccepted;

    }



    public void setWeaponsDelivered(int weaponsDelivered) {

        this.weaponsDelivered = Math.max(0, weaponsDelivered);

    }



    public void setArmorDelivered(int armorDelivered) {

        this.armorDelivered = Math.max(0, armorDelivered);

    }



    public void setNightPackAccepted(boolean nightPackAccepted) {

        this.nightPackAccepted = nightPackAccepted;

    }



    public void setWolfKills(int wolfKills) {

        this.wolfKills = Math.max(0, wolfKills);

    }



    public void setBatKills(int batKills) {

        this.batKills = Math.max(0, batKills);

    }



    public void setHealerUnlocked(boolean healerUnlocked) {

        this.healerUnlocked = healerUnlocked;

    }



    public void setBitterPotionAccepted(boolean bitterPotionAccepted) {

        this.bitterPotionAccepted = bitterPotionAccepted;

    }



    public void setFloor20ReportAccepted(boolean floor20ReportAccepted) {

        this.floor20ReportAccepted = floor20ReportAccepted;

    }



    public void setFloor20ReachedInRun(boolean floor20ReachedInRun) {

        this.floor20ReachedInRun = floor20ReachedInRun;

    }



    public void setCaveBonesAccepted(boolean caveBonesAccepted) {

        this.caveBonesAccepted = caveBonesAccepted;

    }



    public void setSkeletonKills(int skeletonKills) {

        this.skeletonKills = Math.max(0, skeletonKills);

    }



    public void setHermitUnlocked(boolean hermitUnlocked) {

        this.hermitUnlocked = hermitUnlocked;

    }



    public void setSwampWhisperAccepted(boolean swampWhisperAccepted) {

        this.swampWhisperAccepted = swampWhisperAccepted;

    }



    public void setWitchKills(int witchKills) {

        this.witchKills = Math.max(0, witchKills);

    }



    public void setMistSealAccepted(boolean mistSealAccepted) {

        this.mistSealAccepted = mistSealAccepted;

    }



    public void setMineShiftAccepted(boolean mineShiftAccepted) {

        this.mineShiftAccepted = mineShiftAccepted;

    }



    public void setGolemGuardAccepted(boolean golemGuardAccepted) {

        this.golemGuardAccepted = golemGuardAccepted;

    }



    public void setGolemDefeatedForQuest(boolean golemDefeatedForQuest) {

        this.golemDefeatedForQuest = golemDefeatedForQuest;

    }



    public void setForgeMasterUnlocked(boolean forgeMasterUnlocked) {

        this.forgeMasterUnlocked = forgeMasterUnlocked;

    }



    public void setThreeSparksAccepted(boolean threeSparksAccepted) {

        this.threeSparksAccepted = threeSparksAccepted;

    }



    public void setSuccessfulSmelts(int successfulSmelts) {

        this.successfulSmelts = Math.max(0, successfulSmelts);

    }



    public void setSoulSocketAccepted(boolean soulSocketAccepted) {

        this.soulSocketAccepted = soulSocketAccepted;

    }



    public void setGemInsertedForQuest(boolean gemInsertedForQuest) {

        this.gemInsertedForQuest = gemInsertedForQuest;

    }

    public boolean isStarostaUnlocked() {
        return starostaUnlocked;
    }

    public void unlockStarosta() {
        starostaUnlocked = true;
    }

    public boolean isBrokenWagonAccepted() {
        return brokenWagonAccepted;
    }

    public void acceptBrokenWagon() {
        brokenWagonAccepted = true;
    }

    public boolean isBrokenWagonReady(int rawOre) {
        return brokenWagonAccepted
                && !isCompleted(QuestId.BROKEN_WAGON)
                && rawOre >= ROAD_WAGON_ORE;
    }

    public boolean isRoadRaidersAccepted() {
        return roadRaidersAccepted;
    }

    public void acceptRoadRaiders() {
        roadRaidersAccepted = true;
    }

    public int getRoadOrcKills() {
        return roadOrcKills;
    }

    public void addRoadOrcKill() {
        if (roadRaidersAccepted && !isCompleted(QuestId.ROAD_RAIDERS)) {
            roadOrcKills = Math.min(ROAD_ORC_TARGET, roadOrcKills + 1);
        }
    }

    public boolean isRoadRaidersReady() {
        return roadRaidersAccepted
                && !isCompleted(QuestId.ROAD_RAIDERS)
                && roadOrcKills >= ROAD_ORC_TARGET;
    }

    public boolean isStolenCargoAccepted() {
        return stolenCargoAccepted;
    }

    public void acceptStolenCargo() {
        stolenCargoAccepted = true;
    }

    public int getRoadWeaponsDelivered() {
        return roadWeaponsDelivered;
    }

    public void deliverRoadWeapon() {
        if (stolenCargoAccepted && !isCompleted(QuestId.STOLEN_CARGO)) {
            roadWeaponsDelivered = Math.min(ROAD_WEAPON_TARGET, roadWeaponsDelivered + 1);
        }
    }

    public boolean isStolenCargoReady() {
        return stolenCargoAccepted
                && !isCompleted(QuestId.STOLEN_CARGO)
                && roadWeaponsDelivered >= ROAD_WEAPON_TARGET;
    }

    public boolean isBossFloor20Accepted() {
        return bossFloor20Accepted;
    }

    public void acceptBossFloor20() {
        bossFloor20Accepted = true;
    }

    public boolean isBossFloor20Defeated() {
        return bossFloor20Defeated;
    }

    public void markBossFloor20Defeated() {
        if (bossFloor20Accepted && !isCompleted(QuestId.BOSS_FLOOR_20)) {
            bossFloor20Defeated = true;
        }
    }

    public boolean isBossFloor20Ready() {
        return bossFloor20Accepted
                && !isCompleted(QuestId.BOSS_FLOOR_20)
                && bossFloor20Defeated;
    }

    public void setStarostaUnlocked(boolean starostaUnlocked) {
        this.starostaUnlocked = starostaUnlocked;
    }

    public void setBrokenWagonAccepted(boolean brokenWagonAccepted) {
        this.brokenWagonAccepted = brokenWagonAccepted;
    }

    public void setRoadRaidersAccepted(boolean roadRaidersAccepted) {
        this.roadRaidersAccepted = roadRaidersAccepted;
    }

    public void setRoadOrcKills(int roadOrcKills) {
        this.roadOrcKills = Math.max(0, roadOrcKills);
    }

    public void setStolenCargoAccepted(boolean stolenCargoAccepted) {
        this.stolenCargoAccepted = stolenCargoAccepted;
    }

    public void setRoadWeaponsDelivered(int roadWeaponsDelivered) {
        this.roadWeaponsDelivered = Math.max(0, roadWeaponsDelivered);
    }

    public void setBossFloor20Accepted(boolean bossFloor20Accepted) {
        this.bossFloor20Accepted = bossFloor20Accepted;
    }

    public void setBossFloor20Defeated(boolean bossFloor20Defeated) {
        this.bossFloor20Defeated = bossFloor20Defeated;
    }

}

