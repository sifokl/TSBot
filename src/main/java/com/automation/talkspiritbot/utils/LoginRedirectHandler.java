package com.automation.talkspiritbot.utils;


import com.automation.talkspiritbot.service.TalkSpiritLoginService;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class LoginRedirectHandler {

    private final TalkSpiritLoginService talkSpiritLoginService;
    private static final Logger log = LoggerFactory.getLogger(LoginRedirectHandler.class);

    public LoginRedirectHandler(TalkSpiritLoginService talkSpiritLoginService) {
        this.talkSpiritLoginService = talkSpiritLoginService;
    }

    /**
     * Vérifie si le navigateur a été redirigé vers la page de login, exécute le login si nécessaire,
     * puis lance une action à effectuer après connexion.
     *
     * @param driver Le WebDriver actif
     * @param onLoginSuccess L'action à exécuter après authentification
     * @return Le résultat de l'action, ou null si aucune redirection détectée
     */
    public <T> T handleLoginIfRedirected(WebDriver driver, Supplier<T> onLoginSuccess) {
        if (driver.getCurrentUrl().contains("/login")) {
            log.warn("Redirection vers la page de login détectée !");
            talkSpiritLoginService.login();
            ThreadUtil.sleepRandomBetween(2000,4000);
            return onLoginSuccess.get();
        }
        return null;
    }

    /**
     * Version sans retour, à utiliser si tu ne veux pas retourner de valeur.
     */
    public void handleLoginIfRedirected(WebDriver driver, Runnable onLoginSuccess) {
        if (driver.getCurrentUrl().contains("/login")) {
            log.warn("Redirection vers la page de login détectée !");
            talkSpiritLoginService.login();
            onLoginSuccess.run();
        }
    }
}
