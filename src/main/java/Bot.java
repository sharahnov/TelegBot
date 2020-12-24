import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Bot extends TelegramLongPollingBot {

    private static HashMap<Long, User> Users;
    long waitingUser;
    private static DBworker dbworker;

    public static void main(String[] args) {
        dbworker = new DBworker();
        Users = dbworker.getUsers();
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new Bot());

        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }

    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String inText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            registerUser(chatId);
            System.out.println(inText);

            switch (inText){
                case ("/start"):
                    launchMessage(chatId);
                    break;
                default:
                    sendMessage(chatId,"Не знаю такой команды(, попробуйте /start ");
            }
            System.out.println(inText);
        } else if(update.hasCallbackQuery()){
            callBackQueryHandler(update);
        }
    }

    public void registerUser(Long chatId){
        if (!Users.containsKey(chatId)){
            Users.put(chatId, new User(chatId));
            Users.get(chatId).buttons = new ArrayList<>();
            dbworker.insertUser(Users.get(chatId));
        }
    }

    public InlineKeyboardMarkup getInlineKeyBoardWithCBD(ArrayList<ArrayList<String>> buttons, ArrayList<ArrayList<String>> CBdata) {
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


    public InlineKeyboardMarkup getInlineKeyBoard(ArrayList<ArrayList<String>> buttons) {
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


    public void sendMessage(Long chatId, String text) {
        try {
            SendMessage outMessage = new SendMessage();
            outMessage.setChatId(chatId).setText(text);
            execute(outMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Long chatId, SendMessage outMessage, String text) {
        try {
            outMessage.setChatId(chatId);
            outMessage.setText(text);
            execute(outMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void launchMessage(long chatId){
        User user = Users.get(chatId);
        user.state = "default";
        ArrayList<String> curButtons = new ArrayList<>();
        curButtons.add("Играть");
        curButtons.add("Помощь");
        user.buttons.add(curButtons);
        SendMessage outMessage = new SendMessage().setChatId(user.getId()).setReplyMarkup(getInlineKeyBoard(user.buttons));
        sendMessage(chatId, outMessage, "Добро пожаловать. Выбирайте что хотите дальше");
        user.buttons.clear();
    }

    public void gameLaunchMessage(User user){
        user.state = "game";
        String text = getTextStatistics(user);
        ArrayList<String> curButtons = new ArrayList<>();
        curButtons.add("Играть 1");
        curButtons.add("Играть 2");
        curButtons.add("Помощь");
        user.buttons.add(curButtons);
        editAndSendMessageWithIK(user, text + "Выбирайте что хотите", getInlineKeyBoard(user.buttons));
        user.buttons.clear();
    }
    public void gameLaunchMessage(User user, String addText){
        user.state = "game";
        String text = getTextStatistics(user);
        ArrayList<String> curButtons = new ArrayList<>();
        curButtons.add("Играть 1");
        curButtons.add("Играть 2");
        curButtons.add("Помощь");
        user.buttons.add(curButtons);
        editAndSendMessageWithIK(user, text + addText, getInlineKeyBoard(user.buttons));
        user.buttons.clear();
        dbworker.updateUser(user);
    }

    public void helpMessage(User user){
        ArrayList<String> curButtons = new ArrayList<>();
        switch(user.state){
            case("default"):
                curButtons.add("Играть");
                break;
            case("game"):
                curButtons.add("Играть 1");
                curButtons.add("Играть 2");
                break;
        }
        user.buttons.add(curButtons);
        editAndSendMessageWithIK(user, "Управление\n" +
                "Взаимодействуйте с ботом только по полученной клавиатуре.\n" +
                "\n" +
                "Как играть\n" +
                "Во время игры вы будете получать клавиатуру с кнопками, соответствующими клеткам на поле. Нажмите на кнопку с ⬜, чтобы сделать ход.", getInlineKeyBoard(user.buttons));
        user.buttons.clear();
    }

    public void editAndSendMessageWithIK(User user, String text, InlineKeyboardMarkup keyboard){
        EditMessageText outMessage = new EditMessageText()
                .setChatId(user.getId())
                .setMessageId(user.messageId)
                .setText(text)
                .setReplyMarkup(keyboard);
        try {
            execute(outMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void initializeTicTacToe1(User user, long chatId){
        user.game = new TicTacToe(chatId);
        ArrayList<String> curButtons = new ArrayList<>();
        curButtons.add("❌");
        curButtons.add("⭕");
        user.buttons.add(curButtons);
        editAndSendMessageWithIK(user, "Выбирайте чем ходить", getInlineKeyBoard(user.buttons));
        user.buttons.clear();
    }

    public void initializeTicTacToe2(User user1, User user2){
        Game game = new TicTacToe(user1.getId(), user2.getId());
        user1.game = user2.game = game;
        user1.state = user2.state = "game2";
        sendGameMessage(user1, "Ваш ход");
        sendGameMessage(user2, "Ход оппонента");
    }

    public void sendGameMessage(User user, String text){
        UserMap message = user.game.getMessage();
        EditMessageText outMessage = new EditMessageText()
                    .setChatId(user.getId())
                    .setMessageId(user.messageId).setText(text)
                    .setReplyMarkup(getInlineKeyBoardWithCBD(message.buttons, message.callBackData));
            try {
                execute(outMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
    }

    public void callBackQueryHandler(Update update){
        Message inMessage = update.getCallbackQuery().getMessage();
        String inText = update.getCallbackQuery().getData();
        Long chatId = inMessage.getChatId();
        User user = Users.get(chatId);
        user.messageId = inMessage.getMessageId();
        System.out.println(user.messageId);
        System.out.println(user.state);
        switch(user.state) {
            case ("default"):
                switch (inText) {
                    case ("Играть"):
                        gameLaunchMessage(user);
                        break;
                    default:
                        helpMessage(user);
                }
                break;
            case ("game"):
                switch (inText) {
                    case ("Играть 1"):
                       initializeTicTacToe1(user, chatId);
                       user.state = "game1";
                       break;
                    case ("Играть 2"):
                        if (waitingUser != 0){
                            initializeTicTacToe2(user, Users.get(waitingUser));
                            waitingUser = 0;
                            return;
                        }
                        waitingUser = user.getId();
                        waitMessage(user);
                        break;
                    case ("Прервать поиск"):
                        waitingUser = 0;
                        gameLaunchMessage(user);
                        break;
                    default:
                        helpMessage(user);
                }
                break;
            case("game1"):
                switch (inText) {
                    case ("❌"):
                        user.game.setWay(chatId, 0);
                        sendGameMessage(user, "Ваш ход");
                        break;
                    case ("⭕"):
                        user.game.setWay(chatId, 1);
                        sendGameMessage(user, "Ваш ход");
                        break;
                    default:
                        user.game.next(chatId, inText);
                        if (user.game.isFinished()){
                            String text = getTextMap(user.game.getMessage().buttons);
                            switch((int) user.game.getWinner()){
                                case(0):
                                    user.updateStatistics(Enums.statisticState.PvEdraw);
                                    gameLaunchMessage(user, "Draw\n" + text);
                                    break;
                                case(1):
                                    user.updateStatistics(Enums.statisticState.PvElose);
                                    gameLaunchMessage(user, "You lose\n" + text);
                                    break;
                                default:
                                    user.updateStatistics(Enums.statisticState.PvEwin);
                                    gameLaunchMessage(user, "You win\n" + text);
                                    break;
                            }
                            return;
                        }
                        sendGameMessage(user, "Ваш ход");
                        break;
                }
                break;
            case("game2"):
                switch (inText) {
                    default:
                        if (!user.game.next(chatId, inText)){ return; }
                        long user2 = user.game.getOpponentId(user.getId());
                        if (user.game.isFinished()){
                            finishGame(user.game.getWinner(), user, Users.get(user2));
                            return;
                        }
                        sendGameMessage(user, "Ход оппонента");
                        sendGameMessage(Users.get(user2), "Ваш ход");
                        break;
                }
                break;
            default:
                user.state = "game";
                helpMessage(user);
        }
    }

    private void waitMessage(User user) {
        ArrayList<String> curButtons = new ArrayList<>();
        curButtons.add("Прервать поиск");
        user.buttons.add(curButtons);
        editAndSendMessageWithIK(user, "Ожидаем оппонента", getInlineKeyBoard(user.buttons));
        user.buttons.clear();
    }

    private void finishGame(long winner, User user1, User user2) {
        String text = getTextMap(user1.game.getMessage().buttons);
        if (winner == 0){
            user1.updateStatistics(Enums.statisticState.PvPdraw);
            user2.updateStatistics(Enums.statisticState.PvPdraw);
            gameLaunchMessage(user1, "Draw\n" + text);
            gameLaunchMessage(user2, "Draw\n" + text);
            return;
        }
        if (winner == user1.getId()){
            user1.updateStatistics(Enums.statisticState.PvPwin);
            user2.updateStatistics(Enums.statisticState.PvPlose);
            gameLaunchMessage(user1, "You win\n" + text);
            gameLaunchMessage(user2, "You lose\n" + text);
        } else {
            user1.updateStatistics(Enums.statisticState.PvPlose);
            user2.updateStatistics(Enums.statisticState.PvPwin);
            gameLaunchMessage(user1, "You lose\n" + text);
            gameLaunchMessage(user2, "You win\n" + text);
        }
    }

    private String getTextStatistics(User user){
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

    private String getTextMap(ArrayList<ArrayList<String>> map){
        String text = "";
        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++){
                text += map.get(i).get(j);
            }
            text += "\n";
        }
        return text;
    }

    /*private void finishMessage(User user1, User user2, long winner) {
        UserMap map = user1.game.getMessage();
        String
    }*/

    public String getBotUsername() {
        return "My_First_OOP_bot";
    }

    public String getBotToken() {
        return System.getenv("JAVABOT");
    }
}
