package si.dteeam.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import si.dteeam.entity.Link;
import si.dteeam.entity.Users;
import si.dteeam.events.VehicleEvent;
import si.dteeam.parser.AvtonetParser;
import si.dteeam.repository.LinksRepository;
import si.dteeam.repository.UsersRepository;
import si.dteeam.repository.VehiclesRepository;

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


    @Autowired
    private VehiclesRepository vehiclesRepository;
    @Autowired
    private LinksRepository linksRepository;

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
            org.telegram.telegrambots.meta.api.objects.User userFromUpdate = update.getMessage().getFrom();
            String responseText = "";

            Users user = usersRepository.findByChatID(chatId).orElseGet(() -> {
                Users newUser = new Users();
                newUser.setChatID(chatId);
                newUser.setFirstName(userFromUpdate.getFirstName());
                newUser.setLastName(userFromUpdate.getLastName());
                newUser.setUrl(new ArrayList<>());
                return newUser;
            });

            if (messageText.startsWith("http") && messageText.contains("Ads/results")) {
                Link link = new Link();
                link.setUrl(messageText);
                link.setUser(user);

                List<Link> linkList = user.getUrl();
                if (!linkList.contains(link)) {
                    linkList.add(link);
                    user.setUrl(linkList);
                    usersRepository.save(user);
                }
                //parser.updateLink(link);
                link.setSubscribed(true);
                linksRepository.save(link);
                sendMessage(chatId, "Subscribed to a new url");
                System.out.println("New url added for: " + user.getFirstName());
            }
            else if (messageText.contains("Ads/details")) {
                vehiclesRepository.setSubscribeToVehicle(user.getId(), messageText, true);
                sendMessage(chatId, "Subscribed to details!");
            }
            else if (messageText.startsWith("unsub") && messageText.contains("Ads/results")) {
                linksRepository.setSubscribeToLink(user.getId(), messageText, false);
                sendMessage(chatId, "Unsubscribed from details!");
            }
            else if (messageText.startsWith("unsub") && messageText.contains("Ads/details")) {
                vehiclesRepository.setSubscribeToVehicle(user.getId(), messageText, false);
                sendMessage(chatId, "Unsubscribed from details!");
            }
            else if (messageText.startsWith("/stop")) {
                sendMessage(chatId, "Parsing stopped");

            }  else if (messageText.startsWith("/help")) {
                String helper = """
                        Ukazi, ki so na voljo:
                        - Pošlji URL (http...) za začetek parsanja.
                        - /stop – ustavi parser.
                        - /help – prikaže to sporočilo.
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