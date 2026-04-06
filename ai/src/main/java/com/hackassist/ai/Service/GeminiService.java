package com.hackassist.ai.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/";
    private static final String PRIMARY_MODEL = "models/gemini-2.5-flash";
    private static final String FALLBACK_MODEL = "models/gemini-2.5-flash-lite";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateContent(String prompt) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.error("Gemini API key not configured");
            throw new RuntimeException("Gemini API key not configured");
        }

        try {
            String trimmedPrompt = prompt == null ? "" : prompt.trim();
            String promptPreview = trimmedPrompt.length() > 200 ? trimmedPrompt.substring(0, 200) + "..." : trimmedPrompt;
            String payload = buildRequestPayload(prompt);

            return callGemini(PRIMARY_MODEL, payload, promptPreview);
        } catch (RuntimeException ex) {
            log.warn("Primary Gemini model failed, attempting fallback: {}", ex.getMessage());
            String payload = buildRequestPayload(prompt);
            return callGemini(FALLBACK_MODEL, payload, prompt == null ? "" : prompt.trim());
        }
    }

    private String callGemini(String model, String payload, String promptPreview) {
        String url = API_BASE_URL + model + ":generateContent";
        log.info("Calling Gemini API URL: {}", url);
        log.info("Gemini model: {}", model);
        log.info("Gemini prompt preview: {}", promptPreview);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Gemini API response status: {}", response.statusCode());

            if (response.statusCode() != 200) {
                log.error("Gemini API failed using model {} - Status: {}, Body: {}", model, response.statusCode(), response.body());
                throw new RuntimeException("Gemini API error using model " + model + ": " + response.statusCode());
            }

            log.info("Gemini API response received successfully ({} chars)", response.body().length());
            return extractText(response.body());
        } catch (Exception ex) {
            log.error("Gemini API call failed using model {}: {}", model, ex.getMessage(), ex);
            throw new RuntimeException("Gemini API call failed using model " + model + ": " + ex.getMessage());
        }
    }

    private String buildRequestPayload(String prompt) {
        return "{" +
            "\"contents\":[{" +
            "\"parts\":[{" +
            "\"text\":" + objectMapper.valueToTree(prompt).toString() +
            "}]" +
            "}]" +
            "}";
    }

    private String extractText(String apiResponse) {
        try {
            JsonNode root = objectMapper.readTree(apiResponse);
            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new RuntimeException("Gemini response missing candidates");
            }

            JsonNode content = candidates.path(0).path("content");
            if (content.isMissingNode() || content.isNull()) {
                throw new RuntimeException("Gemini response missing content");
            }

            JsonNode parts = content.path("parts");
            if (!parts.isArray() || parts.isEmpty()) {
                throw new RuntimeException("Gemini response missing parts");
            }

            JsonNode textNode = parts.path(0).path("text");
            if (textNode.isMissingNode() || textNode.isNull()) {
                throw new RuntimeException("Gemini response missing text content");
            }

            String text = textNode.asText();
            if (text == null || text.isBlank()) {
                throw new RuntimeException("Gemini response text is empty");
            }
            return text;
        } catch (Exception ex) {
            log.error("Unable to parse Gemini response: {}", ex.getMessage());
            throw new RuntimeException("Unable to parse Gemini response: " + ex.getMessage());
        }
    }
}
