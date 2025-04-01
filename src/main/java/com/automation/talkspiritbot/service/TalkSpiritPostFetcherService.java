package com.automation.talkspiritbot.service;

import com.automation.talkspiritbot.model.PostRecord;
import com.automation.talkspiritbot.parser.TalkSpiritPostParser;
import com.automation.talkspiritbot.utils.LoginRedirectHandler;
import com.automation.talkspiritbot.utils.ThreadUtil;
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
    private final LoginRedirectHandler loginRedirectHandler;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public TalkSpiritPostFetcherService(WebDriverService webDriverService, TalkSpiritPostParser postParser, LoginRedirectHandler loginRedirectHandler) {
        this.webDriverService = webDriverService;
        this.postParser = postParser;
        this.loginRedirectHandler = loginRedirectHandler;
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

            ThreadUtil.sleepRandomBetween(1000, 3000);

            driver.switchTo().window(newTab);
            driver.get(postUrl);

            By articleSelector = By.cssSelector("article.post__card");

            // Ici on encapsule toute la logique de parsing du post dans une lambda
            return loginRedirectHandler.handleLoginIfRedirected(driver, () -> {

                log.info("Current  before Wait: {}", driver.getCurrentUrl()); // 🔍 Log de l'URL actuelle

                wait.until(ExpectedConditions.visibilityOfElementLocated(articleSelector));

                log.info("Current  after Wait: {}", driver.getCurrentUrl()); // 🔍 Log de l'URL actuelle

                for (int attempt = 1; attempt <= 3; attempt++) {
                    try {
                        WebElement article = driver.findElement(articleSelector);
                        PostRecord post = postParser.extractPostDetails(article);
                        log.info("Successfully fetched and parsed post dated {}", post.postDate());
                        return post;
                    } catch (StaleElementReferenceException e) {
                        log.warn("Attempt {}: Stale element reference, retrying...", attempt);
                        ThreadUtil.sleep(attempt*500);
                    }
                }

                log.error("Failed to fetch post after multiple attempts due to stale elements.");
                return null;
            });

        } catch (Exception e) {
            log.error("Erreur inattendue lors du fetch de {}", postUrl, e);
            return null;
        } finally {
            try {
                ThreadUtil.sleepRandomBetween(1000, 3000);
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
