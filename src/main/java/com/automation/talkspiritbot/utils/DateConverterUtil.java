package com.automation.talkspiritbot.utils;

import com.automation.talkspiritbot.config.AppConfig;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DateConverterUtil {

    private final AppConfig appConfig;

    public DateConverterUtil(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    // Mapping des nombres écrits en toutes lettres vers leur valeur numérique
    private static final Map<String, Integer> NUMBER_MAP = new HashMap<>();

    static {
        NUMBER_MAP.put("un", 1);
        NUMBER_MAP.put("une", 1);
        NUMBER_MAP.put("deux", 2);
        NUMBER_MAP.put("trois", 3);
        NUMBER_MAP.put("quatre", 4);
        NUMBER_MAP.put("cinq", 5);
        NUMBER_MAP.put("six", 6);
        NUMBER_MAP.put("sept", 7);
        NUMBER_MAP.put("huit", 8);
        NUMBER_MAP.put("neuf", 9);
        NUMBER_MAP.put("dix", 10);
        NUMBER_MAP.put("onze", 11);
        NUMBER_MAP.put("douze", 12);
    }

    /**
     * Convertit une chaîne relative en date au format spécifié.
     *
     * @param relativeDateString Chaîne relative (ex: "Il y a 3 heures", "Il y a deux mois", "Il y a un an")
     * @param format             Format de sortie (ex: "yyyyMMdd", "dd-MM-yyyy")
     * @return Date formatée en fonction du format spécifié.
     */

    public String convertRelativeDate(String relativeDateString) {
        return convertRelativeDate(relativeDateString, appConfig.getDefaultDateFormat());
    }


    public String convertRelativeDate(String relativeDateString, String format) {

        // Utiliser le format par défaut si le format passé est invalide
        String dateFormat = ValidationUtil.isValidAsDateFormat(format) ? format : appConfig.getDefaultDateFormat();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);

        LocalDate today = LocalDate.now();
        int amount = 0;
        String unit = "";

        // Expression régulière pour extraire le nombre et l'unité de temps
        Pattern pattern = Pattern.compile("(?i)il y a (\\d+|un|une|deux|trois|quatre|cinq|six|sept|huit|neuf|dix|onze|douze) (heure|jour|mois|an)s?");
        Matcher matcher = pattern.matcher(relativeDateString);

        if (matcher.find()) {
            String numberStr = matcher.group(1).toLowerCase(); // Prend le nombre en chiffre ou en lettres
            unit = matcher.group(2); // Prend l’unité de temps (heure, jour, mois, an)

            // Convertit les nombres en toutes lettres en chiffres
            amount = NUMBER_MAP.getOrDefault(numberStr, numberStr.matches("\\d+") ? Integer.parseInt(numberStr) : 1);

            switch (unit) {
                case "heure" -> today = today.minusDays(1);
                case "jour" -> today = today.minusDays(amount);
                case "mois" -> today = today.minusMonths(amount);
                case "an" -> today = today.minusYears(amount);
            }
        }

        return today.format(formatter);
    }
}
