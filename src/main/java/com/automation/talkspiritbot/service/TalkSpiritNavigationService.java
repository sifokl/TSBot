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
    private final WebDriver driver;

    public TalkSpiritNavigationService(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Closes any popups that might block navigation.
     */
    public void closePopupIfPresent() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        By popupCloseButtonBy = By.xpath(UITagsConstants.btn_label_Fermer); // Vérifie si le sélecteur est exact

        try {
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
     * Navigates to the "Fil d'actualité" (News Feed).
     */
    public void goToFilActualite() {
        closePopupIfPresent(); // Vérifie et ferme le popup avant de naviguer

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        By filActuButtonBy = By.xpath(UITagsConstants.btn_href_Actualite);

        try {
            logger.info("Waiting for the News Feed button to be visible and clickable (XPath: {}).", filActuButtonBy);
            WebElement filActuButton = wait.until(ExpectedConditions.elementToBeClickable(filActuButtonBy));
            filActuButton.click();
            logger.info("Successfully navigated to the News Feed.");
            waitForPageLoad(3000);
        } catch (Exception e) {
            logger.error("Failed to navigate to the News Feed. Element not found (XPath: {}).", filActuButtonBy, e);
        }
    }

    /**
     * Navigates to the "Cooptations" section.
     */
    public void goToCooptations() {
        closePopupIfPresent(); // Vérifie et ferme le popup avant de naviguer

        By cooptationsButtonBy = By.xpath(UITagsConstants.btn_href_Cooptation);
        try {
            logger.info("Attempting to navigate to the Cooptations section (XPath: {}).", cooptationsButtonBy);
            WebElement cooptationsButton = driver.findElement(cooptationsButtonBy);
            cooptationsButton.click();
            logger.info("Successfully navigated to the Cooptations section.");
            waitForPageLoad(3000);
        } catch (Exception e) {
            logger.error("Failed to navigate to the Cooptations section. Element not found (XPath: {}).", cooptationsButtonBy, e);
        }
    }

    /**
     * Waits for the page to load after navigation.
     *
     * @param milliseconds Time to wait in milliseconds.
     */
    private void waitForPageLoad(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Thread interrupted while waiting for page load.");
        }
    }
}
