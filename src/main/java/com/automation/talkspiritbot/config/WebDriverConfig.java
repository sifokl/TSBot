package com.automation.talkspiritbot.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebDriverConfig {

    private static final Logger logger = LoggerFactory.getLogger(WebDriverConfig.class);

    @Value("${browser}")
    private String browser;

    @Value("${drivers.path}")
    private String driversPath;

    @Bean
    public WebDriver webDriver() {
        logger.info("Selected browser: {}", browser);
        logger.info("Driver path set to: {}", driversPath);

        WebDriver driver;

        switch (browser.toLowerCase()) {
            case "chrome":
                String chromeDriverPath = driversPath + "chromedriver.exe";
                validateDriverPath(chromeDriverPath);
                System.setProperty("webdriver.chrome.driver", chromeDriverPath);
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--start-maximized", "--disable-infobars");
                driver = new ChromeDriver(chromeOptions);
                logger.info("Chrome WebDriver initialized successfully.");
                break;

            case "edge":
                String edgeDriverPath = driversPath + "msedgedriver.exe";
                validateDriverPath(edgeDriverPath);
                System.setProperty("webdriver.edge.driver", edgeDriverPath);
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.addArguments("--start-maximized", "--disable-infobars");
                driver = new EdgeDriver(edgeOptions);
                logger.info("Edge WebDriver initialized successfully.");
                break;

            case "firefox":
                String firefoxDriverPath = driversPath + "geckodriver.exe";
                validateDriverPath(firefoxDriverPath);
                System.setProperty("webdriver.gecko.driver", firefoxDriverPath);
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.addArguments("--start-maximized");
                driver = new FirefoxDriver(firefoxOptions);
                logger.info("Firefox WebDriver initialized successfully.");
                break;

            default:
                String errorMsg = "Unsupported browser: " + browser;
                logger.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
        }

        return driver;
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
