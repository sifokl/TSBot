package com.automation.talkspiritbot.service;

import com.automation.talkspiritbot.config.AppConfig;
import com.automation.talkspiritbot.model.PostRecord;
import com.automation.talkspiritbot.parser.TalkSpiritPostParser;
import com.automation.talkspiritbot.utils.DateConverterUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class TalkSpiritScrollService {

    private final TalkSpiritPostFetcher postFetcher;
    private final AppConfig appConfig;
    private final DateConverterUtil dateConverterUtil;
    private static final Logger logger = LoggerFactory.getLogger(TalkSpiritScrollService.class);
    private final WebDriverService webDriverService;
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    public TalkSpiritScrollService(TalkSpiritPostFetcher postFetcher, AppConfig appConfig, DateConverterUtil dateConverterUtil, WebDriverService webDriverService) {
        this.postFetcher = postFetcher;
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
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // 🆕 Wait explicite

        SimpleDateFormat usedFormat = new SimpleDateFormat(appConfig.getDefaultDateFormat());

        Date targetDate;
        try {
            targetDate = usedFormat.parse(targetDateStr);
        } catch (ParseException e) {
            logger.error("Invalid target date format: {}. Expected format: yyyyMMdd", targetDateStr, e);
            return;
        }

        logger.info("Scrolling until posts older than {}", usedFormat.format(targetDate));

        // Aller au container central pour focus
        By scrollContainerBy = By.className("post__wrapper__content");
        WebElement scrollContainer = driver.findElement(scrollContainerBy);
        actions.moveToElement(scrollContainer).click().perform();
        logger.info("Clicked on the content area to ensure focus.");

        boolean reachedTargetDate = false;
        boolean noMoreScroll = false;
        int attempts = 0;
        int lastPostCount = 0;


        List<WebElement> postElements = new ArrayList<>();


        while(!reachedTargetDate && !noMoreScroll && attempts<appConfig.getMaxAttempts()){

            attempts++;



            postElements = driver.findElements(By.xpath("//article[contains(@class, 'post__card')]"));

            logger.info("Nombre d'elements actuels : {}", postElements.size());

            WebElement lastPost = postElements.get(postElements.size() - 1);

            // Scroll pour charger le contenu du dernier post
            jsExecutor.executeScript("arguments[0].scrollIntoView({behavior: 'instant', block: 'center'});", lastPost);
            sleep(2000); // Attente DOM

            if (postElements.isEmpty()) {
                logger.warn("Aucun post détecté sur la page.");
                break;
            }

            // no more scroll
            if(postElements.size() == lastPostCount){
                noMoreScroll=true;
                logger.warn("Le Scroll n'est plus disponible. fin de la page");
                //break;
            }



            // max attempts reached , leave
            if(postElements.size()>= appConfig.getMaxAttempts()){
                logger.warn("Nombre d'articles maximum atteint : {}", postElements.size());
                break;
            }



            try {
                wait.until(ExpectedConditions.presenceOfNestedElementLocatedBy(
                        lastPost, By.xpath(".//span[contains(@class, 'showdate')]")
                ));
                logger.info("Lazy-loaded post date is now visible.");
            } catch (TimeoutException e) {
                logger.warn("Le dernier post [{}] ne contient toujours pas 'showdate'.", lastPost.getAttribute("id"));
            }

            Date lastPostDate =  dateConverterUtil.convertRelativeDateAsDate(lastPost.getText());

            // target date reached , leave
            if(lastPostDate.before(targetDate)) {
                reachedTargetDate=true;
                logger.warn("La date maximale est atteinte : {}", lastPostDate);
                //break;
            }


        }

        postElements = postElements.stream()
                .filter(e -> {
                    String id = e.getAttribute("id");
                    return id != null && id.startsWith("post_content_");
                }).toList();

        List<String> postUrls = postElements.stream()
                .map(element -> {
                    String rawId = element.getAttribute("id");
                    if (rawId != null && rawId.startsWith("post_content_")) {
                        String postId = rawId.replace("post_content_", "");
                        String url = "https://exteam.talkspirit.com/#/l/permalink/microblog/" + postId;
                        return url;
                    } else {
                        logger.warn("ID invalide ou manquant pour un post : {}", rawId);
                        return null;
                    }
                })
                .filter(url -> url != null && !url.isEmpty())
                .toList();



        postUrls.forEach(url ->  logger.info("URL recuperée  : {} ", url));




        logger.info("Scroll terminé.");
    }





    private void sleep(int ms){
        try {
            logger.info("Sleep {} ms", ms);
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            logger.error("Sleep not completed ");
            throw new RuntimeException(e);
        }
    }

}