package com.automation.talkspiritbot.utils;

import java.text.SimpleDateFormat;

public class ValidationUtil {

    /**
     * Vérifie si le format de date fourni est valide.
     *
     * @param format Chaîne représentant un format de date (ex: "yyyyMMdd", "dd-MM-yyyy").
     * @return true si le format est valide, sinon false.
     */
    public static boolean isValidAsDateFormat(String format) {
        if (format == null || format.trim().isEmpty()) {
            return false;
        }

        try {
            new SimpleDateFormat(format);
            return true; // Le format est valide
        } catch (IllegalArgumentException e) {
            return false; // Format invalide
        }
    }
}
