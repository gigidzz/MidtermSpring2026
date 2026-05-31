import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Holds all mutable game state and the operations that act purely on it.
 * This is the model: it knows nothing about console input or output.
 */
public class GameState {
    ArrayList<Player> players = new ArrayList<Player>();
    ArrayList<String> deck = new ArrayList<String>();
    ArrayList<String> discard = new ArrayList<String>();
    int[] scores = new int[10];
    int currentPlayer = 0;
    int direction = 1;
    String upCard = "";
    String calledColor = "";
    Random random = new Random();

    void buildDeck() {
        deck.clear();
        String[] colors = {"R", "Y", "G", "B"};
        for (int c = 0; c < colors.length; c++) {
            deck.add(colors[c] + "0");
            for (int n = 1; n <= 9; n++) {
                deck.add(colors[c] + n);
                deck.add(colors[c] + n);
            }
            deck.add(colors[c] + "S");
            deck.add(colors[c] + "S");
            deck.add(colors[c] + "R");
            deck.add(colors[c] + "R");
            deck.add(colors[c] + "+2");
            deck.add(colors[c] + "+2");
        }
        for (int i = 0; i < 4; i++) {
            deck.add("W");
            deck.add("W4");
        }
        Collections.shuffle(deck, random);
    }

    String draw() {
        if (deck.size() == 0) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck, random);
        }
        if (deck.size() == 0) return "W";
        return deck.remove(0);
    }

    void next() {
        currentPlayer += direction;
        if (currentPlayer >= players.size()) currentPlayer = 0;
        if (currentPlayer < 0) currentPlayer = players.size() - 1;
    }

    int calculateScore() {
        int points = 0;
        for (int i = 0; i < players.size(); i++) {
            if (i != currentPlayer) {
                for (String card : players.get(i).hand) {
                    points += Card.points(card);
                }
            }
        }
        return points;
    }
}
