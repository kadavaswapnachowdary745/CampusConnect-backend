package com.example.CampusConnectMP.controller;

import com.example.CampusConnectMP.controller.dto.AIResponse;
import com.example.CampusConnectMP.controller.dto.ChatRequest;
import com.example.CampusConnectMP.controller.dto.DescriptionRequest;
import com.example.CampusConnectMP.controller.dto.PriceRequest;
import com.example.CampusConnectMP.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    @Autowired
    private AIService aiService;

    @PostMapping("/generate-description")
    public ResponseEntity<AIResponse> generateDescription(@RequestBody DescriptionRequest request) {
        String description = aiService.generateDescription(
            request.getTitle() != null ? request.getTitle() : "Unknown Product",
            request.getCategory() != null ? request.getCategory() : "Other",
            request.getCondition() != null ? request.getCondition() : "Unknown"
        );
        return ResponseEntity.ok(new AIResponse(description));
    }

    @PostMapping("/suggest-price")
    public ResponseEntity<AIResponse> suggestPrice(@RequestBody PriceRequest request) {
        String suggestion = aiService.suggestPrice(
            request.getTitle() != null ? request.getTitle() : "Unknown Product",
            request.getCondition() != null ? request.getCondition() : "Unknown"
        );
        return ResponseEntity.ok(new AIResponse(suggestion));
    }

    @PostMapping("/chatbot")
    public ResponseEntity<AIResponse> chatbot(@RequestBody ChatRequest request) {
        String reply = aiService.chatWithAI(request.getMessage() != null ? request.getMessage() : "Hello");
        return ResponseEntity.ok(new AIResponse(reply));
    }
}
