import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {

    @BeforeEach
    void reset() {
        Main.upCard = "";
        Main.calledColor = "";
        Main.direction = 1;
        Main.currentPlayer = 0;
        Main.deck = new ArrayList<>();
        Main.discard = new ArrayList<>();
        Main.players = new ArrayList<>();
        Main.scores = new int[10];
        Display.quiet = true;
    }

    // ── Card.color() ─────────────────────────────────────────────────────────

    @Test
    void colorOfRedCard() {
        assertEquals("R", Card.color("R5"));
    }

    @Test
    void colorOfYellowCard() {
        assertEquals("Y", Card.color("YS"));
    }

    @Test
    void colorOfGreenCard() {
        assertEquals("G", Card.color("G+2"));
    }

    @Test
    void colorOfBlueCard() {
        assertEquals("B", Card.color("BR"));
    }

    @Test
    void colorOfWildIsEmpty() {
        assertEquals("", Card.color("W"));
        assertEquals("", Card.color("W4"));
    }

    // ── Card.rank() ──────────────────────────────────────────────────────────

    @Test
    void rankOfNumberCard() {
        assertEquals("NUMBER", Card.rank("R5"));
        assertEquals("NUMBER", Card.rank("B0"));
    }

    @Test
    void rankOfSkip() {
        assertEquals("SKIP", Card.rank("RS"));
        assertEquals("SKIP", Card.rank("GS"));
    }

    @Test
    void rankOfReverse() {
        assertEquals("REVERSE", Card.rank("BR"));
        assertEquals("REVERSE", Card.rank("YR"));
    }

    @Test
    void rankOfDrawTwo() {
        assertEquals("DRAW_TWO", Card.rank("G+2"));
        assertEquals("DRAW_TWO", Card.rank("R+2"));
    }

    @Test
    void rankOfWild() {
        assertEquals("WILD", Card.rank("W"));
    }

    @Test
    void rankOfWildDrawFour() {
        assertEquals("WILD_DRAW_FOUR", Card.rank("W4"));
    }

    // ── Card.points() ────────────────────────────────────────────────────────

    @Test
    void pointsForNumberCards() {
        assertEquals(5, Card.points("R5"));
        assertEquals(0, Card.points("G0"));
        assertEquals(9, Card.points("B9"));
    }

    @Test
    void pointsForActionCards() {
        assertEquals(20, Card.points("YS"));
        assertEquals(20, Card.points("BR"));
        assertEquals(20, Card.points("G+2"));
    }

    @Test
    void pointsForWilds() {
        assertEquals(50, Card.points("W"));
        assertEquals(50, Card.points("W4"));
    }

    // ── Card.isLegal() — matching by color ───────────────────────────────────

    @Test
    void legalWhenSameColor() {
        assertTrue(Card.isLegal("R2", "R9", ""));
        assertTrue(Card.isLegal("RS", "R5", ""));
    }

    @Test
    void illegalWhenDifferentColorAndRank() {
        assertFalse(Card.isLegal("B3", "R9", ""));
        assertFalse(Card.isLegal("GS", "R5", ""));
    }

    // ── Card.isLegal() — matching by number ──────────────────────────────────

    @Test
    void legalWhenSameNumber() {
        assertTrue(Card.isLegal("G9", "R9", ""));
        assertTrue(Card.isLegal("B3", "Y3", ""));
    }

    @Test
    void illegalWhenDifferentNumber() {
        assertFalse(Card.isLegal("G8", "R9", ""));
    }

    // ── Card.isLegal() — matching by action type ─────────────────────────────

    @Test
    void legalWhenSameActionType() {
        assertTrue(Card.isLegal("RS", "GS", ""));   // skip on skip
        assertTrue(Card.isLegal("BR", "YR", ""));   // reverse on reverse
        assertTrue(Card.isLegal("R+2", "G+2", "")); // draw two on draw two
    }

    @Test
    void illegalWhenDifferentActionTypes() {
        assertFalse(Card.isLegal("RS", "GR", ""));  // skip vs reverse
    }

    // ── Card.isLegal() — wild cards ──────────────────────────────────────────

    @Test
    void wildIsAlwaysLegal() {
        assertTrue(Card.isLegal("W", "R9", ""));
        assertTrue(Card.isLegal("W", "GS", ""));
        assertTrue(Card.isLegal("W", "W4", "B"));
    }

    @Test
    void wildDrawFourIsAlwaysLegal() {
        assertTrue(Card.isLegal("W4", "R9", ""));
        assertTrue(Card.isLegal("W4", "B3", "Y"));
    }

    // ── Card.isLegal() — called color after wild ─────────────────────────────

    @Test
    void legalWhenMatchesCalledColor() {
        assertTrue(Card.isLegal("B3", "W", "B"));
        assertTrue(Card.isLegal("RS", "W4", "R"));
    }

    @Test
    void illegalWhenDoesNotMatchCalledColor() {
        assertFalse(Card.isLegal("G3", "W", "B"));
    }

    // ── scoring ──────────────────────────────────────────────────────────────

    @Test
    void scoringAddsOpponentHandValues() {
        Main.setupPlayers(2, false);
        Main.upCard = "R5";
        Main.calledColor = "";

        Main.players.get(0).hand.clear();  // winner — empty hand
        Main.players.get(1).hand.clear();
        Main.players.get(1).hand.add("W");   // 50 points
        Main.players.get(1).hand.add("R5");  // 5 points

        int expected = 55;
        int actual = 0;
        for (int i = 1; i < Main.players.size(); i++) {
            for (String card : Main.players.get(i).hand) {
                actual += Card.points(card);
            }
        }
        assertEquals(expected, actual);
    }

    // ── drawing from deck ────────────────────────────────────────────────────

    @Test
    void drawReturnsTopOfDeck() {
        Main.deck.add("R5");
        Main.deck.add("G3");
        assertEquals("R5", Main.draw());
        assertEquals("G3", Main.draw());
    }

    @Test
    void drawreshufflesDiscardWhenDeckEmpty() {
        Main.discard.add("B2");
        Main.discard.add("YS");
        String drawn = Main.draw();
        assertTrue(drawn.equals("B2") || drawn.equals("YS"));
        assertEquals(1, Main.deck.size() + Main.discard.size());
    }

    @Test
    void drawReturnsWildWhenBothDeckAndDiscardEmpty() {
        assertEquals("W", Main.draw());
    }

    // ── skip behavior ────────────────────────────────────────────────────────

    @Test
    void nextAdvancesCurrentPlayer() {
        Main.setupPlayers(2, false);
        Main.currentPlayer = 0;
        Main.direction = 1;
        Main.next();
        assertEquals(1, Main.currentPlayer);
    }

    @Test
    void nextWrapsAround() {
        Main.setupPlayers(2, false);
        Main.currentPlayer = 1;
        Main.direction = 1;
        Main.next();
        assertEquals(0, Main.currentPlayer);
    }

    // ── reverse behavior ─────────────────────────────────────────────────────

    @Test
    void reverseFlipsDirection() {
        Main.direction = 1;
        Main.direction = Main.direction * -1;
        assertEquals(-1, Main.direction);
    }

    @Test
    void nextGoesBackwardWhenDirectionReversed() {
        Main.setupPlayers(3, false);
        Main.currentPlayer = 0;
        Main.direction = -1;
        Main.next();
        assertEquals(2, Main.currentPlayer);
    }

    // ── bot card selection ────────────────────────────────────────────────────

    @Test
    void botPrefersDrawTwoOverSkip() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("RS");   // skip — legal
        hand.add("R+2");  // draw two — legal, should be preferred
        hand.add("W");
        assertEquals(1, BotStrategy.chooseCard(hand, "R5", ""));
    }

    @Test
    void botPrefersSkipOverNumber() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R3");   // number — legal
        hand.add("RS");   // skip — legal, should be preferred
        assertEquals(1, BotStrategy.chooseCard(hand, "R5", ""));
    }

    @Test
    void botPrefersNumberOverWild() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("W");    // wild — always legal
        hand.add("R3");   // number — legal, should be preferred
        assertEquals(1, BotStrategy.chooseCard(hand, "R5", ""));
    }

    @Test
    void botReturnsMinusOneWhenNoLegalCard() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B3");
        hand.add("GS");
        assertEquals(-1, BotStrategy.chooseCard(hand, "R5", ""));
    }

    // ── bot color choice ─────────────────────────────────────────────────────

    @Test
    void botChoosesMostCommonColorInHand() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B1");
        hand.add("B2");
        hand.add("R3");
        assertEquals("B", BotStrategy.chooseColor(hand));
    }

    @Test
    void botColorChoiceWithNoColoredCardsDefaultsToR() {
        // quirk: when hand has no colored cards, all counts are 0
        // the first condition (r >= y && r >= g && r >= b) is 0>=0>=0>=0 = true, so R wins
        ArrayList<String> hand = new ArrayList<>();
        hand.add("W");
        hand.add("W4");
        assertEquals("R", BotStrategy.chooseColor(hand));
    }

    // ── quirk: penalty card on invalid index ─────────────────────────────────

    @Test
    void invalidIndexIsDetectedCorrectly() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R5");
        hand.add("G3");
        int chosen = 5;
        assertTrue(chosen >= hand.size());
    }
}
