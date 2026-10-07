package game;

import util.ColorSetup;

public class Main {
    public static void main(String[] args) {
        ColorSetup.install();
        new Game().start();
    }
}
