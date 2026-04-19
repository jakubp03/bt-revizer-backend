package com.opr3.opr3.dto.attempt;

import java.time.LocalDateTime;

import com.opr3.opr3.entity.attempt.QuizAttempt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttemptBasicResponse {
    private Long id;
    private LocalDateTime submittedAt;
    private double scorePercentage;
    private Integer timeSpent;

    public static AttemptBasicResponse from(QuizAttempt attempt) {
        return AttemptBasicResponse.builder()
                .id(attempt.getId())
                .submittedAt(attempt.getSubmittedAt())
                .scorePercentage(attempt.getScorePercentage())
                .timeSpent(attempt.getTimeSpent())
                .build();
    }
}
