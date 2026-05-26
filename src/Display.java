import java.util.ArrayList;

public class Display {
    static boolean quiet = false;

    static void gameHeader(int gameNumber) {
        if (!quiet) System.out.println("\n=== Game " + gameNumber + " ===");
    }

    static void upCardStatus(String upCard, String calledColor) {
        if (!quiet) {
            String suffix = calledColor.equals("") ? "" : " called " + calledColor;
            System.out.println("\nUp card: " + upCard + suffix);
        }
    }

    static void playerHand(String name, ArrayList<String> hand) {
        if (!quiet) System.out.println(name + " hand: " + join(hand));
    }

    static void playerDraws(String name, String card) {
        if (!quiet) System.out.println(name + " draws " + card);
    }

    static void playerPlays(String name, String card) {
        if (!quiet) System.out.println(name + " plays " + card);
    }

    static void playerCallsColor(String name, String color) {
        if (!quiet) System.out.println(name + " calls " + color);
    }

    static void playerSaysUno(String name) {
        if (!quiet) System.out.println(name + " says UNO!");
    }

    static void playerWins(String name, int points) {
        if (!quiet) System.out.println(name + " wins and scores " + points);
    }

    static void playerDrawsTwo(String name) {
        if (!quiet) System.out.println(name + " draws two.");
    }

    static void playerDrawsFour(String name) {
        if (!quiet) System.out.println(name + " draws four.");
    }

    static void invalidIndexPenalty(String name) {
        if (!quiet) System.out.println(name + " selected an invalid index and draws a penalty card.");
    }

    static void illegalCardPenalty(String name, String card) {
        if (!quiet) System.out.println(name + " tried illegal card " + card + " and draws a penalty card.");
    }

    static void gameStopped() {
        if (!quiet) System.out.println("Game stopped at safety limit.");
    }

    static void finalScores(ArrayList<String> names, int[] scores) {
        System.out.println("\nFinal scores:");
        for (int i = 0; i < names.size(); i++) {
            System.out.println(names.get(i) + ": " + scores[i]);
        }
    }

    static void invalidPlayerCount() {
        System.out.println("UNO needs 2 to 4 players.");
    }

    static void usage() {
        System.out.println("Usage: scripts/run.sh [--bots N] [--games N] [--human] [--quiet] [--seed N]");
    }

    static void selfTestResult(int passed) {
        System.out.println("Passed " + passed + " characterization checks.");
    }

    // prompts — always print regardless of quiet (human is waiting for input)
    static void promptChooseCard() {
        System.out.print("Choose card index/code or draw: ");
    }

    static void promptPlayDrawnCard(String card) {
        System.out.print("Play drawn card " + card + "? y/n: ");
    }

    static void cardNotLegal() {
        System.out.println("That card is not legal.");
    }

    static void cardNotFound() {
        System.out.println("Card not found.");
    }

    static void promptColor() {
        System.out.print("Call color R/Y/G/B: ");
    }

    static void badColor() {
        System.out.println("Bad color.");
    }

    private static String join(ArrayList<String> cards) {
        String out = "";
        for (int i = 0; i < cards.size(); i++) {
            out += i + ":" + cards.get(i);
            if (i < cards.size() - 1) out += " ";
        }
        return out;
    }
}
