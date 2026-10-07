package town;

import player.Player;
import util.ConsoleColors;
import util.ScreenFrame;

import java.util.function.Supplier;

/** Игорный клуб — ставки на гладиаторов. */
public final class GamblingClub {
    private GladiatorMatch currentMatch;

    private GamblingClub() {
        this.currentMatch = GladiatorGenerator.generate();
    }

    public static void open(Player player, Supplier<String> readLine, Runnable onSave) {
        new GamblingClub().run(player, readLine, onSave);
    }

    private void run(Player player, Supplier<String> readLine, Runnable onSave) {
        boolean back = false;
        while (!back) {
            ScreenFrame.openPath(ScreenFrame.Screen.GAMBLING_CLUB, "Главное", "Площадь");
            printBanner();
            System.out.println("  Золото: " + ConsoleColors.wrap(ConsoleColors.YELLOW, String.valueOf(player.getGold())));
            System.out.println("  Режим: " + ConsoleColors.bold(currentMatch.formatMode())
                    + "  ·  " + currentMatch.getTier().title);
            printMatchCards(currentMatch);
            GamblingOdds.OddsPair odds = GamblingOdds.calculate(currentMatch.getRed(), currentMatch.getBlue());
            printOdds(odds);
            System.out.println(ConsoleColors.dim("  Лимит ставки: " + GamblingOdds.MIN_BET + "–"
                    + GamblingOdds.MAX_BET + " G"));
            ScreenFrame.section("Ставки");
            System.out.println("  1. Поставить на " + ConsoleColors.wrap(ConsoleColors.BRIGHT_RED, "красных")
                    + " (×" + odds.redOdds() + ")");
            System.out.println("  2. Поставить на " + ConsoleColors.wrap(ConsoleColors.BRIGHT_CYAN, "синих")
                    + " (×" + odds.blueOdds() + ")");
            System.out.println("  3. Следующий бой (новые бойцы)");
            ScreenFrame.back();
            ScreenFrame.prompt();
            switch (readLine.get().trim()) {
                case "1" -> placeBet(player, true, odds, readLine, onSave);
                case "2" -> placeBet(player, false, odds, readLine, onSave);
                case "3" -> currentMatch = GladiatorGenerator.generate();
                case "0" -> back = true;
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    private void placeBet(Player player, boolean onRed, GamblingOdds.OddsPair odds,
                          Supplier<String> readLine, Runnable onSave) {
        System.out.print("  Сумма ставки (" + GamblingOdds.MIN_BET + "–" + GamblingOdds.MAX_BET + " G): ");
        int bet = parsePositiveInt(readLine.get());
        if (bet <= 0) {
            System.out.println("  Нужна положительная сумма.");
            return;
        }
        if (bet < GamblingOdds.MIN_BET) {
            System.out.println("  Минимальная ставка: " + GamblingOdds.MIN_BET + " G.");
            return;
        }
        if (bet > GamblingOdds.MAX_BET) {
            System.out.println("  Максимальная ставка: " + GamblingOdds.MAX_BET + " G.");
            return;
        }
        if (bet > player.getGold()) {
            System.out.println("  Недостаточно золота.");
            return;
        }
        System.out.println("  1. Быстрый бой (сразу результат)");
        System.out.println("  2. Полный лог боя");
        System.out.print("  > ");
        boolean verbose = "2".equals(readLine.get().trim());
        if (!player.spendGold(bet)) {
            System.out.println("  Не удалось списать золото.");
            return;
        }
        onSave.run();
        double multiplier = onRed ? odds.redOdds() : odds.blueOdds();
        String sideName = onRed ? "Красные" : "Синие";
        System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                "\n  Ставка " + bet + " G на " + sideName + " (×" + multiplier + ")"));
        GladiatorArena.Winner winner = GladiatorArena.fight(
                currentMatch.getRed(), currentMatch.getBlue(), verbose);
        boolean won = (onRed && winner == GladiatorArena.Winner.RED)
                || (!onRed && winner == GladiatorArena.Winner.BLUE);
        if (won) {
            int payout = (int) Math.floor(bet * multiplier);
            int profit = GamblingOdds.netProfit(bet, multiplier);
            player.addGold(payout);
            onSave.run();
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                    "  ★ Выигрыш: +" + payout + " G (чистая прибыль +" + profit + " G)"));
        } else {
            System.out.println(ConsoleColors.wrap(ConsoleColors.RED,
                    "  ✗ Ставка проиграна (−" + bet + " G)"));
        }
        currentMatch = GladiatorGenerator.generate();
        System.out.println("\n  [Enter] далее");
        readLine.get();
    }

    private static void printBanner() {
        System.out.println();
        String[] art = {
                "  ╔══════════════════════════════════════╗",
                "  ║   🎲  И Г О Р Н Ы Й   К Л У Б  🎲   ║",
                "  ║      арена · ставки · гладиаторы     ║",
                "  ╚══════════════════════════════════════╝"
        };
        for (String line : art) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW, line));
        }
        System.out.println();
    }

    private static void printMatchCards(GladiatorMatch match) {
        printSideCard(match.getRed());
        System.out.println(ConsoleColors.dim("            ⚔ VS ⚔"));
        printSideCard(match.getBlue());
    }

    private static void printSideCard(GladiatorSide side) {
        System.out.println(ConsoleColors.wrap(side.getColor(), "  ┌─ " + side.getLabel() + " ─────────────"));
        for (Gladiator g : side.getFighters()) {
            StatsLine(g);
        }
        System.out.println(ConsoleColors.wrap(side.getColor(), "  └────────────────────────"));
    }

    private static void StatsLine(Gladiator g) {
        var s = g.getStats();
        System.out.println("  │ " + ConsoleColors.bold(g.getName()) + " · "
                + g.getCharacterClass().getDisplayName());
        System.out.println("  │ HP " + s.getMaxHp() + "  ATK " + s.getAttack()
                + "  DEF " + s.getDefense() + "  CRIT " + (int) s.getCritChance() + "%");
        System.out.println("  │ Сет: " + ConsoleColors.wrap(ConsoleColors.BRIGHT_MAGENTA, g.setLine()));
    }

    private static void printOdds(GamblingOdds.OddsPair odds) {
        System.out.println();
        System.out.println("  Коэффициенты: "
                + ConsoleColors.wrap(ConsoleColors.BRIGHT_RED, "красные ×" + odds.redOdds())
                + " (~" + odds.redWinPercent() + "%)  |  "
                + ConsoleColors.wrap(ConsoleColors.BRIGHT_CYAN, "синие ×" + odds.blueOdds())
                + " (~" + odds.blueWinPercent() + "%)");
        System.out.println(ConsoleColors.dim("  Маржа зала ~"
                + (int) Math.round((GamblingOdds.impliedOverround(odds.redOdds(), odds.blueOdds()) - 1) * 100)
                + "% · макс. выигрыш " + GamblingOdds.MAX_BET + "×"
                + GamblingOdds.MAX_ODDS + " = "
                + (int) Math.floor(GamblingOdds.MAX_BET * GamblingOdds.MAX_ODDS) + " G"));
    }

    private static int parsePositiveInt(String raw) {
        if (raw == null || raw.isBlank()) {
            return -1;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
