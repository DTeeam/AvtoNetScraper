package si.dteeam.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.dteeam.entity.Link;
import si.dteeam.entity.Vehicle;
import si.dteeam.repository.LinksRepository;
import si.dteeam.repository.VehiclesRepository;

import java.util.List;

@Service
public class AvtonetScheduler {

    @Autowired
    private LinksRepository linksRepository;

    @Autowired
    private VehiclesRepository vehiclesRepository;

    @Autowired
    private AvtonetParser avtonetParser;

    @EventListener(ApplicationReadyEvent.class)
    public void runOnceOnStartup() {
        updateLinks();
        updateVehicles();
    }

    @Transactional
    @Scheduled(cron = "0 * * * * *")
    public void updateLinks() {
        List<Link> links = linksRepository.findAll();
        for(Link link : links){
            avtonetParser.updateLink(link);
        }
    }

    @Transactional
    @Scheduled(cron = "0 * * * * *")
    public void updateVehicles() {
        List<Vehicle> vehicles = vehiclesRepository.findAll();
        for(Vehicle vehicle : vehicles){
            if(vehicle.isSubscribed()){
                avtonetParser.updateVehicle(vehicle);
            }
        }
    }
}
