package si.dteeam.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import si.dteeam.entity.Links;
import si.dteeam.entity.Users;
import si.dteeam.parser.AvtonetParser;
import si.dteeam.repository.UsersRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Autowired
    private AvtonetParser parser;
    @Autowired
    private UsersRepository usersRepository;

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
            User userFromUpdate = update.getMessage().getFrom();

            Optional<Users> existingUserOptional = usersRepository.findByChatID(chatId);

            String responseText = "";

            List<Links> linkList = new ArrayList<>();
            Users user;
            user = existingUserOptional.get();

            if (messageText.startsWith("http")) {
                System.out.println("TESTIRAM");

                Links link = new Links();
                if (existingUserOptional.isPresent()) {
                    //user = existingUserOptional.get();
                    linkList = user.getUrl();
                    link.setUrl(messageText);
                    link.setUser(user);

                    if(!linkList.contains(link)) {
                        linkList.add(link);
                        user.setUrl(linkList);
                        usersRepository.save(user); //doda nov url v List<urljev> userja
                    }
                } else {
                    link.setUrl(messageText);
                    linkList.add(link);

                    user = new Users();
                    user.setChatID(chatId);
                    user.setFirstName(userFromUpdate.getFirstName());
                    user.setLastName(userFromUpdate.getLastName());
                    user.setUrl(linkList);
                    usersRepository.save(user);
                }
                System.out.println("Dodan je bil user " + user);

                //parser.parse(messageText, user);
                sendMessage(chatId, "Parser zagnan za: " + messageText);

            } else if (messageText.startsWith("r")) {
                sendMessage(chatId, "Brišem userja");
                usersRepository.delete(user);
                
            } else {
                responseText = "Pošlji url za parsanje.";
                sendMessage(chatId, responseText);


            }


        }
    }

    private void sendMessage(Long chatId, String messageToSend) {
        SendMessage messageOut = new SendMessage();
        messageOut.setChatId(chatId.toString());
        messageOut.setText(messageToSend);

        try {
            execute(messageOut);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}

