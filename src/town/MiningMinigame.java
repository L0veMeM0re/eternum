package town;

import player.Player;
import util.ConsoleColors;

import java.util.function.Supplier;

public final class MiningMinigame {
    private MiningMinigame() {
    }

    public static void run(Player player, MiningPhrases.Difficulty difficulty,
                           Supplier<String> readLine, Runnable onSave) {
        int cost = MiningPhrases.attemptCost();
        if (!player.spendGold(cost)) {
            System.out.println("  Нужно " + cost + " G за попытку копания.");
            return;
        }
        onSave.run();

        MiningPhrases.MathProblem problem = MiningPhrases.randomProblem(difficulty);
        System.out.println();
        System.out.println(ConsoleColors.wrap(ConsoleColors.YELLOW,
                "  Реши пример (" + difficulty.getSeconds() + " сек):"));
        System.out.println(ConsoleColors.bold("  » " + problem.question()));
        System.out.println(ConsoleColors.dim("  (только число · Enter в конце)"));
        System.out.println();
        System.out.print(ConsoleColors.wrap(ConsoleColors.WHITE, "  ▶ Ответ: "));
        System.out.flush();

        long start = System.currentTimeMillis();
        String input = readLine.get();
        long elapsed = System.currentTimeMillis() - start;

        if (MiningPhrases.checkAnswer(problem.answer(), input, elapsed, difficulty)) {
            int ore = difficulty.getOreReward();
            player.addRawOre(ore);
            onSave.run();
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_GREEN,
                    "  ★ Верно! +" + ore + " руды (" + String.format("%.1f", elapsed / 1000.0) + " сек)"));
        } else if (elapsed > difficulty.getSeconds() * 1000L) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.RED,
                    "  ✗ Не успел — жила обрушилась."));
        } else {
            System.out.println(ConsoleColors.wrap(ConsoleColors.RED,
                    "  ✗ Неверный ответ — руды нет."));
            System.out.println(ConsoleColors.dim(
                    "  (правильно: " + problem.answer() + ")"));
        }
        System.out.println("\n  [Enter — продолжить]");
        readLine.get();
    }
}
