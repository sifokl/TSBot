package com.automation.talkspiritbot.controller;


import com.automation.talkspiritbot.service.TalkSpiritLoginService;
import com.automation.talkspiritbot.service.TalkSpiritNavigationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bot")
public class BotController {

    private static final Logger logger = LoggerFactory.getLogger(BotController.class);


    private final TalkSpiritLoginService loginService;
    private final TalkSpiritNavigationService navigationService;

    public BotController(TalkSpiritLoginService loginService, TalkSpiritNavigationService navigationService) {
        this.loginService = loginService;
        this.navigationService = navigationService;
    }

    @GetMapping("/start")
    public String startBot() {
        try {
            logger.info("Before Login...");
            loginService.login();

            logger.info("Before Actualite...");
            navigationService.goToFilActualite();


            logger.info("Before Cooptation...");
            navigationService.goToCooptations();


            return "Bot executed successfully  !";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
