package com.automation.talkspiritbot;

import com.automation.talkspiritbot.config.CredentialConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TalkSpiritBotApplication {

	private static final Logger logger = LoggerFactory.getLogger(TalkSpiritBotApplication.class);

	public static void main(String[] args) {
		logger.info("Starting TalkSpiritBot application...");
		SpringApplication.run(TalkSpiritBotApplication.class, args);
		logger.info("TalkSpiritBot application started successfully.");
	}

	@Bean
	public CommandLineRunner testCredentials(CredentialConfig credentialConfig) {
		return args -> {
			if (credentialConfig.getEmail() != null) {
				logger.info("Successfully retrieved email: {}", credentialConfig.getEmail());
			} else {
				logger.warn("No email found in environment variables!");
			}

			if (credentialConfig.getPassword() != null) {
				logger.info("Password successfully retrieved (value hidden for security).");
			} else {
				logger.warn("No password found in environment variables!");
			}
		};
	}
}
