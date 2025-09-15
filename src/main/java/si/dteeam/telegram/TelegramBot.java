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
import si.dteeam.entity.Subscriber;
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
    private TelegramBotService telegramBotService;

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
        System.out.println("Received update: " + update.toString());
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String responseText = "";

            Subscriber subscriber = telegramBotService.findOrCreateSubscriber(chatId, update.getMessage().getFrom());

            if (messageText.startsWith("http") && messageText.contains("Ads/results")) {
                telegramBotService.processResults(messageText, chatId, subscriber);
                sendMessage(chatId, "Subscribed to a new url");
                System.out.println("New url added for: " + subscriber.getFirstName());
            }
            else if (messageText.startsWith("http") && messageText.contains("Ads/details")) {
                telegramBotService.processDetails(messageText, chatId, subscriber);
                sendMessage(chatId, "Subscribed to vehicle details!");
                System.out.println("New vehicle added for: " + subscriber.getFirstName());
            }
            else if (messageText.startsWith("/unsub") && messageText.contains("Ads/results")) {
                if (telegramBotService.unsubResults(messageText, chatId, subscriber)) {
                    sendMessage(chatId, "Unsubscribed from url!");
                } else {
                    sendMessage(chatId, "You are not subscribed to this url.");
                }
            }
            else if (messageText.startsWith("/unsub") && messageText.contains("Ads/details")) {
                if (telegramBotService.unsubDetails(messageText, chatId, subscriber)) {;
                    sendMessage(chatId, "Unsubscribed from vehicle details!");
                } else {
                    sendMessage(chatId, "You are not subscribed to this vehicle.");
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