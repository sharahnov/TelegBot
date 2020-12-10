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

public class Bot extends TelegramLongPollingBot {

    HashMap<Long, User> Users = new HashMap<>();

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

    public void sendMessage(Long chatId, String text) {
        try {
            SendMessage outMessage = new SendMessage();
            outMessage.setChatId(chatId);
            outMessage.setText(text);
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

    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message inMessage = update.getMessage();
            String inText = inMessage.getText();
            Long chatId = inMessage.getChatId();
            if (!Users.containsKey(chatId)){
                Users.put(chatId, new User(chatId));
                Users.get(chatId).buttons = new ArrayList<>();
                System.out.println(chatId);
            }
            User user = Users.get(chatId);
            switch (inText){
                case ("/start"):
                    user.state = "default";
                    ArrayList<ArrayList<String>> buttons = new ArrayList<>();
                    ArrayList<String> curButtons = new ArrayList<>();
                    curButtons.add("Играть");
                    curButtons.add("Помощь");
                    buttons.add(curButtons);
                    SendMessage outMessage = setInlineKeyBoardMessage(chatId, buttons);
                    sendMessage(chatId, outMessage, "Добро пожаловать. Выбирайте что хотите дальше");
                    break;
                default:
                    sendMessage(chatId,"Не знаю такой команды(, попробуйте /start ");
            }
            System.out.println(inText);
        } else if(update.hasCallbackQuery()){
            Message inMessage = update.getCallbackQuery().getMessage();
            String inText = update.getCallbackQuery().getData();
            Long chatId = inMessage.getChatId();
            User user = Users.get(chatId);
            Integer message_id = inMessage.getMessageId();
            switch(user.state){
                case ("default"):
                    switch (inText){
                        case("Играть"):
                            user.state = "game";
                            ArrayList<ArrayList<String>> buttons = new ArrayList<>();
                            ArrayList<String> curButtons = new ArrayList<>();
                            curButtons.add("Играть TicTacToe");
                            curButtons.add("Помощь");
                            buttons.add(curButtons);
                            SendMessage outMessage = setInlineKeyBoardMessage(chatId, buttons);
                            sendMessage(chatId, outMessage, "Добро пожаловать. Выбирайте что хотите дальше");
                            break;
                        case("Помощь"):
                            sendMessage(chatId,"Управление\n" +
                                    "Взаимодействуйте с ботом только по полученной клавиатуре.\n" +
                                    "\n" +
                                    "Как играть\n" +
                                    "Во время игры вы будете получать клавиатуру с кнопками, соответствующими клеткам на поле. Нажмите на кнопку с ⬜, чтобы сделать ход.");
                            break;
                        default:
                            sendMessage(chatId,"Очень странная ситуация");
                    }
                    break;
                case("game"):
                    switch(inText){
                        case ("Играть TicTacToe"):
                            List<Long> players = new ArrayList<Long>();
                            players.add(chatId);
                            user.game = new TicTacToe(players);
                            ArrayList<String> curButtons = new ArrayList<>();
                            curButtons.add("❌");
                            curButtons.add("⭕");
                            user.buttons.clear();
                            user.buttons.add(curButtons);
                            SendMessage outMessage = setInlineKeyBoardMessage(chatId, user.buttons);
                            sendMessage(chatId, outMessage, "Выбирайте чем ходить");
                            break;
                        case ("❌"):
                            user.game.setWay(chatId, 0);
                            UserMap map = user.game.getMap();
                            outMessage = setInlineKeyBoardMessageWithCBD(chatId, map.buttons, map.callBackData);
                            sendMessage(chatId, outMessage, "Выбирайте");
                            break;
                        case ("⭕"):
                            user.game.setWay(chatId, 0);
                            map = user.game.getMap();
                            outMessage = setInlineKeyBoardMessageWithCBD(chatId, map.buttons, map.callBackData);
                            sendMessage(chatId, outMessage, "Выбирайте");
                            break;
                        default:
                            user.game.next(chatId, inText);
                            map = user.game.getMap();
                            outMessage = setInlineKeyBoardMessageWithCBD(chatId, map.buttons, map.callBackData);
                            sendMessage(chatId, outMessage, "Выбирайте");
                    }
                    break;
                default:
                    sendMessage(chatId,"Очень странная ситуация");
            }
            /*EditMessageText outMessage = new EditMessageText()
                    .setChatId(chatId)
                    .setMessageId(message_id)
                    .setText("Yeahoo");
            try {
                execute(outMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }*/
        }
    }

    public String getBotUsername() {
        return "My_First_OOP_bot";
    }

    public String getBotToken() {
        return "эмм";
    }
}
