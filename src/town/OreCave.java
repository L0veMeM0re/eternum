package town;

import player.Player;
import progression.MetaProgression;
import util.ConsoleColors;
import util.ScreenFrame;

import java.util.function.Supplier;

/** Пещера / шахта — добыча руды и голем. */
public final class OreCave {
    private OreCave() {
    }

    public static void open(Player player, MetaProgression meta,
                            Supplier<String> readLine, Runnable onSave) {
        boolean back = false;
        while (!back) {
            ScreenFrame.openPath(ScreenFrame.Screen.ORE_CAVE, "Главное", "Площадь");
            printBanner();
            printResources(player);
            printGuide();
            printMiningMenu();
            printGolemMenu();
            ScreenFrame.back();
            ScreenFrame.prompt();
            switch (readLine.get().trim()) {
                case "1" -> MiningMinigame.run(player, MiningPhrases.Difficulty.CALM, readLine, onSave);
                case "2" -> MiningMinigame.run(player, MiningPhrases.Difficulty.DEEP, readLine, onSave);
                case "3" -> MiningMinigame.run(player, MiningPhrases.Difficulty.COLLAPSE, readLine, onSave);
                case "4" -> golemMenu(player, meta, readLine, onSave);
                case "0" -> back = true;
                default -> System.out.println("  Неверный выбор.");
            }
        }
    }

    private static void printResources(Player player) {
        System.out.println();
        System.out.println(ConsoleColors.wrap(ConsoleColors.CYAN, "  💰 ── Ресурсы ──"));
        System.out.println("  " + ConsoleColors.wrap(ConsoleColors.YELLOW, "G")
                + " Золото: " + player.getGold());
        System.out.println("  " + ConsoleColors.wrap(ConsoleColors.BRIGHT_CYAN, "⛏")
                + " Руда: " + player.getRawOre());
        if (player.isGolemRewardClaimedToday()) {
            System.out.println(ConsoleColors.dim("  ⚠ Награда голема сегодня уже получена (повтор — меньше руды)"));
        }
    }

    private static void printGuide() {
        System.out.println();
        System.out.println(ConsoleColors.wrap(ConsoleColors.CYAN, "  ℹ ── Гайд ──"));
        System.out.println(ConsoleColors.dim("  Руда нужна в кузнице (площадь → 8): переплавка и слияние."));
        System.out.println(ConsoleColors.dim("  Копание: реши пример за отведённое время. Ошибка = 0 руды."));
        System.out.println(ConsoleColors.dim(
                "  * Тут должен был быть ввод предложений на время, но консоль не дружит с русским языком ("));
    }

    private static void printMiningMenu() {
        int cost = MiningPhrases.attemptCost();
        System.out.println();
        System.out.println(ConsoleColors.wrap(ConsoleColors.CYAN, "  ⛏ ── Копание ──"));
        System.out.println("  1. 🪨 Спокойная жила");
        System.out.println(ConsoleColors.dim("      " + cost + " G · 30 сек · 1 руда · лёгкий пример"));
        System.out.println("  2. ⛰ Глубокий штрек");
        System.out.println(ConsoleColors.dim("      " + cost + " G · 20 сек · 2 руды · средний пример"));
        System.out.println("  3. ⚠ Риск обвала");
        System.out.println(ConsoleColors.dim("      " + cost + " G · 12 сек · 4 руды · сложный пример"));
    }

    private static void printGolemMenu() {
        System.out.println();
        System.out.println(ConsoleColors.wrap(ConsoleColors.CYAN, "  ☗ ── Босс ──"));
        System.out.println("  4. 🗿 Страж жилы — голем");
        System.out.println(ConsoleColors.dim("      Вся партия · 6–12 руды · 1 полная награда в день"));
    }

    private static void golemMenu(Player player, MetaProgression meta,
                                  Supplier<String> readLine, Runnable onSave) {
        System.out.println();
        System.out.println(ConsoleColors.wrap(ConsoleColors.CYAN, "  🎲 ── Подход к голему ──"));
        System.out.println(ConsoleColors.dim("  Цена боя нет · награда зависит от риска"));
        System.out.println();
        System.out.println("  1. 🛡 Осторожный");
        System.out.println(ConsoleColors.dim("      Голем −10% статов · руда −30%"));
        System.out.println("  2. ⚔ Прямой бой");
        System.out.println(ConsoleColors.dim("      Стандартная сложность и награда"));
        System.out.println("  3. 🔥 Провокация");
        System.out.println(ConsoleColors.dim("      Голем сильнее · руда ×1.5"));
        System.out.println(ConsoleColors.dim("  0 — отмена"));
        System.out.print("  ▶ ");
        OreGolemFight.GolemRisk risk = switch (readLine.get().trim()) {
            case "1" -> OreGolemFight.GolemRisk.CAREFUL;
            case "2" -> OreGolemFight.GolemRisk.NORMAL;
            case "3" -> OreGolemFight.GolemRisk.PROVOKE;
            default -> null;
        };
        if (risk == null) {
            System.out.println(ConsoleColors.dim("  Отмена."));
            return;
        }
        OreGolemFight.fight(player, meta, risk, readLine, onSave);
    }

    private static void printBanner() {
        System.out.println();
        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_CYAN,
                "  ╔══════════════════════════════════════╗"));
        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_CYAN,
                "  ║   ⛏  Ш А Х Т А   Р У Д Ы  ⛏        ║"));
        System.out.println(ConsoleColors.wrap(ConsoleColors.GRAY,
                "  ║     копание · голем · руда           ║"));
        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_CYAN,
                "  ╚══════════════════════════════════════╝"));
    }
}
