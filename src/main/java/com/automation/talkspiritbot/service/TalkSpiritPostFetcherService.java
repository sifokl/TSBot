package com.automation.talkspiritbot.service;

import com.automation.talkspiritbot.model.PostRecord;
import com.automation.talkspiritbot.parser.TalkSpiritPostParser;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
    private WebDriverWait wait;
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final TalkSpiritPostParser postParser;

    public TalkSpiritPostFetcherService( WebDriverService webDriverService, TalkSpiritPostParser postParser) {
        this.webDriverService= webDriverService;
        //this.wait = new WebDriverWait(webDriverService.getDriver(), Duration.ofSeconds(10));
        this.postParser = postParser;
    }

    /**
     * Ouvre un nouvel onglet, charge l'URL du post, le parse et retourne un PostRecord.
     */
    public PostRecord fetchFromUrl(String postUrl) {


        log.info("Fetching post from URL in a new tab: {}", postUrl);

        WebDriver driver = webDriverService.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        String originalHandle = driver.getWindowHandle();

        try {
            ((JavascriptExecutor) driver).executeScript("window.open()");
            String newTab = driver.getWindowHandles()
                    .stream()
                    .filter(h -> !h.equals(originalHandle))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("New tab could not be opened"));

            driver.switchTo().window(newTab);
            driver.get(postUrl);

            By articleBy = By.cssSelector("article.post__card");

            wait.until(ExpectedConditions.presenceOfElementLocated(articleBy));
            WebElement postElement = driver.findElement(articleBy);

            PostRecord post = postParser.extractPostDetails(postElement);
            log.info("Successfully fetched and parsed post dated {}", post.postDate());
            return post;

        } catch (Exception e) {
            log.warn("Failed to fetch or parse post from URL: {}", postUrl, e);
            return null;
        } finally {
            driver.close();
            driver.switchTo().window(originalHandle);
        }
    }



    /**
     * pour un ensemble d'URLsOuvre un nouvel onglet, charge l'URL du post, le parse et retourne un PostRecord.
     */
    public List<PostRecord> fetchFromUrl(List<String> postUrls) {
        log.info("Fetching all posts from {} URLs :", postUrls.size());

        List<PostRecord> posts = postUrls.stream().map(this::fetchFromUrl).toList();

        return posts;
    }

}
