package quest;



import enemy.BossType;
import enemy.EnemyType;

import item.Gem;

import item.GemTier;

import item.GemType;

import item.Item;

import item.ItemFactory;

import item.Rarity;

import player.Player;

import progression.GoldShop;

import progression.MetaProgression;

import util.ConsoleColors;

import util.GemRenderer;

import util.RandomUtil;



public final class QuestService {

    public static final int GOBLIN_REWARD_GOLD = 300;

    public static final int MILITIA_REWARD_GOLD = 800;

    public static final int NIGHT_PACK_REWARD_GOLD = 600;

    public static final int NIGHT_PACK_REWARD_ORE = 5;

    public static final int BITTER_POTION_REWARD_GOLD = 900;

    public static final int FLOOR_20_REWARD_GOLD = 1200;

    public static final int CAVE_BONES_REWARD_GOLD = 1500;

    public static final int CAVE_BONES_REWARD_ORE = 10;

    public static final int SWAMP_WHISPER_REWARD_GOLD = 2000;

    public static final int MIST_SEAL_SOUL_CRYSTALS = 1;

    public static final int MINE_SHIFT_REWARD_GOLD = 800;

    public static final int GOLEM_GUARD_REWARD_GOLD = 1200;

    public static final int GOLEM_GUARD_REWARD_ORE = 5;

    public static final int THREE_SPARKS_REWARD_GOLD = 1500;

    public static final int SOUL_SOCKET_EXTRA_STASH = 2;
    public static final int BROKEN_WAGON_REWARD_GOLD = 600;
    public static final int ROAD_RAIDERS_REWARD_GOLD = 1000;
    public static final int STOLEN_CARGO_REWARD_GOLD = 1200;
    public static final int BOSS_FLOOR_20_SOUL_CRYSTALS = 1;
    public static final int ROAD_SHOP_DISCOUNT_PERCENT = 10;



    private QuestService() {

    }



    public static boolean isMineCampAvailable(Player player, MetaProgression meta) {

        if (player == null) {

            return false;

        }

        int bestFloor = meta != null ? meta.getBestFloor() : 0;

        return player.getLevel() >= 10 || bestFloor >= 5;

    }



    public static void onEnemyKilled(Player player, EnemyType type) {

        if (player == null || type == null) {

            return;

        }

        QuestLog log = player.getQuestLog();

        switch (type) {

            case GOBLIN -> {

                if (log.isGoblinHuntAccepted() && !log.isCompleted(QuestId.GOBLIN_HUNT)) {

                    log.addGoblinKill();

                }

            }

            case WOLF -> {

                if (log.isNightPackAccepted() && !log.isCompleted(QuestId.NIGHT_PACK)) {

                    log.addWolfKill();

                }

            }

            case BAT -> {

                if (log.isNightPackAccepted() && !log.isCompleted(QuestId.NIGHT_PACK)) {

                    log.addBatKill();

                }

            }

            case SKELETON -> {

                if (log.isCaveBonesAccepted() && !log.isCompleted(QuestId.CAVE_BONES)) {

                    log.addSkeletonKill();

                }

            }

            case WITCH -> {

                if (log.isSwampWhisperAccepted() && !log.isCompleted(QuestId.SWAMP_WHISPER)) {

                    log.addWitchKill();

                }

            }

            case ORC -> {

                if (log.isRoadRaidersAccepted() && !log.isCompleted(QuestId.ROAD_RAIDERS)) {

                    log.addRoadOrcKill();

                }

            }

            default -> {

                // other types not tracked yet

            }

        }

    }



    public static void onRunEnded(Player player, int peakFloor) {

        if (player == null || peakFloor < QuestLog.FLOOR_20_TARGET) {

            return;

        }

        player.getQuestLog().markFloor20ReachedInRun();

    }



    public static void onGolemDefeated(Player player) {

        if (player == null) {

            return;

        }

        player.getQuestLog().markGolemDefeatedForQuest();

    }



    public static void onSuccessfulSmelt(Player player) {

        if (player == null) {

            return;

        }

        player.getQuestLog().addSuccessfulSmelt();

    }



    public static void onSuccessfulGemInsert(Player player) {

        if (player == null) {

            return;

        }

        player.getQuestLog().markGemInsertedForQuest();

    }



    public static void onBossDefeated(Player player, BossType bossType) {

        if (player == null || bossType != BossType.FLOOR_20) {

            return;

        }

        player.getQuestLog().markBossFloor20Defeated();

    }



    public static void completeGoblinHunt(Player player) {

        QuestLog log = player.getQuestLog();

        if (!log.isGoblinHuntReady()) {

            return;

        }

        player.addGold(GOBLIN_REWARD_GOLD);

        log.markCompleted(QuestId.GOBLIN_HUNT);

        log.unlockMilitia();

    }



    public static void completeMilitiaGear(Player player) {

        QuestLog log = player.getQuestLog();

        if (!log.isMilitiaGearReady()) {

            return;

        }

        player.addGold(MILITIA_REWARD_GOLD);

        Gem gem = randomGem(GemTier.I);

        player.addGem(gem);

        log.markCompleted(QuestId.MILITIA_GEAR);

        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_MAGENTA,

                "  ✦ Награда: " + GemRenderer.formatDescribe(gem)));

        giveQuestBonusGear(player, Rarity.RARE, Math.max(8, player.getLevel() * 2));

    }



    public static void completeNightPack(Player player) {

        QuestLog log = player.getQuestLog();

        if (!log.isNightPackReady()) {

            return;

        }

        player.addGold(NIGHT_PACK_REWARD_GOLD);

        player.addRawOre(NIGHT_PACK_REWARD_ORE);

        log.markCompleted(QuestId.NIGHT_PACK);

        log.unlockHealer();

    }



    public static void completeBitterPotion(Player player) {

        QuestLog log = player.getQuestLog();

        if (!log.isBitterPotionReady(player.getRawOre())) {

            return;

        }

        player.addRawOre(-QuestLog.ORE_TARGET);

        player.addGold(BITTER_POTION_REWARD_GOLD);

        Gem gem = randomGem(GemTier.II);

        player.addGem(gem);

        log.markCompleted(QuestId.BITTER_POTION);

        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_MAGENTA,

                "  ✦ Награда: " + GemRenderer.formatDescribe(gem)));

    }



    public static void completeFloor20Report(Player player) {

        QuestLog log = player.getQuestLog();

        if (!log.isFloor20ReportReady()) {

            return;

        }

        player.addGold(FLOOR_20_REWARD_GOLD);

        log.markCompleted(QuestId.FLOOR_20_REPORT);

    }



    public static void completeCaveBones(Player player) {

        QuestLog log = player.getQuestLog();

        if (!log.isCaveBonesReady()) {

            return;

        }

        player.addGold(CAVE_BONES_REWARD_GOLD);

        player.addRawOre(CAVE_BONES_REWARD_ORE);

        log.markCompleted(QuestId.CAVE_BONES);

        giveQuestBonusGear(player, Rarity.RARE, Math.max(16, player.getLevel() * 2));

        log.unlockHermit();

    }



    public static void completeSwampWhisper(Player player) {

        QuestLog log = player.getQuestLog();

        if (!log.isSwampWhisperReady()) {

            return;

        }

        player.addGold(SWAMP_WHISPER_REWARD_GOLD);

        Gem gem = randomGem(GemTier.III);

        player.addGem(gem);

        log.markCompleted(QuestId.SWAMP_WHISPER);

        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_MAGENTA,

                "  ✦ Награда: " + GemRenderer.formatDescribe(gem)));

    }



    public static void completeMistSeal(MetaProgression meta, Player player) {

        QuestLog log = player.getQuestLog();

        if (!log.isMistSealAccepted() || log.isCompleted(QuestId.MIST_SEAL)) {

            return;

        }

        if (meta != null) {

            meta.addSoulCrystals(MIST_SEAL_SOUL_CRYSTALS);

        }

        player.addBlessingScroll(1, GoldShop.MAX_SCROLLS);

        log.markCompleted(QuestId.MIST_SEAL);

        log.unlockStarosta();

    }



    public static void completeBrokenWagon(Player player) {

        QuestLog log = player.getQuestLog();

        if (!log.isBrokenWagonReady(player.getRawOre())) {

            return;

        }

        player.addRawOre(-QuestLog.ROAD_WAGON_ORE);

        player.addGold(BROKEN_WAGON_REWARD_GOLD);

        log.markCompleted(QuestId.BROKEN_WAGON);

    }



    public static void completeRoadRaiders(Player player) {

        QuestLog log = player.getQuestLog();

        if (!log.isRoadRaidersReady()) {

            return;

        }

        player.addGold(ROAD_RAIDERS_REWARD_GOLD);

        log.markCompleted(QuestId.ROAD_RAIDERS);

        giveQuestBonusGear(player, Rarity.EPIC, Math.max(22, player.getLevel() * 2));

    }



    public static void completeStolenCargo(Player player) {

        QuestLog log = player.getQuestLog();

        if (!log.isStolenCargoReady()) {

            return;

        }

        player.addGold(STOLEN_CARGO_REWARD_GOLD);

        Gem gem = randomGem(GemTier.II);

        player.addGem(gem);

        log.markCompleted(QuestId.STOLEN_CARGO);

        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_MAGENTA,

                "  ✦ Награда: " + GemRenderer.formatDescribe(gem)));

    }



    public static void completeBossFloor20(MetaProgression meta, Player player) {

        QuestLog log = player.getQuestLog();

        if (!log.isBossFloor20Ready()) {

            return;

        }

        if (meta != null) {

            meta.addSoulCrystals(BOSS_FLOOR_20_SOUL_CRYSTALS);

        }

        player.setShopDiscountPercent(ROAD_SHOP_DISCOUNT_PERCENT);

        log.markCompleted(QuestId.BOSS_FLOOR_20);

    }



    public static void completeMineShift(Player player) {

        QuestLog log = player.getQuestLog();

        if (!log.isMineShiftReady(player.getRawOre())) {

            return;

        }

        player.addRawOre(-QuestLog.MINE_ORE_TARGET);

        player.addGold(MINE_SHIFT_REWARD_GOLD);

        log.markCompleted(QuestId.MINE_SHIFT);

    }



    public static void completeGolemGuard(Player player) {

        QuestLog log = player.getQuestLog();

        if (!log.isGolemGuardReady()) {

            return;

        }

        player.addGold(GOLEM_GUARD_REWARD_GOLD);

        player.addRawOre(GOLEM_GUARD_REWARD_ORE);

        log.markCompleted(QuestId.GOLEM_GUARD);

        log.unlockForgeMaster();

    }



    public static void completeThreeSparks(Player player) {

        QuestLog log = player.getQuestLog();

        if (!log.isThreeSparksReady()) {

            return;

        }

        player.addGold(THREE_SPARKS_REWARD_GOLD);

        log.markCompleted(QuestId.THREE_SPARKS);

    }



    public static void completeSoulSocket(Player player) {

        QuestLog log = player.getQuestLog();

        if (!log.isSoulSocketReady()) {

            return;

        }

        Gem gem = randomGem(GemTier.II);

        player.addGem(gem);

        player.addExtraGemStashSlots(SOUL_SOCKET_EXTRA_STASH);

        log.markCompleted(QuestId.SOUL_SOCKET);

        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_MAGENTA,

                "  ✦ Награда: " + GemRenderer.formatDescribe(gem)

                        + ", +" + SOUL_SOCKET_EXTRA_STASH + " слота сундука гемов"));

    }



    private static Gem randomGem(GemTier tier) {

        GemType[] pool = {GemType.RUBY, GemType.SAPPHIRE, GemType.EMERALD, GemType.OBSIDIAN, GemType.AMBER};

        GemType type = pool[RandomUtil.range(0, pool.length - 1)];

        return new Gem(type, tier);

    }



    /** Случайная экипировка фиксированной редкости (бонус к основной награде квеста). */

    private static void giveQuestBonusGear(Player player, Rarity rarity, int itemLevel) {

        Item item = ItemFactory.generateRandomItem(itemLevel, rarity);

        if (player.getInventory().add(item)) {

            System.out.println("  ✦ Бонус: " + item.getDisplayName()

                    + ConsoleColors.dim(" (" + item.getStatDescription() + ")"));

        } else {

            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_RED,

                    "  ! Инвентарь полон — освободите место для бонусной экипировки."));

        }

    }



    public static String formatJournalLine(QuestLog log) {

        StringBuilder sb = new StringBuilder();

        appendActiveQuest(sb, log);

        for (QuestId id : QuestId.values()) {

            appendCompletedQuest(sb, log, id);

        }

        if (sb.length() == 0) {

            return "  (пока нет активных поручений)\n";

        }

        return sb.toString();

    }



    private static void appendActiveQuest(StringBuilder sb, QuestLog log) {

        if (!log.isCompleted(QuestId.GOBLIN_HUNT)) {

            if (log.isGoblinHuntAccepted()) {

                sb.append("  • ").append(QuestId.GOBLIN_HUNT.getTitle())

                        .append(": ").append(log.getGoblinKills()).append("/")

                        .append(QuestLog.GOBLIN_TARGET).append(" гоблинов\n");

            } else {

                sb.append("  • ").append(QuestId.GOBLIN_HUNT.getTitle())

                        .append(": не принят\n");

            }

        }

        if (log.isMilitiaUnlocked() && !log.isCompleted(QuestId.MILITIA_GEAR)) {

            if (log.isMilitiaQuestAccepted()) {

                sb.append("  • ").append(QuestId.MILITIA_GEAR.getTitle())

                        .append(": оружие ").append(log.getWeaponsDelivered()).append("/")

                        .append(QuestLog.WEAPONS_TARGET)

                        .append(", броня ").append(log.getArmorDelivered()).append("/")

                        .append(QuestLog.ARMOR_TARGET).append("\n");

            } else {

                sb.append("  • ").append(QuestId.MILITIA_GEAR.getTitle())

                        .append(": не принят\n");

            }

        }

        if (log.isCompleted(QuestId.MILITIA_GEAR) && !log.isCompleted(QuestId.NIGHT_PACK)) {

            if (log.isNightPackAccepted()) {

                sb.append("  • ").append(QuestId.NIGHT_PACK.getTitle())

                        .append(": волки ").append(log.getWolfKills()).append("/")

                        .append(QuestLog.WOLF_TARGET)

                        .append(", мыши ").append(log.getBatKills()).append("/")

                        .append(QuestLog.BAT_TARGET).append("\n");

            } else {

                sb.append("  • ").append(QuestId.NIGHT_PACK.getTitle())

                        .append(": не принят\n");

            }

        }

        if (log.isHealerUnlocked() && !log.isCompleted(QuestId.BITTER_POTION)) {

            if (log.isBitterPotionAccepted()) {

                sb.append("  • ").append(QuestId.BITTER_POTION.getTitle())

                        .append(": руда ").append(QuestLog.ORE_TARGET)

                        .append(" (сдай у Агаты)\n");

            } else {

                sb.append("  • ").append(QuestId.BITTER_POTION.getTitle())

                        .append(": не принят\n");

            }

        }

        if (log.isCompleted(QuestId.BITTER_POTION) && !log.isCompleted(QuestId.FLOOR_20_REPORT)) {

            if (log.isFloor20ReportAccepted()) {

                sb.append("  • ").append(QuestId.FLOOR_20_REPORT.getTitle())

                        .append(": дойти до этажа ").append(QuestLog.FLOOR_20_TARGET)

                        .append(" за один забег");

                if (log.isFloor20ReachedInRun()) {

                    sb.append(" (готово к сдаче)");

                }

                sb.append("\n");

            } else {

                sb.append("  • ").append(QuestId.FLOOR_20_REPORT.getTitle())

                        .append(": не принят\n");

            }

        }

        if (log.isCompleted(QuestId.FLOOR_20_REPORT) && !log.isCompleted(QuestId.CAVE_BONES)) {

            if (log.isCaveBonesAccepted()) {

                sb.append("  • ").append(QuestId.CAVE_BONES.getTitle())

                        .append(": ").append(log.getSkeletonKills()).append("/")

                        .append(QuestLog.SKELETON_TARGET).append(" скелетов\n");

            } else {

                sb.append("  • ").append(QuestId.CAVE_BONES.getTitle())

                        .append(": не принят\n");

            }

        }

        if (log.isHermitUnlocked() && !log.isCompleted(QuestId.SWAMP_WHISPER)) {

            if (log.isSwampWhisperAccepted()) {

                sb.append("  • ").append(QuestId.SWAMP_WHISPER.getTitle())

                        .append(": ").append(log.getWitchKills()).append("/")

                        .append(QuestLog.WITCH_TARGET).append(" ведьм\n");

            } else {

                sb.append("  • ").append(QuestId.SWAMP_WHISPER.getTitle())

                        .append(": не принят\n");

            }

        }

        if (log.isCompleted(QuestId.SWAMP_WHISPER) && !log.isCompleted(QuestId.MIST_SEAL)) {

            if (log.isMistSealAccepted()) {

                sb.append("  • ").append(QuestId.MIST_SEAL.getTitle())

                        .append(": сдать предмет [Фиолетовый]+ из сумки\n");

            } else {

                sb.append("  • ").append(QuestId.MIST_SEAL.getTitle())

                        .append(": не принят\n");

            }

        }

        if (!log.isCompleted(QuestId.MINE_SHIFT)) {

            if (log.isMineShiftAccepted()) {

                sb.append("  • ").append(QuestId.MINE_SHIFT.getTitle())

                        .append(": руда ").append(QuestLog.MINE_ORE_TARGET)

                        .append(" (сдай у Кости)\n");

            } else if (log.isCompleted(QuestId.BITTER_POTION)) {

                sb.append("  • ").append(QuestId.MINE_SHIFT.getTitle())

                        .append(": не принят (шахтёрская застава)\n");

            }

        }

        if (log.isCompleted(QuestId.MINE_SHIFT) && !log.isCompleted(QuestId.GOLEM_GUARD)) {

            if (log.isGolemGuardAccepted()) {

                sb.append("  • ").append(QuestId.GOLEM_GUARD.getTitle())

                        .append(": победить каменного стража");

                if (log.isGolemDefeatedForQuest()) {

                    sb.append(" (готово к сдаче)");

                }

                sb.append("\n");

            } else {

                sb.append("  • ").append(QuestId.GOLEM_GUARD.getTitle())

                        .append(": не принят\n");

            }

        }

        if (log.isForgeMasterUnlocked() && !log.isCompleted(QuestId.THREE_SPARKS)) {

            if (log.isThreeSparksAccepted()) {

                sb.append("  • ").append(QuestId.THREE_SPARKS.getTitle())

                        .append(": ").append(log.getSuccessfulSmelts()).append("/")

                        .append(QuestLog.SMELT_TARGET).append(" переплавок\n");

            } else {

                sb.append("  • ").append(QuestId.THREE_SPARKS.getTitle())

                        .append(": не принят (кузнец Пётр)\n");

            }

        }

        if (log.isCompleted(QuestId.THREE_SPARKS) && !log.isCompleted(QuestId.SOUL_SOCKET)) {

            if (log.isSoulSocketAccepted()) {

                sb.append("  • ").append(QuestId.SOUL_SOCKET.getTitle())

                        .append(": вставить гем в оружие");

                if (log.isGemInsertedForQuest()) {

                    sb.append(" (готово к сдаче)");

                }

                sb.append("\n");

            } else {

                sb.append("  • ").append(QuestId.SOUL_SOCKET.getTitle())

                        .append(": не принят\n");

            }

        }

        if (log.isStarostaUnlocked() && !log.isCompleted(QuestId.BOSS_FLOOR_20)) {
            if (!log.isCompleted(QuestId.BROKEN_WAGON)) {
                if (log.isBrokenWagonAccepted()) {
                    sb.append("  • ").append(QuestId.BROKEN_WAGON.getTitle())
                            .append(": руда ").append(QuestLog.ROAD_WAGON_ORE).append(" (у Фёдора)\n");
                } else {
                    sb.append("  • ").append(QuestId.BROKEN_WAGON.getTitle())
                            .append(": не принят\n");
                }
            } else if (!log.isCompleted(QuestId.ROAD_RAIDERS)) {
                if (log.isRoadRaidersAccepted()) {
                    sb.append("  • ").append(QuestId.ROAD_RAIDERS.getTitle())
                            .append(": ").append(log.getRoadOrcKills()).append("/")
                            .append(QuestLog.ROAD_ORC_TARGET).append(" орков\n");
                } else {
                    sb.append("  • ").append(QuestId.ROAD_RAIDERS.getTitle())
                            .append(": не принят\n");
                }
            } else if (!log.isCompleted(QuestId.STOLEN_CARGO)) {
                if (log.isStolenCargoAccepted()) {
                    sb.append("  • ").append(QuestId.STOLEN_CARGO.getTitle())
                            .append(": оружие ").append(log.getRoadWeaponsDelivered()).append("/")
                            .append(QuestLog.ROAD_WEAPON_TARGET).append(" [Синий]+\n");
                } else {
                    sb.append("  • ").append(QuestId.STOLEN_CARGO.getTitle())
                            .append(": не принят\n");
                }
            } else if (log.isBossFloor20Accepted()) {
                sb.append("  • ").append(QuestId.BOSS_FLOOR_20.getTitle())
                        .append(": победить некроманта на 20-м этаже");
                if (log.isBossFloor20Defeated()) {
                    sb.append(" (готово к сдаче)");
                }
                sb.append("\n");
            } else {
                sb.append("  • ").append(QuestId.BOSS_FLOOR_20.getTitle())
                        .append(": не принят\n");
            }
        }

    }

    private static void appendCompletedQuest(StringBuilder sb, QuestLog log, QuestId id) {

        if (log.isCompleted(id)) {

            sb.append("  ✓ ").append(id.getTitle()).append("\n");

        }

    }

}

