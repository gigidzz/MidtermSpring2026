import java.util.ArrayList;

public class Main {
    static GameState state = new GameState();

    public static void main(String[] args) {
        int bots = 3;
        int games = 1;
        boolean human = false;
        boolean quiet = false;
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
        state.random = new java.util.Random(seed);
        setupPlayers(bots, human);

        if (state.players.size() < 2 || state.players.size() > 4) {
            Display.invalidPlayerCount();
            return;
        }

        for (int g = 1; g <= games; g++) {
            Display.gameHeader(g);
            playGame();
        }

        ArrayList<String> names = new ArrayList<String>();
        for (Player p : state.players) names.add(p.name);
        Display.finalScores(names, state.scores);
    }

    static void setupPlayers(int bots, boolean human) {
        state.players.clear();
        if (human) {
            state.players.add(new Player("You", true));
        }
        for (int i = 1; i <= bots; i++) {
            state.players.add(new Player("Bot" + i, false));
        }
    }

    static void playGame() {
        setupGame();
        runTurnLoop();
    }

    static void setupGame() {
        state.buildDeck();
        state.discard.clear();
        for (Player p : state.players) p.hand.clear();
        for (Player p : state.players) {
            for (int j = 0; j < 7; j++) p.hand.add(state.draw());
        }
        state.upCard = state.draw();
        while (Card.isWild(state.upCard)) {
            state.discard.add(state.upCard);
            state.upCard = state.draw();
        }
        state.calledColor = "";
        state.direction = 1;
        state.currentPlayer = state.random.nextInt(state.players.size());
    }

    static void runTurnLoop() {
        int guard = 0;
        while (guard < 3000) {
            guard++;
            Player player = state.players.get(state.currentPlayer);
            String name = player.name;
            ArrayList<String> hand = player.hand;

            Display.upCardStatus(state.upCard, state.calledColor);
            Display.playerHand(name, hand);

            int chosen = -1;
            if (player.human) {
                chosen = Display.askHuman(hand, state.upCard, state.calledColor);
            } else {
                chosen = BotStrategy.chooseCard(hand, state.upCard, state.calledColor);
            }

            if (chosen == -1) {
                String drawn = state.draw();
                hand.add(drawn);
                Display.playerDraws(name, drawn);
                if (Card.isLegal(drawn, state.upCard, state.calledColor)) {
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
                    hand.add(state.draw());
                    state.next();
                    continue;
                }

                String card = hand.get(chosen);

                if (!Card.isLegal(card, state.upCard, state.calledColor)) {
                    Display.illegalCardPenalty(name, card);
                    hand.add(state.draw());
                    state.next();
                    continue;
                }

                hand.remove(chosen);
                state.discard.add(state.upCard);
                state.upCard = card;
                state.calledColor = "";
                Display.playerPlays(name, card);

                if (Card.isWild(card)) {
                    if (player.human) {
                        state.calledColor = Display.askColor();
                    } else {
                        state.calledColor = BotStrategy.chooseColor(hand);
                    }
                    Display.playerCallsColor(name, state.calledColor);
                }

                if (hand.size() == 1) {
                    Display.playerSaysUno(name);
                }

                if (hand.size() == 0) {
                    int points = state.calculateScore();
                    state.scores[state.currentPlayer] += points;
                    Display.playerWins(name, points);
                    return;
                }

                applyEffect(card);
            } else {
                state.next();
            }
        }
        Display.gameStopped();
    }

    static void applyEffect(String card) {
        if (Card.rank(card).equals("SKIP")) {
            state.next();
            state.next();
        } else if (Card.rank(card).equals("REVERSE")) {
            state.direction = state.direction * -1;
            if (state.players.size() == 2) {
                state.next();
                state.next();
            } else {
                state.next();
            }
        } else if (Card.rank(card).equals("DRAW_TWO")) {
            state.next();
            state.players.get(state.currentPlayer).hand.add(state.draw());
            state.players.get(state.currentPlayer).hand.add(state.draw());
            Display.playerDrawsTwo(state.players.get(state.currentPlayer).name);
            state.next();
        } else if (Card.rank(card).equals("WILD_DRAW_FOUR")) {
            state.next();
            for (int i = 0; i < 4; i++) {
                state.players.get(state.currentPlayer).hand.add(state.draw());
            }
            Display.playerDrawsFour(state.players.get(state.currentPlayer).name);
            state.next();
        } else {
            state.next();
        }
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
        if (BotStrategy.chooseCard(h, "R9", "") == 1) passed++; else fail("bot normal before wild");

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
