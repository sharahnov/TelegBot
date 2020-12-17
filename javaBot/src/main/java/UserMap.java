import java.util.ArrayList;

public class UserMap {
    ArrayList<ArrayList<String>> buttons;
    ArrayList<ArrayList<String>> callBackData;

    UserMap(ArrayList<ArrayList<String>> buttons, ArrayList<ArrayList<String>> callBackData){
        this.buttons = buttons;
        this.callBackData = callBackData;
    };
}
