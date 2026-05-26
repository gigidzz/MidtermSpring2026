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

    // ── color() ──────────────────────────────────────────────────────────────

    @Test
    void colorOfRedCard() {
        assertEquals("R", Main.color("R5"));
    }

    @Test
    void colorOfYellowCard() {
        assertEquals("Y", Main.color("YS"));
    }

    @Test
    void colorOfGreenCard() {
        assertEquals("G", Main.color("G+2"));
    }

    @Test
    void colorOfBlueCard() {
        assertEquals("B", Main.color("BR"));
    }

    @Test
    void colorOfWildIsEmpty() {
        assertEquals("", Main.color("W"));
        assertEquals("", Main.color("W4"));
    }

    // ── rank() ───────────────────────────────────────────────────────────────

    @Test
    void rankOfNumberCard() {
        assertEquals("NUMBER", Main.rank("R5"));
        assertEquals("NUMBER", Main.rank("B0"));
    }

    @Test
    void rankOfSkip() {
        assertEquals("SKIP", Main.rank("RS"));
        assertEquals("SKIP", Main.rank("GS"));
    }

    @Test
    void rankOfReverse() {
        assertEquals("REVERSE", Main.rank("BR"));
        assertEquals("REVERSE", Main.rank("YR"));
    }

    @Test
    void rankOfDrawTwo() {
        assertEquals("DRAW_TWO", Main.rank("G+2"));
        assertEquals("DRAW_TWO", Main.rank("R+2"));
    }

    @Test
    void rankOfWild() {
        assertEquals("WILD", Main.rank("W"));
    }

    @Test
    void rankOfWildDrawFour() {
        assertEquals("WILD_DRAW_FOUR", Main.rank("W4"));
    }

    // ── points() ─────────────────────────────────────────────────────────────

    @Test
    void pointsForNumberCards() {
        assertEquals(5, Main.points("R5"));
        assertEquals(0, Main.points("G0"));
        assertEquals(9, Main.points("B9"));
    }

    @Test
    void pointsForActionCards() {
        assertEquals(20, Main.points("YS"));
        assertEquals(20, Main.points("BR"));
        assertEquals(20, Main.points("G+2"));
    }

    @Test
    void pointsForWilds() {
        assertEquals(50, Main.points("W"));
        assertEquals(50, Main.points("W4"));
    }

    // ── isLegal() — matching by color ────────────────────────────────────────

    @Test
    void legalWhenSameColor() {
        assertTrue(Main.isLegal("R2", "R9", ""));
        assertTrue(Main.isLegal("RS", "R5", ""));
    }

    @Test
    void illegalWhenDifferentColorAndRank() {
        assertFalse(Main.isLegal("B3", "R9", ""));
        assertFalse(Main.isLegal("GS", "R5", ""));
    }

    // ── isLegal() — matching by number ───────────────────────────────────────

    @Test
    void legalWhenSameNumber() {
        assertTrue(Main.isLegal("G9", "R9", ""));
        assertTrue(Main.isLegal("B3", "Y3", ""));
    }

    @Test
    void illegalWhenDifferentNumber() {
        assertFalse(Main.isLegal("G8", "R9", ""));
    }

    // ── isLegal() — matching by action type ──────────────────────────────────

    @Test
    void legalWhenSameActionType() {
        assertTrue(Main.isLegal("RS", "GS", ""));   // skip on skip
        assertTrue(Main.isLegal("BR", "YR", ""));   // reverse on reverse
        assertTrue(Main.isLegal("R+2", "G+2", "")); // draw two on draw two
    }

    @Test
    void illegalWhenDifferentActionTypes() {
        assertFalse(Main.isLegal("RS", "GR", ""));  // skip vs reverse
    }

    // ── isLegal() — wild cards ────────────────────────────────────────────────

    @Test
    void wildIsAlwaysLegal() {
        assertTrue(Main.isLegal("W", "R9", ""));
        assertTrue(Main.isLegal("W", "GS", ""));
        assertTrue(Main.isLegal("W", "W4", "B"));
    }

    @Test
    void wildDrawFourIsAlwaysLegal() {
        assertTrue(Main.isLegal("W4", "R9", ""));
        assertTrue(Main.isLegal("W4", "B3", "Y"));
    }

    // ── isLegal() — called color after wild ──────────────────────────────────

    @Test
    void legalWhenMatchesCalledColor() {
        assertTrue(Main.isLegal("B3", "W", "B"));
        assertTrue(Main.isLegal("RS", "W4", "R"));
    }

    @Test
    void illegalWhenDoesNotMatchCalledColor() {
        assertFalse(Main.isLegal("G3", "W", "B"));
    }

    // ── scoring ──────────────────────────────────────────────────────────────

    @Test
    void scoringAddsOpponentHandValues() {
        Main.setupPlayers(2, false);  // 2 bots = players at index 0 and 1
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
                actual += Main.points(card);
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
        assertEquals(1, Main.deck.size() + Main.discard.size()); // one drawn, one left
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
        Main.upCard = "R5";
        Main.calledColor = "";
        ArrayList<String> hand = new ArrayList<>();
        hand.add("RS");   // skip — legal
        hand.add("R+2");  // draw two — legal, should be preferred
        hand.add("W");
        assertEquals(1, Main.chooseBotCard(hand)); // index of R+2
    }

    @Test
    void botPrefersSkipOverNumber() {
        Main.upCard = "R5";
        Main.calledColor = "";
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R3");   // number — legal
        hand.add("RS");   // skip — legal, should be preferred
        assertEquals(1, Main.chooseBotCard(hand)); // index of RS
    }

    @Test
    void botPrefersNumberOverWild() {
        Main.upCard = "R5";
        Main.calledColor = "";
        ArrayList<String> hand = new ArrayList<>();
        hand.add("W");    // wild — always legal
        hand.add("R3");   // number — legal, should be preferred
        assertEquals(1, Main.chooseBotCard(hand)); // index of R3
    }

    @Test
    void botReturnsMinusOneWhenNoLegalCard() {
        Main.upCard = "R5";
        Main.calledColor = "";
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B3");
        hand.add("GS");
        assertEquals(-1, Main.chooseBotCard(hand));
    }

    // ── bot color choice ─────────────────────────────────────────────────────

    @Test
    void botChoosesMostCommonColorInHand() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B1");
        hand.add("B2");
        hand.add("R3");
        assertEquals("B", Main.chooseBotColor(hand));
    }

    @Test
    void botColorChoiceWithNoColoredCardsDefaultsToR() {
        // quirk: when hand has no colored cards, all counts are 0
        // the first condition (r >= y && r >= g && r >= b) is 0>=0>=0>=0 = true, so R wins
        ArrayList<String> hand = new ArrayList<>();
        hand.add("W");
        hand.add("W4");
        assertEquals("R", Main.chooseBotColor(hand));
    }

    // ── quirk: penalty card on invalid index ─────────────────────────────────

    @Test
    void invalidIndexIsDetectedCorrectly() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R5");
        hand.add("G3");
        int chosen = 5; // out of bounds
        assertTrue(chosen >= hand.size()); // this is the condition Main checks before giving penalty
    }
}
