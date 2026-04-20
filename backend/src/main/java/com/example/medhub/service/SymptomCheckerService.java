package com.example.medhub.service;

import com.example.medhub.configuration.MedHubProperties;
import com.example.medhub.dto.request.SymptomCheckRequestDto;
import com.example.medhub.dto.response.AiRecommendation;
import com.example.medhub.dto.response.SpecializationRecommendation;
import com.example.medhub.dto.response.SymptomCheckResponseDto;
import com.example.medhub.entity.SpecializationEntity;
import com.example.medhub.exceptions.AiServiceUnavailableException;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.repository.SpecializationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SymptomCheckerService {

    private final ChatClient chatClient;
    private final SpecializationRepository specializationRepository;
    private final MedHubProperties medHubProperties;

    public SymptomCheckerService(ChatClient.Builder chatClientBuilder,
                                  SpecializationRepository specializationRepository,
                                  MedHubProperties medHubProperties) {
        this.chatClient = chatClientBuilder
                .defaultSystem(buildSystemPrompt())
                .build();
        this.specializationRepository = specializationRepository;
        this.medHubProperties = medHubProperties;
    }

    public SymptomCheckResponseDto analyze(SymptomCheckRequestDto request) {
        if (!medHubProperties.getAi().isEnabled()) {
            throw new MedHubServiceException("AI assistant is currently disabled.");
        }

        List<SpecializationEntity> allSpecs = specializationRepository.findAll();
        Map<String, Long> specNameToId = allSpecs.stream()
                .collect(Collectors.toMap(
                        s -> s.getSpecializationName().toLowerCase(),
                        SpecializationEntity::getSpecializationId,
                        (a, b) -> a
                ));

        String specList = allSpecs.stream()
                .map(SpecializationEntity::getSpecializationName)
                .collect(Collectors.joining(", "));

        String userMessage = buildUserPrompt(request, specList);

        AiRecommendation aiResponse = callGeminiForRecommendation(userMessage);

        List<SpecializationRecommendation> recommendations = mapToRecommendations(aiResponse, specNameToId);

        return new SymptomCheckResponseDto(recommendations, SymptomCheckResponseDto.DISCLAIMER_TEXT);
    }

    private AiRecommendation callGeminiForRecommendation(String userMessage) {
        try {
            return chatClient.prompt()
                    .user(userMessage)
                    .call()
                    .entity(AiRecommendation.class);
        } catch (Exception e) {
            log.warn("Symptom checker: Gemini request or structured output failed", e);
            String detail = rootCauseMessage(e);
            String lower = detail != null ? detail.toLowerCase() : "";
            if (detail != null && (detail.contains("429") || lower.contains("quota") || lower.contains("resource_exhausted"))) {
                throw new AiServiceUnavailableException(
                        "AI quota exceeded or rate limited. Retry later or check your Gemini API plan.");
            }
            if (lower.contains("api key") || lower.contains("api_key") || lower.contains("permission_denied")) {
                throw new AiServiceUnavailableException(
                        "AI service rejected the request (invalid API key or API not enabled for this project).");
            }
            throw new AiServiceUnavailableException(
                    "AI analysis is temporarily unavailable. If this persists, verify GEMINI_API_KEY and model name.");
        }
    }

    private static String rootCauseMessage(Throwable e) {
        Throwable c = e;
        while (c.getCause() != null && c.getCause() != c) {
            c = c.getCause();
        }
        return c.getMessage() != null ? c.getMessage() : e.getClass().getSimpleName();
    }

    private List<SpecializationRecommendation> mapToRecommendations(
            AiRecommendation aiResponse, Map<String, Long> specNameToId) {

        if (aiResponse == null || aiResponse.recommendations() == null) {
            return List.of();
        }

        return aiResponse.recommendations().stream()
                .filter(entry -> specNameToId.containsKey(entry.specializationName().toLowerCase()))
                .map(entry -> new SpecializationRecommendation(
                        specNameToId.get(entry.specializationName().toLowerCase()),
                        entry.specializationName(),
                        entry.confidence(),
                        entry.reasoning()
                ))
                .limit(3)
                .toList();
    }

    private String buildUserPrompt(SymptomCheckRequestDto request, String availableSpecializations) {
        StringBuilder sb = new StringBuilder();
        sb.append("Patient profile: ").append(request.gender()).append(", age ").append(request.ageRange().getLabel()).append(".\n");
        sb.append("Reported symptoms: ").append(String.join(", ", request.symptoms())).append(".\n");
        if (request.additionalDescription() != null && !request.additionalDescription().isBlank()) {
            sb.append("Additional details: ").append(request.additionalDescription()).append("\n");
        }
        sb.append("\nAvailable specializations in our system: ").append(availableSpecializations).append("\n");
        sb.append("\nBased on the symptoms, recommend up to 3 medical specializations from the list above.");
        return sb.toString();
    }

    private String buildSystemPrompt() {
        return """
                You are a medical triage assistant for MedHub, a healthcare appointment platform.
                Your role is to suggest the most relevant medical specializations based on patient-reported symptoms.
                
                Rules:
                - Return ONLY specializations from the list provided in each request.
                - Recommend between 1 and 3 specializations, ordered by relevance.
                - For each recommendation provide: specializationName (exact match from the list), \
                confidence (HIGH, MEDIUM, or LOW), and reasoning (one concise sentence).
                - Never provide a medical diagnosis. You are only suggesting which type of specialist to visit.
                - If symptoms are vague or could match many specializations, prefer general ones like Family Medicine or Internal Medicine.
                - Always respond with valid JSON matching this schema: \
                {"recommendations": [{"specializationName": "...", "confidence": "HIGH|MEDIUM|LOW", "reasoning": "..."}]}
                """;
    }
}
