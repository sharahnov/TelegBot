import java.util.ArrayList;
import java.util.List;

public class User {
    Long id;
    Integer messageId;
    String state;
    Game game;
    List<int[][]> statistics;
    ArrayList<ArrayList<String>> buttons;

    User(long id){
        this.id = id;
        state = "";
    }
}
