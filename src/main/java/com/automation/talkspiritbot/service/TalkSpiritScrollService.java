package com.automation.talkspiritbot.service;

import com.automation.talkspiritbot.config.AppConfig;
import com.automation.talkspiritbot.model.PostRecord;
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

        while (!reachedTargetDate && !noMoreScroll && attempts < 100) {
            logger.info("Attempt number: {}", attempts);
            try {
                List<WebElement> postElements = driver.findElements(By.xpath("//article[contains(@class, 'post__card')]"));

                if (postElements.isEmpty()) {
                    logger.warn("No post elements found. Retrying...");
                    continue;
                }

                int currentValidPosts = 0;

                for (WebElement postElement : postElements) {
                    try {
                        // 🆕 Forcer React à charger le contenu via scroll visuel
                        jsExecutor.executeScript("arguments[0].scrollIntoView({behavior: 'instant', block: 'center'});", postElement);
                        Thread.sleep(2000); // 🆕 Attente plus longue pour laisser le DOM se mettre à jour

                        // 🆕 Tentative d’attente active que certains éléments DOM soient bien présents
                        try {
                            wait.until(ExpectedConditions.presenceOfNestedElementLocatedBy(
                                    postElement, By.xpath(".//h2[@class='special-title']/a")
                            ));
                        } catch (TimeoutException e) {
                            logger.warn("Le post [{}] ne contient toujours pas les sous-éléments attendus.", postElement.getAttribute("id"));
                        }

                        PostRecord post = extractPostDetails(postElement);

                        logger.info("Post trouvé: [{}] | Titre: {} | Auteur: {} | Date: {}",
                                post.id(), post.postTitle(), post.postCreator(), post.postDate());

                        if (post.postDate().equalsIgnoreCase("unknown")) {
                            logger.warn("Post non encore rendu (lazy loading ?) — ID: {}", post.id());
                            continue;
                        }

                        currentValidPosts++;

                        Date postDate = usedFormat.parse(post.postDate());
                        if (!postDate.after(targetDate)) {
                            logger.info("Date cible atteinte ({}). Fin du scroll.", usedFormat.format(targetDate));
                            reachedTargetDate = true;
                            break;
                        }

                    } catch (Exception e) {
                        logger.warn("Erreur lors du parsing du post : {}", e.getMessage());
                        logger.debug("Contenu HTML du post en erreur:\n{}", postElement.getAttribute("outerHTML"));
                    }
                }

                if (currentValidPosts == 0) {
                    logger.warn("Aucun post valide sur cette tentative. On continue à scroller...");
                }

                int currentPostCount = postElements.size();
                if (currentPostCount == lastPostCount) {
                    logger.info("Aucun nouveau post après scroll. Fin.");
                    noMoreScroll = true;
                    break;
                }

                lastPostCount = currentPostCount;

                // 🆕 Scroll bas avec pause courte
                jsExecutor.executeScript("arguments[0].scrollTop += 1500;", scrollContainer);
                logger.info("Scroll effectué (bas de page).");
                Thread.sleep(3000);

            } catch (Exception e) {
                logger.error("Erreur pendant le scroll/check à l’itération {}.", attempts, e);
            }

            attempts++;
        }

        logger.info("Scroll terminé.");
    }



    private PostRecord extractPostDetails(WebElement postElement) {
        String postId = postElement.getAttribute("id");
        String postCreator = "Unknown";
        String postDateStr = "Unknown";
        String postTitle = "Unknown";
        boolean hasAttachedFile = false;
        String postLink = "";
        String postAttachedFileName = "";
        String postAttachedFileButtonXPath = "";

        try {
            // Créateur du post
            WebElement creatorElement = postElement.findElement(By.xpath(".//div[@class='post__card__header__info__author']/a"));
            postCreator = creatorElement.getText();

            // Date du post
            WebElement dateElement = postElement.findElement(By.xpath(".//span[contains(@class, 'showdate')]"));
            String dateAsTimeAgo = dateElement.getText();
            postDateStr = dateConverterUtil.convertRelativeDate(dateAsTimeAgo);

            // Titre du post
            WebElement titleElement = postElement.findElement(By.xpath(".//h2[@class='special-title']/a/div/span"));
            postTitle = titleElement.getText();

            // Lien du post
            WebElement linkElement = postElement.findElement(By.xpath(".//h2[@class='special-title']/a"));
            postLink = linkElement.getAttribute("href");

            // Vérifier s'il y a une section "Pièces jointes"
            List<WebElement> attachmentSections = postElement.findElements(By.xpath(".//div[contains(@class, 'gallery gallery--separator')]"));
            if (!attachmentSections.isEmpty()) {
                hasAttachedFile = true;

                // Essayer de récupérer le nom du fichier attaché
                try {
                    WebElement fileNameElement = postElement.findElement(By.xpath(".//span[@class='gallery__caption__item__title gallery__caption__item__title--extension']"));
                    postAttachedFileName = fileNameElement.getText();
                } catch (NoSuchElementException e) {
                    logger.warn("No filename found for attached file in post ID: {}", postId);
                }

                //  Générer le XPath du bouton de téléchargement
                postAttachedFileButtonXPath = String.format("//*[@id='%s']//button[contains(@class, 'gallery__button')]", postId);
            }


        } catch (NoSuchElementException e) {
            logger.warn("Some post elements were not found for post ID: {}", postId);
        }

        return new PostRecord(postId, postCreator, postDateStr, postTitle, hasAttachedFile, postLink, postAttachedFileName, postAttachedFileButtonXPath);
    }


}
