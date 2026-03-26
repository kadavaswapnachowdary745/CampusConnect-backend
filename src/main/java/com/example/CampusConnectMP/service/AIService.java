package com.example.CampusConnectMP.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpStatusCodeException;

@Service
@Slf4j
public class AIService {

    @Value("${huggingface.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String HF_URL = "https://router.huggingface.co/v1/chat/completions";

    public String generateDescription(String title, String category, String condition) {
        String prompt = String.format("Generate a professional product description for a marketplace listing. " +
                "The product is a '%s' in '%s' category, and its condition is '%s'. " +
                "Keep it concise, around 2-3 sentences. Focus on its appeal to a student.", title, category, condition);
        return callHuggingFace(prompt, "You are a professional marketplace assistant.");
    }

    public String suggestPrice(String title, String condition) {
        String prompt = String
                .format("Suggest a price range in INR (₹) and a recommended exact price for a product titled '%s' " +
                        "in '%s' condition. Return ONLY the pricing information in this clear format:\n" +
                        "Suggested Price Range:\n₹X - ₹Y\nRecommended Price:\n₹Z", title, condition);
        return callHuggingFace(prompt, "You are a pricing expert for a student campus marketplace.");
    }

    public String chatWithAI(String message) {
        return callHuggingFace(message,
                "You are a helpful customer support chatbot for CampusConnect, a campus marketplace for students. Answer questions related to selling items, contacting sellers, meeting, payments, etc. Keep answers brief, helpful, and friendly.");
    }

    @SuppressWarnings("unchecked")
    private String callHuggingFace(String prompt, String systemMessage) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("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_replace_me")) {
            return "AI feature requires a valid Hugging Face API Key configured in the environment.";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                    "model", "meta-llama/Meta-Llama-3-8B-Instruct",
                    "messages", List.of(
                            Map.of("role", "system", "content", systemMessage),
                            Map.of("role", "user", "content", prompt)),
                    "max_tokens", 250);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(HF_URL, entity, Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
        } catch (HttpStatusCodeException e) {
            log.error("Hugging Face API error (status {}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getResponseBodyAsString().contains("permissions")) {
                return "The configured Hugging Face API key does not have the 'Make calls to Inference Providers' permission enabled. Please generate a new fine-grained token with this permission at https://huggingface.co/settings/tokens.";
            }
            return "Sorry, I couldn't generate a response at this time. Please try again later.";
        } catch (Exception e) {
            log.error("Unexpected error calling Hugging Face API: ", e);
            return "Sorry, I couldn't generate a response at this time. Please try again later.";
        }
        return "No response from AI.";
    }
}