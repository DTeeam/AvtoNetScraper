package si.dteeam.parser;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;
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
import si.dteeam.entity.Vehicles;
import si.dteeam.repository.UsersRepository;
import si.dteeam.repository.VehiclesRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


@Component
public class AvtonetParser {
    private VehiclesRepository vehiclesRepository;
    private UsersRepository usersRepository;
    private Set<String> savedUrls;
    private Set<String> savedUsers;
    private String rUrl;


    @Autowired
    public void setVehiclesRepository(VehiclesRepository vehiclesRepository) {
        this.vehiclesRepository = vehiclesRepository;
    }

    @PostConstruct
    public void init() {
        savedUrls = new HashSet<>(vehiclesRepository.findAllUrls());
        savedUsers = new HashSet<>(usersRepository.findAllUsers());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runOnceOnStartup() {
        runEveryMinute();
    }

    @Scheduled(cron = "0 * * * * *")
    public void runEveryMinute() {
        //parse(rUrl);
        parse();
    }

    public void parse() {
        try {
            //rUrl = baseUrl;
            savedUrls = new HashSet<>(vehiclesRepository.findAllUrls());
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            WebDriver driver = new ChromeDriver(options);
            Random rand = new Random();

            driver.get("https://www.avto.net/Ads/results.asp?znamka=Yamaha&model=mt07&modelID=&tip=&znamka2=&model2=&tip2=&znamka3=&model3=&tip3=&cenaMin=0&cenaMax=999999&letnikMin=0&letnikMax=2090&bencin=0&starost2=999&oblika=&ccmMin=0&ccmMax=99999&mocMin=&mocMax=&kmMin=0&kmMax=9999999&kwMin=0&kwMax=999&motortakt=0&motorvalji=0&lokacija=0&sirina=&dolzina=&dolzinaMIN=&dolzinaMAX=&nosilnostMIN=&nosilnostMAX=&sedezevMIN=&sedezevMAX=&lezisc=&presek=&premer=&col=&vijakov=&EToznaka=&vozilo=&airbag=&barva=&barvaint=&doseg=&BkType=&BkOkvir=&BkOkvirType=&Bk4=&EQ1=1000000000&EQ2=1000000000&EQ3=1000000000&EQ4=100000000&EQ5=1000000000&EQ6=1000000000&EQ7=1110100120&EQ8=101000000&EQ9=100000002&EQ10=1000000000&KAT=1060000000&PIA=&PIAzero=&PIAOut=&PSLO=&akcija=&paketgarancije=&broker=&prikazkategorije=&kategorija=61000&ONLvid=&ONLnak=&zaloga=10&arhiv=&presort=&tipsort=&stran=");
            //driver.get(baseUrl);
            List<WebElement> posts = driver.findElements(By.className("GO-Results-Row"));

            for (WebElement post : posts) {
                WebElement table = post.findElement(By.cssSelector("table.table.table-striped.table-sm.table-borderless"));
                WebElement titleElement = post.findElement(By.className("GO-Results-Naziv"));
                String hrefUrl = post.findElement(By.cssSelector("a.stretched-link")).getAttribute("href");
                List<WebElement> rows = table.findElements(By.tagName("tr"));

                String title = titleElement.findElement(By.tagName("span")).getText();
                String power = "";
                String modelYear = "";
                String mileage = "";
                String price = "";

                List<WebElement> priceElements = post.findElements(By.cssSelector(".GO-Results-Price-TXT-Regular"));
                for (WebElement pe : priceElements) {
                    String txt = pe.getText().trim();
                    if (!txt.isEmpty()) {
                        price = txt;
                        break;
                    }
                }

                for (WebElement row : rows) {
                    List<WebElement> cells = row.findElements(By.tagName("td"));
                    if (cells.size() >= 2) {
                        String label = cells.get(0).getText().trim();
                        String value = cells.get(1).getText().trim();

                        if (label.contains("Motor")) {
                            power = value;
                        } else if (label.contains("1.registracija")) {
                            modelYear = value;
                        } else if (label.contains("Prevoženih")) {
                            mileage = value;

                        }
                    }
                }


                String[] partsMileage;
                try {
                    Vehicles vehicle = new Vehicles();

                    price = price.replace(".", "");
                    String[] partsPower = power.split(" ");
                    String[] partsPrice = price.split(" ");


                    if (!savedUrls.contains(hrefUrl)) {
                        savedUrls.add(hrefUrl);

                        vehicle.setTitle(title);
                        vehicle.setPrice(Integer.parseInt(partsPrice[0]));
                        vehicle.setModelYear(modelYear);

                        if (mileage.length() > 0) {
                            partsMileage = mileage.split(" ");
                            vehicle.setMileage(Integer.parseInt(partsMileage[0]));
                        } else
                            vehicle.setMileage(0);
                        vehicle.setPowerKW(Integer.parseInt(partsPower[0]));
                        vehicle.setUrl(hrefUrl);


                        Thread.sleep(10000 + rand.nextInt(3000));
                        WebDriver driver2 = new ChromeDriver();
                        driver2.get(hrefUrl);
                        //System.out.println(driver2.getPageSource());
                        WebElement dateOfLastChange = driver2.findElement(By.cssSelector("div.col-12.col-lg-6.p-0.pl-1.text-center.text-lg-left"));

                        String[] parts = dateOfLastChange.getText().split(": ");
                        String dateTimePart = parts[1];
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d.M.yyyy HH:mm:ss");
                        LocalDateTime dateTime = LocalDateTime.parse(dateTimePart, formatter);

                        driver2.quit();

                        vehicle.setDateOfChange(dateTime);
                        vehiclesRepository.save(vehicle);
                        System.out.println(vehicle);
               /*System.out.println("Motor: " + title);
               System.out.println("Letnik: " + modelYear);
               System.out.println("Prevoženi: " + mileage);
               System.out.println("Moč: " + power);
               System.out.println("Cena: " + price);
               System.out.println("URL: " + hrefUrl);*/
                    } else
                        System.out.println("OGLAS ŽE PREBRAN");

                    Thread.sleep(10000 + rand.nextInt(3000));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            driver.quit();
            driver.close();
            Thread.sleep(20000 + rand.nextInt(3000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
