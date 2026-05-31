package com.example.medhub.service;

import com.example.medhub.configuration.MedHubProperties;
import com.example.medhub.dto.request.SymptomCheckRequestDto;
import com.example.medhub.dto.response.SymptomCheckerRecommendation;
import com.example.medhub.dto.response.SymptomCheckResponseDto;
import com.example.medhub.entity.SpecializationEntity;
import com.example.medhub.enums.AgeRange;
import com.example.medhub.enums.Gender;
import com.example.medhub.exceptions.SymptomCheckerUnavailableException;
import com.example.medhub.exceptions.MedHubServiceException;
import com.example.medhub.repository.SpecializationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SymptomCheckerServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;
    @Mock
    private ChatClient chatClient;
    @Mock
    private ChatClientRequestSpec requestSpec;
    @Mock
    private CallResponseSpec callResponseSpec;
    @Mock
    private SpecializationRepository specializationRepository;

    private MedHubProperties medHubProperties;
    private SymptomCheckerService service;

    @BeforeEach
    void setUp() {
        medHubProperties = new MedHubProperties();
        medHubProperties.getAi().setEnabled(true);

        when(chatClientBuilder.defaultSystem(anyString())).thenReturn(chatClientBuilder);
        when(chatClientBuilder.build()).thenReturn(chatClient);

        service = new SymptomCheckerService(chatClientBuilder, specializationRepository, medHubProperties);
    }

    @Test
    void analyze_shouldReturnMappedRecommendations() {
        SpecializationEntity cardiology = new SpecializationEntity();
        cardiology.setSpecializationId(1L);
        cardiology.setSpecializationName("Cardiology");

        SpecializationEntity neurology = new SpecializationEntity();
        neurology.setSpecializationId(2L);
        neurology.setSpecializationName("Neurology");

        when(specializationRepository.findAll()).thenReturn(List.of(cardiology, neurology));
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);

        SymptomCheckerRecommendation mockServiceResponse = new SymptomCheckerRecommendation(List.of(
                new SymptomCheckerRecommendation.Entry("Cardiology", "HIGH", "Chest pain suggests cardiac evaluation."),
                new SymptomCheckerRecommendation.Entry("Neurology", "MEDIUM", "Dizziness may indicate neurological causes.")
        ));
        when(callResponseSpec.entity(SymptomCheckerRecommendation.class)).thenReturn(mockServiceResponse);

        SymptomCheckRequestDto request = new SymptomCheckRequestDto(
                AgeRange.AGE_31_50, Gender.MALE,
                List.of("Chest pain", "Dizziness"), "Pain worsens during exercise"
        );

        SymptomCheckResponseDto response = service.analyze(request);

        assertThat(response.recommendations()).hasSize(2);
        assertThat(response.recommendations().get(0).specializationId()).isEqualTo(1L);
        assertThat(response.recommendations().get(0).specializationName()).isEqualTo("Cardiology");
        assertThat(response.recommendations().get(0).confidence()).isEqualTo("HIGH");
        assertThat(response.recommendations().get(1).specializationId()).isEqualTo(2L);
        assertThat(response.disclaimer()).isEqualTo(SymptomCheckResponseDto.DISCLAIMER_TEXT);
    }

    @Test
    void analyze_shouldFilterOutUnknownSpecializations() {
        SpecializationEntity cardiology = new SpecializationEntity();
        cardiology.setSpecializationId(1L);
        cardiology.setSpecializationName("Cardiology");

        when(specializationRepository.findAll()).thenReturn(List.of(cardiology));
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);

        SymptomCheckerRecommendation mockServiceResponse = new SymptomCheckerRecommendation(List.of(
                new SymptomCheckerRecommendation.Entry("Cardiology", "HIGH", "Relevant."),
                new SymptomCheckerRecommendation.Entry("Nonexistent Specialty", "LOW", "Not in our system.")
        ));
        when(callResponseSpec.entity(SymptomCheckerRecommendation.class)).thenReturn(mockServiceResponse);

        SymptomCheckRequestDto request = new SymptomCheckRequestDto(
                AgeRange.AGE_18_30, Gender.FEMALE, List.of("Chest pain"), null
        );

        SymptomCheckResponseDto response = service.analyze(request);

        assertThat(response.recommendations()).hasSize(1);
        assertThat(response.recommendations().get(0).specializationName()).isEqualTo("Cardiology");
    }

    @Test
    void analyze_shouldThrowWhenSymptomCheckerDisabled() {
        medHubProperties.getAi().setEnabled(false);

        SymptomCheckRequestDto request = new SymptomCheckRequestDto(
                AgeRange.UNDER_18, Gender.OTHER, List.of("Headache"), null
        );

        assertThatThrownBy(() -> service.analyze(request))
                .isInstanceOf(MedHubServiceException.class)
                .hasMessageContaining("disabled");
    }

    @Test
    void analyze_shouldReturnEmptyListWhenServiceReturnsNull() {
        when(specializationRepository.findAll()).thenReturn(List.of());
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(SymptomCheckerRecommendation.class)).thenReturn(null);

        SymptomCheckRequestDto request = new SymptomCheckRequestDto(
                AgeRange.OVER_70, Gender.MALE, List.of("Fatigue"), null
        );

        SymptomCheckResponseDto response = service.analyze(request);

        assertThat(response.recommendations()).isEmpty();
        assertThat(response.disclaimer()).isNotBlank();
    }

    @Test
    void analyze_shouldLimitToThreeRecommendations() {
        SpecializationEntity s1 = new SpecializationEntity(); s1.setSpecializationId(1L); s1.setSpecializationName("A");
        SpecializationEntity s2 = new SpecializationEntity(); s2.setSpecializationId(2L); s2.setSpecializationName("B");
        SpecializationEntity s3 = new SpecializationEntity(); s3.setSpecializationId(3L); s3.setSpecializationName("C");
        SpecializationEntity s4 = new SpecializationEntity(); s4.setSpecializationId(4L); s4.setSpecializationName("D");

        when(specializationRepository.findAll()).thenReturn(List.of(s1, s2, s3, s4));
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);

        SymptomCheckerRecommendation mockServiceResponse = new SymptomCheckerRecommendation(List.of(
                new SymptomCheckerRecommendation.Entry("A", "HIGH", "r1"),
                new SymptomCheckerRecommendation.Entry("B", "HIGH", "r2"),
                new SymptomCheckerRecommendation.Entry("C", "MEDIUM", "r3"),
                new SymptomCheckerRecommendation.Entry("D", "LOW", "r4")
        ));
        when(callResponseSpec.entity(SymptomCheckerRecommendation.class)).thenReturn(mockServiceResponse);

        SymptomCheckRequestDto request = new SymptomCheckRequestDto(
                AgeRange.AGE_51_70, Gender.FEMALE, List.of("Everything hurts"), null
        );

        SymptomCheckResponseDto response = service.analyze(request);

        assertThat(response.recommendations()).hasSize(3);
    }

    @Test
    void analyze_shouldThrowServiceUnavailableWhenQuotaExceeded() {
        when(specializationRepository.findAll()).thenReturn(List.of());
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(SymptomCheckerRecommendation.class)).thenThrow(
                new RuntimeException("upstream 429 RESOURCE_EXHAUSTED"));

        SymptomCheckRequestDto request = new SymptomCheckRequestDto(
                AgeRange.AGE_31_50, Gender.MALE, List.of("Headache"), null);

        assertThatThrownBy(() -> service.analyze(request))
                .isInstanceOf(SymptomCheckerUnavailableException.class)
                .hasMessageContaining("quota");
    }

    @Test
    void analyze_shouldThrowServiceUnavailableWhenInvalidKey() {
        when(specializationRepository.findAll()).thenReturn(List.of());
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(SymptomCheckerRecommendation.class)).thenThrow(
                new RuntimeException("API key not valid"));

        SymptomCheckRequestDto request = new SymptomCheckRequestDto(
                AgeRange.AGE_31_50, Gender.MALE, List.of("Headache"), null);

        assertThatThrownBy(() -> service.analyze(request))
                .isInstanceOf(SymptomCheckerUnavailableException.class)
                .hasMessageContaining("rejected");
    }
}
