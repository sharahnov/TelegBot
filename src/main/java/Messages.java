import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public abstract class Messages {
    public static InlineKeyboardMarkup getInlineKeyBoardWithCBD(ArrayList<ArrayList<String>> buttons, ArrayList<ArrayList<String>> CBdata) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        for (int i = 0; i < buttons.size(); i++) {
            List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
            for (int j = 0; j < buttons.get(i).size(); j++) {
                keyboardButtonsRow.add(new InlineKeyboardButton().setText(buttons.get(i).get(j)).setCallbackData(CBdata.get(i).get(j)));
            }
            rowList.add(keyboardButtonsRow);
        }
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup getInlineKeyBoard(ArrayList<ArrayList<String>> buttons) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        for (ArrayList<String> row : buttons) {
            List<InlineKeyboardButton> keyboardButtonsRow = new ArrayList<>();
            for (String s : row) {
                keyboardButtonsRow.add(new InlineKeyboardButton().setText(s).setCallbackData(s));
            }
            rowList.add(keyboardButtonsRow);
        }
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public static String getTextStatistics(User user){
        String text = "        \uD83C\uDFC6  ⚖️  \uD83D\uDE2D\n" + "\uD83D\uDC76:";
        int[][] stats = user.getStatistics();
        for (int i = 0; i < 3; i++){
            text += "  " + stats[0][i];
        }
        text += "\n\uD83D\uDDA5:";
        for (int i = 0; i < 3; i++){
            text += "  " + stats[1][i];
        }
        return text += "\n";
    }

    public static String getTextMap(ArrayList<ArrayList<String>> map){
        String text = "";
        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++){
                text += map.get(i).get(j);
            }
            text += "\n";
        }
        return text;
    }
}
