package model;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import utils.DuckDuckHandler;
import utils.Translator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Bot extends TelegramLongPollingBot {

    private static final String botUsername = System.getenv("botUsername");
    private static final String botToken = System.getenv("botToken");
    private static final Logger LOG = Logger.getLogger("ESBLOG");
    DuckDuckHandler duckDuckHandler = new DuckDuckHandler();

    /**
     * Метод для приема сообщений.
     *
     * @param update Содержит сообщение от пользователя.
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasInlineQuery()) {
            AnswerInlineQuery inlineAnswer = new AnswerInlineQuery();
            inlineAnswer.setInlineQueryId(update.getInlineQuery().getId());

            InlineQueryResultArticle result1 = new InlineQueryResultArticle();
            InputTextMessageContent messageContent1 = new InputTextMessageContent();
            messageContent1.setMessageText("Hello " + update.getInlineQuery().getFrom().getFirstName());
            result1.setInputMessageContent(messageContent1);
            result1.setId("1");
            result1.setTitle(Translator.TranslateDirection.en_ru.name());
            result1.setDescription("English -> Russian");

            InputTextMessageContent messageContent2 = new InputTextMessageContent();
            messageContent2.setMessageText("Your query is " + update.getInlineQuery().getQuery());
            InlineQueryResultArticle result2 = new InlineQueryResultArticle();
            result2.setInputMessageContent(messageContent2);
            result2.setId("2");
            result2.setTitle(Translator.TranslateDirection.ru_en.name());
            result2.setDescription("Russian -> English");

            inlineAnswer.setResults(result1, result2);

            try {
                execute(inlineAnswer);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else if (update.hasMessage()) {
            sendMsg(update.getMessage().getChatId().toString(), update);
        } else if (update.hasCallbackQuery()) {
            String ans = "ERROR";
            switch (update.getCallbackQuery().getData()) {
                case "id":
                    ans = update.getCallbackQuery().getFrom().getId().toString();
                    break;
                case "name":
                    ans = update.getCallbackQuery().getFrom().getFirstName();
                    break;
            }
            answerCallbackQuery(update.getCallbackQuery().getId(), ans);
        }
    }

    public synchronized void sendMsg(String chatId, Update update) {
        String answer = "ERROR";
        try {
            answer = duckDuckHandler.search(update.getMessage().getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(answer);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            LOG.log(Level.SEVERE, "Exception: " + e.getMessage(), e);
        }
    }

    /**
     * Метод возвращает имя бота, указанное при регистрации.
     *
     * @return имя бота
     */
    @Override
    public String getBotUsername() {
        return botUsername;
    }

    /**
     * Метод возвращает token бота для связи с сервером Telegram
     *
     * @return token для бота
     */
    @Override
    public String getBotToken() {
        return botToken;
    }

    public synchronized void setButtons(SendMessage sendMessage) {
        // Создаем клавиуатуру
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        // Создаем список строк клавиатуры
        List<KeyboardRow> keyboard = new LinkedList<>();

        // Первая строчка клавиатуры
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        // Добавляем кнопки в первую строчку клавиатуры
        keyboardFirstRow.add(new KeyboardButton("Привет"));

        // Вторая строчка клавиатуры
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        // Добавляем кнопки во вторую строчку клавиатуры
        keyboardSecondRow.add(new KeyboardButton("Помощь"));

        // Добавляем все строчки клавиатуры в список
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        // и устанваливаем этот список нашей клавиатуре
        replyKeyboardMarkup.setKeyboard(keyboard);
    }

    public synchronized void answerCallbackQuery(String callbackId, String message) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackId);
        answer.setText(message);
        answer.setShowAlert(true);
        try {
            execute(answer);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void setInline(SendMessage sendMessage) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> buttons1 = new ArrayList<>();
        buttons1.add(new InlineKeyboardButton().setText("Get ID").setCallbackData("id"));
        buttons.add(buttons1);

        List<InlineKeyboardButton> buttons2 = new ArrayList<>();
        buttons2.add(new InlineKeyboardButton().setText("Get Name").setCallbackData("name"));
        buttons.add(buttons2);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(buttons);
        sendMessage.setReplyMarkup(markupKeyboard);
    }
}