import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TicTacToe implements Game{
    private HashMap<Long, Integer> players;
    private int counter;
    List<int[][]> statistics;
    int[][] state;

    TicTacToe(List<Long> players){
        this.players = new HashMap<>();
        this.counter = 0;
        for (Long id : players) {
            this.players.put(id, 0);
        }
        state = new int[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++){
                state[i][j] = 0;
            }
        }
    }

    @Override
    public void next(long player, String way){
        int index = players.get(player);
        int place = Integer.parseInt(way);
        int row = place / 3;
        int i = place % 3;
        state[row][i] = index + 1;
        /*for (i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++){
                System.out.println(state[i][j]);
            }
        }*/
    }

    @Override
    public void setWay(long player, int way){
        players.put(player, way);
        for (Long id : players.keySet()) {
            if (id != player){
                players.put(id, (way + 1) % 2);
                }
            }
    }

    @Override
    public UserMap getMap(){
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
}
