package town;

import forge.ForgeCosts;
import util.RandomUtil;

/** Математические примеры для мини-игры добычи руды (уровень 9–11 класс). */
public final class MiningPhrases {
    public record MathProblem(String question, int answer) {
    }

    public enum Difficulty {
        CALM(30, 1),
        DEEP(20, 2),
        COLLAPSE(12, 4);

        private final int seconds;
        private final int oreReward;

        Difficulty(int seconds, int oreReward) {
            this.seconds = seconds;
            this.oreReward = oreReward;
        }

        public int getSeconds() {
            return seconds;
        }

        public int getOreReward() {
            return oreReward;
        }

        public String title() {
            return switch (this) {
                case CALM -> "Спокойная жила";
                case DEEP -> "Глубокий штрек";
                case COLLAPSE -> "Риск обвала";
            };
        }
    }

    private MiningPhrases() {
    }

    public static MathProblem randomProblem(Difficulty difficulty) {
        return switch (difficulty) {
            case CALM -> easyProblem();
            case DEEP -> mediumProblem();
            case COLLAPSE -> hardProblem();
        };
    }

    /** Простые действия: +, −, ×. */
    private static MathProblem easyProblem() {
        return switch (RandomUtil.range(0, 3)) {
            case 0 -> {
                int a = RandomUtil.range(12, 89);
                int b = RandomUtil.range(12, 89);
                yield new MathProblem(a + " + " + b + " = ?", a + b);
            }
            case 1 -> {
                int a = RandomUtil.range(40, 150);
                int b = RandomUtil.range(12, a - 5);
                yield new MathProblem(a + " − " + b + " = ?", a - b);
            }
            case 2 -> {
                int a = RandomUtil.range(6, 24);
                int b = RandomUtil.range(6, 24);
                yield new MathProblem(a + " × " + b + " = ?", a * b);
            }
            default -> {
                int b = RandomUtil.range(3, 12);
                int q = RandomUtil.range(6, 24);
                int a = b * q;
                yield new MathProblem(a + " ÷ " + b + " = ?", q);
            }
        };
    }

    /** Скобки, два действия, квадрат. */
    private static MathProblem mediumProblem() {
        return switch (RandomUtil.range(0, 3)) {
            case 0 -> {
                int a = RandomUtil.range(10, 40);
                int b = RandomUtil.range(5, 25);
                int c = RandomUtil.range(2, 9);
                yield new MathProblem("(" + a + " + " + b + ") × " + c + " = ?", (a + b) * c);
            }
            case 1 -> {
                int a = RandomUtil.range(50, 200);
                int b = RandomUtil.range(10, 40);
                int c = RandomUtil.range(5, 20);
                yield new MathProblem(a + " − " + b + " × " + c + " = ?", a - b * c);
            }
            case 2 -> {
                int n = RandomUtil.range(5, 15);
                yield new MathProblem(n + "² = ?", n * n);
            }
            default -> {
                int a = RandomUtil.range(2, 9);
                int b = RandomUtil.range(2, 9);
                int c = RandomUtil.range(10, 40);
                yield new MathProblem(a + " × " + b + " + " + c + " = ?", a * b + c);
            }
        };
    }

    /** Линейное уравнение, проценты, корень. */
    private static MathProblem hardProblem() {
        return switch (RandomUtil.range(0, 3)) {
            case 0 -> {
                int x = RandomUtil.range(3, 25);
                int a = RandomUtil.range(2, 9);
                int b = RandomUtil.range(5, 40);
                int c = a * x + b;
                yield new MathProblem(a + "x + " + b + " = " + c + "  →  x = ?", x);
            }
            case 1 -> {
                int x = RandomUtil.range(5, 30);
                int a = RandomUtil.range(2, 7);
                int b = RandomUtil.range(10, 50);
                int c = a * x - b;
                yield new MathProblem(a + "x − " + b + " = " + c + "  →  x = ?", x);
            }
            case 2 -> {
                int n = RandomUtil.range(2, 9);
                int sq = n * n;
                yield new MathProblem("√" + sq + " = ?", n);
            }
            default -> {
                int base = RandomUtil.range(20, 80) * 5;
                int pct = new int[]{10, 20, 25, 50}[RandomUtil.range(0, 3)];
                yield new MathProblem(pct + "% от " + base + " = ?", base * pct / 100);
            }
        };
    }

    public static boolean checkAnswer(int expected, String typed, long elapsedMs, Difficulty difficulty) {
        if (elapsedMs > difficulty.getSeconds() * 1000L) {
            return false;
        }
        Integer value = parseAnswer(typed);
        return value != null && value == expected;
    }

    private static Integer parseAnswer(String typed) {
        if (typed == null) {
            return null;
        }
        String s = typed.trim().replace(" ", "");
        if (s.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static int attemptCost() {
        return ForgeCosts.MINE_ATTEMPT;
    }
}
