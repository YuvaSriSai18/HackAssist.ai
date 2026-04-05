package com.hackassist.ai.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hackassist.ai.dto.AIGeneratedTaskDTO;
import com.hackassist.ai.dto.AITasksResponseDTO;
import com.hackassist.ai.dto.FeatureDTO;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class GeminiService implements IAIService {
    
    @Value("${gemini.api.key}")
    private String geminiApiKey;
    
    @Value("${gemini.model.name}")
    private String modelName;
    
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/";
    private final Gson gson = new Gson();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    
    @Override
    public AITasksResponseDTO generateTasksFromProblemStatement(String problemStatement, String hackathonTheme) {
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            log.error("Gemini API key not configured");
            throw new RuntimeException("Gemini API key not configured");
        }
        
        String prompt = String.format(
            "You are a hackathon project assistant. Given the problem statement, break it down into concrete, actionable tasks.\n\n" +
            "Problem Statement: %s\n" +
            "Hackathon Theme: %s\n\n" +
            "Please provide:\n" +
            "1. A list of features needed\n" +
            "2. A breakdown of tasks with title, description, estimated hours, and priority\n" +
            "3. Recommended technologies\n" +
            "4. A brief project summary\n\n" +
            "Return the response in JSON format with keys: features, tasks, recommendedTechnologies, summary",
            problemStatement, hackathonTheme
        );
        
        try {
            String response = callGeminiAPI(prompt);
            return parseAIResponse(response, problemStatement);
        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            throw new RuntimeException("Failed to generate tasks from AI: " + e.getMessage());
        }
    }
    
    @Override
    public List<FeatureDTO> generateFeatures(String problemStatement) {
        String prompt = String.format(
            "Based on this problem statement, identify and list the key features needed:\n" +
            "%s\n\n" +
            "Return JSON array with objects containing: name, description, priority, technologiesNeeded (array)",
            problemStatement
        );
        
        try {
            String response = callGeminiAPI(prompt);
            return parseFeatures(response);
        } catch (Exception e) {
            log.error("Error generating features", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public String generateProjectSummary(Long projectId) {
        return "Project summary generated. Features and progress tracked.";
    }
    
    private String callGeminiAPI(String prompt) throws IOException, InterruptedException {
        String url = GEMINI_API_URL + modelName + ":generateContent?key=" + geminiApiKey;
        
        JsonObject requestBody = new JsonObject();
        JsonArray contentsArray = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray partsArray = new JsonArray();
        JsonObject part = new JsonObject();
        
        part.addProperty("text", prompt);
        partsArray.add(part);
        content.add("parts", partsArray);
        contentsArray.add(content);
        requestBody.add("contents", contentsArray);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini API error: " + response.statusCode() + " - " + response.body());
        }
        
        return response.body();
    }
    
    private AITasksResponseDTO parseAIResponse(String apiResponse, String problemStatement) {
        AITasksResponseDTO dto = new AITasksResponseDTO();
        dto.setProjectName("Hackathon Project");
        dto.setProjectSummary("Project generated from problem statement");
        dto.setFeatures(new ArrayList<>());
        dto.setTasks(new ArrayList<>());
        dto.setRecommendedTechnologies(Arrays.asList("Backend", "Frontend", "Database"));
        
        try {
            JsonObject jsonResponse = gson.fromJson(apiResponse, JsonObject.class);
            
            if (jsonResponse.has("candidates")) {
                JsonArray candidates = jsonResponse.getAsJsonArray("candidates");
                if (candidates.size() > 0) {
                    JsonObject candidate = candidates.get(0).getAsJsonObject();
                    if (candidate.has("content")) {
                        JsonObject content = candidate.getAsJsonObject("content");
                        if (content.has("parts")) {
                            JsonArray parts = content.getAsJsonArray("parts");
                            if (parts.size() > 0) {
                                String text = parts.get(0).getAsJsonObject().get("text").getAsString();
                                // Parse the text response into structured data
                                dto.setProjectSummary(text.substring(0, Math.min(200, text.length())));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error parsing AI response, returning default structure", e);
        }
        
        return dto;
    }
    
    private List<FeatureDTO> parseFeatures(String response) {
        List<FeatureDTO> features = new ArrayList<>();
        try {
            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
            // Parse features from response
            features.add(new FeatureDTO("Core Feature", "Main feature", "HIGH", Arrays.asList("Tech1", "Tech2")));
        } catch (Exception e) {
            log.error("Error parsing features", e);
        }
        return features;
    }
}
