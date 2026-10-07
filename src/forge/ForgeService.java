package forge;

import item.Gem;
import item.GemTier;
import item.GemType;
import item.Weapon;
import player.Player;
import util.RandomUtil;

import java.util.ArrayList;
import java.util.List;

public final class ForgeService {
    public static final int MAX_GEM_STASH = 40;

    public record ForgeResult(boolean success, String message, Gem resultGem) {
    }

    public record MergeResult(boolean success, String message, Gem resultGem, int gemsLost) {
    }

    private ForgeService() {
    }

    public static boolean canAddGem(Player player) {
        return player.getGemStash().size() < player.getMaxGemStashSize();
    }

    public static ForgeResult smeltOre(Player player) {
        if (player.getRawOre() < ForgeCosts.ORE_PER_SMELT) {
            return new ForgeResult(false, "Need " + ForgeCosts.ORE_PER_SMELT + " ore.", null);
        }
        if (!player.spendGold(ForgeCosts.SMELT_ORE)) {
            return new ForgeResult(false, "Need " + ForgeCosts.SMELT_ORE + " G.", null);
        }
        if (!canAddGem(player)) {
            player.addGold(ForgeCosts.SMELT_ORE);
            return new ForgeResult(false, "Gem stash full (" + player.getMaxGemStashSize() + ").", null);
        }
        player.addRawOre(-ForgeCosts.ORE_PER_SMELT);
        Gem gem = Gem.randomTierOne();
        player.addGem(gem);
        return new ForgeResult(true, "Smelted: " + gem.describe(), gem);
    }

    public static MergeResult mergeGems(Player player, GemType type, GemTier fromTier,
                                        ForgeRiskMode mode) {
        if (fromTier == GemTier.III) {
            return new MergeResult(false, "Already max tier.", null, 0);
        }
        int cost = fromTier == GemTier.I ? ForgeCosts.MERGE_TO_II : ForgeCosts.MERGE_TO_III;
        List<Gem> matching = findGems(player, type, fromTier, 3);
        if (matching.size() < 3) {
            return new MergeResult(false, "Need 3x " + type.getDisplayName() + " " + fromTier.roman() + ".", null, 0);
        }
        if (!player.spendGold(cost)) {
            return new MergeResult(false, "Need " + cost + " G.", null, 0);
        }

        removeGems(player, matching);

        int successChance = mergeSuccessChance(fromTier, mode);
        if (!RandomUtil.chance(successChance)) {
            int lost = mergeFailLoss(mode);
            returnGemsAfterFail(player, matching, lost);
            return new MergeResult(false, failMessage(mode, lost), null, lost);
        }

        GemTier resultTier = fromTier.next();
        double mult = averageMultiplier(matching);
        if (mode == ForgeRiskMode.MAD) {
            mult *= 1.15;
            if (RandomUtil.chance(30)) {
                resultTier = GemTier.III;
            }
        } else if (mode == ForgeRiskMode.NORMAL && RandomUtil.chance(10)) {
            mult *= 1.10;
        }

        if (!canAddGem(player)) {
            player.addGold(cost);
            for (Gem g : matching) {
                player.addGem(g);
            }
            return new MergeResult(false, "Gem stash full.", null, 0);
        }

        Gem result = new Gem(type, resultTier, mult);
        player.addGem(result);
        String bonus = mode == ForgeRiskMode.MAD ? " Risk paid off!" : "";
        return new MergeResult(true, "Forged: " + result.describe() + bonus, result, 0);
    }

    public static ForgeResult insertGem(Player player, Weapon weapon, int socketIndex, Gem gem,
                                        ForgeRiskMode mode) {
        if (weapon.socketCount() == 0) {
            return new ForgeResult(false, "Weapon has no sockets (EPIC+).", null);
        }
        if (socketIndex < 0 || socketIndex >= weapon.socketCount()) {
            return new ForgeResult(false, "Invalid socket.", null);
        }
        if (weapon.getSocket(socketIndex) != null) {
            return new ForgeResult(false, "Socket occupied.", null);
        }
        if (weapon.hasGemType(gem.getType())) {
            return new ForgeResult(false, "Same gem type already socketed.", null);
        }
        if (!player.spendGold(ForgeCosts.INSERT)) {
            return new ForgeResult(false, "Need " + ForgeCosts.INSERT + " G.", null);
        }
        if (!player.removeGem(gem)) {
            player.addGold(ForgeCosts.INSERT);
            return new ForgeResult(false, "Gem not in stash.", null);
        }

        if (mode == ForgeRiskMode.MAD && !RandomUtil.chance(85)) {
            return new ForgeResult(false, "Hammer slipped — gem shattered!", null);
        }

        Gem inserted = gem;
        if (mode == ForgeRiskMode.MAD) {
            inserted = gem.withBonusMultiplier(1.10);
        } else if (mode == ForgeRiskMode.NORMAL && RandomUtil.chance(5)) {
            if (gem.getTier().getLevel() > 1) {
                inserted = gem.withTier(GemTier.values()[gem.getTier().getLevel() - 2]);
                return new ForgeResult(true, "Crack — gem downgraded to " + inserted.describe(), inserted);
            }
        }

        return new ForgeResult(true, "Socketed: " + inserted.describe(), inserted);
    }

    public static ForgeResult extractGem(Player player, Gem gem) {
        if (!player.spendGold(ForgeCosts.EXTRACT)) {
            return new ForgeResult(false, "Need " + ForgeCosts.EXTRACT + " G.", null);
        }
        if (RandomUtil.chance(70)) {
            player.addGem(gem);
            return new ForgeResult(true, "Extracted: " + gem.describe(), gem);
        }
        return new ForgeResult(false, "Gem broke during extraction!", null);
    }

    private static int mergeSuccessChance(GemTier fromTier, ForgeRiskMode mode) {
        if (fromTier == GemTier.I) {
            return switch (mode) {
                case STABLE -> 90;
                case NORMAL -> 65;
                case MAD -> 40;
            };
        }
        return switch (mode) {
            case STABLE -> 70;
            case NORMAL -> 45;
            case MAD -> 25;
        };
    }

    private static int mergeFailLoss(ForgeRiskMode mode) {
        return switch (mode) {
            case STABLE -> 1;
            case NORMAL -> 2;
            case MAD -> 3;
        };
    }

    private static String failMessage(ForgeRiskMode mode, int lost) {
        return switch (mode) {
            case STABLE -> "Failed — lost " + lost + " gem(s).";
            case NORMAL -> "Forge burst — " + lost + " gems destroyed.";
            case MAD -> "Catastrophe — all 3 gems lost!";
        };
    }

    private static List<Gem> findGems(Player player, GemType type, GemTier tier, int count) {
        List<Gem> found = new ArrayList<>();
        for (Gem gem : player.getGemStash()) {
            if (gem.getType() == type && gem.getTier() == tier) {
                found.add(gem);
                if (found.size() >= count) {
                    break;
                }
            }
        }
        return found;
    }

    private static void removeGems(Player player, List<Gem> gems) {
        for (Gem gem : gems) {
            player.removeGem(gem);
        }
    }

    private static double averageMultiplier(List<Gem> gems) {
        double sum = 0;
        for (Gem gem : gems) {
            sum += gem.getStatMultiplier();
        }
        return sum / gems.size();
    }

    private static void returnGemsAfterFail(Player player, List<Gem> consumed, int lost) {
        int keep = Math.max(0, consumed.size() - lost);
        for (int i = 0; i < keep && i < consumed.size(); i++) {
            if (canAddGem(player)) {
                player.addGem(consumed.get(i));
            }
        }
    }
}
