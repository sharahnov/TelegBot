import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bot extends TelegramLongPollingBot {

    HashMap<Long, User> Users = new HashMap<>();
    long waitingUser;

    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new Bot());

        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }

    public synchronized SendMessage setKeyboardsButtons(long chatId, ArrayList<ArrayList<String>> buttons) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        ArrayList<KeyboardRow> keyboard = new ArrayList<>();
        for (ArrayList<String> row : buttons) {
            KeyboardRow keyboardCurRow = new KeyboardRow();
            for (String s : row) {
                keyboardCurRow.add(new KeyboardButton(s));
            }
            keyboard.add(keyboardCurRow);
        }
        replyKeyboardMarkup.setKeyboard(keyboard);
        return new SendMessage().setChatId(chatId).setReplyMarkup(replyKeyboardMarkup);
    }

    public SendMessage setInlineKeyBoardMessage(long chatId, ArrayList<ArrayList<String>> buttons) {
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
        return new SendMessage().setChatId(chatId).setReplyMarkup(inlineKeyboardMarkup);
    }

    public SendMessage setInlineKeyBoardMessageWithCBD(long chatId, ArrayList<ArrayList<String>> buttons, ArrayList<ArrayList<String>> CBdata) {
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
        return new SendMessage().setChatId(chatId).setReplyMarkup(inlineKeyboardMarkup);
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


    public void registerUser(Long chatId){
        if (!Users.containsKey(chatId)){
            Users.put(chatId, new User(chatId));
            Users.get(chatId).buttons = new ArrayList<>();
        }
    }


    public void launchMessage(long chatId){
        User user = Users.get(chatId);
        user.state = "default";
        ArrayList<ArrayList<String>> buttons = new ArrayList<>();
        ArrayList<String> curButtons = new ArrayList<>();
        curButtons.add("Играть");
        curButtons.add("Помощь");
        buttons.add(curButtons);
        SendMessage outMessage = setInlineKeyBoardMessage(chatId, buttons);
        sendMessage(chatId, outMessage, "Добро пожаловать. Выбирайте что хотите дальше");
    }


    public void gameLaunchMessage(User user){
        user.state = "game";
        ArrayList<ArrayList<String>> buttons = new ArrayList<>();
        ArrayList<String> curButtons = new ArrayList<>();
        curButtons.add("Играть 1");
        curButtons.add("Играть 2");
        curButtons.add("Помощь");
        buttons.add(curButtons);
        /*SendMessage outMessage = setInlineKeyBoardMessage(chatId, buttons);
        sendMessage(chatId, outMessage, "Добро пожаловать. Выбирайте что хотите дальше");*/
        editAndSendMessageWithIK(user, "Добро пожаловать. Выбирайте что хотите дальше", getInlineKeyBoard(buttons));
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
        user.buttons.clear();
        user.buttons.add(curButtons);
        editAndSendMessageWithIK(user, "Управление\n" +
                "Взаимодействуйте с ботом только по полученной клавиатуре.\n" +
                "\n" +
                "Как играть\n" +
                "Во время игры вы будете получать клавиатуру с кнопками, соответствующими клеткам на поле. Нажмите на кнопку с ⬜, чтобы сделать ход.", getInlineKeyBoard(user.buttons));
    }




    public void editAndSendMessageWithIK(User user, String text, InlineKeyboardMarkup keyboard){
        EditMessageText outMessage = new EditMessageText()
                .setChatId(user.id)
                .setMessageId(user.messageId)
                .setText(text)
                .setReplyMarkup(keyboard);
        try {
            execute(outMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public void editAndSendMessage(User user, String text){
        EditMessageText outMessage = new EditMessageText()
                .setChatId(user.id)
                .setMessageId(user.messageId).setText(text);
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
        user.buttons.clear();
        user.buttons.add(curButtons);
        /*SendMessage outMessage = setInlineKeyBoardMessage(chatId, user.buttons);
        sendMessage(chatId, outMessage, "Выбирайте чем ходить");*/
        editAndSendMessageWithIK(user, "Выбирайте чем ходить", getInlineKeyBoard(user.buttons));
    }


    public void initializeTicTacToe2(User user1, User user2){
        Game game = new TicTacToe(user1.id, user2.id);
        user1.game = user2.game = game;
        user1.state = user2.state = "game2";
        sendGameMessage(user1);
        sendGameMessage(user2);
    }


    public void sendGameMessage(User user){
        UserMap message = user.game.getMessage();
        EditMessageText outMessage = new EditMessageText()
                    .setChatId(user.id)
                    .setMessageId(user.messageId).setText("ky")
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
                        waitingUser = user.id;
                        sendMessage(user.id, "Ждем соперника");
                        break;
                    default:
                        helpMessage(user);
                }
                break;
            case("game1"):
                switch (inText) {
                    case ("❌"):
                        user.game.setWay(chatId, 0);
                        sendGameMessage(user);
                        break;
                    case ("⭕"):
                        user.game.setWay(chatId, 1);
                        sendGameMessage(user);
                        break;
                    default:
                        user.game.next(chatId, inText);
                        sendGameMessage(user);
                        break;
                }
                break;
            case("game2"):
                switch (inText) {
                    default:
                        user.game.next(chatId, inText);
                        long user2 = user.game.getOpponentId(user.id);
                        sendGameMessage(user);
                        sendGameMessage(Users.get(user2));
                        break;
                }
                break;
            default:
                user.state = "game";
                helpMessage(user);
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
            System.out.println(inText);
        } else if(update.hasCallbackQuery()){
            callBackQueryHandler(update);
            }


    }

    public String getBotUsername() {
        return "My_First_OOP_bot";
    }

    public String getBotToken() {
        return System.getenv("JAVABOT");
    }
}
