package com.nikko.backend.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikko.backend.config.OpenAiProperties;
import com.nikko.backend.dto.report.AiReportContentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAiClient {

    private final RestClient openAiRestClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_PROMPT = """
            You are an expert technical assessor analyzing a student's quiz performance.
            Respond ONLY with a single JSON object, no prose before or after, matching exactly
            this schema:
            {
              "strengths": ["string", ...],
              "weaknesses": ["string", ...],
              "topicsToImprove": ["string", ...],
              "recommendations": "string",
              "recommendedSkills": ["string", ...],
              "recommendedDifficulty": "BEGINNER" | "INTERMEDIATE" | "ADVANCED",
              "suggestedDirection": "string"
            }
            Base every claim strictly on the performance data provided in the user message.
            Do not invent skills or topics that are not present in that data.
            """;

    public AiReportContentDto generateReportContent(String performanceSummaryJson) {

        Map<String, Object> requestBody = Map.of(
                "model", properties.model(),
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", performanceSummaryJson)
                )
        );

        ChatCompletionResponse response = openAiRestClient.post()
                .uri("/chat/completions")
                .body(requestBody)
                .retrieve()
                .body(ChatCompletionResponse.class);

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("OpenAI returned an empty response");
        }

        String content = response.choices().get(0).message().content();

        try {
            return objectMapper.readValue(content, AiReportContentDto.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse OpenAI JSON content: " + e.getMessage(), e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChatCompletionResponse(List<Choice> choices) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Choice(Message message) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Message(String content) {}
}