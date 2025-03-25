package com.automation.talkspiritbot.parser;

import com.automation.talkspiritbot.model.PostRecord;
import com.automation.talkspiritbot.utils.DateConverterUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TalkSpiritPostParser {


    private final DateConverterUtil dateConverterUtil;
    private static final Logger logger = LoggerFactory.getLogger(TalkSpiritPostParser.class);

    public TalkSpiritPostParser(DateConverterUtil dateConverterUtil) {
        this.dateConverterUtil = dateConverterUtil;
    }

    public  PostRecord extractPostDetails(WebElement postElement) {
        String postId = postElement.getAttribute("id");
        String postCreator = "Unknown";
        String postDateStr = "Unknown";
        String postTitle = "Unknown";
        boolean hasAttachedFile = false;
        String postLink = "";
        String postAttachedFileName = "";
        String postAttachedFileButtonXPath = "";

        try {
            // Créateur du post
            WebElement creatorElement = postElement.findElement(By.xpath(".//div[@class='post__card__header__info__author']/a"));
            postCreator = creatorElement.getText();

            // Date du post
            WebElement dateElement = postElement.findElement(By.xpath(".//span[contains(@class, 'showdate')]"));
            String dateAsTimeAgo = dateElement.getText();
            postDateStr = dateConverterUtil.convertRelativeDate(dateAsTimeAgo);

            // Titre du post
            WebElement titleElement = postElement.findElement(By.xpath(".//h2[@class='special-title']/a/div/span"));
            postTitle = titleElement.getText();

            // Lien du post
            WebElement linkElement = postElement.findElement(By.xpath(".//h2[@class='special-title']/a"));
            postLink = linkElement.getAttribute("href");

            // Vérifier s'il y a une section "Pièces jointes"
            List<WebElement> attachmentSections = postElement.findElements(By.xpath(".//div[contains(@class, 'gallery gallery--separator')]"));
            if (!attachmentSections.isEmpty()) {
                hasAttachedFile = true;

                // Essayer de récupérer le nom du fichier attaché
                try {
                    WebElement fileNameElement = postElement.findElement(By.xpath(".//span[@class='gallery__caption__item__title gallery__caption__item__title--extension']"));
                    postAttachedFileName = fileNameElement.getText();
                } catch (NoSuchElementException e) {
                    logger.warn("No filename found for attached file in post ID: {}", postId);
                }

                //  Générer le XPath du bouton de téléchargement
                postAttachedFileButtonXPath = String.format("//*[@id='%s']//button[contains(@class, 'gallery__button')]", postId);
            }


        } catch (NoSuchElementException e) {
            logger.warn("Some post elements were not found for post ID: {}", postId);
        }

        return new PostRecord(postId, postCreator, postDateStr, postTitle, hasAttachedFile, postLink, postAttachedFileName, postAttachedFileButtonXPath);
    }

}
