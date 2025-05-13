package org.example;


import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;



import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;


public class Avtonet {
    public static void main(String[] args) {
        try {

            ChromeOptions options = new ChromeOptions();

            options.addArguments("start-minimized");
            //options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
            options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/123.0.0.0");
            WebDriver driver = new ChromeDriver(options);


            Random rand = new Random();

            String baseUrl = "https://www.avto.net/";
            driver.get(baseUrl);
            Thread.sleep(10000 + rand.nextInt(3000));


            driver.get("https://www.avto.net/Ads/results.asp?znamka=Yamaha&model=mt07&modelID=&tip=&znamka2=&model2=&tip2=&znamka3=&model3=&tip3=&cenaMin=0&cenaMax=999999&letnikMin=0&letnikMax=2090&bencin=0&starost2=999&oblika=&ccmMin=0&ccmMax=99999&mocMin=&mocMax=&kmMin=0&kmMax=9999999&kwMin=0&kwMax=999&motortakt=0&motorvalji=0&lokacija=0&sirina=&dolzina=&dolzinaMIN=&dolzinaMAX=&nosilnostMIN=&nosilnostMAX=&sedezevMIN=&sedezevMAX=&lezisc=&presek=&premer=&col=&vijakov=&EToznaka=&vozilo=&airbag=&barva=&barvaint=&doseg=&BkType=&BkOkvir=&BkOkvirType=&Bk4=&EQ1=1000000000&EQ2=1000000000&EQ3=1000000000&EQ4=100000000&EQ5=1000000000&EQ6=1000000000&EQ7=1110100120&EQ8=101000000&EQ9=100000002&EQ10=1000000000&KAT=1060000000&PIA=&PIAzero=&PIAOut=&PSLO=&akcija=&paketgarancije=&broker=&prikazkategorije=&kategorija=61000&ONLvid=&ONLnak=&zaloga=10&arhiv=&presort=&tipsort=&stran=");
            //driver.get("https://www.avto.net/Ads/results.asp?znamka=Ducati&model=696&modelID=&tip=&znamka2=&model2=&tip2=&znamka3=&model3=&tip3=&cenaMin=0&cenaMax=999999&letnikMin=0&letnikMax=2090&bencin=0&starost2=999&oblika=&ccmMin=0&ccmMax=99999&mocMin=&mocMax=&kmMin=0&kmMax=9999999&kwMin=0&kwMax=999&motortakt=0&motorvalji=0&lokacija=0&sirina=&dolzina=&dolzinaMIN=&dolzinaMAX=&nosilnostMIN=&nosilnostMAX=&sedezevMIN=&sedezevMAX=&lezisc=&presek=&premer=&col=&vijakov=&EToznaka=&vozilo=&airbag=&barva=&barvaint=&doseg=&BkType=&BkOkvir=&BkOkvirType=&Bk4=&EQ1=1100000000&EQ2=1000000000&EQ3=1000000000&EQ4=100000000&EQ5=1000000000&EQ6=1000000000&EQ7=1110100120&EQ8=101000000&EQ9=100000002&EQ10=1000000000&KAT=1060000000&PIA=&PIAzero=&PIAOut=&PSLO=&akcija=&paketgarancije=&broker=&prikazkategorije=&kategorija=61000&ONLvid=&ONLnak=&zaloga=10&arhiv=&presort=&tipsort=&stran=");
            Thread.sleep(10000 + rand.nextInt(3000));


            List<Post> posts = new ArrayList<>();
            List<WebElement>postElements = driver.findElements(By.cssSelector("div.GO-Results-Row"));
            WebElement ad = driver.findElement(By.className("GO-Results-Row"));



            WebElement titleElement = driver.findElement(By.className("GO-Results-Naziv"));
            String title = titleElement.findElement(By.tagName("span")).getText();

            WebElement table = ad.findElement(By.cssSelector("table.table.table-striped.table-sm.table-borderless"));
            List<WebElement> rows = table.findElements(By.tagName("tr"));

            String power = "";
            String modelYear = "";
            String mileage = "";

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
                    }
                }
            }
            System.out.println("Motor: " + title);
            System.out.println("Letnik: " + modelYear);
            System.out.println("Prevoženi: " + mileage);
            System.out.println("Moč: " + power);


            Thread.sleep(10000 + rand.nextInt(3000));

           driver.quit();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
