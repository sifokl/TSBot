package com.automation.talkspiritbot.service;

import com.automation.talkspiritbot.config.AppConfig;
import com.automation.talkspiritbot.utils.DateConverterUtil;
import org.openqa.selenium.*;
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


    private final AppConfig appConfig;
    private final DateConverterUtil dateConverterUtil;
    private static final Logger logger = LoggerFactory.getLogger(TalkSpiritScrollService.class);
    private final WebDriverService webDriverService;
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    public TalkSpiritScrollService(AppConfig appConfig, DateConverterUtil dateConverterUtil, WebDriverService webDriverService) {
        this.appConfig = appConfig;
        this.dateConverterUtil = dateConverterUtil;
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
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        Actions actions = new Actions(driver);

        SimpleDateFormat usedFormat = new SimpleDateFormat(appConfig.getDefaultDateFormat());

        Date targetDate;
        try {
            targetDate = usedFormat.parse(targetDateStr);
        } catch (ParseException e) {
            logger.error("Invalid date format: {}. Expected format: yyyyMMdd", targetDateStr, e);
            return;
        }

        logger.info("Scrolling until posts older than {}", usedFormat.format(targetDate));

        // ✅ Trouver l'élément scrollable
        By scrollContainerBy = By.className("post__wrapper__content");
        WebElement scrollContainer = driver.findElement(scrollContainerBy);

        // ✅ Assurer le focus sur la zone centrale
        actions.moveToElement(scrollContainer).click().perform();
        logger.info("Clicked on the content area to ensure focus.");

        // ✅ Scroll progressif avec contrôle de hauteur
        int lastScrollHeight = 0;
        int maxAttempts = 50;
        boolean reachedTargetDate = false;
        boolean noMoreScroll = false;

        for (int attempt = 0; attempt < maxAttempts && !reachedTargetDate && !noMoreScroll; attempt++) {
            try {
                // 🔥 Vérifier la hauteur actuelle du conteneur
                int newScrollHeight = ((Number) jsExecutor.executeScript("return arguments[0].scrollHeight;", scrollContainer)).intValue();

                if (newScrollHeight == lastScrollHeight) {
                    logger.info("No more content to load. Stopping scroll.");
                    noMoreScroll = true;
                    break;
                }

                // ⏬ Scroller de manière progressive
                jsExecutor.executeScript("arguments[0].scrollTop += 500;", scrollContainer);
                lastScrollHeight = newScrollHeight;

                // 🕒 Attendre le chargement des nouveaux posts
                Thread.sleep(2000);

                // 🔍 Vérifier la date du dernier post affiché
                List<WebElement> dateElements = driver.findElements(By.xpath("//span[contains(@class, 'showdate')]"));
                if (!dateElements.isEmpty()) {
                    WebElement lastDateElement = dateElements.get(dateElements.size() - 1);
                    String dateAsTimeAgo = lastDateElement.getText();
                    String dateFormatted = dateConverterUtil.convertRelativeDate(dateAsTimeAgo);
                    Date exactDate = usedFormat.parse(dateFormatted);

                    logger.info("Current last visible post date: {}", usedFormat.format(exactDate));

                    // 📌 Arrêt si on atteint la date cible
                    if (!exactDate.after(targetDate)) {
                        logger.info("Reached target date {}. Stopping scroll.", usedFormat.format(targetDate));
                        reachedTargetDate = true;
                        break;
                    }
                }

            } catch (Exception e) {
                logger.error("Error while scrolling and checking date.", e);
            }
        }

        logger.info("Scrolling process completed.");
    }

}
