import java.util.ArrayList;

public class User {
    private Long id;
    int messageId;
    String state;
    Game game;
    private int[][] statistics;
    ArrayList<ArrayList<String>> buttons;

    User(long id){
        this.id = id;
        state = "default";
        statistics = new int[2][3];
    }

    User(long id, int[][] statistics){
        this.id = id;
        this.statistics = statistics;
        buttons = new ArrayList<>();
    }

    public Long getId(){
        return id;
    }

    public int[][] getStatistics(){ return statistics; }

    public void  updateStatistics(Enums.statisticState state){
        switch(state){
            case PvPwin:
                statistics[0][0]++;
                break;
            case PvEwin:
                statistics[1][0]++;
                break;
            case PvPdraw:
                statistics[0][1]++;
                break;
            case PvEdraw:
                statistics[1][1]++;
                break;
            case PvPlose:
                statistics[0][2]++;
                break;
            case PvElose:
                statistics[1][2]++;
                break;
        }
    }
}
