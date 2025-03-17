package com.automation.talkspiritbot.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CredentialConfig {
    @Value("${TALKSPIRIT_EMAIL}")
    private String email;

    @Value("${TALKSPIRIT_PASSWORD}")
    private String password;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
