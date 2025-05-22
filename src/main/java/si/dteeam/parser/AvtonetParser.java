package si.dteeam.parser;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import si.dteeam.entity.Vehicle;
import si.dteeam.repository.VehicleRepository;

import org.jsoup.nodes.Document;



@Component
public class AvtonetParser {

    private VehicleRepository vehicleRepository;

    @Autowired
    public void setVehicleRepository(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runOnceOnStartup() {
        runEveryMinute();
    }

    @Scheduled(cron = "0 * * * * *")
    public void runEveryMinute() {
        parse();
    }

    public void parse() {
     try {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        WebDriver driver = new ChromeDriver(options);
        Random rand = new Random();

        driver.get("https://www.avto.net/Ads/results.asp?znamka=Yamaha&model=mt07&modelID=&tip=&znamka2=&model2=&tip2=&znamka3=&model3=&tip3=&cenaMin=0&cenaMax=999999&letnikMin=0&letnikMax=2090&bencin=0&starost2=999&oblika=&ccmMin=0&ccmMax=99999&mocMin=&mocMax=&kmMin=0&kmMax=9999999&kwMin=0&kwMax=999&motortakt=0&motorvalji=0&lokacija=0&sirina=&dolzina=&dolzinaMIN=&dolzinaMAX=&nosilnostMIN=&nosilnostMAX=&sedezevMIN=&sedezevMAX=&lezisc=&presek=&premer=&col=&vijakov=&EToznaka=&vozilo=&airbag=&barva=&barvaint=&doseg=&BkType=&BkOkvir=&BkOkvirType=&Bk4=&EQ1=1000000000&EQ2=1000000000&EQ3=1000000000&EQ4=100000000&EQ5=1000000000&EQ6=1000000000&EQ7=1110100120&EQ8=101000000&EQ9=100000002&EQ10=1000000000&KAT=1060000000&PIA=&PIAzero=&PIAOut=&PSLO=&akcija=&paketgarancije=&broker=&prikazkategorije=&kategorija=61000&ONLvid=&ONLnak=&zaloga=10&arhiv=&presort=&tipsort=&stran=");

        WebElement ad = driver.findElement(By.className("GO-Results-Row"));
        WebElement titleElement = driver.findElement(By.className("GO-Results-Naziv"));
        WebElement table = ad.findElement(By.cssSelector("table.table.table-striped.table-sm.table-borderless"));
        List<WebElement> rows = table.findElements(By.tagName("tr"));

        List<Vehicle> vehicles = new ArrayList<>();

        String title = titleElement.findElement(By.tagName("span")).getText();
        String power = "";
        String modelYear = "";
        String mileage = "";
        String price = "";

        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.size() >= 2) {
                String label = cells.get(0).getText().trim();
                String value = cells.get(1).getText().trim();

                if (label.contains("Motor")) {
                    power = value;
                } else if (label.contains("1.registracija")) {
                    modelYear = value;
                }else if (label.contains("Prevoženih")) {
                    mileage = value;
                }else if (label.contains("Price")) {
                    price = value;
                }
            }
        }
        System.out.println("Motor: " + title);
        System.out.println("Letnik: " + modelYear);
        System.out.println("Prevoženi: " + mileage);
        System.out.println("Moč: " + power);
        System.out.println("Cena: " + price);

        String[] partsPower = power.split(" ");
        String[] partsMileage = mileage.split(" ");

        Vehicle vehicle = new Vehicle();
        vehicle.setTitle(title);
        vehicle.setModelYear(modelYear);
        vehicle.setMileage(Integer.parseInt(partsMileage[0]));
        vehicle.setPowerKW(Integer.parseInt(partsPower[0]));

         vehicleRepository.save(vehicle);

        Thread.sleep(10000 + rand.nextInt(3000));

        driver.quit();

    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
    }
}
