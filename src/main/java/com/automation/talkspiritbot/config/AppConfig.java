package com.automation.talkspiritbot.config;

import com.automation.talkspiritbot.utils.ValidationUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${date.format.default}")
    private String defaultDateFormat;

    @Value("${attempts.number.max}")
    private int maxAttempts;

    // ➕ Getter exposé pour le format de date
    public String getDefaultDateFormat() {
        return defaultDateFormat;
    }

    // ➕ Getter exposé pour le nombre maximum d'essais lors du scroll
    public int getMaxAttempts() {
        return maxAttempts;
    }

    /**
     * Vérifie si le format de date par défaut est valide.
     * En cas d'échec, l'application est arrêtée avec un message d'erreur clair.
     */
    @PostConstruct
    private void isDefaultFormatValidorFail() {
        if (!ValidationUtil.isValidAsDateFormat(defaultDateFormat)) {
            throw new IllegalStateException("Invalid date format in configuration: '"
                    + defaultDateFormat + "'. Please provide a valid format (e.g., 'yyyyMMdd').");
        }
    }
}
