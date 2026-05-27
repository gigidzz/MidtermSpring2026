import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Main {
    static ArrayList<Player> players = new ArrayList<Player>();
    static ArrayList<String> deck = new ArrayList<String>();
    static ArrayList<String> discard = new ArrayList<String>();
    static int[] scores = new int[10];
    static int currentPlayer = 0;
    static int direction = 1;
    static String upCard = "";
    static String calledColor = "";
    static boolean quiet = false;
    static Random random = new Random();

    public static void main(String[] args) {
        int bots = 3;
        int games = 1;
        boolean human = false;
        long seed = System.currentTimeMillis();

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--bots") && i + 1 < args.length) {
                bots = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--games") && i + 1 < args.length) {
                games = Integer.parseInt(args[++i]);
            } else if (args[i].equals("--human")) {
                human = true;
            } else if (args[i].equals("--quiet")) {
                quiet = true;
            } else if (args[i].equals("--seed") && i + 1 < args.length) {
                seed = Long.parseLong(args[++i]);
            } else if (args[i].equals("--self-test")) {
                selfTest();
                return;
            } else if (args[i].equals("--help")) {
                Display.usage();
                return;
            }
        }

        Display.quiet = quiet;
        random = new Random(seed);
        setupPlayers(bots, human);

        if (players.size() < 2 || players.size() > 4) {
            Display.invalidPlayerCount();
            return;
        }

        for (int g = 1; g <= games; g++) {
            Display.gameHeader(g);
            playGame();
        }

        ArrayList<String> names = new ArrayList<String>();
        for (Player p : players) names.add(p.name);
        Display.finalScores(names, scores);
    }

    static void setupPlayers(int bots, boolean human) {
        players.clear();
        if (human) {
            players.add(new Player("You", true));
        }
        for (int i = 1; i <= bots; i++) {
            players.add(new Player("Bot" + i, false));
        }
    }

    static void playGame() {
        buildDeck();
        discard.clear();
        for (Player p : players) {
            p.hand.clear();
        }
        for (Player p : players) {
            for (int j = 0; j < 7; j++) {
                p.hand.add(draw());
            }
        }
        upCard = draw();
        while (Card.isWild(upCard)) {
            discard.add(upCard);
            upCard = draw();
        }
        calledColor = "";
        direction = 1;
        currentPlayer = random.nextInt(players.size());

        int guard = 0;
        while (guard < 3000) {
            guard++;
            Player player = players.get(currentPlayer);
            String name = player.name;
            ArrayList<String> hand = player.hand;

            Display.upCardStatus(upCard, calledColor);
            Display.playerHand(name, hand);

            int chosen = -1;
            if (player.human) {
                chosen = Display.askHuman(hand, upCard, calledColor);
            } else {
                chosen = BotStrategy.chooseCard(hand, upCard, calledColor);
            }

            if (chosen == -1) {
                String drawn = draw();
                hand.add(drawn);
                Display.playerDraws(name, drawn);
                if (Card.isLegal(drawn, upCard, calledColor)) {
                    if (!player.human) {
                        chosen = hand.size() - 1;
                    } else {
                        if (Display.askPlayDrawnCard(drawn)) {
                            chosen = hand.size() - 1;
                        }
                    }
                }
            }

            if (chosen >= 0) {
                if (chosen >= hand.size()) {
                    Display.invalidIndexPenalty(name);
                    hand.add(draw());
                    next();
                    continue;
                }

                String card = hand.get(chosen);

                if (!Card.isLegal(card, upCard, calledColor)) {
                    Display.illegalCardPenalty(name, card);
                    hand.add(draw());
                    next();
                    continue;
                }

                hand.remove(chosen);
                discard.add(upCard);
                upCard = card;
                calledColor = "";
                Display.playerPlays(name, card);

                if (Card.isWild(card)) {
                    if (player.human) {
                        calledColor = Display.askColor();
                    } else {
                        calledColor = BotStrategy.chooseColor(hand);
                    }
                    Display.playerCallsColor(name, calledColor);
                }

                if (hand.size() == 1) {
                    Display.playerSaysUno(name);
                }

                if (hand.size() == 0) {
                    int points = calculateScore();
                    scores[currentPlayer] += points;
                    Display.playerWins(name, points);
                    return;
                }

                applyEffect(card);
            } else {
                next();
            }
        }
        Display.gameStopped();
    }

    static int calculateScore() {
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

    static void buildDeck() {
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

    static String draw() {
        if (deck.size() == 0) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck, random);
        }
        if (deck.size() == 0) return "W";
        return deck.remove(0);
    }




    static void applyEffect(String card) {
        if (Card.rank(card).equals("SKIP")) {
            next();
            next();
        } else if (Card.rank(card).equals("REVERSE")) {
            direction = direction * -1;
            if (players.size() == 2) {
                next();
                next();
            } else {
                next();
            }
        } else if (Card.rank(card).equals("DRAW_TWO")) {
            next();
            players.get(currentPlayer).hand.add(draw());
            players.get(currentPlayer).hand.add(draw());
            Display.playerDrawsTwo(players.get(currentPlayer).name);
            next();
        } else if (Card.rank(card).equals("WILD_DRAW_FOUR")) {
            next();
            for (int i = 0; i < 4; i++) {
                players.get(currentPlayer).hand.add(draw());
            }
            Display.playerDrawsFour(players.get(currentPlayer).name);
            next();
        } else {
            next();
        }
    }

    static void next() {
        currentPlayer += direction;
        if (currentPlayer >= players.size()) currentPlayer = 0;
        if (currentPlayer < 0) currentPlayer = players.size() - 1;
    }

    static String join(ArrayList<String> cards) {
        String out = "";
        for (int i = 0; i < cards.size(); i++) {
            out += i + ":" + cards.get(i);
            if (i < cards.size() - 1) out += " ";
        }
        return out;
    }

    static void selfTest() {
        int passed = 0;
        if (Card.color("R5").equals("R")) passed++; else fail("color R5");
        if (Card.rank("G+2").equals("DRAW_TWO")) passed++; else fail("rank +2");
        if (Card.points("W4") == 50) passed++; else fail("wild points");
        if (Card.isLegal("R2", "R9", "")) passed++; else fail("same color");
        if (Card.isLegal("G9", "R9", "")) passed++; else fail("same number");
        if (Card.isLegal("B3", "W", "B")) passed++; else fail("called color");
        if (!Card.isLegal("B3", "R9", "")) passed++; else fail("illegal mismatch");

        ArrayList<String> h = new ArrayList<String>();
        h.add("B3");
        h.add("R4");
        h.add("W");
        upCard = "R9";
        calledColor = "";
        if (BotStrategy.chooseCard(h, upCard, calledColor) == 1) passed++; else fail("bot normal before wild");

        ArrayList<String> h2 = new ArrayList<String>();
        h2.add("B1");
        h2.add("B2");
        h2.add("R3");
        if (BotStrategy.chooseColor(h2).equals("B")) passed++; else fail("bot color");

        Display.selfTestResult(passed);
    }

    static void fail(String name) {
        throw new RuntimeException("Failed: " + name);
    }
}
