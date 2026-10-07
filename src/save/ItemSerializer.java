package save;

import inventory.Equipment;
import item.Armor;
import item.Item;
import item.ItemSet;
import item.Rarity;
import item.Gem;
import item.GemTier;
import item.GemType;
import item.Weapon;

public final class ItemSerializer {
    private ItemSerializer() {
    }

    public static String toJson(Item item) {
        if (item == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"kind\":\"").append(item.getSlot() == Equipment.Slot.WEAPON ? "WEAPON" : "ARMOR").append("\",");
        sb.append("\"name\":\"").append(escape(item.getName())).append("\",");
        sb.append("\"rarity\":\"").append(item.getRarity().name()).append("\",");
        sb.append("\"slot\":\"").append(item.getSlot().name()).append("\",");
        sb.append("\"set\":\"").append(item.getItemSet().name()).append("\",");
        if (item instanceof Weapon weapon) {
            sb.append("\"atk\":").append(weapon.getBaseAttackBonus()).append(",");
            sb.append("\"def\":").append(item.getDefenseBonus()).append(",");
            sb.append("\"hp\":").append(0).append(",");
            sb.append("\"crit\":").append(weapon.getBaseCritBonus()).append(",");
            sb.append("\"critDmg\":").append(weapon.getBaseCritDamageBonus()).append(",");
            sb.append("\"dodge\":").append(weapon.getBaseDodgeBonus()).append(",");
            sb.append("\"vamp\":").append(weapon.getBaseVampirismChance()).append(",");
            sb.append("\"vampHeal\":").append(weapon.getBaseVampirismHealPercent()).append(",");
            sb.append("\"gems\":").append(serializeGems(weapon.getSocketedGems()));
        } else {
            sb.append("\"atk\":").append(item.getAttackBonus()).append(",");
            sb.append("\"def\":").append(item.getDefenseBonus()).append(",");
            sb.append("\"hp\":").append(item.getHpBonus()).append(",");
            sb.append("\"crit\":").append(item.getCritBonus()).append(",");
            sb.append("\"critDmg\":").append(item.getCritDamageBonus()).append(",");
            sb.append("\"dodge\":").append(item.getDodgeBonus()).append(",");
            sb.append("\"vamp\":").append(item.getVampirismChance()).append(",");
            sb.append("\"vampHeal\":").append(item.getVampirismHealPercent());
        }
        sb.append("}");
        return sb.toString();
    }

    public static Item fromJson(String json) {
        if (json == null || json.isBlank() || "null".equals(json.trim())) {
            return null;
        }
        String kind = readStringField(json, "kind");
        String name = readStringField(json, "name");
        Rarity rarity = Rarity.valueOf(readStringField(json, "rarity"));
        Equipment.Slot slot = Equipment.Slot.valueOf(readStringField(json, "slot"));
        ItemSet itemSet = readItemSet(json);
        int atk = readIntField(json, "atk");
        int def = readIntField(json, "def");
        int hp = readIntField(json, "hp");
        double crit = readDoubleField(json, "crit");
        double critDmg = readDoubleField(json, "critDmg");
        double dodge = readDoubleField(json, "dodge");
        double vamp = readDoubleField(json, "vamp");
        double vampHeal = readDoubleField(json, "vampHeal");

        if (itemSet == ItemSet.NONE) {
            itemSet = ItemSet.fromItemName(name);
        }

        if ("WEAPON".equals(kind)) {
            Gem[] gems = parseGems(json);
            return new Weapon(name, rarity, atk, crit, critDmg, dodge, vamp, vampHeal, gems);
        }
        return new Armor(name, rarity, slot, atk, def, hp, crit, critDmg, dodge, vamp, vampHeal);
    }

    private static ItemSet readItemSet(String json) {
        String raw = readStringField(json, "set");
        if (raw.isEmpty()) {
            return ItemSet.NONE;
        }
        try {
            return ItemSet.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return ItemSet.NONE;
        }
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String readStringField(String json, String key) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return m.find() ? m.group(1).replace("\\\"", "\"").replace("\\\\", "\\") : "";
    }

    private static int readIntField(String json, String key) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\"" + key + "\"\\s*:\\s*(-?\\d+)").matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }

    private static double readDoubleField(String json, String key) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\"" + key + "\"\\s*:\\s*(-?[\\d.]+)").matcher(json);
        return m.find() ? Double.parseDouble(m.group(1)) : 0;
    }

    private static String serializeGems(Gem[] gems) {
        if (gems == null || gems.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < gems.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            Gem gem = gems[i];
            if (gem == null) {
                sb.append("null");
            } else {
                sb.append("{\"type\":\"").append(gem.getType().name()).append("\",");
                sb.append("\"tier\":\"").append(gem.getTier().name()).append("\",");
                sb.append("\"mult\":").append(gem.getStatMultiplier()).append("}");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static Gem[] parseGems(String json) {
        String block = extractArray(json, "gems");
        if (block == null || block.length() < 2) {
            return null;
        }
        java.util.List<Gem> list = new java.util.ArrayList<>();
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
                list.add(null);
                i += 4;
            } else if (content.charAt(i) == '{') {
                int end = findClosingBrace(content, i);
                String gemJson = content.substring(i, end + 1);
                GemType type = GemType.valueOf(readStringField(gemJson, "type"));
                GemTier tier = GemTier.valueOf(readStringField(gemJson, "tier"));
                double mult = readDoubleField(gemJson, "mult");
                if (mult <= 0) {
                    mult = 1.0;
                }
                list.add(new Gem(type, tier, mult));
                i = end + 1;
            } else {
                break;
            }
        }
        return list.toArray(new Gem[0]);
    }

    private static String extractArray(String json, String key) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\"" + java.util.regex.Pattern.quote(key) + "\"\\s*:\\s*\\[").matcher(json);
        if (!m.find()) {
            return null;
        }
        int start = m.end() - 1;
        int end = findClosingBracket(json, start);
        return json.substring(start, end + 1);
    }

    private static int findClosingBrace(String text, int openIndex) {
        return findMatchingDelimiter(text, openIndex, '{', '}');
    }

    private static int findClosingBracket(String text, int openIndex) {
        return findMatchingDelimiter(text, openIndex, '[', ']');
    }

    private static int findMatchingDelimiter(String text, int openIndex, char open, char close) {
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
}
