import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.ArrayList;
import java.util.HashMap;

public class Bot extends TelegramLongPollingBot {

    private static HashMap<Long, User> Users;
    private long waitingUser;
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
            switch (inText){
                case ("/start"):
                    launchMessage(chatId);
                    break;
                default:
                    sendMessage(chatId,"Не знаю такой команды(, попробуйте /start ");
            }
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
        SendMessage outMessage = new SendMessage().setChatId(user.getId()).setReplyMarkup(Messages.getInlineKeyBoard(user.buttons));
        sendMessage(chatId, outMessage, "Добро пожаловать. Выбирайте что хотите дальше");
        user.buttons.clear();
        dbworker.updateUser(user);
    }

    public void gameLaunchMessage(User user, String addText){
        user.state = "game";
        String text = Messages.getTextStatistics(user);
        ArrayList<String> curButtons = new ArrayList<>();
        curButtons.add("Играть 1");
        curButtons.add("Играть 2");
        curButtons.add("Помощь");
        user.buttons.add(curButtons);
        if (addText != "") text += addText;
        editAndSendMessageWithIK(user, text, Messages.getInlineKeyBoard(user.buttons));
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
                "Во время игры вы будете получать клавиатуру с кнопками, соответствующими клеткам на поле. Нажмите на кнопку с ⬜, чтобы сделать ход.", Messages.getInlineKeyBoard(user.buttons));
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

    public void initializeTicTacToe1(User user){
        user.game = new TicTacToe(user.getId());
        ArrayList<String> curButtons = new ArrayList<>();
        curButtons.add("❌");
        curButtons.add("⭕");
        user.buttons.add(curButtons);
        editAndSendMessageWithIK(user, "Выбирайте чем ходить", Messages.getInlineKeyBoard(user.buttons));
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
                    .setReplyMarkup(Messages.getInlineKeyBoardWithCBD(message.buttons, message.callBackData));
            try {
                execute(outMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
    }

    private void defaultCallback(User user, String text){
        if (text.equals("Играть")){
            gameLaunchMessage(user, "");
        } else {
            helpMessage(user);
        }
    }

    private void initialize2players(User user){
        if (waitingUser != 0){
            initializeTicTacToe2(user, Users.get(waitingUser));
            waitingUser = 0;
            return;
        }
        waitingUser = user.getId();
        waitMessage(user);
    }

    private void gameInitialization(User user, String inText){
        switch (inText) {
            case ("Играть 1"):
                initializeTicTacToe1(user);
                user.state = "game1";
                break;
            case ("Играть 2"):
                initialize2players(user);
                break;
            case ("Прервать поиск"):
                waitingUser = 0;
                gameLaunchMessage(user, "");
                break;
            default:
                helpMessage(user);
        }
    }

    public void callBackQueryHandler(Update update){
        Message inMessage = update.getCallbackQuery().getMessage();
        String inText = update.getCallbackQuery().getData();
        Long chatId = inMessage.getChatId();
        User user = Users.get(chatId);
        user.messageId = inMessage.getMessageId();
        switch(user.state) {
            case ("default"):
                defaultCallback(user, inText);
                break;
            case ("game"):
                gameInitialization(user, inText);
                break;
            case("game1"):
                soloGameIteration(user, inText);
                break;
            case("game2"):
                multiplayerIteration(user, inText);
                break;
            default:
                user.state = "game";
                dbworker.updateUser(user);
                helpMessage(user);
        }
    }

    private void soloGameIteration(User user, String inText){
        switch (inText) {
            case ("❌"):
                user.game.setWay(user.getId(), 0);
                sendGameMessage(user, "Ваш ход");
                break;
            case ("⭕"):
                user.game.setWay(user.getId(), 1);
                sendGameMessage(user, "Ваш ход");
                break;
            default:
                user.game.next(user.getId(), inText);
                if (user.game.isFinished()){
                    String text = Messages.getTextMap(user.game.getMessage().buttons);
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
    }

    private void multiplayerIteration(User user, String inText){
        if (!user.game.next(user.getId(), inText)){ return; }
        long user2 = user.game.getOpponentId(user.getId());
        if (user.game.isFinished()){
            finishGame(user.game.getWinner(), user, Users.get(user2));
            return;
        }
        sendGameMessage(user, "Ход оппонента");
        sendGameMessage(Users.get(user2), "Ваш ход");
    }

    private void waitMessage(User user) {
        ArrayList<String> curButtons = new ArrayList<>();
        curButtons.add("Прервать поиск");
        user.buttons.add(curButtons);
        editAndSendMessageWithIK(user, "Ожидаем оппонента", Messages.getInlineKeyBoard(user.buttons));
        user.buttons.clear();
    }

    private void finishGame(long winner, User user1, User user2) {
        String text = Messages.getTextMap(user1.game.getMessage().buttons);
        if (winner == 0){
            user1.updateStatistics(Enums.statisticState.PvPdraw);
            user2.updateStatistics(Enums.statisticState.PvPdraw);
            gameLaunchMessage(user1, "Draw\n" + text);
            gameLaunchMessage(user2, "Draw\n" + text);
            return;
        }
        if (winner == user1.getId()){
            finishWinGame(user1, user2, text);
        } else {
            finishWinGame(user2, user1, text);
        }
    }

    private void finishWinGame(User winner, User looser, String text){
        winner.updateStatistics(Enums.statisticState.PvPwin);
        looser.updateStatistics(Enums.statisticState.PvPlose);
        gameLaunchMessage(winner, "You win\n" + text);
        gameLaunchMessage(looser, "You lose\n" + text);
    }



    public String getBotUsername() {
        return "My_First_OOP_bot";
    }

    public String getBotToken() {
        return System.getenv("JAVABOT");
    }
}
