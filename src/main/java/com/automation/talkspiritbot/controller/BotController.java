package com.automation.talkspiritbot.controller;


import com.automation.talkspiritbot.service.TalkSpiritLoginService;
import com.automation.talkspiritbot.service.TalkSpiritNavigationService;
import com.automation.talkspiritbot.service.TalkSpiritScrollService;
import com.automation.talkspiritbot.service.WebDriverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bot")
public class BotController {

    private static final Logger logger = LoggerFactory.getLogger(BotController.class);


    private final WebDriverService webDriverService;
    private final TalkSpiritLoginService loginService;
    private final TalkSpiritNavigationService navigationService;
    private final TalkSpiritScrollService scrollService;

    public BotController(TalkSpiritLoginService loginService, TalkSpiritNavigationService navigationService, WebDriverService webDriverService, TalkSpiritScrollService scrollService) {
        this.loginService = loginService;
        this.navigationService = navigationService;
        this.webDriverService = webDriverService;
        this.scrollService = scrollService;
    }

    @GetMapping("/start")
    public String startBot() {

        try {

            webDriverService.getDriver();

            logger.info("Before Login...");
            loginService.login();

            logger.info("Before Actualite...");
            navigationService.goToFilActualite();


            logger.info("Before Cooptation...");
            navigationService.goToCooptations();


            logger.info("Before Scroll Down...");
            scrollService.scrollUntilDate("20240101");

            //webDriverService.closeDriverAfter(5000);

            return "Bot executed successfully  !";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
