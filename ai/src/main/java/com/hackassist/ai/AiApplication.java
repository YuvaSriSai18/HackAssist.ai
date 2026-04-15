package com.hackassist.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AiApplication {

	private static final Logger logger = LoggerFactory.getLogger(AiApplication.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(AiApplication.class, args);
		String backendBaseUrl = System.getenv("BACKEND_BASE_URL");
		if (backendBaseUrl == null || backendBaseUrl.isEmpty()) {
			backendBaseUrl = "http://localhost:8080";
		}
		logger.info("===========================================");
		logger.info("🚀 AI-Powered Hackathon Assistant Started!");
		logger.info("===========================================");
		logger.info("Server running on: " + backendBaseUrl);
		logger.info("API Base URL: " + backendBaseUrl + "/api");
		logger.info("===========================================");
	}

}
