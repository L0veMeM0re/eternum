package town;

import forge.ForgeCosts;
import forge.ForgeRiskMode;
import item.Gem;
import item.GemType;
import item.Weapon;
import player.Player;
import util.ConsoleColors;
import util.GemRenderer;
import util.ScreenFrame;

/** Оформление меню кузницы: блоки, иконки, серые подсказки. */
public final class ForgeRenderer {

    private ForgeRenderer() {
    }

    public static void printResources(Player player, int stashSize, int stashMax) {
        divider("💰", "Ресурсы");
        System.out.println("  " + ConsoleColors.wrap(ConsoleColors.YELLOW, "G")
                + " Золото: " + ConsoleColors.wrap(ConsoleColors.YELLOW, String.valueOf(player.getGold())));
        System.out.println("  " + ConsoleColors.wrap(ConsoleColors.BRIGHT_CYAN, "⛏")
                + " Руда: " + ConsoleColors.wrap(ConsoleColors.BRIGHT_CYAN, String.valueOf(player.getRawOre())));
        System.out.println("  " + ConsoleColors.wrap(ConsoleColors.BRIGHT_CYAN, "💎")
                + " Сундук: " + stashSize + "/" + stashMax + " камней");
    }

    public static void printQuickGuide() {
        divider("ℹ", "Краткий гайд");
        hint("Руду добывают в шахте (площадь → 9). Камни вставляются только в оружие.");
        hint("Гнёзда: EPIC — 1, LEGENDARY — 2, MYTHIC — 3. Один тип камня на оружие.");
        hint("Цена операции одна; режим риска влияет на шанс и бонус, не на стоимость.");
    }

    public static void printMainMenu() {
        divider("🔥", "Крафт");
        menuLine("1", "⚗", "Переплавка",
                "3 руды + " + ForgeCosts.SMELT_ORE + " G → случайный камень I");
        menuLine("2", "🔗", "Слияние",
                "3 одинаковых камня + золото → tier выше (выбор риска)");

        divider("⚔", "Сокеты");
        menuLine("3", "🔨", "Вставить камень",
                ForgeCosts.INSERT + " G · камень из сундука → оружие EPIC+");
        menuLine("4", "🧲", "Извлечь камень",
                ForgeCosts.EXTRACT + " G · 70% вернуть в сундук");

        divider("📦", "Склад и справка");
        menuLine("5", "💎", "Сундук камней", "список всех камней в запасе");
        menuLine("6", "📖", "Справка по типам", "что даёт каждый камень");
    }

    public static void printGemCodex() {
        ScreenFrame.openPath(util.ScreenFrame.Screen.FORGE, "Главное", "Площадь", "Справка");
        divider("📖", "Типы камней");
        for (GemType type : GemType.values()) {
            if (type == GemType.SHARD) {
                printShardCodex();
                continue;
            }
            Gem sample = new Gem(type, item.GemTier.I);
            System.out.println("  " + GemRenderer.coloredIcon(type) + " "
                    + GemRenderer.formatTypeName(type)
                    + ConsoleColors.dim(" — " + effectLine(sample)));
        }
        System.out.println();
        hint("Осколок — единственный камень без плоских статов: работает только в бою.");
        hint("Слияние «Безумная»: шанс +tier и +15% к статам камня.");
        hint("Вставка «Безумная»: +10% к эффекту или риск разбить камень.");
    }

    private static void printShardCodex() {
        System.out.println("  " + GemRenderer.coloredIcon(GemType.SHARD) + " "
                + GemRenderer.formatTypeName(GemType.SHARD)
                + ConsoleColors.dim(" — особый эффект (зависит от tier):"));
        for (item.GemTier tier : item.GemTier.values()) {
            Gem gem = new Gem(GemType.SHARD, tier);
            System.out.println("      "
                    + ConsoleColors.wrap(GemRenderer.tierColor(tier), tier.roman())
                    + ConsoleColors.dim(" — ")
                    + ConsoleColors.wrap(GemRenderer.color(GemType.SHARD), gem.procDescription()));
        }
    }

    private static String effectLine(Gem gem) {
        String full = gem.describe();
        int colon = full.indexOf(':');
        return colon >= 0 ? full.substring(colon + 2) : full;
    }

    public static void printSmeltConfirm(Player player) {
        divider("⚗", "Переплавка");
        hint("Списывается: 3 руды и " + ForgeCosts.SMELT_ORE + " G.");
        hint("Результат: 1 случайный камень tier I в сундук.");
        System.out.println("  Сейчас: руда " + player.getRawOre()
                + " · золото " + player.getGold() + " G");
    }

    public static void printMergeHeader() {
        divider("🔗", "Слияние камней");
        hint("Нужно 3 камня одного типа и tier в сундуке.");
        hint("I → II: " + ForgeCosts.MERGE_TO_II + " G  |  II → III: " + ForgeCosts.MERGE_TO_III + " G");
    }

    public static void printSocketHeader() {
        divider("🔨", "Вставка в оружие");
        hint("Камень уходит из сундука и садится в свободное гнездо.");
        hint("Тот же тип на этом оружии уже стоять не может.");
    }

    public static void printExtractHeader() {
        divider("🧲", "Извлечение");
        hint("Стоимость: " + ForgeCosts.EXTRACT + " G за попытку.");
        hint("70% — камень вернётся в сундук. 30% — камень разрушится.");
    }

    public static void printStash(Player player) {
        divider("💎", "Сундук камней");
        if (player.getGemStash().isEmpty()) {
            hint("(пусто — добывай руду в шахте или переплавляй)");
            return;
        }
        int i = 1;
        for (Gem gem : player.getGemStash()) {
            System.out.println("  " + ConsoleColors.dim(String.format("%2d.", i))
                    + " " + gem.describe());
            i++;
        }
    }

    public static void printGemTypesPicker() {
        divider("💠", "Выберите тип");
        GemType[] types = GemType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.println("  " + (i + 1) + ". " + GemRenderer.coloredIcon(types[i]) + " "
                    + GemRenderer.formatTypeName(types[i])
                    + ConsoleColors.dim(" (" + types[i].getStatLabel() + ")"));
        }
        hint("0 — отмена");
    }

    public static void printTierPicker() {
        divider("⬆", "Куда улучшаем");
        System.out.println("  1. " + ConsoleColors.wrap(ConsoleColors.WHITE, "I → II")
                + ConsoleColors.dim("   " + ForgeCosts.MERGE_TO_II + " G"));
        System.out.println("  2. " + ConsoleColors.wrap(ConsoleColors.WHITE, "II → III")
                + ConsoleColors.dim("  " + ForgeCosts.MERGE_TO_III + " G"));
        hint("0 — отмена");
    }

    public static void printMergeRiskPicker(int goldCost) {
        divider("🎲", "Режим слияния");
        hint("Цена: " + goldCost + " G · одинакова для всех режимов.");
        System.out.println();
        System.out.println("  1. " + ConsoleColors.wrap(ConsoleColors.WHITE, ForgeRiskMode.STABLE.getTitle()));
        subHint("~90% I→II / ~70% II→III · провал: теряешь 1 камень");
        System.out.println("  2. " + ConsoleColors.wrap(ConsoleColors.WHITE, ForgeRiskMode.NORMAL.getTitle()));
        subHint("~65% / ~45% · провал: теряешь 2 камня");
        System.out.println("  3. " + ConsoleColors.wrap(ConsoleColors.WHITE, ForgeRiskMode.MAD.getTitle()));
        subHint("~40% / ~25% · провал: все 3 · успех: +tier и +15% стат");
        hint("0 — отмена");
    }

    public static void printInsertRiskPicker() {
        divider("🎲", "Режим вставки");
        hint("Цена: " + ForgeCosts.INSERT + " G · одинакова для всех режимов.");
        System.out.println();
        System.out.println("  1. " + ConsoleColors.wrap(ConsoleColors.WHITE, ForgeRiskMode.STABLE.getTitle()));
        subHint("100% успех · камень без изменений");
        System.out.println("  2. " + ConsoleColors.wrap(ConsoleColors.WHITE, ForgeRiskMode.NORMAL.getTitle()));
        subHint("100% · ~5% шанс трещины (−1 tier)");
        System.out.println("  3. " + ConsoleColors.wrap(ConsoleColors.WHITE, ForgeRiskMode.MAD.getTitle()));
        subHint("85% · успех +10% стат · провал: камень разбит");
        hint("0 — отмена");
    }

    public static void printSocketPickerHeader() {
        divider("🧲", "Какое гнездо");
        hint("Выбери камень, который хочешь снять.");
    }

    public static void printWeaponPickerHeader() {
        divider("⚔", "Выберите оружие");
        hint("Только EPIC / LEGENDARY / MYTHIC. Ниже — занятые гнёзда.");
    }

    public static void printWeaponLine(int index, String owner, Weapon weapon) {
        String sockets = formatSockets(weapon);
        System.out.println("  " + index + ". " + ConsoleColors.wrap(ConsoleColors.WHITE, owner)
                + " — " + weapon.getDisplayName());
        System.out.println("     " + sockets);
        System.out.println("     " + ConsoleColors.dim(weapon.getStatDescription()));
    }

    public static void printGemPickerHeader() {
        divider("💎", "Выберите камень");
    }

    private static String formatSockets(Weapon weapon) {
        if (weapon.socketCount() == 0) {
            return "нет гнёзд";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(ConsoleColors.dim("Гнёзда " + weapon.socketCount() + ": "));
        Gem[] gems = weapon.getSocketedGems();
        for (int i = 0; i < gems.length; i++) {
            if (i > 0) {
                sb.append(" | ");
            }
            if (gems[i] == null) {
                sb.append(ConsoleColors.dim("[— пусто —]"));
            } else {
                sb.append(GemRenderer.formatSocket(gems[i]));
            }
        }
        return sb.toString();
    }

    private static void divider(String icon, String title) {
        System.out.println();
        System.out.println(ConsoleColors.wrap(ConsoleColors.CYAN, "  " + icon + " ── " + title + " ──"));
    }

    private static void menuLine(String num, String icon, String title, String hintText) {
        System.out.println("  " + num + ". " + icon + " "
                + ConsoleColors.wrap(ConsoleColors.WHITE, title));
        subHint(hintText);
    }

    /** Серая строка-подсказка (отступ 4 пробела). */
    private static void hint(String text) {
        System.out.println(ConsoleColors.dim("  " + text));
    }

    /** Подсказка под пунктом меню (отступ 6 пробелов). */
    private static void subHint(String text) {
        System.out.println(ConsoleColors.dim("      " + text));
    }
}
