import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {
    static ArrayList<String> playerNames = new ArrayList<String>();
    static ArrayList<Boolean> humanPlayers = new ArrayList<Boolean>();
    static ArrayList<ArrayList<String>> hands = new ArrayList<ArrayList<String>>();
    static ArrayList<String> deck = new ArrayList<String>();
    static ArrayList<String> discard = new ArrayList<String>();
    static int[] scores = new int[10];
    static int currentPlayer = 0;
    static int direction = 1;
    static String upCard = "";
    static String calledColor = "";
    static boolean quiet = false;
    static Random random = new Random();
    static Scanner scanner = new Scanner(System.in);

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

        if (playerNames.size() < 2 || playerNames.size() > 4) {
            Display.invalidPlayerCount();
            return;
        }

        for (int g = 1; g <= games; g++) {
            Display.gameHeader(g);
            playGame();
        }

        Display.finalScores(playerNames, scores);
    }

    static void setupPlayers(int bots, boolean human) {
        playerNames.clear();
        humanPlayers.clear();
        hands.clear();
        if (human) {
            playerNames.add("You");
            humanPlayers.add(Boolean.TRUE);
            hands.add(new ArrayList<String>());
        }
        for (int i = 1; i <= bots; i++) {
            playerNames.add("Bot" + i);
            humanPlayers.add(Boolean.FALSE);
            hands.add(new ArrayList<String>());
        }
    }

    static void playGame() {
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
        discard.clear();
        for (int i = 0; i < hands.size(); i++) {
            hands.get(i).clear();
        }
        for (int i = 0; i < playerNames.size(); i++) {
            for (int j = 0; j < 7; j++) {
                hands.get(i).add(draw());
            }
        }
        upCard = draw();
        while (upCard.startsWith("W")) {
            discard.add(upCard);
            upCard = draw();
        }
        calledColor = "";
        direction = 1;
        currentPlayer = random.nextInt(playerNames.size());

        int guard = 0;
        while (guard < 3000) {
            guard++;
            String name = playerNames.get(currentPlayer);
            ArrayList<String> hand = hands.get(currentPlayer);

            Display.upCardStatus(upCard, calledColor);
            Display.playerHand(name, hand);

            int chosen = -1;
            if (humanPlayers.get(currentPlayer).booleanValue()) {
                chosen = askHuman(hand);
            } else {
                chosen = chooseBotCard(hand);
            }

            if (chosen == -1) {
                String drawn = draw();
                hand.add(drawn);
                Display.playerDraws(name, drawn);
                if (isLegal(drawn, upCard, calledColor)) {
                    if (!humanPlayers.get(currentPlayer).booleanValue()) {
                        chosen = hand.size() - 1;
                    } else {
                        Display.promptPlayDrawnCard(drawn);
                        String answer = scanner.nextLine();
                        if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) {
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
                boolean ok = isLegal(card, upCard, calledColor);

                if (!ok) {
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

                if (card.equals("W") || card.equals("W4")) {
                    if (humanPlayers.get(currentPlayer).booleanValue()) {
                        calledColor = askColor();
                    } else {
                        calledColor = chooseBotColor(hand);
                    }
                    Display.playerCallsColor(name, calledColor);
                }

                if (hand.size() == 1) {
                    Display.playerSaysUno(name);
                }

                if (hand.size() == 0) {
                    int points = 0;
                    for (int i = 0; i < hands.size(); i++) {
                        if (i != currentPlayer) {
                            for (int j = 0; j < hands.get(i).size(); j++) {
                                points += points(hands.get(i).get(j));
                            }
                        }
                    }
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

    static String draw() {
        if (deck.size() == 0) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck, random);
        }
        if (deck.size() == 0) {
            return "W";
        }
        return deck.remove(0);
    }

    static int chooseBotCard(ArrayList<String> hand) {
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            if (rank(card).equals("DRAW_TWO") && isLegal(card, upCard, calledColor)) {
                return i;
            }
        }
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            if (rank(card).equals("SKIP") && isLegal(card, upCard, calledColor)) {
                return i;
            }
        }
        for (int i = 0; i < hand.size(); i++) {
            String card = hand.get(i);
            if (rank(card).equals("NUMBER") && isLegal(card, upCard, calledColor)) {
                return i;
            }
        }
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).startsWith("W")) {
                return i;
            }
        }
        return -1;
    }

    static int askHuman(ArrayList<String> hand) {
        while (true) {
            Display.promptChooseCard();
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("DRAW")) {
                return -1;
            }
            try {
                int index = Integer.parseInt(input);
                if (index >= 0 && index < hand.size()) {
                    return index;
                }
            } catch (Exception ignored) {
            }
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).equals(input)) {
                    if (isLegal(hand.get(i), upCard, calledColor)) {
                        return i;
                    }
                    Display.cardNotLegal();
                }
            }
            Display.cardNotFound();
        }
    }

    static String askColor() {
        while (true) {
            Display.promptColor();
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("R")) return "R";
            if (input.equals("Y")) return "Y";
            if (input.equals("G")) return "G";
            if (input.equals("B")) return "B";
            Display.badColor();
        }
    }

    static String chooseBotColor(ArrayList<String> hand) {
        int r = 0;
        int y = 0;
        int g = 0;
        int b = 0;
        for (int i = 0; i < hand.size(); i++) {
            String c = color(hand.get(i));
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

    static boolean isLegal(String card, String up, String call) {
        if (card.startsWith("W")) return true;
        if (color(card).equals(color(up))) return true;
        if (!call.equals("") && color(card).equals(call)) return true;
        if (rank(card).equals(rank(up)) && !rank(card).equals("NUMBER")) return true;
        if (rank(card).equals("NUMBER") && rank(up).equals("NUMBER") && number(card) == number(up)) return true;
        return false;
    }

    static String color(String card) {
        if (card.startsWith("R")) return "R";
        if (card.startsWith("Y")) return "Y";
        if (card.startsWith("G")) return "G";
        if (card.startsWith("B")) return "B";
        return "";
    }

    static String rank(String card) {
        if (card.equals("W")) return "WILD";
        if (card.equals("W4")) return "WILD_DRAW_FOUR";
        if (card.endsWith("S")) return "SKIP";
        if (card.endsWith("R")) return "REVERSE";
        if (card.endsWith("+2")) return "DRAW_TWO";
        return "NUMBER";
    }

    static int number(String card) {
        if (rank(card).equals("NUMBER")) return Integer.parseInt(card.substring(1));
        return -1;
    }

    static int points(String card) {
        String r = rank(card);
        if (r.equals("NUMBER")) return number(card);
        if (r.equals("SKIP") || r.equals("REVERSE") || r.equals("DRAW_TWO")) return 20;
        if (r.equals("WILD") || r.equals("WILD_DRAW_FOUR")) return 50;
        return 0;
    }

    static void applyEffect(String card) {
        if (rank(card).equals("SKIP")) {
            next();
            next();
        } else if (rank(card).equals("REVERSE")) {
            direction = direction * -1;
            if (playerNames.size() == 2) {
                next();
                next();
            } else {
                next();
            }
        } else if (rank(card).equals("DRAW_TWO")) {
            next();
            hands.get(currentPlayer).add(draw());
            hands.get(currentPlayer).add(draw());
            Display.playerDrawsTwo(playerNames.get(currentPlayer));
            next();
        } else if (rank(card).equals("WILD_DRAW_FOUR")) {
            next();
            for (int i = 0; i < 4; i++) {
                hands.get(currentPlayer).add(draw());
            }
            Display.playerDrawsFour(playerNames.get(currentPlayer));
            next();
        } else {
            next();
        }
    }

    static void next() {
        currentPlayer += direction;
        if (currentPlayer >= playerNames.size()) currentPlayer = 0;
        if (currentPlayer < 0) currentPlayer = playerNames.size() - 1;
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
        if (color("R5").equals("R")) passed++; else fail("color R5");
        if (rank("G+2").equals("DRAW_TWO")) passed++; else fail("rank +2");
        if (points("W4") == 50) passed++; else fail("wild points");
        if (isLegal("R2", "R9", "")) passed++; else fail("same color");
        if (isLegal("G9", "R9", "")) passed++; else fail("same number");
        if (isLegal("B3", "W", "B")) passed++; else fail("called color");
        if (!isLegal("B3", "R9", "")) passed++; else fail("illegal mismatch");

        ArrayList<String> h = new ArrayList<String>();
        h.add("B3");
        h.add("R4");
        h.add("W");
        upCard = "R9";
        calledColor = "";
        if (chooseBotCard(h) == 1) passed++; else fail("bot normal before wild");

        ArrayList<String> h2 = new ArrayList<String>();
        h2.add("B1");
        h2.add("B2");
        h2.add("R3");
        if (chooseBotColor(h2).equals("B")) passed++; else fail("bot color");

        Display.selfTestResult(passed);
    }

    static void fail(String name) {
        throw new RuntimeException("Failed: " + name);
    }
}
