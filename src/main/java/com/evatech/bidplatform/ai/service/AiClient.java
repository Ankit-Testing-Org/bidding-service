package com.evatech.bidplatform.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiClient {

    @Value("${ai.type}")
    private String aiType;

    @Value("${ai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private final  ObjectMapper mapper;

    public String analysePrompt(String prompt) {

        HttpEntity<Map<String, Object>> entity = null;
        String url = "";

        log.info("AI Type : "+aiType);
        if(aiType.equalsIgnoreCase("openrouter")) {
            entity = openRouter(prompt);
            url = "https://openrouter.ai/api/v1/chat/completions";
        } else if (aiType.equalsIgnoreCase("gemini")) {
            entity = gemini(prompt);
            url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key="
                    + apiKey;
        } else if (aiType.equalsIgnoreCase("github_model")) {
            entity = githubModel(prompt);
            url = "https://models.inference.ai.azure.com/chat/completions";
        } else if (aiType.equalsIgnoreCase("litellm")) {
            entity = liteLlm(prompt);
            url = "https://litellm.sandbox.starplatform.cloud";
        }

        log.info("AI Type url : "+url);
        ResponseEntity<String> response = restTemplate.postForEntity(
                url,
                entity,
                String.class
        );

        return extractContent(response.getBody());
    }

    private HttpEntity<Map<String, Object>> openRouter(String prompt) {

        Map<String, Object> message = Map.of(
                "role", "user",
                "content", prompt
        );

        Map<String, Object> request = Map.of(
                "model", "openai/gpt-4o-mini",
                "messages", List.of(message)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.add("HTTP-Referer", "http://localhost:8083");
        headers.add("X-Title", "Analyzer");

        return new HttpEntity<>(request, headers);
    }

    private HttpEntity<Map<String, Object>> gemini(String prompt) {
        Map<String, Object> part = Map.of(
                "text", prompt
        );

        Map<String, Object> content = Map.of(
                "parts", List.of(part)
        );

        Map<String, Object> request = Map.of(
                "contents", List.of(content)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(request, headers);
    }

    private HttpEntity<Map<String, Object>> githubModel(String prompt) {
        Map<String, Object> message = Map.of(
                "role", "user",
                "content", prompt
        );

        Map<String, Object> request = Map.of(
                "messages", List.of(message),
                "model", "gpt-4o-mini"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(request, headers);
    }

    private HttpEntity<Map<String, Object>> liteLlm(String prompt) {

        Map<String, Object> message = Map.of(
                "role", "user",
                "content", prompt
        );

        Map<String, Object> request = Map.of(
                "model", "gpt-5-dz-germany",
                "messages", List.of(message),
                "temperature", 0.2
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(request, headers);
    }

    private String extractContent(String json) {
        try {
            return mapper.readTree(json)
                    .get("choices").get(0)
                    .get("message").get("content")
                    .asText();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}