package com.automation.talkspiritbot.service;

import com.automation.talkspiritbot.config.CredentialConfig;
import com.automation.talkspiritbot.config.TalkSpiritConfig;
import com.automation.talkspiritbot.utils.UITagsConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;

import static com.automation.talkspiritbot.utils.UITagsConstants.btn_Identifiant_et_MotDePasse;

@Service
public class TalkSpiritLoginService {

    private static final Logger logger = LoggerFactory.getLogger(TalkSpiritLoginService.class);

    private final TalkSpiritConfig talkSpiritConfig;
    private final WebDriver driver;
    private final CredentialConfig credentialConfig;

    public TalkSpiritLoginService(WebDriver driver, CredentialConfig credentialConfig, TalkSpiritConfig talkSpiritConfig) {
        this.driver = driver;
        this.credentialConfig = credentialConfig;
        this.talkSpiritConfig = talkSpiritConfig;
    }

    public void login() {
        // Load the login page
        String loginPageUrl = talkSpiritConfig.getTalkSpiritLoginUrl();
        driver.get(loginPageUrl);
        logger.info("Loaded login page: {}", loginPageUrl);


        // Step 1: Wait and click on "Identifiant & Mot de passe"
        By loginMethodButtonBy = By.xpath(UITagsConstants.btn_contains_Identifiant_et_MotDePasse);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));


        try {
            WebElement loginMethodButton = wait.until(ExpectedConditions.elementToBeClickable(loginMethodButtonBy));
            loginMethodButton.click();
            logger.info("Clicked on login method button (XPath: {}).", loginMethodButtonBy);
        } catch (Exception e) {
            logger.error("Failed to find or click on login method button (XPath: {}).", loginMethodButtonBy, e);
            return;
        }

        // Step 2: Wait for the email & password fields
        try {
            WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(UITagsConstants.username)));
            emailInput.sendKeys(credentialConfig.getEmail());
            logger.info("Entered email in field (ID: {}).", UITagsConstants.username);

            WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(UITagsConstants.password)));
            passwordInput.sendKeys(credentialConfig.getPassword());
            logger.info("Entered password in field (ID: {}).", UITagsConstants.password);

            // Step 3: Click login button
            WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.id(UITagsConstants.btn_id_login)));
            loginButton.click();
            logger.info("Clicked on login button (ID: {}).", UITagsConstants.btn_id_login);

        } catch (Exception e) {
            logger.error("Error while filling in login credentials.", e);
            return;
        }

        logger.info("Authentication process completed.");
    }
}
