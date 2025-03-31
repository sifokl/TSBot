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

    public TalkSpiritPostFetcherService(WebDriverService webDriverService, TalkSpiritPostParser postParser) {
        this.webDriverService = webDriverService;
        this.postParser = postParser;
    }

    public PostRecord fetchFromUrl(String postUrl) {
        log.info("Fetching post from URL in a new tab: {}", postUrl);
        WebDriver driver = webDriverService.getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        String originalHandle = driver.getWindowHandle();

        try {
            ((JavascriptExecutor) driver).executeScript("window.open()");
            String newTab = driver.getWindowHandles().stream()
                    .filter(h -> !h.equals(originalHandle))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("New tab could not be opened"));

            driver.switchTo().window(newTab);
            driver.get(postUrl);

            By articleSelector = By.cssSelector("article.post__card");
            wait.until(ExpectedConditions.visibilityOfElementLocated(articleSelector));

            // Réessayer jusqu’à 3 fois en cas de stale element
            for (int attempt = 1; attempt <= 3; attempt++) {
                try {
                    WebElement article = driver.findElement(articleSelector);
                    PostRecord post = postParser.extractPostDetails(article);
                    log.info("Successfully fetched and parsed post dated {}", post.postDate());
                    return post;
                } catch (StaleElementReferenceException e) {
                    log.warn("Attempt {}: Stale element reference, retrying...", attempt);
                    Thread.sleep(500); // petite pause avant retry
                }
            }

            log.error("Failed to fetch post after multiple attempts due to stale elements.");
            return null;

        } catch (Exception e) {
            log.error("Erreur inattendue lors du fetch de {}", postUrl, e);
            return null;
        } finally {
            try {
                driver.close();
                driver.switchTo().window(originalHandle);
            } catch (Exception e) {
                log.warn("Erreur lors de la fermeture de l’onglet ou du retour à l’onglet original", e);
            }
        }
    }

    public List<PostRecord> fetchFromUrl(List<String> postUrls) {
        log.info("Fetching all posts from {} URLs :", postUrls.size());
        return postUrls.stream()
                .map(this::fetchFromUrl)
                .toList();
    }
}
