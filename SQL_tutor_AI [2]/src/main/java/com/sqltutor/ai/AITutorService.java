package com.sqltutor.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqltutor.model.ChatMessage;
import com.sqltutor.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AITutorService {

    @Value("${ai.groq.api.key}")
    private String apiKey;

    // Groq API endpoint
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    // ✅ FIX: Use a valid Groq model (llama3-8b-8192 is free and fast)
    private static final String GROQ_MODEL = "llama3-8b-8192";

    private final ChatMessageRepository chatRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    public AITutorService(ChatMessageRepository chatRepo) {
        this.chatRepo = chatRepo;
    }

    private static final String SYSTEM_PROMPT =
            "You are an expert SQL Tutor AI. Help students learn SQL clearly and concisely. " +
            "Explain queries, fix errors, and teach SQL concepts with short examples. " +
            "Keep responses focused and educational.";

    public String chat(String username, String userMessage,
                       String currentSql, List<ChatMessage> history) {

        String aiResponse = callGroq(userMessage, currentSql);

        chatRepo.save(new ChatMessage(username, "user", userMessage, currentSql));
        chatRepo.save(new ChatMessage(username, "assistant", aiResponse, currentSql));

        return aiResponse;
    }

    private String callGroq(String userMessage, String currentSql) {
        try {
            String fullPrompt = (currentSql != null && !currentSql.isBlank())
                    ? "SQL Query under review:\n```sql\n" + currentSql + "\n```\n\nUser Question: " + userMessage
                    : userMessage;

            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("model", GROQ_MODEL);
            requestMap.put("temperature", 0.7);
            requestMap.put("max_tokens", 1024);   // ✅ FIX: correct param name for Groq
            requestMap.put("top_p", 1);
            requestMap.put("stream", false);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
            messages.add(Map.of("role", "user",   "content", fullPrompt));
            requestMap.put("messages", messages);

            String requestBody = objectMapper.writeValueAsString(requestMap);

            System.out.println("➡️ Calling Groq API | Model: " + GROQ_MODEL);
            System.out.println("➡️ Request Body: " + requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GROQ_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(60))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            System.out.println("⬅️ Groq Status: " + response.statusCode());
            System.out.println("⬅️ Groq Response: " + response.body());

            return parseGroqResponse(response.body());

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ AI Error: " + e.getMessage();
        }
    }

    private String parseGroqResponse(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);

            if (root.has("error")) {
                JsonNode error = root.get("error");
                String errorMsg = error.has("message")
                        ? error.get("message").asText()
                        : error.toString();
                return "❌ AI Error: " + errorMsg;
            }

            JsonNode choices = root.path("choices");

            if (choices.isArray() && choices.size() > 0) {
                JsonNode content = choices.get(0)
                        .path("message")
                        .path("content");

                if (!content.isMissingNode() && !content.isNull()) {
                    return content.asText();
                }
            }

            return "⚠️ AI returned an empty response. Please try again.";

        } catch (Exception e) {
            return "❌ Parse Error: " + e.getMessage();
        }
    }

    public List<ChatMessage> getChatHistory(String username) {
        return chatRepo.findRecentByUsername(username, PageRequest.of(0, 50));
    }

    public void clearChatHistory(String username) {
        chatRepo.deleteByUsername(username);
    }
}
