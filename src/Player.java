import java.util.ArrayList;

public class Player {
    String name;
    boolean human;
    ArrayList<String> hand;

    Player(String name, boolean human) {
        this.name = name;
        this.human = human;
        this.hand = new ArrayList<String>();
    }
}
