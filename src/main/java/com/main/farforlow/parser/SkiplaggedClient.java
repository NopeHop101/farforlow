package com.main.farforlow.parser;

import com.main.farforlow.entity.Result;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SkiplaggedClient {

    private Logger log = LoggerFactory.getLogger(SkiplaggedClient.class);

    private List<String> proxies = new ArrayList<>(Arrays.asList(
            "51.222.21.93:32768"
    ));

    private String chromeDriverPath = "YOUR_PATH";

    public Result getTripOptionBestPrice(String departureAirport,
                                         String destinationAirport,
                                         Date departureDate,
                                         Date returnDate) {
        if (departureAirport.equals(destinationAirport)) {
            return null;
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String request = String.format("Request {departure airport: %s,  destination airport: %s, departure date: %s, return date: %s}",
                departureAirport, destinationAirport, dateFormat.format(departureDate), dateFormat.format(returnDate));
        Result res = new Result();
        int randomProxy = ThreadLocalRandom.current().nextInt(0, proxies.size());

        System.setProperty("webdriver.chrome.driver", chromeDriverPath);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--ignore-certificate-errors", "--silent", "--proxy-server=" + proxies.get(randomProxy));
        WebDriver driver = new ChromeDriver(options);
        try {
            String requestLink;
            requestLink = String.format("https://skiplagged.com/flights/%s/%s/%s/%s",
                    departureAirport, destinationAirport, dateFormat.format(departureDate), dateFormat.format(returnDate));

            // Get the main page
            driver.get(requestLink);

            new WebDriverWait(driver, 20).until(d -> d.findElement(By.cssSelector("div.header-left")));

            if (!driver.findElement(By.cssSelector("div.header-left")).isDisplayed()) {
                driver.close();
                return null;
            }

            // 3 seconds waiting options from aggregator
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                log.error(request + ": skiplagged didn't load " + e.getMessage());
            }

            // Exit is no options for this dates/ airports
            String noOptions;
            noOptions = driver.findElement(By.xpath("//*[@id='flights-container']")).getText();
            if (noOptions.contains("No flights found.")) {
                log.warn(request + ": " + randomProxy + ":" + proxies.get(randomProxy) + ": No flights found.");
                driver.close();
                return null;
            }

            // 20 seconds waiting full options from aggregator
            try {
                Thread.sleep(20000);
            } catch (Exception e) {
            }

            // Exit is no options for this dates/ airports
            noOptions = driver.findElement(By.xpath("//*[@id='flights-container']")).getText();
            if (noOptions.contains("No flights found.")) {
                log.info(request + ": No flights found.");
                driver.close();
                return null;
            }

            //Price in GBP
            String bestPrice;
            bestPrice = driver.findElement(By.cssSelector("div.span2.trip-cost.text-success")).getText();

            res.setPriceGbp(priceCoversion(bestPrice));
            res.setLink(requestLink);
            res.setOfferDate(new Date());

            log.info(request + ": OK " + res.getPriceGbp());
            driver.close();

            return res;
        } catch (Exception e) {
            log.error(request + ": FAILED " + e.getMessage());
            driver.close();
            return null;
        }
    }

    private Integer priceCoversion(String price) {
        return Integer.parseInt(price.trim().split("\n")[0].substring(1));
    }

    public static void main(String[] args) {
        SkiplaggedClient client = new SkiplaggedClient();
        Result result = new Result();
        Calendar c = Calendar.getInstance();
        c.set(2021, 10, 11);
        Date deptDate = c.getTime();
        c.set(2021, 10, 21);
        Date retDate = c.getTime();
        result = client.getTripOptionBestPrice("GVA", "LED", deptDate, retDate);
        System.out.println(result);
    }
}
