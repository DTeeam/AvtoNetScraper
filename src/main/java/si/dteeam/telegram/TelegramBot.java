package si.dteeam.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import si.dteeam.entity.Link;
import si.dteeam.entity.Users;
import si.dteeam.entity.Vehicle;
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
                return usersRepository.save(newUser);
            });

            if (messageText.startsWith("http") && messageText.contains("Ads/results")) {
                Link link = new Link();
                link.setUrl(messageText);
                link.setUser(user);
                link.setSubscribed(true);

                List<Link> linkList = user.getUrl();
                if (!linkList.contains(link)) {
                    linkList.add(link);
                    user.setUrl(linkList);
                    usersRepository.save(user);
                }
                //parser.updateLink(link);
                //linksRepository.save(link);
                sendMessage(chatId, "Subscribed to a new url");
                System.out.println("New url added for: " + user.getFirstName());
            }
            else if (messageText.startsWith("http") && messageText.contains("Ads/details")) {
                Vehicle vehicle = vehiclesRepository.findByUrl(messageText).orElseGet(() -> {
                    Vehicle newVehicle = new Vehicle();
                    newVehicle.setUrl(messageText);
                    newVehicle.setSubscribed(true);
                    return newVehicle;
                });

                Link linkForVehicles = linksRepository
                        .findByUserIdAndUrlIsNull(user.getId())
                        .orElseGet(() -> {
                            Link l = new Link();
                            l.setUser(user);
                            l.setUrl(null);
                            l.setSubscribed(true);
                            return linksRepository.save(l);
                        });

                vehicle.setLink(linkForVehicles);
                vehiclesRepository.save(vehicle);

                linkForVehicles.getVehicles().add(vehicle);
                linksRepository.save(linkForVehicles);

                if (!user.getUrl().contains(linkForVehicles)) {
                    user.getUrl().add(linkForVehicles);
                    usersRepository.save(user);
                }


                sendMessage(chatId, "Subscribed to vehicle details!");
                System.out.println("New vehicle added for: " + user.getFirstName());
            }
            else if (messageText.startsWith("/unsub") && messageText.contains("Ads/results")) {
                String messageTextTrim = messageText;
                int index = messageTextTrim.indexOf("http");
                //indexOf lahko vrne -1
                if (index != -1) {
                    messageTextTrim = messageTextTrim.substring(index).trim();
                    Link newLink = linksRepository.findLinkByUserIdAndUrl(user.getId(), messageTextTrim);
                    if (newLink == null || !newLink.isSubscribed()) {
                        sendMessage(chatId, "You are not subscribed to this link.");
                    }
                    else {
                        linksRepository.setSubscribeToLink(user.getId(), messageTextTrim, false);
                        sendMessage(chatId, "Unsubscribed from link!");
                    }

                }
            }
            else if (messageText.startsWith("/unsub") && messageText.contains("Ads/details")) {
                String messageTextTrim = messageText;
                int index = messageTextTrim.indexOf("http");
                //indexOf lahko vrne -1
                if (index != -1) {
                    messageTextTrim = messageTextTrim.substring(index).trim();

                    Vehicle newVehicle = vehiclesRepository.findVehicleByUrl(messageTextTrim);
                    if (newVehicle == null || !newVehicle.isSubscribed()) {
                        sendMessage(chatId, "You are not subscribed to this vehicle.");
                    }
                    else {
                        vehiclesRepository.setSubscribeToVehicle(user.getId(), messageTextTrim, false);
                        sendMessage(chatId, "Unsubscribed from vehicle!");
                    }

                }
            }
             else if (messageText.startsWith("/help")) {
                String helper = """
                        Aivailable commands:
                        - Send URL (http...)  for a search query (Ads/result) or for a specific ad (Ads/details).
                        - /unsub <url> – unsubscribe from a specific search query or ad.
                        - /help – shows this message.
                        """;
                sendMessage(chatId, helper);
            } else {
                responseText = "Unknown command. Type /help for assistance.";
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

    private String buildLink(Vehicle vehicle) {
        String baseUrl = "https://www.avto.net/Ads/results.asp?znamka=" + vehicle.getBrand() + "%20Romeo&model=" + vehicle.getModel() + "&modelID=&tip=&znamka2=&model2=&tip2=&znamka3=&model3=&tip3=&cenaMin=0&cenaMax=999999&letnikMin=0&letnikMax=2090&bencin=0&starost2=999&oblika=&ccmMin=0&ccmMax=99999&mocMin=&mocMax=&kmMin=" + vehicle.getMileage() + "&kmMax=" + vehicle.getMileage() + "&kwMin=0&kwMax=999&motortakt=&motorvalji=&lokacija=0&sirina=&dolzina=&dolzinaMIN=&dolzinaMAX=&nosilnostMIN=&nosilnostMAX=&sedezevMIN=&sedezevMAX=&lezisc=&presek=&premer=&col=&vijakov=&EToznaka=&vozilo=&airbag=&barva=&barvaint=&doseg=&BkType=&BkOkvir=&BkOkvirType=&Bk4=&EQ1=1000000000&EQ2=1000000000&EQ3=1000000000&EQ4=100000000&EQ5=1000000000&EQ6=1000000000&EQ7=1110100120&EQ8=101000000&EQ9=100000002&EQ10=100000000&KAT=1012000000&PIA=&PIAzero=&PIAOut=&PSLO=&akcija=&paketgarancije=0&broker=&prikazkategorije=&kategorija=61000&ONLvid=&ONLnak=&zaloga=10&arhiv=&presort=&tipsort=&stran="; // Replace with your actual base URL
        return baseUrl;
    }

}