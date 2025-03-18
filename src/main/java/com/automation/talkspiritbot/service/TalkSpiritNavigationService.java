package com.automation.talkspiritbot.service;

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

@Service
public class TalkSpiritNavigationService {

    private static final Logger logger = LoggerFactory.getLogger(TalkSpiritNavigationService.class);
    private final WebDriverService webDriverService;
    private static final Duration TIMEOUT = Duration.ofSeconds(10); // Temps d'attente configurable

    public TalkSpiritNavigationService(WebDriverService webDriverService) {
        this.webDriverService = webDriverService;
    }

    /**
     * Récupère le WebDriver avec une gestion centralisée.
     */
    private WebDriver getWebDriver() {
        return webDriverService.getDriver();
    }

    /**
     * Ferme les popups s'ils sont présents.
     */
    public void closePopupIfPresent() {
        WebDriver driver = getWebDriver();
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        By popupCloseButtonBy = By.xpath(UITagsConstants.btn_label_Fermer);

        try {
            logger.info("Checking for popup...");
            WebElement popupCloseButton = wait.until(ExpectedConditions.elementToBeClickable(popupCloseButtonBy));
            if (popupCloseButton.isDisplayed()) {
                popupCloseButton.click();
                logger.info("Popup closed successfully.");
            }
        } catch (Exception e) {
            logger.info("No popup detected. Continuing navigation...");
        }
    }

    /**
     * Navigue vers "Fil d'actualité".
     */
    public void goToFilActualite() {
        closePopupIfPresent();
        WebDriver driver = getWebDriver();
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        By filActuButtonBy = By.xpath(UITagsConstants.btn_href_Actualite);

        try {
            logger.info("Waiting for the News Feed button (XPath: {}).", filActuButtonBy);
            WebElement filActuButton = waitForElement(filActuButtonBy);
            filActuButton.click();
            logger.info("Successfully navigated to the News Feed.");
        } catch (Exception e) {
            logger.error("Failed to navigate to the News Feed. Element not found (XPath: {}).", filActuButtonBy, e);
        }
    }

    /**
     * Navigue vers "Cooptations".
     */
    public void goToCooptations() {
        closePopupIfPresent();
        WebDriver driver = getWebDriver();
        By cooptationsButtonBy = By.xpath(UITagsConstants.btn_href_Cooptation);

        try {
            logger.info("Waiting for the Cooptations button (XPath: {}).", cooptationsButtonBy);
            WebElement cooptationsButton = waitForElement(cooptationsButtonBy);
            cooptationsButton.click();
            logger.info("Successfully navigated to the Cooptations section.");
        } catch (Exception e) {
            logger.error("Failed to navigate to the Cooptations section. Element not found (XPath: {}).", cooptationsButtonBy, e);
        }
    }

    /**
     * Méthode générique pour attendre qu'un élément soit visible et cliquable.
     */
    private WebElement waitForElement(By locator) {
        WebDriver driver = getWebDriver();
        WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }
}
