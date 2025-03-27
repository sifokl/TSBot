package com.automation.talkspiritbot.service;

import com.automation.talkspiritbot.model.PostRecord;
import com.automation.talkspiritbot.parser.TalkSpiritPostParser;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

@Service
public class TalkSpiritPostFetcherService {

    private final WebDriverService webDriverService;
    private final TalkSpiritPostParser postParser;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final int MAX_RETRIES = 3;

    public TalkSpiritPostFetcherService(WebDriverService webDriverService, TalkSpiritPostParser postParser) {
        this.webDriverService = webDriverService;
        this.postParser = postParser;
    }

    /**
     * Fait plusieurs tentatives pour ouvrir un post et le parser.
     */
    public PostRecord fetchFromUrl(String postUrl) {
        int attempts = 0;

        while (attempts < MAX_RETRIES) {
            try {
                return internalFetch(postUrl);
            } catch (StaleElementReferenceException | TimeoutException e) {
                attempts++;
                log.warn("Tentative {}/{} échouée pour {}", attempts, MAX_RETRIES, postUrl, e);
            } catch (Exception e) {
                log.error("Erreur inattendue lors du fetch de {}", postUrl, e);
                break;
            }
        }

        log.warn("Echec total après {} tentatives pour : {}", MAX_RETRIES, postUrl);
        return null;
    }

    /**
     * Une seule tentative pour ouvrir, charger et parser un post.
     */
    private PostRecord internalFetch(String postUrl) {
        log.info("Fetching post from URL in a new tab: {}", postUrl);

        WebDriver driver = webDriverService.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        String originalHandle = driver.getWindowHandle();

        try {
            // Ouvre un nouvel onglet
            ((JavascriptExecutor) driver).executeScript("window.open()");
            String newTab = driver.getWindowHandles()
                    .stream()
                    .filter(h -> !h.equals(originalHandle))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Impossible d'ouvrir un nouvel onglet"));

            driver.switchTo().window(newTab);
            driver.get(postUrl);

            // Attend l'article principal
            By articleBy = By.cssSelector("article.post__card");
            WebElement postElement = wait.until(ExpectedConditions.presenceOfElementLocated(articleBy));

            // Relocalise pour éviter stale reference
            postElement = driver.findElement(articleBy);
            PostRecord post = postParser.extractPostDetails(postElement);
            log.info("✅ Post récupéré et parsé : {}", post.postDate());
            return post;

        } finally {
            // Ferme l'onglet et retourne à l'onglet original
            driver.close();
            driver.switchTo().window(originalHandle);
        }
    }

    /**
     * Version batch multi-URL.
     */
    public List<PostRecord> fetchFromUrl(List<String> postUrls) {
        log.info("Fetching {} posts en parallèle (séquentiellement ici)", postUrls.size());
        return postUrls.stream()
                .map(this::fetchFromUrl)
                .toList();
    }
}
