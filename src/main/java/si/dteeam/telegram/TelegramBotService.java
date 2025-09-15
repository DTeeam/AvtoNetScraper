package si.dteeam.telegram;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.User;
import si.dteeam.entity.Link;
import si.dteeam.entity.Subscriber;
import si.dteeam.entity.Vehicle;
import si.dteeam.repository.LinksRepository;
import si.dteeam.repository.UsersRepository;
import si.dteeam.repository.VehiclesRepository;

import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
public class TelegramBotService {

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private VehiclesRepository vehiclesRepository;
    @Autowired
    private LinksRepository linksRepository;

    public Subscriber findOrCreateSubscriber(Long chatId, User telegramUser) {
        return usersRepository.findByChatID(chatId).orElseGet(() -> {
            Subscriber newSubscriber = new Subscriber();
            newSubscriber.setChatID(chatId);
            newSubscriber.setFirstName(telegramUser.getFirstName());
            newSubscriber.setLastName(telegramUser.getLastName());
            newSubscriber.setUrl(new ArrayList<>());
            return usersRepository.save(newSubscriber);
        });
    }

    public void processDetails(String messageText, Long chatId, Subscriber subscriber) {
        Vehicle vehicle = vehiclesRepository.findByUrl(messageText).orElseGet(() -> {
            Vehicle newVehicle = new Vehicle();
            newVehicle.setUrl(messageText);
            newVehicle.setSubscribed(true);
            return newVehicle;
        });

        Link linkForVehicles = linksRepository
            .findBySubscriberIdAndUrlIsNull(subscriber.getId()).orElseGet(() -> {
                Link l = new Link();
                l.setSubscriber(subscriber);
                l.setUrl(null);
                l.setSubscribed(true);
                return linksRepository.save(l);
            });

        vehicle.setLink(linkForVehicles);
        linkForVehicles.setSubscriber(subscriber);
        linksRepository.save(linkForVehicles);
        vehiclesRepository.save(vehicle);

    }

    public void processResults(String messageText, Long chatId, Subscriber subscriber) {
        Link link = new Link();
        link.setUrl(messageText);
        link.setSubscriber(subscriber);
        link.setSubscribed(true);

        List<Link> linkList = subscriber.getUrl();
        if (!linkList.contains(link)) {
            linkList.add(link);
            subscriber.setUrl(linkList);
            usersRepository.save(subscriber);
        }
    }

    public boolean unsubResults(String messageText, Long chatId, Subscriber subscriber) {
        String messageTextTrim = messageText;
        int index = messageTextTrim.indexOf("http");
        if (index != -1) {
            messageTextTrim = messageTextTrim.substring(index).trim();
            Link newLink = linksRepository.findLinkBySubscriberIdAndUrl(subscriber.getId(), messageTextTrim);
            if (newLink == null || !newLink.isSubscribed()) {
                return false;

            }
            else {
                linksRepository.setSubscribeToLink(subscriber.getId(), messageTextTrim, false);
                return true;
            }

        }
        return false;
    }

    public boolean unsubDetails(String messageText, Long chatId, Subscriber subscriber) {
        String messageTextTrim = messageText;
        int index = messageTextTrim.indexOf("http");
        if (index != -1) {
            messageTextTrim = messageTextTrim.substring(index).trim();

            Vehicle newVehicle = vehiclesRepository.findVehicleByUrl(messageTextTrim);
            if (newVehicle == null || !newVehicle.isSubscribed()) {
                return false;
            }
            else {
                vehiclesRepository.setSubscribeToVehicle(subscriber.getId(), messageTextTrim, false);
                return true;
            }

        }
        return false;
    }
}
