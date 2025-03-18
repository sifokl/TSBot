package com.automation.talkspiritbot.service;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;

@Service
public class TalkSpiritScrollService {

    private static final Logger logger = LoggerFactory.getLogger(TalkSpiritScrollService.class);
    private final WebDriverService webDriverService;
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    public TalkSpiritScrollService(WebDriverService webDriverService) {
        this.webDriverService = webDriverService;
    }

    private WebDriver getWebDriver() {
        return webDriverService.getDriver();
    }

    /**
     * Fait défiler la page jusqu'à atteindre la date limite.
     *
     * @param targetDateStr La date limite sous format "dd/MM/yyyy"
     */

    public void scrollUntilDate(String targetDateStr) {
        WebDriver driver = getWebDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        Actions actions = new Actions(driver);

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd"); // Format du paramètre
        SimpleDateFormat displayedFormat = new SimpleDateFormat("dd/MM/yyyy"); // Format affiché dans le tooltip

        Date targetDate;
        try {
            targetDate = inputFormat.parse(targetDateStr);
        } catch (ParseException e) {
            logger.error("Invalid date format: {}. Expected format: yyyyMMdd", targetDateStr, e);
            return;
        }

        logger.info("Scrolling until posts older than {}", displayedFormat.format(targetDate));

        boolean reachedTargetDate = false;
        boolean noMoreScroll = false;
        int attempts = 0;
        int lastPostCount = 0; // Nombre d'éléments avant scroll

        while (!reachedTargetDate && !noMoreScroll && attempts < 50) {
            try {
                List<WebElement> dateElements = driver.findElements(By.xpath("//span[contains(@class, 'showdate')]"));
                if (dateElements.isEmpty()) {
                    logger.warn("No date elements found. Retrying...");
                    continue;
                }

                WebElement lastDateElement = dateElements.get(dateElements.size() - 1);

                // 🛑 *** NOUVEAU : Simuler le hover pour récupérer la vraie date ***
                actions.moveToElement(lastDateElement).perform();
                Thread.sleep(1000); // Laisser le temps au tooltip d'apparaître

                String exactDateStr = lastDateElement.getAttribute("title");

                if (exactDateStr == null || exactDateStr.isEmpty()) {
                    logger.warn("Exact date not available, using relative date: {}", lastDateElement.getText());
                    continue;
                }

                Date exactDate = displayedFormat.parse(exactDateStr);

                logger.info("Current last visible post date: {}", displayedFormat.format(exactDate));

                // 📌 **Arrêt si on atteint la date cible**
                if (!exactDate.after(targetDate)) {
                    logger.info("Reached target date {}. Stopping scroll.", displayedFormat.format(targetDate));
                    reachedTargetDate = true;
                    break;
                }

                // 📌 **Détection de fin de scroll**
                int currentPostCount = driver.findElements(By.xpath("//article[contains(@class, 'post__card')]")).size();
                if (currentPostCount == lastPostCount) {
                    logger.info("No new posts loaded after scrolling. Stopping.");
                    noMoreScroll = true;
                    break;
                }
                lastPostCount = currentPostCount;

            } catch (Exception e) {
                logger.error("Error while scrolling and checking date.", e);
            }

            // 🔽 **Faire défiler vers le bas**
            jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight)");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            attempts++;
        }

        logger.info("Scrolling process completed.");
    }
}
