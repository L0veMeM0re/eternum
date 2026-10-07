package town;

import player.Player;
import util.ConsoleColors;
import util.ScreenFrame;
import util.StripClubRenderer;

import java.util.function.Supplier;

/** Стриптиз-клуб — донаты за смену арта (прогресс сбрасывается при выходе). */
public final class StripClub {
    private int currentStage;

    private StripClub() {
        this.currentStage = 0;
    }

    public static void open(Player player, Supplier<String> readLine, Runnable onSave) {
        new StripClub().run(player, readLine, onSave);
    }

    private void run(Player player, Supplier<String> readLine, Runnable onSave) {
        boolean back = false;
        while (!back) {
            ScreenFrame.openPath(ScreenFrame.Screen.STRIP_CLUB, "Главное", "Площадь");
            StripClubRenderer.printClubBanner();
            System.out.println("  Золото: " + ConsoleColors.wrap(ConsoleColors.YELLOW, String.valueOf(player.getGold())));
            StripClubRenderer.printDancer(currentStage);
            if (currentStage >= StripClubRenderer.STAGES) {
                System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_MAGENTA,
                        "  ★ " + StripClubRenderer.DANCER_NAME + " показала всё, что планировала на сегодня."));
                ScreenFrame.back();
                ScreenFrame.prompt();
                if ("0".equals(readLine.get().trim())) {
                    back = true;
                }
                continue;
            }
            int cost = StripClubRenderer.costForStage(currentStage);
            ScreenFrame.section("Чаевые");
            System.out.println("  1. Дать " + cost + " G — "
                    + StripClubRenderer.stageTitle(currentStage)
                    + " (" + (currentStage + 1) + "/" + StripClubRenderer.STAGES + ")");
            ScreenFrame.back();
            ScreenFrame.prompt();
            switch (readLine.get().trim()) {
                case "1" -> tip(player, cost, readLine, onSave);
                case "0" -> back = true;
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    private void tip(Player player, int cost, Supplier<String> readLine, Runnable onSave) {
        if (!player.spendGold(cost)) {
            System.out.println("  Недостаточно золота (нужно " + cost + " G).");
            return;
        }
        onSave.run();
        currentStage++;
        System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_YELLOW,
                "\n  ♥ +" + cost + " G — " + StripClubRenderer.DANCER_NAME + " выходит на сцену...\n"));
        StripClubRenderer.printDancer(currentStage);
        if (currentStage >= StripClubRenderer.STAGES) {
            System.out.println(ConsoleColors.wrap(ConsoleColors.BRIGHT_MAGENTA,
                    "  ★ Финал! Спасибо за щедрость."));
        }
        System.out.println("\n  [Enter]");
        readLine.get();
    }
}
