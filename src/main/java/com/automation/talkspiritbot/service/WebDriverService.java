package com.automation.talkspiritbot.service;


import jakarta.annotation.PostConstruct;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Service
public class WebDriverService {

    private static final Logger logger = LoggerFactory.getLogger(WebDriverService.class);

    @Value("${browser}")
    private String browser;

    @Value("${drivers.path}")
    private String driversPath;

    private WebDriver driver;
    private ChromeOptions chromeOptions;
    private EdgeOptions edgeOptions;
    private FirefoxOptions firefoxOptions;

    /**
     * Configure browser options at startup
     */
    @PostConstruct
    public void setupOptions() {
        logger.info("Initializing browser options...");

        // Chrome Options
        chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--start-maximized", "--disable-infobars");

        // Edge Options
        edgeOptions = new EdgeOptions();
        edgeOptions.addArguments("--start-maximized", "--disable-infobars");

        // Firefox Options
        firefoxOptions = new FirefoxOptions();
        firefoxOptions.addArguments("--start-maximized");

        logger.info("Browser options initialized.");
    }


    /**
     * Get WebDriver instance on demand
     */
    public WebDriver getDriver() {
        if (driver == null) {
            logger.info("Starting WebDriver for browser: {}", browser);
            driver = createWebDriver();
        }
        return driver;
    }

    /**
     * Create WebDriver instance based on configured browser
     */
    private WebDriver createWebDriver() {
        switch (browser.toLowerCase()) {
            case "chrome":
                String chromeDriverPath = driversPath + "chromedriver.exe";
                validateDriverPath(chromeDriverPath);
                System.setProperty("webdriver.chrome.driver", driversPath + "chromedriver.exe");
                logger.info("Chrome WebDriver initialized successfully.");
                return new ChromeDriver(chromeOptions);

            case "edge":
                String edgeDriverPath = driversPath + "msedgedriver.exe";
                validateDriverPath(edgeDriverPath);
                System.setProperty("webdriver.edge.driver", driversPath + "msedgedriver.exe");
                logger.info("Edge WebDriver initialized successfully.");
                return new EdgeDriver(edgeOptions);

            case "firefox":
                String firefoxDriverPath = driversPath + "geckodriver.exe";
                validateDriverPath(firefoxDriverPath);
                System.setProperty("webdriver.gecko.driver", driversPath + "geckodriver.exe");
                logger.info("Firefox WebDriver initialized successfully.");
                return new FirefoxDriver(firefoxOptions);

            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
    }

    /**
     * Close WebDriver and clean resources
     */
    public void closeDriver() {
        if (driver != null) {
            logger.info("Closing WebDriver...");
            driver.quit();
            driver = null;
        }
    }

    public void closeDriverAfter(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Thread interrupted while waiting for page to auto-close.");
        }
        closeDriver();
    }


    /**
     * Validates if the driver file exists in the specified path.
     *
     * @param driverPath The full path of the WebDriver executable.
     */
    private void validateDriverPath(String driverPath) {
        Path path = Paths.get(driverPath);
        if (!Files.exists(path)) {
            String errorMsg = "WebDriver not found at: " + driverPath;
            logger.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
        logger.info("WebDriver found at: {}", driverPath);
    }

}
