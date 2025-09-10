package si.dteeam.parser;

import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import si.dteeam.entity.Link;
import si.dteeam.entity.Vehicle;
import si.dteeam.events.VehicleEvent;
import si.dteeam.repository.LinksRepository;
import si.dteeam.repository.UsersRepository;
import si.dteeam.repository.VehiclesRepository;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class AvtonetParser {
    private VehiclesRepository vehiclesRepository;
    private UsersRepository usersRepository;
    private LinksRepository linksRepository;
    private volatile boolean stopThread = false;


    @Autowired
    public void setVehiclesRepository(VehiclesRepository vehiclesRepository) {
        this.vehiclesRepository = vehiclesRepository;
    }

    @Autowired
    public void setUsersRepository(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Autowired
    public void setLinksRepository(LinksRepository linksRepository) {
        this.linksRepository = linksRepository;
    }

    @Autowired
    private ApplicationEventPublisher eventPublisher;


    @PostConstruct
    public void init() {

    }

    public void updateVehicle(Vehicle vehicle) {
        try {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            WebDriver driver = new ChromeDriver(options);
            Random rand = new Random();
            driver.get(vehicle.getUrl());

            WebElement titleElement = driver.findElement(
                    By.xpath("//div[@class='col-12 mt-3 pt-1']/h3")
            );
            WebElement parkElement = driver.findElement(
                    By.cssSelector("a[href*='ParkEUR']")
            );
            /*WebElement yearElement = driver.findElement(
                    By.xpath("//tr[th[contains(text(),'Letnik:')]]/td")
            );*/
            WebElement kmElement = driver.findElement(
                    By.xpath("//tr[th[contains(text(),'Prevoženi km:')]]/td")
            );
            WebElement powerElement = driver.findElement(
                    By.xpath("//tr[th[contains(text(),'Moč motorja:')]]/td")
            );
            WebElement dateOfLastChange = driver.findElement(By.cssSelector("div.col-12.col-lg-6.p-0.pl-1.text-center.text-lg-left"));
           /* WebElement linkElement = driver.findElement(
                    By.cssSelector("a[href*='znamka='][href*='model=']")
            );*/

            //String hrefLink = linkElement.getAttribute("href");

            Pattern brandPattern = Pattern.compile("znamka=([^&]+)");
            //Matcher brandMatcher = brandPattern.matcher(hrefLink);
            //String brand = brandMatcher.find() ? brandMatcher.group(1) : "";

            Pattern modelPattern = Pattern.compile("model=([^&]+)");
            /*Matcher modelMatcher = modelPattern.matcher(hrefLink);
            String model = modelMatcher.find() ? modelMatcher.group(1) : "";

            brand = URLDecoder.decode(brand, StandardCharsets.UTF_8);
            model = URLDecoder.decode(model, StandardCharsets.UTF_8);*/

            String title = titleElement.getText().trim();
            String href = parkElement.getAttribute("href");
            //String yearText = yearElement.getText().trim();
            String kmText = kmElement.getText().replaceAll("[^0-9]", "");
            String[] dateParts = dateOfLastChange.getText().split(": ");
            String dateTimePart = dateParts[1];

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d.M.yyyy HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(dateTimePart, formatter);

            Pattern pattern = Pattern.compile("ParkEUR=(\\d+)");
            Matcher matcherPrice = pattern.matcher(href);
            Matcher matcherPower = Pattern.compile("(\\d+)\\s*kW").matcher(powerElement.getText());

            int price = 0;
            if (matcherPrice.find()) {
                price = Integer.parseInt(matcherPrice.group(1));
            }
            //int year = Integer.parseInt(yearText);
            int kilometers = Integer.parseInt(kmText);
            int powerKW = 0;
            if (matcherPower.find()) {
                powerKW = Integer.parseInt(matcherPower.group(1));
            }

            Vehicle newVehicle = new Vehicle();
            newVehicle.setPrice(price);
            newVehicle.setTitle(title);
            newVehicle.setBrand(null);
            newVehicle.setModel(null);
            //newVehicle.setModelYear(String.valueOf(year));
            newVehicle.setModelYear("2000");
            newVehicle.setMileage(kilometers);
            newVehicle.setPowerKW(powerKW);
            newVehicle.setDateOfChange(dateTime);
            newVehicle.setUrl(vehicle.getUrl());
            newVehicle.setLink(vehicle.getLink());
            newVehicle.setId(vehicle.getId());
            newVehicle.setSubscribed(vehicle.isSubscribed());

            if (!newVehicle.equals(vehicle) && vehicle.isSubscribed()) {
                vehiclesRepository.save(newVehicle);
                eventPublisher.publishEvent(new VehicleEvent(this, vehicle.getLink().getUser().getChatID(),
                        "Sprememba pri oglasu: "
                ));
            } else {
                System.out.println("No change in ad: " + title);
            }

            /*System.out.println("Znamka: " + brand);
            System.out.println("Model: " + model);
            //System.out.println("Naslov: " + title);
            System.out.println("Cena: " + price);
            System.out.println("Letnik: " + year);
            System.out.println("Kilometri: " + kilometers);
            System.out.println("Moč motorja: " + powerKW + " kW");
*/
            vehicle.setDateOfChange(dateTime);

            driver.close();
            driver.quit();
            Thread.sleep(10000 + rand.nextInt(3000));
        } catch (Exception e){
            eventPublisher.publishEvent(new VehicleEvent(this, vehicle.getLink().getUser().getChatID(),
                    "Error updating vehicle: " + vehicle.getUrl() + "\n" + e.getMessage()));
        }
    }


    public void updateLink(Link link) {
        try {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            WebDriver driver = new ChromeDriver(options);
            Random rand = new Random();
            if (Objects.nonNull(link.getUrl()) && !link.getUrl().isEmpty()) {
                driver.get(link.getUrl());

                List<WebElement> posts = driver.findElements(By.className("GO-Results-Row"));
                for (WebElement post : posts) {
                    if (stopThread) {
                        driver.quit();
                        return;
                    }

                    WebElement table = post.findElement(By.cssSelector("table.table.table-striped.table-sm.table-borderless"));
                    WebElement titleElement = post.findElement(By.className("GO-Results-Naziv"));
                    String hrefUrl = post.findElement(By.cssSelector("a.stretched-link")).getAttribute("href");
                    List<WebElement> rows = table.findElements(By.tagName("tr"));

                    Link currentLink = linksRepository.findLinksById(link.getId());


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
                        price = price.replace(".", "");
                        String[] partsPower = power.split(" ");
                        String[] partsPrice = price.split(" ");

                        if (link.getVehicles().stream().filter(v -> v.getUrl().equals(hrefUrl)).findFirst().orElse(null) == null) {
                            Vehicle vehicle = new Vehicle();
                            vehicle.setLink(currentLink);
                            vehicle.setTitle(title);
                            vehicle.setModelYear(modelYear);
                            try {
                                vehicle.setPrice(Integer.parseInt(partsPrice[0]));
                                System.out.println("Price: " + vehicle.getPrice());
                            }
                            catch (NumberFormatException e) {
                                vehicle.setPrice(null);
                                System.out.println("Call for price");
                            }
                            try {
                                if (mileage.length() > 0) {
                                    partsMileage = mileage.split(" ");
                                    vehicle.setMileage(Integer.parseInt(partsMileage[0]));
                                } else{
                                    vehicle.setMileage(0);
                                }
                            } catch (NumberFormatException e) {
                                vehicle.setMileage(null);
                            }
                            try {
                                vehicle.setPowerKW(Integer.parseInt(partsPower[0]));
                            } catch (NumberFormatException e) {
                                vehicle.setPowerKW(null);
                            }
                            vehicle.setUrl(hrefUrl);
                            Thread.sleep(10000 + rand.nextInt(3000));
                            WebDriver driver2 = new ChromeDriver();
                            driver2.get(hrefUrl);
                            WebElement dateOfLastChange = driver2.findElement(By.cssSelector("div.col-12.col-lg-6.p-0.pl-1.text-center.text-lg-left"));

                            WebElement linkElement = driver.findElement(
                                    By.cssSelector("a[href*='znamka='][href*='model=']")
                            );
                            String href = linkElement.getAttribute("href");

                            Pattern brandPattern = Pattern.compile("znamka=([^&]+)");
                            Matcher brandMatcher = brandPattern.matcher(href);
                            String brand = brandMatcher.find() ? brandMatcher.group(1) : "";

                            Pattern modelPattern = Pattern.compile("model=([^&]+)");
                            Matcher modelMatcher = modelPattern.matcher(href);
                            String model = modelMatcher.find() ? modelMatcher.group(1) : "";

                            brand = URLDecoder.decode(brand, StandardCharsets.UTF_8);
                            model = URLDecoder.decode(model, StandardCharsets.UTF_8);



                            String[] dateParts = dateOfLastChange.getText().split(": ");
                            String dateTimePart = dateParts[1];
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d.M.yyyy HH:mm:ss");
                            LocalDateTime dateTime = LocalDateTime.parse(dateTimePart, formatter);
                            vehicle.setDateOfChange(dateTime);
                            vehicle.setBrand(brand);
                            vehicle.setModel(model);
                            driver2.quit();

                            vehiclesRepository.save(vehicle);
                            System.out.println(vehicle);

                            if(link.isSubscribed()){
                                eventPublisher.publishEvent(new VehicleEvent(this, link.getUser().getChatID(),
                                        "New ad for: " + title +  "\n" + vehicle.getUrl()
                                ));
                            }

                        } else {
                            System.out.println("Add already read: " + title);
                        }
                        Thread.sleep(10000 + rand.nextInt(3000));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                driver.close();
                driver.quit();
                Thread.sleep(20000 + rand.nextInt(3000));
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
