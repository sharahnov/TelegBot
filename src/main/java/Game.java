import java.util.ArrayList;

public interface Game {
    void next(long player, String way);
    void setWay(long player, int way);
    UserMap getMap();
}
