package si.dteeam.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import si.dteeam.parser.AvtonetParser;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Autowired
    private AvtonetParser parser;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            String responseText = "";

            if (messageText.startsWith("http")) {
                    //parser.parse(messageText);
                    sendMessage(chatId, "Parser zagnan za: " + messageText);

            } else {
                responseText = "Pošlji url za parsanje.";
                sendMessage(chatId, responseText);
            }
        }
    }

    private void sendMessage(Long chatId, String messageIn) {
        SendMessage messageOut = new SendMessage();
        messageOut.setChatId(chatId.toString());
        messageOut.setText(messageIn);

        try {
            execute(messageOut);
        }
        catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public TelegramBot(AvtonetParser avtonetParser) {

    }
}

