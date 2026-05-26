import java.util.ArrayList;

public class BotStrategy {

    static int chooseCard(ArrayList<String> hand, String upCard, String calledColor) {
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            if (Card.rank(card).equals("DRAW_TWO") && Card.isLegal(card, upCard, calledColor)) return i;
        }
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            if (Card.rank(card).equals("SKIP") && Card.isLegal(card, upCard, calledColor)) return i;
        }
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            if (Card.rank(card).equals("NUMBER") && Card.isLegal(card, upCard, calledColor)) return i;
        }
        for (int i = 0; i < hand.size(); i++) {
            if (Card.isWild(hand.get(i))) return i;
        }
        return -1;
    }

    static String chooseColor(ArrayList<String> hand) {
        int r = 0, y = 0, g = 0, b = 0;
        for (String card : hand) {
            String c = Card.color(card);
            if (c.equals("R")) r++;
            else if (c.equals("Y")) y++;
            else if (c.equals("G")) g++;
            else if (c.equals("B")) b++;
        }
        if (r >= y && r >= g && r >= b) return "R";
        else if (y >= r && y >= g && y >= b) return "Y";
        else if (g >= r && g >= y && g >= b) return "G";
        else return "B";
    }
}
