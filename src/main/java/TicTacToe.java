import java.util.ArrayList;

public class TicTacToe implements Game{
    private long player1;
    private long player2;
    private boolean reverse;
    private int counter;
    private int[][] state;
    private long winner = 0;

    TicTacToe(long player1,long player2){
        this.player1 = player1;
        this.player2 = player2;
        counter = 0;
        state = new int[3][3];
    }

    TicTacToe(long player){
        player1 = player;
        player2 = 1;
        counter = 0;
        state = new int[3][3];
    }

    @Override
    public boolean next(long player, String way){
        int index = (player == player1) ? 0 : 1;
        if (reverse) index += 1;
        int place = Integer.parseInt(way);
        int row = place / 3;
        int i = place % 3;
        if (counter % 2 != index) return false;
        counter += 1;
        state[row][i] = index + 1;
        if (player2 == 1 & !isFinished()) AIway();
        return true;
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

    private boolean checkRowCol(int row, int column){
        int check = row + column;
        if (check != 0){
            winner = (check == 1 & !reverse || check == 2 & reverse) ? player1 : player2;
            return true;
        }
        return false;
    }

    private boolean checkDiag(){
        int diag1 = state[0][0];
        int diag2 = state[0][2];
        for (int i = 1; i < 3; i++){
            diag1 = (diag1 == state[i][i]) ? diag1 : 0;
            diag2 = (diag2 == state[i][2-i]) ? diag2 : 0;
        }
        int check = diag1 + diag2;
        if (check != 0){
            winner = (check == 1 & !reverse || check == 2 & reverse) ? player1 : player2;
            return true;
        }
        return false;
    }

    @Override
    public Boolean isFinished() {
        int column = 0;
        int rowS = 0;
        int columnS = 0;
        for (int i = 0; i < 3; i++){
            rowS = state[i][0];
            columnS = state[0][column];
            for (int j = 1; j < 3; j++){
                rowS = (rowS == state[i][j]) ? rowS : 0;
                columnS = (columnS == state[j][column]) ? columnS : 0;
            }
            if (checkRowCol(rowS,columnS)){ return true; }
            column++;
        }
        return checkDiag() || counter == 9;
    }

    @Override
    public long getWinner(){
        return winner;
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
