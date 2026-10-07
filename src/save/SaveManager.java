package save;

import inventory.Equipment;
import inventory.Inventory;
import item.Item;
import party.Mercenary;
import party.Party;
import player.CharacterClass;
import player.Player;
import progression.LevelService;
import progression.MetaProgression;
import progression.RovingMerchant;
import quest.QuestId;
import quest.QuestLog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SaveManager {
    private static final Path SAVE_PATH = Path.of("savegame.json");

    public void save(MetaProgression meta, Player player, boolean autoRestartOnDeath) {
        String json = buildJson(meta, player, autoRestartOnDeath);
        try {
            Files.writeString(SAVE_PATH, json);
        } catch (IOException e) {
            System.out.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    public SaveData load() {
        if (!Files.exists(SAVE_PATH)) {
            return null;
        }
        try {
            String json = Files.readString(SAVE_PATH);
            return parseJson(json);
        } catch (IOException e) {
            System.out.println("Ошибка загрузки: " + e.getMessage());
            return null;
        }
    }

    private String buildJson(MetaProgression meta, Player player, boolean autoRestartOnDeath) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"soulCrystals\": ").append(meta.getSoulCrystals()).append(",\n");
        sb.append("  \"damageLevel\": ").append(meta.getDamageLevel()).append(",\n");
        sb.append("  \"healthLevel\": ").append(meta.getHealthLevel()).append(",\n");
        sb.append("  \"dropLevel\": ").append(meta.getDropLevel()).append(",\n");
        sb.append("  \"bestFloor\": ").append(meta.getBestFloor()).append(",\n");
        sb.append("  \"totalKills\": ").append(meta.getTotalKills()).append(",\n");
        sb.append("  \"totalDeaths\": ").append(meta.getTotalDeaths()).append(",\n");
        sb.append("  \"totalGoldEarned\": ").append(meta.getTotalGoldEarned()).append(",\n");
        sb.append("  \"totalItemsFound\": ").append(meta.getTotalItemsFound()).append(",\n");
        sb.append("  \"totalPlayTimeSeconds\": ").append(meta.getTotalPlayTimeSeconds()).append(",\n");
        sb.append("  \"selectedClass\": \"").append(player.getCharacterClass().name()).append("\",\n");
        sb.append("  \"playerLevel\": ").append(player.getLevel()).append(",\n");
        sb.append("  \"playerXp\": ").append(player.getXp()).append(",\n");
        sb.append("  \"playerGold\": ").append(player.getGold()).append(",\n");
        sb.append("  \"goldTrainingHp\": ").append(player.getGoldTrainingHp()).append(",\n");
        sb.append("  \"goldTrainingAtk\": ").append(player.getGoldTrainingAtk()).append(",\n");
        sb.append("  \"inventoryExpansions\": ").append(player.getInventoryExpansions()).append(",\n");
        sb.append("  \"inventoryMaxSize\": ").append(player.getInventory().getMaxSize()).append(",\n");
        sb.append("  \"autoRestartOnDeath\": ").append(autoRestartOnDeath).append(",\n");
        sb.append("  \"equipment\": ").append(serializeEquipment(player.getEquipment())).append(",\n");
        sb.append("  \"inventory\": ").append(serializeInventory(player.getInventory())).append(",\n");
        sb.append("  \"partySlotsUnlocked\": ").append(player.getParty().getUnlockedMercSlots()).append(",\n");
        sb.append("  \"mercenaries\": ").append(serializeMercenaries(player.getParty())).append(",\n");
        sb.append("  \"ascensionUnlocked\": ").append(player.hasAscensionUnlock()).append(",\n");
        sb.append("  \"abdolbosPotions\": ").append(player.getAbdolbosPotions()).append(",\n");
        sb.append("  \"blessingScrolls\": ").append(player.getBlessingScrolls()).append(",\n");
        sb.append("  \"xpScrolls\": ").append(player.getXpScrolls()).append(",\n");
        sb.append("  \"luckScrolls\": ").append(player.getLuckScrolls()).append(",\n");
        sb.append("  \"greedScrolls\": ").append(player.getGreedScrolls()).append(",\n");
        sb.append("  \"fogScrolls\": ").append(player.getFogScrolls()).append(",\n");
        sb.append("  \"secondBreathScrolls\": ").append(player.getSecondBreathScrolls()).append(",\n");
        sb.append("  \"autoSellRarities\": ").append(serializeAutoSell(player.getAutoSellRarities())).append(",\n");
        sb.append("  \"skillTrees\": ").append(serializeSkillTrees(player.getSkillTrees())).append(",\n");
        sb.append("  \"rawOre\": ").append(player.getRawOre()).append(",\n");
        sb.append("  \"gemStash\": ").append(serializeGemStash(player.getGemStash())).append(",\n");
        sb.append("  \"lastGolemRewardEpochDay\": ").append(player.getLastGolemRewardEpochDay()).append(",\n");
        sb.append("  \"questsCompleted\": ").append(serializeQuestsCompleted(player.getQuestLog())).append(",\n");
        sb.append("  \"goblinHuntAccepted\": ").append(player.getQuestLog().isGoblinHuntAccepted()).append(",\n");
        sb.append("  \"goblinKills\": ").append(player.getQuestLog().getGoblinKills()).append(",\n");
        sb.append("  \"militiaUnlocked\": ").append(player.getQuestLog().isMilitiaUnlocked()).append(",\n");
        sb.append("  \"militiaQuestAccepted\": ").append(player.getQuestLog().isMilitiaQuestAccepted()).append(",\n");
        sb.append("  \"questWeaponsDelivered\": ").append(player.getQuestLog().getWeaponsDelivered()).append(",\n");
        sb.append("  \"questArmorDelivered\": ").append(player.getQuestLog().getArmorDelivered()).append(",\n");
        sb.append("  \"nightPackAccepted\": ").append(player.getQuestLog().isNightPackAccepted()).append(",\n");
        sb.append("  \"wolfKills\": ").append(player.getQuestLog().getWolfKills()).append(",\n");
        sb.append("  \"batKills\": ").append(player.getQuestLog().getBatKills()).append(",\n");
        sb.append("  \"healerUnlocked\": ").append(player.getQuestLog().isHealerUnlocked()).append(",\n");
        sb.append("  \"bitterPotionAccepted\": ").append(player.getQuestLog().isBitterPotionAccepted()).append(",\n");
        QuestLog ql = player.getQuestLog();
        sb.append("  \"floor20ReportAccepted\": ").append(ql.isFloor20ReportAccepted()).append(",\n");
        sb.append("  \"floor20ReachedInRun\": ").append(ql.isFloor20ReachedInRun()).append(",\n");
        sb.append("  \"caveBonesAccepted\": ").append(ql.isCaveBonesAccepted()).append(",\n");
        sb.append("  \"skeletonKills\": ").append(ql.getSkeletonKills()).append(",\n");
        sb.append("  \"hermitUnlocked\": ").append(ql.isHermitUnlocked()).append(",\n");
        sb.append("  \"swampWhisperAccepted\": ").append(ql.isSwampWhisperAccepted()).append(",\n");
        sb.append("  \"witchKills\": ").append(ql.getWitchKills()).append(",\n");
        sb.append("  \"mistSealAccepted\": ").append(ql.isMistSealAccepted()).append(",\n");
        sb.append("  \"mineShiftAccepted\": ").append(ql.isMineShiftAccepted()).append(",\n");
        sb.append("  \"golemGuardAccepted\": ").append(ql.isGolemGuardAccepted()).append(",\n");
        sb.append("  \"golemDefeatedForQuest\": ").append(ql.isGolemDefeatedForQuest()).append(",\n");
        sb.append("  \"forgeMasterUnlocked\": ").append(ql.isForgeMasterUnlocked()).append(",\n");
        sb.append("  \"threeSparksAccepted\": ").append(ql.isThreeSparksAccepted()).append(",\n");
        sb.append("  \"successfulSmelts\": ").append(ql.getSuccessfulSmelts()).append(",\n");
        sb.append("  \"soulSocketAccepted\": ").append(ql.isSoulSocketAccepted()).append(",\n");
        sb.append("  \"gemInsertedForQuest\": ").append(ql.isGemInsertedForQuest()).append(",\n");
        sb.append("  \"extraGemStashSlots\": ").append(player.getExtraGemStashSlots()).append(",\n");
        sb.append("  \"shopDiscountPercent\": ").append(player.getShopDiscountPercent()).append(",\n");
        sb.append("  \"starostaUnlocked\": ").append(ql.isStarostaUnlocked()).append(",\n");
        sb.append("  \"brokenWagonAccepted\": ").append(ql.isBrokenWagonAccepted()).append(",\n");
        sb.append("  \"roadRaidersAccepted\": ").append(ql.isRoadRaidersAccepted()).append(",\n");
        sb.append("  \"roadOrcKills\": ").append(ql.getRoadOrcKills()).append(",\n");
        sb.append("  \"stolenCargoAccepted\": ").append(ql.isStolenCargoAccepted()).append(",\n");
        sb.append("  \"roadWeaponsDelivered\": ").append(ql.getRoadWeaponsDelivered()).append(",\n");
        sb.append("  \"bossFloor20Accepted\": ").append(ql.isBossFloor20Accepted()).append(",\n");
        sb.append("  \"bossFloor20Defeated\": ").append(ql.isBossFloor20Defeated()).append(",\n");
        sb.append("  \"merchantOffers\": ").append(serializeMerchantOffers(player.getRovingMerchant())).append("\n");
        sb.append("}\n");
        return sb.toString();
    }

    private String serializeEquipment(Equipment equipment) {
        StringBuilder sb = new StringBuilder("[");
        Equipment.Slot[] slots = Equipment.Slot.values();
        for (int i = 0; i < slots.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{\"slot\":\"").append(slots[i].name()).append("\",");
            sb.append("\"item\":").append(ItemSerializer.toJson(equipment.get(slots[i]))).append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private String serializeMercenaries(Party party) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < Party.MAX_MERC_SLOTS; i++) {
            if (i > 0) {
                sb.append(",");
            }
            Mercenary m = party.getMercenary(i);
            if (m == null) {
                sb.append("null");
            } else {
                sb.append("{");
                sb.append("\"slot\":").append(i).append(",");
                sb.append("\"name\":\"").append(escape(m.getName())).append("\",");
                sb.append("\"class\":\"").append(m.getCharacterClass().name()).append("\",");
                sb.append("\"level\":").append(m.getLevel()).append(",");
                sb.append("\"xp\":").append(m.getXp()).append(",");
                sb.append("\"skillTree\":").append(serializeSkillTreeProgress(m.getSkillTree())).append(",");
                sb.append("\"equipment\":").append(serializeEquipment(m.getEquipment()));
                sb.append("}");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String serializeInventory(Inventory inventory) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(ItemSerializer.toJson(inventory.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private String serializeMerchantOffers(RovingMerchant merchant) {
        StringBuilder sb = new StringBuilder("[");
        List<RovingMerchant.Offer> offers = merchant.getOffers();
        for (int i = 0; i < offers.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            RovingMerchant.Offer offer = offers.get(i);
            sb.append("{\"price\":").append(offer.basePrice).append(",\"sold\":").append(offer.sold).append(",\"item\":");
            if (offer.sold || offer.item == null) {
                sb.append("null");
            } else {
                sb.append(ItemSerializer.toJson(offer.item));
            }
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private SaveData parseJson(String json) {
        SaveData data = new SaveData();
        data.soulCrystals = readInt(json, "soulCrystals");
        data.damageLevel = readInt(json, "damageLevel");
        data.healthLevel = readInt(json, "healthLevel");
        data.dropLevel = readInt(json, "dropLevel");
        data.bestFloor = readInt(json, "bestFloor");
        data.totalKills = readInt(json, "totalKills");
        data.totalDeaths = readInt(json, "totalDeaths");
        data.totalGoldEarned = readLong(json, "totalGoldEarned");
        data.totalItemsFound = readInt(json, "totalItemsFound");
        data.totalPlayTimeSeconds = readLong(json, "totalPlayTimeSeconds");
        data.selectedClass = readString(json, "selectedClass");
        data.playerLevel = readInt(json, "playerLevel");
        if (data.playerLevel == 0) {
            data.playerLevel = 1;
        }
        data.playerXp = readInt(json, "playerXp");
        data.playerGold = readInt(json, "playerGold");
        data.goldTrainingHp = readInt(json, "goldTrainingHp");
        data.goldTrainingAtk = readInt(json, "goldTrainingAtk");
        data.inventoryExpansions = readInt(json, "inventoryExpansions");
        data.inventoryMaxSize = readInt(json, "inventoryMaxSize");
        if (data.inventoryMaxSize == 0) {
            data.inventoryMaxSize = Inventory.BASE_SIZE;
        }
        data.autoRestartOnDeath = json.contains("\"autoRestartOnDeath\": true");
        data.equipment = parseEquipment(json);
        data.inventory = parseInventory(json);
        data.partySlotsUnlocked = readInt(json, "partySlotsUnlocked");
        data.mercenaries = parseMercenaries(json);
        data.ascensionUnlocked = json.contains("\"ascensionUnlocked\": true");
        data.ascensionKeys = readInt(json, "ascensionKeys");
        data.abdolbosPotions = readInt(json, "abdolbosPotions");
        data.blessingScrolls = readInt(json, "blessingScrolls");
        data.xpScrolls = readInt(json, "xpScrolls");
        data.luckScrolls = readInt(json, "luckScrolls");
        data.greedScrolls = readInt(json, "greedScrolls");
        data.fogScrolls = readInt(json, "fogScrolls");
        data.secondBreathScrolls = readInt(json, "secondBreathScrolls");
        data.autoSellRaritiesJson = extractArray(json, "autoSellRarities");
        data.skillTreesJson = extractObject(json, "skillTrees");
        data.rawOre = readInt(json, "rawOre");
        data.gemStashJson = extractArray(json, "gemStash");
        data.lastGolemRewardEpochDay = readLong(json, "lastGolemRewardEpochDay");
        data.questsCompletedJson = extractArray(json, "questsCompleted");
        data.goblinHuntAccepted = json.contains("\"goblinHuntAccepted\": true");
        data.goblinKills = readInt(json, "goblinKills");
        data.militiaUnlocked = json.contains("\"militiaUnlocked\": true");
        data.militiaQuestAccepted = json.contains("\"militiaQuestAccepted\": true");
        data.questWeaponsDelivered = readInt(json, "questWeaponsDelivered");
        data.questArmorDelivered = readInt(json, "questArmorDelivered");
        data.nightPackAccepted = json.contains("\"nightPackAccepted\": true");
        data.wolfKills = readInt(json, "wolfKills");
        data.batKills = readInt(json, "batKills");
        data.healerUnlocked = json.contains("\"healerUnlocked\": true");
        data.bitterPotionAccepted = json.contains("\"bitterPotionAccepted\": true");
        data.floor20ReportAccepted = json.contains("\"floor20ReportAccepted\": true");
        data.floor20ReachedInRun = json.contains("\"floor20ReachedInRun\": true");
        data.caveBonesAccepted = json.contains("\"caveBonesAccepted\": true");
        data.skeletonKills = readInt(json, "skeletonKills");
        data.hermitUnlocked = json.contains("\"hermitUnlocked\": true");
        data.swampWhisperAccepted = json.contains("\"swampWhisperAccepted\": true");
        data.witchKills = readInt(json, "witchKills");
        data.mistSealAccepted = json.contains("\"mistSealAccepted\": true");
        data.mineShiftAccepted = json.contains("\"mineShiftAccepted\": true");
        data.golemGuardAccepted = json.contains("\"golemGuardAccepted\": true");
        data.golemDefeatedForQuest = json.contains("\"golemDefeatedForQuest\": true");
        data.forgeMasterUnlocked = json.contains("\"forgeMasterUnlocked\": true");
        data.threeSparksAccepted = json.contains("\"threeSparksAccepted\": true");
        data.successfulSmelts = readInt(json, "successfulSmelts");
        data.soulSocketAccepted = json.contains("\"soulSocketAccepted\": true");
        data.gemInsertedForQuest = json.contains("\"gemInsertedForQuest\": true");
        data.extraGemStashSlots = readInt(json, "extraGemStashSlots");
        data.shopDiscountPercent = readInt(json, "shopDiscountPercent");
        data.starostaUnlocked = json.contains("\"starostaUnlocked\": true");
        data.brokenWagonAccepted = json.contains("\"brokenWagonAccepted\": true");
        data.roadRaidersAccepted = json.contains("\"roadRaidersAccepted\": true");
        data.roadOrcKills = readInt(json, "roadOrcKills");
        data.stolenCargoAccepted = json.contains("\"stolenCargoAccepted\": true");
        data.roadWeaponsDelivered = readInt(json, "roadWeaponsDelivered");
        data.bossFloor20Accepted = json.contains("\"bossFloor20Accepted\": true");
        data.bossFloor20Defeated = json.contains("\"bossFloor20Defeated\": true");
        data.merchantOffersJson = extractArray(json, "merchantOffers");
        return data;
    }

    private List<RovingMerchant.Offer> parseMerchantOffers(String arrayJson) {
        List<RovingMerchant.Offer> offers = new ArrayList<>();
        if (arrayJson == null || arrayJson.length() < 2) {
            return offers;
        }
        String content = arrayJson.substring(1, arrayJson.length() - 1);
        int i = 0;
        while (i < content.length()) {
            while (i < content.length() && (content.charAt(i) == ',' || Character.isWhitespace(content.charAt(i)))) {
                i++;
            }
            if (i >= content.length() || content.charAt(i) != '{') {
                break;
            }
            int end = findClosingBrace(content, i);
            String obj = content.substring(i, end + 1);
            int price = readInt(obj, "price");
            boolean sold = obj.contains("\"sold\": true");
            Item item = null;
            String itemJson = readRawField(obj, "item");
            if (itemJson != null && !"null".equals(itemJson)) {
                item = ItemSerializer.fromJson(itemJson);
            }
            offers.add(new RovingMerchant.Offer(item, price, sold));
            i = end + 1;
        }
        return offers;
    }

    private List<EquipmentEntry> parseEquipment(String json) {
        String block = extractArray(json, "equipment");
        if (block == null) {
            return new ArrayList<>();
        }
        return parseEquipmentBlock(block);
    }

    private List<MercenaryEntry> parseMercenaries(String json) {
        List<MercenaryEntry> list = new ArrayList<>();
        String block = extractArray(json, "mercenaries");
        if (block == null || block.length() < 2) {
            return list;
        }
        String content = block.substring(1, block.length() - 1);
        int i = 0;
        while (i < content.length()) {
            while (i < content.length() && (content.charAt(i) == ',' || Character.isWhitespace(content.charAt(i)))) {
                i++;
            }
            if (i >= content.length()) {
                break;
            }
            if (content.startsWith("null", i)) {
                i += 4;
                continue;
            }
            if (content.charAt(i) == '{') {
                int end = findClosingBrace(content, i);
                String entryJson = content.substring(i, end + 1);
                MercenaryEntry me = new MercenaryEntry();
                me.slot = readInt(entryJson, "slot");
                me.name = readString(entryJson, "name");
                me.characterClass = readString(entryJson, "class");
                if (me.characterClass.isEmpty()) {
                    me.characterClass = readField(entryJson, "class");
                }
                me.level = readInt(entryJson, "level");
                if (me.level == 0) {
                    me.level = 1;
                }
                me.xp = readInt(entryJson, "xp");
                me.skillTreeJson = extractObject(entryJson, "skillTree");
                me.equipment = parseEquipmentFromObject(entryJson);
                list.add(me);
                i = end + 1;
            } else {
                break;
            }
        }
        return list;
    }

    private List<EquipmentEntry> parseEquipmentFromObject(String json) {
        String block = extractArray(json, "equipment");
        if (block == null) {
            return new ArrayList<>();
        }
        return parseEquipmentBlock(block);
    }

    private List<EquipmentEntry> parseEquipmentBlock(String block) {
        List<EquipmentEntry> entries = new ArrayList<>();
        int i = 0;
        while (i < block.length()) {
            int start = block.indexOf('{', i);
            if (start < 0) {
                break;
            }
            int end = findClosingBrace(block, start);
            String entryJson = block.substring(start, end + 1);
            EquipmentEntry entry = new EquipmentEntry();
            entry.slot = Equipment.Slot.valueOf(readField(entryJson, "slot"));
            String itemPart = readRawField(entryJson, "item");
            entry.item = ItemSerializer.fromJson(itemPart);
            entries.add(entry);
            i = end + 1;
        }
        return entries;
    }

    private List<Item> parseInventory(String json) {
        List<Item> items = new ArrayList<>();
        String block = extractArray(json, "inventory");
        if (block == null || block.length() < 2) {
            return items;
        }
        String content = block.substring(1, block.length() - 1);
        int i = 0;
        while (i < content.length()) {
            while (i < content.length() && (content.charAt(i) == ',' || Character.isWhitespace(content.charAt(i)))) {
                i++;
            }
            if (i >= content.length()) {
                break;
            }
            if (content.startsWith("null", i)) {
                items.add(null);
                i += 4;
            } else if (content.charAt(i) == '{') {
                int end = findClosingBrace(content, i);
                items.add(ItemSerializer.fromJson(content.substring(i, end + 1)));
                i = end + 1;
            } else {
                break;
            }
        }
        return items;
    }

    private int findClosingBrace(String text, int openIndex) {
        return findMatchingDelimiter(text, openIndex, '{', '}');
    }

    private int findClosingBracket(String text, int openIndex) {
        return findMatchingDelimiter(text, openIndex, '[', ']');
    }

    /** Учитывает вложенность и строки в кавычках. */
    private int findMatchingDelimiter(String text, int openIndex, char open, char close) {
        int depth = 0;
        boolean inString = false;
        boolean escape = false;
        for (int i = openIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escape) {
                escape = false;
                continue;
            }
            if (inString) {
                if (c == '\\') {
                    escape = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
                continue;
            }
            if (c == open) {
                depth++;
            } else if (c == close) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return text.length() - 1;
    }

    private String readField(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        return m.find() ? m.group(1) : "";
    }

    private String readRawField(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:").matcher(json);
        if (!m.find()) {
            return "null";
        }
        int i = m.end();
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
            i++;
        }
        if (json.startsWith("null", i)) {
            return "null";
        }
        if (json.charAt(i) == '{') {
            int end = findClosingBrace(json, i);
            return json.substring(i, end + 1);
        }
        return "null";
    }

    private String extractArray(String json, String key) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\\[").matcher(json);
        if (!m.find()) {
            return null;
        }
        int start = m.end() - 1;
        int end = findClosingBracket(json, start);
        return json.substring(start, end + 1);
    }

    private int readInt(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)").matcher(json);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    private long readLong(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)").matcher(json);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : 0L;
    }

    private String readString(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        return matcher.find() ? matcher.group(1) : CharacterClass.WARRIOR.name();
    }

    public void applyToMeta(SaveData data, MetaProgression meta) {
        if (data == null) {
            return;
        }
        meta.setSoulCrystals(data.soulCrystals);
        meta.setDamageLevel(data.damageLevel);
        meta.setHealthLevel(data.healthLevel);
        meta.setDropLevel(data.dropLevel);
        meta.setBestFloor(data.bestFloor);
        meta.setTotalKills(data.totalKills);
        meta.setTotalDeaths(data.totalDeaths);
        meta.setTotalGoldEarned(data.totalGoldEarned);
        meta.setTotalItemsFound(data.totalItemsFound);
        meta.setTotalPlayTimeSeconds(data.totalPlayTimeSeconds);
    }

    public void applyToPlayer(SaveData data, Player player) {
        if (data == null) {
            return;
        }
        if (data.equipment != null) {
            player.getEquipment().clear();
            for (EquipmentEntry entry : data.equipment) {
                if (entry.item != null) {
                    player.getEquipment().equip(entry.slot, entry.item);
                }
            }
        }
        if (data.inventory != null) {
            player.getInventory().clear();
            player.getInventory().setMaxSize(data.inventoryMaxSize);
            for (int i = 0; i < data.inventory.size() && i < player.getInventory().getMaxSize(); i++) {
                player.getInventory().set(i, data.inventory.get(i));
            }
        }
        Party party = player.getParty();
        party.setUnlockedMercSlots(data.partySlotsUnlocked);
        for (int i = 0; i < Party.MAX_MERC_SLOTS; i++) {
            party.setMercenary(i, null);
        }
        if (data.mercenaries != null) {
            LevelService levelService = new LevelService();
            for (MercenaryEntry me : data.mercenaries) {
                if (me != null && me.name != null && !me.name.isEmpty()) {
                    CharacterClass cls = CharacterClass.valueOf(me.characterClass);
                    Mercenary m = new Mercenary(me.name, cls, me.level, me.xp, levelService);
                    if (me.skillTreeJson != null) {
                        m.getSkillTree().loadSnapshot(parseSkillTreeProgress(me.skillTreeJson));
                    }
                    if (me.equipment != null) {
                        for (EquipmentEntry entry : me.equipment) {
                            if (entry.item != null) {
                                m.getEquipment().equip(entry.slot, entry.item);
                            }
                        }
                    }
                    party.setMercenary(me.slot, m);
                }
            }
        }
        if (data.skillTreesJson != null) {
            player.getSkillTrees().loadSnapshot(parsePlayerSkillTrees(data.skillTreesJson));
        }
        player.setBlessingScrolls(data.blessingScrolls);
        player.setXpScrolls(data.xpScrolls);
        player.setLuckScrolls(data.luckScrolls);
        player.setGreedScrolls(data.greedScrolls);
        player.setFogScrolls(data.fogScrolls);
        player.setSecondBreathScrolls(data.secondBreathScrolls);
        player.setAutoSellRarities(parseAutoSellRarities(data.autoSellRaritiesJson));
        player.setRawOre(data.rawOre);
        player.clearGemStash();
        for (item.Gem gem : parseGemStash(data.gemStashJson)) {
            player.addGem(gem);
        }
        player.setLastGolemRewardEpochDay(data.lastGolemRewardEpochDay);
        player.setExtraGemStashSlots(data.extraGemStashSlots);
        player.setShopDiscountPercent(data.shopDiscountPercent);
        player.getRovingMerchant().loadOffers(parseMerchantOffers(data.merchantOffersJson));
        applyQuestLog(data, player.getQuestLog());
    }

    private void applyQuestLog(SaveData data, QuestLog log) {
        if (data == null || log == null) {
            return;
        }
        log.loadCompleted(parseQuestsCompleted(data.questsCompletedJson));
        log.setGoblinHuntAccepted(data.goblinHuntAccepted);
        log.setGoblinKills(data.goblinKills);
        log.setMilitiaUnlocked(data.militiaUnlocked);
        log.setMilitiaQuestAccepted(data.militiaQuestAccepted);
        log.setWeaponsDelivered(data.questWeaponsDelivered);
        log.setArmorDelivered(data.questArmorDelivered);
        log.setNightPackAccepted(data.nightPackAccepted);
        log.setWolfKills(data.wolfKills);
        log.setBatKills(data.batKills);
        log.setHealerUnlocked(data.healerUnlocked);
        log.setBitterPotionAccepted(data.bitterPotionAccepted);
        log.setFloor20ReportAccepted(data.floor20ReportAccepted);
        log.setFloor20ReachedInRun(data.floor20ReachedInRun);
        log.setCaveBonesAccepted(data.caveBonesAccepted);
        log.setSkeletonKills(data.skeletonKills);
        log.setHermitUnlocked(data.hermitUnlocked);
        log.setSwampWhisperAccepted(data.swampWhisperAccepted);
        log.setWitchKills(data.witchKills);
        log.setMistSealAccepted(data.mistSealAccepted);
        log.setMineShiftAccepted(data.mineShiftAccepted);
        log.setGolemGuardAccepted(data.golemGuardAccepted);
        log.setGolemDefeatedForQuest(data.golemDefeatedForQuest);
        log.setForgeMasterUnlocked(data.forgeMasterUnlocked);
        log.setThreeSparksAccepted(data.threeSparksAccepted);
        log.setSuccessfulSmelts(data.successfulSmelts);
        log.setSoulSocketAccepted(data.soulSocketAccepted);
        log.setGemInsertedForQuest(data.gemInsertedForQuest);
        log.setStarostaUnlocked(data.starostaUnlocked);
        log.setBrokenWagonAccepted(data.brokenWagonAccepted);
        log.setRoadRaidersAccepted(data.roadRaidersAccepted);
        log.setRoadOrcKills(data.roadOrcKills);
        log.setStolenCargoAccepted(data.stolenCargoAccepted);
        log.setRoadWeaponsDelivered(data.roadWeaponsDelivered);
        log.setBossFloor20Accepted(data.bossFloor20Accepted);
        log.setBossFloor20Defeated(data.bossFloor20Defeated);
        if (log.isCompleted(QuestId.GOBLIN_HUNT)) {
            log.unlockMilitia();
        }
        if (log.isCompleted(QuestId.NIGHT_PACK)) {
            log.unlockHealer();
        }
        if (log.isCompleted(QuestId.CAVE_BONES)) {
            log.unlockHermit();
        }
        if (log.isCompleted(QuestId.GOLEM_GUARD)) {
            log.unlockForgeMaster();
        }
        if (log.isCompleted(QuestId.MIST_SEAL)) {
            log.unlockStarosta();
        }
    }

    private String serializeQuestsCompleted(QuestLog log) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (QuestId id : QuestId.values()) {
            if (log.isCompleted(id)) {
                if (!first) {
                    sb.append(",");
                }
                first = false;
                sb.append("\"").append(id.name()).append("\"");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private java.util.Set<QuestId> parseQuestsCompleted(String json) {
        java.util.EnumSet<QuestId> out = java.util.EnumSet.noneOf(QuestId.class);
        if (json == null || json.isBlank() || "[]".equals(json.trim())) {
            return out;
        }
        java.util.regex.Matcher m = Pattern.compile("\"([A-Z0-9_]+)\"").matcher(json);
        while (m.find()) {
            try {
                out.add(QuestId.valueOf(m.group(1)));
            } catch (IllegalArgumentException ignored) {
                // skip
            }
        }
        return out;
    }

    private String serializeAutoSell(java.util.Set<item.Rarity> rarities) {
        if (rarities == null || rarities.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (item.Rarity r : item.Rarity.values()) {
            if (rarities.contains(r)) {
                if (!first) {
                    sb.append(",");
                }
                first = false;
                sb.append("\"").append(r.name()).append("\"");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private java.util.Set<item.Rarity> parseAutoSellRarities(String json) {
        java.util.EnumSet<item.Rarity> out = java.util.EnumSet.noneOf(item.Rarity.class);
        if (json == null || json.isBlank() || "[]".equals(json.trim())) {
            return out;
        }
        java.util.regex.Matcher m = Pattern.compile("\"([A-Z]+)\"").matcher(json);
        while (m.find()) {
            try {
                out.add(item.Rarity.valueOf(m.group(1)));
            } catch (IllegalArgumentException ignored) {
                // skip
            }
        }
        return out;
    }

    public boolean readAutoRestart(SaveData data) {
        return data != null && data.autoRestartOnDeath;
    }

    private String serializeSkillTrees(progression.skill.SkillTreeProgress.PlayerTrees trees) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (var e : trees.snapshot().entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append("\"").append(e.getKey()).append("\":").append(serializeRankMap(e.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    private String serializeSkillTreeProgress(progression.skill.SkillTreeProgress progress) {
        return serializeRankMap(progress.snapshot());
    }

    private String serializeRankMap(java.util.Map<String, Integer> ranks) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (var e : ranks.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append("\"").append(e.getKey()).append("\":").append(e.getValue());
        }
        sb.append("}");
        return sb.toString();
    }

    private java.util.Map<String, java.util.Map<String, Integer>> parsePlayerSkillTrees(String json) {
        java.util.Map<String, java.util.Map<String, Integer>> out = new java.util.HashMap<>();
        if (json == null || json.isBlank() || "{}".equals(json.trim())) {
            return out;
        }
        String body = json.trim();
        if (body.startsWith("{")) {
            body = body.substring(1, body.length() - 1);
        }
        int i = 0;
        while (i < body.length()) {
            int keyStart = body.indexOf('"', i);
            if (keyStart < 0) {
                break;
            }
            int keyEnd = body.indexOf('"', keyStart + 1);
            String className = body.substring(keyStart + 1, keyEnd);
            int colon = body.indexOf(':', keyEnd);
            int objStart = body.indexOf('{', colon);
            int objEnd = findClosingBrace(body, objStart);
            out.put(className, parseSkillTreeProgress(body.substring(objStart, objEnd + 1)));
            i = objEnd + 1;
        }
        return out;
    }

    private java.util.Map<String, Integer> parseSkillTreeProgress(String json) {
        java.util.Map<String, Integer> out = new java.util.HashMap<>();
        if (json == null || json.isBlank()) {
            return out;
        }
        java.util.regex.Matcher m = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\\d+)").matcher(json);
        while (m.find()) {
            out.put(m.group(1), Integer.parseInt(m.group(2)));
        }
        return out;
    }

    private String serializeGemStash(java.util.List<item.Gem> gems) {
        if (gems == null || gems.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < gems.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            item.Gem gem = gems.get(i);
            sb.append("{\"type\":\"").append(gem.getType().name()).append("\",");
            sb.append("\"tier\":\"").append(gem.getTier().name()).append("\",");
            sb.append("\"mult\":").append(gem.getStatMultiplier()).append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private java.util.List<item.Gem> parseGemStash(String json) {
        java.util.List<item.Gem> list = new java.util.ArrayList<>();
        if (json == null || json.isBlank() || "[]".equals(json.trim())) {
            return list;
        }
        String content = json.substring(1, json.length() - 1);
        int i = 0;
        while (i < content.length()) {
            while (i < content.length() && (content.charAt(i) == ',' || Character.isWhitespace(content.charAt(i)))) {
                i++;
            }
            if (i >= content.length()) {
                break;
            }
            if (content.charAt(i) == '{') {
                int end = findClosingBrace(content, i);
                String gemJson = content.substring(i, end + 1);
                item.GemType type = item.GemType.valueOf(readField(gemJson, "type"));
                item.GemTier tier = item.GemTier.valueOf(readField(gemJson, "tier"));
                double mult = readDouble(gemJson, "mult");
                if (mult <= 0) {
                    mult = 1.0;
                }
                list.add(new item.Gem(type, tier, mult));
                i = end + 1;
            } else {
                break;
            }
        }
        return list;
    }

    private double readDouble(String json, String key) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\"" + key + "\"\\s*:\\s*([\\d.]+)").matcher(json);
        return matcher.find() ? Double.parseDouble(matcher.group(1)) : 0;
    }

    private String extractObject(String json, String key) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\\{").matcher(json);
        if (!m.find()) {
            return null;
        }
        int start = m.end() - 1;
        int end = findClosingBrace(json, start);
        return json.substring(start, end + 1);
    }

    public static class SaveData {
        public int soulCrystals;
        public int damageLevel;
        public int healthLevel;
        public int dropLevel;
        public int bestFloor;
        public int totalKills;
        public int totalDeaths;
        public long totalGoldEarned;
        public int totalItemsFound;
        public long totalPlayTimeSeconds;
        public String selectedClass;
        public int playerLevel;
        public int playerXp;
        public int playerGold;
        public int goldTrainingHp;
        public int goldTrainingAtk;
        public int inventoryExpansions;
        public int inventoryMaxSize;
        public boolean autoRestartOnDeath;
        public List<EquipmentEntry> equipment;
        public List<Item> inventory;
        public int partySlotsUnlocked;
        public List<MercenaryEntry> mercenaries;
        public int ascensionKeys;
        public boolean ascensionUnlocked;
        public int abdolbosPotions;
        public int blessingScrolls;
        public int xpScrolls;
        public int luckScrolls;
        public int greedScrolls;
        public int fogScrolls;
        public int secondBreathScrolls;
        public String autoSellRaritiesJson;
        public String skillTreesJson;
        public int rawOre;
        public String gemStashJson;
        public long lastGolemRewardEpochDay;
        public String questsCompletedJson;
        public boolean goblinHuntAccepted;
        public int goblinKills;
        public boolean militiaUnlocked;
        public boolean militiaQuestAccepted;
        public int questWeaponsDelivered;
        public int questArmorDelivered;
        public boolean nightPackAccepted;
        public int wolfKills;
        public int batKills;
        public boolean healerUnlocked;
        public boolean bitterPotionAccepted;
        public boolean floor20ReportAccepted;
        public boolean floor20ReachedInRun;
        public boolean caveBonesAccepted;
        public int skeletonKills;
        public boolean hermitUnlocked;
        public boolean swampWhisperAccepted;
        public int witchKills;
        public boolean mistSealAccepted;
        public boolean mineShiftAccepted;
        public boolean golemGuardAccepted;
        public boolean golemDefeatedForQuest;
        public boolean forgeMasterUnlocked;
        public boolean threeSparksAccepted;
        public int successfulSmelts;
        public boolean soulSocketAccepted;
        public boolean gemInsertedForQuest;
        public int extraGemStashSlots;
        public int shopDiscountPercent;
        public boolean starostaUnlocked;
        public boolean brokenWagonAccepted;
        public boolean roadRaidersAccepted;
        public int roadOrcKills;
        public boolean stolenCargoAccepted;
        public int roadWeaponsDelivered;
        public boolean bossFloor20Accepted;
        public boolean bossFloor20Defeated;
        public String merchantOffersJson;
    }

    public static class MercenaryEntry {
        public int slot;
        public String name;
        public String characterClass;
        public int level;
        public int xp;
        public String skillTreeJson;
        public List<EquipmentEntry> equipment;
    }

    public static class EquipmentEntry {
        public Equipment.Slot slot;
        public Item item;
    }
}
