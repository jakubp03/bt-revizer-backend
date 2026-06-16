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
public class AttemptSummaryResponse {
    private Long id;
    private LocalDateTime submittedAt;
    private double scorePercentage;
    private Integer timeSpent;
    private Long quizId;
    private String quizTitle;

    public static AttemptSummaryResponse from(QuizAttempt attempt) {
        return AttemptSummaryResponse.builder()
                .id(attempt.getId())
                .submittedAt(attempt.getSubmittedAt())
                .scorePercentage(attempt.getScorePercentage())
                .timeSpent(attempt.getTimeSpent())
                .quizId(attempt.getQuiz().getId())
                .quizTitle(attempt.getQuiz().getTitle())
                .build();
    }
}
