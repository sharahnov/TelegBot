import java.util.ArrayList;

public interface Game {
    boolean next(long player, String way);
    void setWay(long player, int way);
    Boolean isFinished();
    UserMap getMessage();
    long getOpponentId(long player);
    long getWinner();
}
