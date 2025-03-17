package com.automation.talkspiritbot.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TalkSpiritConfig {

    private static final Logger logger = LoggerFactory.getLogger(WebDriverConfig.class);

    @Value("${talkspirit.loginurl}")
    private String talkSpiritLoginUrl;

    public String getTalkSpiritLoginUrl() {
        return talkSpiritLoginUrl;
    }


}
