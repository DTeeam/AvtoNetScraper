package si.dteeam.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import si.dteeam.entity.Links;
import si.dteeam.entity.Users;
import si.dteeam.events.VehicleEvent;
import si.dteeam.parser.AvtonetParser;
import si.dteeam.repository.UsersRepository;

import java.util.ArrayList;
import java.util.List;

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

    @EventListener
    public void handleNewVehicleEvent(VehicleEvent event) {
        sendMessage(event.getChatId(), event.getMessage());
    }


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
            String responseText = "";


            Users user = usersRepository.findByChatID(chatId).orElseGet(() -> {
                Users newUser = new Users();
                newUser.setChatID(chatId);
                newUser.setFirstName(userFromUpdate.getFirstName());
                newUser.setLastName(userFromUpdate.getLastName());
                newUser.setUrl(new ArrayList<>());
                return newUser;
            });


            if (messageText.startsWith("http")) {
                /*List<Links> linkList = new ArrayList<>();
                Links link = new Links();
                link.setUrl(messageText);
                link.setUser(user);
*/

                Links link = new Links();
                link.setUrl(messageText);
                link.setUser(user);

                List<Links> linkList = user.getUrl();

                if (!linkList.contains(link)) {
                    linkList.add(link);
                    user.setUrl(linkList);
                    usersRepository.save(user); //doda nov url v List<urljev> userja
                    System.out.println("Dodan je bil nov url za: " + user.getFirstName() + " " + user.getLastName());

                }
                parser.startParser(messageText, user);

            } else if (messageText.startsWith("/deleUser")) {
                sendMessage(chatId, "Brišem userja");
                usersRepository.delete(user);

            } else if (messageText.startsWith("/stop")) {
                parser.disableScheduler();
                sendMessage(chatId, "Ustavljen");
            } else if (messageText.startsWith("/status")) {
                String status = parser.isSchedulerEnabled
                        ? "Parser je trenutno aktiven in preverja nove oglase."
                        : "Parser je trenutno ustavljen.";
                sendMessage(chatId, status);
            } else if (messageText.startsWith("/help")) {
                String helper = """
                        Ukazi, ki so na voljo:
                        - Pošlji URL (http...) za začetek parsanja.
                        - /deleUser – izbriše uporabnika.
                        - /stop – ustavi parser.
                        - /status – prikaže stanje parserja.
                        - /help – prikaže to pomoč.
                        """;
                sendMessage(chatId, helper);
            } else {
                responseText = "Neznan ukaz. Pošlji url za parsanje ali uporabi /help za seznam ukazov.";
                sendMessage(chatId, responseText);


            }


        }
    }

    public void sendMessage(Long chatId, String messageToSend) {
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

