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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SkiplaggedClient {

    private Logger log = LoggerFactory.getLogger(SkiplaggedClient.class);

    private List<String> proxies = new ArrayList<>(Arrays.asList(
            System.getenv("PROXIES").split(",")
    ));

    private String chromeDriverPath="/usr/local/bin/chromedriver";

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

        for (int i = 3; i > 0; i--) {
            if (i < 3) {
                log.error(request + ": NEW ROUND DUE TO FAILURE");
            }

            int randomProxy = ThreadLocalRandom.current().nextInt(0, proxies.size());

            System.setProperty("webdriver.chrome.driver", chromeDriverPath);
            ChromeOptions options = new ChromeOptions();
            options.addArguments(
                    "--verbose",
                    "--headless",
                    "--ignore-certificate-errors",
                    "--disable-web-security",
                    "--silent",
                    "--proxy-server=" + proxies.get(randomProxy),
                    "--allow-running-insecure-content",
                    "--allow-insecure-localhost",
                    "--no-sandbox",
                    "--disable-gpu",
                    "--window-size=1920,1200",
                    "--disable-dev-shm-usage",
                    "--disable-impl-side-painting",
                    "--disable-gpu-sandbox",
                    "--disable-accelerated-2d-canvas",
                    "--disable-accelerated-jpeg-decoding",
                    "--test-type=ui");
            WebDriver driver = new ChromeDriver(options);
            try {
                String requestLink;
                // Url for specific flight option search https://skiplagged.com/flights/GVA/LED/2021-11-11/2021-11-11
                requestLink = String.format("https://skiplagged.com/flights/%s/%s/%s/%s",
                        departureAirport, destinationAirport, dateFormat.format(departureDate), dateFormat.format(returnDate));

//                System.out.println("start");

                // Get the main page
                driver.get(requestLink);

                new WebDriverWait(driver, 20).until(d -> d.findElement(By.cssSelector("div.header-left")));

                if (!driver.findElement(By.cssSelector("div.header-left")).isDisplayed()) {
                    driver.quit();
                    return res;
                }

//                System.out.println("site");

                // 3 seconds waiting options from aggregator
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    log.error(request + ": " + randomProxy + ":" + proxies.get(randomProxy) + ": skiplagged didn't load " + e.getMessage());
                }

                // Exit is no options for this dates/ airports
                String noOptions;
                noOptions = driver.findElement(By.xpath("//*[@id='flights-container']")).getText();
                if (noOptions.contains("No flights found.")) {
                    log.warn(request + ": " + randomProxy + ":" + proxies.get(randomProxy) + ": No flights found.");
                    driver.quit();
                    return res;
                }

                // 20 seconds waiting full options from aggregator
                try {
                    Thread.sleep(20000);
                } catch (Exception e) {
                }

                // Exit is no options for this dates/ airports
                noOptions = driver.findElement(By.xpath("//*[@id='flights-container']")).getText();
                if (noOptions.contains("No flights found.")) {
                    log.info(request + ": " + randomProxy + ":" + proxies.get(randomProxy) + ": No flights found.");
                    driver.quit();
                    return res;
                }

                String bestPrice;
                bestPrice = driver.findElement(By.cssSelector("div.span2.trip-cost > p")).getText();
//                System.out.println(bestPrice);
                priceCoversion(bestPrice, res);
                res.setLink(requestLink);
                res.setOfferDate(new Date());

                if (i < 3) {
                    res.setSecondAttemptSuccessCount(res.getSecondAttemptSuccessCount() + 1);
                }

                log.info(request + ": " + randomProxy + ":" + proxies.get(randomProxy) + ": OK " + res.getPrice() + " " + bestPrice);
                driver.quit();

                return res;
            } catch (Exception e) {
                res.setFailuresCount(res.getFailuresCount() + 1);
                if (res.getFailedProxies() == null) {
                    Set<String> failedProxies = new HashSet<>();
                    failedProxies.add(proxies.get(randomProxy));
                    res.setFailedProxies(failedProxies);
                } else {
                    res.getFailedProxies().add(proxies.get(randomProxy));
                }
                log.error(request + ": " + randomProxy + ":" + proxies.get(randomProxy) + ": FAILED with " + e.getMessage());
                driver.quit();
            }
        }
        return res;
    }

    private void priceCoversion(String price, Result res) {
        res.setCurrency("USD");
        res.setPrice(Integer.parseInt(price.trim().split("\n")[0].substring(1).replaceAll(",", "")));
    }

    public static void main(String[] args) {
        SkiplaggedClient client = new SkiplaggedClient();
        Result result = new Result();
        Calendar c = Calendar.getInstance();
        c.set(2021, 11, 11);
        Date deptDate = c.getTime();
        c.set(2021, 11, 21);
        Date retDate = c.getTime();
        result = client.getTripOptionBestPrice("GVA", "LED", deptDate, retDate);
        System.out.println(result);
    }
}
