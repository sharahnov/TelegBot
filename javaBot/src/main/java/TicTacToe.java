import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TicTacToe implements Game{
    private long player1;
    private long player2;
    private boolean reverse;
    private int counter;
    List<int[][]> statistics;
    int[][] state;

    TicTacToe(long player1,long player2){
        this.player1 = player1;
        this.player2 = player2;
        this.counter = 0;
        state = new int[3][3];
    }


    TicTacToe(long player){
        this.player1 = player;
        this.counter = 0;
        state = new int[3][3];
    }


    @Override
    public void next(long player, String way){
        int index = (player == player1) ? 0 : 1;
        if (reverse) index += 1;
        int place = Integer.parseInt(way);
        int row = place / 3;
        int i = place % 3;
        if (counter % 2 != index) return;
        counter += 1;
        state[row][i] = index + 1;
        if (player2 == 0) AIway();
    }

    public void AIway(){
        int way = (reverse) ? 1 : 2;
        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++){
                if (state[i][j] == 0) {
                    counter++;
                    state[i][j] = way;
                    return;
                }
            }
        }
    }

    @Override
    public void setWay(long player, int way){
        if (way == 1){
            reverse = true;
            AIway();
        }
    }

    @Override
    public Boolean isFinished() {
        return counter != 9;
    }

    @Override
    public UserMap getMessage(){
        ArrayList<ArrayList<String>> buttons = new ArrayList<>();
        ArrayList<ArrayList<String>> callBackData = new ArrayList<>();
        int j = 0;
        for (int[] line : state) {
            ArrayList<String> row = new ArrayList<>();
            ArrayList<String> cRow = new ArrayList<>();
            for (int i : line){
                switch(i) {
                    case(0):
                        row.add("⬜");
                        break;
                    case(1):
                        row.add("❌");
                        break;
                    case(2):
                        row.add("⭕");
                        break;
                }
                cRow.add(String.valueOf(j));
                j++;
            }
            buttons.add(row);
            callBackData.add(cRow);
        }
        return new UserMap(buttons, callBackData);
    }

    @Override
    public long getOpponentId(long player) {
        return (player == player1) ? player2 : player1;
    }
}
