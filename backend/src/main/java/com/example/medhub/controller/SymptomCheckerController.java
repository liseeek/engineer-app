package com.example.medhub.controller;

import com.example.medhub.dto.request.SymptomCheckRequestDto;
import com.example.medhub.dto.response.SymptomCheckResponseDto;
import com.example.medhub.service.SymptomCheckerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/symptom-checker")
@RequiredArgsConstructor
@Tag(name = "AI Symptom Checker")
public class SymptomCheckerController {

    private final SymptomCheckerService symptomCheckerService;

    @PostMapping
    @Operation(summary = "Analyze symptoms and recommend specializations",
            description = "Uses AI to suggest up to 3 medical specializations based on reported symptoms.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recommendations returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "503", description = "AI service unavailable")
    })
    public ResponseEntity<SymptomCheckResponseDto> analyzeSymptoms(
            @Valid @RequestBody SymptomCheckRequestDto request) {
        return ResponseEntity.ok(symptomCheckerService.analyze(request));
    }
}
